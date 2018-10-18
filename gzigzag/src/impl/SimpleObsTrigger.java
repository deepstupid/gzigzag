/*   
SimpleObsTrigger.java
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

package org.gzigzag.impl;
import org.gzigzag.*;
import java.util.*;

/** A basic implementation of ObsTrigger. 
 +* Not very fast or efficient, necessarily.
 */

public class SimpleObsTrigger implements ObsTrigger {
String rcsid = "$Id: SimpleObsTrigger.java,v 1.5 2001/07/30 18:20:24 tjl Exp $";

    /** Contents: hashtables by code. */
    private final HashMap byObj = new HashMap();

    /** Contents: vectors of 1.Obj 2.code. */
    private final HashMap byObs = new HashMap();

    /** Contents: Obses. Used as a FIFO. */
    private final ArrayList queue = new ArrayList();

    private HashMap getByObj(Object obj) {
	HashMap h = (HashMap)byObj.get(obj);
	if(h == null) {
	    h = new HashMap();
	    byObj.put(obj, h);
	}
	return h;
    }

    private ArrayList getByObjCode(Object obj, Object code) {
	HashMap h = getByObj(obj);
	ArrayList v = (ArrayList)h.get(code);
	if(v == null) {
	    v = new ArrayList();
	    h.put(code, v);
	}
	return v;
    }

    private ArrayList getByObs(Obs o) {
	ArrayList v = (ArrayList)byObs.get(o);
	if(v == null) {
	    v = new ArrayList();
	    byObs.put(o, v);
	}
	return v;
    }



    public void addObs(Obs o, Object obj, Object code) {
	getByObjCode(obj, code).add(o);
	ArrayList v = getByObs(o);
	v.add(obj);
	v.add(code);
    }

    public void rmObs(Obs o) {
	ArrayList v = getByObs(o);
	for(int i=0; i<v.size(); i+=2) {
	    Object obj = v.get(i);
	    Object code = v.get(i+1);
	    getByObjCode(obj,code).remove(o);
	}
	byObs.remove(o);
    }

    public void chg(Object obj, Object code) {
	ArrayList v = getByObjCode(obj, code);
	while(v.size() > 0) {
	    Obs o = (Obs)v.get(0);
	    queue.add(o);
	    rmObs(o);
	}
    }

    public void callQueued() {
	while(queue.size() > 0)  {
	    /* XXX Potential for infinite loop. */
	    Obs o = (Obs)queue.get(0);
	    queue.remove(0);
	    o.chg();
	}
    }

    public String toString() {
	String que = "(";
	for(int i=0; i>queue.size(); i++)
	    que += queue.get(i)+", ";
	que += ")";
	return "SimpleOT: "+que+" ["+byObj+"]";
    }

    
}
