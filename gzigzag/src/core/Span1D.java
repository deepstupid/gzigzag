/*   
Span1D.java
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

/** A 1-dimensional span inside one document.
 * The different routines use "natural units" the definition
 * of which depends on the span type - see the appropriate subinterfaces.
 */

public interface Span1D extends Span {
String rcsid = "$Id: Span1D.java,v 1.6 2001/08/06 16:40:24 bfallenstein Exp $";

    /** Get the offset of this span inside the Mediaserver block,
     *  in natural units starting at zero.
     */
    int offset();

    /** Get the length of this span, in natural units.
     */
    int length();

    /** Return the subSpan starting at o1 in natural units 
     * and ending just before o2,
     * analogous to java.lang.String.substring().
     */
    Span1D subSpan(int o1, int o2);

    /** Return the subSpan starting at o1 in natural units 
     * and ending at the end
     * of this span,
     * analogous to java.lang.String.substring().
     */
    Span1D subSpan(int o1);

    /** Get the start of the given subspan, relative to the start of this.
     *  subspan must be wholly contained in this span; otherwise, an error
     *  is thrown.
     */
    int getRelativeStart(Span1D subspan);

    /** Get the end of the given subspan, relative to the start of this.
     *  subspan must be wholly contained in this span; otherwise, an error
     *  is thrown.
     */
    int getRelativeEnd(Span1D subspan);

    /** Return the span that results from appending the other span
     * to this span, <em>if</em> the resulting span is contiguous.
     * Otherwise, returns null.
     */
    Span1D append(Span s);

}


