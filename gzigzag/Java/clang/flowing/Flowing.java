/*   
Flowing.java
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
 * Written by Benjamin Fallenstein
 */
package org.gzigzag.module;
import org.gzigzag.*;
import org.gzigzag.flowing.*;
import org.gzigzag.flowing.Data;
import java.awt.*;

/** A module interface to Flowing Clang.
 */

public class Flowing {
public static final String rcsid = "$Id: Flowing.java,v 1.8 2001/08/16 08:48:26 tjl Exp $";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }

    static final void createdebugger(ZZCell debug) {
	debug.getSpace().getHomeCell().h("d.2", 1).insert("d.2", 1, debug);
	ZZCell normal = debug.N("d.1", 1);
	normal.setText("Debug mode");
	ZZCell edit = normal.N("d.1", 1);
	edit.setText("Text edit mode");
	ZZCell dim0 = edit.N("d.1", 1);
	dim0.setText("d..flowing-frame");
	dim0.N("d.2", 1).setText("d.2");
	dim0.N("d.2", 1).setText("d..flowing-data");
	ZZCell dim1 = dim0.N("d.1", 1);
	dim1.setText("d.1");
	dim1.N("d.2", 1).setText("d.clone");
	dim1.N("d.2", 1).setText("d.xeq");
	
	// EDIT MODE
	
	ZZCell c = edit.N("d.2", 1);
	c.setText("Tab");
	c.N("d.1", 1).setText("SWITCHOFFCURSMODE");
	normal.insert("d.3", 1, c.s("d.1"));
	
	c = c.N("d.2", 1);
	c.setText("Esc");
	c.N("d.1", 1).setText("SWITCHOFFCURSMODE");
	normal.insert("d.3", 1, c.s("d.1"));
	
	c = c.N("d.2", 1);
	ZZDefaultSpace.findOnSystemlist(c.getSpace(), "Bindings", false)
	    .s("d.1", 2).s("d.2", 1).insert("d.3", -1, c);
	
	// NORMAL MODE
	
	c = normal.N("d.2", 1);
	c.setText(" ");
	c.N("d.1", 1).setText("Flowing.D-STEP");
	
	c = c.N("d.2", 1);
	c.setText("Enter");
	c.N("d.1", 1).setText("Flowing.D-JUMP");
	
	c = c.N("d.2", 1);
	c.setText("Backspace");
	c.N("d.1", 1).setText("UNDO");
	
	c = c.N("d.2", 1);
	c.setText("Alt-Backspace");
	c.N("d.1", 1).setText("REDO");
	
	c = c.N("d.2", 1);
	c.setText("q");
	c.N("d.1", 1).setText("QUIT");
	
	c = c.N("d.2", 1);
	c.setText("Tab");
	c.N("d.1", 1).setText("SWITCHCURSMODE");
	edit.insert("d.3", 1, c.s("d.1"));
	
	c = c.N("d.2", 1);
	c.setText("Left");
	c.N("d.1", 1).setText("CRSR X-");
	
	c = c.N("d.2", 1);
	c.setText("Right");
	c.N("d.1", 1).setText("CRSR X+");
	
	c = c.N("d.2", 1);
	c.setText("Up");
	c.N("d.1", 1).setText("CRSR Y-");
	
	c = c.N("d.2", 1);
	c.setText("Down");
	c.N("d.1", 1).setText("CRSR Y+");
	
	c = c.N("d.2", 1);
	c.setText("j");
	c.N("d.1", 1).setText("CRSR X-");
	
	c = c.N("d.2", 1);
	c.setText("l");
	c.N("d.1", 1).setText("CRSR X+");
	
	c = c.N("d.2", 1);
	c.setText("i");
	c.N("d.1", 1).setText("CRSR Y-");
	
	c = c.N("d.2", 1);
	c.setText(",");
	c.N("d.1", 1).setText("CRSR Y+");
	
	c = c.N("d.2", 1);
	c.setText("K");
	c.N("d.1", 1).setText("CRSR Z-");
	
	c = c.N("d.2", 1);
	c.setText("k");
	c.N("d.1", 1).setText("CRSR Z+");
	
	c = c.N("d.2", 1);
	c.setText("s");
	c.N("d.1", 1).setText("CRSR CX-");
	
	c = c.N("d.2", 1);
	c.setText("f");
	c.N("d.1", 1).setText("CRSR CX+");
	
	c = c.N("d.2", 1);
	c.setText("e");
	c.N("d.1", 1).setText("CRSR CY-");
	
	c = c.N("d.2", 1);
	c.setText("c");
	c.N("d.1", 1).setText("CRSR CY+");

	c = c.N("d.2", 1);
	c.setText("D");
	c.N("d.1", 1).setText("CRSR CZ-");
	
	c = c.N("d.2", 1);
	c.setText("d");
	c.N("d.1", 1).setText("CRSR CZ+");
    }

    static public ZZModule module = new ZZModule() {
	public void action(String id,
		ZZCell code, 
		ZZCell target,
		ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
	    if(id.equals("BIND") || id.equals("BINDREAL") ||
	       id.equals("RUN")  || id.equals("RUNREAL")) {
		ZZCell prog;
		if(id.indexOf("BIND") > -1) {
		    prog = code.s("d.2");
		    if(prog == null)
		        throw new ZZError("Flowing.BIND without program");
		    prog = prog.h("d.clone", -1, true);
		    if(prog == null)
		        throw new ZZError("Flowing.BIND prog must be cloned");
		} else {
		    prog = target;
		    if(prog == null)
			prog = ZZCursorReal.get(view.getViewcell());
		}
		boolean real = id.indexOf("REAL") > 0;
		FlowingClang.run(prog, new Data(cview.getViewcell(),
				                view.getViewcell(),
				                key), real);
	    } else if(id.equals("DEBUG")) {
		p("Create debugger:");
		ZZCell prog =
		    ZZCursorReal.get(view.getViewcell()).getRootclone();
		ZZCell debug = ZZDefaultSpace.findOnSystemlist(
		    view.getViewcell().getSpace(), "Flowing.Debugger", true);
		if(debug.s("d.1") == null) createdebugger(debug);
		ZZCell views = ZZDefaultSpace.findOnSystemlist(
		    view.getViewcell().getSpace(), "Views", false).s("d.1");
		
		p("Start program.");
		Data d = new Data(cview.getViewcell(),
				  view.getViewcell(), key);
		ZZCell frame = FlowingClang.start(prog, d);
		
		p("Create windows.");
		ZZCell frameview = ZZDefaultSpace.newToplevelView(
			prog.getSpace(), "Flowing stack frame",
			20, 20, 400, 500,
			views, debug.s("d.1"),
			frame, null,
			debug.s("d.1", 3).readRank("d.2", 1, true),
			ZZUtil.perturb(Color.blue.brighter()));
		ZZCursorReal.setColor(frameview, Color.blue.darker());

		ZZCell progview = ZZDefaultSpace.newToplevelView(
			prog.getSpace(), "Flowing program",
			440, 20, 400, 500,
			views, debug.s("d.1"),
			ZZCursorReal.get(frame), null,
			debug.s("d.1", 4).readRank("d.2", 1, true),
			ZZUtil.perturb(Color.red.brighter()));
		ZZCursorReal.setColor(progview, Color.red.darker());
		
		p("Connecting windows.");
		frameview.connect("d.ctrlview", 1, progview);
		ZZCell framecur = progview.N("d.2", 1);
		framecur.setText("Flowing.StackFrame");
		ZZCursorReal.set(framecur, frame);
		
		p("Update windows.");
		// ZZWindows.update(view.getViewcell().getSpace());
		ZZWindows.update();
	    } else if(id.indexOf("D-") > -1) {
		ZZCell framecur = ZZDefaultSpace.findInheritableParam(
		    view.getViewcell(), "Flowing.StackFrame");
		if(framecur == null)
		    throw new ZZError("Flowing Clang debugging command on a "
				    + "non-debug view");
		ZZCell frame = ZZCursorReal.get(framecur);
		StackFrameReal sf = new StackFrameReal(frame), nsf;
		
		if(id.equals("D-STEP"))
		    nsf = (StackFrameReal)FlowingClang.step(sf, true);
		else if(id.equals("D-JUMP")) {
		    nsf = (StackFrameReal)FlowingClang.jump(sf);
		} else
		    throw new ZZError("Unknown Flowing.XXX code");
		
		if(nsf != null) {
		    if(nsf != sf) {
		        ZZCursorReal.set(framecur, nsf.main);
			ZZCursorReal.set(cview.getViewcell(), nsf.main);
		    }
		    ZZCursorReal.set(view.getViewcell(), nsf.getPos());
		} else {
		    view.getViewcell().h("d.1").delete();
		    cview.getViewcell().h("d.1").delete();
		    // ZZWindows.update(view.getViewcell().getSpace());
		    ZZWindows.update();
		    ZZCursorReal.delete(view.getViewcell());
		    ZZCursorReal.delete(cview.getViewcell());
		}
	    } else
		throw new ZZError("Unknown Flowing.XXX code");
	}
    };

}

