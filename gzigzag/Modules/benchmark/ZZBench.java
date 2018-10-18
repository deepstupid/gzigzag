/*   
ZZBench.java
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

package org.gzigzag.benchmark;
import org.gzigzag.*;
import java.util.*;

/** Test speeds of various simple java operations,
 * provide a generic simple speed-test framework.
 */

public class ZZBench {
public static final String rcsid = "$Id: ZZBench.java,v 1.3 2001/04/01 11:38:49 tjl Exp $";
    static private void p(String s) { System.out.println(s); }

    public void runTest(String s, Runnable r) {
	// Start it up so that the JIT is ready
	for(int i=0; i<32; i++) r.run();
	// run more and more until we get over 3 secs of time.
	int nt = 20; 
	long diff = 0;
	do {
	    long t0 = System.currentTimeMillis();
	    for(int i = nt; i >= 0; i--)
		r.run();
	    long t1 = System.currentTimeMillis();
	    nt *= 2;
	    diff = t1 - t0;
	} while(diff < 1500);
	p(s+": "+nt+" iter, "+diff+" ms, per iter: "+
		    (long)(1000000 * diff / (float)nt) + " ns");
    }

    /** Run the given tests, time them and print out the results.
     * Each test should be relatively fast to execute.
     * @param tests Triplets of String, String, Runnable:
     * 		first string is a unique ID for the test,
     *		second string a human-readable explanation (without newlines)
     *		and the Runnable is the test itself.
     */
    public void run(Object[] tests) {
	for(int i=0; i<tests.length; i+= 3) {
	    p("Running "+tests[i]+": "+tests[i+1]);
	    Runnable r = (Runnable)tests[i+2];
	    runTest((String)tests[i], r);
	}
    }

    public static void main(String[] argv) {
	ZZBench b = new ZZBench();
	b.run(new Object[] {
	    "PUTHASH", "Put a string into a hash (same place)",
		new Runnable() { Hashtable h = new Hashtable();
			    public void run() { h.put("foo", "foo");  } },
	    "PUTGETCASTHASH", 
		"Put, get and cast a string into a hash (same place)",
		new Runnable() { Hashtable h = new Hashtable(); String s;
			    public void run() { h.put("foo", "foo"); 
					        s = (String)h.get("foo"); } },
	    "CREATE", "Create an object",
		new Runnable() { Object o;
			    public void run() { o = new Object();  } },
	    "CREATE4I", "Create an object with four ints inside",
		new Runnable() { Object o;
			    public void run() { o = new Object() {
					    int i1, i2, i3, i4;
					};  } },
	    "EMPTY", "empty function", 
		new Runnable() { public void run() { } },
	    "INCRINT", "Increment a member integer",
		new Runnable() { int i; public void run() { i++; } },
	    "ASSIGN", "assign a member to another",
		new Runnable() { ZZBench o = new ZZBench(); ZZBench b;
			    public void run() { b = (ZZBench)o;  } },
	    "CLSCAST", "Class-cast a member, store to another",
		new Runnable() { Object o = new ZZBench(); ZZBench b;
			    public void run() { b = (ZZBench)o;  } },
	});
    }
}
