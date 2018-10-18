/*   
GZZ1Space.java
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
import java.io.*;

/** A space GZZ1SpaceHandler can read a file into.
 *  This is necessary because the ordinary Space commands do not allow
 *  creating new cells with specific IDs etc.
 */

public interface GZZ1Space extends GIDSpace {
String rcsid = "$Id: GZZ1Space.java,v 1.7 2002/01/04 14:49:44 vegai Exp $";

    /** Create a new cell with a specific (global) ID.
     */
    void gzz1_NewCell(String id);

    /** (like in space, for connect/disconnect)
     */
    Cell getCell(String id);
    /**
     * @param id Must be from IdentitySpace.space.
     */
    Dim getDim(Cell id);

    /** Transcopy a cell, using a specific tid.
     *  @param tid From this space, identity of the transcopy.
     *  @param c From other space, cell to be transcopied.
     */
    Cell gzz1_transcopy(Cell tid, Cell c);

    /** Transclude a span, using a specific tid.
     *  @param tid From this space, identity of the transclusion.
     *  @param blockId The mediaserver block containing the scroll.
     *  @param first The first unit to transclude from the scroll.
     *  @param last The last unit to transclude.
     */
    void gzz1_transcludeSpan(Cell tid, Mediaserver.Id blockId, 
			     int first, int last) throws IOException;

    

    // XXX These were added per-demand, ie.
    // when converting from coding to an implementation
    // to coding to an interface. They require
    // further consideration.
    Mediaserver.Id save(Mediaserver ms) throws java.io.IOException ;
    Cell getHomeCell(Cell c);
    Cell translate(Cell c);
    VStreamDim getVStreamDim();
    Space getSpace(Cell c);
    Cell makeSpanRank(String s);

}
