/*   
GridBgVob.java
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
 * Written by Vesa Kaihlavirta
 */
package org.gzigzag.modules.pp.vob;
import org.gzigzag.*;
import java.awt.*;
	
public class GridBgVob extends BgVob {
public static final String rcsid = "$Id: GridBgVob.java,v 1.8 2002/03/14 00:03:24 vegai Exp $";

    private Color color;
    private final int panx, pany;
    private int x, y, w, h;
    private void pa(String x) { System.out.println(x); }
    
    public GridBgVob(Cell cell, 
		     int x0, int x1, int y0, int y1, int panx, int pany, int zoom) {
	super(cell, x0, x1, y0, y1, zoom);
	if(bgcolor != null) 
	    color = bgcolor.darker();
	else
	    color = Color.gray;
	this.panx=panx;
	this.pany=pany;
    }

    // scrx, scry return coordinates relative to
    // Vob's center, [scrx(0), scry(0]
    // zoom is applied *here*, ELSEWHERE, NOT!
    private int scrx(int xx) {
	return xx+x+w/2 + x0*zoom/1000;
    } 
    private int scry(int yy) {
	return yy+y+h/2 + y0*zoom/1000;
    }

    public void render(Graphics g,
		       int _x, int _y, int _w, int _h,
		       boolean boxDrawn,
		       RenderInfo info
		       ) {
	x=_x; y=_y; w=_w; h=_h;
	final int size = 16; // size of one square
	final double dSize = size * zoom/1000 + 0.00000001;
	final int xoff = panx % size - w/2;    // offsets
	final int yoff = pany % size - h/2;
	Rectangle oldClip = null;


	// Clear background
	super.render(g, x, y, w, h, boxDrawn, info);
	if (clip != null) {
	    oldClip = g.getClipBounds();
	    g.setClip(clip);
	}

	// draw!
	int temp;
	for (int i=0; i<w/dSize; i++){
	    temp = scrx(panx+xoff+size*i);
	    g.drawLine( temp, scry(-h/2), temp, scry(h/2) );
	}
	for (int i=0; i<h/dSize; i++) {
	    temp = scry(pany+yoff+size*i);
	    g.drawLine( scrx(-w/2), temp, scrx(w/2), temp );
	}

	if(oldClip != null) g.setClip(oldClip);
    }
}

