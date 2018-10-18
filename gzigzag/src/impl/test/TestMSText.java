/*   
TestMSText.java
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


public class TestMSText extends TestCase {
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }

    public TestMSText(String name) { super(name);} 

    Mediaserver zms = TestImpl.zms;

    public void testCreateRead() throws Exception {
	TransientTextScroll scr = new TransientTextScroll();
	scr.append('a');
	scr.append('b');
	scr.append('c');
	scr.append('\u263a'); // smileface;WHITE SMILING FACE
	scr.append('d');
	Mediaserver.Id id = scr.save(zms, null);
	ScrollBlock sb = ScrollBlockManager.getScrollBlock(zms, id);
	TextSpan cur = (TextSpan)sb.getCurrent();
	assertEquals("abc\u263ad", cur.getText());
    }


}
