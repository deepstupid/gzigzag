/*
CellBgVob.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** A vob for the cell background.
 * Draws a filled (possibly with several colors) background rectangle,
 * surrounded by a rectangle of the current foreground color.
 */

public abstract class CellBgVob extends DecoratedVob {
public static final String rcsid = "$Id: CellBgVob.java,v 1.2 2001/12/15 07:53:15 tuukkah Exp $";

    boolean drawBorder = true;

    public Cell c;

    int nsolids = 0;
    Color[] solids;
    protected Color bg = Color.white;

    public void setBg(Color c){ if( c != null ) bg = c; }


    /** Override this in subclasses to render something inside the cell.
     */
    protected abstract void renderContent(Graphics g, int mx, int my, int mw, int mh, Vob.RenderInfo info);

    public void render(Graphics g, int mx, int my, int mw, int mh,
		       boolean boxDrawn, Vob.RenderInfo info) {

	// p("ZZCC1 render: "+g+" "+mx+" "+my+" "+mw+" "+mh);
	Color oldfg = g.getColor();
	// Shape oldclip = g.getClip();

	renderDecorations(g, mx, my, mw, mh, info);

	// XXX save font?

	// Don't clip here - let the subclass take care of that.
	// g.clipRect(mx, my, mw, mh);

	//      if(solids == null) {
	// if (drawBorder) g.setColor(bg); else g.setColor(info.getBgColor());
	g.setColor(bg);
	g.fillRect(mx, my, mw, mh);
	//	} else {
	for(int i=0; i<nsolids; i++) {
	    g.setColor(solids[i]);
	    g.fillRect(mx+(mw*i)/nsolids, my+mh/2, mw/nsolids, mh/2);
	}

	//	}

	g.setColor(info.getMixedFgColor());

	renderContent(g, mx, my, mw, mh, info);

	// XXX Span indication???

	if(drawBorder) {
	    g.drawRect(mx, my, mw-1, mh-1);
	    if(mh >= 14) {
	    	g.drawRect(mx+1, my+1, mw-3, mh-3);
	    }
	}
	// g.setClip(oldclip);
	g.setColor(oldfg);
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

    public CellBgVob(Cell c, CellConnector connector) {
	super(c, connector);
	this.c = c;
    }
}


