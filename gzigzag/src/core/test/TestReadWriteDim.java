/*   
TestReadWriteDim.java
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

package org.gzigzag;
import junit.framework.*;
import java.util.*;

/** Test simple read-write dimensions for the usual operations.
 * This is meant to be subclassed so that different implementations
 * can be tested.
 * <p>
 * The setup method should create a new, fresh dimension and 10 distinct
 * cells with no connections between them.
 */

public abstract class TestReadWriteDim extends TestCase {
public static final String rcsid = "$Id: TestReadWriteDim.java,v 1.2 2001/08/25 18:28:44 bfallenstein Exp $";

    public TestReadWriteDim(String name) { super(name); }

    public Dim d;
    public Cell dcell;
    public Cell[] c = new Cell[10];

    public void testDimCell() {
	assertEquals(dcell.space.getDim(dcell), d);
    }

    public void testNullCell() {
	assertEquals(null, dcell.space.getCell(null));
    }

    public void assertOrder(int from, int dir, int[] cells) {
	for (int i = 0; i < cells.length; i++) {
	    assertTrue(d.s(c[from], dir*(i+1)) == (cells[i] >= 0 ? c[cells[i]] : 
							    null));
	}
    }

    public void order(int last) {
	for(int i=0; i<last; i++)
	    d.connect(c[i], c[i+1]);
    }

    public List iterList(Iterator i, int max) {
	List l = new ArrayList();
	while(i.hasNext()) {
	    l.add(i.next());
	    max--;
	    if(max < 0) throw new Error("Too many");
	}
	return l;
    }

    public void testIterator1() throws ZZException{
	order(5);
	List l = iterList(d.iterator(c[2]), 100);
	assertEquals("Start from 2", 4, l.size()); // 2, 3, 4, 5
	assertEquals("First", c[2], l.get(0));
    }

    public void testIterator2() throws ZZException{
	order(5);
	List l = iterList(d.iterator(c[5]), 100);
	assertEquals("Start from 2", 1, l.size()); // 2, 3, 4, 5
	assertEquals("Only", c[5], l.get(0));
    }



    /** Test that the dim we start with is empty.
     */
    public void testEmpty() {
	for(int i=0; i<c.length; i++)  {
	    assertTrue(d.s(c[i], 0, null) == c[i]);
	    assertTrue(d.s(c[i], 1, null) == null);
	    assertTrue(d.s(c[i], -1, null) == null);
	    assertTrue(d.s(c[i], 2, null) == null);
	    assertTrue(d.s(c[i], -2, null) == null);
	}
    }

    /* Test that connecting works.
     */
    public void testConnect() throws ZZException {
	d.connect(c[0], c[1]);
	assertTrue(d.s(c[0], -1) == null);
	assertTrue(d.s(c[0], 1) == c[1]);
	assertTrue(d.s(c[0], 2) == null);

	assertTrue(d.s(c[1], 1) == null);
	assertTrue(d.s(c[1], -1) == c[0]);
	assertTrue(d.s(c[1], -2) == null);

	d.connect(c[2], -1, c[1]);

	assertTrue(d.s(c[0], -1) == null);
	assertTrue(d.s(c[0], 1) == c[1]);
	assertTrue(d.s(c[0], 2) == c[2]);
	assertTrue(d.s(c[0], 3) == null);

	assertTrue(d.s(c[2], 1) == null);
	assertTrue(d.s(c[2], -1) == c[1]);
	assertTrue(d.s(c[2], -2) == c[0]);
	assertTrue(d.s(c[2], -3) == null);

    }

    /** Test that the alreadyconnectedexception is thrown
     * AND that everything does still work afterwards.
     */
    public void testAlreadyConnectedException() throws ZZException {
	d.connect(c[0], c[1]);
	try {
	    d.connect(c[0], c[2]);
	    fail("Should except");
	} catch(ZZAlreadyConnectedException e) {
	}

	try {
	    d.connect(c[2], c[1]);
	    fail("Should except");
	} catch(ZZAlreadyConnectedException e) {
	}

	assertTrue(d.s(c[2],1) == null);
	assertTrue(d.s(c[2],-1) == null);
	assertTrue(d.s(c[0],1) == c[1]);
	assertTrue(d.s(c[1],-1) == c[0]);
    }

