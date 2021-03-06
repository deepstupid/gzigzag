/*   
Bench.java
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
import java.lang.ref.*;

/** Test speeds of various simple java operations,
 * provide a generic simple speed-test framework.
 * The parameters that can be passed are
 * <dl>
 * <dt> -min <i>s</i>
 *   <dd> The minimum number of seconds that each benchmark should physically run.
 *   	In order to avoid noise, each benchmark's number of times to run is successively
 *		increased until the whole benchmark runs at least this many seconds.
 *		E.g. -min 2.3  = run at least 2.3 seconds
 *		
 * </dl>
 */

public class Bench {
public static final String rcsid = "$Id: Bench.java,v 1.12 2001/09/15 18:27:18 bfallenstein Exp $";
    static private void p(String s) { System.out.println(s); }

    static String formatTime(long nanos) {
	if(nanos < 1000)
	    // e.g. "450 nanosec."
	    return nanos + " nanosec.";
	
	else if(nanos < 20000)
	    // e.g. "12.4 microsec."
	    return ((float)(nanos/100))/10 + " microsec.";
	
	else if(nanos < 1000000)
	    // e.g. "730 microsec."
	    return (nanos/1000) + " microsec.";
	
	else if(nanos < 20000000)
	    // e.g. "7.8 millisec."
	    return ((float)(nanos/100000))/10 + " millisec.";
	
	else if(nanos < 1000000000)
	    // e.g. "120 millisec."
	    return (nanos/1000000) + " millisec.";
	
	else
	    // e.g. "1.3 sec."
	    return ((float)(nanos/100000000))/10 + " sec.";
    }

    /** The names of tests to actually run, as strings.
     */
    Set testsToRun;

    int minms = 1500;

    /** Parse the arguments for the benchmark,
     * described above.
     */
    public Bench(String[] argv) { 
	ArrayList l = new ArrayList();
	for(int i=0; i<argv.length; i++)
	    l.add(argv[i]);

	p("Bench: not yet parsed params: "+l+" "+minms);

	for(Iterator i = l.iterator(); i.hasNext();) {
	    String s = (String)i.next();
	    if(s.equals("-min")) {
		i.remove();
		s = (String)i.next();
		i.remove();
		double d = Double.valueOf(s).doubleValue();
		minms = (int)(1000 * d);
	    } else 
		break;
	}

	p("Bench: parsed params: "+l+" "+minms);

	if(l.size() > 0) {
	    this.testsToRun = new HashSet(); 
	    testsToRun.addAll(l);
	}
    }

    public void runTest(String s, Runnable r, String level) {
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
	} while(diff < minms);
	p(level+s+": "+nt+" iter, "+diff+" ms, per iter: "+
		    formatTime((long)(1000000 * diff / (float)nt)));
    }

    /** Run the given tests, time them and print out the results.
     * Each test should be relatively fast to execute.
     * @param tests Triplets of String, String, Runnable:
     * 		first string is a unique ID for the test,
     *		second string a human-readable explanation (without newlines)
     *		and the Runnable is the test itself.
     */
    public void run(Object[] tests) {
	p("Running tests for minimum "+minms+" ms");
	run(tests, "", "");
    }

    public void run(Object[] tests, String prefix, String level) {
	for(int i=0; i<tests.length; i+= 3) {
	    String name = prefix + tests[i];

	    if(tests[i+2] instanceof Runnable) {
		if(testsToRun != null && !testsToRun.contains(name))
		    continue;
		p(level+"Running "+name+": "+tests[i+1]);
		Runnable r = (Runnable)tests[i+2];
		runTest(name, r, level);
	    } else {
		p(level+"Running "+name+": "+tests[i+1]);
		Object[] arr = (Object[])tests[i+2];
		run(arr, name+"_", level + "   ");
	    }
	}
    }

    static class Dummy {
    }

    public static void main(String[] argv) {
	Bench b = new Bench(argv);
	b.run(new Object[] {
	    "FLOATMULTSUM", "Multiply two member floats and place result in third.",
	        new Runnable() {
		    float f1=1.1f, f2=1.05f, f3=0.0f;
		    public void run() { f3 = f1*f2; }
		},
	    "DOUBLEMULTSUM", "Multiply two member doubles and place result in third.",
	        new Runnable() {
		    double f1=1.1f, f2=1.05f, f3=0.0f;
		    public void run() { f3 = f1*f2; }
		},
	    "PUTHASH", "Put a string into a hash (same place)",
		new Runnable() { HashMap h = new HashMap();
			    public void run() { h.put("foo", "foo");  } },
	    "PUTGETCASTHASH", 
		"Put, get and cast a string into a hash (same place)",
		new Runnable() { HashMap h = new HashMap(); String s;
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
		new Runnable() { Dummy o = new Dummy(); Dummy b;
			    public void run() { b = (Dummy)o;  } },
	    "CLSCAST", "Class-cast a member, store to another",
		new Runnable() { Object o = new Dummy(); Dummy b;
			    public void run() { b = (Dummy)o;  } },
            "SOFTREF", "Deref a soft ref",
                new Runnable() { Dummy o = new Dummy();
                    SoftReference r = new SoftReference(o);
                    Object b;
                    public void run() { b = r.get(); } },
	    "INTERN", "Intern a string (the same each time)",
	    new Runnable() { 
		    // s is a non-interned string (literals are interned):
		    String s = "abcdefghijklmnopqrstuvwxyz1".substring(0, 26);
		    String t;
		    public void run() { t = s.intern(); }
		},
	    "STRTOBYTES", "Convert a cell id String to byte array",
	    new Runnable() {
		    String s = "0000000008000000E82FE24A4B00044874D25A3BD6337DB7C6A39C7755CC71E24CBE47CBA45472-5";
		    byte[] b;
		    public void run() { b = s.getBytes(); }
		},
	    "BYTESTOSTR", "Convert a cell id byte array to a String",
            new Runnable() {
                    byte[] b = "0000000008000000E82FE24A4B00044874D25A3BD6337DB7C6A39C7755CC71E24CBE47CBA45472-5".getBytes();
		    String s;
                    public void run() { s = new String(b); }
                },
	});
    }
}
