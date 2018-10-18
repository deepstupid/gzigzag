/*   
SpanVob.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
import org.gzigzag.vob.*;

/** An interface to vobs connected to some span.
 */

public interface SpanVob {
String rcsid = "$Id: SpanVob.java,v 1.1 2001/10/21 12:58:12 tjl Exp $";

    /** Get the span associated with this vob. */
    Span getSpan();

    /** Get the part of this vob corresponding to <code>subspan</code>.
     *  <code>subspan</code> must be wholly contained in what
     *  <code>getSpan()</code> returns.
     */
    SpanVob getPart(Span subspan);

    /** Get the coordinates of the part of this vob corresponding to subspan.
     *  x, y, w and h are the coordinates of this span as a whole.
     *  <p>
     *  <code>subspan</code> must be wholly contained in what
     *  <code>getSpan()</code> returns.
     *  @param out An existing rectangle object to write the coords into.
     */
    void getPartCoords(int x, int y, int w, int h, Span subspan,
		       java.awt.Rectangle out);
}


