/*   
TestPartialOrder.java
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

/** Test PartialOrders.
 */

public class TestPartialOrder extends TestCase {
public static final String rcsid = "$Id: TestPartialOrder.java,v 1.9 2002/03/10 01:16:23 bfallenstein Exp $";

    static boolean dbg = false; 
    static void p(String s) { if(dbg) System.out.println(s); }

    Mediaserver ms = TestImpl.zms;
    Mediaserver.Id id1, id2, id3;
    PermanentSpace s1, s2, s3;

    PartialOrder p1;
    PartialOrder p2;

    String dim;

    String[] cells = new String[TestMerge1.NCELLS];
    Set allcells = new HashSet();

    public void setUp() throws Exception {

	TestMerge1 m = new TestMerge1("foo");
	m.setUp();
	s1 = m.s1;
	s2 = m.s2;
	s3 = m.s3;

	id1 = m.id1;
	id2 = m.id2;
	id3 = m.id3;

	dim = m.dim;
	cells = m.cells;
	allcells = m.allcells;

	p1 = new PartialOrder(allcells, s1.getDim(s1.getCell(dim)));
	p2 = new PartialOrder(allcells, s2.getDim(s2.getCell(dim)));
	
    }

    public TestPartialOrder(String name) { super(name); }

    // Partial order

    public void testPartialOrder() {
	PartialOrder p = p2;
	p("Pairs: "+p.pairs);

	Cell d = s2.getCell(dim);
	assertEquals(cells[1], s2.getCell(cells[0]).s(d, 1).id);
	assertEquals(cells[2], s2.getCell(cells[1]).s(d, 1).id);
	assertEquals(cells[32], s2.getCell(cells[2]).s(d, 1).id);

// (see image)
	assertEquals( 1, p.compare(cells[ 0], cells[32]));
	assertEquals( 1, p.compare(cells[ 0], cells[ 9]));
	assertEquals( 1, p.compare(cells[ 0], cells[10]));
	assertEquals( 1, p.compare(cells[ 3], cells[10]));
	assertEquals( 1, p.compare(cells[ 3], cells[ 9]));

	assertEquals(-1, p.compare(cells[ 9], cells[ 3]));

	assertEquals( 0, p.compare(cells[ 0], cells[11]));
	assertEquals( 1, p.compare(cells[11], cells[14]));
	assertEquals(-1, p.compare(cells[13], cells[14]));
	assertEquals(-1, p.compare(cells[15], cells[12]));
    }

    public void testPartialOrderIntersect() throws Exception {
	// Now, we want the intersection
	PartialOrder p = p1.intersection(p2);

// (see image)

// This is not in version 0
	assertEquals( 0, p.compare(cells[ 0], cells[32]));

	assertEquals( 1, p.compare(cells[ 0], cells[ 9]));
	assertEquals( 1, p.compare(cells[ 0], cells[10]));
	assertEquals( 1, p.compare(cells[ 3], cells[10]));
	assertEquals( 1, p.compare(cells[ 3], cells[ 9]));

	assertEquals(-1, p.compare(cells[ 9], cells[ 3]));

	assertEquals( 0, p.compare(cells[ 0], cells[11]));
	assertEquals( 1, p.compare(cells[11], cells[14]));
// Here, too different result 
	assertEquals(0, p.compare(cells[13], cells[14]));
	assertEquals(-1, p.compare(cells[15], cells[12]));
    }

    public void testPartialOrderViolation() throws Exception {
	PartialOrder p = p1.intersection(p2);

	// Now, change another space from s1 one small part of the total change.
	
	PermanentSpace cs = new PermanentSpace(ms, id1);

	Dim ddim = cs.getDim(dim);

	cs.getCell(cells[17]).disconnect(ddim, 1);
	cs.getCell(cells[22]).disconnect(ddim, 1);
	cs.getCell(cells[17]).connect(ddim, cs.getCell(cells[23]));

	PartialOrder.Pair pair = p.shortViolation(ddim, s1.getDim(s1.getCell(dim)));

	p("PAIR: "+pair);
	assertEquals(cells[17], pair.neg); 
	assertEquals(cells[20], pair.pos); 

    }
}






