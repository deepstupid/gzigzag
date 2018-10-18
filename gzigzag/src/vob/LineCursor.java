/*   
CharArrayVob.java
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

/** A vob used in linebreakable chains that shows a line cursor.
 *  XXX rename to ...Vob
 */

public class LineCursor extends Vob implements HBox {
String rcsid = "$Id: LineCursor.java,v 1.6 2001/08/12 22:32:23 tuukkah Exp $";
    static public boolean dbg = false;
    static private void p(String s) { if(dbg) System.out.println(s); }

    Color col;
    TextStyle style;

    public LineCursor(Color col, TextStyle style) {
	super(null);
	this.col = col;
	this.style = style;
	p("create linevob");
    }

    
    // IMPLEMENTATION OF Vob

    public void render(java.awt.Graphics g, 
		       int x, int y, int w, int h,
		       boolean boxDrawn,
	               RenderInfo info) {
	p("render linevob");
	if(x>0)
	    x = x-1;
	if(x==w)
	    x = x-1;
	//	g.setXORMode(info.getBgColor());

	g.drawLine(x, y-1, x, y+h-1);       // Left
	g.drawLine(x+1, y-1, x+1, y+h-1);   // Right

	//	g.drawLine(x-1, y-1, x+2, y-1);     // Top
	//	g.drawLine(x-1, y+h-1, x+2, y+h-1); // Bottom

	//	g.setPaintMode();
    }

    // HBox implementation
    public int getWidth(int scale) {
	return 0;
    }
    public int getHeight(int scale) { return style.getAscent(scale); }
    public int getDepth(int scale) { return style.getDescent(scale); }

    public Vob getVob(int scale) {
	//this.scale = scale;
	p("linevob getVob()");
        return this;
    }
	
    public void setPrev(HBox b) { }
    public void setPosition(int depth, int x, int y, int w, int h) { }

}

