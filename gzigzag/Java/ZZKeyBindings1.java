/*   
ZZKeyBindings1.java
 *    
 *    Copyright (c) 1999, Ted Nelson and Tuomas Lukka
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
import java.awt.event.*;

/** The first meaningful keybindings class.
 * A stateless class which looks along d.2 for a string matching 
 * the result of the getKeyText method of the java.awt.event.KeyEvent class.
 */

public class ZZKeyBindings1 implements ZZKeyBindings {
public static final String rcsid = "$Id: ZZKeyBindings1.java,v 1.64 2001/06/09 09:41:10 wikikr Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }
    static final void pa(String s) { ZZLogger.log(s); }

    /** Create ZZKeyBindings1.
        @deprecated Use the noarg constructor instead. */
    ZZKeyBindings1(ZZExec x) { this(); }
    ZZKeyBindings1() {}
   
    Point origpt;
    boolean dragging = false;
    Object mouseLastLocated = null;

    public void perform(InputEvent ie, ZZView v, ZZView ctrlv, ZZScene xi) {
	ZZCell vcell = v.getViewcell();

	String t = null;
	p(" "+ie);
	if(ie instanceof KeyEvent) {
	    /*
	     * This is quite complicated - more complicated than it
	     * should be but Java seems to screw up key events quite
	     * nicely for us...
	     * We want to have Shift-Alt-X, for example, which on some
	     * Javas needs quite a bit of attention.
	     */
	    KeyEvent k = (KeyEvent) ie;
	    char c= k.getKeyChar();
	    int kc = k.getKeyCode();
	    String kt = KeyEvent.getKeyText(kc);
	    String kbychar = new String(new char[] {c});
	    p("Have: "+c+" "+kc+" '"+kt+"' '"+kbychar+"'");
	    if((c == k.CHAR_UNDEFINED  
		// k.CHAR_UNDEFINED changed between JDK1.1 and 1.2
		|| c == 0x0 || c == 0xFFFF // needed for cross-compiling
                || kc == k.VK_DELETE 
		|| kc == k.VK_BACK_SPACE)
		&& kc != 0)
		    t = kt;
	    else
		    t = kbychar;
	    p("Chose: "+t);
	    if(t.equals("\n")) t = "Enter";
	    if(t.equals("\t")) t = "Tab";
	    if(t.equals("\033")) t = "Esc";
	    if(t.equals("") ||
		Character.isISOControl(t.charAt(0)) ||
		k.isAltDown() || k.isControlDown() ||
		kt.equals("Left") || // These need shift.
		kt.equals("Right") ||
		kt.equals("Up") ||
		kt.equals("Down")
		) {
		    p("Maybe rechoosing");
		    if(kc != 0)
			t = kt;
		    if(k.isShiftDown()) t = "Shift-"+t;
	    }
	    if(k.isAltDown()) t = "Alt-"+t;
	    if(k.isControlDown()) t = "Ctrl-"+t;

	    p("KEYTEXT: '" + t + "', was by kt: '"+kt+"' and by char: '"
		    +kbychar+"'");
	    if(t==null) return;
	    perf(t, null, v, ctrlv, t, null, xi);
	} else if(ie instanceof MouseEvent) {
	    MouseEvent m = ZZKeyHacks.mouseEventHack((MouseEvent)ie);
	    p("MouseEvent after ZZKeyHacks: "+m);
	    String modstr = "";

	    if(m.isAltDown()) modstr = "Alt-"+modstr;
	    if(m.isControlDown()) modstr = "Ctrl-"+modstr;
	    if(m.isShiftDown()) modstr = "Shift-"+modstr;

	    Point pt = new Point(m.getX(), m.getY());
	    String mec = null;
	    ZZCell targ = null;
	    if(m.getID() == m.MOUSE_CLICKED) {
		mec = "Clicked";
		dragging = false;
		// If it was a cell, give it as the target.
		// XXX Need we really do this here?
		Object ob = xi.getObjectAt(m.getX(), m.getY());
		if(ob instanceof ZZCell) targ = (ZZCell) ob;
		
		// Call applitude manager, to change app if necessary
		// (Actually, just realized: because we do this here, before
		//  we call perf(), the applitude bindings of the new
		//  applitude are used, which is exactly what we want!)
		ApplitudeMgr.clicked(vcell, xi, pt);
	    } else if(m.getID() == m.MOUSE_PRESSED) {
		mec = "Pressed";
		origpt = pt;
		dragging = false;
	    } else if(m.getID() == m.MOUSE_DRAGGED) {
		mec = "Dragged";
		if(!dragging) {
		    perf(modstr+"MouseStartDrag", null, v, ctrlv, null, origpt, xi);
		    dragging = true;
		}
	    } else if(m.getID() == m.MOUSE_RELEASED) {
		mec = "Released";
		dragging = false;
		// XXX EndDrag?
	    } else if(m.getID() == m.MOUSE_MOVED) {
                mec = "Moved";
		// MOUSE_MOVED event is dispatched further if
		// cursor is pointing at a new object
		Object ob = xi.getObjectAt(m.getX(), m.getY());
		if(ob instanceof ZZCursor) { 
		    ob = ((ZZCursor)ob).get(); 
		}  
		if(mouseLastLocated == ob) return;
		mouseLastLocated = ob;
		dragging = false;
	    } else return;
	    int but = 0;
	    int mod = m.getModifiers();
	    if((mod & m.BUTTON1_MASK) != 0) but = 1;
	    else if((mod & m.BUTTON2_MASK) != 0) but = 2;
	    else if((mod & m.BUTTON3_MASK) != 0) but = 3;

	    t = modstr+"Mouse"+mec+but;

	    p("MOUSE: '"+t+"': event was "+m);

	    perf(t, targ, v, ctrlv, null, pt, xi);

	}
    }

