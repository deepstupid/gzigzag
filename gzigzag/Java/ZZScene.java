/*   
ZZScene.java
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

/** An interface for obtaining clicked -objects based on coordinates.
 * This is what is given with mouse events.
 */

public interface ZZScene {
String rcsid = "$Id: ZZScene.java,v 1.4 2000/09/19 10:31:58 ajk Exp $";

    /** Get an identifying object for the given point on this canvas.
     * This is one point where OO decoupling doesn't work properly:
     * different things want to return different kinds of information:
     * CellThings want to return the cell, TextSpans a VStream.Pos and so on.
     */
    Object getObjectAt(int x, int y);

    /** Render this canvas, things interpolated with the given other canvas,
     * on the given graphics context. 
     * Interpolation is used for great advantage to show the user what the
     * relationship between two different states is, i.e. which cells
     * are the same. The modular nature of the ZZCanvas allows Things
     * to define their own interpolation semantics: for example, by default,
     * links are not rendered at all when interpolating and cells 
     * are interpolated linearly.
     */
    void renderInterp(Graphics g, ZZScene other, float fract);


    /** Paint a cursor-like thing using XOR.
     * Originally intended for drawing a ghost cursor on text when
     * moving the mouse in order to show where a click would put the cursor.
     */
    void renderXOR(Graphics g, int x, int y);

    /** Is interpolation to the given canvas visually useful.
     * This routine returns true if there is something in this canvas
     * which will be seen animated when interpolating to the new canvas.
     * For example, when only changing a dimension, some cells stay where
     * they are and all the others are replaced by other cells (usually... 
     * there are of course situations where some cells move on the screen
     * when changing a dimension). In this case, the user is better served
     * by not animating but simply displaying the new state as fast as
     * possible.
     */
    boolean isInterpUseful(ZZScene s2);

    /** Render this scene in the given graphics context.
     */
    void render(Graphics g);
}
