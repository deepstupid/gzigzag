/*   
SimpleBoxType.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka, BoxType adaption by Benja Fallenstein
 */

package org.gzigzag;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** A simple BoxType looking like the old CellBgFlobs.
 * Draws a filled (possibly with several colors) background rectangle,
 * surrounded by a rectangle of the current foreground color.
 */

public class SimpleBoxType extends BoxType {
public static final String rcsid = "$Id: SimpleBoxType.java,v 1.2 2001/03/18 17:50:16 bfallenstein Exp $";
    public static final boolean dbg = true;
//    static final void p(String s) { if(dbg) System.out.println(s); }
//    static final void pa(String s) { System.out.println(s); }

    public void renderBg(Graphics g, BoxedFlob f, int mx, int my, 
			 int md, int mw, int mh) {
	Color oldfg = g.getColor();

	if(f.solids != null) {
	    for(int i=0; i<f.nsolids; i++) {
		g.setColor(f.solids[i]);
		g.fillRect(mx+(mw*i)/f.nsolids, my, mw/f.nsolids+1, mh);
	    }
	} else {
	    g.setColor(bg);
	    g.fillRect(mx, my, mw, mh);
	}
	g.setColor(oldfg);
    }

    public void renderFrame(Graphics g, BoxedFlob f, int mx, int my,
			    int md, int mw, int mh) {
	g.drawRect(mx-1, my-1, mw, mh);
	if(mh >= 14) {
	    g.drawRect(mx-2, my-2, mw+2, mh+2);
	}
    }
}


