;; util.scm
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

;; This is mzscheme-specific
(require-library "functio.ss")
(define (sort < l) (quicksort l <))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; General utilities
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Many of these could be implemented better, but I didn't bother ATM.

(define (perror . args)
  (display "Fatal error: ")
  (map display args) (newline)
  (error ""))

(define (=0 x) (= x 0))
(define (identity x) x)

(define true? identity)
(define false? not)
(define (true!) #t)
(define (false!) #f)

(define (all? f l)
  (cond ((null? l) #t)
	((not (f (car l))) #f)
	(else (all? f (cdr l)))))

(define (any? f l)
  (cond ((null? l) #f)
	((f (car l)) #t)
	(else (any? f (cdr l)))))

;; Returns (list (head l n) (tail l n))
(define (head-tail l n)
  (define (move l1 l2 n)
    (if (or (<= n 0) (null? l2)) (list (reverse l1) l2)
	(move (cons (car l2) l1) (cdr l2) (- n 1))))
  (move '() l n))

(define (map-car f l) (cons (map f (car l)) (cdr l)))
(define (map-cdr f l) (cons (car l) (map f (cdr l))))
(define (map-cadr f l) (cons (car l) (cons (map f (cadr l)) (cddr l))))
(define (map-cddr f l) (cons (car l) (map-cdr f (cdr l))))
(define (cat ls) (apply append ls))
(define (list-head l n) (car (head-tail l n)))

(define (filter retain? l)
  (cond ((null? l) '())
	((retain? (car l)) (cons (car l) (filter retain? (cdr l))))
	(else (filter retain? (cdr l)))))

(define (filter2 retain? l)
  (if (null? l) '(() ())
      (let* ((rec (filter2 retain? (cdr l))))
	(if (retain? (car l))
	    (list (cons (car l) (car rec)) (cadr rec))
	    (list (car rec) (cons (car l) (cadr rec)))))))

(define (foldl func init lst)
  (if (null? lst) init
      (foldl func (func init (car lst)) (cdr lst))))

(define (foldl1 func lst) (foldl func (car lst) (cdr lst)))

(define (zip . ls)
  (cond ((all? null? ls) '())
	((any? null? ls)
	 (perror "zip got lists of different lengths! " ls))
	(else (cons (map car ls) (apply zip (map cdr ls))))))

(define (ints s e)
  (define (iter s e l)
    (if (> s e) l
	(iter s (- e 1) (cons e l))))
  (if (> s e) '()
      (iter s (- e 1) (list e))))

;; Returns list like ((i1 i2 i3) delim1 (i4 i5) delim2 ...)
(define (split delim? lst)
  (define (iter res collect lst)
    (define (handle-collect!)
      (if (not (null? collect))
	  (set! res (cons (reverse collect) res))))
    (cond ((null? lst) (handle-collect!) (reverse res))
	  ((delim? (car lst))
	   (handle-collect!)
	   (iter (cons (car lst) res) '() (cdr lst)))
	  (else (iter res (cons (car lst) collect) (cdr lst)))))
  (iter '() '() lst))

(define (list-recurse internal leaf lst)
  (if (not (pair? lst)) (leaf lst)
      (map (curry list-recurse internal leaf) (internal lst))))

(define (list-flatten lst)
  (cond ((not (list? lst)) lst)
	((null? lst) '())
	((list? (car lst))
	 (list-flatten (append (car lst) (cdr lst))))
	(else (cons (car lst) (list-flatten (cdr lst))))))

(define (replace what with lst)
  (map (lambda (x) (if (equal? x what) with x)) lst))

(define (remove-first remove? lst)
  (cond ((null? lst) '())
	((remove? (car lst)) (cdr lst))
	(else (cons (car lst) (remove-first remove? (cdr lst))))))

(define (uniq = lst)
  (if (null? lst) '()
      (reverse (foldl (lambda (lst x)
			(if (= (car lst) x) lst
			    (cons x lst)))
		      (list (car lst))
		      (cdr lst)))))

(define (has-element? match? lst)
  (cond ((null? lst) #f)
	((match? (car lst)) #t)
	(else (has-element? match? (cdr lst)))))

(define (combine f g) (lambda x (f (apply g x))))
(define (curry f . args) (lambda x (apply f (append args x))))

(define (sane-substring str start end)
  (define len (string-length str))
  (if (> end len) (set! end len))
  (substring str start end))

(define (string-prefix? pref str)
  (string=? pref (sane-substring str 0 (string-length pref))))
(define (string-tokens seps str)
  (set! seps (string->list seps))
  (set! str (string->list str))
  (map list->string
       (filter list? (split (lambda (x) (any? (curry char=? x) seps)) str))))

(define (symbol-tokens seps sym)
  (map string->symbol
       (string-tokens (symbol->string seps) (symbol->string sym))))
(define (symbol< x y) (string-ci<? (symbol->string x) (symbol->string y)))
(define (number->symbol n) (string->symbol (number->string n)))
(define (symbol-append . s)
  (string->symbol (apply string-append (map symbol->string s))))
(define (symbol-prefix? pref sym)
  (string-prefix? (symbol->string pref) (symbol->string sym)))

;; Takes multisets like ((5 a) (10 b) (3 a)) ((6 a) ( 2 b)) and returns a
;; multiset like ( (14 a) (12 b) ). The amounts need not be integers.
;; < compares the cadrs, + combines the cars. zero? is for cars.
(define (multiset-combine < + zero? . msets)
  (define (do-combine to from)
    (cond ((null? from) to)
	  ((not (< (cadar to) (cadar from)))
	   (do-combine (cons (cons (+ (caar to) (caar from)) (cdar to))
			     (cdr to))
		       (cdr from)))
	  (else (do-combine (cons (car from) to) (cdr from)))))
  (define (combine l)
    (if (null? l) '()
	(do-combine (list (car l)) (cdr l))))
  (filter (lambda (x) (not (zero? (car x))))
	  (combine (sort (lambda (x y) (< (cadr x) (cadr y)))
			 (apply append msets)))))

(define (sorted-set-minus < a b)
  (cond ((or (null? a) (null? b)) a)
	((< (car b) (car a)) (sorted-set-minus < a (cdr b)))
	((< (car a) (car b))
	 (cons (car a) (sorted-set-minus < (cdr a) b)))
	(else (sorted-set-minus < (cdr a) b))))
(define (set-minus < a b)
  (sorted-set-minus < (sort < a) (sort < b)))

(define (topsort depends? lst)
  (define tbl (list->vector lst))
  (define size (vector-length tbl))
  (define fin (make-vector size #f))
  (define cntr 0)
  (define (dfs idx)
    (if (not (vector-ref fin idx))
	(let ((nod (vector-ref tbl idx)))
	  (vector-set! fin idx #t)
	  (for-each dfs (filter (lambda (i) (depends? nod (vector-ref tbl i)))
				(ints 0 (- size 1))))
	  (vector-set! fin idx cntr)
	  (set! cntr (+ cntr 1)))))
  (define tbl2 (make-vector size))
  (define (verify idx)
    (define (check id)
      (if (depends? (vector-ref tbl2 idx) (vector-ref tbl2 id))
	  (perror "topsort error: " idx " depends on " id "!")))
    (for-each check (ints (+ idx 1) (- size 1))))
  (for-each dfs (ints 0 (- size 1)))
  (for-each (curry apply (lambda (i j) (vector-set! tbl2 i (vector-ref tbl j))))
	    (zip (ints 0 (- size 1))
		 (sort (lambda (x y) (< (vector-ref fin x) (vector-ref fin y)))
		       (ints 0 (- size 1)))))
  (for-each verify (ints 0 (- size 1)))
  (vector->list tbl2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Hash tables
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; This is mzscheme-specific
(define (hashtbl-cons) (make-hash-table))
(define (hashtbl? h) (hash-table? h))
(define (hashtbl-define! h k v) (hash-table-put! h k v))
(define (hashtbl-set! h k v)
  (if (not (hashtbl-has-key? h k)) (error "Invalid hashtbl-set!")
      (hashtbl-define! h k v)))
(define (hashtbl-has-key? h k)
  (define test #t)
  (hash-table-get h k (lambda () (set! test #f)))
  test)
(define (hashtbl-get h k)
  (hash-table-get h k (lambda () (perror "Invalid hashtbl-get: " k))))
(define (hashtbl-remove! h k) (hash-table-remove! h k))
(define (hashtbl-keys h) (hash-table-map h (lambda (x y) x)))
(define (hashtbl-contents h) (hash-table-map h (lambda xy xy)))
(define (hashtbl-map h f)
  (define h2 (hashtable-cons))
  (map (lambda (k) (hashtbl-define! h2 k (f (hashtbl-get h k))))
       (hashtbl-keys h))
  h2)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Equation stuff.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (-eq-type? type eq) (and (pair? eq) (eq? (car eq) type)))
(define (-eq-body eq) (cdr eq))
(define (-eq-product-const-split x)
  (list (apply * (map eq-constant-value
		      (filter eq-constant? (-eq-body x))))
	(cons 'product (filter (lambda (x) (not (eq-constant? x)))
			       (-eq-body x)))))
(define (-eq-multiply-nonsum x y)
  (define (to-plist x) (if (eq-product? x) (cdr x) (list x)))
  (define (const-eq? x v) (and (eq-constant? x) (= v (eq-constant-value x))))
  (define (clean-product p)
    (define v (-eq-product-const-split p))
    (cond ((= (car v) 0) (eq-constant 0))
	  ((null? (cddr p)) (cadr p))
	  (else (cons 'product (cons (eq-constant (car v)) (cdadr v))))))
  (define (invclean inv)
    (cond ((const-eq? (cadr inv) 1) (eq-constant 1))
	  ((const-eq? (cadr inv) -1) (eq-constant -1))
	  (else inv)))
  (if (eq-inverse? x) (set! x (invclean x)))
  (if (eq-inverse? y) (set! y (invclean y)))
  (cond ((and (eq-constant? x) (eq-constant? y))
	 (eq-constant (* (eq-constant-value x) (eq-constant-value y))))
	((or (const-eq? x 0) (const-eq? y 0)) (eq-constant 0))
	((const-eq? x 1) y)
	((const-eq? y 1) x)
	((and (eq-inverse? x) (eq-inverse? y))
	 (invclean (list 'inverse (-eq-multiply-nonsum (cadr x) (cadr y)))))
	(else (clean-product
	       (cons 'product (append (to-plist x) (to-plist y)))))))

(define (eq-variable? eq) (-eq-type? 'variable eq))
(define (eq-constant? eq) (-eq-type? 'constant eq))
(define (eq-sum? eq) (-eq-type? 'sum eq))
(define (eq-product? eq) (-eq-type? 'product eq))
(define (eq-inverse? eq) (-eq-type? 'inverse eq))

(define (eq-variable sym) (list 'variable sym))
(define (eq-variable-sym var) (cadr var))

(define (eq-constant value) (list 'constant value))
(define (eq-constant-value const) (cadr const))

(define (eq-sum . eqs)
  (define (collapse eqs)
    (cond ((null? eqs) '())
	  ((eq-sum? (car eqs))
	   (append (-eq-body (car eqs)) (collapse (cdr eqs))))
	  (else (cons (car eqs) (collapse (cdr eqs))))))
  (define (collect terms)
    (define (constfact-split x)
      (cond ((eq-constant? x) (list (eq-constant-value x) (eq-constant 1)))
	    ((eq-product? x) (-eq-product-const-split x))
	    (else (list 1 x))))
    (map (lambda (x) (-eq-multiply-nonsum (eq-constant (car x)) (cadr x)))
	 (multiset-combine eq< + =0 (map constfact-split terms))))
  (define (kill-empty-sum s)
    (cond ((and (eq-sum? s) (null? (cdr s))) (eq-constant 0))
	  ((and (eq-sum? s) (null? (cddr s))) (cadr s))
	  (else s)))
  (kill-empty-sum (cons 'sum (collect (collapse eqs)))))

(define (eq-multiply . eqs)
  (define (multiply-sum l v) (map (lambda (x) (-eq-multiply-nonsum x v)) l))
  (define (sum-multiply l)
    (if (null? l) (list (eq-constant 1))
	(let ((r (sum-multiply (cdr l))))
	  (apply append (map (curry multiply-sum r) (car l))))))
  (apply eq-sum
	 (foldl multiply-sum
		(sum-multiply (map -eq-body (filter eq-sum? eqs)))
		(filter (lambda (x) (not (eq-sum? x))) eqs))))

(define (eq-variables eq)
  (cond ((not (list? eq)) '())
	((eq-variable? eq) (list eq))
	(else (cat (map eq-variables eq)))))

(define (eq< x y)
  (define (sp< a b)
    (cond ((and (null? a) (null? b)) #f)
	  ((null? a) #t)
	  ((null? b) #f)
	  ((eq< (car a) (car b)) #t)
	  ((eq< (car b) (car a)) #f)
	  (else (sp< (cdr a) (cdr b)))))
  (cond ((symbol< (car x) (car y)) #t)
	((symbol< (car y) (car x)) #f)
	((eq-constant? x) (< (eq-constant-value x) (eq-constant-value y)))
	((eq-variable? x) (symbol< (eq-variable-sym x) (eq-variable-sym y)))
	((or (eq-sum? x) (eq-product? x))
	 (sp< (sort eq< (cdr x)) (sort eq< (cdr y))))
	(else #f)))
(define (eq= x y) (and (not (eq< x y)) (not (eq< y x))))

;; x \subset ((eq0 0) (eq1 1) (eq2 2) ...)
;; Can't handle inverses.
(define (eq-degree-form eq var)
  ;; (eq degree-in-var)
  (define (var-split eq var)
    (cond ((eq= eq var) (list (eq-constant 1) 1))
	  ((eq-product? eq)
	   (list (apply eq-multiply
			(filter (lambda (x) (not (eq= x var))) (-eq-body eq)))
		 (length (filter (curry eq= var) (-eq-body eq)))))
	  (else (list eq 0))))
  (multiset-combine < eq-sum (curry eq= (eq-constant 0))
		    (if (eq-sum? eq)
			(map (lambda (x) (var-split x var)) (-eq-body eq))
			(list (var-split eq var)))))

(define (eq-cleanup eq) (eq-parse (eq-unparse eq)))

;; Expression with + * /. This does no simplification whatsoever.
;; Can't handle inverses.
(define (eq-firstdeg-solve eq var)
  (define (get-eq df deg)
    (define x (map car (filter (lambda (x) (= (cadr x) deg)) df)))
    (if (null? x) (eq-constant 0) (car x)))
  (define degform (eq-degree-form eq var))
  (define deg-0 (get-eq degform 0))
  (define deg-1 (get-eq degform 1))
  (cond
   ((and (eq= deg-1 (eq-constant 0)) (eq= deg-0 (eq-constant 0))) 0) ;; Id. true
   ((eq= deg-1 (eq-constant 0)) 'inconsistent) ;; Id. false
   ((eq= deg-0 (eq-constant 0)) 0)
   (else `(/ ,(eq-unparse (eq-multiply deg-0 (eq-constant -1)))
	     ,(eq-unparse deg-1)))))

;; XXX This might still be left in an infinite loop, I think.
(define (eq-mul-invs-out eq)
  (define (normal-form eq)
    (cons 'sum (map (lambda (x)
		      (if (eq-product? x) x
			  (list 'product x)))
		    (if (eq-sum? eq) (-eq-body eq)
			(list eq)))))
  (define (iter eq)
    (define (inv-in-prod? eq)
      (and (eq-product? eq)
	   (has-element? eq-inverse? eq)))
    (define invps1 (filter inv-in-prod? (-eq-body eq)))
    (if (null? invps1) eq
	(let* ((inv (car (filter eq-inverse? (car invps1))))
	       (invps (filter (lambda (prod) (member inv prod)) invps1))
	       (restp (filter (lambda (prod) (not (memq prod invps))) eq))
	       (prods
		(map (lambda (prod) (remove-first (curry equal? inv) prod))
		     invps)))
	  (iter (normal-form
		 (apply eq-sum (eq-multiply (cadr inv) restp) prods))))))
  (eq-cleanup (iter (normal-form eq))))

;; Parses things of form '(+ 5 a (* b 5 (/ a 6))).
(define (eq-parse eq)
  (cond ((number? eq) (eq-constant eq))
	((symbol? eq) (eq-variable eq))
	((not (pair? eq)) (error "Invalid equation!"))
	((null? eq) (eq-constant 0))
	((eq? (car eq) '+) (apply eq-sum (map eq-parse (cdr eq))))
	((eq? (car eq) '-)
	 (eq-sum (eq-parse (cadr eq))
		 (eq-multiply (eq-constant -1)
			      (apply eq-sum (map eq-parse (cddr eq))))))
	((eq? (car eq) '*) (apply eq-multiply (map eq-parse (cdr eq))))
	((eq? (car eq) '/) (eq-multiply (eq-parse (cadr eq))
					(list 'inverse (eq-parse (caddr eq)))))
	(else (error "eq-parse argument not an equation!"))))

(define (eq-unparse eq)
  (cond ((eq-constant? eq) (eq-constant-value eq))
	((eq-variable? eq) (eq-variable-sym eq))
	((eq-sum? eq) (cons '+ (map eq-unparse (cdr eq))))
	((eq-product? eq) (cons '* (map eq-unparse (cdr eq))))
	((eq-inverse? eq) (list '/ 1 (eq-unparse (cadr eq))))
	(else (error "eq-unparse argument not an equation!"))))

;; Parses things like '(+ 5 a) = '(+ 3 b)
(define (equation-parse left right)
  (eq-sum (eq-parse left) (eq-multiply (eq-constant -1) (eq-parse right))))

(define (eq-expr-to-java expr symbol->string)
  (define (recurse expr) (eq-expr-to-java expr symbol->string))
  (define (to-infix delim expr)
    (cond ((null? expr) "")
	  ((null? (cdr expr)) (recurse (car expr)))
	  (else (string-append (recurse (car expr)) delim
			       (to-infix delim (cdr expr))))))
  (cond ((and (number? expr) (>= expr 0)) (number->string expr))
	((and (number? expr) (< expr 0))
	 (string-append "(" (number->string expr) ")"))
	((symbol? expr) (symbol->string expr))
	((null? expr) "")
	((eq? (car expr) '+) (to-infix "+" (cdr expr)))
	((eq? (car expr) '-) (to-infix "-" (cdr expr)))
	((eq? (car expr) '*) (to-infix "*" (cdr expr)))
	((eq? (car expr) '/) (string-append "(" (recurse (cadr expr)) ") / ("
					    (recurse (caddr expr)) ")"))
	(else (error "Invalid expression given to eq-expr-to-java!"))))
