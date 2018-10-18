/*   
PageImageSpan.java
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
import java.awt.*;
import java.awt.image.*;
import org.gzigzag.util.*;
import org.gzigzag.mediaserver.*;
import java.io.*;
import java.util.*;


/** An scrollblock containing paged media (PS/PDF).
 */

public class PageImageScroll extends ScrollBlock {    
String rcsid = "$Id: PageImageScroll.java,v 1.11 2002/03/24 19:50:54 tjl Exp $";
    public static boolean dbg = true;
    final static void p(String s) { if(dbg) System.out.println(s); }
    final static void pa(String s) { System.out.println(s); }

    int WIDTH=612; // Letter size fixed, for now, in points, 1/72ths of inches..
    int HEIGHT=792;
    int RESOLUTION=72;

    String DIR="mstmpimg/"; // XXX Non-platform-independent!

    int pages;
    Mediaserver ms;

    Image[] pageimages;

    private Image getPageImage(int page) {
	if(pageimages == null)
	    pageimages = new Image[pages];
	if(pageimages[page] == null)
	    try {
		pageimages[page] = 
		    GlobalToolkit.toolkit.getImage(kludgeFilename(page)); 
	    } catch (Exception _) {
		pa("Exception when loading image: "+_);
	    }
	return pageimages[page];
    }

    //    Mediaserver.Block block;

    public PageImageScroll(Mediaserver ms, String id) {
	super(id);
	this.ms = ms;

	checkLen();
    }

    /** 0-based page.
     */
    protected String kludgeFilename(int page) {
	return new String(DIR+getID()+"-"+RESOLUTION+"-"+(page+1));
    }

    public void checkLen() {
	int i=0;
	while(new File(kludgeFilename(i)).exists())
	    i++;
	pages = i;
	pa("Checked document of "+i+" pages");
    }

    class SimplePageSpan extends ScrollBlockManager.PageSpanBase implements Runnable {
	SimplePageSpan(int p0, int p1, int x, int y, int w, int h) {
	    super(PageImageScroll.this, p0, p1, x, y, w, h);
	}

	protected ScrollBlockManager.PageSpanBase
		createNew(int p0, int p1, int x, int y, int w, int h) {
	    return new SimplePageSpan(p0, p1, x, y, w, h);
	}

	Image cached;

	Image loadImage() {
	    if(pages == 0) return null; // Don't even try if no pages avail.
	    Image img = getPageImage(offs0);
	    
	    if(img==null)
		throw new Error("Couldn't load image from file "+kludgeFilename(offs0)); 
	    int count = 0;
	    while(img.getWidth(null) < 0 || img.getHeight(null) < 0) {
		try {
		    count++;
		    if(count > 100) 
			throw new Error("Timeout while loading file "+kludgeFilename(offs0)); 
		    Thread.sleep(200);
		} catch(InterruptedException e) {
		    throw new Error("Interrupted");
		}
	    }
	    pa("Returning image");
	    return img;
	}

	public Image getImage() {
	    if(cached != null) return cached;
	    if(length() != 1) 
		//		return null;
		throw new ZZError("Must have only one page to show...");
	    run();
	    return cached;
	    // XXX
	    // Background.addTask(SimplePageSpan.this);
	    // p("GetImage returning null!!!!!");
	    // return null;
	}

	public void run() {
	    p("Running page: "+cached);
	    if(cached != null) return;
	    cached = loadImage();
	    p("Loaded image: "+cached);
	    
	    if(x==0 && y==0 && w==WIDTH && h==HEIGHT)
	 	return;

	    ImageFilter filter = new CropImageFilter(x,y,w,h);
	    cached = GlobalToolkit.toolkit.createImage
		    (new FilteredImageSource(cached.getSource(), filter));
	    p("Cropped image: "+cached);
	}
    }

    public Span getCurrent() {
	return new SimplePageSpan(0, pages, 0, 0, WIDTH, HEIGHT);
    }

    public Span getSpan(int p0, int p1, int x, int y, int w, int h) {
	return new SimplePageSpan(p0, p1, x, y, w, h);
    }


    public boolean isFinalized() { return true; }
}


