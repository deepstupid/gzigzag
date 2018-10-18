/*   
ImageVob.java
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
package org.gzigzag.vob.demo;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.net.URL;

/** A vob showing an image, either given directly or read from a file/URL.
 */

public class ImageVob extends Vob implements java.awt.image.ImageObserver {
    String rcsid = "$Id: ImageVob.java,v 1.3 2001/12/14 20:58:15 tuukkah Exp $";

    Image loader;
    Image image; File f;
    public static Component comp;

    //    public ImageVob(Object key, Image loader) {
    //	super(key);
    //this.loader = loader;
    //imageUpdate();
    //}

    static HashMap loaded = new HashMap();

    public ImageVob(Object key, File f) {
	super(key);
	this.f = f;
	Object l = loaded.get(f);
	if(l == null) {
	    loader = Toolkit.getDefaultToolkit().getImage(f.getAbsolutePath());
	    imageUpdate();
	} else {
	    image = (Image)l;
	}
    }

    //    public ImageVob(Object key, URL url) {
    //	super(key);
    //	loader = Toolkit.getDefaultToolkit().getImage(url);
    //	imageUpdate();
    // }

    public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
	if(flags != ALLBITS) return true;

        if(comp == null) throw new Error("Before using ImageVob, must set ImageVob.comp to some showing component!");
        image = comp.createImage(loader.getWidth(this), loader.getHeight(this));
        if(image == null) throw new Error("ARGH!");
        Graphics g = image.getGraphics();
        g.drawImage(loader, 0, 0, this);
	g.dispose();
	loaded.put(f, image);
	comp.repaint();
	return false;
    }
    public void imageUpdate() {
	// Graphics g = comp.getGraphics();
	loader.getWidth(this);
    }

    
    public void render(java.awt.Graphics g, 
		       int x, int y, int w, int h,
		       boolean boxDrawn,
		       RenderInfo info) {
	if(image != null)
	    g.drawImage(image, x, y, w, h, null);
	else
	    System.out.println("Cannot draw image: not completely loaded yet");
    }
}

