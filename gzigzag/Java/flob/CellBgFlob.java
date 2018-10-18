/*   
CellBgFlob.java
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

/** A flob for the cell background.
 * Draws a filled (possibly with several colors) background rectangle,
 * surrounded by a rectangle of the current foreground color.
 */

public class CellBgFlob extends Flob implements Colorer {
public static final String rcsid = "$Id: CellBgFlob.java,v 1.16 2001/02/26 18:03:29 raulir Exp $";
    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    boolean drawBorder = true;

    int nsolids = 0;
    Color[] solids;
    protected Color bg = Color.white;

    public Object hit(int x0, int y0) {
	if( x0 >= x && y0 >= y && x0 < x+w && y0 < y+h )
	    return c;
	return null;
    }

    public void setBg(Color c){ if( c != null ) bg = c; }

    /** Override this in subclasses to render something inside the cell.
     */
    protected void renderContent(Graphics g, int mx, int my, int mw, int mh) {
    }

    public void render(Graphics g, int mx, int my, int md, int mw, int mh) {

	// p("ZZCC1 render: "+g+" "+mx+" "+my+" "+mw+" "+mh);
	Color oldfg = g.getColor();
	Shape oldclip = g.getClip();

	// XXX save font?

	g.clipRect(mx, my, mw, mh);

	if(solids != null) {
	    for(int i=0; i<nsolids; i++) {
		g.setColor(solids[i]);
		g.fillRect(mx+(mw*i)/nsolids, my, mw/nsolids+1, mh);
	    }
	} else {
	    g.setColor(bg);
	    g.fillRect(mx, my, mw, mh);
	}

	g.setColor(oldfg);

	renderContent(g, mx, my, mw, mh);

	// XXX Span indication???

	if(drawBorder) {
	    g.drawRect(mx, my, mw-1, mh-1);
	    if(mh >= 14) {
	    	g.drawRect(mx+1, my+1, mw-3, mh-3);
	    }
	}
	g.setClip(oldclip);
    }

    /** Adds one more solid color to be drawn inside
     * the cell.
     */
    public boolean addColor(Color c) {
	if(solids == null || nsolids >= solids.length) {
	    Color[] n= new Color[nsolids + 10];
	    if(solids != null) System.arraycopy(solids, 0, n, 0, nsolids);
	    solids = n;
	}
	solids[nsolids++] = c;
	return false;
    }
    /** The currently put solid colors.
     * null = none. There may be null references near the end
     * of the array. Mostly useful for checking for nullness.
     */
    public Color[] getSolidColors() { return solids; }

    public CellBgFlob(int x, int y, int d, int w, int h, 
	    ZZCell c) {
	super(x, y, d, w, h, c);
    }
}


