/*   
TestGZZ1.java
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

/** Test the reader and writer for GZZ1 space format
 */

public class TestGZZ1 extends TestCase {
public static final String rcsid = "$Id: TestGZZ1.java,v 1.16 2002/03/10 01:16:23 bfallenstein Exp $";

    static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }

    public TestGZZ1(String s) { super(s); }

    /** Cell IDs. 
     * Important to have both with and without space since
     * they are encoded differently.
     */
    static String s1="test-cell 1",s2="test-cell2",s3="test-cell 3",
	s4="test-cell 4",s5="test-cell 5";
    static byte[] id1 = s1.getBytes(), id2 = s2.getBytes(),
	id3 = s3.getBytes(), id4 = s4.getBytes(), id5 = s5.getBytes();


    PermanentSpace space;
    Cell c1, c2, c3, c4, c5;

    public void setUp() throws IOException {
	space = new PermanentSpace(TestImpl.zms);

        space.gzz1_NewCell(s1); c1 = space.getCell(s1);
        space.gzz1_NewCell(s2); c2 = space.getCell(s2);
        space.gzz1_NewCell(s3); c3 = space.getCell(s3);
        space.gzz1_NewCell(s4); c4 = space.getCell(s4);
        space.gzz1_NewCell(s5); c5 = space.getCell(s5);
    }

    public void testWriteRead() throws IOException {
	Writer sw = new StringWriter();
	GZZ1Writer w = new GZZ1Writer(sw);
	w.start(null);
	GZZ1Handler.SimpleDim dim = w.dimSection(id1);
	dim.connect(id1, id2);
	dim.connect(id3, id5);
	dim.close();
	w.close();

	GZZ1SpaceHandler spacehdl = new GZZ1SpaceHandler(space, null);
	p("W: "+sw.toString());
	Reader r = new StringReader(sw.toString());
	GZZ1Reader.read(r, spacehdl);

	assertEquals(c2, c1.s(c1));
	assertEquals(c3, c5.s(c1, -1));
	assertEquals(null, c4.s(c1));

	// Now, mediaserver stuff...

	Mediaserver ms = new SimpleMediaserver(
	    new org.gzigzag.mediaserver.storage.TransientStorer(),
	    new org.gzigzag.mediaserver.ids.IDSpace(),
	    0);
	Mediaserver.Id delta1 = ms.addDatum(sw.toString().getBytes(),
					    "application/x-gzigzag-GZZ1");
	
	sw = new StringWriter();
	w = new GZZ1Writer(sw);
        w.start(delta1);
        dim = w.dimSection(id1);
        dim.disconnect(id1, id2);
        dim.connect(id1, id4);
	dim.connect(id4, id2);
        dim.close();
	w.close();
	Mediaserver.Id delta2 = ms.addDatum(sw.toString().getBytes(),
					    "application/x-gzigzag-GZZ1");

	// new space, new cells
	setUp();

	GZZ1SpaceHandler.read(ms, delta2, space);

        assertEquals(c4, c1.s(c1));
	assertEquals(c2, c4.s(c1));
        assertEquals(c3, c5.s(c1, -1));
        assertEquals(null, c2.s(c1));

    }
}
