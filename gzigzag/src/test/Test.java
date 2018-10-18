/*   
Test.java
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

/** Perform all currently relevant tests for our GZigZag implementation. 
 *  Covers org.gzigzag.impl and org.gzigzag.vob.
 */

public class Test {
public static final String rcsid = "$Id: Test.java,v 1.8 2002/03/08 21:14:39 bfallenstein Exp $";

    public static junit.framework.Test suite() {
	TestSuite suite = new TestSuite("GZigZag tests");
	suite.addTest(org.gzigzag.impl.TestImpl.suite());
	suite.addTest(org.gzigzag.client.TestClient.suite());
	suite.addTest(org.gzigzag.util.TestUtil.suite());
	suite.addTest(org.gzigzag.mediaserver.TestMS.suite());
	suite.addTest(org.gzigzag.vob.TestVobs.suiteStable());
	return suite;
    }
}
