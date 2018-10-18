/*   
Nile1Div.java
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
package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;

/** A representation of a particular way of dividing up the nile stream.
 * For instance, words, sentences, clauses, paragraphs or user-specified
 * split points could be represented this way.
 * It is allowed to have an non-zero-width break between the divisions.
 * <p>
 * It is assumed that no elements will be zero-width.
 */

public interface Nile1Div {
String rcsid = "$Id: Nile1Div.java,v 1.1 2000/11/20 17:36:13 tjl Exp $";

    /** Find the next break.
     * To iterate, over breaks, pass the start2 returned 
     * from the previous iteration
     * as start to the next and set includeThis to false.
     * <p>
     * An important property here is that the stream may be cut
     * at any location returned in end2.
     * 
     * @param start The location to start from
     * @param dir The direction to move into, 1 or -1.
     * @param includeThis Whether to exclude the break where 
     * @param start2 The cursor to store the start of the next break in.
     * @param end2 The cursor to store the end of the next break in.
     * @return true if next break found.
     */
    boolean findBreak(ZZCursor start,
		    int dir, boolean includeThis,
		    ZZCursor start2, ZZCursor end2);

    /** Cut the stream at a boundary specified
     * by this division.
     * @param start An end of a break. This MUST have been returned by 
     * 		findBreak as start2 or end2.
     */

    /** Splice two pieces of the stream, so that between them a division
     * of this level is maintained.
     */
    ZZCell splice(ZZCell p1, ZZCell p2);
}




