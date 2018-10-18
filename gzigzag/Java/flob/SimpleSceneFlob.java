/*   
SceneFlob.java
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
package org.gzigzag;
import java.util.*;
import java.awt.*;

/** A simple SceneFlob which just draws the given FlobSet, untransformed.
 */

public class SimpleSceneFlob extends SceneFlob {
public static final String rcsid = "$Id: SimpleSceneFlob.java,v 1.4 2001/03/18 17:50:16 bfallenstein Exp $";
    public static final boolean dbg = true;
//    static final void p(String s) { if(dbg) System.out.println(s); }
//    static final void pa(String s) { System.out.println(s); }

    public SimpleSceneFlob(int x, int y, int d, int w, int h, ZZCell c, FlobSet sc) {
	super(x,y,d,w,h,c,sc);
    }

    /** Transform the given points between the internal and external coordinates.
     * @param toExt Whether to transform internal to external or vice versa.
     */
    public void transform(double[] srcPtr, double[]dstPts, boolean toExt) {
	if(toExt)
	    for(int i=0; i<srcPtr.length; i+=2) {
		dstPts[i] = srcPtr[i]-x; 
		dstPts[i+1] = srcPtr[i+1]-y;
	    }
	else
	    for(int i=0; i<srcPtr.length; i+=2) {
		dstPts[i] = srcPtr[i]+x; 
		dstPts[i+1] = srcPtr[i+1]+y;
	    }
    }

    /** Transform the given point between the internal and external coordinates.
     * @param toExt Whether to transform internal to external or vice versa.
     */
    public Point transform(Point src, Point dst, boolean toExt) {
	if(toExt) {
	    dst.x = src.x + x;
	    dst.y = src.y + y;
	} else {
	    dst.x = src.x - x;
	    dst.y = src.y - y;
	}
	return dst;
    }

    /** Make this graphics so that we can render into it with the correct
     * transformation.
     */
    void prepareGraphics(Graphics g, float interpFract) {
	if(interpFract == 0) {
	    g.translate(x, y);
	    g.setClip(0, 0, w, h);
	} else {
	    g.translate(
		(int)(x + interpFract*(interpTo.x-x)),
		(int)(y + interpFract*(interpTo.y-y))
	    );
	    g.setClip(0, 0,
		(int)(w + interpFract*(interpTo.w-w)),
		(int)(h + interpFract*(interpTo.h-h))
	    );
	}
    }
}
