/*   
TestMerge1.java
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
import junit.framework.*;

import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;

import java.util.*;

/** Test merging.
 * Contains functionality for testing SimpleTransientDim.diff,
 * SimpleDimChangeList.
 * First tests use
 * the sequence in
 * <img src="../../../../doc-images/TestMerge1-1.jpg">
 * <img src="../../../../doc-images/TestMerge1-2.jpg">
 */

public class TestMerge1 extends TestCase {
public static final String rcsid = "$Id: TestMerge1.java,v 1.36 2002/03/10 11:46:02 bfallenstein Exp $";

    public static boolean dbg = false;
    private static void pa(String s) { System.out.println(s); }
    private static void p(String s) { if(dbg) pa(s); }

    public TestMerge1(String name) { super(name); }

    Mediaserver ms = TestImpl.zms;
    Mediaserver.Id id1, id2, id3;
    PermanentSpace s1, s2, s3;

    PartialOrder p1;
    PartialOrder p2;

    String[] cells = new String[NCELLS];
    String dim;

    String[] cs2 = new String[NCELLS2];

    Set allcells = new HashSet();

    static public int NCELLS2 = 18;
    static public int NCELLS1 = 33;
    static public int NCELLS=NCELLS1 + NCELLS2;

    public void setUp() throws Exception {

	PermanentSpace s = new PermanentSpace(ms);
	Cell dimcell = s.N(s.getHomeCell());
	Dim ddim = s.getDim(dimcell);
	dim = dimcell.id.substring(dimcell.id.indexOf("-"));

	Cell[] ccells = new Cell[TestMerge1.NCELLS];
	for(int i=0; i<ccells.length; i++) {
	    ccells[i] = s.N(s.getHomeCell());
	    if(i < NCELLS1) 
		ccells[i].setText("M: "+i);
	    else
		ccells[i].setText("WS: "+(i-NCELLS1));
	    cells[i] = ccells[i].id.substring(ccells[i].id.indexOf("-"));
	}
	Mediaserver.Id id0 = s.save(ms);
	s = new PermanentSpace(ms, id0);
	String xid = id0.getString();
	for(int i=0; i<cells.length; i++) {
	    cells[i] = xid + cells[i];
	    allcells.add(cells[i]);
	}
	dim = xid + dim;
	ddim = s.getDim(s.getCell(dim));

	for(int i=0; i<NCELLS2; i++)
	    cs2[i] = cells[i + NCELLS - NCELLS2];


	// now, we want to make these globals...

	TestMerge1.setupCells1(s, cells, ddim);
	id1 = s.save(ms);
	    s = new PermanentSpace(ms, id1);
	    ddim = s.getDim(s.getCell(dim));
	TestMerge1.setupCells2(s, cells, ddim);
	id2 = s.save(ms);


	    s = new PermanentSpace(ms, id1);
	    ddim = s.getDim(s.getCell(dim));
	TestMerge1.setupCells3(s, cells, ddim);
	// XXX
	id3 = s.save(ms);

	s1 = new PermanentSpace(ms, id1);
	s2 = new PermanentSpace(ms, id2);
	s3 = new PermanentSpace(ms, id3);

    }


    static Cell[] sc(Space s, String[] cells) {
	Cell[] c = new Cell[cells.length];
	for(int i=0; i<c.length; i++)
	    c[i] = s.getCell(cells[i]);
	return c;
    }

// These are used from outside as well.
    static public void setupCells1(Space s, String[] scells, Dim dim) throws ZZAlreadyConnectedException {
	Cell[] cells = sc(s, scells);
	// Two ranks of 16, 2 singletons
	for(int i=0; i<cells.length-1; i++) {
	    if(i != 15 && i < 31)
		cells[i].connect(dim, cells[i+1]);
	}

	cells[21].disconnect(dim, 1);
	cells[24].disconnect(dim, 1);
	cells[29].disconnect(dim, 1);
	cells[30].disconnect(dim, 1);

	// whole-space test
	Cell[] cs = new Cell[NCELLS2];
	for(int i=0; i<NCELLS2; i++)
	    cs[i] = cells[i+NCELLS1];

	for(int i=1; i<17; i++)
	    cs[i].connect(dim, cs[i+1]);
	cs[6].disconnect(dim, 1);
	cs[12].disconnect(dim, 1);
	cs[13].disconnect(dim, 1);
	cs[14].disconnect(dim, 1);
	cs[15].disconnect(dim, 1);
	cs[16].disconnect(dim, 1);
    }

