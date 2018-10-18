/*   
BenchSpace.java
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
import org.gzigzag.client.Client;
import org.gzigzag.client.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.vob.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/** Benchmark common space operations.
 */

public class BenchSpace {
public static final String rcsid = "$Id: BenchSpace.java,v 1.7 2002/03/18 08:34:07 tjl Exp $";
    static private void p(String s) { System.out.println(s); }

    static private class CreationTest {

	PermanentSpace space;
	Mediaserver ms;
	Mediaserver.Id id;

	View view = new VobVanishingClient();
	Cell dimc;
	String dimc_id;
	Cell wc;

	Dim dim1;

	CreationTest() throws IOException {
	    ms = TestImpl.zms;
	    space = new PermanentSpace(ms, 
	      new Mediaserver.Id("0000000008000000E7D09CA0D30004BFA0D323AF067FABCC6255AC80E538F54F6C6A5D76A25B9F"));

	    Cell clientCell = space.getHomeCell().s(Client.d1);
	    Cell scr = Params.getParam(clientCell, Client.c_screen);
	    dim1 = space.getDim(Client.d1);

	    wc = Params.getParam(scr, Client.c_window);

	    dimc = Params.getParam(wc, Client.c_dims);
	    dimc_id = dimc.id;

	    dimc.N(dim1);

	    // dimc = dimc.s(Client.d1);

	}
	
	void runTests(org.gzigzag.benchmark.Bench b) {

	    b.run(new Object[] {
		"STEP_DIMDIM_D", "Step with a Dim object, calling Dim directly",
		new Runnable() { public void run() {
		    Cell foo = dim1.s(dimc,1);
		}},
		"STEP_DIMDIM_D_S", "Step with a Dim object, calling Dim directly, but cell as string",
		new Runnable() { public void run() {
		    Cell foo = dim1.s(dimc,1);
		}},
		"STEP_DIMDIM", "Step with a Dim object",
		new Runnable() { public void run() {
		    Cell foo = dimc.s(dim1);
		}},
		"STEP_IDDIM", "Step with the dim specified by an Id cell",
		new Runnable() { public void run() {
		    Cell foo = dimc.s(Client.d1);
		}},
		"CELL_HASHCODE", "Get cell hashcode",
		new Runnable() { int hash;
		public void run() {
			hash = dimc.hashCode();
		}},

	    });
	}
    }

    public static void main(String[] argv) throws IOException {
	org.gzigzag.benchmark.Bench b = new org.gzigzag.benchmark.Bench(argv);

        System.out.println("BENCHMARK Space");
        System.out.println("=====================");
	System.out.println("");
	new CreationTest().runTests(b);
	System.exit(0);
    }
}
