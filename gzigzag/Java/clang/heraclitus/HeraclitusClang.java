/*   
HeraclitusClang.java
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

/** A simple execution-based clang.
 * Based on a cascading interpreter model, like #! in unix.
 * Executables are given as parameters the cell they were referred
 * to in (for structural params) and another cell, the parameter.
 */

public class HeraclitusClang {
public static final String rcsid = "$Id$";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }

    static Hashtable pss = new Hashtable();
    static PrimitiveSet findPrimitiveSet(String set) {
	Object o = pss.get(set);
	if(o != null) return (PrimitiveSet)o;
	try {
	    o = Class.forName("org.gzigzag.heraclitus."+set).newInstance();
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    return null;
	}
	if(!(o instanceof PrimitiveSet)) return null;
	pss.put(set, o);
	return (PrimitiveSet)o;
    }

    static Hashtable prims = new Hashtable();
    static Primitive findPrimitive(ZZCell c) {
	String s = c.getText();
	Object o = prims.get(s);
	if(o != null) return (Primitive)o;

	int ind = s.indexOf(".");
	if(ind < 0) return null;
	String set = s.substring(0, ind);
	String id = s.substring(ind+1);
	PrimitiveSet ps = findPrimitiveSet(set);
	Primitive prim = ps.get(id);
	if(prim != null)
	    prims.put(id, prim);
	return prim;
    }

    /** Execute a heraclitus operation.
     * @param op This cell gives the operation, either by
     *		being the clone of a Heraclitus primitive
     *		or by having its root clone have an "exe-type".
     * @param param The parameter to be given for the
     *			execution.
     */
    public static ZZCell execute(ZZCell op, ZZCell param) {
	Primitive p = findPrimitive(op);
	p("Heraclitus exec: "+p);
	if(p == null) {
	    ZZCell n = op.h("d.clone", -1);
	    p = findPrimitive(n);
	    if(p == null) {
		ZZCell nx = op.h("d.exe-type", -1, true);
		if(nx == null) {
		    throw new ZZError("No executable primitive");
		}
		// The core: create the new langcache level
		ZZCell curs1 = op.getHomeCell().N();
		ZZCell curs2 = curs1.N("d.1", 1);
		ZZCursorReal.set(curs1, op);
		ZZCursorReal.set(curs2, param);
		try {
		    p("Heraclitus recurse: "+nx);
		    return execute(nx, curs1);
		} finally {
		    // XXX ZZCursorReal -delete as well?!?!
		    ZZCursorReal.delete(curs1);
		    ZZCursorReal.delete(curs2);
		    curs1.delete();
		    curs2.delete();
		}
	    }
	}
	return p.execute(op, param);
    }
}

