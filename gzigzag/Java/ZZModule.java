/*   
ZZModule.java
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
import java.awt.*;
import java.util.*;
import java.io.*;

/** An extension module implementing some Java functionality.
 * Extension module classes should have a single static public member
 * named <code>module</code> of this type.
 * <p>
 * Clang commands implemented in extension modules are accessed
 * through the Module.FUNCTION interface.
 */

public abstract class ZZModule {
String rcsid = "$Id: ZZModule.java,v 1.15 2001/04/18 21:40:51 bfallenstein Exp $";

    /** Perform an action.
     * For instance, if the action Foo.BAR is used in a cell, the method
     * org.gzigzag.module.Foo.module.action("BAR",...) is called.
     * @param id The name of the action.
     * @param code The cell that the code was obtained from.
     * @param target The cell that is the target of the action. Either the
     * 			cell the cursor is on, or the cell that the user clicked
     * 			on with the mouse.
     * @param view The view the user activated this action from
     * @param cview The control view associated
     * @param key The event identifier
     * @param pt The point clicked by the mouse
     * @param xi The ZZScene describing the current contents of the window.
     * 		 May be used e.g. to draw XOR cursors.
     */
    public void action(String id,
	    ZZCell code, 
	    ZZCell target,
	    ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
    }

    /** A callback with a direction.
     *  This is for actions like NEW, which take a direction as a parameter.
     */
    public void dirAction(String id, ZZCell win, ZZCell accursed, String vdim, 
			  int dir, ZZCell dataWin, ZZCell ctrlWin, ZZCell code) {
    }

    /** Returns a ZOb for the given id.
     * A cell parameter is not needed: since it's a ZOb, the caller must
     * explicitly read them (and set the Component) after getting it.
     * @see ZOb
     */
    public ZOb newZOb(String id) { return null; }

    // Static routines for loading

    static Hashtable mods = new Hashtable();
    
    /** Obtain the ZZModule object corresponding to the given string.
     * This is the <b>module</b> member of the corresponding class.
     */
    static public ZZModule getModule(String name) {
	ZZModule m;
	if((m= (ZZModule)mods.get(name)) == null) {
	    try {
		Class clazz = Class.forName("org.gzigzag.module."+name);
		m = 
			(ZZModule)clazz.getField("module").get(null);
	    } catch(Exception e) {
		ZZLogger.exc(e);
		ZZLogger.log("Error while loading module '"+name+"'");
		return null;
	    }
	    mods.put(name, m);
	}
	return m;
    }

}
