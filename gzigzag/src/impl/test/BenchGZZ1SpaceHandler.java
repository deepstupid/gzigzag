/*   
BenchGZZ1SpaceHandler.java
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
 * Written by Benja Fallenstein
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;
import java.util.*;
import java.io.*;

/** Something is slow in merge testing. Benchmark to see how fast PermanentSpace
 *  creation, saving, and loading is.
 */

public class BenchGZZ1SpaceHandler {
public static final String rcsid = "$Id: BenchGZZ1SpaceHandler.java,v 1.3 2002/03/10 01:16:23 bfallenstein Exp $";
    static private void p(String s) { System.out.println(s); }

    static private class CreationTest {

	Mediaserver ms = TestImpl.zms;
	PermanentSpace sp;
	GZZ1SpaceHandler hdl = new GZZ1SpaceHandler(sp, ms);

        byte[] c1 = "-blah".getBytes(), c2 = "-foo".getBytes();
	GZZ1Handler.SimpleDim sdim = hdl.dimSection(c1);

	CreationTest() throws IOException {
	    sp = new PermanentSpace(ms);
	    hdl.setId(new Mediaserver.Id("0000000000000E0012345678"));
	    GZZ1Handler.NewCells nc = hdl.newCellsSection();
	    nc.newCell(c1); nc.newCell(c2); nc.close();
	}

	void runTests(org.gzigzag.benchmark.Bench b) {
	    b.run(new Object[] {
		"MAKEBREAK", "Make a connection and then break it",
		new Runnable() { public void run() {
		    sdim.connect(c1, c2);
		    sdim.disconnect(c1, c2);
		}},
	    });
	}
    }

    public static void main(String[] argv) throws IOException {
	org.gzigzag.benchmark.Bench b = new org.gzigzag.benchmark.Bench(argv);

        System.out.println("BENCHMARK PermanentSpace");
        System.out.println("=====================");
	System.out.println("");
	new CreationTest().runTests(b);
    }
}
