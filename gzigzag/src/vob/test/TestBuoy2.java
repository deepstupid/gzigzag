/*   
TestBuoy2.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
import java.awt.*;

/** A unit test for Buoy2.
 */

public class TestBuoy2 extends TestCase {
String rcsid = "$Id: TestBuoy2.java,v 1.3 2001/10/17 21:05:38 tjl Exp $";

    public TestBuoy2(String s) { super(s); }

    static class XBuoy extends AbstractBuoy {
	public XBuoy(Vob anchor, int pw, int ph) {
	    super(anchor, new Dimension(pw, ph));
	}
	int x, y, w, h, d;
	boolean set;
	public void put(VobScene into, int depth, 
		    int x, int y, int w, int h) {
	    this.x = x;
	    this.y = y;
	    this.w = w;
	    this.h = h;
	    this.d= depth;
	    this.set = true;
	}
    }

    static class XVob extends Vob {
	XVob() { super(null); }
	public void render(Graphics g, 
				    int x, int y, int w, int h,
				    boolean boxDrawn,
				    RenderInfo info
				    ) { }
    }

    XVob[] xvobs = new XVob[10];
    { for(int i=0; i<xvobs.length; i++) xvobs[i] = new XVob(); }

    public void testSingle() {
	VobScene vobscene = new SimpleVobScene();

	XBuoy b1 = new XBuoy(xvobs[0], 30, 20);
	vobscene.put(xvobs[0], 25, 100, 150, 20, 20); // center at 110, 160

	Vob.Coords coords = new Vob.Coords();
	vobscene.getCoords(xvobs[0], coords);
	assertEquals(25, coords.depth);
	assertEquals(100, coords.x);
	assertEquals(150, coords.y);
	assertEquals(20, coords.width);
	assertEquals(20, coords.height);

	Point p = coords.getCenter(null);
	assertEquals(110, p.x);
	assertEquals(160, p.y);

	(new Buoy2()).place(vobscene, new Buoy[] { b1 }, 
				new Rectangle(200, 0, 100, 300));

	assertTrue(b1.set);
	assertEquals(200, b1.x);
	assertEquals(150, b1.y);
	assertEquals(30, b1.w);
	assertEquals(20, b1.h);

	// re-init
	b1 = new XBuoy(xvobs[0], 30, 20);
	XBuoy b2 = new XBuoy(xvobs[1], 40, 10);
	vobscene.put(xvobs[1], 30, 80, 155, 20, 20);

	(new Buoy2()).place(vobscene, new Buoy[] { b1, b2 },
				new Rectangle(200, 0, 100, 300));
				
	// b1 must be the same as earlier
	assertTrue(b1.set);
	assertEquals(200, b1.x);
	assertEquals(150, b1.y);
	assertEquals(30, b1.w);
	assertEquals(20, b1.h);
	// b2 must be just below b1
	assertTrue(b2.set);
	assertEquals(200, b2.x);
	assertEquals(170, b2.y);
	assertEquals(40, b2.w);
	assertEquals(10, b2.h);


    }

}

