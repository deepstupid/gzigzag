;; vanishing.scm
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
;; Written by Tuomas Lukka and Rauli Ruohonen

(load "constraint.scm")

(java-funcs (align "align" 2) (fsize "fsize" 2))
(java-variables ("varsize" "boolean"))

(constants (x (1 0)) (y (0 1)) (depthmul 10))
(global-invars gfrac revper shrink0 shrink1 shrink2 shrink3 initmul)
(node-outvars vd d (aoffs 2) (v 2) (av 2) (rv 2) (rav 2) fract (size 2)
	      (gap 2))
(arc-sets (dim_0 (out_0 io jo) (pos_0 ip jp))
	  (dim_1 (out_1 io jo) (pos_1 ip jp))
	  (dim_2 (out_2 io jo) (pos_2 ip jp))
	  (dim_3 (out_3 io jo) (pos_3 ip jp)))

(global-constraints (= (av s) (0 0))
		    (= (fract s) initmul)
		    (= (d s) 1))
(node-constraints (= av (+ v aoffs))
		  (= rav (+ rv aoffs))
		  (= aoffs (java-call (call align i size)))
		  (= av (* revper rav))
		  (= size (java-call "if (varsize) " (call fsize i fract)
				     " else " (call fsize "null" fract)))
		  (= gap (* gfrac size))
		  (= vd (+ 1 (* depthmul (- d 1)))))
(arc-constraints (dim_0 (= (fract jo) (* shrink0 (fract io)))
			(= (d jo) (+ (d io) 1))
			(= (dot (rv jo) x)
			   (dot (+ (v io)
				   (* (- (* (eqdir? out_0 pos_0) 2) 1)
				      (+ (size ip) (gap io)))) x))
			(= (dot (rav jo) y) (dot (av io) y)))
		 (dim_1 (= (fract jo) (* shrink1 (fract io)))
			(= (d jo) (+ (d io) 1))
			(= (dot (rv jo) y)
			   (dot (+ (v io)
				   (* (- (* (eqdir? out_1 pos_1) 2) 1)
				      (+ (size ip) (gap io)))) y))
			(= (dot (rav jo) x) (dot (av io) x)))
		 (dim_2 (= (fract jo) (* shrink2 (fract io)))
			(= (d jo) (+ (d io) 1))
			(= (rv jo) (+ (v io)
				      (* (- (* (eqdir? out_2 pos_2) 2) 1)
					 (+ (size ip) (gap io))))))
		 (dim_3 (= (fract jo) (* shrink3 (fract io)))
			(= (d jo) (+ (d io) 1))
			(= (rv jo)
			   (+ (v io)
			      (* (- (* (eqdir? out_3 pos_3) 2) 1)
				 (* (1 -1) (+ (size ip) (gap io))))))))
(generate)
