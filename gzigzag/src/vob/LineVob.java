/*   
LineVob.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
package org.gzigzag.vob;
import java.awt.*;

/** A plain line drawn in given color.
 */

public class LineVob extends Vob {
public static final String rcsid = "$Id: LineVob.java,v 1.4 2001/10/18 03:16:09 tjl Exp $";

    int x0, y0, x1, y1;
    Color color;

    public LineVob(int x0, int y0, int x1, int y1) {
	this(x0, y0, x1, y1, null);
    }

    public LineVob(int x0, int y0, int x1, int y1, Color color) {
	super(null);
	this.x0 = x0;
	this.y0 = y0;
	this.x1 = x1;
	this.y1 = y1;
	this.color = color;

    }

    public void render(Graphics g, 
				int x, int y, int w, int h,
				boolean boxDrawn,
				RenderInfo info
				) {
	if(info.isFast()) return;
	if(color != null) g.setColor(color);
	g.drawLine(x0, y0, x1, y1);
    }

}


