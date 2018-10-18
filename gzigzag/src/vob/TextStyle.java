/*   
TextStyle.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
import org.gzigzag.*;
import java.awt.*;

/** A style to draw text in.
 */

public abstract class TextStyle {
public static final String rcsid = "$Id: TextStyle.java,v 1.9 2001/08/04 09:12:57 bfallenstein Exp $";

    public ScalableFont font;
    public Color col;

    public TextStyle(ScalableFont font, Color col) {
	this.font = font;
	this.col = col;
    }

    public int getScale(String s, int w, int h) {
	return font.getScale(h);
    }
    public int getScale(char[] chars, int offs, int len, int w, int h) {
	return font.getScale(h);
    }

    public int getWidth(String s, int scale) {
	return font.getFontMetrics(scale).stringWidth(s);
    }
    public int getWidth(char[] chars, int offs, int len, int scale) {
	return font.getFontMetrics(scale).charsWidth(chars, offs, len);
    }

    public int getHeight(String s, int scale) {
	return font.getFontMetrics(scale).getHeight();
    }
    public int getHeight(char[] chars, int offs, int len, int scale) {
	return font.getFontMetrics(scale).getHeight();
    }


    public int getAscent(int scale) {
	return font.getFontMetrics(scale).getAscent();
    }

    public int getDescent(int scale) {
	return font.getFontMetrics(scale).getDescent();
    }

    public int getX(String s, int scale, int offs) {
	return getWidth(s.substring(0, offs), scale);
    }
    public int getX(char[] chars, int offs, int len, int scale, int xoffs) {
	return getWidth(chars, offs, xoffs, scale);
    }

    /** Render the given string, in the given scale, at the given coordinates. 
     * @param g The graphics context to draw into
     * 		The color should be set to the default foreground color,
     *		already mixed.
     * @param s The string to draw.
     * @param scale The scale to draw at.
     * @param x,y,w,h The coordinates and size of the "interesting area". The
     *                width and height are given additionally to the scale
     *                so that the style can clip the area if it wants to
     *                (XXX good? or should the VobScene be responsible?).
     * @param boxDrawn Whether a box background has been drawn.
     *                  If false, the background is transparent and
     *                  this text may drawn differently,
     *                  e.g. by drawing a border or drop-shadow around text
     *                  to clarify the visual appearance.
     * @param info General parameters about rendering.
     */
    public void render(java.awt.Graphics g,
		       String s, int scale,
		       int x, int y, int w, int h,
		       boolean boxDrawn, Vob.RenderInfo info) {
	render(g, s.toCharArray(), 0, s.length(), scale, x, y, w, h,
	       boxDrawn, info);
    }

    abstract public void render(java.awt.Graphics g,
				char[] chars, int offs, int len,
				int scale, int x, int y, int w, int h,
				boolean boxDrawn,
				Vob.RenderInfo info);

}


