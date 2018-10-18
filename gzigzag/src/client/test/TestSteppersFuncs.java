/*   
TestSteppersFuncs.java
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
 * Written by Tuomas Lukka and Antti-Juhani Kaijanaho
 */


package org.gzigzag.client;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import org.gzigzag.util.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import java.io.*;
import junit.framework.*;
import java.awt.*;
import java.awt.image.*;

/** Test the new stepper stuff.
 */

public class TestSteppersFuncs extends TestCase {
    public static boolean dbg = true;
    final static void p(String s) { if(dbg) System.out.println(s); }

    public TestSteppersFuncs(String name) { super(name);} 

    SimpleTransientSpace triv = new SimpleTransientSpace();
    Cell thome = triv.getHomeCell();
    Cell tdim1 = triv.N(triv.getHomeCell());
    Cell tdim2 = triv.N(triv.getHomeCell());
    Dim rd1 = triv.getDim(tdim1);
    Dim rd2 = triv.getDim(tdim2);

    Stepper s = new DirectStepper(thome, null);
    StepperDim d1 = s.getDim(tdim1.id);
    StepperDim d2 = s.getDim(tdim2.id);

    int count = 0;

    class HeadcellTextFunction implements Function {
	public Object apply(Stepper s0) {
	    Stepper s = (Stepper)s0.cloneStepper();
	    s.h(d1);
	    count++; 
	    return s.t();
	}
    }

    public void setUp() {
	thome.setText("A");
	Cell c = thome.N(rd1);
	c.setText("B");
	c = c.N(rd1);
	c.setText("C");
	c = c.N(rd2);
	c.setText("D");
	c.connect(rd1, thome);
    }

    public void testSteppers1() {
	assertEquals("A", s.t());
	assertTrue(s.s(d1, -1));
	assertEquals("D", s.t());
	assertTrue(s.s(d2, -1));
	assertEquals("C", s.t());
	assertTrue(s.s(d1, -1));
	assertEquals("B", s.t());
	assertTrue(s.s(d1, -1));
	assertEquals("A", s.t());
	assertTrue(s.s(d1, 1));
	assertEquals("B", s.t());
	assertTrue( ! s.s(d2, 1) ); // No connection there

	Stepper s2 = s.cloneStepper();
	assertEquals("B", s.t());
	assertEquals("B", s2.t());

	assertTrue(s2.h(d1, 1));
	assertEquals("C", s2.t());
	assertTrue(s2.h(d1, -1));
	assertEquals("D", s2.t());

    }

    void trigObs() {
	triv.getObsTrigger().callQueued();
    }

    public void testFunc1() {
	HeadcellTextFunction f = new HeadcellTextFunction();
	CachedFunction cf = new CachedFunction(f);

	assertEquals("D", f.apply(s));
	assertEquals("D", f.apply(s));
	assertEquals(2, count); count = 0;

	assertEquals("D", cf.apply(s));
	assertEquals("D", cf.apply(s));
	trigObs();
	assertEquals("D", cf.apply(s));
	assertEquals(1, count); count = 0;

	thome.disconnect(rd1, -1);
	trigObs();
	assertEquals("A", cf.apply(s));
	trigObs();
	assertEquals("A", cf.apply(s));
	assertEquals(1, count); count = 0;

    }

}
