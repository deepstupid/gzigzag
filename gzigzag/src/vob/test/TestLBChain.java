/*   
TestLBChain.java
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
package org.gzigzag.vob;
import junit.framework.*;
import java.util.*;
import java.awt.*;

/** A unit test for LineBreakableChain.
 */

public class TestLBChain extends TestCase {
String rcsid = "$Id: TestLBChain.java,v 1.15 2001/10/25 12:31:01 bfallenstein Exp $";

    public TestLBChain(String s) { super(s); }

    static class TestBox extends Vob implements HBox {
	TestBox(Object key, int w) { super(key); this.w = w; }
	int w;
	public int getHeight(int scale) { return 5; }
	public int getWidth(int scale) { return w; }
	public int getDepth(int scale) { return 9; }
	public Vob getVob(int scale) { return this; }
	public void setPrev(HBox box) { }
        public void setPosition(int depth, int x, int y, int w, int h) { }

	public void render(Graphics g, 
				int x, int y, int w, int h,
				boolean boxDrawn,
				RenderInfo info
				) {
	}

    }

    TestBox[] boxes = new TestBox[20];

    LinebreakableChain lb1 = new LinebreakableChain();
    LinebreakableChain lb2 = new LinebreakableChain();
    LinebreakableChain lb3 = new LinebreakableChain();
    SimpleLinebreaker lbr = new SimpleLinebreaker();

    VobScene sc = new SimpleVobScene();

    Vob.Coords c = new Vob.Coords();

    void assertCoords(int box, int x, int y, int w) {
	sc.getCoords(boxes[box], c);
	assertEquals("bx "+box, x, c.x);
	assertEquals("by "+box, y, c.y);
	assertEquals("bw "+box, w, c.width);
    }

    public void setUp() {
	for(int i=0; i<boxes.length; i++) {
	    boxes[i] = new TestBox(null, i);
	}
	lb1.addBox(boxes[1]);
	lb1.addBox(boxes[2]);
	lb1.addBox(boxes[3]);

	lb2.addBox(boxes[4]);
	lb2.addGlue(2,2,2);
	lb2.addBox(boxes[5]);
	lb2.addGlue(2,4,8);
	lb2.addBox(boxes[6]);
	lb2.addGlue(2,6,1);
	lb2.addBox(boxes[7]);
	lb2.addGlue(2,2,2);
	lb2.addBox(boxes[8]);
    }

    public void testBasicGlueNoStretch() {
	assertEquals(0,     LinebreakingUtil.stretchFactor(lb1, 0, 1, 1000, 1), 0.01);
	assertEquals( 1000, LinebreakingUtil.stretchFactor(lb1, 0, 1, 1000, 2), 0.01);
	assertEquals(-1000, LinebreakingUtil.stretchFactor(lb1, 0, 1, 1000, 0), 0.01);

	assertEquals(0,     LinebreakingUtil.stretchFactor(lb1, 0, 2, 1000, 3), 0.01);
	assertEquals(-1000, LinebreakingUtil.stretchFactor(lb1, 1, 3, 1000, 4), 0.01);
    }

    public void testBasicGlueStretch() {
	assertEquals(0,     LinebreakingUtil.stretchFactor(lb2, 1, 2, 1000, 5), 0.01);
	assertEquals(-1000, LinebreakingUtil.stretchFactor(lb2, 1, 2, 1000, 4), 0.01);
	assertEquals( 1000, LinebreakingUtil.stretchFactor(lb2, 1, 2, 1000, 6), 0.01);

	assertEquals(0,     LinebreakingUtil.stretchFactor(lb2, 1, 3, 1000, 13), 0.01);
	assertEquals(-0.125,  LinebreakingUtil.stretchFactor(lb2, 1, 3, 1000, 12), 0.01);
	assertEquals( 0.25, LinebreakingUtil.stretchFactor(lb2, 1, 3, 1000, 14), 0.01);

	assertEquals( 0, LinebreakingUtil.stretchFactor(lb2, 1, 4, 1000, 22), 0.01);
	assertEquals( -1.0/9, LinebreakingUtil.stretchFactor(lb2, 1, 4, 1000, 21), 0.01);
	assertEquals( 0.1, LinebreakingUtil.stretchFactor(lb2, 1, 4, 1000, 23), 0.01);
    }

