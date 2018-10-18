/*   
TextVob.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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

/** An abstract class for 2D vobs drawing text.
 *  The main convenience you get from using this superclass is not having to
 *  implement the methods indicating where this vob can be broken.
 */

public class TextVob extends Vob implements HBox {
String rcsid = "$Id: TextVob.java,v 1.12 2001/08/12 10:26:49 bfallenstein Exp $";

    public final TextStyle style;
    public final String text;

    public TextVob(Object key, TextStyle style, String text) {
	super(key);
	this.style = style;
	this.text = text;
    }

    public String getText() { return text; }

    
    // IMPLEMENTATION OF Vob

    /** The scale this Vob's TextStyle should be used.
     * XXX Performance?
     */
    protected int scale;
    public int getScale() { return scale; }

    public void render(java.awt.Graphics g, 
		       int x, int y, int w, int h,
		       boolean boxDrawn,
	               RenderInfo info) {
	style.render(g, text, scale, x, y, w, h, boxDrawn, info);
    }

    /** Scale this Vob to fit the given width and height.
     */
    protected void scaleToFit(int w, int h) {
	scale = style.getScale(text, w, h);
    }


    // HBox implementation
    public int getWidth(int scale) { return style.getWidth(text, scale); }
    public int getHeight(int scale) { return style.getAscent(scale); }
    public int getDepth(int scale) { return style.getDescent(scale); }

    public Vob getVob(int scale) {
	this.scale = scale;
	return this;
    }
	
    public void setPrev(HBox b) { }
    public void setPosition(int depth, int x, int y, int w, int h) { }

}

