/*   
NileDemo.java
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


public class NileDemo {
public static final String rcsid = "$Id: NileDemo.java,v 1.3 2000/12/28 01:19:07 bfallenstein Exp $";
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    /** Update the bindings etc.in the space.
     * This is temporary and should go away once we have better
     * space management.
     */
    static void updateSpace(ZZCell code, ZZCell viewCell, ZZCell ctrlCell) {
	// First, make sure the system list cell is ok
	// and recreate actions.

	ZZSpace sp = code.getSpace();
	ZZCell t0 = ZZDefaultSpace.findOnSystemlist(sp, "Nile1", true);
	ZZCell bind = t0.getOrNewCell("d.1", 1);
	ZZCell rast = t0.getOrNewCell("d.2", 1);

	bind.disconnect("d.1", 1);
	bind = bind.N("d.1", 1);
	Nile1.actions(bind);

	rast.disconnect("d.1",1);
	rast = rast.N("d.1");
	rast.setText("Nile1View");
	rast = rast.N("d.1");
	rast.setText("Nile1.Normal");
	rast = rast.s("d.1", -1); // go to the cell we need
	
	// Then, update windows.
	
	ZZCell datac = updateWin(sp, viewCell, rast, bind, false);
	ZZCell ctrlc = updateWin(sp, ctrlCell, rast, bind, true);
	if(datac != null && ctrlc != null) {
	    datac.excise("d.nile-wins"); ctrlc.excise("d.nile-wins");
	    ctrlc.connect("d.nile-wins", 1, datac);
	    datac.connect("d.nile-wins", 1, ctrlc);
	}
    }

    static ZZCell updateWin(ZZSpace sp, ZZCell viewCell, ZZCell rast,
			  ZZCell bind, boolean ctrl) {
	if(viewCell == null) return null;

	// Make sure we have a window and update its
	// binding and raster.

	ZZCell win = null;
	String title = ctrl ? "Nile1-Left" : "Nile1W";
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
		    title, ctrl ? 0 : 400, 20, 400, 400,
		    null, null, null, viewCell,
		    null, new Color(0x819fff));
	}

	// Then, set window bindings
	win = win.s("d.1", 2);
	ZZCell bcurs = win.getOrNewCell("d.bind", 1);
	ZZCursorReal.set(bcurs, bind);
	ZZCell viewsel = win.findText("d.2", 1, "View");
	ZZCursorReal.set(viewsel, rast);
	ZZCursorReal.setColor(viewsel, ZZUtil.perturb(Color.red));
	return win;	
    }

    static public ZZModule module = new ZZModule() {
	public void action(String id,
		ZZCell code, 
		ZZCell target,
		ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
	    ZZUpdateManager.setFast(view);

	    ZZCell viewCell = view.getViewcell();
	    ZZCell otherCell = viewCell.s("d.nile-wins");

	    p("Nile action: "+id+" "+key);

	    StringTokenizer st = new StringTokenizer(id);
	    String[] toks = new String[st.countTokens()];
	    for(int i=0; i<toks.length; i++)
		toks[i] = st.nextToken();

	    id = toks[0];


	    // Update the Nile stuff to the latest bindings (defined
	    // unfortunately in this file -- oh how I long for the day
	    // when they are in a ZZ space as the definitive version)
	    if(id.equals("UPDATE")) {
		updateSpace(code, viewCell, null);
	    } else if(id.equals("UPDATEBOTH")) {
		updateSpace(code, viewCell, cview.getViewcell());
	    } else if(id.equals("REGISTER")) {
		ZZCell binds = code.N();
		Nile1.actions(binds);
		ApplitudeMgr.register(code.getSpace(), "Nile",
				      new String[] { "d.nile" },
				      "Nile1.Normal", binds);
	    }
	}
    };
}
