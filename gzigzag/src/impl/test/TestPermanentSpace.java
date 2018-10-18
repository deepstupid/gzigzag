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

/** Test the persistent PermanentSpace.
 */

public class TestPermanentSpace extends TestCase {
public static final String rcsid = "$Id: TestPermanentSpace.java,v 1.7 2002/03/17 12:51:27 bfallenstein Exp $";

    static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static private void pa(String s) { System.out.println(s); }

    public TestPermanentSpace(String s) { super(s); }

    PermanentSpace space;
    Mediaserver ms;

    String s1, s1c, s2, s3, s4, s5;
    Cell c1, c1c, c2, c3, c4, c5;

    private String str(String id, Cell c) {
	return id + c.id.substring(c.id.indexOf("-"));
    }

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
	s1=str(id, c1); 
	s2=str(id, c2); 
	s3=str(id, c3); 
	s4=str(id, c4); 
	s5=str(id, c5); 
	s1c=str(id, c1c);

        space = new PermanentSpace(ms, delta0);
        c1 = space.getCell(s1); c2 = space.getCell(s2); c3 = space.getCell(s3);
        c4 = space.getCell(s4); c5 = space.getCell(s5);
	c1c = space.getCell(s1c);
    }

    /** Just see if there's an exception for now.
     */
    public void testGetRealDims() {
	Map m = space.getRealDims();
    }

    public void testSpanIds() throws Exception {
	Cell c = space.makeSpanRank("bla");
	space.getVStreamDim().insertAfterCell(c1, c);
	assertEquals(c.id, c1.s((Dim)space.getVStreamDim()).id);
	assertEquals(-1, c.id.indexOf("null"));
    }

    public void testObs() throws ZZException {
	ObsTrigger trig = space.trigger;
	c1.h(c1, -1, obs[0]).s(c1, obs[0]);
	trig.callQueued();
	assertResp(0, 0);
	c1.connect(c1, -1, c2);
        trig.callQueued();
	assertResp(1, 0);
	
	c1.connect(c2, c1c);
	c1c.connect(c2, c4);
	c2.h(c1, 1, obs[1]).s(c2, 1, obs[1]).s(c1, 1, obs[1]);
        trig.callQueued();
	assertResp(1, 0);
	c2.disconnect(c2, 1);
        trig.callQueued();
	assertResp(1, 0);
	c2.connect(c1, -1, c1);
        trig.callQueued();
	assertResp(1, 1);
    }

    public void testMediaserverNotNull() {
	assertNotNull(space.mediaserver);
    }

    public void testDVstreamInIdSpacepart() {
	space.getCell("home-id:bla").s(Dims.d_vstream_id);
    }

    public void testGetSpace() {
	Cell c = new SimpleTransientSpace().getHomeCell();
	try {
	    space.getSpace(c);
	    fail("No exception when doing getSpace() on a cell from "+
		 "a different space");
	} catch(ZZError e) {
	}
    }

    public void testLoadNull() throws IOException {
	try {
	    new PermanentSpace(ms, null);
	    fail("No exception when trying to load a space with ID <null>");
	} catch(NullPointerException _) {
	}
    }

    /** Assert that a space isn't saved if there haven't been changes. */
    public void testSaveNoChanges() throws IOException {
	assertEquals(null, space.save(ms));
    }

    public void testHomeCellExistence() {
	try {
	    Cell home = space.getHomeCell();
	    space.getCell(home.id);
	} catch(ZZImpossibleCellException e) {
	    fail("ImpossibleCell exception thrown when requesting home cell.");
	}
    }

    public void testWriteRead() throws Exception {
	String c1id = c1.id;

	p("twr10");

	c1.connect(c1, c2);
	c2.connect(c1, c4);
	c4.connect(c1, c5);
	c1.connect(c3, c5);

	c1.setText("c1");
	c2.setText("c2\nc2\\!");
	c5.setText("foo");

        assertEquals(c1, c2.s(c1, -1));
        assertEquals(c2, c4.s(c1, -1));

	c1.connect(space.getCell("home-id:bla"), c2);
	assertEquals(c2, c1.s(space.getCell("home-id:bla")));

	p("twr20");

	Mediaserver.Id delta1 = space.save(ms);
	space = new PermanentSpace(ms, delta1);
        c1 = space.getCell(s1); c2 = space.getCell(s2); c3 = space.getCell(s3);
        c4 = space.getCell(s4); c5 = space.getCell(s5); c1c = space.getCell(s1c);

	try {
	    space.getCell(s1+"blah");
	    fail("No exception when getting nonexistent cell");
	} catch(ZZImpossibleCellException e) {
	}

	p("twr30");

        assertEquals(c2, c1.s(space.getCell("home-id:bla")));

        assertEquals(c1, c2.s(c1, -1));
        assertEquals(c2, c4.s(c1, -1));
	assertEquals(c1, c1c.getRootclone());

	p("twr33");

	c1.setText("changed");
	p("twr34");
	space.getVStreamDim().insertAfterCell(c1, space.makeSpanRank("1-"));
	c2.setText(null);
	c4.setText("changed\n4\n");
	p("twr35");
        space.getVStreamDim().insertAfterCell(c5.s(Dims.d_vstream_id), space.makeSpanRank("bar"));

	p("twr36");

	c2.disconnect(c1, 1);
	c3.connect(c1, -1, c2);
	c3.connect(c1, c4);
	c1.disconnect(c3, 1);

	p("twr40");

	Mediaserver.Id delta2 = space.save(ms);

	p("ID 0: "+null);
	p("File 1:\n"+new String(ms.getDatum(delta1).getBytes()));

	space = new PermanentSpace(ms, delta1);
	c1 = space.getCell(s1); c2 = space.getCell(s2); c3 = space.getCell(s3);
	c4 = space.getCell(s4); c5 = space.getCell(s5);

	assertEquals(c1id, c1.id);

	assertEquals(c1, c2.s(c1, -1));
	assertEquals(c2, c4.s(c1, -1));
	assertEquals(null, c3.s(c1));
	assertEquals(c5, c1.s(c3));

	assertEquals("c1", c1.t());
	assertEquals("c2\nc2\\!", c2.t());
	assertEquals("", c3.t());
	assertEquals("", c4.t());
	assertEquals("foo", c5.t());

	p("ID 1: "+delta1);
        p("File 2:\n"+new String(ms.getDatum(delta2).getBytes()));
	p("ID 2: "+delta2);

        space = new PermanentSpace(ms, delta2);
        c1 = space.getCell(s1); c2 = space.getCell(s2); c3 = space.getCell(s3);
        c4 = space.getCell(s4); c5 = space.getCell(s5);

	p("twr50");

	assertEquals(c1, c2.s(c1, -1));
	assertEquals(c2, c3.s(c1, -1));
	assertEquals(c3, c4.s(c1, -1));
	assertEquals(c4, c5.s(c1, -1));
	assertEquals(null, c5.s(c1));
	assertEquals(null, c1.s(c3));

	assertEquals("1-changed", c1.t());
	assertEquals("", c2.t());
	assertEquals("", c3.t());
	assertEquals("changed\n4\n", c4.t());
	assertEquals("fbaroo", c5.t());
    }

    public void testInclusionIsLoaded() throws Exception {
	Cell ci = space.N();
  	PermanentSpace incl = new PermanentSpace(TestImpl.zms);
	space.include(ci, incl);

	assertNotNull(space.getSpaceByInclusionId(ci.id));

	Mediaserver.Id delta1 = space.save(ms);
	space = new PermanentSpace(ms, delta1);

	String si = str(delta1.getString(), ci);

	assertTrue(space.exists(si));
	assertNotNull(space.getSpaceByInclusionId(si));
    }

    public void testCompound() throws Exception {
	PermanentSpace incl = new PermanentSpace(TestImpl.zms);
	Cell i1 = incl.getHomeCell(), i2 = incl.N(), i3 = i1.zzclone();
	i1.setText("one"); i1.connect(i1, i2); i2.setText("two");
	i2.connect(i1, i3);
	Mediaserver.Id incl_id = incl.save(ms);
	assertEquals(i2, i1.s(i3));
	assertEquals(i2, i1.s(i1));

	incl = new PermanentSpace(ms, incl_id);
	Cell ci = space.N();
	space.include(ci, incl);

	i1 = space.translate(incl.getHomeCell());
	assertEquals(i1, space.getIncludedCell(ci, Id.space.getCell("home-id:home-cell")));
	i2 = i1.s(i1);
	assertNotNull(i2);
	i3 = i2.s(i1);

	try {
	    space.getIncludedCell(ci, Id.space.getCell("home-id:blah-foo"));
	    fail("No exception when requesting non-existing cell");
	} catch(ZZImpossibleCellException e) {
	}
        try {
            space.getIncludedCell(c1, Id.space.getCell("home-id:blah-foo"));
            fail("No exception when requesting cell from nonexisting space");
        } catch(ZZImpossibleCellException e) {
        } catch(ZZError e) {
	}
	
	assertEquals(i2, i1.s(i1));
	assertEquals(i2, i1.s(i3));
	assertEquals("one", i3.t());
	assertEquals("two", i2.t());
	assertEquals(i1, i3.h(i3));

	GIDSpace includedSpace = space.getSpaceByInclusionId(ci.id);
	assertNotNull(includedSpace);

	// Test getMSBlockCell() on inclusions.
	assertEquals(space, space.getMSBlockCell("foo", ci).space);
	Cell inclMSBlockCell = space.getMSBlockCell("foo", i2);
	assertEquals(space, inclMSBlockCell.space);
	assertEquals(includedSpace, space.getSpace(inclMSBlockCell));
	// Finished testing getMSBlockCell().

	Mediaserver.Id delta1 = space.save(ms);
	Mediaserver.Block bl = ms.getDatum(delta1);
	p("Block: "+new String(bl.getBytes()));

	space = new PermanentSpace(ms, delta1);

	String si = str(delta1.getString(), ci);

	assertTrue(space.exists(si));
	assertNotNull(space.getSpaceByInclusionId(si));
	i1 = space.getCell(str(delta1.getString(), ci) + ":home-cell");
        i2 = i1.s(i1);
	assertNotNull(i1.s(i3));
	assertNotNull(i2);
        i3 = i2.s(i1);

        assertEquals(i2, i1.s(i1));
        assertEquals(i2, i1.s(i3));
        assertEquals("one", i3.t());
        assertEquals("two", i2.t());
        assertEquals(i1, i3.h(i3));
    }

    public void testIdCells() throws Exception {
	Cell id1 = space.getCell("home-id:doesn't-exist");
	Cell id2 = space.getCell("home-id:"+s1);
	Cell id3 = Id.get(c1);
	Cell id4 = Id.get(c1c);
	assertTrue(!id1.equals(id2));
	assertEquals(id1.space, space);
	assertEquals(id2.space, space);
	assertEquals(id2, id3);
	assertTrue(Id.equals(c1, id2));
	assertTrue(Id.equals(c1, id3));
	assertTrue(Id.equals(c1c, id4));
	assertTrue(Id.equals(id2, id3));
	assertEquals(id4, id2);
	assertTrue(!id1.equals(id4));
	Dim d1 = space.getDim(id1);
	Dim d2 = space.getDim(id2);
	Dim d3 = space.getDim(id3);
	Dim d4 = space.getDim(c1c);
	Dim d5 = space.getDim(c1);
	c1.connect(c1, c2);
	assertEquals(c1.s(d1), null);
	assertEquals(c1.s(d2), c2);
	assertEquals(c1.s(d3), c2);
	assertEquals(c2, c1.s(d4));
	assertEquals(c2, c1.s(d5));

	try {
	    id1.zzclone();
	    fail("No error when creating a clone of a cell from the " +
		 "Id.space spacepart.");
	} catch(ZZError e) {
	} catch(RuntimeException e) {
	}
    }

    public void testLocalIdentities() {
	SimpleTransientSpace s1 = new SimpleTransientSpace(),
	    s2 = new SimpleTransientSpace();
	Cell c1 = s1.N(), c2 = s2.N();

	assertEquals("Not required by the definition of Space, but needed " +
		     "for this test (i.e., if this fails, the test is wrong)",
		     c1.id.substring(c1.id.indexOf("-")), 
		     c2.id.substring(c2.id.indexOf("-")));
	
	assertTrue("Id.equals must return false on two cells with the same " +
		   "ids, but from two different spaces, i.e. '-xxx' cells " +
		   "which haven't been assigned a global ID.",
		   !Id.equals(c1, c2));
    }

    public void testSingleCellTranscopy() throws Exception {
	PermanentSpace sp2 = new PermanentSpace(TestImpl.zms);
	Cell c = sp2.N();
	Mediaserver.Id delta = sp2.save(ms);
	sp2 = new PermanentSpace(ms, delta);
	c = sp2.getCell(str(delta.getString(), c));

	Cell t = space.transcopy(c);
	assertEquals(c.t(), t.t());
	assertTrue(Id.equals(c, t));
	c2.connect(c2, 1, t);

        Mediaserver.Id delta1 = space.save(ms);
        //space = new PermanentSpace(ms, delta1);
        //c1 = space.getCell(s1); c2 = space.getCell(s2); c3 = space.getCell(s3);
        //c4 = space.getCell(s4); c5 = space.getCell(s5);
    }

    public void testRankList() throws Exception {
	c1.connect(c1, c2);
	c2.connect(c1, c3);

	List rank = new RankList(c1, c1);
        assertEquals(c1, c1.h(c1));
	assertEquals(c3, c1.h(c1, 1));

	assertEquals(3, rank.size());
	assertEquals(c1, rank.get(0));
	assertEquals(c2, rank.get(1));
	assertEquals(c3, rank.get(2));
	try {
	    rank.get(3);
	    fail("no IndexOutOfBounds");
	} catch(IndexOutOfBoundsException _) {
	}

	Cell dim = Id.space.getCell("home-id:foo-bar");
        c1.connect(dim, c2);
        c2.connect(dim, c3);
        rank = new RankList(c1, dim);

        assertEquals(3, rank.size());
        assertEquals(c1, rank.get(0));
        assertEquals(c2, rank.get(1));
        assertEquals(c3, rank.get(2));
        try {
            rank.get(3);
            fail("no IndexOutOfBounds");
        } catch(IndexOutOfBoundsException _) {
        }
    }

    /** Test saving and then reloading a vstream. */
    public void testSaveVStream() throws Exception {
	p("1");
	VStreamDim vs = space.getVStreamDim();
	p("2");
	vs.insertAfterCell(c1, space.makeSpanRank("foobar"));
	p("3");
	assertEquals("foobar", c1.t());
	p("4");
	vs.insertAfterCell(c1.s((Dim)vs, 3), space.makeSpanRank("-"));
	p("5");
	assertEquals("foo-bar", c1.t());
	p("6");

        Mediaserver.Id delta1 = space.save(ms);
	p("7");
	Mediaserver.Block block = ms.getDatum(delta1);
	//try {
	    space = new PermanentSpace(ms, delta1);
	    /*} catch(Error e) {
	    String name = "error.err";
	    if(e.getMessage().indexOf("Bad file") >= 0) name = "conned.err";
            FileOutputStream fos = new FileOutputStream(name, true);
            fos.write(block.getBytes());
            fos.close();
	    }*/
	p("8");
        c1 = space.getCell(s1); c2 = space.getCell(s2); c3 = space.getCell(s3);
        c4 = space.getCell(s4); c5 = space.getCell(s5); c1c = space.getCell(s1c);
	vs = space.getVStreamDim();
	p("9");

	assertNotNull(c1.s((Dim)vs));
	assertEquals("foo-bar", c1.t());
	vs.insertAfterCell(c1.s((Dim)vs, 4), space.makeSpanRank("baz-"));
	assertEquals("foo-baz-bar", c1.t());

        delta1 = space.save(ms);
        space = new PermanentSpace(ms, delta1);
        c1 = space.getCell(s1); c2 = space.getCell(s2); c3 = space.getCell(s3);
        c4 = space.getCell(s4); c5 = space.getCell(s5); c1c = space.getCell(s1c);

	assertEquals("foo-baz-bar", c1.t());
    }



    // --- internal stuff needed for Obs testing ---

    final int NOBS = 10;
    int[] obsResp = new int[NOBS];
    Obs[] obs = new Obs[NOBS];
    {
        for(int i=0; i<NOBS; i++) obs[i] = new O(i);
    }

    class O implements Obs {
        O(int i) { this.i = i; }
        int i;
        public void chg() {
            synchronized(TestPermanentSpace.this) {
                obsResp[i]++;
            }
        }
    }


    public void assertResp(int[] ass) {
        for(int i=0; i<ass.length; i++)
            assertTrue(obsResp[i] == ass[i]);
    }
    public void assertResp(int ass) {
	assertResp(new int[] { ass });
    }
    public void assertResp(int ass1, int ass2) {
	assertResp(new int[] { ass1, ass2 });
    }
}
