/*   
TestClient.java
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

package org.gzigzag.client;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import java.io.*;
import junit.framework.*;
import java.awt.*;

/** Perform all currently relevant tests for org.gzigzag.client.
 */
public class TestClient {
public static final String rcsid = "$Id: TestClient.java,v 1.1 2002/02/08 07:57:12 tjl Exp $";
    public static Test suite() {
	TestSuite suite = new TestSuite("org.gzigzag.client tests");
	suite.addTestSuite(TestSteppersFuncs.class);
	return suite;
    }
}
