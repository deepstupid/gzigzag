/*   
Flob.java
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

/** An abstract class to represent a flob shown on screen.
 * Flobs are constant read-only objects like Strings.
 * <p>
 * Flobs should be able to render themselves quickly for animations; 
 * caching relevant
 * information in this structure is therefore a good idea.
 * <p>
 * The minimal flob implementation simply implements the
 * render(Graphics, int, int, int, int, int) method.
 */

public abstract class Flob extends Renderable {
public static final String rcsid = "$Id: Flob.java,v 1.18 2001/03/17 23:08:23 bfallenstein Exp $";
    public int x, y, w, h;

    /** The identifier of the place this flob is.
     * This is used to create a possibly hierarchical 
     * system so that animation doesn't confuse two
     * instances of the same cell.
     * deprecated Use setParent() / getParent().
     */
    public String flobPath = "";

    /** The cell associated with this flob.
     * This may be set to null for things that are - well - just
     * things rendered. But those should be in a minority always.
     */
    public ZZCell c;

    /** The flob we are interpolating with.
     * Storing it here is a pure speed hack: in reality it'd belong in 
     * a hashtable somewhere. But this is crucial for the frame rate.
     */
    protected Flob interpTo;

    /** The parent of this flob.
     * Only flobs with no parents and flobs whose parents are interpolated
     * to each other are interpolated to each other.
     * <p>
     * set/get by setParent() / getParent()
     */
    private Flob flobparent = null;

    /** The applitude that has placed this flob.
     * When the views of different applitudes are combined, this field is set
     * by the combining view. When a flob is clicked upon, the field is read,
     * the applitude is made the window's prefered applitude, and the mouse
     * binding is executed according to the bindings of that applitude.
     */
    public ZZCell applitude = null;

    public void setParent(Flob p) {
	flobparent = p;
	flobPath = p.flobPath + "-" + p;
    }

    public Flob getParent() { return flobparent; }

    /** Return true if a box should be drawn around this flob.
     */
    public boolean needsBox() { return false; }

    public Flob(int x, int y, int d, int w, int h, ZZCell c) {
	this.x = x;
	this.y = y;
	this.w = w;
	this.h = h;
	this.d = d;
	this.c = c;
    }

    /** Get a point aligned in a cell.
     *  getPoint(-1, 0) --> left/middle
     *  getPoint(1, 1)  --> right/lower
     *  etc. -- middle means visual center (getCenter()).
     */
    public Point getPoint(Point p, int xa, int ya) {
	getCenter(p);
	if(xa < 0) p.x = x;
	if(xa > 0) p.x = x+w;
	if(ya < 0) p.y = y;
	if(ya > 0) p.y = y+h;
	return p;
    }
    public Point getPoint(int xo, int yo) {
	return getPoint(new Point(), xo, yo);
    }

    /** Get the visual center of this flob.
     */
    public Point getCenter(Point p) {
	p.x = x+w/2; p.y = y+h/2;
	return p;
    }

    /** Get the visual center of this flob.
     */
    public Point getCenter() {
	return getCenter(new Point());
    }

    /** Render this flob at the given coordinates. 
     * Default: render nothing.
     * You may override either this function or one or both of
     * render(Graphics) and renderInterp(Graphics, float).
     */
    public void render(Graphics g, int mx, int my, int md,
			int mw, int mh) {
    }

    /** Render this flob.
     * May be overridden; default implementation uses render(g, x, y, w, h, d);
     */
    public void render(Graphics g) {
	render(g, x, y, d, w, h);
    }
    /** Render an interpolated version of this flob.
     * Default implementation uses render(g, x, y, w, h, d) with linearly
     * interpolated coordinates.
     */
    public void renderInterp(Graphics g, float fract) {
	Flob r = interpTo;
	if(r==null) return;
	render(g, 
		(int)(x + fract*(r.x-x)),
		(int)(y + fract*(r.y-y)),
		(int)(d + fract*(r.d-d)),
		(int)(w + fract*(r.w-w)),
		(int)(h + fract*(r.h-h))
		);
    }

    /** If there is a XOR-cursor associated with this flob,
     * render it.
     * This can be made to e.g. follow the mouse cursor to
     * show where the click would land.
     * Default implementation: do nothing, return 
     * <pre> hit(x,y)!=null </pre>
     * @return Whether the coordinates hit this flob, i.e.
     * 		whether the system should stop looking for the
     *		XOR curser.
     */
    public boolean renderXOR(Graphics g, int x, int y) { 
	return hit(x,y) != null;
    }

    /** Whether it would make visually sense to interpolate
     * this flob.
     * Default implementation: whether the sum of the squares of
     * the differences of the coordinates and widths is more than 100.
     * Not a terribly useful measure but good enough.
     */
    public boolean isInterpUseful() {
    Flob r = interpTo;
    if(r==null) return false;
    return (r.x - x) * (r.x - x) +
	    (r.y - y) * (r.y - y) +
	    (r.w - w) * (r.w - w) +
	    (r.h - h) * (r.h - h) > 100;
    }


    /** Returns an object telling what was hit.
     * Default implementation: returns null.
     */
    public Object hit(int x, int y) { return null; }

    /** A convenience routine to check whether a point 
     * is inside the defining rectangle.
     * If the flob is (as many are) rectangular, this routine can 
     * be used to check the hit.
     */
    public final boolean insideRect(int x0, int y0) {
	return x0 >= x && y0 >= y && x0 < x+w && y0 < y+h;
    }
}
