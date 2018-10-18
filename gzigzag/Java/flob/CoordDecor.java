/*   
CoordDecor.java
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
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** A base class from which to derive fast decorations with coordinates.
 */

abstract public class CoordDecor extends Renderable {
public static final String rcsid = "$Id: CoordDecor.java,v 1.7 2000/12/26 20:15:14 bfallenstein Exp $";
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    /** The number of coordinates.
     */
    int n;
    /** The coordinates.
     * Note: the translate() method assumes that at each even index there's
     * an x coordinate, and at each odd index there's an y coordinate. If
     * that's not true, override translate() in subclasses (possibly returning
     * false, if the coords can't be translated).
     */
    int[] coords;

    /** A simple class that knows how to efficiently build
     * these things. The efficiency comes from always allocating 
     * an array and never letting go of it; adding the arrays to the
     * flob when they're full and starting fresh.
     */
    static abstract public class Builder {
	/** The flobset the finished products go into. */
	FlobSet into;
	/** The depth of the finished product. */
	int d;

	/** The nth object being rendered. */
	int nth;
	/** The current point index. */
	int curp; 
	/** The number of point coordinates to reserve. */
	int np;
	/** The coordinates. */
	int[] p; 

	public Builder(FlobSet into) {
	    this.into = into;
	}
	/** Start a set.
	 * @param n The estimated number of coordinates to come.
	 * @param d The depth of the set now begun.
	 */
	public void startl(int n, int d) {
	    if(curp != 0) endl();
	    this.d = d;
	    curp = 0;
	    np = n;
	    p = null;
	}
	/** Make room for n coordinates. */
	protected void makeRoom(int l) {
	    if(p!=null && curp + l > p.length) 
		endl();
	    if(p==null)
		alloc();
	}
	/** Allocate the arrays. */
	protected void alloc() {
	    p = new int[np];
	    curp = 0;
	    nth = 0;
	}
	public void endl() {
	    if(p==null) return;
	    into.add(create());
	    p = null; curp = 0;
	}
	abstract protected Renderable create();

    }

    public CoordDecor(int[] coords, int n, int d) {
	this.coords = coords;
	this.n = n;
	this.d = d;
	if(coords != null && coords.length < n)
	    throw new Error("CoordDecor: less length than n!!!");
    }

    abstract public void render(Graphics g);

    public boolean translate(int x, int y) {
	for(int i=0; i<coords.length; i+=2) {
	    coords[i] += x;
	    if(i+1 < coords.length) coords[i+1] += y;
	}
	return true;
    }
}
