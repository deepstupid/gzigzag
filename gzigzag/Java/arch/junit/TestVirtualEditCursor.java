/*   
TestVirtualEditCursor.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
import org.gzigzag.*;
import junit.framework.*;
import java.awt.*;
import java.awt.event.*;

/** A JUnit test for the VirtualEditCursor class.
 */

public class TestVirtualEditCursor extends TestCase {
public static final String rcsid = "$Id: TestVirtualEditCursor.java,v 1.1 2001/04/23 12:07:26 bfallenstein Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) ZZLogger.log(s); }
    protected static void pa(String s) { ZZLogger.log(s); }

    public TestVirtualEditCursor(String s) { super(s); }

    ZZDimSpace sp = new ZZDimSpace();
    ZZCell home = sp.getHomeCell();
    
    public void testEditCursorStack() {
	ZZCell[] c = new ZZCell[] { home.N(), home.N(), home.N(), home.N() };
	VirtualEditCursor cur = new VirtualEditCursor(c[0]);

	cur.set(c[0]);
	assertEquals(c[0], cur.get());
	assertEquals(c[0], cur.get());
	
	// Push
	cur.push(c[1]);
	assertEquals(c[1], cur.get());
	assertEquals(c[1], cur.get());
	
	cur.set(c[2]);
	assertEquals(c[2], cur.get());
	assertEquals(c[2], cur.get());
	
	cur.push(c[3]);
	assertEquals(c[3], cur.get());
	assertEquals(c[3], cur.get());
	
	// Now pop
	cur.pop();
	assertEquals(cur.get(), c[2]);
	assertEquals(c[2], cur.get());
	assertEquals(c[2], cur.get());
	
	cur.pop();
	assertEquals(c[0], cur.get());
	
	cur.set(c[3]);
	cur.push(c[1]);
	assertEquals(c[1], cur.get());
	
	// Control the getStack() function
	cur = new VirtualEditCursor(c[0]);
	cur.push(c[2]);
	cur.push(c[1]);
	cur.push(c[1]);
	ZZCursor[] cs = cur.getStack();
        assertEquals(4, cs.length);
	assertEquals(c[2], cs[1].get());
	assertEquals(c[0], cs[0].get());
	assertEquals(c[1], cs[3].get());

	// Assure that popping the topmost cursor will raise an exception
	cur.pop();
	cur.pop();
	cur.pop();
	
	try {
	    cur.pop();
	    fail("Popping topmost cursor didn't raise an exception!");
	} catch(Exception e) {
	}
    }
    
}
