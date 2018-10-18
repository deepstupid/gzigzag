/*   
TextScrollBlock.java
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

/** A text scroll block.
 */

public abstract class TextScrollBlock extends ScrollBlock {
String rcsid = "$Id: TextScrollBlock.java,v 1.4 2001/08/07 21:16:43 bfallenstein Exp $";

    public TextScrollBlock(String id) { super(id); }

    public abstract TextSpan append(char ch) throws ImmutableException;

    /** Get the internal character array of this scrollblock.
     * This is used for speeding up drawing strings from a TextScrollBlock
     * by being able to give a scrollblock and two offsets to the drawing 
     * routines.
     * <p>
     * <b><big>THE RETURNED CHARACTER ARRAY MUST NOT BE MODIFIED UNDER
     *   <font size="+3">ANY</font> CIRCUMSTANCES.
     * </b>
     */
    public abstract char[] getCharArray();

    /** Get a span that represents the given range inside this scrollblock.
     *  @param offs The offset the span should start at.
     *  @param len The length of the span.
     */
    public abstract Span getSpan(int offs, int len);
}