    static public void setupCells2(Space s, String[] scells, Dim dim) throws ZZAlreadyConnectedException {
	Cell[] cells = sc(s, scells);
	cells[2].insert(dim, 1, cells[32]);
	cells[9].excise(dim);
	cells[6].insert(dim, -1, cells[9]);
	cells[4].excise(dim);
	cells[5].excise(dim);
	cells[6].insert(dim, 1, cells[5]);
	cells[6].insert(dim, 1, cells[4]);
	cells[10].disconnect(dim, 1);
	cells[14].hop(dim, -1);

	cells[17].disconnect(dim, 1);
	cells[22].disconnect(dim, 1);
	cells[17].connect(dim, cells[23]);
	cells[24].connect(dim, cells[25]);
	cells[27].disconnect(dim, -1);
	cells[27].disconnect(dim, 1);
	cells[26].connect(dim, cells[30]);
	cells[30].connect(dim, cells[28]);
	cells[19].disconnect(dim, 1);
	cells[29].connect(dim, cells[20]);

	// whole-space test: official changes
	Cell[] cs = new Cell[NCELLS2];
	for(int i=0; i<NCELLS2; i++)
	    cs[i] = cells[i+NCELLS1];

	cs[8].excise(dim);
	cs[9].excise(dim);

	cs[6].insert(dim, 1, cs[8]);
	cs[8].insert(dim, 1, cs[9]);
	cs[3].disconnect(dim, 1);
	cs[9].connect(dim, 1, cs[1]);
    }

    static public void setupCells3(Space s, String[] scells, Dim dim) throws ZZAlreadyConnectedException {
	Cell[] cells = sc(s, scells);
	// whole-space test: proposed changes
	Cell[] cs = new Cell[NCELLS2];
	for(int i=0; i<NCELLS2; i++)
	    cs[i] = cells[i+NCELLS1];
	cs[13].excise(dim);
	cs[14].excise(dim);
	cs[17].excise(dim);

	cs[2].excise(dim);
	cs[1].insert(dim, 1, cs[13]);
	cs[4].insert(dim, 1, cs[14]);
	cs[7].insert(dim, 1, cs[17]);
    }


    public void testOrder() {

	String i0 = cells[0];

	Dim dim0 = s2.getDim(dim);

	Cell i = s2.getCell(i0);

	assertEquals(" 0", cells[ 0], i.id);
	assertEquals(" 1", cells[ 1], (i = dim0.s(i)).id);
	assertEquals(" 2", cells[ 2], (i = dim0.s(i)).id);
	assertEquals("32", cells[32], (i = dim0.s(i)).id);
	assertEquals(" 3", cells[ 3], (i = dim0.s(i)).id);
	assertEquals(" 9", cells[ 9], (i = dim0.s(i)).id);
	assertEquals(" 6", cells[ 6], (i = dim0.s(i)).id);
	assertEquals(" 4", cells[ 4], (i = dim0.s(i)).id);
	assertEquals(" 5", cells[ 5], (i = dim0.s(i)).id);
	assertEquals(" 7", cells[ 7], (i = dim0.s(i)).id);
	assertEquals(" 8", cells[ 8], (i = dim0.s(i)).id);
	assertEquals("10", cells[10], (i = dim0.s(i)).id);
	assertEquals("end1", null, (i = dim0.s(i)));

	assertEquals("11", cells[11], (i = s2.getCell(cells[11])).id);
	assertEquals("12", cells[12], (i = dim0.s(i)).id);
	assertEquals("14", cells[14], (i = dim0.s(i)).id);
	assertEquals("13", cells[13], (i = dim0.s(i)).id);
	assertEquals("15", cells[15], (i = dim0.s(i)).id);
	assertEquals("end", null, (i = dim0.s(i)));

    }

/*
    Cell cell(int ind) {
	if(ind < 0) return null;
	return cells[ind];
    }

    void assertHas(SimpleDimChangeList l, int i1, int i2, int i3, int i4) {
	if(i1 >= 0) {
	    for(int i=0; i<l.quarks.length; i+=4) {
		if(cells[i1].equals(l.quarks[i])) {
		    assertEquals("prevpos "+i2, cell(i2), l.quarks[i+1]);
		    assertEquals("prevneg "+i3, cell(i3), l.quarks[i+2]);
		    assertEquals("pos "+i4, cell(i4), l.quarks[i+3]);
		    // Zero it out so we can check that there is nothing
		    // about this cell.
		    l.quarks[i] = l.quarks[i+1] = l.quarks[i+2] = l.quarks[i+3] = 
			    cells[33];
		    // Don't allow dups
		    assertNone(l, i1, 1, 1);
		    return;
		}
	    }
	} else {
	    for(int i=0; i<l.quarks.length; i+=4) {
		if(cells[i4].equals(l.quarks[i+3])) {
		    assertEquals("neg "+i1, cell(i1), l.quarks[i]);
		    assertEquals("prevpos "+i2, cell(i2), l.quarks[i+1]);
		    assertEquals("prevneg "+i3, cell(i3), l.quarks[i+2]);
		    // Zero it out so we can check that there is nothing
		    // about this cell.
		    cells[i] = cells[i+1] = cells[i+2] = cells[i+3] = cells[33];
		    assertNone(l, i4, -1, 1);
		    return;
		}
	    }
	}
	fail("Not found: "+l+" "+i1+" "+i2+" "+i3+" "+i4);
    }
*/

