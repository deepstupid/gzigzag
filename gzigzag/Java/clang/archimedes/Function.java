/*   
Function.java
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

/** An Archimedes function object used to modify Archimedes funcs easily.
 */

public class Function extends Callable {
String rcsid = "$Id: Function.java,v 1.4 2001/04/15 11:33:57 bfallenstein Exp $";

    public ZZCell main;
    private Expression exp;

    /** Create a Function object from an existing Function maincell. */
    public Function(ZZCell main) { this.main = main; getExpression(); }

    /** Create a Function object from scratch and name it. */
    public Function(ZZSpace sp, String name, Expression exp) {
	ZZCell home = sp.getHomeCell();
	main = home.N();
	main.setText(name);
	exp.connect(main);
	this.exp = exp;
    }

    /** Create a Function object from scratch without naming it. */
    public Function(ZZSpace sp, Expression exp) { this(sp, "", exp); }
    
    /** Get the expression defining this function object. */
    public Expression getExpression() {
	ZZCell def = main.h("d.expression", true);
	if(def == null)
	    throw new MissingTermError("No expression connected to "
				     + "this function!");
	if(exp == null || !exp.main.equals(def))
	    exp = new Expression(def);
	return exp;
    }

    public ZZCell getParam(String dim, int steps) {
	int dir, n;

	if(steps > 0) { dir = 1; n = steps; }
	else if(steps < 0) { dir = -1; n = -steps; }
	else return main;
	
	ZZCell c = main;
	for(int i=0; i<n; i++)
	    c = c.getOrNewCell(dim, dir);

	return c;
    }

    public ZZCell evaluate(Expression e, Namespace context) {
	Namespace subcontext = context.create();
	Parser.parse(main, e.main, context, subcontext);
	return Archimedes.evaluateExpression(exp, subcontext);
    }

}


