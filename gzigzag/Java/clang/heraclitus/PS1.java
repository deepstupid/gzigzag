/*   
PS1.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
package org.gzigzag.heraclitus;
import org.gzigzag.*;
import java.util.*;

/** 
 * The first primitive set for Heraclitus clang.
 */

public class PS1 implements PrimitiveSet {
public static final String rcsid = "$Id$";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }

    public Primitive get(String id) {
	if(id.equals("Move1Test"))
	    return new Move1Test();
	return null;
    }

    /** First test: move a HERA1 (see ZZPrimitiveActions) datacursor one
     * step pos on d.1.
     */
    static public class Move1Test implements Primitive {
	public ZZCell execute(ZZCell primitive, ZZCell cursorparam) {
	    p("Move1test");
	    ZZCell c0 = ZZCursorReal.get(cursorparam);
	    ZZCell c = c0.s("d.1", 1);
	    ZZCell v = ZZCursorReal.get(c);
	    ZZCell t0 = ZZCursorReal.get(v);
	    ZZCell t = t0.s("d.1", 1);
	    p("M1T: "+cursorparam+" "+c0+" "+c+" "+t0+" "+t);
	    ZZCursorReal.set(v, t);
	    return cursorparam;
	}
    }

    /** A text translator: translates fixed strings into other fixed
     * strings.
     */
    

    /** Dereference the cursorparameter: expects the cursorparameter
     * to point to another cursor, and makes it point to the place
     * the other cursor pointed to.
     */
    static public class Deref1 implements Primitive {
	public ZZCell execute(ZZCell primitive, ZZCell cursorparam) {
	    ZZCell c = ZZCursorReal.get(cursorparam);
	    if(c == null) return null;
	    ZZCursorReal.set(cursorparam, ZZCursorReal.get(c));
	    return cursorparam;
	}
    }

    /** Execute a sequence of cells with the same cursorparam.
     */
    static public class Seq1 implements Primitive {
	public ZZCell execute(ZZCell primitive, ZZCell cursorparam) {
	    // ZZCell root = cursorParam.
	    return cursorparam;
	}
    }
}
