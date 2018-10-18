/*
Buoy2.java
 *    
 *    Copyright (c) 2000, Tuomas Lukka
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
package org.gzigzag.vob;
import java.util.*;
import java.util.List;
import java.awt.*;
import org.gzigzag.util.*;

/** A second implementation of buoys.
 *  <blockquote>
 *  buoy (n) : bright-colored; a float attached by rope to the seabed to
 *             mark channels in a harbor or underwater hazards
 *             <i>(WordNet (r) 1.7)</i>
 *  </blockquote>
 *  The importance of the buoys is determined from the depth of the anchor:
 *  the ones attached less deep have more power to say where they want to 
 *  be. This is actually analogous to the physical situation when
 *  all buoys have long enough strings to just float on surface: displacement
 *  is easier for the deeper ones.
 */
public class Buoy2 {
public static final String rcsid = "$Id: Buoy2.java,v 1.7 2001/11/05 20:55:35 bfallenstein Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }

    /** The minimum space between buoys where another buoy might be placed.
     */
    int MINSPACE = 10;

    public Buoy2() {
    }

    Point aCenter = new Point();
    Point bCenter = new Point();
    Dimension prefSize = new Dimension();
    Vob.Coords result = new Vob.Coords();

    /** A temporary object taking care of placing a single buoy.
     */
    class Placement {
	Buoy b;
	Vob.Coords coords = new Vob.Coords();

	/** Place the current buoy into one of the given rectangles.
	 * @param rectangles A list of <b>non-overlapping</b>
	 * 		rectangles. The buoy will be placed into one of these
	 * 		and the list adjusted to remove this space.
	 * 		XXX The list should allow overlapping rectangles 
	 * 		to make
	 * 		better use of space...
	 * @param area An integer array containing a single integer, which
	 * 		is the total area of all rectangles on the list.
	 * 		This is adjusted by the current routine.
	 */
	void place(VobScene into, List rectangles, int[] area) {
	    b.getPreferredSize(prefSize);
	    coords.getCenter(aCenter);

	    // Find the closest of the rectangles
	    int closest = -1;
	    int closestIndex = -1;
	    for(int i=0; i<rectangles.size(); i++) {
		Rectangle r = (Rectangle) rectangles.get(i);
		int d = GeomUtil.sqDistance(r, aCenter);
		if(closest == -1 || closest > d) {
		    closest = d; closestIndex = i;
		}
	    }
	    
	    // And cut-to-fit and place the buoy inside it.
	    Rectangle cr = (Rectangle)rectangles.get(closestIndex);
	    p("Fitting: "+cr+" "+prefSize+" "+aCenter);
	    GeomUtil.fitRectangle(cr, prefSize, true);
	    p("Fitted: "+prefSize);
	    GeomUtil.placeRectangle(cr, prefSize, aCenter, result);
	    p("Placed: "+prefSize+" "+result);

	    b.put(into, coords.depth, 
		    result.x, result.y, result.width, result.height);


	    // Add a line between the centers of the anchor and buoy.
	    result.getCenter(bCenter);
	    into.put(new LineVob(aCenter.x, aCenter.y, bCenter.x, bCenter.y),
			coords.depth - 1,
			0, 0, 1, 1);

	    // Cut apart the rectangle on the list and place the resulting
	    // smaller rectangles on the list

	    rectangles.remove(closestIndex);
	    area[0] -= cr.width * cr.height;

	    // XXX This only works out right for vertical seas!!!
	    if(result.y - cr.y >= MINSPACE) {
		Rectangle r = 
			new Rectangle(cr.x, cr.y, cr.width, result.y-cr.y);
		rectangles.add(r);
		area[0] += r.width * r.height;
	    }
	    if((cr.y + cr.height) - (result.y + result.height) >= MINSPACE) {
		Rectangle r = new Rectangle(cr.x, result.y + result.height,
		    cr.width, (cr.y + cr.height) - (result.y + result.height)
			);
		rectangles.add(r);
		area[0] += r.width * r.height;
	    }
	}
    }

    Placement[] places = new Placement[100];

    /** Place the given buoys into the given rectangle.
     * @param scene The vobscene which contains the Buoy's anchors
     * 			and into which the buoys are placed.
     * @param b	The buoys to place. (XXX a List or Set?)
     * @param sea The rectangle into which the buoys should be limited.
     */
    public void place(VobScene into, Buoy[] b, Rectangle sea) {
	if(places.length < b.length) places = new Placement[b.length];

	// Create the Placement objects
	for(int i=0; i<b.length; i++) {
	    if(places[i] == null) places[i] = new Placement();
	    places[i].b = b[i];
	    into.getCoords(b[i].getAnchor(), places[i].coords);
	}

	// Sort the buoys in anchor depth order. 
	Arrays.sort(places, 0, b.length, new Comparator() {
	    public  int compare(Object o1, Object o2) {
		int i1 = ((Placement)o1).coords.depth;
		int i2 = ((Placement)o2).coords.depth;
		// return i1 > i2 ? 1 : i1 == i2 ? 0 : -1;
		return i1 - i2;
	    }
	});

	// Place the buoys starting from the shallowest-anchored one.
	List rectangles = new ArrayList();
	rectangles.add(new Rectangle(sea));
	int origArea = sea.width * sea.height;
	int[] area = new int[] { origArea };
	for(int i=0; i<b.length; i++) {
	    places[i].place(into, rectangles, area);
	    if(area[0] < origArea/5) return;  // quit if the remaining area is small.
	    if(rectangles.size() > 4) return; // don't allow splintering
	}
    }

}
