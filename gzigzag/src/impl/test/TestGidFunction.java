
/*   
TestPermanentSpace.java
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

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;
import junit.framework.*;
import java.io.*;
import java.util.*;

/** Test simpleSpace.gid
 */

public class TestGidFunction extends TestCase {
public static final String rcsid = "$Id: TestGidFunction.java,v 1.3 2002/03/10 01:16:23 bfallenstein Exp $";

    static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }

    public TestGidFunction(String s) { super(s); }

    private Map map = new HashMap();

    public void setUp() {
	map.put("tmp(1)", "foo");
	map.put("tmp(2)", "bar");
	map.put("tmp(42)", "baz");
    }

    private String gid(String orig) { 
	return new String(PermanentSpace.gid(orig, map)); 
    }

    public void testGidFunction() {
	assertEquals("blah", gid("blah"));
	assertEquals("blah-foo", gid("blah-foo"));
	assertEquals("foo-xyz", gid("tmp(1)-xyz"));
	assertEquals("foo-1:bar-2", gid("tmp(1)-1:tmp(2)-2"));
	assertEquals("foo-1:bar-2:baz-0", gid("tmp(1)-1:bar-2:tmp(42)-0"));
	assertEquals("foo bar/baz", gid("tmp(1) tmp(2)/tmp(42)"));
    }
}