    /** Assert that there is no connection in the change list for given index.
     * The parameters can be used to select one, the other or both of
     * things to check.
     * @param dir If gt or eq to 0, this is negend. lt or eq to 0, posend.
     * @param time If gt or eq to 0, check current. If lt or eq 0, prev
     */
/*
    void assertNone(SimpleDimChangeList l, int ci, int dir, int time) {
	for(int i=0; i<l.quarks.length; i+=4) {
	    if(time >= 0 && dir >= 0 && cells[ci].equals(l.quarks[i]))
		fail("Pos connection "+i+" "+cells[ci]);
	    if(time >= 0 && dir <= 0 && cells[ci].equals(l.quarks[i+3]))
		fail("Neg connection "+i+" "+cells[ci]);

	    if(time <= 0 && dir >= 0 && cells[ci].equals(l.quarks[i+2]))
		fail("prevPos connection "+i+" "+cells[ci]);
	    if(time <= 0 && dir <= 0 && cells[ci].equals(l.quarks[i+1]))
		fail("prevNeg connection "+i+" "+cells[ci]);
	}
    }

    public void testDiff() {
	SimpleDimChangeList l = changeList0;
	assertNone(l, 33, 0, 0);
	assertHas(l, 2, 3, -1, 32);
	assertHas(l, 32, -1, 2, 3);
	assertNone(l, 1, 0, 0);
	assertNone(l, 2, -1, 0);
    }
*/

    public void testChanges1() {
	Set s = Merge1.makeChanges(s1, s2);

	Merge1.Quark q = Merge1.findQuark(
		new Merge1.Quark(null, null, null, null, cells[20]), s);
	assertNotNull(q);
	p("Quark found: "+q);
	assertEquals(cells[29], q.neg);
	assertEquals(null, q.prevpos);
	assertEquals(cells[19], q.prevneg);
	assertEquals(cells[20], q.pos);

	assertTrue(q.isConsistent(s1));
	assertTrue(!q.isConsistent(s2));
	
	q = Merge1.findQuark(
		new Merge1.Quark(null, null, null, null, cells[18]), s);
	p("Quark2 found: "+q);
	assertEquals(null, q.neg);
	assertEquals(null, q.prevpos);
	assertEquals(cells[17], q.prevneg);

	assertTrue(q.isConsistent(s1));
	assertTrue(!q.isConsistent(s2));
    }

    public void testChangesAtomical() {
	Set s = Merge1.makeChanges(s1, s2);

	s = Merge1.atoms(s);
    /*
	Merge1.OneDimChanges c0 = new Merge1.OneDimChanges(null, changeList0);
	Merge1.OneDimChanges c1 = (Merge1.OneDimChanges)c0.clone();
	c1.makeAtomical();

	// p(""+ c1);

	Merge1.Change[] c = c1.getChanges();
	assertEquals("Len", 9, c.length); // X???X?XXX
    */
    }


