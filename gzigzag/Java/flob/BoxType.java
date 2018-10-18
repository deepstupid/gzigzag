/*   
BoxType.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2001, Benja Fallenstein
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
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** A type of boxes to be drawn around vobs (like the CellBgFlob backgrounds).
 *  Default: render nothing.
 */

public class BoxType {
public static final String rcsid = "$Id: BoxType.java,v 1.2 2001/03/18 17:50:16 bfallenstein Exp $";
    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    protected Color bg = Color.white;
    public void setBg(Color c) { if( c != null ) bg = c; }

    protected void renderBg(Graphics g, BoxedFlob f, int mx, int my, 
			    int md, int mw, int mh) {}

    protected void renderFrame(Graphics g, BoxedFlob f, int mx, int my, 
			       int md, int mw, int mh) {}
					
    /** Render the background behind the flob.
     */
    public void renderBg(Graphics g, Flob f) {
	renderBg(g, (BoxedFlob)f, f.x, f.y, f.d, f.w, f.h);
    }

    /** Render the frame around the flob.
     */
    public void renderFrame(Graphics g, Flob f) {
	renderFrame(g, (BoxedFlob)f, f.x, f.y, f.d, f.w, f.h);
    }

    /** Render the interpolated background behind the flob.
     */
    public void renderBg(Graphics g, Flob f, float fract) {
	// XXX the interpolation path isn't necessarily linear!
	Flob r = f.interpTo;
	renderBg(g, (BoxedFlob)f,
	    (int)(f.x + fract*(r.x-f.x)),
	    (int)(f.y + fract*(r.y-f.y)),
	    (int)(f.d + fract*(r.d-f.d)),
	    (int)(f.w + fract*(r.w-f.w)),
	    (int)(f.h + fract*(r.h-f.h))
	);
    }

    /** Render the interpolated frame around the flob.
     */
    public void renderFrame(Graphics g, Flob f, float fract) {
	// XXX the interpolation path isn't necessarily linear!
	Flob r = f.interpTo;
	renderFrame(g, (BoxedFlob)f,
	    (int)(f.x + fract*(r.x-f.x)),
	    (int)(f.y + fract*(r.y-f.y)),
	    (int)(f.d + fract*(r.d-f.d)),
	    (int)(f.w + fract*(r.w-f.w)),
	    (int)(f.h + fract*(r.h-f.h))
	);
    }
}


