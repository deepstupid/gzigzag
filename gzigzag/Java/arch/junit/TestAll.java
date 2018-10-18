/*   
TestAll.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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
package org.gzigzag.test;
import junit.framework.*;

/** Perform all current JUnit tests.
 */

public class TestAll {
public static final String rcsid = "$Id: TestAll.java,v 1.2 2001/04/23 11:38:53 bfallenstein Exp $";

    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new TestSuite(TestEditCursors.class));
        suite.addTest(new TestSuite(TestVirtualEditCursor.class));
	suite.addTest(new TestSuite(TestIntersections.class));
	suite.addTest(new TestSuite(TestMacMouse.class));
	suite.addTest(new TestSuite(TestUmlauts.class));
	suite.addTest(new TestSuite(TestModuleDirActions.class));
	suite.addTest(TestArchimedes.suite());
	return suite;
    }

}
