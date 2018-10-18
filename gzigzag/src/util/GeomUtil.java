/*   
GeomUtil.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
package org.gzigzag.util;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** Some geometric utilities.
 */

public final class GeomUtil {
public static final String rcsid = "$Id: GeomUtil.java,v 1.2 2001/10/17 20:27:57 tjl Exp $";
    
    /** Calculate the minimum square distance between the
     * given rectangle and the given point.
     */
    public static int sqDistance(Rectangle r, Point p) {
	int dx = 0, dy = 0;
	if(p.x < r.x) dx = r.x - p.x;
	else if(p.x > r.x + r.width) dx = p.x - (r.x + r.width);
	else dx = 0;
	if(p.y < r.y) dy = r.y - p.y;
	else if(p.y > r.y + r.height) dy = p.y - (r.y + r.height);
	else dx = 0;
	return dx*dx + dy*dy;
    }
    /** Make a dimension fit the given framing rectangle by 
     * reducing its size.
     * If the given dimensions do not fit, they are scaled while
     * maintaining aspect ratio.
     * @param frame The containing rectangle
     * @param d The dimension to shrink. 
     * @param maintainAspect Whether the aspect ration of d should
     * 			be maintained.
     *
     */
    public static void fitRectangle(Rectangle frame, Dimension d,
	    boolean maintainAspect) {
	// degenrate cases first
	if(frame.width == 0 || frame.height == 0 ||
	    d.width == 0 || d.height == 0)  {
	    d.width = 0; d.height = 0; return;
	}
	if(maintainAspect) {
	    double ratx = frame.width / (double) d.width;
	    double raty = frame.height / (double) d.height;
	    double rat;
	    if(ratx < raty) rat = ratx; else rat = raty;
	    if(rat >= 1) return; // No adjustment necessary
	    d.width *= rat;
	    d.height *= rat;
	} else {
	    if(d.width > frame.width) d.width = frame.width;
	    if(d.height > frame.height) d.height = frame.height;
	}
    }

    /** Place a rectangle with the given size into the given
     * rectangle with its center as close as possible to the given
     * point.
     * It may be reasonable to call fitRectangle first on the dimension.
     * This function assumes that the dimension fits the rectangle.
     */
    public static void placeRectangle(Rectangle r,
		Dimension d, Point p, Rectangle out) {
	// Compute extents for the placement of the center point
	// of the inner rectangle. This is simple: from each edge
	// we must subtract half of the size of the smaller rectangle.
	int ix = r.x + d.width / 2;
	int iw = r.width - d.width;
	int iy = r.y + d.height / 2;
	int ih = r.height - d.height;

	if(p.x < ix) out.x = ix;
	else if(p.x > ix + iw) out.x = ix + iw;
	else out.x = p.x;

	if(p.y < iy) out.y = iy;
	else if(p.y > iy + ih) out.y = iy + ih;
	else out.y = p.y;

	out.x -= d.width / 2;
	out.y -= d.height / 2;
	out.width = d.width;
	out.height = d.height;
    }
}
