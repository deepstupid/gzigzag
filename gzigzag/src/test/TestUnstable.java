/*   
TestUnstable.java
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

package org.gzigzag.test;
import junit.framework.*;

/** Perform all tests that may currently fail due to 
 * changes.
 *  Covers org.gzigzag.impl and org.gzigzag.vob.
 */

public class TestUnstable {
public static final String rcsid = "$Id: TestUnstable.java,v 1.3 2001/07/19 12:17:48 tjl Exp $";

    public static junit.framework.Test suite() {
	TestSuite suite = new TestSuite("GZigZag unstable tests");
	suite.addTest(org.gzigzag.vob.TestVobs.suiteUnstable());
	suite.addTestSuite(org.gzigzag.impl.TestMerge1.class);
	return suite;
    }
}



