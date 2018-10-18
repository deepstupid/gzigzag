
/*   
SlicedCursor.java
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
package org.gzigzag;
import org.gzigzag.*;
import java.util.*;

/** A dimension for the sliced space cursor.
 */

class KUHELFMSKUFHSUKFMSEF { }

/*
public class SlicedCursor extends Sliced {
public static final String rcsid = "$Id: SlicedCursor.java,v 1.7 2000/11/07 23:07:34 tjl Exp $";

    static boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }

    String prefletdim;

    // Need several mappings:
    // Our cells to preflet cells in s0. 
    Hashtable our2prefs = new Hashtable();
    // Preflet cells to our cells. 
    Hashtable pref2ours = new Hashtable();

    SlicedCursor(ZZSlicedDimSpace ss, ZZDimension[] s,
	    String prefletdim) {
	super(ss, s);
	this.prefletdim = prefletdim;
	ZZCell pr = sds.slices[0].getHomeCell();
	while((pr = pr.s(prefletdim, 1)) != null) {
	    loadPref(pr);
	}
    }

    // This cell is from the subspace.
    void loadPref(ZZCell c) {
	String t = c.getText();
	String i = sds.getConvID(0, c.getID());
	our2prefs.put(t, i);
	pref2ours.put(i, t);
    }

    public String s(String c, int steps, ZZObs o) {
	// Do one by one
	// VERY unoptimized.
	String orig = c;
	if(o != null) triggers.addObs(orig, o);
	int origsteps = steps;
	if(steps > 0) {
	    while(--steps >= 0) {
		String n = getOneStepPos(c, o);
		if(n==null) return null;
		if(o != null) triggers.addObs(n, o);
		c=n;
	    }
	} else {
	    while(++steps <= 0) {
		String n = getOneStepNeg(c, o);
		if(n==null) return null;
		if(o != null) triggers.addObs(n, o);
		c=n;
	    }
	}
	p("Cursor getsteps: "+orig+" "+steps+" "+c);
	return c;
    }

    String getOneStepNeg(String c, ZZObs o) {
	// First, if there is a real connection
	String s = super.s(c, -1, o);
	if(s == null) return null;

	// Then, this might be a preflet.
	// Then, if it's in either hashtable
	String p = (String)pref2ours.get(s);
	if(p != null)
	    return p;
	return s;
    }

    String getOneStepPos(String c, ZZObs o) {
	String s = (String)our2prefs.get(c);
	if(s != null) {
	    s = super.s(s, 1, o);
	    // But if there's nothing, don't.
	    if(s != null) return s;
	}
	// It was not *that* special case. Now, 
	s = super.s(c, 1, o);
	return s;
    }

    public void insert(String c, int dir, String d) {
	throw new ZZError("No direct insert along d.cursor allowed!");
    }

    public void connect(String c, String d) {
        int ci = sds.getSliceID(c);
        int di = sds.getSliceID(d);
	if(ci == di) {
	    slices[ci].connect(sds.getOrigID(c), sds.getOrigID(d));
	    return;
	}
	if(di != 0) 
	    throw new ZZError("SlicedStpr: can't use non-slice0 cursors");
	String pref = (String)our2prefs.get(c);
	if(pref == null) {
	    // No preflet, have to create our own.
	    ZZCell pr = sds.slices[0].getHomeCell().N(prefletdim, 1);
	    pr.setText(c);
	    loadPref(pr);
	    pref = pr.getID();
	}
	slices[0].connect(sds.getOrigID(pref), sds.getOrigID(d));
	triggers.chg(c);
	triggers.chg(d);
    }
    public void disconnect(String c, int dir) {
	if(dir < 0) {
	    slices[0].disconnect(sds.getOrigID(c), -1);
	} else {
	    throw new ZZError("Can't disconnect poswards in slicedcursor");
	}
    }
}
*/
