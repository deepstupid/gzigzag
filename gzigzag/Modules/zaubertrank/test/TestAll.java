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
package org.zaubertrank;
import org.gzigzag.*;
import org.gzigzag.clang.*;
import junit.framework.*;

/** Perform all current JUnit tests.
 */

public class TestAll {
public static final String rcsid = "$Id: TestAll.java,v 1.2 2001/04/21 04:24:26 bfallenstein Exp $";

    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTestSuite(TestZaubertrankTemplate.class);
	return suite;
    }

}
