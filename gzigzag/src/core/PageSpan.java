/*   
PageSpan.java
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
import java.awt.*;

/** A page span - contiguous piece of a permascroll consisting of
 * pages of the same size - e.g.postscript or pdf file. 
 * These spans are rectangular regions of one or more pages.
 * <p>
 * ImageSpan.getImage should return the image of a page when a single
 * page is in the span, calling getImage for a PageSpan of more than one
 * page is currently undefined (!!), waiting for a decision between showing
 * the first page, the first page in a graphical representation showing
 * a stack of pages, some type of splicing of the pages or whatever.
 * It is also possible that it will be decided that an IllegalArgumentException
 * should be thrown in this case.
 */

public interface PageSpan extends ImageSpan, Span1D {
    /** Get a subspan of the current span.
     * The coordinates are <b>relative to this span</b>.
     */
    PageSpan subArea(int page0, int page1, int x, int y, int w, int h);
    PageSpan subArea(int page0, int page1);
}

