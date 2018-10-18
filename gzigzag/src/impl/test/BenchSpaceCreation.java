/*   
BenchSpaceCreation.java
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

public class BenchSpaceCreation {
public static final String rcsid = "$Id: BenchSpaceCreation.java,v 1.7 2002/03/10 01:16:23 bfallenstein Exp $";
    static private void p(String s) { System.out.println(s); }

    static private class CreationTest {

	PermanentSpace sp;
	Mediaserver ms;
	Mediaserver.Id id;

	CreationTest() throws IOException {
	    ms = TestImpl.zms;
	    sp = new PermanentSpace(ms);
	    sp.N(); // change the space, to ensure it gets saved
	    id = sp.save(ms);
	    sp = new PermanentSpace(ms);
	}

	Mediaserver.Id id2 = new Mediaserver.Id("0000000008000000E83CA694D500045EA1CB3B2366CAE2E3F75C2426035C373D362CD429D93062");
	
	void runTests(org.gzigzag.benchmark.Bench b) {
	    b.run(new Object[] {
		"CREATESPACE", "Create a new PermanentSpace()",
		new Runnable() { public void run() {
		    try {
			new PermanentSpace(ms);
		    } catch(IOException e) {
                        throw new ZZError(e+": "+e.getMessage());
		    }
		}},

                "CREATESAVE", "Create a new PermanentSpace() and save it",
                new Runnable() { public void run() {
                    try {
                        sp = new PermanentSpace(ms);
			sp.save(ms);
                    } catch(IOException e) {
                        throw new ZZError(e+": "+e.getMessage());
                    }
                }},

                "LOAD", "Load an existing PermanentSpace()",
                new Runnable() { public void run() {
                    try {
                        new PermanentSpace(ms, id);
                    } catch(IOException e) {
                        throw new ZZError(e+": "+e.getMessage());
                    }
                }},

		"LOADLATER", "Load a newer existing PermanentSpace()",
                new Runnable() { public void run() {
                    try {
                        new PermanentSpace(ms, id2);
                    } catch(IOException e) {
                        throw new ZZError(e+": "+e.getMessage());
                    }
                }},

		"LATERTONULL", "Send later space to null handler",
		new Runnable() { 
			GZZ1Handler hdl; byte[] data;

			{
			    try {
				hdl = new GZZ1NullHandler();
				data = ms.getDatum(id2).getBytes();
			    } catch(Throwable t) {
				t.printStackTrace();
			    }
			}

			public void run() {
			    InputStream is = new ByteArrayInputStream(data);
			    Reader r = new InputStreamReader(is);
			    try {
				GZZ1Reader.read(r, hdl);
			    } catch(IOException e) {
				throw new Error("IOException: "+e);
			    }
			}
		    },
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
