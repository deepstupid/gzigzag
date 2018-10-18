/*   
ExtensibleVobScene.java
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

/** An interface which is used to provide extra functionality 
 * for VobScenes.
 */
public interface ExtensibleVobScene extends VobScene {
    interface InterpHook {
	void start(VobScene to, InterpSetter i);
	boolean setInterp(Vob vob);
    }

    /** An interface through which vob interpolation may be placed.
     * An InterpHook has two options of calling the InterpSetter
     * in the setInterp(Vob vob) method:
     * either a single call <pre>set(x,y,w,h)</pre>, or a sequence of calls
     * like
     * <pre>
     *		put(splitPart, d, splitX, splitY, splitW, splitH);
     *		put(null / anything, d, targetX, targetY, targetW, targetH);
     *		put(splitPart, d, splitX, splitY, splitW, splitH);
     *		put(null / anything, d, targetX, targetY, targetW, targetH);
     * </pre>
     * which means that the vob is split and represented by the given 
     * parts. The original vob is then not shown during interpolation,
     * but the vob inserted as the splitPart is interpolated towards the
     * given target coordinates.
     * <p>
     * This style of interface attempts to minimize the number of new
     * objects created in the interest of speed.
     */
    interface InterpSetter extends VobPlacer {
	/** Set the target coordinates of the given vob.
	 */
	void set(int x, int y, int w, int h);
    }

    void addHook(VobPlacer hook);
    // void addHook(InterpHook hook);
    VobPlacer getIndexHook(Class type);
}

