/*   
TestDims.java
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

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import junit.framework.*;

/** Test various dimension-related issues
 */

public class TestDims {
public static final String rcsid = "$Id: TestDims.java,v 1.2 2001/08/17 21:02:01 bfallenstein Exp $";

    public static Test suite() {
	TestSuite suite = new TestSuite("tests for dimension implementations");
	suite.addTestSuite(TestSimpleDim.class);
	suite.addTestSuite(TestSimpleDimInvariants.class);
	return suite;
    }
}



