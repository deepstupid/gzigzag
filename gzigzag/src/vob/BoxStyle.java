/*   
BoxStyle.java
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
package org.gzigzag.vob;
import org.gzigzag.*;
import java.awt.*;

/** A style for boxes to draw around sub-vobsets.
 *  This class is resposible for drawing a background under and a frame above
 *  some vobset or vob. Default: render nothing.
 */

public class BoxStyle {
public static final String rcsid = "$Id: BoxStyle.java,v 1.2 2001/05/02 10:38:48 tjl Exp $";

    /** Render a box background at the given coordinates.
     *  Default: render nothing.
     * @param g The graphics context to draw into
     * 		The color should be set to the default foreground color,
     *		already mixed.
     * @param x,y,w,h The coordinates and size of the "interesting area".
     *                Note that the box must be drawn <em>around</em> that
     *                area.
     *                XXX possibly not: here we could get&use the size passed
     *                    to <code>VobScene.createSubVobScene()</code>; the
     *                    size available for actual content of the vobset
     *                    would be requested from the BoxStyle at sub-vobset
     *                    creation time.
     * @param info General parameters about rendering.
     */
    public void renderBg(java.awt.Graphics g,
			 int x, int y, int w, int h,
			 Vob.RenderInfo info) {
    }

    /** Render a box frame at the given coordinates.
     *  Default: render nothing.
     * @param g The graphics context to draw into
     * 		The color should be set to the default foreground color,
     *		already mixed.
     * @param x,y,w,h The coordinates and size of the "interesting area".
     *                Note that the box must be drawn <em>around</em> that
     *                area.
     * @param info General parameters about rendering.
     */
    public void renderBox(java.awt.Graphics g,
			  int x, int y, int w, int h,
			  Vob.RenderInfo info) {
    }

    /** Whether the background of this box is transparent.
     *  Used to request whether special visual enhancements need to be rendered
     *  (like drop shadows for text).
     */
    public boolean isTransparent() {
	return true;
    }
}


