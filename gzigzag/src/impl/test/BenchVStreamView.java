/*   
BenchVStreamView.java
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

/** Benchmark viewing a vstream.
 *  The action that is benchmarked is slurping a vstream into a
 *  LinebreakableChain, breaking lines, creating a vob scene and putting 
 *  the lines into it, and
 *  finally rendering that vobscene once in an actual window.
 */

public class BenchVStreamView {
public static final String rcsid = "$Id: BenchVStreamView.java,v 1.8 2002/03/10 01:16:23 bfallenstein Exp $";
    static private void p(String s) { System.out.println(s); }

    static private class CreationTest {

	PermanentSpace space;
	Mediaserver ms;
	Mediaserver.Id id;

	View view = new VStreamView();
	View billow = new VStreamView(true);
	Cell vstream, wc;

	CreationTest() throws IOException {
	    ms = TestImpl.zms;
	    space = new PermanentSpace(ms);
	    VStreamDim vsd = space.getVStreamDim();

	    vstream = space.N();
	    wc = space.N();
	    Cursor.set(wc, vstream);

	    for(int i=0; i<15; i++) {
	    vsd.insertAfterCell(vstream, space.makeSpanRank(
"    You may use and distribute under the terms of either the GNU Lesser"));
            vsd.insertAfterCell(vstream, space.makeSpanRank(
"    General Public License, either version 2 of the license or,"));
            vsd.insertAfterCell(vstream, space.makeSpanRank(
"    at your choice, any later version. Alternatively, you may use and"));
            vsd.insertAfterCell(vstream, space.makeSpanRank(
"    distribute under the terms of the XPL."));
	    vsd.insertAfterCell(vstream, space.makeSpanRank("\n"));
            vsd.insertAfterCell(vstream, space.makeSpanRank(
"    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of"));
            vsd.insertAfterCell(vstream, space.makeSpanRank(
"    the licenses."));
            vsd.insertAfterCell(vstream, space.makeSpanRank("\n"));
            vsd.insertAfterCell(vstream, space.makeSpanRank(
"    This software is distributed in the hope that it will be useful,"));
            vsd.insertAfterCell(vstream, space.makeSpanRank(
"    but WITHOUT ANY WARRANTY; without even the implied warranty of"));
            vsd.insertAfterCell(vstream, space.makeSpanRank(
"    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README"));
            vsd.insertAfterCell(vstream, space.makeSpanRank(
"    file for more details."));
	    }

	    view.render(plainScene, wc); 
	    view.render(billowedScene, wc); 

	}

	Frame f = new Frame();
	{
	    f.setBounds(0, 0, 400, 400);
	    f.setVisible(true);
	}

	Color fg = Color.black, bg = Color.white;

	VobScene plainScene = new TrivialVobScene(new Dimension(400, 400));
	VobScene billowedScene = new TrivialVobScene(new Dimension(400, 400));
	
	void runTests(org.gzigzag.benchmark.Bench b) {

	    b.run(new Object[] {
		"PLAIN", "A plain VStreamView",
		new Runnable() { public void run() {
		    VobScene sc = new TrivialVobScene(new 
					    Dimension(400, 400));
		    view.render(sc, wc);
		    sc.render(f.getGraphics(), fg, bg, null, 0.0f);
		}},

		"BILLOWED", "A billowed VStreamView",
                new Runnable() { public void run() {
                    VobScene sc = new TrivialVobScene(new
			Dimension(400, 400));
                    billow.render(sc, wc);
                    sc.render(f.getGraphics(), fg, bg, null, 0.0f);
                }},

		"BUILDPLAIN", "Build a plain VStreamView",
                new Runnable() { public void run() {
                    VobScene sc = new TrivialVobScene(new
                        Dimension(400, 400));
		    view.render(sc, wc);
		}},

                "BUILDBILLOWED", "Build a billowed VStreamView",
                new Runnable() { public void run() {
                    VobScene sc = new TrivialVobScene(new
                        Dimension(400, 400));
                    billow.render(sc, wc);
                }},

		"RENDERPLAIN", "Render a plain VStreamView",
                new Runnable() { public void run() {
                    plainScene.render(f.getGraphics(), fg, bg, null, 0.0f);
                }},

                "RENDERBILLOWED", "Render a billowed VStreamView",
                new Runnable() { public void run() {
                    billowedScene.render(f.getGraphics(), fg, bg, null, 0.0f);
                }},
	    });
	}
    }

    public static void main(String[] argv) throws IOException {
	org.gzigzag.benchmark.Bench b = new org.gzigzag.benchmark.Bench(argv);

        System.out.println("BENCHMARK VStreamView");
        System.out.println("=====================");
	System.out.println("");
	new CreationTest().runTests(b);
	System.exit(0);
    }
}
