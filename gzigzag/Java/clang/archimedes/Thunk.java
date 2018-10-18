/*   
Thunk.java
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

/** An Archimedes Thunk object used to modify Archimedes thunks easily.
 *  An Archimedes thunk is something that holds a value. That value can be
 *  requested. However, before the value is requested, the thunk may be
 *  connected to an expression-- at the time the value is requested for the
 *  first time, the thunk is disconnected from the expression, the expression
 *  is evaluated in an empty evaluation context (namespace), and the result is
 *  written into the thunk. This is used to implement lazy evaluation.
 */

public class Thunk {
String rcsid = "$Id: Thunk.java,v 1.1 2001/04/13 22:42:51 bfallenstein Exp $";

    public ZZCell main;

    /** Create a Thunk object from an existing Thunk maincell. */
    public Thunk(ZZCell main) { this.main = main; }

    /** Create a Thunk object from an expression. */
    public Thunk(ZZSpace sp, Expression e) {
	ZZCell home = sp.getHomeCell();
	main = home.N();
	e.connect(main);
    }

    /** Create a Thunk object from a value reference. */
    public Thunk(ZZSpace sp, ZZCell ref) {
	main = sp.getHomeCell().N();
	Archimedes.copy(ref, main);
    }

    public ZZCell get() {
	ZZCell exc = main.h("d.expression", true);
	if(exc != null) {
	    Expression e = new Expression(exc);
	    Namespace n = new Namespace(main.getSpace());
	    ZZCell val = Archimedes.evaluateExpression(e, n);
	    Archimedes.copy(val, main);
	    main.excise("d.expression");
	}
	return main;
    }
    public ZZCell getcell() {
	return ZZCursorReal.get(get());
    }
    public int getint() {
	return Data.i(get());
    }
    public String getstr() {
	return Data.s(get());
    }
    public boolean getbool() {
	return Data.b(get());
    }

}


