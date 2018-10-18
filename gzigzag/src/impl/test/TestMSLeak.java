/*   
TestMSLeak.java
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
 * Written by Tuukka Hastrup
 */


package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import org.gzigzag.util.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import java.io.*;
import junit.framework.*;
import java.awt.*;
import java.awt.image.*;
import java.util.Set;

public class TestMSLeak extends TestCase {
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }

    public TestMSLeak(String name) { super(name);} 

    Mediaserver zms0 = TestImpl.zms0;
    Set zms0ids = TestImpl.zms0ids;

    public void testLeakedIDs() throws Exception {
	if(!zms0ids.equals(zms0.getIDs()))
	    throw new ZZError("Tests leak blocks to permanent Mediaserver "
			      +"storage");
    }
}
