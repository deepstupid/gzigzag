/*   
AlmanacBgVob.java
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
    
public class AlmanacBgVob extends BgVob {
public static final String rcsid = "$Id: AlmanacBgVob.java,v 1.4 2002/03/02 17:43:33 vegai Exp $";
    
    Color color;
    
    
    public AlmanacBgVob(Cell cell, 
			int x0, int x1, int y0, int y1, int zoom) {
	super(cell, x0, x1, y0, y1, zoom);
    }
    
    public void render(Graphics g,
		       int x, int y, int w, int h,
		       boolean boxDrawn,
		       RenderInfo info
		       ) {
	final Color old = g.getColor();
	Rectangle oldClip = null;
	
	final int width = 96;  // the size of 
	final int height= 128; // one box
	
	final int pad = 32 * zoom/1000; // padding between months
	
	final int ww = (7*width+pad) * zoom/1000;  // width and height
	final int wh = (6*height+pad) * zoom/1000; // of a month
	
	
	int xoffset = -(x0 % (7*width+pad) * zoom/1000);
	if(xoffset<0) 
	    xoffset += ww;
	int yoffset = -(y0 % (6*height+pad) * zoom/1000);
	if(yoffset<0)
	    yoffset += wh;
	
	// Clear background
	super.render(g, x, y, w, h, boxDrawn, info);
	
	if(clip != null) {
	    oldClip = g.getClipBounds();
	    g.setClip(clip);
	}
	
	
	// Draw background (almanack)
	
	if(color != null)
	    g.setColor(color);
	
	Color realCol = g.getColor();
	
	for(int i=-1; i<(w-xoffset)/ww+1; i++)
	    for (int j=-1; j<(h-yoffset)/wh+1; j++) {
		final int xx = x+ww*i+xoffset+pad;
		final int yy = y+wh*j+yoffset+pad;
		
		g.setColor(Color.red); // sundays -> red
		g.fillRect(xx+6*width*zoom/1000, yy+height*zoom/1000, width*zoom/1000, 5*height*zoom/1000); 
		g.setColor(realCol);
		
		for (int k=1; k<6; k++) // vertical lines
		    g.drawLine(xx+width*k*zoom/1000, yy+height*zoom/1000, xx+width*k*zoom/1000, yy+wh-pad);
		for (int k=1; k<6; k++) // horizontal lines
		    g.drawLine(xx, yy+height*k*zoom/1000, xx+ww-pad, yy+height*k*zoom/1000);
		
		g.drawRect(xx, yy, ww-pad, wh-pad);
	    }
	
	if(color != null)
	    g.setColor(old);
	if(oldClip != null)
	    g.setClip(oldClip);
    }
    
}
