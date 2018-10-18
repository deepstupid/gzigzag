/*   
RasterFlobFactory.java
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
import java.util.*;
import java.awt.*;

/** An interface to generate flobs to put in a raster.
 * This is for abstracting out what the cells look like.
 * The "CellViews" in ZZDefaultSpace corresponds to classes
 * implementing this interface.
 */

public interface FlobFactory {
String rcsid = "$Id$";
    /** Get the default dimensions for the given cell and fraction.
     * @param c The cell. If null, the default dimensions overall are returned.
     * @param fract The magnification fraction.
     */
    Dimension getSize(ZZCell c, float fract);

    /** Create a 2-D flob.
     * This call creates the flob and places it into the given FlobSet.
     * @param c The cell for the flob
     * @param handleCell The cell from which to show e.g. cursor.
     *                   This mechanism may change.
     */
    Flob makeFlob(FlobSet f,
	    ZZCell c, ZZCell handleCell,
		float fract, int x, int y, int d, int w, int h);

    /** Place a flob.
     * Here, the size can vary freely: the flob is placed so that 
     * the point (x,y) is at (xfract, yfract) in the unit coordinate
     * system stretched to fit inside. E.g. if (xfract, yfract)==(0,1)
     * then (x,y) will be the lower left corner.
     * The reason for this call is that it may be almost twice as fast
     * as doing getSize() and makeFlob() since both may have to do some 
     * of the same calculations (e.g. line breaking).
     * <p>
     * This call should give the same results as
     * <pre>
     *		Dimension d = getSize(c, fract);
     *		return makeFlob(c, fract, 
     *			(int)(x-xfract*d.width),
     *			(int)(y-yfract*d.height),
     *			depth, d.width, d.height);
     * </pre>
     */
    Flob placeFlob(FlobSet f, 
			ZZCell c, ZZCell handleCell,
			float fract, 
			int x, int y, int depth,
			float xfract, float yfract);
			

    // should call centerFlob(..., null)
    Flob centerFlob(FlobSet f,
			ZZCell c, ZZCell handleCell,
			float fract, Point p, int xalign, int yalign,
			int depth);

    // if d==null, use what getSize would return
    Flob centerFlob(FlobSet f,
			ZZCell c, ZZCell handleCell,
			float fract, Point p, int xalign, int yalign,
			int depth, Dimension d);
}


