/*   
Vob.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag.vob;
import java.awt.*;

/** A two-dimensional renderable, interpolatable object.
 * This is simply a thing that is able to render itself.
 * Instead of just painting stuff we create a temporary structure of these
 * in order to be able to paint all the data in the correct depth-order.
 * <p>
 * The key field is what identifies different Vobs to be representing
 * the same object in different frames.
 */

public abstract class Vob {
public static final String rcsid = "$Id: Vob.java,v 1.18 2001/12/15 02:40:36 tuukkah Exp $";

    /** A class that stores the coordinates for a vob.
     * Extends the Rectangle class by a single public depth field.
     */
    public static class Coords extends Rectangle {
	/** The depth-value of the vob in the vobset.
	 */
	public int depth;
	public boolean visible = true;

	/** Return the center point of these coordinates.
	 */
	public Point getCenter(Point p) {
	    if(p == null) p = new Point();
	    p.x = this.x + this.width / 2;
	    p.y = this.y + this.height / 2;
	    return p;
	}
    }

    /** The key associated with this vob.
     * This may be set to null for things that are - well - just
     * things rendered. 
     * Keys are compared with the Object.equals method.
     */
    public Object key; // Not final: CharArrayVob is its own key...

    /** Creates a vob which will represent some object in a visualization. Two
     *  vobs representing the same object can be interpolated in-between, and
     *  the represented object can be used when directing user interface events
     *  back to the underlying model.
     *  @param key the object this vob represents
     */
    public Vob(Object key) {
	this.key = key;
    }

    /** Renders this vob at the given screen coordinates and in given size. 
     * XXX Too many params?
     * @param g The graphics context to draw into
     * 		The color should already be set to the default foreground 
     *          color, mixed by the caller. 
     * @param x vob top-left corner x-coordinate     
     * @param y vob top-left corner x-coordinate
     * @param w width of the vob in pixels
     * @param h height of the vob in pixels
     * @param boxDrawn Whether a box background has been drawn.
     *			If false, the background is transparent and
     *			this Vob may draw itself differently,
     *			e.g. by drawing a border or drop-shadow around text
     *			to clarify the visual appearance.
     * @param info General parameters about rendering.
     * @see VobPlacer#put(Vob vob, int depth, int x, int y, int w, int h)
     * @see RenderInfo
     */
    abstract public void render(Graphics g, 
				int x, int y, int w, int h,
				boolean boxDrawn,
				RenderInfo info
				);


    /** An interface which provides information about the current 
     * rendering context.
     * This is given as a parameter to Vob.render() and can be used
     * to get various parameters.
     */
    public static abstract class RenderInfo {
	/**The background color of the canvas where we're being drawn.
	 * 		 What about background images?!?
	 */
	public abstract Color getBgColor();

	/** The fraction by which we should mix our colors
	 *			with the bg color.
	 * 0 = use real color, 1 = use background color fully
	 */
	public abstract float getBgFract();

	/** Get the default foreground color, already mixed with
	 * the background color in the correct proportion.
	 * This is the color that the java.awt.Graphics object
	 * passed to Vob.render should have set as its foreground.
	 */
	public abstract Color getMixedFgColor();

	/** Whether to draw a quick version for e.g. animation.
	 *			XXX Should be numeric?
	 */
	public abstract boolean isFast();
	
	/** Get the coordinates of the given vob, possibly interpolated.
	 *  This is needed so that we can draw connections during
	 *  interpolation: VobScene.getCoords() doesn't work when interpolating.
	 *  As there is no state in VobScene specifying anything like "the
	 *  current interpolation fraction" or "the vobset we currently
	 *  interpolate to," the correct place to put this is here.
	 *  @returns false if this vob isn't currently shown (because it's
	 *           not interpolated).
	 */
	public abstract boolean getInterpCoords(Vob vob, Vob.Coords writeInto);
    }

}


