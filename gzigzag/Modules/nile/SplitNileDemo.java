/*   
SplitNileDemo.java
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
package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;
import java.io.*;


public class SplitNileDemo {
public static final String rcsid = "$Id: SplitNileDemo.java,v 1.2 2000/12/26 21:59:22 bfallenstein Exp $";
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    /** Update the bindings etc.in the space.
     * This is temporary and should go away once we have better
     * space management.
     */
    static public void updateSpace(ZZCell code, ZZCell viewCell, 
						ZZCell ctrlCell) {
	// First, make sure the system list cell is ok
	// and recreate actions.

	ZZSpace sp = code.getSpace();
	ZZCell t0 = ZZDefaultSpace.findOnSystemlist(sp, "SplitNile", true);
	ZZCell bind = t0.getOrNewCell("d.1", 1);
	ZZCell rast = t0.getOrNewCell("d.2", 1);

	bind.disconnect("d.1", 1);
	bind = bind.N("d.1", 1);
	Nile1.actions(bind);

	rast.disconnect("d.1",1);
	rast = rast.N("d.1");
	rast.setText("Nile1View");
	rast = rast.N("d.1");
	rast.setText("Nile1.Parallel");
	rast = rast.s("d.1", -1); // go to the cell we need
	
	// Next, make sure we have a window

	ZZCell win = null;
	String title = "Nile";
	int round = 0;
	while(win == null) {
	    if(round > 1)
		throw new ZZError("Couldn't create window");
	    round++;

	    win = ZZDefaultSpace.findOnSystemlist(sp, "Windows", false);
	    win = win.s("d.1");
	    win = win.findText("d.2", 1, title);

	    if(win == null)
		ZZDefaultSpace.newToplevelView(sp,
		    title, 20, 20, 600, 400,
		    null, null, null, null,
		    null, new Color(0x819fff));
	}
	
	// Then, create the cursor pair and set the window cursor
	win = win.s("d.1", 2);
	ZZCell leftcurs = win.N();
	ZZCursorReal.attach(leftcurs, ctrlCell);
	ZZCell rightcurs = leftcurs.N("d.nile-pair");
	ZZCursorReal.attach(rightcurs, viewCell);
	ZZCursorReal.set(win, leftcurs);

	// Finally, set window bindings
	ZZCell bcurs = win.getOrNewCell("d.bind", 1);
	ZZCursorReal.set(bcurs, bind);
	ZZCell viewsel = win.findText("d.2", 1, "View");
	ZZCursorReal.set(viewsel, rast);
	ZZCursorReal.setColor(viewsel, ZZUtil.perturb(Color.red));
    }

    static public ZZModule module = new ZZModule() {
	public void action(String id,
		ZZCell code, 
		ZZCell target,
		ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
	    ZZUpdateManager.setFast(view);

	    // Update the Nile stuff to the latest bindings (defined
	    // unfortunately in this file -- oh how I long for the day
	    // when they are in a ZZ space as the definitive version)
	    if(id.equals("UPDATE")) {
		updateSpace(code, view.getViewcell(), cview.getViewcell());
	    }
	}
    };
}
