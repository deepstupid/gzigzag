/*   
PsykoTest.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
package org.gzigzag.gfx;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import org.gzigzag.client.*;

/** Run some usability tests.
 */
public class PsykoTest implements GLUpdateManager.Window {
    public static void p(String s) { System.out.println(s); }
    GZZGL.Window window;

    class Statistic {
	double min, max, sum, N;
	String name;
	Statistic(String name) { this.name = name; }
	void zero() {
	    min = 1E30;
	    max = -1E30;
	    sum = 0;
	    N = 0;
	}
	public void add(double v) {
	    sum += v;
	    if(v > max) max = v;
	    if(v < min) min = v;
	    N ++;
	    print();
	}
	public void print() {
	    if(N > 20) {
		double avg = sum / N;
		p(name+" : \t"+min+"\t"+avg+"\t"+max+"\n");
		zero();
	    }
	}
    }

    class TimeStatistic extends Statistic {
	TimeStatistic(String name) { super(name); }
	long t;
	void start() {
	    t = System.currentTimeMillis();
	}
	void end() {
	    add(System.currentTimeMillis() - t);
	}
    }

    TimeStatistic genview = new TimeStatistic("Generate view");
    TimeStatistic drawInterpFrame = new TimeStatistic("Draw interp frame");
    TimeStatistic drawFullFrame = new TimeStatistic("Draw full frame");

    GLVobScene vs1, vs2;

    Space space = new SimpleTransientSpace();
    Cell cd1 = space.N();
    Cell cd2 = space.N();
    Dim d1 = space.getDim(cd1);
    Dim d2 = space.getDim(cd2);

    Cell cursor;

    Cell[] cells = new Cell[100];

    Cell[] cellarr;
    int arrn = 5;

    long seed = 7L;
    long trials[] = { 14, 30, 52, 105, 106, 259 };
    int trial = 0;
    static long t0 = 0;
    boolean animusef = false;

    GLVanishingClient vc;
    PlainVanishing vanishing = new PlainVanishing();

    static Object glsynch = new Object();

    public boolean animUseful() { return animusef; }
    public boolean generateEndState(float lod) {
	genview.start();

	vs2 = new GLVobScene();
	vc.restart(vs2);
	vanishing.render(vc, cursor, 400, 400, new Dim[] { d1, d2 });
	vs2.makeInterpList(vs1);
	//vs2.dump();

	genview.end();

	return true;
    }
    
    /** Renders and display a frame of the view animation on screen
     */
    public void renderAnim(float fract, float lod) {
	if (vs2 != null) {
	    drawInterpFrame.start();
	    vs2.renderInterp(window, vs1, 1-fract);
	    drawInterpFrame.end();
	} else {
	    drawFullFrame.start();
	    vs1.makeFullList();
	    //vs1.dump();
	    vs1.renderFull(window);
	    drawFullFrame.end();
	}

	//window.repaint();
    }

    /** Renders and display current view state on screen
     */
    public void renderStill(float lod) {
	renderAnim(1, lod);
    }
    
    public boolean hasEndState() { return vs2 != null; }
    
    public void changeStartState(float fract) {
	// vs2.setInterpCoords(vs1, 1-fract);
	if (vs2 != null) {
	    vs1 = vs2;
	    vs2 = null;
	}
    }
    
    public void endAnimation() {
	p("endAnimation!!!!!!!!!!!!!!!!!!!!!!!!\n");
	if (vs2 != null) {
	    vs1 = vs2; vs2 = null;
	}
    }

    class EH implements GZZGL.EventHandler {
	public void repaint() {
	    // renderStill(0.5f);
	    /*
	    vs1 = new GLVobScene();
	    vc.restart(vs1);
	    vanishing.render(vc, cursor, 400, 400, new Dim[] { d1, d2 });
	    vs1.makeFullList();
	    vs1.dump();
	    vs1.renderFull(window);
	    // window.repaint();
	    try {
		Thread.sleep(100);
	    }catch(Exception e) {
	    }
	    */
	}
	public boolean keystroke(String s) {
	    Cell tmp = null;
	    // p("GOT KEYSTROKE '"+s+"'");
	    if (s.equals("Up")) {
		tmp = cursor.s(d2, -1);
	    } else if (s.equals("Down")) {
		tmp = cursor.s(d2, +1);
	    } else if (s.equals("Left")) {
		tmp = cursor.s(d1, -1);	      
	    } else if (s.equals("Right")) {
		tmp = cursor.s(d1, +1);	      
	    } else if (s.equals("Prior")) {
		vc.imgtype = (vc.imgtype + 8) % 9;
		tmp = cursor;
	    } else if (s.equals("Next")) {
		vc.imgtype = (vc.imgtype + 1) % 9;
		tmp = cursor;
	    } else if (s.equals("minus")) {
		seed--;
		makeRandomCells();
		tmp = cursor;
	    } else if (s.equals("plus")) {
		seed++;
		makeRandomCells();
		tmp = cursor;
	    } else if (s.equals("space")) {
		long t = System.currentTimeMillis();
		p("TrialTime=" + (t - t0) + "\n");
			
		if (trial == trials.length) {
		    seed = 0; 
		    for (int i = 0; i < trials.length; i++) {
			int j = (int)(Math.random() * (trials.length - i));
			long ttt = trials[i];
			trials[i] = trials[j];
			trials[j] = ttt;
		    }
		} else {
		    seed = trials[trial];
		}

		cursor = space.N();
		if (seed == 0) {
		    Cell tt = cursor;
		    tt.setText("D"); cursor = tt = tt.N(d1);
		    tt.setText("o"); tt = tt.N(d1);
		    tt.setText("n"); tt = tt.N(d1);
		    tt.setText("e"); 
		} else {
		    cursor.setText("");
		}
		
		generateEndState(.5f);
		endAnimation();
		renderStill(.5f);
		
		makeRandomCells();
		tmp = cursor;
		p("Trial " + trial + ", seed=" + seed + ", type=" + 
		  vc.imgtype + "\n");

		if (seed == 0) trial = 0; else trial++;

		try {
		    Thread.sleep(seed == 0 ? 5000 : 2500);
		}catch(Exception e) {
		}

		t0 = System.currentTimeMillis();
	    }  
	    
	    if (tmp != null) {
		animusef = cursor != tmp;
		if (animusef) {
		    cursor = tmp;
		    GLUpdateManager.chg();
		} else {
		    p("seed=" + seed + ", imgtype=" + vc.imgtype + "\n");
		    generateEndState(.5f);
		    renderStill(.5f);
		}
		return false;
	    }
	    return true;
	}
    }

