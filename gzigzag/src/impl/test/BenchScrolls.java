/*   
BenchScrolls.java
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
import org.gzigzag.vob.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/** Benchmark different implementations of TextScroll.
 */

public class BenchScrolls {
public static final String rcsid = "$Id: BenchScrolls.java,v 1.1 2001/08/04 08:15:07 bfallenstein Exp $";
    static private void p(String s) { System.out.println(s); }

    static final int NCHARS = 2000;

    static private class ScrollTest {

	String s = "";
	{
	    for(int i=0; i<NCHARS; i++)
		s += "x";
	}

	TextScrollBlock _transient = new TransientTextScroll();
	TextScrollBlock permanent = new PermanentTextScroll("foo", s);

	ScrollTest() throws Exception {
	    for(int i=0; i<NCHARS; i++)
		_transient.append('x');
	}
	
	void runTests(org.gzigzag.benchmark.Bench b) {

	    b.run(new Object[] {
		"GETPERMACHAR", "Get a char array from the perm. block",
		new Runnable() { public void run() {
		    permanent.getCharArray();
		}},

                "GETTRANSCHAR", "Get a char array from the trans. block",
                new Runnable() { public void run() {
                    _transient.getCharArray();
                }},
	    });
	}
    }

    public static void main(String[] argv) throws Exception {
	org.gzigzag.benchmark.Bench b = new org.gzigzag.benchmark.Bench(argv);

        System.out.println("BENCHMARK TextScrollBlocks");
        System.out.println("==========================");
	System.out.println("");
	new ScrollTest().runTests(b);
	System.exit(0);
    }
}
