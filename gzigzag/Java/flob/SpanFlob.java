/*   
SpanFlob.java
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

/** An interface to Flobs that contain a single span.
 */

public interface SpanFlob {
String rcsid = "$Id: SpanFlob.java,v 1.5 2000/09/19 10:32:00 ajk Exp $";

    /** The previous and following spanflobs.
     * These are used for coloring beams: if a beam is being drawn 
     * between vstreams with a lot of the same content, then
     * the beam should have just one color. These fields enable 
     * the beam drawer to see what the vstream actually is.
     * These are to be set by the routine that puts the spanflobs in the
     * flobset.
     */
    SpanFlob getPrev();
    SpanFlob getNext();
    void setNext(SpanFlob sf);

    /** The span this flob contains. Might not be the same as 
     * flob.c.getSpan() since this might be just a piece of the cell.
     * Can return null.
     */
    Span getSpan();

    /** Get the rectangle corresponding to the given span. The offs and end
     * are given as substring indices to the span returned by getSpan(),
     * i.e. the characters offs...end-1 are included.
     */
    Rectangle getRectangle(int offs, int end);
}


