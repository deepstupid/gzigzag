/*   
Span.java
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
 * Written by Tuomas Lukka
 */

package org.gzigzag;

/** An address span in the stable media streams.
 * This is abstract since we have different kinds of spans for
 * text, images and video, with different operations.
 * <p>
 * Spans are immutable, just like Strings: all the verb-like methods
 * such as join and split return new Span objects.
 * <p>
 * Spans are always of some type, like TextSpan or ImageSpan,
 * and belong to a particular ScrollBlock.
 * <p>
 * The operations for comparing spans are somewhat interesting.
 * XXX Something not right...
 * <img src="../../../doc-images/spancomp-1.jpg"/>
 * <img src="../../../doc-images/spancomp-2.jpg"/>
 * Important invariants:
 * <pre>
 *  	a.intersectsAfter(b) == b.intersectsBefore(a)
 *  	a.intersects(b) == a.intersectsBefore(b) &amp;&amp; a.intersectsAfter(b)
 * </pre>
 * @see Span
 * @see Span1D
 * @see TextSpan
 * @see ImageSpan
 * @see ScrollBlock
 */

public interface Span {
String rcsid = "$Id: Span.java,v 1.6 2001/05/15 04:33:13 tjl Exp $";

    String toString();

    /** Whether this span and the given span intersect.
     */
    boolean intersects(Span s);

    /** Get the ScrollBlock that this span points to.
     */
    ScrollBlock getScrollBlock();

    /** If the given span is contained in the same scrollblock after this. 
     * See the images above.
     */
    // boolean intersectsAfter(Span s);

    /** If the given span is  contained in the same scrollblock before this. 
     */
    // boolean intersectsBefore(Span s);

    // int compareStart(Span s);
    // int compareEnd(Span s);


}

