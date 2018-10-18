/*   
ZZMap.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
 * Written by Kimmo Wideroos
 */

/* abstract base class for different maps to be used in views
 */

package org.gzigzag.map;
import org.gzigzag.*;
import java.util.*;

public abstract class ZZMap implements Runnable, Cloneable {
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    final public class NoFocusException extends Exception {
        public NoFocusException() { super("No focus defined!"); }
    }

    float _erosionCoeff = (float)0.2;
    int _ITERATION_RATE = 500;
    int TIMEOUT = 5000;
    String _weightdim = "d.weights";

    protected InputReader inputReader = null;

    public void setErosionCoeff(float ec) { _erosionCoeff = ec; }
    public void setIterationRate(int ir) { _ITERATION_RATE = ir; }
    public void setWeightDim(String wd) { _weightdim = wd; }

    // 'Particle' represents input vector and its 2d position  
    public interface Particle {
        int id();
        ZZCell cell();
	int q(int ind);
	int[] q();
	void setq(int ind, int q);
	float x();
	float y();
	void x(float new_x);
	void y(float new_y);
	float[] coord();
	void coord(float[] new_coord);
	float priority();
        int size();
    } 

    public interface InputReader {
	boolean contextChanged(String[] dims, ZZCell context);
	void read(String[] dims, ZZCell context, 
		  ZZCell[][] ids, int[][][] inputs);
    }

    protected float[] weights;
    //protected float[] particleWeights;
    protected float weight0;
    protected Particle[] inputs; 
    protected int[] shown_inputs; 
    private int focusId = -1; 
    private boolean changed = true;
    private boolean init = false;

    private volatile Thread t;
    private volatile boolean threadSuspended = false;
    private long update_time = 0;

    public boolean equals(ZZMap map) {
	return this.getClass().equals(map.getClass());
    } 

    protected class WeightWatcher implements ZZObs {
        private ZZCell c;
        private int ind;
	public void chg() {
	    float new_weight;
            new_weight = Float.parseFloat(c.getText(this));
	    //updateParticleWeights(new_weight-weights[ind], ind);
	    weights[ind] = new_weight;
        }
        public WeightWatcher(ZZCell c, int ind) { 
            this.c = c;
            this.ind = ind; 
        }
    } 

    protected void rollbackWeights(ZZCell mroot) {
        ZZCell c = mroot;
        for(int i=0; i<weights.length; i++) {
            c.setText(""+weights[i]);
            c = c.s(_weightdim);
        }       
    }
    /*
    // change particle weights according to change in weights @ 'ind'
    protected void updateParticleWeights(float delta_weight, int ind) {
	for(int i=0; i<particleWeights.length; i++)
	    particleWeights[i] += inputs[i].q(ind) * delta_weight;
    }
    
    // particle weights 
    // XXX not ready yet!
    protected void computeParticleWeights() {
	for(int i=0; i<particleWeights.length; i++) {
	    particleWeights[i] = (float)1.0;
	}
    }
    */
    protected void readWeights(ZZCell mroot, float w0) {
	if(w0<0) { 
	    weights = new float[inputs.length];
	    for(int i=0; i<weights.length; i++) weights[i] = 1;
	} else {
	    ZZCell c = mroot.s(_weightdim);
	    if(c != null)	
		for(int i=0; i<inputs.length; i++) {
		    WeightWatcher wobs = new WeightWatcher(c, i);
		    weights[i] = Float.parseFloat(c.getText(wobs));
		    c = c.s(_weightdim);
		}
	    else {
		for(int i=0; i<inputs.length; i++) 
		    weights[i] = weight0;
		rollbackWeights(mroot);
	    }
	}
    }

    protected void erodeWeights() {
        for(int i=0; i<weights.length; i++) {
            float w = weights[i];
	    w = (w-weight0) * _erosionCoeff + weight0;
	    w -= _erosionCoeff;
	    if(w < weight0) w = weight0;
            weights[i] = w;
	}
    }

    // returns the number of inputs
    public int nInputs() { return inputs.length; }

    // returns the size of single input vector
    public int inputSize() { return inputs[0].size(); }

    public float weight(int ind) { return weights[ind]; }

    /*
    public float particleWeight(int input_ind) { 
	return particleWeights[input_ind]; 
    }
    */

    public ZZMap() { super(); } 

    public boolean isInitialized() { return init; }

