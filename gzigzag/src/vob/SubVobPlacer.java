/*   
SubVobPlacer.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
package org.gzigzag.vob;

/** A vob placer putting the vobs in a different placer -- coords translated.
 *  This is a preliminary version of "panes" in a vobset.
 */

public class SubVobPlacer implements VobPlacer {
    VobPlacer parent;
    int x, y;
    java.awt.Dimension size;

    /** Create a new SubVobPlacer.
     *  @param parent The VobPlacer to place vobs into.
     *  @param x, y The coordinates by which vobs are translated when added.
     *  @param w, h The size of this VobPlacer, as returned by getSize().
     */
    public SubVobPlacer(VobPlacer parent, int x, int y, int w, int h) {
	this.parent = parent;
	this.x = x; this.y = y;
	this.size = new java.awt.Dimension(w, h);
    }

    public SubVobPlacer(VobPlacer parent, int x, int y, java.awt.Dimension size) {
	this.parent = parent; this.x = x; this.y = y; this.size = size;
    }

    public void put(Vob vob, int depth, int x, int y, int w, int h) {
	parent.put(vob, depth, x+this.x, y+this.y, w, h);
    }

    public java.awt.Dimension getSize() { return size; }
}


