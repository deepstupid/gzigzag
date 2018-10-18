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

/** An abstract class for 2D vobs drawing text stored in a char array.
 *  The main convenience you get from using this superclass is not having to
 *  implement the methods indicating where this vob can be broken.
 *  <p>
 *  This differs from TextVob in that its contents are a range of characters
 *  from a char array-- thus, if the contents come from some cache and
 *  the char array can be re-used each time a new vob scene is generated.
 *  <p>
 *  The vob keys are a horrible but workable hack.
 *  The vob key is the CharArrayVob itself, which
 *  calculates its hashcode and equality as a combination of the streamkey,
 *  the char array and the offset (not length).
 */

public class CharArrayVob extends HBox.VobHBox {
String rcsid = "$Id: CharArrayVob.java,v 1.6 2001/09/27 14:53:43 tjl Exp $";

    public final TextStyle style;
    public final char[] chars;
    /** The offset and length of the range of chars inside the char array. */
    public final int offs, len;
    public final Object streamKey;

    public CharArrayVob(Object streamKey, TextStyle style,
			char[] chars, int offs, int len) {
	super(null);
	key = this;
	this.style = style;
	this.chars = chars;
	this.offs = offs;
	this.len = len;
	this.streamKey = streamKey;
    }

    
    // IMPLEMENTATION OF Vob


    public void render(java.awt.Graphics g, 
		       int x, int y, int w, int h,
		       boolean boxDrawn,
	               RenderInfo info) {
	style.render(g, chars, offs, len, 
		     scale, x, y, w, h, boxDrawn, info);
    }

    /** Scale this Vob to fit the given width and height.
     */
    protected void scaleToFit(int w, int h) {
	scale = style.getScale(chars, offs, len, w, h);
    }

    /** We cache the width because it's used a lot. */
    protected int widthCache = -1;
    /** We need to remember for which scale we cached it. */
    protected int widthCacheScale = -1;


    // HBox implementation

    public int getWidth(int scale) {
	if(scale != widthCacheScale) {
	    widthCache = style.getWidth(chars, offs, len, scale);
	    widthCacheScale = scale;
	}
	return widthCache;
    }
    public int getHeight(int scale) { return style.getAscent(scale); }
    public int getDepth(int scale) { return style.getDescent(scale); }


    public int hashCode() {
	return (streamKey == null ? 0 : streamKey.hashCode()) ^ 
		(chars == null ? 0 : chars.hashCode()) ^ 
		offs /* ^ (1024*1024*len) */;
    }

    public boolean equals(Object o) {
	if(!(o instanceof CharArrayVob)) return false;
	CharArrayVob other = (CharArrayVob)o;
	if(streamKey == null || other.streamKey == null) return false;
	return  streamKey.equals(other.streamKey) &&
		chars == other.chars &&
		offs == other.offs &&
		// len == other.len &&   // Maybe better off without testing
		true ;
    }

}

