/*
TestCellVobFactory.java
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

package org.gzigzag.impl;
import org.gzigzag.*;
import junit.framework.*;

/** Test that cellvobfactory does the right things.
 */

public class TestCellVobFactory extends TestCase {
public static final String rcsid = "$Id: TestCellVobFactory.java,v 1.2 2001/09/27 20:55:43 deetsay Exp $";

    public TestCellVobFactory(String s) { super(s); }

	// using Math.atan2(double, double) now :-)
    public void testAngles() {
    // up
/*	assertEquals(0, CellVobFactory.getAngle(0, -1));

	assertTrue(CellVobFactory.getAngle(1, -10) > 0);
	assertTrue(CellVobFactory.getAngle(1, -10) < 45);

    // up right
	assertEquals(45, CellVobFactory.getAngle(1, -1));

	assertEquals(84, CellVobFactory.getAngle(10, -1));
	assertEquals(90, CellVobFactory.getAngle(1,0));
	assertEquals(96, CellVobFactory.getAngle(10, 1));

    // down right
	assertEquals(90+45, CellVobFactory.getAngle(1, 1));

    // up left
	assertEquals(-45, CellVobFactory.getAngle(-1, -1));


	assertEquals(-84, CellVobFactory.getAngle(-10, -1));
	assertEquals(-90, CellVobFactory.getAngle(-1,0));
	assertEquals(-96, CellVobFactory.getAngle(-10, 1));

    // down left
	assertEquals(-90-45, CellVobFactory.getAngle(-1, 1));
    */
    }
}




