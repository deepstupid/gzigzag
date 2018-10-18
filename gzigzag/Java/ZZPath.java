
/*   
ZZPath.java
 *    
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
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
import java.util.*;

/* Possibly publish this API later on? */
interface PathOp {
	/** Apply the pathop to the from cell, attaching the observer o
	 * to observe the relevant information about all the cells on the way.
	 * These objects do not need to attach observers to the from 
	 * cell but all the others, including the last cell.
	 * @return 	null if the requested cell does not exist.
	 */
	ZZCell apply(ZZCell from, ZZObs o);
}

/** A class to represent paths in the structure.
 * It used to also be a notifier but that was changed: now it's just
 * an abstract "path".
 */

public class ZZPath {
public static final String rcsid = "$Id: ZZPath.java,v 1.26 2000/10/18 14:35:31 tjl Exp $";
	static public boolean dbg = false;
	static final void p(String s) { if(dbg) System.out.println(s); }

	PathOp[] arr = new PathOp[0];

	public ZZPath() { }

	static final String drdim(ZZCell c) {
		return ZZDefaultSpace.getPossDerefCell(c).getText();
	}

	static final int drdir(ZZCell c) {
		String s = ZZDefaultSpace.getPossDerefCell(c).getText();
		if(s.equals("-")) return -1;
		if(s.equals("+")) return +1;
		return Integer.parseInt(s);
	}

	static final String drtext(ZZCell c) { return drdim(c); }

	/** Creates a ZZPath from cells.
	 * The cells give the path through a simple format in d.1 and d.2:
	 * The first cell is the operation name, and then follow the
	 * parameters, just like in the other calls for this class.
	 * <pre>
	 * 	STEP  dim  n
	 *	HEAD dim dir
	 *	END dim dir
	 *	FIND dim dir str
	 * </pre>
	 * Note that this is not automatically updated each time the path
	 * or the cursors in the path change - to be up to date, you need
	 * to create a new path each time. However, this is not such a 
	 * time-consuming operation.
	 *	<p>
	 * "HEAD" is now deprecated in favor of "END".
	 */
	static public ZZPath createFromStructure1(ZZCell c) {
	    ZZPath p = new ZZPath();
	    while(c!=null) {
		ZZCell[] args = c.readRank("d.1", 1, false);
		String s = c.getText();
		if(s.equals("STEP")) {
			if(args.length < 2)
			    throw new ZZError("Not enough args for '"+s+"'");
			p = p.step(drdim(args[0]), drdir(args[1]));
		} else if(s.equals("HEAD") || s.equals("END")) {
			if(args.length < 2)
			    throw new ZZError("Not enough args for '"+s+"'");
			p = p.headcell(drdim(args[0]), drdir(args[1]));
		} else if(s.equals("FIND")) {
			if(args.length < 3)
			    throw new ZZError("Not enough args for '"+s+"'");
			p = p.untilstring(drdim(args[0]),
				drdir(args[1]), drtext(args[2]));
		} else {
		    throw new ZZError("ZZPath syntax: '"+s+"'");
		}
		c = c.s("d.2", 1);
	    }
	    return p;
	}

	/** Reads the structure through this path to obtain a cell.
	 * @param from The cell to start from
	 * @param error Whether not finding a cell is an error.
	 * 		This is for coder convenience: the message
	 * 		in the exception thrown is descriptive of
	 *		where the not-found leg of the path was.
	 * @param o	The observer to attach.
	 */
	public ZZCell readFrom(ZZCell from, boolean error, ZZObs o) {
	    if(from == null)
		throw new ZZError("Can't read path from null!");
	    ZZCell n = from;
	    for(int i=0; i<arr.length; i++) {
		    n = arr[i].apply(n, o);
		    if(n==null) {
			    String s = "Couldn't find through ";
			    for(int j=0; j<i; j++)
				    s += arr[j];
			    s += "   Failed: ";
			    s += arr[i];
			    p(s);
			    if(!error)
				    return null;
			    throw new ZZError(s);
		    }
	    }
	    return n;
	}
	public ZZCell readFrom(ZZCell from, boolean error) {
	    return readFrom(from, error, null);
	}

	// XXX ??? A little odd down from here...
	// but basically right: ZZPath is immutable.

	/** Sign gives the direction on dim, absolute value
	 * number of steps */
	ZZPath step(final String dim, final int n) {
		return appendOp(new PathOp() {
			public ZZCell apply(ZZCell from, ZZObs o) {
				return from.s(dim, n, o);
			}
			public String toString() {
				return "step("+dim+","+n+")";
			}
		});
	}


	ZZPath headcell(final String dim, final int dir) {
		return headcell(dim, dir, false);
	}
	ZZPath headcell(final String dim, final int dir, final boolean ensuremove) {
		return appendOp(new PathOp() {
			public ZZCell apply(ZZCell from, ZZObs o) {
			    return from.h(dim, dir, 
				ensuremove, o);
			}
			public String toString() {
			    return "headcell("+dim+","+dir+","
				    +ensuremove+")";
			}
		});
	}

	ZZPath untilstring(final String dim, final int dir, final String str)
	{
		return appendOp(new PathOp()  {
			public ZZCell apply(ZZCell from, ZZObs o) {
			    return from.findText(dim, dir, str, o);
			}
			public String toString() {
			    return "untilstring("+dim+","+dir+",'"+str+"')";
			}
		});
	}

	ZZPath copy() {
		ZZPath n = new ZZPath();
		n.arr = new PathOp[arr.length];
		System.arraycopy(arr, 0, n.arr, 0, arr.length);
		return n;
	}

	ZZPath appendOp(PathOp p) {
		ZZPath n = new ZZPath();
		n.arr = new PathOp[arr.length + 1];
		System.arraycopy(arr, 0, n.arr, 0, arr.length);
		n.arr[arr.length] = p;
		return n;
	}


}

