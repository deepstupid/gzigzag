/*   
BenchLBChain.java
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

package org.gzigzag.vob;
import java.util.*;
import java.awt.*;

/** benchmark line-breakable chain.
 */

public class BenchLBChain {
public static final String rcsid = "$Id: BenchLBChain.java,v 1.4 2001/10/25 12:31:01 bfallenstein Exp $";
    static private void p(String s) { System.out.println(s); }

    static private class TestVob extends Vob implements HBox {
	TestVob(Object key) { super(key); }
	public void render(java.awt.Graphics g, 
				int x, int y, int w, int h,
				boolean boxDrawn,
				RenderInfo info
				) {
	}

	public int getWidth(int scale) { return 20; }
	public int getHeight(int scale) { return 20; }
	public int getDepth(int scale) { return 20; }
	public Vob getVob(int scale) { return this; }
	public void setPrev(HBox b) { }
	public void setPosition(int depth, int x, int y, int w, int h) { }
    }


    static private int nvobs = 500;
    static private TestVob[] vobs;
    static private Object[] keys;

    static private int[] lines = new int[] {
	100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 
	100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 
	100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 
	100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 
	100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 
	100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 
	100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 
    };
    static private int[] scales = new int[] { 1000 };

    static private int[] into = new int[] {
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    };

    static private void add100(LinebreakableChain lbc) {
	for(int i=0; i<100; i++) {
	    lbc.addBox(new TestVob(null));
	    lbc.addGlue(4, 2, 2);
	}
    }

    static private void putvobs(VobScene set, boolean useDifferentDepths) {
	if(!useDifferentDepths)
		for(int i=0; i<nvobs; i++)
		    set.put(vobs[i],1,0,0,0,0); 
	else
		for(int i=0; i<nvobs; i++)
		    set.put(vobs[i],i%60,0,0,0,0);
    }

    static Linebreaker lb = new SimpleLinebreaker();

    static void runTests(org.gzigzag.benchmark.Bench b) {
	b.run(new Object[] {
	    "ADD100WORDS", "Create chain and add 100 words to the chain.",
		new Runnable() { public void run() {
		    LinebreakableChain lbc = new LinebreakableChain();
		    add100(lbc);
		}},
	    "ADD100WORDSBREAK", "Create chain and add 100 words to the chain. linebraek. ",
		new Runnable() { public void run() {
		    LinebreakableChain lbc = new LinebreakableChain();
		    add100(lbc);
		    lb.breakLines(lbc, lines, scales, 20, 0);
		}},
	});
    }

    public static void main(String[] argv) {
	try {
	    nvobs = Integer.parseInt(argv[0]);
	} catch(Exception e) {
	}
	
        vobs = new TestVob[nvobs];
        keys = new Object[nvobs];

	for(int i=0; i<nvobs; i++) {
	    Object key = new Object();
	    vobs[i] = new TestVob(key);
	    keys[i] = key;
	}
			
	org.gzigzag.benchmark.Bench b = new org.gzigzag.benchmark.Bench(argv);

	System.out.println("BENCHMARK LinkedListVobScene");
	System.out.println("======================");
	System.out.println("");
	runTests(b);
    }
}
