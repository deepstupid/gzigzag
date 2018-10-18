/*   
TestSlow.java
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
 * Written by Benja Fallenstein
 */

package org.gzigzag.test;
import junit.framework.*;

/** Perform all tests that are too slow to be run on every make.
 *  Covers org.gzigzag.impl and org.gzigzag.util.
 */

public class TestSlow {
public static final String rcsid = "$Id: TestSlow.java,v 1.1 2001/07/28 17:23:49 bfallenstein Exp $";

    public static junit.framework.Test suite() {
	TestSuite suite = new TestSuite("GZigZag unstable tests");
	suite.addTest(org.gzigzag.util.TestUtil.suiteSlow());
	suite.addTest(org.gzigzag.impl.TestImpl.suiteSlow());
	return suite;
    }
}



