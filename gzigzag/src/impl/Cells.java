/*   
Cells.java
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
package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.Mediaserver;
import java.util.*;

/** Conveniece functions for ZZ cells.
 */

public class Cells {

    /** Find a cell in a space that has a given ID.
     *  Searches all included spaces for a cell with that ID (not a transcopy
     *  of it). If no space containing that cell is found, includes the
     *  space that created the cell (i.e., in "abc-def," it loads space abc).
     *  @param id Usually from Id.space.
     */
    /*    public static Cell findIncludedCell(CompoundSpace sp, Cell id) {
	String idstr = id.id.substring(id.id.lastIndexOf(":")+1);
	
	if(sp.exists(idstr)) return sp.getCell(idstr);

	for(Iterator i = sp.spaces.keySet().iterator(); i.hasNext(); ) {
	    Space inc = (Space)i.next();
	    if(inc.exists(idstr))
		return inc.getCell(idstr);
	}
	
	Mediaserver.Id msid = 
	    new Mediaserver.Id(idstr.substring(0, idstr.indexOf("-")));

	throw new ZZError("including space not implemented");
	}*/

    /** Finds conflicts that would emerge if <code>curr</code> was updated into
     *  <code>upd</code>. Prints them. 
     *  @return Whether conflicts were found
     */

    public static boolean conflicts(Space cs, Space curr, Space upd) {
	
	return false;
    }


    /** Insert a span rank at a cursor.
     */
    static public void vStreamInsert(VStreamDim dim, Cell atCursor, Cell rank){
        Cell c = Cursor.getVStreamCellBefore(atCursor);
        if(c == null) {
            throw new Error("Invalid insert at non-character cursor");
            // c = atCursor; // At beginning of rank (!?)
        }
        dim.insertAfterCell(c, rank);
        /*
        Cell pos = Cursor.getPosition(atCursor);
        int side = Cursor.getSide(atCursor);
        if(side < 0 && pos.s(this, -1) == null) {
            // special case: negside of headcell represents end of stream
            pos = h(pos, 1, null);
            side = 1;
        }
        if(side < 0)
            pos = s(pos, -1, null);
        insertAfterCell(pos, rank);
        */
    }
}
