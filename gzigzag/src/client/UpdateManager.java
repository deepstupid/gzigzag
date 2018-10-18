/*   
UpdateManager.java
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
 * Written by Tuomas Lukka and Rauli Ruohonen
 */
package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import org.gzigzag.util.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.image.*;

/** A single global instance to manage the updating of windows.
 * The windows are set in a priority order so that even if the user is
 * moving fast, he will get an immediate response through the less important
 * windows not being updated at each step.
 * <p>
 * Currently, GZigZag is single-threaded very strongly, so you can't
 * allow anything to happen while the UpdateManager is working.
 * Because of this, <b>all code which modifies the space must do</b>
 * <pre>
 * synchronized(UpdateManager.getSynchronizer()) {
 *  	... alter space
 * }
 * </pre>
 */

public class UpdateManager implements Runnable {
public static final String rcsid = "$Id: UpdateManager.java,v 1.3 2002/03/19 19:33:04 tjl Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }

    /** An interface for avoiding the Java event queue, which we have
     * synchronization problems with.
     * This way we can encapsulate a call to the Java2 EventQueue API
     * if we want to.
     */
    public interface EventProcessor {
	void zzProcessEvent(AWTEvent e);
    }

    private static List eventList = new LinkedList();
    public static void addEvent(EventProcessor proc, AWTEvent e) {
	synchronized(eventList) {
	    eventList.add(proc);
	    eventList.add(e);
	}
	synchronized(ordering) {
	    ordering.notifyAll();
	}
    }

    /** The order of windows, from the most important to the least.
     */
    private static ArrayList ordering = new ArrayList();

    /** Return the object which is used to synchronize the update loop.
     * All code which changes a zigzag space must be synchronized as per
     * above using this object.
     * <p>
     * <b>Nothing else may be done with this object, whatever
     * type it has.</b>
     */
    public static Object getSynchronizer() {
	return ordering;
    }

