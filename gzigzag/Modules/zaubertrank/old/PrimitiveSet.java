/*   
PrimitiveSet.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2001, Benjamin Fallenstein
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
 * Written by Tuomas Lukka (for Heraclitus Clang)
 */
package org.zaubertrank;
import org.gzigzag.*;
import java.util.*;

/** A set of Zaubertrank primitives.
 */

public abstract class PrimitiveSet {
String rcsid = "$Id: PrimitiveSet.java,v 1.1 2001/04/02 20:29:05 bfallenstein Exp $";
    public abstract Primitive get(String id);

    static Hashtable pss = new Hashtable();
    static PrimitiveSet findPrimitiveSet(String set) {
	Object o = pss.get(set);
	if(o != null) return (PrimitiveSet)o;
	try {
	    o = Class.forName("org.gzigzag.flowing."+set).newInstance();
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    return null;
	}
	if(!(o instanceof PrimitiveSet)) return null;
	pss.put(set, o);
	return (PrimitiveSet)o;
    }

    static Hashtable prims = new Hashtable();
    static Primitive findPrimitive(String s) {
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

    public static Primitive findPrimitive(ZZCell c0) {
	ZZCell c = c0.getRootclone();
	c = c.s("d.zaubertrank-primitive-binding");
	if(c == null) return null;
	if(!c.h("d.zaubertrank-primitives").equals(
		c.getSpace().getHomeCell()))
	    throw new ZZError("Zaubertrank primitive not enumerated on " +
			      "d.zaubertrank-primitives rank from home cell!");
	return findPrimitive(c.t());
    }
}