    public void testMolecules() throws Exception {
	Set c0 = Merge1.makeChanges(s1, s2);
	Set c1 = Merge1.atoms(c0);
	Set c2 = Merge1.makeMoleculesSucc(c1, s1, ms, id1, null, s2);

	Space test = new PermanentSpace(ms, id1);

	for(Iterator i = c2.iterator(); i.hasNext();) {
	    Merge1.Change ch = (Merge1.Change)i.next();
	    assertTrue(ch.isConsistent(s1));
	    assertTrue(ch.isConsistent(test));
	}

	Merge1.Change change17 = Merge1.getChange(c2, cells[17], 1);

	Space as17 = Merge1.appliedSpace(ms, id1, null, null, change17);
	Dim sdim17 = as17.getDim(dim);

	assertEquals(cells[23], sdim17.s(as17.getCell(cells[17]), 1).id);
	assertEquals(null, sdim17.s(as17.getCell(cells[18]), -1));
	assertEquals(cells[25], sdim17.s(as17.getCell(cells[24]), 1).id);
	assertEquals(cells[27], sdim17.s(as17.getCell(cells[26]), 1).id);
	assertEquals(cells[28], sdim17.s(as17.getCell(cells[27]), 1).id);
	assertEquals(cells[20], sdim17.s(as17.getCell(cells[29]), 1).id);

	assertEquals(cells[29], sdim17.s(as17.getCell(cells[20]), -1).id);

	Merge1.Change change30 = Merge1.getChange(c2, cells[30], -1);
	Space as30 = Merge1.appliedSpace(ms, id1, null, null, change30);
	Dim sdim30 = as30.getDim(dim);

	assertEquals(cells[26], sdim30.s(as30.getCell(cells[30]), -1).id);
	assertEquals(cells[28], sdim30.s(as30.getCell(cells[30]), 1).id);
	assertEquals(cells[30], sdim30.s(as30.getCell(cells[26]), 1).id);

	assertEquals(null, sdim30.s(as30.getCell(cells[25]), -1));
	assertEquals(null, sdim30.s(as30.getCell(cells[24]), 1));
    }

    public void testMerge() throws Exception {
	Merge1.Result res = Merge1.merge(ms, id2, id3, id1);
	Set conf = res.conflicts;
	Space applyTo = res.newSpace;
	p("CONFLICTS: "+conf);
	assertEquals(1, conf.size());

	Dim d = applyTo.getDim(dim);

	assertEquals(null, d.s(applyTo.getCell(cs2[4]), -1));
	assertEquals(cs2[14], d.s(applyTo.getCell(cs2[4]), 1).id);
	assertEquals(cs2[5], d.s(applyTo.getCell(cs2[14]), 1).id);
	assertEquals(cs2[6], d.s(applyTo.getCell(cs2[5]), 1).id);
	assertEquals(cs2[8], d.s(applyTo.getCell(cs2[6]), 1).id);
	assertEquals(cs2[9], d.s(applyTo.getCell(cs2[8]), 1).id);
	assertEquals(cs2[1], d.s(applyTo.getCell(cs2[9]), 1).id);
	assertEquals(cs2[13], d.s(applyTo.getCell(cs2[1]), 1).id);
	assertEquals(cs2[3], d.s(applyTo.getCell(cs2[13]), 1).id);
	assertEquals(null, d.s(applyTo.getCell(cs2[3]), 1));

	assertEquals(null, d.s(applyTo.getCell(cs2[7]), -1));
	assertEquals(cs2[10], d.s(applyTo.getCell(cs2[7]), 1).id);
	assertEquals(cs2[11], d.s(applyTo.getCell(cs2[10]), 1).id);
	assertEquals(cs2[12], d.s(applyTo.getCell(cs2[11]), 1).id);
	assertEquals(null, d.s(applyTo.getCell(cs2[12]), 1));
    }

    public void testRealMerge() throws Exception {
    p("===== Start testRealMerge");
	Mediaserver.Id idOff = 
	    new Mediaserver.Id(
	    "0000000008000000E830E3BF2C0004A58091D33E4C146B3E7EF6910C9B2FAD6B009134A5E459E4");
	Mediaserver.Id idProp = 
	    new Mediaserver.Id(
	    "0000000008000000E830522CF200044905910F8819429E74DD8E048DA2DDB69BD278D6FA6B6CF0");
	// Space off = new PermanentSpace(ms, idOff);
	// Space prop = new PermanentSpace(ms, idProp);
	Mediaserver.Id idAnc = Synch.findCommonAncestor(ms, idOff, idProp);

	Merge1.Result res = Merge1.merge(ms, idOff, idProp, idAnc);
	Set conf = res.conflicts;
	Space applyTo = res.newSpace;

    p("===== End testRealMerge");
	
    }
}





