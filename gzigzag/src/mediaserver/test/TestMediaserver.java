/*   
TestMediaserver.java
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

package org.gzigzag.mediaserver;
import org.gzigzag.util.*;
import java.util.*;
import junit.framework.*;
import java.io.*;

/** Abstract test for Mediaserver implementations.
 */

public abstract class TestMediaserver extends ZZTestCase {
public static final String rcsid = "$Id: TestMediaserver.java,v 1.23 2002/03/30 02:06:33 bfallenstein Exp $";
    static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }

    public TestMediaserver(String name) { super(name); }

    /** The mediaserver to use.
     *  To be set by subclasses.
     */
    public Mediaserver ms;

    public void testAddGet() throws IOException {
        byte[] datum = new byte[] { 0x42, 0x12, 0x39 };
	Mediaserver.Id id = ms.addDatum(datum, "x-foo/x-foo");
	Mediaserver.Block block = ms.getDatum(id);
	assertEquals(datum, block.getBytes());
	assertEquals("x-foo/x-foo", block.getContentType());
	
	datum[2] = 0x22;
	id = ms.addDatum(datum, "x-foo/x-foo");
	assertEquals(datum, ms.getDatum(id).getBytes());
    }

    public void testAddGetWithOwnHeaders() throws IOException {
	byte[] datum = new byte[] { 0x42, 0x12, 0x39 };
	String[] headers = new String[] {
	    "Content-type: x-foo/x-foo-bar",
	    "Subject: This is a Mediaserver text.",
	    "X-Foo: Is X-Foo over and over, and over again.",
	    "Content-transfer-encoding: binary",
	    "X-Injected-By: A nice cozy Mediaserver test program.",
	};

	Mediaserver.Id id = ms.addDatum(datum, headers, null);
	Mediaserver.Block block = ms.getDatum(id);

	assertEquals(datum, block.getBytes());
	assertEquals("x-foo/x-foo-bar", block.getContentType());

	// XXX should also test the headers are correct... sigh

        datum[2] = 0x22;
        id = ms.addDatum(datum, headers, null);
        assertEquals(datum, ms.getDatum(id).getBytes());
    }

    static byte b(int i) { return (byte)i; }

    public void testStore() throws IOException {
        byte[] datum = new byte[] {
            0x43, 0x6f, 0x6e, 0x74, 0x65, 0x6e, 0x74, 0x2d,
            0x54, 0x79, 0x70, 0x65, 0x3a, 0x20, 0x61, 0x70,
            0x70, 0x6c, 0x69, 0x63, 0x61, 0x74, 0x69, 0x6f,
            0x6e, 0x2f, 0x6f, 0x63, 0x74, 0x65, 0x74, 0x2d, 
            0x73, 0x74, 0x72, 0x65, 0x61, 0x6d, 0x0a, 0x0d,
            0x0a, 0x0d, 0x0a,
            10, 11, 12};
        byte[] id = new byte[] {
            0x00, // id format version
            0x00, 0x00, // no server id
            0x00, 0x08, // timestamp length?

            // timestamp
            0x12, 0x34, 0x56, 0x78,
            b(0x9a), b(0xbc), b(0xde), b(0xf1),

            0x00, 0x04, // length of the random block
            b(0xf6), b(0xef), 0x57, b(0xb3), // the random block
            
            // hash
            b(0xe9), 0x51, 0x22, 0x19, b(0xb3),
            0x6c, 0x76, b(0xea), 0x6e, 0x74,
            b(0xb7), 0x07, 0x64, 0x79, b(0xa8),
            0x6e, b(0xbf), b(0xa1), b(0xe9), 0x4a
        };

	Mediaserver.Id msid = new Mediaserver.Id(id);

	try {
            ms.storeDatum(msid, datum);
        } catch (Mediaserver.InvalidID _) {
            fail("caught InvalidID");
        }
	assertEquals(datum, ms.getDatum(msid).getRaw());
	
	datum[datum.length-1] = 0x44;
	id[3] = 0x49;
	msid = new Mediaserver.Id(id);

	try {
            ms.storeDatum(msid, datum);
            fail("no exception was thrown");
        } catch (Mediaserver.InvalidID _) {}
	assertTrue(!ms.getIDs().contains(msid));
    }

    public void testGetIDs() throws IOException {
	byte[] datum = new byte[] { 0x42, 0x12, 0x39 };
	
	assertEquals(0, ms.getIDs().size());

	Mediaserver.Id id1 = ms.addDatum(datum, "x-foo/x-foo");
	datum[1] = 0x01;
	Mediaserver.Id id2 = ms.addDatum(datum, "x-foo/x-foo");

	Set dir = ms.getIDs();
	assertEquals(2, dir.size());

	p("Dir: "+dir);
	p("Ids: "+id1+" "+id2);

	Object[] arr = dir.toArray();
	Mediaserver.Id qid = (Mediaserver.Id)arr[0];

	p("DirId: "+qid);

	p("DirIdEq: "+(qid.equals(id1))+" "+(qid.equals(id2)));

	assertTrue(dir.contains(id1));
	assertTrue(dir.contains(id2));
    }

    public void testIds() {
	Mediaserver.Id i1 = new Mediaserver.Id(new byte[] { 0x02, (byte)0x83 });
	Mediaserver.Id i2 = new Mediaserver.Id(new byte[] { 0x02, (byte)0x83 });
	Mediaserver.Id i3 = new Mediaserver.Id(new byte[] { 0x02, (byte)0x84 });
	Mediaserver.Id i4 = new Mediaserver.Id(new byte[] { 0x02, (byte)0x83, 0x40 });
	assertTrue(i1.equals(i2));
	assertTrue(!i1.equals(i3));
	assertTrue(!i1.equals(i4));

	assertTrue(i1.hashCode() == i2.hashCode());

	i1 = new Mediaserver.Id(new byte[] { 0x02, (byte)0x83, 
	    0x42, 0x53, 0x63 });
	i2 = new Mediaserver.Id(new byte[] { 0x02, (byte)0x83,
	    0x42, 0x53, 0x63});
	assertTrue(i1.hashCode() == i2.hashCode());
    }

    /** Create two local pointers to IDs and then change them.
     */
    public void testPointers() throws IOException {
        Mediaserver.Id i1 = new Mediaserver.Id(new byte[] { 0x02, (byte)0x83 });
        Mediaserver.Id i2 = new Mediaserver.Id(new byte[] { 0x04, (byte)0x83 });
        Mediaserver.Id i3 = new Mediaserver.Id(new byte[] { 0x02, (byte)0x84 });

        assertEquals(null, ms.getPointer("key1"));
	ms.setPointer("key1", i1);
	ms.setPointer("key2", i2);
	assertEquals(i1, ms.getPointer("key1"));
	assertEquals(i2, ms.getPointer("key2"));
	ms.setPointer("key1", i3);
	assertEquals(i3, ms.getPointer("key1"));
	assertEquals(i2, ms.getPointer("key2"));
    }
}



