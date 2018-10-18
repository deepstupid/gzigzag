/*   
Expression.java
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

/** An Archimedes expression object used to modify Archimedes exps easily.
 */

public class Expression {
String rcsid = "$Id: Expression.java,v 1.5 2001/04/16 10:37:59 bfallenstein Exp $";

    public ZZCell main;

    /** Create an expression object from an existing expression maincell. */
    public Expression(ZZCell main) {
	if(main.s("d.expression", -1) != null)
	    throw new ZZError("Tried to create Expression from a cell which "+
			      "is not headcell on d.expression! Cell: "+main);
	this.main = main;
    }

    /** Create an expression object from scratch. */
    public Expression(ZZSpace sp, String primitive) {
	main = AllPrimitives.getCell(sp, primitive).zzclone();
    }

    /** Create an expression object from scratch. 
     *  @param root The rootclone for the new expression.
     */
    public Expression(ZZSpace sp, ZZCell root) {
	main = root.zzclone();
    }

    /** Create an expression object from scratch. 
     */
    public Expression(ZZSpace sp, Function fn) {
	main = fn.main.zzclone();
    }

    /** Get the function at the center of the expression.
     */
    public ZZCell getFunction() {
	return main;
    }

    /** Create a subexpression and connect it to a parameter.
     *  This looks up the specified param and connects a new expression to it.
     */
    public Expression create(String dim, int steps, String primitive) {
	ZZCell root = AllPrimitives.getCell(main.getSpace(), primitive);
	return create(dim, steps, root);
    }

    /** Create a subexpression and connect it to a parameter.
     *  This looks up the specified param and connects a new expression to it.
     *  @param root The rootclone for the new expression.
     */
    public Expression create(String dim, int steps, ZZCell root) {
	ZZCell c = getParam(dim, steps);
	if(c.s("d.expression", -1) != null) c.excise("d.expression");
	ZZCell exp = root.zzclone();
	exp.insert("d.expression", 1, c);
	return new Expression(exp);
    }

    /** Create a subexpression and connect it to a parameter.
     *  This looks up the specified param and connects a new expression to it.
     *  @param root The function to put at the center of the new expression.
     */
    public Expression create(String dim, int steps, Function fn) {
	return create(dim, steps, fn.main);
    }


    /** Set the integer value of a parameter.
     */
    public void set(String dim, int steps, int value) {
	ZZCell c = getParam(dim, steps);
	c.setText(""+value);
    }

    /** Set the String value of a parameter.
     */
    public void set(String dim, int steps, String value) {
	ZZCell c = getParam(dim, steps);
	c.setText(value);
    }

    /** Set the boolean value of a parameter.
     */
    public void set(String dim, int steps, boolean value) {
	ZZCell c = getParam(dim, steps);
	if(value) c.setText("true");
	else c.setText("false");
    }

    /** Set the ZZCell value of a parameter.
     */
    public void set(String dim, int steps, ZZCell value) {
	ZZCell c = getParam(dim, steps);
	ZZCursorReal.set(c, value);
    }

    /** Find a parameter cell.
     */
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
    public ZZCell getParam(String dim) { return getParam(dim, 1); }

    /** Connect the given cell to this expression (on d.expression).
     *  All this does is inserting the cell poswards from main on d.expression.
     */
    public void connect(ZZCell c) {
	main.insert("d.expression", 1, c);
    }

}


