/*   
GLUpdateManager.java
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

/** Manage redrawing of views. Similar to org.gzigzag.impl.UpdateManager but 
 * cleaner since we don't have to worry about multiple threads, thank god.
 */
public class GLUpdateManager {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }

    static public interface FractCalculator {
	float OVER = -100f;
	void eventAt(long time);
	float getFract(long time);
    }
    static public class SimpleCalculator implements FractCalculator {
	protected float r = 10, n = 1, spd = 1;
	protected long startTime = 0;
	public void eventAt(long time) {
	    startTime = time;
	}
	public float getFract(long time) {
	    float x = (time-startTime)/(spd*1000.0f);
	    if(x > 1) return OVER;
	    return x;
	}
    }
    static public FractCalculator fractCalc = new SimpleCalculator();


    /** A window animation is performed in. It is a mutable pair of states:
     * (start, end). The end state doesn't initially exist.
     */
    public interface Window {
	/** Creates an end state, and returns true iff the time given
	 * for generation was sufficient for the given level of detail.
	 * If false is returned, lower level of detail would have produced
	 * a better result.
	 */
	boolean generateEndState(float lod);
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


    /** The order of windows, from the most important to the least.
     */
    private static ArrayList ordering = new ArrayList();

    private static boolean regenViews = true;

    static public void chg() {
	regenViews = true;
    }


    public static void addWindow(Window w) {
	pa("UpdManager: Adding window "+w+" into \n"+ordering);
	if (ordering.contains(w))
	    throw new IllegalArgumentException("Window already added!");
	ordering.add(w);
    }
    public static void rmWindow(Window w) { ordering.remove(w); }


    /** Perform one round of processing.
     * @return true, if tick needs to be called again shortly.
     */
    static public boolean tick() {
	if(regenViews) {
	    ((Window)ordering.get(0)).changeStartState(1);
	    ((Window)ordering.get(0)).generateEndState(0);
	    fractCalc.eventAt(System.currentTimeMillis());
	    regenViews = false;
	    return true;
	}
	while(true) {
	    float fract = fractCalc.getFract(System.currentTimeMillis());
	    if(fract == FractCalculator.OVER) {
		((Window)ordering.get(0)).changeStartState(1);
		((Window)ordering.get(0)).renderStill(0);

		return false;
	    }
	    ((Window)ordering.get(0)).renderAnim(fract, 0);
	}
//	return true;
	

    }

    public static GZZGL.Ticker getTicker() { 
	return new GZZGL.Ticker() {
	    public boolean tick() {
		return GLUpdateManager.tick();
	    }
	};
    }

}
