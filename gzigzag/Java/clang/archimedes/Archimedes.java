/*   
Archimedes.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2001, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.clang;
import org.gzigzag.*;

/** The Archimedes Procedural Layer executor/evaluator.
 */

public class Archimedes {
String rcsid = "$Id: Archimedes.java,v 1.3 2001/04/13 22:42:51 bfallenstein Exp $";

    /** Evaluate the given expression in the given namespace. */
    static public ZZCell evaluateExpression(Expression e, Namespace context) {
	Callable c = Callable.get(e.main);
	return c.evaluate(e, context);
    }

    /** Get the value associated with param, possibly evaluating param first.
     */
    public static ZZCell getValue(ZZCell param, Namespace context) {
	ZZCell exp = param.h("d.expression", true);
	if(exp == null)
	    return param;
	else if(Namespace.isVariable(exp)) {
	    ZZCell c = context.get(exp);
	    if(c == null) throw new ZZError("Reference to variable not in " +
					    "stackframe. Variable: "+param);
	    return c;
	} else
	    return Archimedes.evaluateExpression(new Expression(exp), context);
    }

    /** Same as getValue(), but will never do lazy evaluation.
     *  Currently there's no difference.
     */
    public static ZZCell execute(ZZCell param, Namespace context) {
	return getValue(param, context);
    }

    /** Copy the value refered by source into target. */
    public static final void copy(ZZCell source, ZZCell target) {
	ZZCell val = ZZCursorReal.get(source);
	if(val != null)
	    ZZCursorReal.set(target, val);
	else
	    target.setText(source.getText());
    }

    /** Create a new cell in the same slice as c and copy c's value into it. */
    public static final ZZCell copy(ZZCell c) {
	ZZCell res = c.N();
	copy(c, res);
	return res;
    }

}