    /** A helper class for sorting into a random permutation.
     */
    class Comp implements Comparable {
	int ind;
	double rand;
	Comp(int ind) { this.ind = ind; rand = Math.random(); }
	public int compareTo(Object o) {
	    Comp c = (Comp)o;
	    if(c.rand < rand) return -1;
	    if(c.rand > rand) return 1;
	    return 0;
	}
    }

    int[] randperm(int n) {
	Comp[] c = new Comp[n];
	for(int i=0; i<n; i++) c[i] = new Comp(i);
	Arrays.sort(c);
	int[] ret = new int[n];
	for(int i=0; i<n; i++)
	    ret[i] = c[i].ind;
	return ret;
    }

    /** Create a random cell configuration.
     */
    void makeRandomCells() {
	Random r = new Random(seed);

	for(int i=0; i<cells.length; i++) {
	    cells[i] = space.N();
	    cells[i].setText(""+(char)('A' + i));
	}
	
	if (false) {
	int[] h = randperm(cells.length);
	int[] v = randperm(cells.length);
	for(int i=0; i<cells.length; i++) {
	    p("CD1: "+i+" "+h[i]);
	    cells[i].connect(d1, cells[h[i]]);
	    p("CD2: "+i+" "+v[i]);
	    cells[i].connect(d2, cells[v[i]]);
	}
	} else {
	for(int i=1; i<cells.length; i++) {
	    while (true) {
		int j = (int)(r.nextDouble() * i);
		int dir = r.nextFloat() < .5 ? -1 : 1;
		Dim d = r.nextFloat() < .5 ? d1 : d2;
		if (cells[i].s(d, dir) != null) continue;
		if (cells[j].s(d, -dir) != null) continue;
		if (dir == 1)
		    cells[i].connect(d, cells[j]);
		else
		    cells[j].connect(d, cells[i]);
		break;
	    }
	}
	}

	for (int c=0; c<4 ; c++) {
	    while (true) {
		int i = (int)(r.nextDouble() * cells.length);
		int j = (int)(r.nextDouble() * cells.length);
		Dim d = r.nextFloat() < .5 ? d1 : d2;
		if (cells[i].s(d, 1) != null) continue;
		if (cells[j].s(d, -1) != null) continue;
		cells[i].connect(d, cells[j]);
		break;
	    }
	}

	cursor = cells[0];
    }

    public PsykoTest() {
	window = GZZGL.createWindow(10, 10, 800, 800, new EH());
	vc = new GLVanishingClient();
	vanishing.shrink[0] = 0.7f;
	vanishing.shrink[1] = 0.7f;
	//vanishing.xgapmult = 1.8f;
	//vanishing.ygapmult = 1.8f;

	vanishing.raster.depth = 10;
	makeRandomCells();

	for (int i = 0; i < trials.length; i++) {
	    int j = (int)(Math.random() * (trials.length - i));
	    long t = trials[i];
	    trials[i] = trials[j];
	    trials[j] = t;
	}

	// Render initial scene
	vs1 = new GLVobScene();
	vc.restart(vs1);
	vanishing.render(vc, cursor, 400, 400, new Dim[] { d1, d2 });
	    vs1.makeFullList();
	    //vs1.dump();
	    vs1.renderFull(window);
    }



    public static void main(String[] argv) throws Exception {
	//PsykoTest p = new PsykoTest();
	//p.cursor = p.cursor.s(p.d1, 1);
	//p.generateEndState(100, 0.5f);
	//p.renderAnim(.2f, 0.5f);
	//p.renderAnim(.4f, 0.5f);
	//p.renderAnim(.6f, 0.5f);
	//p.renderAnim(.8f, 0.5f);
	//p.renderAnim(1f, 0.5f);

	GLUpdateManager.addWindow(new PsykoTest());
	while(true) {
	    p("Going to synch for eventloop");
	    GZZGL.eventLoop(GLUpdateManager.getTicker());
	    p("Event loop returned!");
	}
    }

}
