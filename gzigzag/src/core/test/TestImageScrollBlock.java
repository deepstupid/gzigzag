/*   
TestImageScrollBlock.java
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

package org.gzigzag;
import junit.framework.*;
import java.awt.*;
import java.awt.image.*;

/** Tests for ImageScrollBlocks (and thereby ImageSpans)
 */

public abstract class TestImageScrollBlock extends TestCase {
public static final String rcsid = "$Id: TestImageScrollBlock.java,v 1.3 2001/07/19 12:07:57 tjl Exp $";

    public TestImageScrollBlock(String name) { super(name); }

    final int w = 200;
    final int h = 200;

    int pix[] = new int[w * h];

    Image img;

    ScrollBlock scr;

    public abstract ScrollBlock getScrollBlock(Image img, int w, int h);

    ImageSpan whole;

    void waitForImage(Image img) {
	// Wait until width and height known
	long time = System.currentTimeMillis();
	while(img.getWidth(null) == -1 || img.getHeight(null) == -1) {
	    if(System.currentTimeMillis() - time > 5000) 
		fail("Image width and height wouldn't load!");
	    try {
		Thread.sleep(500);
	    } catch(Exception e) { }
	}
    }

    public void setUp() {
	int index = 0;
	for (int y = 0; y < h; y++) {
	    for (int x = 0; x < w; x++) {
		pix[index++] = (255 << 24) | (x << 16) | y;
	    }
	}
	img = org.gzigzag.util.GlobalToolkit.toolkit.
		    createImage(new MemoryImageSource(w, h, pix, 0, w));
	scr = getScrollBlock(img, w, h);
	whole = (ImageSpan)scr.getCurrent();
    }

    public void assertPix(ImageSpan span, int w, int h, int[] pix) {
	Dimension dim = span.getSize();
	assertEquals("Span width", w, dim.width);
	assertEquals("Span height", h, dim.height);

	Image img = span.getImage();

	// Images load lazily - we should give it a moment.
	waitForImage(img);

	assertEquals("Image width", w, img.getWidth(null));
	assertEquals("Image heiht", h, img.getHeight(null));
	int[] pixels = new int[w*h];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
	    throw new Error("No pixels");
        }
	for(int i=0; i<pix.length; i+=4) {
	    assertEquals("Pixel "+pix[i]+" "+pix[i+1],
		    (255<<24) | (pix[i+2]<<16) | (pix[i+3]), 
			pixels[pix[i] + w * pix[i+1]]);
	}
    }

    public void testWhole() {
	assertPix(whole, w, h,
	    new int[] {
		0, 0,  0, 0,
		1, 0,  1, 0,
		0, 10, 0, 10,
		100, 0, 100, 0,
		199, 199, 199, 199
	    });
    }

    public void testSub() {
	ImageSpan sub = (ImageSpan)whole.subArea(15, 20, 50, 100);
	assertPix(sub, 50, 100,
	    new int[] {
		0, 0,  15, 20,
		1, 0,  16, 20,
		0, 10, 15, 30,
		49, 99, 64, 119
	    });
    }

    public void testSubSub() {
	ImageSpan part = (ImageSpan)whole.subArea(7, 9, 100, 150);
	ImageSpan sub = (ImageSpan)part.subArea(8, 11, 50, 100);
	assertPix(sub, 50, 100,
	    new int[] {
		0, 0,  15, 20,
		1, 0,  16, 20,
		0, 10, 15, 30,
		49, 99, 64, 119
	    });
    }

    public void testNegX() {
	try {
	    ImageSpan sub = (ImageSpan)whole.subArea(-1, 20, 50, 100);
	} catch(ZZError e) {
	    return;
	}
	fail("Expected error to be thrown!");
    }

    public void testNegY() {
	try {
	    ImageSpan sub = (ImageSpan)whole.subArea(15, -1, 50, 100);
	} catch(ZZError e) {
	    return;
	}
	fail("Expected error to be thrown!");
    }

    public void testTooWide() {
	try {
	    ImageSpan sub = (ImageSpan)whole.subArea(15, 20, w-15+1, 100);
	} catch(ZZError e) {
	    return;
	}
	fail("Expected error to be thrown!");
    }

    public void testTooHigh() {
	try {
	    ImageSpan sub = (ImageSpan)whole.subArea(15, 20, 50, h-20+1);
	} catch(ZZError e) {
	    return;
	}
	fail("Expected error to be thrown!");
    }



}
