/*   
ClippingSubVobPlacer.java
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
import java.awt.Dimension;

/** A clipping vob placer putting the vobs in a different placer.
 *  The "clipping" part works as follows: If a vob is completely outside this
 *  vob placer, do not add it. If a vob is partly inside this vob placer, but
 *  its x or y coordinate is smaller than zero, do not add it either; we cannot
 *  clip it in that case. If a vob is partly inside this vob placer, but both
 *  its x and y coordinates are greater than or equal to zero, i.e. its width
 *  or height is too big, reduce its width or height so that it fits; the
 *  rationale is that the vob is expected to do some sort of clipping because
 *  it doesn't have enough space. If a vob is wholly inside this vob placer,
 *  just add it. 
 */

public class ClippingSubVobPlacer extends SubVobPlacer {
    VobPlacer parent;
    int x, y;
    Dimension size;

    /** Create a new ClippingSubVobPlacer.
     *  @param parent The VobPlacer to place vobs into.
     *  @param x, y The coordinates by which vobs are translated when added.
     *  @param w, h The size of this VobPlacer, as returned by getSize().
     */
    public ClippingSubVobPlacer(VobPlacer parent, int x, int y, int w, int h) {
	super(parent, x, y, w, h);
    }

    public ClippingSubVobPlacer(VobPlacer parent, int x, int y, Dimension size) {
	super(parent, x, y, size);
	if(size == null)
	    throw new Error("Cannot create ClippingSubVobPlacer with size null!");
    }

    public void put(Vob vob, int depth, int x, int y, int w, int h) {
	Dimension size = getSize();
	
	if(x < 0 || y < 0) return;
	if(x > size.width || y > size.height) return;
	
	if(x+w > size.width)  w -= ((x+w) - size.width);
	if(y+h > size.height) h -= ((y+h) - size.height);
	
	super.put(vob, depth, x, y, w, h);
    }
}


