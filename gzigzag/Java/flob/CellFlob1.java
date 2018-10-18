/*   
CellFlob1.java
 *    
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
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
import java.awt.event.*;
import java.util.*;

public class CellFlob1 extends BoxedFlob {
public static final String rcsid = "$Id: CellFlob1.java,v 1.16 2001/03/18 17:50:16 bfallenstein Exp $";
    public static final boolean dbg = true;

    String s;
    Font f;
    // XXX Should this *really* need to be stored here?
    FontMetrics fm;

    /** Font height. */
    int fh; 
    int fasc;
    /** String width. */
    int sw;

    ZZCell strCell;

    int xoffs = 0; // Previously, these margins where three, but at the moment
    int yoffs = 0; // margins are cared for by the BoxTypes

    static public final Dimension getSize(FontMetrics fm, String str, 
		int xoffs, int yoffs) {
	int w = fm.stringWidth(str) + xoffs * 2 + 2;
	int h = fm.getHeight() + 2*yoffs;
	if(w < h) w = h;
	return new Dimension( w, h );
    }

    public int getStrX(int ind) {
	if(fm == null) throw new Error("No fontmetrics");
	if(ind > s.length()) {
	    // XXX What to do?
	    return x + xoffs + fm.stringWidth(s);
	}
	return x + xoffs + fm.stringWidth(s.substring(0, ind)) ;
    }

    public void render(Graphics g, int mx, int my, int md, int mw, int mh) {
	Shape oldClip = g.getClip();
	g.clipRect(mx, my, mw, mh);
	int ty = my + (mh-fh)/2 + fasc;
	g.setFont(f);
	g.drawString(s, mx+xoffs, ty);
	g.setClip(oldClip);
    }

    public CellFlob1(int x, int y, int d, int w, int h, 
	    ZZCell c, ZZCell strCell, String s,
	    Font f, FontMetrics fm) {
	super(x, y, d, w, h, c);

	// Not true, since c can be the flob cell and d the referring
	// cell.
	// this.s = c.getText();

	this.s = (s==null? "" : s); 
	this.strCell = strCell;

	this.f = f;
	this.fm = fm;

	this.fh = fm.getAscent()+fm.getDescent();
	this.fasc = fm.getAscent()+fm.getLeading();
	this.sw = fm.stringWidth(this.s);
    }

    public Object hit(int x0, int y0) { 
	if(!insideRect(x0, y0)) return null;
	// Search for the right position in the string.
	// We return a virtual cursor.
	int ind = ZZUtil.findStringHit(s, x0-x-xoffs, fm);

	return new ZZCursorVirtual(strCell, ind);
    }

}

