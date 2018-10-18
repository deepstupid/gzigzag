/*   
TestFullSpacepart.java
 *    
 *    Copyright (c) 2002, Benja Fallenstein
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


package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.client.Client;
import org.gzigzag.client.*;
import org.gzigzag.vob.*;
import org.gzigzag.util.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import java.io.*;
import junit.framework.*;
import java.awt.*;
import java.awt.image.*;

/** Test FullSpacepart.
 */

public class TestFullSpacepart extends TestCase {
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }

    public TestFullSpacepart(String name) { super(name);} 

    Spacepart part;

    public void setUp() {
	Space s = new SimpleTransientSpace();
	part = new FullSpacepart(s, s.getHomeCell(), ":",
				 Spacepart.NON_EDITABLE);
    }

    public void testCells() {
	assertNotNull(part.getCell("home-cell:fongbar"));
	assertNotNull(part.getCell("home-cell:xanadoodler.org"));
	assertNotNull(part.getCell("home-cell:skip the noodles"));
	assertNotNull(part.getCell("home-cell:foo:is:bar"));

	assertTrue(!part.exists("home-cell:"));

	try {
	    part.getCell("home-cell:");
	    fail("No error when getting non-existing cell");
	} catch(Throwable t) {
	}
    }

}
