/*   
Function.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
 *
 *    You may use and distribute under the terms of either the GNU Lesser
 *    General Public License, either version 2 of the license or,
 *    at your choice, any later version. Alternatively, you may use and
 *    distribute under the terms of the XPL.
 *
 *    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
 *    the licenses.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
 *    file for more details.
 *
 */
/*
 * Written by Tuomas Lukka
 */

package org.gzigzag;

/** A base class for referentially transparent functions of cell groups.
 * The idea of Function objects is to enable transparent optimization and caching
 * of values interpreted from the structure without losing clarity.
 * All Function objects <b>must</b> obey the following rule:
 * <pre>
 * 	For all Function f, Function g
 * 		if
 * 			f.equals(g)
 * 		then
 * 			For all X,
 * 				f(X).equals(g(X))
 * </pre>
 * Also, there must be absolutely NO side (detectable) side effects to
 * <pre>
 * 	For all Function f, Function g
 * 		For all X, Y
 * 			A = g(X)
 * 			B = f(Y)
 * 			C = g(X)
 * 			MUST RESULT IN A.equals(C)
 * </pre>
 * A side effect of caching some value <b>is</b> allowable if it does not alter
 * any evaluation results.
 * <p>
 * Functions are not allowed to modify the contents of their argument stepper.
 * <p>
 * This function class is the base. Due to Java's restrictions, we automatically
 * generate subclasses of the form Function_Type which have a method apply that 
 * returns an object of type Type.
 */
public interface Function {
    Object apply(Stepper s);
}
