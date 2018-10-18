/*   
TestLoopDetector.java
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

package org.gzigzag.util;
import org.gzigzag.*;
import junit.framework.*;

public class TestLoopDetector extends TestCase {
public static final String rcsid = "$Id: TestLoopDetector.java,v 1.2 2001/06/09 15:31:36 tjl Exp $";

    public TestLoopDetector(String name) { super(name); }

    LoopDetector l;

    static class EqThing {
	int i;
	EqThing(int i) { this.i = i; }
	public boolean equals(Object o2) {
	    if(o2 == this) return true;
	    if(!(o2 instanceof EqThing)) return false;
	    return i == ((EqThing)o2).i;
	}
    }

    Object[][] objs = new Object[50][50];

    public void setUp() {
	l = new LoopDetector();
	for(int i=0; i<objs.length; i++) {
	    for(int j=0; j<objs[i].length; j++) {
		objs[i][j] = new EqThing(j);
	    }
	}
    }

    public void testDetectSame() {
	for(int i=0; i<objs[0].length; i++) {
	    boolean caught = false;
	    try {
		for(int loop = 0; loop < 3; loop++) {
		    for(int j=0; j<=i; j++) {
			l.detect(objs[0][j]);
		    }
		}
	    } catch(InfiniteLoopException e) {
		caught = true;
	    }
	    assertTrue(caught);
	}
    }
}
