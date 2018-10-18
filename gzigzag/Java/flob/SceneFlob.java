/*   
SceneFlob.java
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
import java.util.*;
import java.awt.*;

/** Flob which contains flobset, possibly transformed.
 * This is subclassed by e.g. ZZDrawingJ2D.
 */

public abstract class SceneFlob extends BoxedFlob {
public static final String rcsid = "$Id: SceneFlob.java,v 1.10 2001/04/30 09:31:33 tjl Exp $";
    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    FlobSet sc;

    public SceneFlob(int x, int y, int d, int w, int h, ZZCell c, FlobSet sc) {
	super(x,y,d,w,h,c);
	this.sc = sc;
	sc.sflob = this;
    }

    /** The flobs that are important to interpolate
     * even when level-of-detail is small
     */
    Flob[] important;

    /** Set the important-to-interpolate flobs.
     */
    public void setImportant(Flob[] what) {
	important = what;
    }

    /** Whether this is a focused sceneflob, i.e.whether level-of-detail
     * is high.
     */
    boolean focus;

    public void setFocus(boolean focus) {
	this.focus = focus;
    }

    /** The other sceneflob we are currently prepared to interpolate to.
     */
    SceneFlob interpPrep;

    /** The scene flob this one is in.
     *  This is needed to convert coordinates to top-level; and that is needed
     *  to create connections between windows.
     */
    SceneFlob parent;

    /** Transform the given points between the internal and external coordinates.
     * @param toExt Whether to transform internal to external or vice versa.
     */
    public abstract void transform(double[] srcPtr, double[]dstPts, boolean toExt);

    /** Transform the given point between the internal and external coordinates.
     * @param toExt Whether to transform internal to external or vice versa.
     */
    public abstract Point transform(Point src, Point dst, boolean toExt);

    /** Transform the given points between the internal and top-level coordinates.
     * @param toTop Whether to transform internal to top-level or vice versa.
     */
    public void transformTop(double[] srcPtr, double[] dstPts, boolean toTop) {
	if(toTop) transform(srcPtr, dstPts, true);
	if(parent != null)
	    parent.transformTop(srcPtr, dstPts, toTop);
	if(!toTop) transform(srcPtr, dstPts, false);
    }

    /** Transform the given point between the internal and top-level coordinates.
     * @param toTop Whether to transform internal to top-level or vice versa.
     */
    public Point transformTop(Point src, Point dst, boolean toTop) {
	if(toTop) transform(src, dst, true);
	if(parent != null)
	    parent.transformTop(src, dst, toTop);
	if(!toTop) transform(src, dst, false);
	return dst;
    }

    /** Make this graphics so that we can render into it with the correct
     * transformation.
     */
    abstract void prepareGraphics(Graphics g, float interpFract);
    
    public void render(Graphics g0) { render(g0, g0); }
    public void render(Graphics g0, Graphics orig) {
	Graphics g = g0.create();
	prepareGraphics(g, 0);
	sc.render(g, orig);
	g.dispose();
    }

    public void renderInterp(Graphics g0, float fract) {
	if(!(interpTo instanceof SceneFlob)) return;
	// p("sceneflob renderinterp");
	// Here we test the level-of-detail stuff.
	if(interpTo != interpPrep) {
	    interpPrep = (SceneFlob)interpTo;
	    // p("Prepinterp sceneflob: "+focus+" "+interpPrep.focus+" "+important);
	    if(focus || interpPrep.focus || important == null)
		sc.prepareInterp(interpPrep.sc);
	    else
		sc.prepareInterp(interpPrep.sc, important);
	}
	Graphics g = g0.create();
	prepareGraphics(g, fract);
	sc.renderInterp(g, ((SceneFlob)interpTo).sc, fract);
	g.dispose();
    }

    public boolean isInterpUseful() { return true; }

    public Object hit(int x, int y) {
	    Point p = new Point(x,y);
	    Point d = new Point();
	    transform(p,d, false);
	    return sc.getObjectAt(d.x, d.y);
    }

    public FlobSet getFlobSet(){ return sc;}

}
