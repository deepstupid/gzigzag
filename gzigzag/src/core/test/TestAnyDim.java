/*   
TestAnyDim.java
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

/** Tests that ANY dimension MUST pass in order to be a real ZZ dimension.
 * Mostly related to the fundamental invariances.
 * Unlike TestSimpleDim, this class expects the given cells
 * to already be in some structure. This class does not use any
 * calls that change the space.
 * <p>
 * XXX TODO: Test that Obses work by allowing subclasses to do different
 * changes and testing (like testStepNeg) that the correct changes
 * have been informed about.
 */

public abstract class TestAnyDim extends TestCase {
public static final String rcsid = "$Id: TestAnyDim.java,v 1.3 2001/06/09 15:31:36 tjl Exp $";

    public TestAnyDim(String name) { super(name); }
    
    public Dim d;
    public Cell[] c;

    /** Test that stepping and then stepping back gets us back to 
     * where we were.
     */
    public void testStepNeg() {
	for(int i=0; i<c.length; i++) {
	    assertTrue(d.s(c[i], 0) == c[i]);
	    for(int n = -10; n <= 10; n++) {
		Cell end = d.s(c[i], n);
		if(end == null) continue;
		assertTrue(d.s(end, -n) == c[i]);
	    }
	}
    }

    /** Test that assumptions about ranks, ringranks and headcells work.
     */
    public void testHeads() {
	for(int i=0; i<c.length; i++) {
	    Cell head = d.h(c[i], -1);
	    if(d.s(head, -1) != null) {
		// Ringrank. check that the other head is the same.
		assertTrue(d.h(c[i], 1) == head);
		assertTrue(d.s(head, 1) != null);
	    } else {
		// Not ringrank. Ensure other head also works.
		Cell ohead = d.h(c[i], 1);
		assertTrue(ohead == d.h(head, 1));
		assertTrue(head == d.h(ohead, -1));
		assertTrue(d.s(ohead, 1) == null);
	    }
	}
    }
}



