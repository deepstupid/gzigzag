/*   
SimpleImageScroll.java
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
 * Written by Tuomas Lukka
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.Mediaserver;
import java.io.IOException;
import java.awt.*;
import java.awt.image.*;
import org.gzigzag.util.*;

/** An image scrollblock
 */

public class SimpleImageScroll extends ScrollBlock {
String rcsid = "$Id: SimpleImageScroll.java,v 1.5 2002/01/24 18:46:31 tjl Exp $";

    Image im;
    int width, height;

    Mediaserver ms;

    public SimpleImageScroll(String id, Image im, int width, int height) {
	super(id);
	this.im = im; 
	this.width = width;
	this.height = height;
    }

    public SimpleImageScroll(Image im, int width, int height) {
	this( ScrollBlockManager.getTmpID(), im, width, height );
    }

    public SimpleImageScroll(Mediaserver ms,  String id) {
	super(id);
	this.ms = ms;
    }

    private void loadImage() {
	Background.addTask(new Runnable() { 
	    public void run() {
		Mediaserver.Block block ;
		String ct;
		try {
		    block = ms.getDatum(new Mediaserver.Id(getID()));
		    ct = block.getContentType();
		} catch(IOException e) {
		    throw new Error("Couldn't load image block");
		}
		
		if(!ct.substring(0,ct.indexOf('/')).equals("image"))
		    throw new Error("Block isn't an image");

		Image img = null;
		try {
		    img = GlobalToolkit.toolkit.createImage(block.getBytes());
		} catch (IOException _) {}
		if(img == null) 
		    throw new Error("Unknown image type");
		int count = 0;
		while(img.getWidth(null) < 0 || img.getHeight(null) < 0) {
		    try {
			count++;
			if(count > 100) 
			    throw new Error("Timeout");
			Thread.sleep(200);
		    } catch(InterruptedException e) {
			throw new Error("Interrupted");
		    }
		}
		SimpleImageScroll.this.im = img;
		SimpleImageScroll.this.width = img.getWidth(null);
		SimpleImageScroll.this.height = img.getHeight(null);
	    }
	});
    }

    class SimpleImageSpan extends ScrollBlockManager.ImageSpanBase implements Runnable{
	SimpleImageSpan(int x, int y, int w, int h) {
	    super(SimpleImageScroll.this, x, y, w, h);
	}

	protected ScrollBlockManager.ImageSpanBase
		createNew(int x, int y, int w, int h) {
	    return new SimpleImageSpan(x, y, w, h);
	}

	Image cached;

	public Image getImage() {
	    if(cached != null) return cached;
	    if(im == null) {
		loadImage();
		return null;
	    }
	    if(x==0 && y==0 && w==width && h==height)
		return im;

	    Background.addTask(this);
	    return null;
	}

	public void run() {
	    if(cached != null) return;

	    ImageFilter filter = new CropImageFilter(x,y,w,h);
	    cached = GlobalToolkit.toolkit.createImage
		    (new FilteredImageSource(im.getSource(), filter));
	}
    }

    public Span getCurrent() {
	if(im == null) loadImage();
	return new SimpleImageSpan(0, 0, width, height);
    }

    public Span getSpan(int x, int y, int w, int h) {
	return new SimpleImageSpan(x, y, w, h);
    }


    public boolean isFinalized() { return true; }
}


