/*   
ZZUpdateManager.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag;
import java.util.*;

/** A single global instance to manage the updating of views.
 * The views are set in a priority order so that even if the user is
 * moving fast, he will get an immediate response through the less important
 * views not being updated at each step.
 */

public class ZZUpdateManager implements Runnable {
public static final String rcsid = "$Id: ZZUpdateManager.java,v 1.29 2001/02/27 08:36:15 raulir Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

    /** The order of views, from the most important to the least.
     */
    private static Vector ordering = new Vector();

    /** Whether an update is currently in progress, through 
     * a view being painted. 
     */
    private static boolean updating;
    private static int disabled;
    private static boolean noanimation;
    /** Whether the currently ongoing update should stop immediately
     * and restart from the beginning
     */
    private static boolean restartUpd = false;

    public static void addView(ZZView v) { ordering.addElement(v); }
    public static void rmView(ZZView v) { ordering.removeElement(v); }

    /** Set the view that has the privilege of animating the next
     * update.
     * Setting this to null will make all views update fast.
     */
    public static void setFast(ZZView v) {
	p("Setfast: "+v + " cur: "+ordering.elementAt(0));
	if(v == null) {
	    noanimation = true;
	    return;
	}
	if(ordering.elementAt(0)!=v) {
	    ordering.removeElement(v);
	    ordering.insertElementAt(v, 0);
	}
    }


    /* Called by a space to inform that some cells have been 
     * changed 
     */
    public static void chg() {
	p("UPDMANAGER CHG");
	if(restartUpd) return;
	synchronized(ordering) {
		restartUpd = true;
		ordering.notifyAll();
	}
	p("UPDMANAGER CHGOUT");
    }

    /** Disable all updating of views.
     * XXX While frozen, might consider updating e.g. once every 
     * second...
     */
    public static void freeze() {
	p("ZZUpdatemanager freeze");
	synchronized(ordering) { disabled++; }
	p("ZZUpdatemanager frozen "+disabled);
    }
    /** Enable again the updating of views. */
    public static void thaw() {
	p("ZZUpdatemanager thaw");
	synchronized(ordering) {
	    if(disabled<=0)
		throw new ZZError("thaw() without matching freeze()!");
	    if(--disabled==0) ordering.notifyAll();
	}
	p("ZZUpdatemanager thawed "+disabled);
    }

    static ZZUpdateManager m = new ZZUpdateManager();
    static Thread t= new Thread(m);
    static {
	p("STARTORDTHREAD");
	t.start();
    }

    public void run() {
	try {
	int ind = -1;
	boolean reras = true;
	boolean anim = false;
	Vector curUpdVec = new Vector();

	while(true) {
	    p("STARTORD");
	    synchronized(ordering) {
		p("STARTORD SYNCHED");
		if(!anim) ind++;
		updating = false;
		try {
		while(disabled>0 || (ind>=curUpdVec.size() && !restartUpd)) {
		    p("STARTORD "+disabled+" "+ind+" "+restartUpd);
		    ordering.wait();
		}
		} catch(Exception e) {
		    ZZLogger.exc(e);
		}
		p("STARTORD WAITED");
		updating = true;
		if(restartUpd) {
		    restartUpd = false;
		    curUpdVec = (Vector)ordering.clone();
		    ind = 0;
		    reras = true;
		    // Try to make the system stop less at 
		    // inconvenient, unequal times.

		    // System.gc(); // doesn't seem to work.
		}
		if(curUpdVec.size()==0) continue;
	    }
	    try {
		p("STARTORD DOING " + ind + " "+ curUpdVec+" "+reras);
		ZZView v = (ZZView)curUpdVec.elementAt(ind);
		if(ind==0) { // animate if need be
		    if(reras) {
			timeNewCycle();

			anim = v.reraster();
			reras = false;
		    }
		    float fract = timeAnimFract();

		    if(fract >= 1 || !anim || noanimation) {
			fract = 1;
			anim = false;
			noanimation = false;
		    }
		    p("PN 0: "+fract+" "+anim+" "+lastStart+" "+millis);
		    // XXX paintNow() is not really safe here, because ZZ code
		    // in general isn't thread-safe. If Kaffe someday provides
		    // EventQueue.invokeAndWait(), this could be fixed.
		    boolean care = ZZDrawing.instance.enableQuality(false);
		    v.paintNow((float)fract);
		    if (fract == 1 && care) {
			ZZDrawing.instance.enableQuality(true);
			v.paintNow(1.0f);
		    }
		} else {
		    p("PN: "+ind);
		    v.reraster();
		    // XXX Ditto.
		    boolean care = ZZDrawing.instance.enableQuality(false);
		    v.paintNow(1.0f);
		    if (care) {
			ZZDrawing.instance.enableQuality(true);
			v.paintNow(1.0f);
		    }
		}
		((java.awt.Component)v).getToolkit().sync();
	    } catch(ZZError e) {
		ZZLogger.exc(e);
		System.out.println("EXCEPTION WHILE UPDATING!");
	    }
	    p("STARTORD DONE: anim "+anim);
	}
	} finally {
	    System.out.println("HELP! UPDATE LOOP STOPPED. XXX");
	}
    }

    static final long defmillis = 800;
    /** The time before the next user event
     * to try to be ready.
     */
    static final long trybefore = 150;
    /** The time to increment the interval with.
     */
    static final long inctime = 80;
    private static long lastStart = 0;
    /** The interval between start and end of animation.
     */
    private static long millis = defmillis;

    /** Called when we begin a new animation. */
    private void timeNewCycle() {
	long cur = System.currentTimeMillis();
	// Interval between start of last animation and start of the new one.
	long sinceLast = cur-lastStart;

	// sinceLast - millis = interval between end of last animation and
	// start of the new one. We try to end the last animation
	// trybefore ticks before the start of the new one, so if
	// sinceLast - millis < trybefore, the animation takes too long.
	if(sinceLast - millis < trybefore)
	    millis = sinceLast-trybefore; // Shorten the animation time.

	// If the animation didn't take too long, and if it's at least a unit
	// (inctime) shorter than it was by default, gradually lengthen it.
	else if(millis < defmillis - inctime)
	    millis += inctime;

	lastStart = cur;
    }

    private float timeAnimFract() {
	long cur = System.currentTimeMillis();

	double fract = 1.0;

	// If we are animating at all (20 milliseconds is
	// minimum time we try to use animation with), 
	// calculate the fraction of the animation we are at.
	if(millis > 20)
	    fract = (cur-lastStart) / (double)millis;

	// Test for a really weird JVM problem.
	if(cur - lastStart >= millis && fract < 1) {
	    System.out.println("AAAARRRRRGGGHHH!!! Division problem "+ cur + " "+lastStart+" "+millis);
	}

	return (float)fract;
    }

}