    public void testDisconnectNeg() throws ZZException {
	order(4);

	d.disconnect(c[2], -1);
	assertTrue(d.s(c[0],1) == c[1]);
	assertTrue(d.s(c[0],2) == null);
	assertTrue(d.s(c[4],-1) == c[3]);
	assertTrue(d.s(c[4],-2) == c[2]);
	assertTrue(d.s(c[4],-3) == null);
    }

    public void testDisconnectPos() throws ZZException {
	order(4);

	d.disconnect(c[1], 1);
	assertTrue(d.s(c[0],1) == c[1]);
	assertTrue(d.s(c[0],2) == null);
	assertTrue(d.s(c[4],-1) == c[3]);
	assertTrue(d.s(c[4],-2) == c[2]);
	assertTrue(d.s(c[4],-3) == null);
    }

    public void testInsert() throws Exception {
	d.insert(c[0], 1, c[3]);
	d.insert(c[0], 1, c[1]);
	d.insert(c[3], -1, c[2]);
	d.insert(c[3], 1, c[4]);

	assertTrue(d.s(c[0], 1) == c[1]);
	assertTrue(d.s(c[0], 2) == c[2]);
	assertTrue(d.s(c[0], 3) == c[3]);
	assertTrue(d.s(c[0], 4) == c[4]);
    }

    public void testHead() throws ZZException {
	order(4);

	assertTrue(d.h(c[2], -1) == c[0]);
	assertTrue(d.h(c[0], -1) == c[0]);
	assertTrue(d.h(c[2], 1) == c[4]);
	assertTrue(d.h(c[0], 1) == c[4]);
	assertTrue(d.h(c[4], 1) == c[4]);

	assertTrue(c[2].h(dcell, -1) == c[0]);
	assertTrue(c[2].h(dcell, 1) == c[4]);
	assertTrue(c[2].h(dcell, -1, true) == c[0]);
	assertTrue(c[0].h(dcell, -1, true) == null);
	assertTrue(c[0].h(dcell, -1, false) == c[0]);
	assertTrue(c[0].h(dcell, 1, true) == c[4]);
	assertTrue(c[4].h(dcell, 1, true) == null);
    }

    public void testLoop() throws ZZException {
	d.connect(c[0], c[1]);
	d.connect(c[1], c[2]);
	d.connect(c[2], c[3]);
	d.connect(c[3], c[4]);
	d.connect(c[4], c[0]);

	assertTrue(d.s(c[2], 1) == c[3]);
	assertTrue(d.s(c[2], 2) == c[4]);
	assertTrue(d.s(c[2], 3) == c[0]);
	assertTrue(d.s(c[2], 4) == c[1]);
	assertTrue(d.s(c[2], 5) == c[2]);
	assertTrue(d.s(c[2], 6) == c[3]);
	assertTrue(d.s(c[2], 7) == c[4]);
	assertTrue(d.s(c[2], 8) == c[0]);

	assertTrue(d.s(c[2], -1) == c[1]);
	assertTrue(d.s(c[2], -2) == c[0]);
	assertTrue(d.s(c[2], -3) == c[4]);
	assertTrue(d.s(c[2], -4) == c[3]);
	assertTrue(d.s(c[2], -5) == c[2]);
	assertTrue(d.s(c[2], -6) == c[1]);
	assertTrue(d.s(c[2], -7) == c[0]);
	assertTrue(d.s(c[2], -8) == c[4]);

	// Important invariant: head always same in same rank.
	Cell head = d.h(c[2], -1);
	assertTrue(d.h(c[2], 1) == head);
	assertTrue(d.h(c[3], 1) == head);
	assertTrue(d.h(c[3], -1) == head);
	assertTrue(d.h(c[0], -1) == head);
	assertTrue(d.h(c[0], 1) == head);
    }