    /** Get the key bindings mode cursor associated with this window.
     *  Does currently <em>not</em> create a new one if there is none.
     *  Instead, returns null.
     */
    public static ZZCell getModeCursor(ZZCell window) {
	return window.h("d.bind", 1, true);
    }

    /** Get the key bindings mode cell associated with this window.
     *  Set to default mode if no mode is there.
     */
    public static ZZCell getMode(ZZCell window) {
	ZZCell c0 = getModeCursor(window);
	if(c0 == null) {
	    p("NO BIND YET: SET CURS");
	    c0 = window.N("d.bind", 1);
	    ZZCell cur0 = ZZDefaultSpace.findOnSystemlist(
		    window.getSpace(), "Bindings", false).s("d.1", 1);
	    ZZCursorReal.set(c0, cur0);
	    ZZCursorReal.setColor(c0, new Color(0xaf5a00));
	}
	return ZZCursorReal.get(c0); 
    }

    void perf(String id, ZZCell target,
		ZZView v, ZZView ctrl, String key, Point pt, ZZScene xi) {
	p("PERF: '"+id+"'");
	ZZCell vcell = v.getViewcell();
	ZZCell accursed = ZZCursorReal.get(vcell);
	ZZCell cvcell = ctrl.getViewcell();
	ZZCell cur = getMode(vcell);
	p("BIND CURS: "+cur+" "+(cur!=null?cur.getID():null));
	
	/** Use applitude bindings?
	 * As not all views show applitude data, views which do have to
	 * have an "appbindings" structparam to declare the bindings of the
	 * applitude currently prefered by this window shall be used.
	 */
	boolean useAppBinds = false;
	
	/** Is the binding found an applitude binding?
	 * This affects how the bindings mode is changed.
	 */
	boolean isAppBind = false;
	
	ZZCell raster = 
	    ZZDefaultSpace.findInheritableParam(vcell, "View");
	if(raster != null) raster = ZZCursorReal.get(raster);
	if(raster != null) raster = raster.h("d.clone", -1);
	if(raster != null) raster = raster.s("d.1", 2);
	if(ZZDefaultSpace.findInheritableParam(raster, "appbindings") != null)
	    useAppBinds = true;
	if(raster != null) raster = 
	    ZZDefaultSpace.findInheritableParam(raster, "databindings");
	if(raster != null) raster = raster.intersect("d.1",1,cur,"d.clone",1);

	ZZCell craster = 
	    ZZDefaultSpace.findInheritableParam(cvcell, "View");
	if(craster != null) craster = ZZCursorReal.get(craster);
	if(craster != null) craster = craster.h("d.clone", -1);
	if(craster != null) craster = craster.s("d.1", 2);
	if(craster != null) craster = 
	    ZZDefaultSpace.findInheritableParam(craster, "ctrlbindings");
	if(craster != null) craster = craster.intersect(
	    "d.1", 1, cur, "d.clone", 1);

	ZZCell binding = null;
	if(raster != null) {
	    binding = ZZDefaultSpace.findInheritableParam(raster, id);
	    p("Tried by view");
	}
	if(binding == null && useAppBinds) {
	    ZZCell app = ApplitudeMgr.getAppBindsForWin(vcell);
	    binding = ZZDefaultSpace.findInheritableParam(app, id);
	    p("Tried by applitude");
	    if(binding == null && key != null && key.length() > 0) {
		binding = ZZDefaultSpace.findInheritableParam(app, "INSERT");
		p("Tried by applitude INSERT");
	    }
	    if(binding == null) {
		binding = ZZDefaultSpace.findInheritableParam(app, "DEFAULT");
		p("Tried by applitude DEFAULT");
	    }
	    if(binding != null)
		isAppBind = true;
	}
	if(binding == null && craster != null) {
	    binding = ZZDefaultSpace.findInheritableParam(craster, id);
	    p("Tried by ctrl view");
	}
	if(binding == null) {
	    binding = ZZDefaultSpace.findInheritableParam(cur, id);
	    p("Tried by cur");
	}
	if(binding == null && key != null && key.length() == 1) {
	    binding = ZZDefaultSpace.findInheritableParam(cur, "INSERT");
	}
	if(binding == null) {
	    binding = ZZDefaultSpace.findInheritableParam(cur, "DEFAULT");
	    p("Tried by DEFAULT");
	}
	if(binding == null && 
	   ZZDefaultSpace.findInheritableParam(cur, "EDITBINDS") != null) {
	    ZZCell cellview = accursed.h("d.cellview", true), ecur = null;
	    p("cellview: "+cellview);
	    if(cellview != null)
		cellview = cellview.s("d.1", 2);
	    if(cellview != null)
		cellview = ZZDefaultSpace.findInheritableParam(cellview, "EditBindings");
	    if(cellview != null)
		ecur = cellview.s("d.1", 1); 
	    p("editbinds cur: "+ecur);
	    if(ecur == null)
		ecur = ZZDefaultSpace.findOnSystemlist(vcell.getSpace(), 
		    "EditBindings", true).getOrNewCell("d.1", 1);
	    if(ecur != null) {
		binding = ZZDefaultSpace.findInheritableParam(ecur, id);
		p("Tried by editbinds");
		if(binding == null && key != null && key.length() == 1) {
		    binding = ZZDefaultSpace.findInheritableParam(ecur, "INSERT");
		    p("Tried by editbinds INSERT");
		}
		if(binding == null) {
		    binding = ZZDefaultSpace.findInheritableParam(ecur, "DEFAULT");
		    p("Tried by editbinds DEFAULT");
		}
	    }
	}

	p("BINDING: "+binding);
	if(binding != null) {
	    ZZCell bc = binding.s("d.1", 1);
	    p("Bind command cell: "+bc);
	    if(bc == null) {
		    System.out.println("AUGH! NO BINDING");
		    return;
	    }
	    ZZCell nextstate = bc.h("d.3", -1, true);
	    if(nextstate != null) {
		if(!isAppBind)
		    ZZCursorReal.set(getModeCursor(vcell), nextstate);
		else
		    ApplitudeMgr.setAppBindsForWin(vcell, nextstate);
	    }
	    ZZCommand comm = ZZCommand.getCommand(bc);
	    if(comm != null)
		comm.execCallback(
			target,
			v, ctrl,
			key, pt, xi
			);
	    else
	        new ZZPrimitiveActions().execCallback(
			bc, 
			target,
			v, ctrl,
			key, pt, xi
			);
	    return;
	} else if (key != null && key.equals("Esc")) {
            v.getViewcell().getSpace().undo();
        }
    }
}

