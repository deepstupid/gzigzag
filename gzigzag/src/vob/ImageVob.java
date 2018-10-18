/*   
ImageVob.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag.vob;
import java.awt.*;

public class ImageVob extends HBox.VobHBox {
    Image im;
    public ImageVob(Image im) {
	super(im);
	this.im = im;
    }

    public int getWidth(int scale) {
	return im.getWidth(null) * scale / 1000;
    }

    public int getHeight(int scale) {
	return im.getHeight(null) * scale / 1000;
    }

    public int getDepth(int scale) { return 0; }


    public void render(Graphics g, int x, int y, int w, int h,
		    boolean boxDrawn, RenderInfo info) {
	// XXX Scaling?
	if(im != null)
	    g.drawImage(im, x, y, w, h, null); // XXX updatemanager call?
    }
}
