/*   
TestClasmTestSpace.java
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

package org.gzigzag.impl.clasm;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import junit.framework.*;
import java.io.*;

/** Test the reader and writer for GZZ1 space format
 */

public class TestClasmTestSpace extends TestCase {
public static final String rcsid = "$Id: TestClasmTestSpace.java,v 1.1 2001/07/02 16:16:23 bfallenstein Exp $";

    static void p(String s) { System.out.println(s); }

    public TestClasmTestSpace(String s) { super(s); }

    ClasmTestSpace sp = new ClasmTestSpace();

    public void testNullJavaObject() {
	assertEquals(null, sp.N().getJavaObject());
	assertEquals(null, sp.getJavaObject(sp.N(), null));
    }

    public void testSetGetJavaObject() {
	Object ob1 = "<ob1>", ob2 = "<ob2>";
	Cell c = sp.N();

	sp.setJavaObject(c, ob1);
	assertEquals(ob1, c.getJavaObject());
	assertEquals(ob1, c.getJavaObject());
	
	sp.setJavaObject(c, ob2);
	assertEquals(ob2, sp.getJavaObject(c, null));
	assertEquals(ob2, sp.getJavaObject(c, null));
    }
}