    public void testBadness() {
	assertEquals(0,     LinebreakingUtil.badness(lb2, 1, 2, 1000, 5), 0.01);
	assertEquals( 1000000, LinebreakingUtil.badness(lb2, 1, 2, 1000, 4), 0.01);
	assertEquals( 1000000, LinebreakingUtil.badness(lb2, 1, 2, 1000, 6), 0.01);

	assertEquals(0,     LinebreakingUtil.badness(lb2, 1, 3, 1000, 13), 0.01);
	assertEquals(125,  LinebreakingUtil.badness(lb2, 1, 3, 1000, 12), 0.01);
	assertEquals(250, LinebreakingUtil.badness(lb2, 1, 3, 1000, 14), 0.01);
    }

    public void testPut1() { // 5+2+6+2+7 == 22 -> correct width
	LinebreakingUtil.putLine(lb2, sc, 100, 100, 22, 10, 1, 4, 1000);
	// 95 = y - height
	assertCoords(5, 100, 95, 7);
	assertCoords(6, 107, 95, 8);
	assertCoords(7, 115, 95, 9);
    }

    public void testPut2() {
	// One pixel of extra width
	LinebreakingUtil.putLine(lb2, sc, 100, 100, 23, 10, 1, 4, 1000);
	assertCoords(5, 100, 95, 7);
	assertCoords(6, 107, 95, 9);
	assertCoords(7, 116, 95, 9);
    }

    public void testPut3() {
	// ten pixel of extra width
	LinebreakingUtil.putLine(lb2, sc, 100, 100, 32, 10, 1, 4, 1000);
	assertCoords(5, 100, 95, 11);
	assertCoords(6, 111, 95, 14);
	assertCoords(7, 125, 95, 9);
    }

    public void testPut4() {
	// Exact widh
	LinebreakingUtil.putLine(lb2, sc, 100, 100, 11, 10, 0, 2, 1000);
	assertCoords(4, 100, 95, 6);
	assertCoords(5, 106, 95, 7);
    }

    public void testPut5() {
	// Width + 50pix
	LinebreakingUtil.putLine(lb2, sc, 100, 100, 61, 10, 0, 2, 1000);
	assertCoords(4, 100, 95, 56);
	assertCoords(5, 156, 95, 7);
    }

    public void testChompTriv() {
	assertEquals(1, lbr.chompTriv(lb2, 0, 1, 1000));
	assertEquals(1, lbr.chompTriv(lb2, 0, 4, 1000));
	assertEquals(2, lbr.chompTriv(lb2, 0, 12, 1000));
	assertEquals(3, lbr.chompTriv(lb2, 0, 13, 1000));
	assertEquals(3, lbr.chompTriv(lb2, 0, 19, 1000));
    }

    public void testBreakTriv1() {
	Linebreaker.Broken bro = 
	    lbr.breakLines(lb2, new int[] {13, 13, 13, 13, 13}, 
		  new int[] {1000, 1000, 1000, 1000, 1000},
		  0, 0);
	/* XXX Make more tests, enable these.
	assertEquals(0, bro.tokenStarts[0]);
	assertEquals(3, bro.tokenStarts[1]);
	assertEquals(4, bro.tokenStarts[2]);
	assertEquals(3, bro.endLine);
	*/
    }

    public void testPutLines() {
	int[] lines = new int[] {13, 13, 13, 13, 13}, 
	      scales = new int[] {1000, 1000, 1000, 1000, 1000};
	Linebreaker.Broken bro = 
		    lbr.breakLines(lb2, lines, scales, 0, 0);
	bro.putLines(sc, 100, 100, 10);
	// assertCoords(4, 100, 100, 7); XXX
	// assertCoords(7, 100, 114, 13);
    }

    public void testEmptyGlue() {
	LinebreakableChain lb = new LinebreakableChain();
	lb.addGlue(10, 10, 10);
    }


}
