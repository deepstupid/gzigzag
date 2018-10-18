/*   
BgVob.java
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
 * Written by Tuukka Hastrup
 */
package org.gzigzag.modules.pp.vob;
import org.gzigzag.impl.*;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.awt.*;
    
    /** A vob that is the background paper.
     * This vob stores the inside coordinates so clicks on it can be 
     * used to pan pretty easily.
     */
public class BgVob extends org.gzigzag.vob.Vob {
public static final String rcsid = "$Id: BgVob.java,v 1.4 2002/03/02 17:43:33 vegai Exp $";

    int x0, y0, x1, y1, zoom;
    public Rectangle clip;
    Color bgcolor;
    
    
    public BgVob(Cell cell, int x0, int y0, int x1, int y1, int zoom) {
	super(cell);
	this.x0 = x0;
	this.y0 = y0;
	this.x1 = x1;
	this.y1 = y1;
	this.zoom = zoom;
	
	Cell colorCell = cell.s(Dims.d_user_3_id);
	if(colorCell != null && colorCell.t().length() > 0) {
	    int i = 0xffffff;
	    try {
		i = Integer.parseInt(colorCell.t(),16);
	    } catch(NumberFormatException e) {
		// e.printStackTrace();
		i = 0xffffff;
		//		pa("NumberFormatException: \""+colorCell.t()+"\"");
	    }
	    bgcolor = new Color(i);
	}
    }
    public void render(Graphics g, 
		       int x, int y, int w, int h,
		       boolean boxDrawn,
		       RenderInfo info
		       ) {
	Color old = g.getColor();
	// g.drawRect(x, y, w, h);
	g.setColor(bgcolor != null ? bgcolor : Color.white);
	g.fillRect(x, y, w, h);
	// p("Draw filled rect!");
	g.setColor(old);
    }
    
    public Point getPan(Vob.Coords coords, int x, int y) {
	return new Point(
			 (x - coords.x) * (x1 - x0) / coords.width + x0,
			 (y - coords.y) * (y1 - y0) / coords.height + y0
			 );
    }
}
