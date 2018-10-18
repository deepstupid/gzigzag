/*   
TestTextScrollBlock.java
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

/** Tests for TextScrollBlocks (and thereby TextSpans)
 */

public abstract class TestTextScrollBlock extends TestCase {
public static final String rcsid = "$Id: TestTextScrollBlock.java,v 1.4 2001/06/09 15:31:36 tjl Exp $";

    public TestTextScrollBlock(String name) { super(name); }

    public TextScrollBlock immutable;
    public TextScrollBlock mutable;
    public TextScrollBlock mutable2; // Should be in same state (offs) as prev


    TextSpan sp1, sp2, sp3, sp4,
	    sp12, sp23, sp13,
	    all, sub23_1, sub23_2, sub2, none1, none2, none3;

    TextSpan sp2_1, sp2_2, sp2_3, sp2_4;

    public void setUp() throws Exception {
	sp1 = mutable.append('a');
	sp2 = mutable.append('b');
	sp3 = mutable.append('c');
	sp4 = mutable.append('d');

	sp2_1 = mutable2.append('a');
	sp2_2 = mutable2.append('b');
	sp2_3 = mutable2.append('c');
	sp2_4 = mutable2.append('d');

	sp12 = (TextSpan)sp1.append(sp2);
	sp23 = (TextSpan)sp2.append(sp3);
	sp13 = (TextSpan)sp1.append(sp3);
	all = (TextSpan)sp12.append(sp3);

	sub23_1 = (TextSpan)all.subSpan(1, 3);
	sub23_2 = (TextSpan)all.subSpan(1);

	sub2 = (TextSpan)all.subSpan(1, 2);
	none1 = (TextSpan)all.subSpan(1,1);
	none2 = (TextSpan)all.subSpan(1,1);
	none3 = (TextSpan)all.subSpan(2,2);
    }

    public void testImmutable() {
	// Only test if it exists..
	if(immutable == null)
	    return;

	try {
	    immutable.append(' ');
	} catch(ImmutableException e) {
	    return;
	}
	fail("No immutableexception");
    }

// XXX Change a,b,c to international characters!
// Must have multi-byte UTF8 test

    public void testMutableSpanResults() {
	assertTrue(sp1.length() == 1);
	assertTrue(sp2.length() == 1);
	assertTrue(sp3.length() == 1);
	assertTrue(sp1.getText().equals("a"));
	assertTrue(sp3.getText().equals("c"));
    }

    public void testAppendResults() {
	assertTrue(sp12 != null);
	assertTrue(sp12.getText().equals("ab"));
	assertTrue(sp12.length() == 2);

	assertTrue(sp23 != null);
	assertTrue(sp23.getText().equals("bc"));
	assertTrue(sp23.length() == 2);

	assertTrue(sp13 == null);

	assertTrue(all != null);
	assertTrue(all.getText().equals("abc"));
	assertTrue(all.length() == 3);
    }

    public void testSubspans() {
	assertTrue(sub23_1.equals(sp23));
	assertTrue(!sub23_1.equals(sp12));
	assertTrue(sub23_1.getText().equals("bc"));

	assertTrue(sub23_1.equals(sp23));
	assertTrue(!sub23_1.equals(sp12));
	assertTrue(sub23_1.getText().equals("bc"));

	assertTrue(sub2.equals(sp2));
	assertTrue(sub2.getText().equals("b"));

	assertTrue(none1.equals(none2));
	assertTrue(!none1.equals(none3));
    }

    public void testIllegalSubSpans() {
	try {
	    Span error1 = all.subSpan(2,4);
	    fail("Should raise indexoutofbounds");
	} catch(IndexOutOfBoundsException e) {
	}
	
	try {
	    Span error1 = all.subSpan(-1,2);
	    fail("Should raise indexoutofbounds");
	} catch(IndexOutOfBoundsException e) {
	}

	try {
	    Span error1 = all.subSpan(3,2);
	    fail("Should raise indexoutofbounds");
	} catch(IndexOutOfBoundsException e) {
	}
    }

    public void testBetween() {
	assertTrue(!sp1.equals(sp2_1));
	assertTrue(sp1.getText().equals(sp2_1.getText()));
	assertTrue(sp1.append(sp2_2) == null);
    }

    public void testIntersect() {
	assertTrue(sp1.intersects(sp1));
	assertTrue(!sp1.intersects(sp2));
	assertTrue(sp12.intersects(sp1));
	assertTrue(sp12.intersects(sp2));
	assertTrue(sp12.intersects(sp12));
	assertTrue(!sp12.intersects(sp3));
	assertTrue(all.intersects(sp2));
	assertTrue(!all.intersects(sp4));
	assertTrue(all.intersects(sp23.append(sp4)));
	assertTrue(sp23.append(sp4).intersects(sp12));
	assertTrue(!sp2_2.intersects(all));
	assertTrue(!sp2_4.intersects(all));
    }

    public void testHull() {
	SpanArea h13 = SpanArea.hull(sp1,sp3);
	SpanArea h31 = SpanArea.hull(sp3,sp1);
	// assert(h13.equals(all));
	// assert(h31.equals(all));
    }
    
}



