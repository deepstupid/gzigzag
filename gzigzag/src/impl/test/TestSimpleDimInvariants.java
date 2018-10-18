/*   
TestSimpleDimInvariants.java
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
import org.gzigzag.impl.*;
import junit.framework.*;

/** 
 */

public class TestSimpleDimInvariants extends TestAnyDim {
public static final String rcsid = "$Id: TestSimpleDimInvariants.java,v 1.1 2001/08/17 21:02:01 bfallenstein Exp $";

    public TestSimpleDimInvariants(String name) { super(name); }

    Space space = new SimpleTransientSpace();

    public void setUp() {
	d = new SimpleDim(space, new DummyObsTrigger());
	c = new Cell[20];
	for(int i=0; i<c.length; i++)
	    c[i] = space.N();
	try{
	    for(int i=0; i<c.length-1; i++) {
		d.connect(c[i], c[i+1]);
	    }
	    d.disconnect(c[5], 1);
	    d.connect(c[5], c[0]);
	    d.disconnect(c[12], -1);
	    d.disconnect(c[12], 1);
	} catch(ZZAlreadyConnectedException e) {
	    throw new ZZError("SETP!");
	}
    }

    public void tearDown() {
	d = null;
	for(int i=0; i<c.length; i++)
	    c[i] = null;
    }
}



