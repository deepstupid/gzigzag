/*   
LogicalSpan.java
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

/** A span that doesn't correspond to a single fixed 
 * piece of media but rather a large chunk of the 
 * permascroll space.
 * <p>
 * The span is delimited by two physicalspans, which <b>shall not</b>
 * go over each other. They may overlap, but end of the start span must
 * be earlier than the end of the end span etc. Of course, this is
 * only relevant when the physical spans are in the same scrollblock.
 */

public final class SpanArea {

    public final Span start;
    public final Span end;

    private SpanArea(Span start, Span end) {
	this.start = start;
	this.end = end;
    }


    static SpanArea hull(Span s1, Span s2) {
	return null;
    }

    /** Return the smallest span that encompasses both this and
     * the given span.
     * Note that this may be a VERY large span, containing
     * all of the material in the system and anywhere on the net.
     * The reasonability of this operation should be checked first.
     * <p>
     * Also note that the hull will not be a span of any specific
     * type: 
     */
    SpanArea hull(Span s) {
	return null;
    }


    /** Whether this span and the given span intersect.
     */
    boolean intersects(Span s) {
	return false;
    }

    /** Whether this span and the given spanarea intersect.
     */
    boolean intersects(SpanArea s) {
	return false;
    }

}
