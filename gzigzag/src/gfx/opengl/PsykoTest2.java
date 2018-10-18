/*   
PsykoTest2.java
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
public class PsykoTest2 implements GLUpdateManager.Window {
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

    Cell save = null;
    Cell results = null;
    Cell info = space.N();
    Cell info2 = space.N();
    boolean trialrunning = false;
    String correct;

    Cell cursor;

    Cell message = space.N();
    Cell done;
    Cell[] cells = new Cell[18];

    int arrn = 8;
    int maxn = 8;
    Cell[] cellarr = new Cell[maxn * maxn];
    int winw = 1400;
    int winh = 1050;

    String subject = "";

    long seed = 7L;
    int numtrials = 24;
    long[] trials;
    int trial = 0;
    static long t0 = 0;
    boolean animusef = false;

    GLVanishingClient vc;
    PlainVanishing vanishing = new PlainVanishing();

    static Object glsynch = new Object();

    void makeTrials(int num) {
	Random r = new Random(/*seed*/);

	trials = new long[num];
	for (int i = 0; i < trials.length; i++) {
	    trials[i] = (r.nextLong() & -4) + (long)(i % 3);
	}
    }

    void shuffleTrials() {
      	for (int i = 0; i < trials.length; i++) {
	    int j = (int)(Math.random() * (trials.length - i));
	    long t = trials[i];
	    trials[i] = trials[j];
	    trials[j] = t;
	}
    }

    void renderScene(GLVobScene vs) {
	vc.restart(vs);
	if (cursor != cellarr[0]) {
	    vanishing.render(vc, cursor, winw/2, winh/2, 
			     new Dim[] { d1, d2 });

	    if (cursor != message) {
		info.setText("" + vc.imgtype);
		info2.setText(subject);
		vc.place(info, null, 1, 0, 0, 50, 50, 5, 0.f);
		vc.place(info2, null, 1, 60, 0, 110, 50, 5, 0.f);
	    }

	} else  {
	    double m = (double)winh / maxn;

	    for (int i = 0; i < arrn; i++) {
		for (int j = 0; j < arrn; j++) {
		    int d = 0;//(int)(i < arrn/2 ? -m*.1 : +m*.1);
		    vc.place(cellarr[i + arrn*j], null, 1,
			     (int)(m * (i - 0.5 * arrn + .2)) + winw/2 + d, 
			     (int)(m * (j - 0.5 * arrn + .2)) + winh/2, 
			     (int)(m * (i - 0.5 * arrn + .8)) + winw/2 + d, 
			     (int)(m * (j - 0.5 * arrn + .8)) + winh/2, 5, 0);
		}
	    }
	    
	    for (int i = 0; i < arrn; i++) {
		for (int j = 0; j < arrn; j++) {
		    Cell c1 = cellarr[i + arrn*j], c2;
		    c2 = cellarr[i + arrn*j].s(d1);
		    if (c2 != null) vc.connect(c1, c2, 1, 0);
		    c2 = cellarr[i + arrn*j].s(d2);
		    if (c2 != null) vc.connect(c1, c2, 0, 1);
		}
	    }
	}
    }

    public boolean animUseful() { return animusef; }
    public boolean generateEndState(float lod) {
	genview.start();
	
	vs2 = new GLVobScene();
	renderScene(vs2);
	vs2.makeInterpList(vs1);

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
	if (vs2 != null) {
	    vs1 = vs2; vs2 = null;
	}
    }

    Cell makeTrial() {
	if (trial >= trials.length) {
	    return null;
	} 
	seed = trials[trial];

	int[] sizes = { 4,6,8 };
	arrn = sizes[(int)(seed & 3)];
	
	results = results.N(d1);
	results.setText("" + trial);
	results = results.N(d2);
	results.setText("" + vc.imgtype);
	results = results.N(d2);
	results.setText("" + arrn*arrn);
	
	Random r = new Random(seed*1013L);

	
	//p("" + seed + "!!!!!!!!" + (int)(seed & 3) + "\n" );
	if (cellarr[0] == null) {
	    for (int i = 0; i < maxn * maxn; i++)
		cellarr[i] = space.N();
	}

	// Remove all connections
	for(int i=0; i<arrn; i++)
	    for(int j=0; j<arrn; j++)
		{
		    cellarr[i+arrn*j].excise(d1);
		    cellarr[i+arrn*j].excise(d2);
		}

	// Reconnect to a grid
	for(int i=0; i<arrn; i++)
	    for(int j=0; j<arrn; j++)
		{
		    if (i > 0) cellarr[i-1+arrn*j].connect(d1, cellarr[i+arrn*j]);
		    if (j > 0) cellarr[i+arrn*(j-1)].connect(d2, cellarr[i+arrn*j]);
		}
	
	//Dim d = (seed & 1) == 0? d1 : d2;
	
	while (true) {
	    int i = (int)(r.nextDouble() * arrn);
	    int j = (int)(r.nextDouble() * arrn);
	    Dim d = r.nextBoolean() ? d1 : d2;
	    
	    if ((i == 0 || i == arrn - 1) && d == d1) continue;
	    if ((j == 0 || j == arrn - 1) && d == d2) continue;

	    p("excising (" + i + "," + j + ") from " + (d == d1 ? "d1" : "d2") + "\n");
	    correct = i < arrn/2 ? "L" : "R"; //(d==d1 ? "H":"V");

	    cellarr[i+arrn*j].excise(d);
	    results = results.N(d2);
	    results.setText("("+i+","+j+")" + correct);
	    break;
	}
      
	p("Trial " + trial + ", seed=" + seed + ", type=" + 
	  vc.imgtype + "\n");
	
	return cellarr[0];
    }

    void showMessage(Cell message, int time) {
	cursor = message;

	generateEndState(.5f);
	endAnimation();
	renderStill(.5f);
	vanishing.initmul = 1.6f;
	
	try {
	    Thread.sleep(time);
	}catch(Exception e) {
	}
    }

    Cell nextTrial() {
	Cell trial = makeTrial();
	if (trial == null) {
	    seed = 0; 
	    vc.clearbg = vc.clearbg1;
	    showMessage(done, 2000);

	    cursor = results;
	    return results;
	}

	trialrunning = true;

	System.gc();

	vc.clearbg = vc.clearbg0;

	message.setText("");
	vanishing.initmul = .2f;
	showMessage(message, 1500);
	vanishing.initmul = 1.6f;

	cursor = trial;
	return cursor;
    }

    Cell endTrial(String answer) {
	if (!trialrunning) return null;

	long t = System.currentTimeMillis();
	if (t - t0 < 150) return cursor; // Too early
	p("TrialTime=" + (t - t0) + "\n");

	results = results.N(d2);
	results.setText(answer == null ? "F" : 
			answer.equals(correct) ? "C" : "W");
	

	trialrunning = false;
	results = results.N(d2);
	results.setText((t - t0) + " ms");
	t0 = 0;
	    
	Cell n;
	while ((n = results.s(d2, -1)) != null) results = n;
	trial++;
	saveResults(results, true);
	
	if (answer == null) return cursor = results;

	message.setText(answer.equals(correct) ? "+" : "-");
	showMessage(message, 1000);

	return nextTrial();
    }

    void saveResults(Cell save, boolean tofile) {
	String data = "";
	for (Cell c = save; c != null; c = c.s(d1)) 
	    {
		for (Cell t = c; t != null; t = t.s(d2))
		    data += t.t() + " ";
		data += "\n";
	    }
	if (tofile) {
	    try {
		String file = "subject" + subject + ".data";
		java.io.FileWriter f = new java.io.FileWriter(file, true);
		f.write(data);
		f.close();
	    } catch (Exception e) {
	    }
	}
	else p(data);

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
	    // window.re
		if (triapaint();
	    try {
		Thread.sleep(100);
	    }catch(Exception e) {
	    }
	    */
	}
	public boolean keystroke(String s) {
	    Cell tmp = null;
	    // p("GOT KEYSTROKE '"+s+"'");
	    if (trialrunning) {
		if (s.equals("Control_L")) {
		    tmp = endTrial("L");
		} else if (s.equals("Control_R")) {
		    tmp = endTrial("R");
		} else if (s.equals("Escape")) {
		    tmp = endTrial(null);
		}
	    } else if (s.equals("Up") && !trialrunning) {
		tmp = cursor.s(d2, -1);
	    } else if (s.equals("Down")) {
		tmp = cursor.s(d2, +1);
	    } else if (s.equals("Left")) {
		tmp = cursor.s(d1, -1);	      
	    } else if (s.equals("Right")) {
		tmp = cursor.s(d1, +1);	      
	    } else if (s.equals("Prior")) {
		vc.imgtype = (vc.imgtype + 7) % 8;
		tmp = cursor;
	    } else if (s.equals("Next")) {
		vc.imgtype = (vc.imgtype + 1) % 8;
		tmp = cursor;
	    } else if (s.equals("minus")) {
		seed--;
		makeRandomCells();
		tmp = cursor;
	    } else if (s.equals("plus")) {
		seed++;
		makeRandomCells();
		tmp = cursor;
	    } else if (s.equals("e") || s.equals("p")) {
		if (results == null) {
		    save = results = space.N();
		} else {
		    Cell n;
		    for (; (n = results.s(d2, -1)) != null; results = n);
		    for (; (n = results.s(d1, +1)) != null; results = n);
		    results = results.N(d1);
		}
		Date now = new Date(System.currentTimeMillis());
		results.setText((s.equals("p") ? "Practice " : "Experiment ") 
				+ now.toString());
		saveResults(results, true);

		if (s.equals("p")) {
		    makeTrials(3);
		} else {
		    makeTrials(numtrials);
		    shuffleTrials();
		}
		trial = 0;
		tmp = nextTrial();
	    } else if (s.equals("c")) {
		if (trial > 0 && trial < trials.length) {
		    tmp = nextTrial();
		}
	    } else if (s.equals("q")) {
		System.exit(0);
	    } else if (s.equals("BackSpace")) {
		if (subject.length() > 0) {
		    subject = subject.substring(0, subject.length() - 1);
		    tmp = cursor;
		} else {
		    save = results = null;
		    tmp = cursor = cells[0];
		}
	    } else if ("0123456789".lastIndexOf(s) != -1) {
		subject += s;
		tmp = cursor;
	    } else if (s.equals("s")) {
		saveResults(save, false);
	    }
	    
	    if (tmp != null) {
		if (!trialrunning) 
		    vc.clearbg = vc.clearbg1;
		animusef = cursor != tmp;
		if (animusef) {
		    cursor = tmp;
		    GLUpdateManager.chg();
		} else {
		    generateEndState(.5f);
		    endAnimation();
		    renderStill(.5f);
		}
		if (trialrunning && t0 == 0) {
		    t0 = System.currentTimeMillis();
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

    public PsykoTest2() {
	window = GZZGL.createWindow(10, 10, winw, winh, new EH());
	vc = new GLVanishingClient();
	vc.clearbg = vc.clearbg1;
	vanishing.shrink[0] = 0.7f;
	vanishing.shrink[1] = 0.7f;
	vanishing.xgapmult = 1.3f;
	vanishing.ygapmult = 1.3f;

	vanishing.initmul = 1.6f;
	
	//vanishing.shrink[0] = 1.0f;
	//vanishing.shrink[1] = 1.0f;

	vanishing.raster.depth = 10;
	makeRandomCells();

	done = space.N(); done.setText("D");
	done = done.N(d1); done.setText("o");
	done = done.N(d1); done.setText("n");
	done = done.N(d1); done.setText("e");
	done = done.s(d1, -2);

	// Render initial scene
	vs1 = new GLVobScene();
	renderScene(vs1);
	vs1.makeFullList();
	//vs1.dump();
	vs1.renderFull(window);
    }



    public static void main(String[] argv) throws Exception {
	GLUpdateManager.addWindow(new PsykoTest2());
	while(true) {
	    p("Going to synch for eventloop");
	    GZZGL.eventLoop(GLUpdateManager.getTicker());
	    p("Event loop returned!");
	}
    }

}

