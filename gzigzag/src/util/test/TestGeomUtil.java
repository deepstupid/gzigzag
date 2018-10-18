/*   
TestGeomUtil.java
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
package org.gzigzag.util;
import junit.framework.*;
import java.awt.*;

/** A unit test for GeomUtil.
 */

public class TestGeomUtil extends TestCase {
String rcsid = "$Id: TestGeomUtil.java,v 1.3 2001/10/17 20:27:57 tjl Exp $";

    public TestGeomUtil(String s) { super(s); }

    public void testFit() {
	Rectangle frame1 = new Rectangle(20, 20, 50, 100);

	Dimension d1 = new Dimension(50, 50);
	GeomUtil.fitRectangle(frame1, d1, true);
	assertEquals(50, d1.width);
	assertEquals(50, d1.height);

	Rectangle frame2 = new Rectangle(20, 20, 40, 100);
	GeomUtil.fitRectangle(frame2, d1, true);
	assertEquals(40, d1.width);
	assertEquals(40, d1.height);

	Rectangle frame3 = new Rectangle(20, 20, 600, 30);
	GeomUtil.fitRectangle(frame3, d1, true);
	assertEquals(30, d1.width);
	assertEquals(30, d1.height);

	Rectangle frame4 = new Rectangle(20, 20, 800, 800);
	GeomUtil.fitRectangle(frame4, d1, true);
	assertEquals(30, d1.width);
	assertEquals(30, d1.height);
	// XXX Test not maintaining aspect
    }

    public void testPlace() {
	Rectangle frame = new Rectangle(20, 30, 100, 100);
	Dimension d = new Dimension(20, 20);
	Point p = new Point(0, 0);
	Rectangle out = new Rectangle();

	GeomUtil.placeRectangle(frame, d, p, out);

	assertEquals(20, out.x);
	assertEquals(30, out.y);
	assertEquals(20, out.width);
	assertEquals(20, out.height);

	p.y = 60;
	GeomUtil.placeRectangle(frame, d, p, out);

	assertEquals(20, out.x);
	assertEquals(50, out.y);
	assertEquals(20, out.width);
	assertEquals(20, out.height);
    }

    /** Tests that come from the buoys' use of this.
     */
    public void testPlaceBuoy() {
	Rectangle cr = new Rectangle(200, 0, 100, 300);
	Dimension size = new Dimension(30, 20);
	Point p = new Point(110, 160);
	Rectangle out = new Rectangle();

	GeomUtil.placeRectangle(cr, size, p, out);
	assertEquals(200, out.x);
	assertEquals(150, out.y);
	assertEquals(30, out.width);
	assertEquals(20, out.height);
    }
}

