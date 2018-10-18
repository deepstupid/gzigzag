/*   
BenchVobScenes.java
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

/** benchmark simple and trivial vobset implementations
 */

public class BenchVobScenes {
public static final String rcsid = "$Id: BenchVobScenes.java,v 1.4 2001/07/30 16:37:59 tjl Exp $";
    static private void p(String s) { System.out.println(s); }

    static private class TestVob extends Vob {
	TestVob(Object key) { super(key); }
	public void render(java.awt.Graphics g, 
				int x, int y, int w, int h,
				boolean boxDrawn,
				RenderInfo info
				) {
	}
    }

    static private int nvobs = 500;
    static private TestVob[] vobs;
    static private Object[] keys;

    static private void putvobs(VobScene set, boolean useDifferentDepths) {
	if(!useDifferentDepths)
		for(int i=0; i<nvobs; i++)
		    set.put(vobs[i],1,0,0,0,0); 
	else
		for(int i=0; i<nvobs; i++)
		    set.put(vobs[i],i%60,0,0,0,0);
    }

    static abstract private class VobSceneTest {
	abstract VobScene getset();
	
	void runTests(org.gzigzag.benchmark.Bench b) {
	    b.run(new Object[] {
		"ADDVOBS", "Add "+nvobs+" vobs to the vobset",
		    new Runnable() { public void run() {
			VobScene set = getset();
			putvobs(set, false);
		    }},
		"ADDVOBDEPTHS", "Add "+nvobs+" at 60 depths",
		    new Runnable() { public void run() {
			VobScene set = getset();
			putvobs(set, true);
		    }},
		"RENDER", "Render "+nvobs+" vobs",
		    new Runnable() {
			VobScene set = getset(); { putvobs(set, true); }
			public void run() {
			set.render(null, Color.black, Color.white, null, 0);
		    }},
		"ADDRENDER100", "Add "+nvobs+" and render 100 times",
		    new Runnable() {
			VobScene set = getset(); { putvobs(set, true); }
			public void run() {
			for(int i=0; i<100; i++)
			    set.render(null, Color.black, Color.white, null, 0);
		    }},
		"LOOKUP", "Look up by key, using get()",
		    new Runnable() { 
			VobScene set = getset(); { putvobs(set, false); }
			public void run() {
			for(int i=0; i<nvobs; i++)
			    set.get(keys[i]);
		    }},
/**
		"LOOKUPALL", "Look up first 20 vobs by key, using getAll()",
		    new Runnable() { public void run() {
			for(int i=0; i<20; i++)
			    set.getAll(keys[i]);
		    }},
**/
		"INTERP", "Add "+nvobs+" to two vobsets and render interp.",
		    new Runnable() { public void run() {
			VobScene set1 = getset(), set2 = getset();
			putvobs(set1, false); putvobs(set2, true);
			set1.render(null, Color.black, Color.white, set2, 0.5f);
		    }},
		"INTERP100", "Add "+nvobs+" to two vobsets and render interp. 100 times",
		    new Runnable() { public void run() {
			VobScene set1 = getset(), set2 = getset();
			putvobs(set1, false); putvobs(set2, true);
			for(int i=0; i<100; i++)
			    set1.render(null, Color.black, Color.white, set2, 0.5f);
		    }},
	    });
	}
    }

    private static VobSceneTest simpletest = new VobSceneTest() {
	VobScene getset() { return new SimpleVobScene(); }
    };
    private static VobSceneTest trivialtest = new VobSceneTest() {
	VobScene getset() { return new TrivialVobScene(new Dimension(400, 400)); }
    };
    private static VobSceneTest linkedtest = new VobSceneTest() {
	VobScene getset() { return new LinkedListVobScene(new Dimension(400, 400)); }
    };

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
	linkedtest.runTests(b);

	System.out.println("");
	System.out.println("");
	System.out.println("BENCHMARK SimpleVobScene");
	System.out.println("======================");
	System.out.println("");
	simpletest.runTests(b);
	
	System.out.println("");
	System.out.println("");
	System.out.println("BENCHMARK TrivialVobScene");
	System.out.println("======================");
	System.out.println("");
	trivialtest.runTests(b);
    }
}
