/*   
TestClasm.java
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

package org.gzigzag.impl.clasm;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import junit.framework.*;

/** Test clasm.
 */

public class TestClasm {
public static final String rcsid = "$Id: TestClasm.java,v 1.8 2001/07/13 11:57:22 bfallenstein Exp $";

    public static Test suite() {
	TestSuite suite = new TestSuite("tests for clasm");
	suite.addTestSuite(TestClasmTestSpace.class);
	suite.addTestSuite(TestVariable.class);
	suite.addTestSuite(TestPrimitive.class);
	suite.addTestSuite(TestCallExpression.class);
	suite.addTestSuite(TestConstantExpression.class);
	suite.addTestSuite(TestFunction.class);
    suite.addTestSuite(TestClasmPrimitiveSet1.class);
	return suite;
    }
}