    public void testHop1() throws ZZException {
	order(5);
	d.hop(c[1], 1);
	assertOrder(0, 1, new int[] {2, 1, 3, 4, 5, -1});
    }

    public void testHopm1() throws ZZException {
	order(5);
	d.hop(c[2], -1);
	assertOrder(0, 1, new int[] {2, 1, 3, 4, 5, -1});
    }

    public void testHop2() throws ZZException {
	order(5);
	d.hop(c[1], 2);
	assertOrder(0, 1, new int[] {2, 3, 1, 4, 5, -1});
    }

    /** Test what hopping out of bounds.
     * XXX IS THIS REALLY THE CORRECT SEMANTICS? LEAVING UNTOUCHED.
     */
    public void testHopout() throws ZZException {
	order(5);
	d.hop(c[3], 10);
	assertOrder(0, 1, new int[] {1, 2, 3, 4, 5, -1});
    }

    // Test observation
    // XXX This may be moved to its own test class later.

    final int NOBS = 10;
    int[] obsResp = new int[NOBS];
    Obs[] obs = new Obs[NOBS];
    {
	for(int i=0; i<NOBS; i++) obs[i] = new O(i);
    }
    // Must be set by subclass so we can run the queue.
    public ObsTrigger obstrig;
    
    class O implements Obs {
	O(int i) { this.i = i; }
	int i;
	public void chg() {
	    synchronized(TestReadWriteDim.this) {
		obsResp[i]++;
	    }
	}
    }

    public void assertResp(int[] ass) {
	assertResp("no message", ass);
    }
    public void assertResp(String msg, int[] ass) {
	for(int i=0; i<ass.length; i++) 
	    assertTrue(msg, obsResp[i] == ass[i]);
    }

    public void testObsBreak() throws ZZException {
	order(5);
	Cell c3 = d.s(c[2], 1, obs[0]);
	Cell c5 = d.h(c[0], 1, obs[2]);
	Cell c6 = d.h(c[5], -1, obs[3]);
	assertResp(new int[] {0,0,0,0});
	d.disconnect(c[2], 1);
	assertResp(new int[] {0,0,0,0});

	String que = ""+obstrig;
	obstrig.callQueued();
	assertResp(""+obsResp[0]+" "+que, new int[] {1,0,1,1});

	d.connect(c[2], 1, c[3]);
	assertResp(new int[] {1,0,1,1});
	c3 = d.s(c[2], 1, obs[1]);
	c5 = d.h(c[0], 1, obs[2]);
	c6 = d.h(c[5], -1, obs[3]);
	d.disconnect(c[3], -1);
	assertResp(new int[] {1,0,1,1});
	obstrig.callQueued();
	assertResp(new int[] {1,1,2,2});

    }

    public void testObsConnect() throws ZZException {
	order(5);
	d.disconnect(c[2], 1);
	d.h(c[0], 1, obs[0]);
	d.h(c[5], -1, obs[1]);
	d.s(c[2], 1, obs[2]);
	d.s(c[3], -1, obs[3]);
	d.s(c[1], 3, obs[4]);

	d.connect(c[2], c[3]);
	assertResp(new int[] {0,0,0,0,0,0});
	obstrig.callQueued();
	assertResp(new int[] {1,1,1,1,1,0});
    }

    public void testWrongSpace() throws ZZException {
	order(5);
	try {
	    d.connect(c[5], new Cell(null, "test-20"));
	    fail("No WrongSpaceError generated");
	} catch(ZZWrongSpaceError e) {
	}

    }

    public void testInsertRank() {
	order(9);
	d.disconnect(c[4], 1);
	d.insertRank(c[2], 1, c[5]);
	assertEquals(c[5], c[2].s(d));
	assertEquals(c[3], c[9].s(d));
    }

 

}