    private static ImageObserver imageobs = new ImageObserver() {
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
	    // if((infoflags & ALLBITS) != 0) chg();
	    return true; // continue loading image. XXX Should this be false? We redraw soon anyway!
	}
    };

    /** Get the imageobserver object to give to image painting commands, if
     * the update manager should redraw things when the image becomes available.
     */
    static public ImageObserver getImageObserver() { return imageobs; }

    /** Whether an update is currently in progress, through 
     * a window being painted. 
     */
    private static boolean updating;
    private static int disabled;
    private static boolean noanimation;
    /** If true, all windows have incorrect end state, and generateEndState()
     * should be called for all of them at appropriate times.
     */
    private static boolean restartUpd = false;
    public static final float jumpFract = 0.00000000001f;

    /** A window animation is performed in. It is a mutable pair of states:
     * (start, end). The end state doesn't initially exist.
     */
    public interface Window {
	/** Creates an end state, and returns true iff the time given
	 * for generation was sufficient for the given level of detail.
	 * If false is returned, lower level of detail would have produced
	 * a better result.
	 */
	boolean generateEndState(int millis, float lod);
	/* Returns true iff animation is wanted. The end state is
	 * sometimes so different from the start state that animation isn't
	 * useful, and this function returns false.
	 */
	boolean animUseful();
	/** Render state between start and end (interpolation), with the
	 * specified level of detail. fract is in [0, 1], and so is lod.
	 * 0.5 should be assumed to be the default lod. If the end state
	 * doesn't exist, may crash.
	 */
	void renderAnim(float fract, float lod);
	/** After this renderAnim(0, ...) would do the same as
	 * renderAnim(fract, ...) did before the call, if the end state wasn't
	 * destroyed (which it is). Used when the end state changes during
	 * animation, so that the animation continues from the state it
	 * reached, instead of jumping. If the end state doesn't exist, may
	 * crash.
	 */
	void changeStartState(float fract);
	/** Same as renderAnim(0, lod), except that it's not allowed to crash
	 * even if the end state doesn't exist. lod may be interpreted
	 * differently to show things that are not important when animating
	 * (such as connections).
	 */
	void renderStill(float lod);
	boolean hasEndState();
	/** Same as changeStartState(1), except that the end state doesn't
	 * exist afterwards. If there is no end state, must be a no-op.
	 */
	void endAnimation();
    }

    public static void addWindow(Window w) {
	pa("UpdManager: Adding window "+w+" into \n"+ordering);
	if (ordering.contains(w))
	    throw new IllegalArgumentException("Window already added!");
	ordering.add(w);
    }
    public static void rmWindow(Window w) { ordering.remove(w); }

    /** Set the window that has the privilege of animating the next
     * update. Setting this to null will make all windows update fast.
     */
    public static void setSlow(Window w) {
	synchronized(ordering) {
	    if (ordering.size() <= 0) return;
	    p("Setslow: "+w + " cur: "+ordering.get(0));
	    if(w == null) {
		noanimation = true;
		return;
	    }
	    if(ordering.get(0) != w) {
		if(!ordering.contains(w)) {
		    pa("Tried to set a fast window that isn't registered "+w);
		    return;
		}
		ordering.remove(w);
		ordering.add(0, w);
	    }
	}
    }

    private static boolean chgRun = true;
    private static Runnable makeChg = new Runnable() {
	public void run() {
	    synchronized(ordering) {
		chgRun = true;
		restartUpd = true;
		ordering.notifyAll();
	    }
	}
    };
    private static EventQueue systemEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();

    /* Called by a space to inform that some cells have been 
     * changed 
     */
    public static void chg() {
	p("UPDMANAGER CHG");
	if(restartUpd) return;
	if(chgRun) {
	    chgRun = false;
	    makeChg.run();
	    // systemEventQueue.invokeLater(makeChg);
	}
	/*
	synchronized(ordering) {
		restartUpd = true;
		ordering.notifyAll();
	}
	*/
	p("UPDMANAGER CHGOUT");
    }

    /** Disable all updating of windows.
     * XXX While frozen, might consider updating e.g. once every 
     * second...
     */
    public static void freeze() {
	p("Updatemanager freeze");
	synchronized(ordering) { disabled++; }
	p("Updatemanager frozen "+disabled);
    }
    /** Enable again the updating of windows. */
    public static void thaw() {
	p("Updatemanager thaw");
	synchronized(ordering) {
	    if(disabled<=0)
		throw new ZZError("thaw() without matching freeze()!");
	    if(--disabled==0) ordering.notifyAll();
	}
	p("Updatemanager thawed "+disabled);
    }

    private static UpdateManager m = new UpdateManager();
    private static Thread t = new Thread(m);
    static {
	p("STARTORDTHREAD");
	t.start();
    }

    public interface FractCalculator {
	float OVER = -100f;
	void eventAt(long time);
	float getFract(long time);
    }
    public class SimpleCalculator implements FractCalculator {
	protected float r = 10, n = 2, spd = 1;
	protected long startTime = 0;
	public void eventAt(long time) {
	    startTime = time;
	}
	public float getFract(long time) {
	    float x = (time-startTime)/(spd*1000.0f);
	    //x = (float)(1-Math.cos(2*Math.PI*n*x)*Math.pow(1-x, r));
	    float y = (float)(1-Math.cos(2*Math.PI*n*x)*Math.exp(-x*r));
	    if (-x*r < Math.log(0.02)) return OVER;
	    return y;
	}
    }
    public FractCalculator fractCalc = new SimpleCalculator();

    private void generateEndState(Window w) {
	w.generateEndState(100, 1); // XXX
    }
    private void renderAnim(Window w, float fract) {
	w.renderAnim(fract, 1); // XXX
    }
    public void run() {
	// Handle events.

	// We want this thead to have the lowest priority, since
	// we want all incoming key events to be handled before
	// this thread gets its moment to run.
	// This is not very nice and it would be nicer to use
	// the java.awt.EventQueue wakeup routines of 1.3 / 1.4 here,
	// but kaffe doesn't have them.
	// Sigh.
	// Maybe we should check at runtime...
	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

	int ind = -1;
	boolean initNeeded = false; // Init outside synch(ord) needed.
	boolean firstWinAnim = false; // The first window is still animating.
	boolean jumpDone = false;
	Window[] wins = new Window[0], newwins = null;
	boolean[] regenNeeded = new boolean[0];
	long lastTime = 0; // Last start of first win animation.
	while(true) try {
	    // This is necessary so that the JVM won't stuff
	    // X's queue full of stuff and render far more scenes
	    // far more quickly than it should and leave X lagging.
	    GlobalToolkit.toolkit.sync();
	    p("STARTORD");
		p("STARTORD SYNCHED");
		if (!firstWinAnim) ind++;
		updating = false;
		while(eventList.size() != 0 ||
			    disabled>0 || (ind >= wins.length && !restartUpd)) {
		    p("STARTORD "+disabled+" "+ind+" "+restartUpd);
		    if(eventList.size() != 0) {
			EventProcessor proc;
			AWTEvent evt;
			synchronized(eventList) {
			    proc = (EventProcessor)eventList.get(0);
			    evt = (AWTEvent)eventList.get(1);
			    eventList.remove(1);
			    eventList.remove(0);
			}
			proc.zzProcessEvent(evt);
		    } else 
			if(!restartUpd)
			    try {
			    // System.gc();
				synchronized(ordering) {
				ordering.wait(2000); // wait a finite time
						    // just in case...
				}
			    } catch(InterruptedException e) {
			    }
		    // Thread.yield(); // Do this here to allow the event processing
				// thread to continue...
		}
	    // Thread.yield(); // Again, try to let the event thread have control.
	    synchronized(ordering) {
		p("STARTORD WAITED");
		updating = true;
		if (restartUpd) {
		    restartUpd = false;
		    newwins = (Window[])ordering.toArray(new Window[0]);
		    ind = 0;
		    initNeeded = true;
		}
		try {
		    if (initNeeded) {
			initNeeded = false;
			long time = System.currentTimeMillis();
			float fract = fractCalc.getFract(time);
			if (firstWinAnim) {
			    if (fract == FractCalculator.OVER ||
				!wins[0].hasEndState())
				wins[0].endAnimation();
			    else wins[0].changeStartState(fract);
			}
			firstWinAnim = false;
			fractCalc.eventAt(time);
			wins = newwins; newwins = null;
			regenNeeded = new boolean[wins.length];
			for (int i = 0; i < regenNeeded.length; i++)
			    regenNeeded[i] = true;
			if (wins.length > 0) {
			    p("Generating end state for window 0");
			    generateEndState(wins[0]);
			    p("Generating end state: Phase 1 done");
			    if (!wins[0].hasEndState()) {
				pa("Couldn't generate end state for window 0!");
			    } else {
				regenNeeded[0] = false;
				if (!noanimation && wins[0].animUseful())
				    firstWinAnim = true;
			    }
			    p("Finished end state for window 0");
			}
			jumpDone = false;
			if (firstWinAnim && !wins[0].hasEndState())
			    pa("GRAA!");
		    }
		    if (ind >= wins.length) continue;
		    if(dbg) p("Try firstwinanim: "+firstWinAnim);
		    if (firstWinAnim) {
			long time = System.currentTimeMillis();
			float fract = fractCalc.getFract(time);
			if (fract == FractCalculator.OVER) firstWinAnim = false;
			else {
			    if (!wins[0].hasEndState())
				pa("AIEE! Animating without endstate!");
			    renderAnim(wins[0], fract);
			    if (!jumpDone && fract >= 0.3f) {
				// Shouldn't be many windows needing regen, or
				// this is going to jump.
				/*
				for (int i = 0; i < regenNeeded.length; i++) {
				    if (regenNeeded[i]) {
					generateEndState(wins[i]);
					if (!wins[i].hasEndState()) {
					    pa("Couldn't generate end state for win "+i);
					} else regenNeeded[i] = false;
				    }
				}
				for (int i = 1; i < wins.length; i++) {
				    wins[i].endAnimation();
				    wins[i].renderStill(0);
				}
				*/
				jumpDone = true;
			    }
			    continue;
			}
		    }
		    if (regenNeeded[ind]) {
			p("Generating end state for "+ind);
			generateEndState(wins[ind]);
			if (!wins[ind].hasEndState()) {
			    pa("Couldn't generate end state for win "+ind);
			} else regenNeeded[ind] = false;
		    }
		    p("End animation "+ind);
		    wins[ind].endAnimation();
		    p("Render still "+ind);
		    wins[ind].renderStill(1);
		} catch(ZZError e) {
		    System.err.println("EXCEPTION WHILE UPDATING!");
		    e.printStackTrace();
		}
	    }
	} catch(Throwable t) {
	    t.printStackTrace();
	    pa("Stopping update loop for five seconds.");
	    try {
		Thread.sleep(5000);
	    } catch(InterruptedException _) {};
	}

    }
}


