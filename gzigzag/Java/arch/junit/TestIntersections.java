/*   
TestIntersections.java
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

/** A JUnit test for the intersect method of ZZCells.
 *  intersectAll is not currently tested.
 */

public class TestIntersections extends TestCase {
public static final String rcsid = "$Id: TestIntersections.java,v 1.1 2001/04/19 14:47:11 bfallenstein Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) ZZLogger.log(s); }
    protected static void pa(String s) { ZZLogger.log(s); }

    public TestIntersections(String s) { super(s); }

    ZZDimSpace sp = new ZZDimSpace();
    ZZCell home = sp.getHomeCell();
    
    public void testSimpleIntersection() {
	ZZCell c = home.N();
	ZZCell c1 = c.N("d.1").N("d.1").N("d.1");
	ZZCell c2 = c.N("d.2", -1).N("d.2", -1);
	
	c1.N("d.1").N("d.1");
	c2.N("d.2", -1);
	
	assertEquals(c, c1.intersect("d.1", -1, c2, "d.2", 1));
	assertEquals(c, c2.intersect("d.2", 1, c1, "d.1", -1));
    }

    /** An intersection of a cell with itself should return that cell. */
    public void testSameCellIntersection() {
	ZZCell c = home.N();
	c.N("d.1").N("d.1"); c.N("d.1", -1);
	c.N("d.2"); c.N("d.2", -1);
	
	assertEquals(c, c.intersect("d.1", 1, c, "d.2", -1));
	assertEquals(c, c.intersect("d.2", 1, c, "d.1", -1));
    }

    /** The cells where we start from can be the intersections. */
    public void testIntersectionAtFirstCell() {
	ZZCell c = home.N();
	ZZCell c2 = c.N("d.1", -1).N("d.1", -1);
	c.N("d.2", -1); c.N("d.2", 1);
	
	assertEquals(c, c.intersect("d.2", 1, c2, "d.1", 1));
	assertEquals(c, c2.intersect("d.1", 1, c, "d.2", 1));
    }
    
}