    // this must be called outside to start
    public void initialize(InputReader ir, /*
			   String[] dims, ZZCell accursed, */ float w0) {
        inputReader = ir;
	weight0 = w0;
	mapInit();
	//update(dims, accursed);
	update_time = System.currentTimeMillis();
        init = true;
    }

    // this must be called outside to start (uses no weights!)
    public void initialize(InputReader ir/*, 
			   String[] dims, ZZCell accursed*/) {
	initialize(ir, /*dims, accursed,*/ -1);
    }

    // map specific initialization stuff, 'initialize' calls this
    protected abstract void mapInit();

    // map specific initialization stuff, 'update' calls this
    protected abstract void mapUpdate();

    // if context has changed, initialize map
    public synchronized boolean update(String[] dims, ZZCell accursed) {
	update_time = System.currentTimeMillis();
	if(t == null && _ITERATION_RATE > 0) {
	    t = new Thread(this);
	    t.start();
	    t.setPriority(t.MIN_PRIORITY);
	}

	// map's input reader decides whether it is time for update
	if(!inputReader.contextChanged(dims, accursed)) {
	    System.out.println("update returns false!");
	    return false;
	} else {
	    System.out.println("update returns true!");
	}

	threadSuspended = true;
        ZZCell[][] cells = new ZZCell[1][];
	int[][][] input_vecs = new int[1][][];
	inputReader.read(dims, accursed, cells, input_vecs);
	System.out.println("read!");
        inputs = new Particle[cells[0].length];
        for(int i=0; i<cells[0].length; i++) {
	    ZZCell c=cells[0][i];
	    if(c.equals(accursed)) { 
                inputs[i] = makeParticle(i, c, input_vecs[0][i], true);
		focusId = i;
	    } else
		inputs[i] = makeParticle(i, c, input_vecs[0][i], false);
	}
	System.out.println("made particles!");
	weights = new float[inputs.length];
	//particleWeights = new float[weights.length];
	readWeights(accursed.getSpace().getHomeCell(), weight0);
	System.out.println("read weights!");	
	//computeParticleWeights();
	mapUpdate();
	System.out.println("map updated!");
	changed(true);
	iterate();
	System.out.println("iterated!");
	threadSuspended = false;
	notify();
	return true;
    }

    // returns a particle representing input vec 'input_vec' with an id 'id'
    // 'focus' means whether this particle is in focus. 
    protected abstract Particle makeParticle(int id, ZZCell c, 
					     int[] input_vec, boolean focus);

    // return only particles to be shown.
    // if 'shown_inputs' == null, this acts like 'getParticles'
    public ZZMap.Particle[] getShownParticles() {
	if(shown_inputs == null) return getParticles();
        int nshown = shown_inputs.length;
        ZZMap.Particle[] sp_list = new ZZMap.Particle[nshown];
        for(int i=0; i<nshown; i++)
	    sp_list[i] = inputs[shown_inputs[i]];
	return sp_list;
    } 

    // return all particles (=input objects, vectors)
    public ZZMap.Particle[] getParticles() {
        ZZMap.Particle[] p_list = new ZZMap.Particle[inputs.length];
        for(int i=0; i<inputs.length; i++)
	    p_list[i] = inputs[i];
	return p_list;
    } 

    public void run() {
	System.out.println("iterate, suspended = "+threadSuspended);
        long delay; 
        Thread thisThread = Thread.currentThread();
        while (t == thisThread) {
            try {
                thisThread.sleep(_ITERATION_RATE);
		if (threadSuspended && t == thisThread) {
		    synchronized(this) {
			while (threadSuspended && t == thisThread)
			    wait();
		    }
		}
            } catch (InterruptedException e) { }
	    try {
		synchronized(this) {
		    System.out.println("run!");
		    if(System.currentTimeMillis() - update_time > TIMEOUT) {
			System.out.println("ZZMap timeout - stop!");
			stop();
		    }
		    if(iterate()) changed = true; 
		    if(weights != null)
			erodeWeights();
		    if(changed) {
			ZZUpdateManager.chg();
		    }
		} 
	    } catch(Exception e) {
		e.printStackTrace();
		System.out.println("Exception in ZZMap: "+e);
	    }
	}
    }

    public synchronized void stop() {
        t = null;
        notify();
    }

    public int focusId() throws NoFocusException { 
	if(focusId < 0) throw new NoFocusException();
	return focusId; 
    }
    
    public boolean changed() { return changed; }
    public void changed(boolean new_value) { changed = new_value; }

    // return 'true', if something changed radically during iteration
    public abstract boolean iterate();

}


