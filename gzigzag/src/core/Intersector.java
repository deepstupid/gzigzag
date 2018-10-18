/*   
Intersector.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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

/** An interface for finding intersections of two ranks.
 * Intersectors are created for pairs of dimensions.
 * XXX Obs!
 */

public interface Intersector {
String rcsid = "$Id: Intersector.java,v 1.1 2001/04/19 07:56:34 tjl Exp $";

    /** Find the first cell dir1wards from c1 on dim1 that is 
     * dir2wards from c2 on dim2.
     * The cell c1 may be returned, or null if none.
     */
    Cell intersect(Cell c1, int dir1, Cell c2, int dir2);

    /** The default implementation of Intersector for arbitrary Dim objects.
     */
    class Default implements Intersector {
	public final Dim dim1, dim2;
	public Default(Dim dim1, Dim dim2) { this.dim1 = dim1; this.dim2 = dim2; }
	public Cell intersect(Cell c1, int dir1, Cell c2, int dir2) {
	    Cell h2 = dim2.h(c2, -1, null);
	    boolean first = true;
	    for(Cell c = c1; c != null && (first || c != c1); c = dim1.s(c, dir1)) {
		first = false;
		if(dim2.h(c, -1, null).equals(h2)) {
		    // See whether in correct dir.
		    throw new ZZError("Not implemented");
		}
	    }
	    return null;
	}
    }
}

