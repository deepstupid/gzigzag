/*   
Iter.java
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
 * Written by Benjamin Fallenstein
 */

package org.gzigzag;
import java.util.*;

/** Help for iterating along ranks in various ways.
 *  A proposed alternative to ZZIter.
 *  XXX make constructors work (how?)
 *  XXX remove verbose debugging messages
 *  XXX make MUCH faster!
 */

public class Iter {
public static final String rcsid = "$Id: Iter.java,v 1.1 2000/11/16 20:28:53 bfallenstein Exp $";
    public static final boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }

    static public class Step {
	public ZZCell cell;
	public Step from;
	public int nth;

	public Object info;
	
	// Do not need to be set (then, dir == null).
	public String dim = null;
	public int dir;
	
	public Step(ZZCell c) { from = null; cell = c; nth = 0; }
	public Step(Step f, ZZCell c, int n) { from = f; cell = c; nth = n; }

	public Step go(ZZCell c) { return new Step(this, c, nth+1); }
	public Step go(String dim, int dir) {
	    p("step "+cell+" "+dim+" "+dir);
	    ZZCell tocell = cell.s(dim, dir);
	    p("tocell "+tocell);
	    if(tocell == null) return null;
	    Step res = go(tocell);
	    p("res.cell "+res.cell);
	    res.dim = dim; res.dir = dir;
	    return res;
	}
	
	public String toString() { return "[Iter.Step for "+cell+"]"; }
    }

    static public abstract class Any {
	public Step here, root;
	public boolean included;
	
	protected abstract Step getNext();
	protected void doStart() {}
	
//	public Any(boolean included) { this.included = included; }
	public ZZCell start(Step s) {
	    here = s; root = s;
	    doStart();
	    if(!included) next();
	    return here != null ? here.cell: null;
	}
	public ZZCell next() {
	    if(here == null) return null;
	    here = getNext();
	    return here != null ? here.cell : null;
	}
	public boolean more() { return here != null; }
	public void stop() { here = null; }
    }
    

    static public abstract class Dim extends Any {
	String dim; int fdir = 1;
	
//	public Dim(String dim, int fdir, boolean included) {
//	    this.dim = dim;
//	    this.fdir = fdir;
//	    this.included = included;
//	}
	
	public void start(Step s, String dim) {
	    this.dim = dim; start(s);
	}
	public void start(Step s, String dim, int fdir) {
	    this.dim = dim; this.fdir = fdir; start(s);
	}
    }


    static public class Rank extends Dim {
	protected Step getNext() { return here.go(dim, fdir); }
    }


    static public class Alter extends Dim {
	/** Current position negwards and poswards. */
	Step neg, pos;

	public int ind;
	
	public void doStart() {
	    neg = here.go(dim, -fdir);
	    pos = here.go(dim, fdir);
	    p("alter start: "+pos+" "+neg+" // "+(pos==root?"yes":"no"));
	    ind = 0;
	}
	protected Step getNext() {
		if(neg == null && pos == null) { p("alter null"); return null; }
		// Trick: always change ind, only after see if we have something
		// to return. If not, recurse and get the next one from the other side.
		Step res;
		if(ind <= 0) {
		    ind = -ind;
		    ind ++;
		    if(pos == null)
			return getNext();
		    res = pos;
		    pos = pos.go(dim, fdir);
		    p("alter pos from "+res+" to "+pos);
		} else {
		    ind = -ind;
		    if(neg == null)
			return getNext();
		    res = neg;
		    neg = neg.go(dim, -fdir);
		    p("alter neg from "+res+" to "+neg);
		}
		p("alter: "+res);
		// Don't loop XXX doesn't work yet
//		if(neg!=null && pos!=null && neg.cell==pos.cell) return null;
		return res;
	}
	public void stop() {
		if(ind > 0) neg = null;
		else if(ind < 0) pos = null;
		else super.stop();
	}
    }


    static public abstract class Filter extends Any {
	protected Any iter;
	
	protected abstract boolean check();
	protected void accepted() {}
	
//	public Filter(Any iter) { this.iter = iter; }
	public ZZCell start(Step s) {
	    iter.start(s);
	    here = iter.here;
	    doStart();
	    return here.cell;
	}
	public ZZCell next() {
	    if(here == null) return null;
	    do {
		p("Filter get next...");
		iter.next();
		here = iter.here;
		p("Filter check: "+here);
		if(here == null) return null;
	    } while(!check());
	    accepted();
	    p("Filter accept "+here);
	    return here.cell;
	}
	public void stop() { iter.stop(); }
	protected Step getNext() { throw new ZZError("getNext on Filter"); }
    }


    static public class Once extends Filter {
	protected Hashtable done;

	protected void doStart() { done = new Hashtable(); }
	protected boolean check() { return done.get(here.cell) == null; }
	protected void accepted() { done.put(here.cell, here.cell); }
    }


    static public class Limited extends Filter {
	protected int n, donenum;

//	public Limited(Any iter, int n) { this.iter = iter; this.n = n; }
	protected void doStart() { donenum = 0; }
	protected boolean check() {
	    if(donenum > n*n*2) return false;
	    return iter.here.nth < n;
	}
	protected void accepted() { donenum++; }
    }
}