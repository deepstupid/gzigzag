/*   
TestArchimedes.java
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
import org.gzigzag.clang.test.*;
import junit.framework.*;

/** Test for the Archimedes Clang Procedural Level
 */

public class TestArchimedes {
public static final String rcsid = "$Id: TestArchimedes.java,v 1.3 2001/04/21 01:00:02 bfallenstein Exp $";

    private static final void pa(String s) { System.out.println(s); }

    // public TestArchimedes(String s) { super(s); }

    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new TestSuite(
		org.gzigzag.clang.test.TestObjects.class));
	suite.addTest(new TestSuite(
		org.gzigzag.clang.test.TestPrimitives.class));
	suite.addTest(new TestSuite(
		org.gzigzag.clang.test.TestCompounds.class));
	suite.addTest(new TestSuite(
		org.gzigzag.clang.test.TestFunctions.class));
	suite.addTest(new TestSuite(
		org.gzigzag.clang.test.TestEarlyTermination.class));
	suite.addTest(new TestSuite(
	        org.gzigzag.clang.test.TestTemplate.class));
	return suite;
    }

}
