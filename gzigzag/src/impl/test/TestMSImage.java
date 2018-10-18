/*   
TestMSImage.java
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
 * Written by Tuomas Lukka and Antti-Juhani Kaijanaho
 */


package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import org.gzigzag.util.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import java.io.*;
import junit.framework.*;
import java.awt.*;
import java.awt.image.*;

/** Test the storage of images in mediaserver.
 * Curiously, Kaffe 1.0.6 has some problems with these...
 */

public class TestMSImage extends TestCase {
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }

    public TestMSImage(String name) { super(name);} 

    /** Mediaserver ID of the test PNG image.
     */
    static public Mediaserver.Id pid = 
		new Mediaserver.Id(
"0000000008000000E8181097FF000440F32F9BC54E267998CC98AE08122B98F1918A8ADB06EF37"
		);

    /** Mediaserver ID of the test JPEG image.
     */
    static public Mediaserver.Id jid = 
		new Mediaserver.Id(
"0000000008000000E80F37682A0004B0024E68FD1DB78FF8E1DAD90953DA5584EA33F93CB6FB82"
		);
   
    /** Get an image span for the whole block of the given mediaserver block.
     */
    static public ImageSpan getIS(Mediaserver.Id id) throws Exception {
	ScrollBlock block = 
	    ScrollBlockManager.getScrollBlock(TestImpl.zms, id);
	assertNotNull(block);
	return (ImageSpan)block.getCurrent();
    }

    int getSinglePixel(Image img, int x, int y) throws Exception {
	int[] p = new int[1];
	PixelGrabber g = new PixelGrabber(img, x, y, 1, 1, p, 0, 1);
	g.grabPixels();
	if((g.getStatus() & ImageObserver.ABORT) != 0)
	    throw new Error("Problem!");
	return p[0];
    }

    private void helpTestBlouse(Mediaserver.Id id) throws Exception {
	ImageSpan whole = getIS(id);
	int pixel = getSinglePixel(whole.getImage(), 404, 336);
	pixel &= 0xffffff;
        System.out.println(Integer.toHexString(pixel));
	assertEquals(0x5f2b51, pixel);
    }

    public void testBlouseJPG() throws Exception {
        helpTestBlouse(jid);
    }

    public void testBlousePNG() throws Exception {
        helpTestBlouse(pid);
    }

    public void testMSBlockJPG() throws Exception {
	Mediaserver.Block block = TestImpl.zms.getDatum(jid);
	byte[] data = block.getBytes();
        assertEquals((byte)0xff, data[0]);
        assertEquals((byte)0xd8, data[1]);
    }

    public void testMSBlockPNG() throws Exception {
	Mediaserver.Block block = TestImpl.zms.getDatum(pid);
	byte[] data = block.getBytes();
	assertEquals((byte)0x89, data[0]);
	assertEquals((byte)0x50, data[1]);
    }

    public void testCreateImageJPG() throws Exception {
	Mediaserver.Block block = TestImpl.zms.getDatum(jid);
	byte[] data = block.getBytes();
	Image img = GlobalToolkit.toolkit.createImage(data);
	p("IMG: "+img);
    }

    public void testCreateImagePNG() throws Exception {
	Mediaserver.Block block = TestImpl.zms.getDatum(pid);
	byte[] data = block.getBytes();
	Image img = GlobalToolkit.toolkit.createImage(data);
	p("IMG: "+img);
    }


}
