/*   
TestSimpleDim.java
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

/** Test the SimpleDim class using the TestReadWriteDim tests.
 * @see TestReadWriteDim
 */

public class TestSimpleDim extends TestReadWriteDim {
public static final String rcsid = "$Id: TestSimpleDim.java,v 1.1 2001/08/17 21:02:01 bfallenstein Exp $";

    public TestSimpleDim(String name) { super(name); }

    Space space = new SimpleTransientSpace();

    public void setUp() {
	dcell = space.N(space.getHomeCell());
	d = space.getDim(dcell);
	obstrig = ((SimpleDim)d).trigger;
	for(int i=0; i<c.length; i++) {
	    // c[i] = space.N();
	    c[i] = new Cell(space, "test"+i+"-X");
	    c[i].setText(""+i);
	}
    }

    public void tearDown() {
	d = null;
	for(int i=0; i<c.length; i++)
	    c[i] = null;
    }
}
