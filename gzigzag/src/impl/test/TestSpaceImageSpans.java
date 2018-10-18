/*   
TestSpaceImageSpans.java
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
import org.gzigzag.util.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import java.io.*;
import junit.framework.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

/** Test Image and Page spans stored in spaces (currently SimpleTransientSpace).
 */

public class TestSpaceImageSpans extends TestCase {
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }

    public TestSpaceImageSpans(String name) { super(name);} 

    Mediaserver ms = TestImpl.zms;

    PermanentSpace space;
    Cell c1, c2;

    public void setUp() throws Exception {
	space = new PermanentSpace(ms);
	c1 = space.N();
	c2 = space.N();
    }

    public void testPut() throws Exception {
	ImageSpan span = TestMSImage.getIS(TestMSImage.pid);
	c1.setSpan(span);
	ImageSpan sp2 = (ImageSpan)c1.getSpan();
	assertEquals(span, sp2);
    }

    public void testSpanSet() throws Exception {
	ImageSpan span1 = TestMSImage.getIS(TestMSImage.pid);
	ImageSpan span2 = TestMSImage.getIS(TestMSImage.jid);

	ImageSpan a = span1.subArea(10, 10, 10, 10);
	ImageSpan b = span1.subArea(15, 10, 20, 10);
	ImageSpan c = span1.subArea(30, 10, 10, 10);

	Cell ca = space.N();
	Cell cb = space.N();
	Cell cc = space.N();

	c1.setSpan(span1);
	c2.setSpan(span2);
	ca.setSpan(a);
	cb.setSpan(b);
	cc.setSpan(c);

	SpanSet ss = space.getSpanSet();

	Set o = new HashSet(ss.overlaps(a));

	assertEquals(
	 new HashSet(Arrays.asList(
	    new Object[] { ca, cb, c1 } )),
	 o);
	    
	o = new HashSet(ss.overlaps(b));

	assertEquals(
	 new HashSet(Arrays.asList(
	    new Object[] { ca, cb, cc, c1 } )),
	 o);

	o = new HashSet(ss.overlaps(span2));
	assertEquals(
	 new HashSet(Arrays.asList(
	    new Object[] { c2 } )),
	 o);

    }

    public void testRealImagePage() throws Exception {
// The space where a couple of first .pdf spans were made.
	Mediaserver.Id id = new Mediaserver.Id(
"0000000008000000E9385C9FE900041EDB882005FF8ADD3E2026C9444B5E305E02FE4FC6C12CEA"
	    );
	space  = new PermanentSpace(ms, id);

	SpanSet spanset = space.getSpanSet();
	Collection spans = spanset.spans();

	// Now, for each span, assert we have at least one object!
	// this fails, if we have an incorrectly implemented span type.
	for(Iterator i = spans.iterator(); i.hasNext();) {
	    Span s = (Span)i.next();
	    Collection overlaps = spanset.overlaps(s);
	    assertTrue(overlaps.size() > 0);
	}
	
    }
}
