/*   
BuoyAnchor.java
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
package org.gzigzag.vob;
import java.awt.*;

/** An endpoint that a buoy con be anchored to.
 */

public class BuoyAnchor extends Vob implements HBox {
String rcsid = "$Id: BuoyAnchor.java,v 1.7 2001/10/16 22:01:21 tjl Exp $";
    static public boolean dbg = false;
    static private void p(String s) { if(dbg) System.out.println(s); }

    static Color fg = new Color(0xe06000);
    TextStyle style;
    boolean marked;
    BuoyPlacer placer;
    BuoyPlacer.Buoy buoy;

    public BuoyAnchor(Object key, TextStyle style, boolean marked, 
		      BuoyPlacer placer, BuoyPlacer.Buoy buoy) {
	super(key);
	this.style = style;
	this.marked = marked;
	this.placer = placer;
	this.buoy = buoy;
    }

    
    // IMPLEMENTATION OF Vob

    public void render(java.awt.Graphics g, 
		       int x, int y, int w, int h,
		       boolean boxDrawn,
	               RenderInfo info) {
	Color old = g.getColor();
	g.setColor(fg);
	if(marked) {
	    g.fillRect(x, y, 7, h-1);
	    g.setColor(info.getBgColor());
	}
	int x2 = x + 3, y2 = y + h/2;
	g.drawOval(x2-2, y2-2, 4, 4);
	g.setColor(old);
    }

    // HBox implementation
    public int getWidth(int scale) {
	return 7;
    }
    public int getHeight(int scale) { return style.getAscent(scale); }
    public int getDepth(int scale) { return style.getDescent(scale); }

    public Vob getVob(int scale) {
	//this.scale = scale;
	p("linevob getVob()");
        return this;
    }
	
    public void setPrev(HBox b) { }

    /*
     * XXX Remove this! Wrong abstraction! This information MUST be
     *     inquired from the VobScene, not through the Vob!!! --Tjl
     */
    public void setPosition(int depth, int x, int y, int w, int h) { 
	if(placer != null)
	    placer.add(buoy, x, y, depth);
    }

}
