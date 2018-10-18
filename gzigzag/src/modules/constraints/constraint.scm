;; constraint.scm
;;
;;    You may use and distribute under the terms of either the GNU Lesser
;;    General Public License, either version 2 of the license or,
;;    at your choice, any later version. Alternatively, you may use and
;;    distribute under the terms of the XPL.
;;
;;    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
;;    the licenses.
;;
;;    This software is distributed in the hope that it will be useful,
;;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
;;    file for more details.
;;
;; Written by Rauli Ruohonen

(load "util.scm")

(define (uniq-symbol l) (uniq eq? (sort symbol< l)))
(define (split-var sym) (symbol-tokens '! sym))
(define (join-var var)
  (foldl1 (lambda (x y) (symbol-append x '! y)) (filter true? var)))
(define (varsymmangle2 sym . mapping)
  (define split (split-var sym))
  (define inames (map (lambda (i) (symbol-append '! (number->symbol i)))
		      (ints 1 (length mapping))))
  (define (do-replace repls)
    (if (not (null? repls))
	(begin (set! split (replace (caar repls) (cadar repls) split))
	       (do-replace (cdr repls)))))
  (do-replace (zip (map car mapping) inames))
  (do-replace (zip inames (map cadr mapping)))
  (join-var split))
(define (varsymmangle s i j sym)
  (varsymmangle2 sym (list 's s) (list 'i i) (list 'j j)))
(define (varmangle2 eqs . mapping)
  (list-recurse
   (lambda (v)
     (if (eq-variable? v)
	 (eq-variable (apply varsymmangle2 (eq-variable-sym v) mapping))
	 v))
   identity
   eqs))
(define (varmangle s i j eqs)
  (varmangle2 eqs (list 's s) (list 'i i) (list 'j j)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The core constraint system
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define cs-invars '())
(define cs-depvars '())
(define cs-global-constr '())
(define cs-node-constr '())
(define cs-arc-constr '())
(define cs-arc-sets '())

(define (cs-global-constraint constr)
  (set! cs-global-constr (cons constr cs-global-constr)))
(define (cs-node-constraint constr)
  (set! cs-node-constr (cons constr cs-node-constr)))
(define (cs-arc-constraint rel constrs)
  (set! cs-arc-constr (cons (cons rel constrs) cs-arc-constr)))
(define (cs-var-depends var vars)
  (set! cs-depvars (cons (list var (uniq-symbol vars)) cs-depvars)))
(define (cs-input-vars . vars) (set! cs-invars (append vars cs-invars)))
(define (cs-arcsets sets) (set! cs-arc-sets sets))

(define (cs-eqvars eq) (uniq-symbol (map eq-variable-sym (eq-variables eq))))

(define (cs-project var eqs)
  (define match (filter (lambda(eq) (any? (curry eq? var) (cs-eqvars eq))) eqs))
  (if (null? match) eqs
      (let* ((f (car match))
	     (sol (eq-parse (eq-firstdeg-solve f (eq-variable var)))))
	(map (lambda (eq)
	       (eq-mul-invs-out
		(eq-cleanup
		 (list-recurse (curry replace (eq-variable var) sol)
			       identity eq))))
	     (remove-first (curry eq? f) eqs)))))

(define (cs-lineqs-solve eqs constant?)
  (define (eqvars constant? eq) (filter (combine not constant?) (cs-eqvars eq)))
  (define (eqsvars constant? eqs)
    (uniq-symbol (cat (map (curry eqvars constant?) eqs))))
  (define (plistgen vars eqs)
    (cond ((null? vars) '())
	  ((not (memq (car vars) (eqsvars constant? eqs)))
	   (plistgen (cdr vars) eqs))
	  (else (let ((x (cs-project (car vars) eqs)))
		  (cons (list (car vars) x)
			(plistgen (cdr vars) x))))))
  (define (skew lst)
    (define (do-skew prop lst)
      (if (null? lst) '()
	  (cons (list prop (cadar lst)) (do-skew (caar lst) (cdr lst)))))
    (if (null? lst) '()
	(do-skew (caar lst) (cdr lst))))
  (define (firstdeg-solve eq var)
    (eq-unparse (eq-parse (eq-firstdeg-solve eq var))))
  (define (compile constant? plist)
    (if (null? plist) '()
	(let* ((var (caar plist))
	       (-eqs (cadar plist))
	       (eq (car (filter (lambda (e) (memq var (cs-eqvars e))) -eqs)))
	       (eqs (remove-first (curry eq? eq) -eqs))
	       (ambs (filter (lambda (x) (not (eq? var x)))
			     (eqvars constant? eq)))
	       (nconstant? (lambda (v) (or (eq? v var) (constant? v)
					   (memq v ambs))))
	       (code (append (map (lambda (v) `(= ,v whatever)) ambs)
			     `((= ,var
				  ,(firstdeg-solve eq (eq-variable var)))))))
	  (append code (compile nconstant? (cdr plist))))))
  (define porder (eqsvars constant? eqs))
  (define plist (skew (reverse (cons (list #f eqs) (plistgen porder eqs)))))
  (define compiled (compile constant? plist))
  (define newline (string #\newline))
  (if (not (null? (filter (curry eq? 'whatever) (list-flatten compiled))))
      (perror newline "Under-constrained: " (map eq-unparse eqs) newline newline
	      "Compiled: " compiled newline)
      compiled))

(define (cs-solve)
  (define (constant? var)
    (or (any? (lambda (x) (symbol-prefix? x var)) cs-invars)
	(any? (lambda (x) (symbol-prefix? (car x) var)) cs-depvars)))
  (define (solved-vars sol) (map cadr sol))
  (define (node-constr i) (varmangle '? i '? cs-node-constr))
  (define (propagate i-known arcseteqs)
    (define asets (cdr (assq (car arcseteqs) cs-arc-sets)))
    (define eqs (cdr arcseteqs))
    (define (one-way dirs)
      (define ij-map
	(cat (map (curry apply (lambda (dir arcset)
				 (let* ((ai (cadr arcset))
					(aj (caddr arcset)))
				   (if (eq? dir '->)
				       `((,ai i) (,aj j))
				       `((,ai j) (,aj i))))))
		  (zip dirs asets))))
      (define (rewrite-equal sym)
	(if (not (symbol-prefix? 'equal@ sym)) (eq-variable sym)
	    (let* ((split (cdr (symbol-tokens '@ sym)))
		   (ijlist (map (lambda (ij) (cadr (assq ij ij-map))) split)))
	      (if (or (all? (lambda (x) (eq? x 'i)) ijlist)
		      (all? (lambda (x) (eq? x 'j)) ijlist))
		  (eq-constant 1) (eq-constant 0)))))
      (define (rewrite-equals eqs)
	(list-recurse
	 (lambda (v)
	   (if (eq-variable? v)
	       (rewrite-equal (eq-variable-sym v))
	       v))
	 identity
	 eqs))
      (define ij-rules (rewrite-equals (apply varmangle2 eqs ij-map)))
      (define (const? v) (or (constant? v) (memq v i-known)))
      (define sol (cs-lineqs-solve (append ij-rules (node-constr 'j)) const?))
      (define solved (solved-vars sol))
      (define unsolved (set-minus
			symbol< (map (curry varsymmangle '? 'j '?) i-known)
			solved))
      sol)
    (define (combinations n)
      (cond ((< n 1) (perror "(combinations " n ") called!"))
	    ((= n 1) '((->) (<-)))
	    (else (let* ((rec (combinations (- n 1)))
			 (recf (map (lambda (x) (cons '-> x)) rec))
			 (recb (map (lambda (x) (cons '<- x)) rec)))
		    (append recf recb)))))
    (cons (map car asets)
	  (map (lambda (x) (list x (one-way x)))
	       (combinations (length asets)))))
  
  (define init (cs-lineqs-solve (append cs-global-constr (node-constr 'i))
				constant?))
  (define arc
    (map (lambda (acs)
	   (propagate (solved-vars init)
		      (cons
		       (car acs)
		       (cat
			(map cdr
			     (filter (lambda (x) (eq? (car x) (car acs)))
				     cs-arc-constr))))))
	 cs-arc-sets))
  ;(newline)
  (append init arc))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The constraint language
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define cl-csys (hashtbl-cons))
(define cl-consts (hashtbl-cons))
(define cl-vars (hashtbl-cons))
(define cl-javafuncs (hashtbl-cons))
(define cl-javavars (hashtbl-cons))
(define cl-java-vars '())
(define cl-javavaridx 1)
(define cl-arcsets '())

(define (getdef str) (hashtbl-get cl-csys str))
(define (print . things) (for-each display things))

(define (generate)
  (define (input-vars)
    (map car (filter (lambda (x) (eq? (caddr x) 'in))
		     (map (lambda (x) (cons (car x) (cadr x)))
			  (hashtbl-contents cl-vars)))))
  (define (java-depvars)
    (map (lambda (v)
	   (list (car v)
		 (filter (lambda (y)
			   (and (symbol? y)
				(hashtbl-has-key? cl-vars
						  (car (symbol-tokens '% y)))))
			 (cat
			  (map cddr
			       (filter (lambda (y)
					 (and (pair? y) (eq? (car y) 'call)))
				       (cadr v)))))))
	 (hashtbl-contents cl-javavars)))
  (define (java-compile sol)
    (define newline (string #\newline))
    (define (split2-var sym) ;; size sym sij
      (define spl (split-var sym))
      (define tok (symbol-tokens '% (car spl)))
      (list (cadr tok) (car tok) (apply symbol-append (cdr spl))))
    (define (var->string var)
      (define (real-var->string var)
	(define spl (split2-var var))
	(define tail (string-append (symbol->string (cadr spl))
				    (symbol->string (car spl))))
	(define head (symbol->string (caddr spl)))
	(if (not (string=? head "")) (set! head (string-append head ".")))
	(string-append head tail))
      (if (number? var) (string-append (number->string var) "f")
	  (real-var->string var)))
    (define (compile-expr expr)
      (define (comp expr)
	(cond ((number? expr) (string-append (number->string expr) "f"))
	      ((symbol? expr) (var->string expr))
	      ((not (pair? expr)) (perror "Invalid expression: " expr))
	      ((null? (cddr expr)) (comp (cadr expr)))
	      ((null? (cdddr expr))
	       (string-append "(" (comp (cadr expr))
			      (symbol->string (car expr))
			      (comp (caddr expr)) ")"))
	      (else
	       (string-append "(" (comp (cadr expr))
			      (symbol->string (car expr))
			      (comp (cons (car expr) (cddr expr)))
			      ")"))))
      (string-append (comp expr) ";" newline))
    (define (compile-assign assign)
      (string-append (var->string (cadr assign))
		     " = "
		     (compile-expr (caddr assign))))
    (define (compile-javavar i j var)
      (define (interp x)
	(define func (hashtbl-get cl-javafuncs (cadr x)))
	(define args (cddr x))
	(define (arg->string arg)
	  (cond ((string? arg) arg)
		((memq arg '(i j))
		 (symbol->string (cadr (assq arg `((i ,i) (j ,j))))))
		(else (var->string (varsymmangle '? i j arg)))))
	(define (call func)
	  (string-append
	   func "("
	   (foldl1 (lambda (x y) (string-append x ", " y))
		  (map arg->string args))
	   "); "))
	(define nod (cadr (hashtbl-get cl-javavars var)))
	(if (eq? nod 'i) (set! nod i)
	    (if (eq? nod 'j) (set! nod j)))
	(set! nod (symbol->string nod))
	(if (not (string=? nod "")) (set! nod (string-append nod ".")))
	(string-append
	 "{ "
	 (apply string-append
		(map (lambda (n)
		       (string-append
			nod (symbol->string var) (number->string n) " = "
			(call (string-append (car func) (number->string n)))))
		     (ints 1 (cadr func))))
	 "}"))
      (define code (cdar (hashtbl-get cl-javavars var)))
      (string-append
       (apply string-append (map (lambda (x) (if (list? x) (interp x) x)) code))
       newline))
    (define (indfun n)
      (define str (make-string n #\space))
      (lambda x (apply string-append str x)))
    (define ind (indfun 0))
    (define ind2 (indfun 2))
    (define ind3 (indfun 4))
    (define ind4 (indfun 6))
    (define ind5 (indfun 8))
    (define global (filter (lambda (x) (eq? (car x) '=)) sol))
    (define global-jvs
      (uniq-symbol
       (map (combine cadr split2-var)
	    (filter (lambda (x) (symbol-prefix? 'javavar x))
		    (filter symbol? (list-flatten global))))))
    (define globvars ;; ( (name size) ... )
      (map (lambda (v) (list (symbol->string (car v)) (caddr (cadr v))))
	   (append
	    (filter (combine (curry eq? 'global) caadr)
		    (hashtbl-contents cl-vars))
	    (filter (lambda (x) (eq? (string->symbol "") (cadr (cadr x))))
		    (hashtbl-contents cl-javavars)))))
    (define nodvars
      (map (lambda (v) (list (symbol->string (car v)) (caddr (cadr v))))
	   (append
	    (filter (combine (curry eq? 'node) caadr)
		    (hashtbl-contents cl-vars))
	    (filter (lambda (x) (eq? 'i (cadr (cadr x))))
		    (hashtbl-contents cl-javavars)))))
    (define asnames
      (map symbol->string (cat (map (lambda (x) (map car (cdr x)))
				    cl-arcsets))))
    (define (tprint ind i j jvars assigns)
      (define (var? x) (and (symbol? x) (not (memq x '(+ - / * = s i j)))))
      (define (demvars x)
	(if (symbol? x)
	    (map
	     (curry varsymmangle '? i j)
	     (filter
	      var?
	      (cat
	       (map cddr
		    (filter (lambda (x) (and (pair? x) (eq? (car x) 'call)))
			    (car (hashtbl-get cl-javavars x)))))))
	    (filter var? (list-flatten (list (caddr x))))))
      (define (suppvars x)
	(if (symbol? x)
	    (map
	     (lambda (k)
	       (define n (symbol-append x '% (number->symbol k)))
	       (define asso (cadr (hashtbl-get cl-javavars x)))
	       (if (eq? asso 'i) (set! asso i))
	       (if (eq? asso (string->symbol "")) n
		   (symbol-append n '! asso)))
	     (ints 1 (caddr (hashtbl-get cl-javavars x))))
	    (list (cadr x))))
      (define (depends? a b)
	(define supp (suppvars b))
	(define dem (demvars a))
	(define result (has-element? (lambda (e) (memq e supp)) dem))
	;(display "depends: a = ") (display a) (display ", b = ") (display b)
	;(display newline) (display "  supp(b) = ") (display supp)
	;(display ", dem(a) = ") (display dem)
	;(display ", result: ") (display result) (display newline)
	result)
      (define order (topsort depends? (append jvars assigns)))
      (define (compile i j x)
	(string-append (ind)
		       (if (symbol? x) (compile-javavar i j x)
			   (compile-assign x))))
      (apply string-append (map (curry compile i j) order)))
    (define procs '())
    (define (rel-loop-dir arc dir arcn)
      (define -ind ind2)
      (define -ind2 ind3)
      (define -ind3 ind4)
      (define -ind4 ind5)
      (define (ifdir arc)
	(define dirs
	  (map (lambda (d) (cadr (assq d '((-> "true") (<- "false")))))
	       (cdar arc)))
	(define assigns (cadr arc))
	(define jvars
	  (uniq-symbol
	   (map (combine cadr split2-var)
		(filter (lambda (x) (and (symbol-prefix? 'javavar x)
					 (eq? (caddr (split2-var x)) 'j)))
			(filter symbol? (list-flatten assigns))))))
	(string-append
	 (-ind2) "if ("
	 (foldl1 (lambda (x y) (string-append x " && " y))
		 (map (lambda (x) (string-append "dir" (number->string (car x))
						 " == " (cadr x)))
		      (zip (ints 2 (+ (length dirs) 1)) dirs)))
	 ") {" newline
	 (tprint -ind3 'j '? jvars assigns)
	 (-ind2) "}" newline))
      (define dirsym (if (string=? dir "1") '-> '<-))
      (define procname (string-append "proc" (if (string=? dir "1") "p" "n")
				      (number->string arcn)))
      (define (defproc)
	(string-append
	 (-ind) "for(int ptr = 0; ptr < js.length; ptr++) {" newline
	 (-ind2) "Node j = js[ptr];" newline
	 (-ind2) "boolean "
	 (foldl1 (lambda (x y) (string-append x ", " y))
		 (map (lambda (x)
			(string-append "dir" (number->string x) " = false"))
		      (ints 2 (length (car arc))))) ";" newline
         (-ind2) "if (black.get(j) != null) continue;" newline
	 (apply
	  string-append
	  (map (lambda (n)
		 (define ns (number->string n))
		 (define js (string-append "js" ns))
		 (string-append
		  (-ind2)"for (int idx = 0; idx < " js ".length; idx++)" newline
		  (-ind3) "if (" js "[idx].equals(j)) { dir" ns " = true; "
		  "break; }" newline))
	       (ints 2 (length (car arc)))))
	 (apply
	  string-append
	  (map ifdir (filter (lambda (a) (eq? (caar a) dirsym)) (cdr arc))))
	 (-ind2) "if (j.valid()) { black.put(j, j); grey.add(j); }" newline
	 (-ind) "}" newline))
      (set! procs (cons (list procname (cdar arc) (defproc)) procs))
      (string-append
       procname "(i, i.s(" (symbol->string (caar arc)) ", " dir ")"
       (apply string-append
	      (map(lambda (x)(string-append ", i.s(" (symbol->string x) ", 1)"))
			   (cdar arc)))
       ");"))
    (define arc (filter (lambda (x) (not (eq? (car x) '=))) sol))
    (define (rel-loop arcn)
      (define -arc (list-ref arc arcn))
      (string-append
       (ind3) (rel-loop-dir -arc "1" arcn) newline
       (ind3) (rel-loop-dir -arc "-1" arcn) newline))
    (define (print-proc proc)
      (string-append
       "private final void " (car proc)
       "(Node i, Node[] js"
       (apply string-append
	      (map (lambda (n) (string-append ", "
					      "Node[] js" (number->string n)))
		   (ints 2 (+ 1 (length (cadr proc))))))
       ") {" newline
       (caddr proc)
       "}" newline))
    (define (var< a b) (string<? (car a) (car b)))
    (print (ind) "public abstract class Node {" newline
	   (ind2) "public float ")
    (print
     (foldl1 (lambda (x y) (string-append x ", " y))
	     (cat (map (lambda (v)
			 (map (combine (curry string-append (car v))
				       number->string)
			      (ints 1 (cadr v)))) nodvars))))
    (print ";" newline 
	   (ind2) "public abstract boolean equals(Object c);" newline
	   (ind2) "public abstract int hashCode();" newline
	   (ind2) "public abstract Node[] s(int rel, int dir);" newline
	   (ind2) "public abstract boolean valid();" newline
	   (ind) "}" newline)
    (print (ind) "public final int "
     (foldl1 (lambda (x y) (string-append x ", " y))
	     (map (lambda (x) (string-append (car x) " = "
					     (number->string (cadr x))))
		  (zip asnames (ints 1 (length asnames)))))
     ";" newline)
    (apply print
	   (map (curry apply (lambda (n t)
			       (string-append (ind) "public " t " " n ";"
					      newline)))
		cl-java-vars))
    (if (not (null? globvars))
	(begin
	  (print (ind) "public float ")
	  (print
	   (foldl1 (lambda (x y) (string-append x ", " y))
		   (cat (map (lambda (v)
			       (map (combine (curry string-append (car v))
					     number->string)
				    (ints 1 (cadr v))))
			     (sort var< globvars)))))
	  (print ";" newline)))
    (print (ind) "public HashMap black;" newline)
    (print (ind) "public LinkedList grey;" newline)
    (print (ind) "public final void solve(Node i) {"
	   #\newline)
    (print (ind2) "black = new HashMap();" newline)
    (print (ind2) "grey = new LinkedList();" newline)
    (print (tprint ind2 'i 'j global-jvs global))
    (print (ind2) "if (i.valid()) { black.put(i,i); grey.add(i); }" newline)
    (print (ind2) "while (grey.size() > 0) {" newline
	   (ind3) "i = (Node)grey.removeFirst();" newline)
    (for-each (combine print rel-loop) (ints 0 (- (length arc) 1)))
    (print (ind2) "}" newline
	   (ind2) "black = null; grey = null;" newline
	   (ind) "}" newline)
    (for-each (combine print print-proc) (reverse procs)))
  (apply cs-input-vars (input-vars))
  (map (curry apply cs-var-depends) (java-depvars))
  (cs-arcsets cl-arcsets)
  (java-compile (cs-solve)))

(define (cl-defvars scope io lst)
  (define (canonize v) (if (symbol? v) (list v 1) v))
  (define (name v) (car (canonize v)))
  (define (size v) (cadr (canonize v)))
  (map (lambda (v) (hashtbl-define! cl-vars (name v) (list scope io (size v))))
       lst))

(define (cl-defmisc what lst)
  (define (constdef name value) (hashtbl-define! cl-consts name value))
  (define (javafuncdef name javaname retsize)
    (hashtbl-define! cl-javafuncs name (list javaname retsize)))
  (case what
    ((const) (map (curry apply constdef) lst))
    ((java-func) (map (curry apply javafuncdef) lst))
    ((java-var) (set! cl-java-vars (append lst cl-java-vars)))
    ((arcset) (set! cl-arcsets lst))
    (else (begin
	    (display "defmisc: ") (display what)
	    (display ": ") (display lst) (newline)))))

(define (cl-constraints type lst)
  (define (vector->scalar lst)
    (case type
      ((global node) (cat (map (curry cl-constr->cs type) lst)))
      ((arc) (map (lambda (x)
		    (let* ((ht (head-tail x 1))
			   (h (car ht))
			   (t (cadr ht)))
		      (append h (cat (map (curry cl-constr->cs type) t)))))
		  lst))
      (else (perror "Illegal constraint type: " type))))
  (set! lst (vector->scalar lst))
  (case type
    ((global) (for-each cs-global-constraint lst))
    ((node) (for-each cs-node-constraint lst))
    ((arc)
     (for-each (lambda (constr)
		 (let ((rel (car constr))
		       (constrs (cdr constr)))
		   (cs-arc-constraint rel constrs)))
	       lst))))

(define (cl-constr->cs context constr)
  (define (clean exp)
    (define (expand expr)
      (cond ((number? expr) (list expr))
	    ((hashtbl-has-key? cl-vars expr)
	     (cons expr (cdr (assq (car (hashtbl-get cl-vars expr))
				   '((global) (node i) (arc i j))))))
	    ((hashtbl-has-key? cl-consts expr)
	     (expand (hashtbl-get cl-consts expr)))
	    ((and (pair? expr) (eq? (car expr) 'eqdir?))
	     (list
	      (foldl (lambda (x y) (symbol-append x '@ y))
		     'equal
		     (map (lambda (x)
			    (cadar (filter (lambda (y) (eq? x (car y)))
					   (cat (map cdr cl-arcsets)))))
			  (cdr expr)))))
	    (else expr)))
    (define expr (expand exp))
    (cond ((null? expr) '())
	  ((or (number? (car expr)) (hashtbl-has-key? cl-vars (car expr))
	       (eq? (car expr) 'java-call) (string? expr)) expr)
	  (else (cons (car expr) (map clean (cdr expr))))))
  (define (vec-combine op l r) (map (lambda (x) (cons op x)) (zip l r)))
  (define (dot-combine foo l r) (list (cons '+ (vec-combine '* l r))))
  (define (scalvec-combine op l r)
    (define (scalar? x) (null? (cdr x)))
    (define (doit l r) (map (lambda (x) (append (list op) l (list x))) r))
    (cond ((scalar? l) (doit l r))
	  ((scalar? r) (doit r l))
	  (else (vec-combine op l r))))
  (define (varpartnames base n)
    (map (lambda (i) (symbol-append base '% (number->symbol i)))
	 (ints 1 n)))
  (define (varparts-var expr)
    (define name (car expr))
    (define names (varpartnames name (caddr (hashtbl-get cl-vars name))))
    (map (lambda (name) (cons name (cdr expr))) names))
  (define (varparts expr)
    (cond ((number? (car expr)) expr)
	  ((symbol-prefix? 'equal@ (car expr)) expr)
	  (else (varparts-var expr))))
  
  (define (constant? e) (number? (car e)))
  (define (variable? e)
    (or (hashtbl-has-key? cl-vars (car e))
	(symbol-prefix? 'equal@ (car e))))
  (define (split expr)
    (define combiner `((+ ,vec-combine +)
		       (- ,vec-combine -)
		       (dot ,dot-combine foo)
		       (* ,scalvec-combine *)))
    (define (java-call-size expr)
      (apply max (map (lambda (x)
			(if (and (list? x) (eq? (car x) 'call))
			    (cadr (hashtbl-get cl-javafuncs (cadr x)))
			    0)) expr)))
    (define (new-java-call expr size)
      (define name (symbol-append 'javavar (number->symbol cl-javavaridx)))
      (define (clean-calls expr)
	(define (splitty expr)
	  (if (not (list? expr)) (list expr)
	      (map varclean (varparts expr))))
	(map (lambda (x)
	       (if (and (pair? x) (eq? (car x) 'call))
		   (cons (car x) (cons (cadr x)
				       (cat (map splitty (clean (cddr x))))))
		   x))
	     expr))
      (hashtbl-define! cl-javavars name (list (clean-calls expr)
					      (if (eq? context 'global)
						  (string->symbol "") 'i)
					      size))
      (set! cl-javavaridx (+ cl-javavaridx 1))
      name)
    (cond ((constant? expr) expr)
	  ((variable? expr) (varparts expr))
	  ((not (list? expr)) (perror "Unknown expr in split: " expr))
	  ((eq? (car expr) 'java-call)
	   (let* ((size (java-call-size expr))
		  (jname (new-java-call expr size))
		  (asso (case context
			  ((global) (string->symbol ""))
			  ((node) '!i)
			  ((arc) (perror "java-call in arc context!")))))
	     (map (lambda (x) (symbol-append x asso))
		  (varpartnames jname size))))
	  (else (foldl1 (apply curry (cdr (assq (car expr) combiner)))
			(map split (cdr expr))))))
  (define (varclean expr)
    (cond ((not (list? expr)) expr)
	  ((memq (car expr) '(= * + -))
	   (cons (car expr) (map varclean (cdr expr))))
	  (else (foldl1 (lambda (x y) (symbol-append x '! y))
			(if (eq? context 'global)
			    (replace 's 'i expr)
			    expr)))))
  (define (to-eq expr)
    (equation-parse (cadr expr) (caddr expr)))

  ;(display "Cleaned: (= ") (display (clean (cadr constr)))
  ;(display " ") (display (clean (caddr constr))) (display ")") (newline)
  (if (eq? (car constr) '=)
      (let ((left (split (clean (cadr constr))))
	    (right (split (clean (caddr constr)))))
	(map (combine to-eq varclean) (vec-combine '= left right)))))

;; These are MzScheme-specific hacks.

(define-macro global-invars (lambda vars (cl-defvars 'global 'in vars) #f))
(define-macro global-outvars (lambda vars (cl-defvars 'global 'out vars) #f))
(define-macro node-invars (lambda vars (cl-defvars 'node 'in vars) #f))
(define-macro node-outvars (lambda vars (cl-defvars 'node 'out vars) #f))
;; These don't work.
;(define-macro arc-invars (lambda vars (cl-defvars 'arc 'in vars) #f))
;(define-macro arc-outvars (lambda vars (cl-defvars 'arc 'out vars) #f))
(define-macro arc-sets (lambda lst (cl-defmisc 'arcset lst) #f))
(define-macro java-funcs (lambda lst (cl-defmisc 'java-func lst) #f))
(define-macro java-variables (lambda lst (cl-defmisc 'java-var lst) #f))
(define-macro constants (lambda lst (cl-defmisc 'const lst) #f))
(define-macro global-constraints (lambda lst (cl-constraints 'global lst) #f))
(define-macro node-constraints (lambda lst (cl-constraints 'node lst) #f))
(define-macro arc-constraints (lambda lst (cl-constraints 'arc lst) #f))
