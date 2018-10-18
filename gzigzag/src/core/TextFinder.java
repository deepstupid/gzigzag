/*   
TextFinder.java
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

/** An interface for finding text along a given dimension, more importantly:
 * an example of a dimension extension that could be implemented
 * by a dimension.
 * 
 */

public interface TextFinder {
String rcsid = "$Id: TextFinder.java,v 1.2 2001/04/13 14:03:13 tjl Exp $";

    /** Find the next cell on the rank given by the implied rank from the
     * given cell whose text content matches txt.
     * The cell start is included.
     * <b>Must</b> return null if used on a looping rank that does not
     * contain a cell with the given text.
     */
    Cell findText(String txt, Cell start, int dir);

    /** The default implementation of TextFinder for arbitrary Dim objects.
     */
    class Default implements TextFinder {
	public final Dim dim;
	public Default(Dim dim) { this.dim = dim; }
	public Cell findText(String txt, 		
			Cell start, int dir) {
	    // Check for initial match: otherwise test cur==start at end
	    // gives wrong result.
	    if(start.t().equals(txt)) return start;
	    Cell cur = start;
	    // XXX Possible race condition loop!
	    while(cur != null && cur != start && 
		    !cur.t().equals(txt))
			cur = dim.s(cur, 1);
	    if(cur == start) return null; // No match
	    return cur;
	}
    }
}
