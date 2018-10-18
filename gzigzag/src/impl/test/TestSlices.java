/*   
TestSlices.java
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
 * Written by Benja Fallenstein and Tuukka Hastrup
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;
import junit.framework.*;
import java.io.*;
import java.util.*;

/** Test slices.
 */

public class TestSlices extends TestCase {
public static final String rcsid = "$Id: TestSlices.java,v 1.11 2002/03/10 14:44:25 bfallenstein Exp $";

    static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }

    public TestSlices(String s) { super(s); }

    PermanentSpace space;
    PermanentSpace incl;
    Mediaserver ms;

    /** Cells in the enclosing space.
     */
    String s1, s2, s3, s4, s5, s1c;
    Cell c1, c1c, c2, c3, c4, c5;

    /** The cell that includes the included space.
     */
    Cell ci;

    /** Cells in the included space.
     *  <code>i1.space == incl</code>
     */
    Cell i1, i2, i3;

    /** Cells from the included space, as reflected in the enclosing space.
     *  <code>ci1.space == space</code>
     */
    Cell ci1, ci2, ci3;

    public void setUp() throws Exception {
	space = new PermanentSpace(TestImpl.zms);
	
	c1 = space.N();
        c2 = space.N();
        c3 = space.N();
        c4 = space.N();
        c5 = space.N();
	c1c = c1.zzclone();

	ms = TestImpl.zms;

	Mediaserver.Id delta0 = space.save(ms);
	String id = delta0.getString();
	s1=id+"-1"; s2=id+"-2"; s3=id+"-3"; s4=id+"-4"; s5=id+"-5"; s1c=id+"-6";

        space = new PermanentSpace(ms, delta0);
        c1 = space.getCell(s1); c2 = space.getCell(s2); c3 = space.getCell(s3);
        c4 = space.getCell(s4); c5 = space.getCell(s5);
	c1c = space.getCell(s1c);

	incl = new PermanentSpace(TestImpl.zms);
	i1 = incl.getHomeCell(); i2 = incl.N(); i3 = i1.zzclone();
	i1.setText("one"); i1.connect(i1, i2); i2.setText("two");
	i2.connect(i1, i3);
	Mediaserver.Id incl_id = incl.save(ms);
	assertEquals(i2, i1.s(i3));

	incl = new PermanentSpace(ms, incl_id);
    }

    public void testIsVStreamDim() {
	// try to cast the Dim object to VStreamDim:
	// will throw a ClassCastException if the test fails
	VStreamDim bla = (VStreamDim)space.getDim(Dims.d_vstream_id);
    }

    public void testNonEditable() throws Exception {
	doSliceTest(false);
    }

    public void testEditable() throws Exception {
	doSliceTest(true);
    }

    /** @param editable Is the tested slice editable (== not updateable)
      */
    public void doSliceTest(boolean editable) throws Exception {
	ci = space.N();
	space.include(ci, incl, editable);

	i1 = incl.getHomeCell(); ci1 = space.translate(i1);
	i2 = i1.s(i1); ci2 = space.translate(i2);
	i3 = i2.s(i1); ci3 = space.translate(i3);

	c1.connect(ci1, ci1);

	ci1.connect(ci2, ci2);
	
	Cell cin = ci2.N(ci2);
	c5.connect(c5, cin);

	assertEquals(i2, i1.s(i1));
	assertEquals(i2, i1.s(i3));
	assertEquals("one", i3.t());
	assertEquals("two", i2.t());
	assertEquals(i1, i3.h(i3));
	assertEquals(null, i1.s(i1, -1));

	if(!editable) {
	    assertEquals(null, i1.s(i2));
	    assertEquals(null, i2.s(i2));
	    assertEquals(null, space.getSpace(cin));
	} else {
	    assertEquals(i2, i1.s(i2));
	    assertNotNull(i2.s(i2));
	    assertEquals(incl, space.getSpace(cin));
	}
	
	assertEquals(ci2, ci1.s(ci1));
	assertEquals(ci2, ci1.s(ci3));
	assertEquals("one", ci3.t());
	assertEquals("two", ci2.t());
	assertEquals(c1, ci3.h(ci3));
	assertEquals(c1, ci1.s(ci1, -1));

	assertEquals(ci2, ci1.s(ci2));
	assertEquals(cin, ci2.s(ci2));
	assertEquals(cin, c5.s(c5));

	if(!editable) {
	    try {
		ci2.setText("two, too");
		fail("no exception when editing text in non-editable slice");
	    } catch(ZZError _) {
	    }

	    Cell nu = space.N(ci2);
	    assertEquals(null, space.getSpace(nu));
	} else {
	    ci2.setText("two, too");
	    assertEquals("two, too", ci2.t());
	    assertEquals("two, too", i2.t());

	    Cell nu = space.N(ci2);
	    assertEquals(incl, space.getSpace(nu));

	    nu = space.N(c1);
	    assertEquals(null, space.getSpace(nu));


	    // Test cloning
	    assertEquals(null, space.getSpace(ci.zzclone(ci)));
	    assertEquals(incl, space.getSpace(ci2));
	    assertEquals(incl, space.getSpace(ci.zzclone(ci2)));
	    // Finished testing cloning
	}
	
	Mediaserver.Id delta1 = space.save(ms);
	if(dbg)
	    System.err.println("Space saved: "+delta1+"\n"
			       +new String(ms.getDatum(delta1).getRaw()));
	space = new PermanentSpace(ms, delta1);

	c1 = space.getCell(s1);
	String ciid = ci.id.substring(ci.id.indexOf("-"));
	ci1 = space.getCell(delta1.getString() + ciid + ":home-cell");
        ci2 = ci1.s(ci1);
        ci3 = ci2.s(ci1);
	c5 = space.getCell(s5);

        assertEquals(ci2, ci1.s(ci1));
        assertEquals(ci2, ci1.s(ci3));
        assertEquals("one", ci3.t());
	if(!editable)
	    assertEquals("two", ci2.t());
	else
	    assertEquals("two, too", ci2.t());
        assertEquals(c1, ci3.h(ci3));

	assertEquals(ci2, ci1.s(ci2));
	assertNotNull(ci2.s(ci2));
	assertEquals(ci2.s(ci2), c5.s(c5));
    }
}
