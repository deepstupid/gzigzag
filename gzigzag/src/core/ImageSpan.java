/*   
ImageSpan.java
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

/** An image span - contiguous piece of a permascroll -. 
 * Spans in images are rectangular regions.
 */

public interface ImageSpan extends Span {

    /** Get a drawable image for this span.
     * This method may return null if image not yet loaded / created.
     * This is because we can't easily put ourselves into the 
     * Java Image class. 
     * <p>
     * The class which loads the images should naturally call 
     * UpdateManager.chg() to 
     */
    Image getImage();

    /** Get the location of this span in the mediaserver block.
     */
    Point getLocation();

    /** Get the size of the image in pixels, at the default resolution.
     */
    Dimension getSize();

    /** Get a subspan of the current span.
     * The coordinates are <b>relative to this span</b>.
     */
    ImageSpan subArea(int x0, int y0, int w, int h);

}
