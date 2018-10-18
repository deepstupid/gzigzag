/*   
Namespace.java
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

/** An Archimedes namespace (usable as a stack frame).
 *  A namespaces associates variables with values. A value is an intersection
 *  between the rank on d.namespace-set from the namespace's maincell, and the
 *  rank on d.namespace-var from the variable's cell.
 */

public class Namespace {
String rcsid = "$Id: Namespace.java,v 1.3 2001/04/13 22:42:51 bfallenstein Exp $";

    public ZZCell main;

    /** Create a namespace object for the given namespace maincell. */
    public Namespace(ZZCell main) { this.main = main; }

    /** Create a new namespace in the given ZZSpace. */
    public Namespace(ZZSpace sp) { main = sp.getHomeCell().N(); }

    public static final String variable_dim = "d.namespace-var";
    public static final String namespace_dim = "d.namespace-set";

    /** Create a namespace as a sub-namespace of this namespace.
     *  Currently, the connection is not saved in any way.
     *  It's just in the same space, and that's it.
     */
    public Namespace create() {
	return new Namespace(main.getSpace());
    }

    public ZZCell get(ZZCell var0) {
	ZZCell var = var0.getRootclone();
	ZZCell field = main.intersect(namespace_dim, 1, var, variable_dim, 1);
	Thunk t = new Thunk(ZZCursorReal.get(field));
	return t.get();
    }
    public ZZCell getcell(ZZCell var) {
	return ZZCursorReal.get(get(var));
    }
    public int getint(ZZCell var) {
	return Data.i(get(var));
    }
    public String getstr(ZZCell var) {
	return Data.s(get(var));
    }
    public boolean getbool(ZZCell var) {
	return Data.b(get(var));
    }


    /** Put the given thunk into that variable. */
    public void put(ZZCell var0, Thunk t) {
	ZZCell var = var0.getRootclone();
	ZZCell field = main.intersect(namespace_dim, 1, var, variable_dim, 1);
	if(field == null) {
	    field = var.N(variable_dim);
	    main.insert(namespace_dim, 1, field);
	}
	ZZCursorReal.set(field, t.main);	
    }
    public void put(ZZCell var, Object value) {
	ZZCell c = var.N();
	if(value instanceof ZZCell)
	    ZZCursorReal.set(c, (ZZCell)value);
	else
	    c.setText(Data.s(value));
	put(var, new Thunk(c.getSpace(), c));
    }
    public void put(ZZCell var, int i) {
	put(var, new Integer(i));
    }
    public void put(ZZCell var, boolean b) {
	put(var, new Boolean(b));
    }

    /** Put the value referenced to by that cell into that variable.
     *  This first dereferences the value of the ref cell, i.e. if ref accurses
     *  a cell, it gives var the accursed cell as its value, and otherwise it
     *  just copies the content of ref. put(), on the other hand, would let
     *  the variable point to the cell you give it.
     */
    public void putvalue(ZZCell var, ZZCell ref) {
	put(var, new Thunk(ref.getSpace(), ref));
    }

    /** Test whether a cell is a variable.
     *  "For the moment, let's add a static boolean function isVariable(cell) 
     *  to Namespace, just testing whether the variable is connected on
     *  variable_dim. (In the long run, that's not o.k., though, because there
     *  may simply be _no_ value associated with that variable. But that's
     *  the long run.)"
     *  <p>
     *  Another intermediate is that a cell can be made a variable recognized
     *  by this function by making it a single-cell loop on d.isvariable.
     *  This is done by calling makeVariable(var).
     */
    static public boolean isVariable(ZZCell c0) {
	ZZCell c = c0.getRootclone();
	if(c.s("d.isvariable") != null) return true;
	return (c.s(variable_dim) != null) && (c.s(variable_dim, -1) == null);
    }
    static public void makeVariable(ZZCell c0) {
	ZZCell c = c0.getRootclone();
	if(c.s("d.isvariable") == null)
	    c.connect("d.isvariable", 1, c);
    }

}


