/*   
TestUtil.java
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

package org.gzigzag.util;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import junit.framework.*;

/** Perform all currently relevant tests for org.gzigzag.util.
 */

public class TestUtil {
public static final String rcsid = "$Id: TestUtil.java,v 1.8 2001/10/17 20:10:31 tjl Exp $";

    public static Test suite() {
	TestSuite suite = new TestSuite("org.gzigzag.util tests");
        suite.addTestSuite(TestLoopDetector.class);
        suite.addTestSuite(TestHex.class);
        suite.addTestSuite(TestUmlauts.class);
        suite.addTestSuite(TestMacMouse.class);
	suite.addTestSuite(TestStringSearchers.class);
	suite.addTestSuite(TestGeomUtil.class);
	return suite;
    }
    
    public static Test suiteSlow() {
        TestSuite suite = new TestSuite("org.gzigzag.util tests");
        suite.addTestSuite(TestUTF8Char.class);
        return suite;
    }
}



