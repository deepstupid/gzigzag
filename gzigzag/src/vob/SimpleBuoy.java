/*
SimpleBuoy.java
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
package org.gzigzag.vob;
import java.util.*;
import java.awt.*;

/** A simple buoy that just takes a size and a vob to place.
 *  When called to place itself in a vobscene, this buoy places the given vob.
 */
public class SimpleBuoy implements BuoyPlacer.Buoy {
public static final String rcsid = "$Id: SimpleBuoy.java,v 1.1 2001/08/11 19:48:32 bfallenstein Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }

    public int prefWidth, prefHeight, minWidth, minHeight;
    public boolean constAspectScalable;

    public int depth;
    public Vob vob;

    public SimpleBuoy(int prefWidth, int prefHeight, 
		      int minWidth, int minHeight,
		      boolean constAspectScalable,
		      int depth, Vob vob) {
	this.prefWidth = prefWidth;
	this.prefHeight = prefHeight;
	this.minWidth = minWidth;
	this.minHeight = minHeight;
	this.constAspectScalable = constAspectScalable;
	this.depth = depth;
	this.vob = vob;
    }

    public int getPrefWidth() { return prefWidth; }
    public int getPrefHeight() { return prefHeight; }
    public int getMinWidth() { return minWidth; }
    public int getMinHeight() { return minHeight; }
    public boolean constAspectScalable() { return constAspectScalable; }


    public void put(VobScene into, int x, int y, int w, int h) {
	into.put(vob, depth, x, y, w, h);
    }
}
