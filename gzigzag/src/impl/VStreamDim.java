/*
VStreamDim.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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
import java.util.*;
import org.gzigzag.*;

/** A dim supporting the new span concept.
 *  XXX DOC!!!
 */

public interface VStreamDim extends Dim {
String rcsid = "$Id: VStreamDim.java,v 1.23 2002/03/15 23:35:40 bfallenstein Exp $";

    /** The CURSOR_INDEX parameter in the iterate extra parameters. */
    Object CURSOR_INDEX = new Object();

    /** Insert a span rank after some other cell on the vstream dimension.
     */
    void insertAfterCell(Cell at, Cell rank);

    /** Remove a range from a vstream, given first and last cell to remove. */
    void removeRange(Cell first, Cell last);

    /** Iterate a callback object through a stream.
     *  The <code>tag</code> object passed to <code>CharRangeIter</code>
     *  is currently defined as the <code>ScrollBlock</code> the character
     *  range is from.
     *  <p>
     *  XXX we need a variant of this taking a range inside a VStream to
     *  iterate over.
     * @param i The callback object
     * @param stream The headcell of the stream
     * @param extra A map of extra parameters,
     *		such as key CharRangeIter.CURSOR with the value being
     *		a the cursor cell (note: NOT the cell the cursor points to).
     */
    void iterate(org.gzigzag.vob.CharRangeIter i, Cell stream,
		Map extra);

    /** Called to notify the VStreamDim of a new span transclusion.
     *  Each time a space transcludes a span, and thus has new
     *  VStream cells appear, it has to notify its VStreamDim so that
     *  it will be able to handle the VStream cells correctly. In turn,
     *  the VStreamDim ensures that the cells inside the transcluded range
     *  are connected along <code>d.vstream</code>, unless they were
     *  disconnected explicitly.
     *  <p>
     *  If the space does not remember to call notifyTransclusion, or if
     *  it calls notifyTransclusion multiple times with overlapping
     *  ranges of cells, the results are undefined. (We cannot afford
     *  the efficiency loss of checking for these conditions always in
     *  the dims-- we just have to have good tests...)
     *  <p>
     *  It is not required that the transclusion be the only one
     *  with that transclusion ID and mediaserver block. Specifically,
     *  if a space adds to a transcluded span when text is appended
     *  char-by-char, it is ok to issue one call to notifyTransclusion
     *  for each newly transcluded piece. The space just has to take care
     *  that the ranges it calls notifyTransclusion with do not overlap.
     * 
     *  @param start The first vstream cell in the transclusion.
     *  @param length The number of vstream cells in the transclusion.
     */
    void notifyTransclusion(Cell start, int length);

    void dumpVStreamInfo(Cell c);
}
