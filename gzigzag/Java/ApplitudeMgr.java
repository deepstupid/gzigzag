/*   
ApplitudeMgr.java
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

package org.gzigzag;
import java.awt.*;
import java.util.*;

/** Routines related to applitude management.
 * This handles registering and identification of applitudes. An applitude
 * has a name and a number of params. Currently, the only used param is
 * the View param, which is simply a view (as a ZOb). That ought to change
 * into something more sophisticated...<br>
 * Applitudes are associated with dimensions. If a cell is connected on one
 * of an applitude's dimensions, it is said to be "in" that applitude.<br>
 * XXX need to make this faster!
 */

public class ApplitudeMgr {
public static final String rcsid = "$Id: ApplitudeMgr.java,v 1.11 2001/06/13 11:29:06 wikikr Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }
    
    private static ZZCell getDimCell(ZZCell c0) {
	ZZSpace s = c0.getSpace();
	ZZCell c = ZZDefaultSpace.findOnClientlist(s, "Applitudes", false);
	if(c == null) return putDefApps(s.getHomeCell());
	return c.getOrNewCell("d.1", 1);
    }

    /** Get a list of all the applitudes a cell is in. 
     */
    static public ZZCell[] getApplitudes(ZZCell c) {
	ZZCell d = getDimCell(c);
	ZZCell app = null;
	boolean added = false;
	Vector v = new Vector();
	for(d = d.s("d.2"); d != null; d = d.s("d.2")) {
	    if(d.s("d.1") != null) { app = d.s("d.1"); added = false; }
	    if(!added && (c.s(d.t()) != null || c.s(d.t(), -1) != null)) {
		v.addElement(app);
		added = true;
	    }
	}
	
	ZZCell[] res = new ZZCell[v.size()];
	for(int i=0; i<res.length; i++) res[i] = (ZZCell)v.elementAt(i);
	return res;
    }
    
    /** Get applitudes by names. Return all apps found 
     */
    static public ZZCell[] getApplitudesByName(String[] app_names, ZZCell c) {
	Vector v = new Vector();
	int i;
	ZZCell app_list = ApplitudeMgr.getDimCell(c).s("d.1");
	for(i=0; i<app_names.length; i++) {
	    v.add(app_list.findText("d.2", 1, app_names[i]));
	}
	i = v.size();
	ZZCell[] apps = new ZZCell[i];
	for(i--; i>=0; i--) {
	    apps[i] = (ZZCell)v.elementAt(i);
	}
	return apps;
    }

    private static void unregister(ZZCell d, String name) {
	ZZCell a = d.s("d.1");
	a = a.findText("d.2", 1, name);
	if(a == null) return;
	d = a.s("d.1", -1);
	for(ZZCell e = d.s("d.2"); e.s("d.1") == null; e = d.s("d.2"))
	    e.delete();
	d.delete();
	for(ZZCell c = a.s("d.clone"); c != null; c = a.s("d.clone"))
	    c.delete();
	a.delete();
    }

    /** Register a new applitude.
     * @param name The title of this applitude.
     * @param dims The dimensions to associate with this applitude.
     * @param view The id of the view for this applitude. (Currently,
     *             applitudes can have only one view.)
     * @param binds The bindings cell for this applitude. If null, a bindings
     *             cell without a connected list is created. Applitude
     *             bindings work just like normal bindings.
     * @returns The maincell of the applitude.
     */
    static public ZZCell register(ZZSpace sp, String name, String[] dims,
				  String view, ZZCell binds) {
	if(dims.length < 1)
	    throw new ZZError("Cannot register applitude without dims!");
		
	ZZCell home = sp.getHomeCell();
	ZZCell app = home.N();
	app.setText(name);
	ZZCell vc = app.N("d.1").N("d.2");
	vc.setText("View");
	vc.N("d.1").setText(name+" View");
	vc.s("d.1").N("d.1").setText(view);
	ZZCell bindc = vc.N("d.2");
	bindc.setText("Bindings");
	if(binds != null)
	    bindc.connect("d.1", 1, binds);
	else
	    bindc.N("d.1");

	ZZCell d = getDimCell(home);
	unregister(d, name);
	ZZCell a = d.s("d.1");
	a.insert("d.2", 1, app);
	d = d.N("d.2");
	d.setText(dims[0]);
	d.connect("d.1", 1, app);
	for(int i=1; i<dims.length; i++) {
	    d = d.N("d.2");
	    d.setText(dims[i]);
	}
	
	return app;
    }

    static public ZZCell putDefApps(ZZCell home) {
	ZZSpace sp = home.getSpace();
	ZZCell c = ZZDefaultSpace.findOnClientlist(sp, "Applitudes", true);

	ZZCell d = c.getOrNewCell("d.1", 1);
	d.setText("Dims");
	d.getOrNewCell("d.1", 1).setText("Applitudes");
	
	ZZCell zz = register(sp, "ZigZag", new String[] { "d.1", "d.2", "d.3" },
			     "VanishingView", null);
	ZZCursorReal.set(d.s("d.1"), zz);

	ZZCell nbinds = d.N();
	ZZCell nbc = nbinds.N("d.2"); nbc.setText("n");
	nbc.N("d.1").setText("Notemap.NEW");
	nbc = nbc.N("d.2"); nbc.setText("-");
	nbc.N("d.1").setText("Notemap.CNCT");
	register(sp, "Notemap", new String[] { "d.map-1", "d.map-2" },
		 "Notemap.Star", nbinds);
		
	return c;
    }

    /** Get the "Applitude" param cell of a window.
     * @param create Whether to create the param if it isn't there already.
     */
    private static ZZCell getWinAppCell(ZZCell win, boolean create) {
	ZZCell res = ZZDefaultSpace.findInheritableParam(win, "Applitude");
	if(res != null || !create) return res;
	res = win.N("d.2");
	res.setText("Applitude");
	return res;
    }

    /** Get the applitude associated with a window.
     * This reads the "Applitude" inheritable param of a window and returns
     * it (or null if it isn't set).
     */
    static public ZZCell getWindowApp(ZZCell win) {
	ZZCell c = getWinAppCell(win, false);
	if(c == null) return null;
	return ZZCursorReal.get(c);
    }

    /** Set the applitude associated with a window.
     * May be set to null. (ESC should do that.)
     */
    static public void setWindowApp(ZZCell win, ZZCell app) {
	ZZCell c = getWinAppCell(win, true);
	ZZCursorReal.set(c, app);
    }

    /** Get the "fallback" applitude used when no matching one is found. */
    static public ZZCell getFallback(ZZSpace sp) {
	return ZZCursorReal.get(getDimCell(sp.getHomeCell()).s("d.1"));
    }

    /** A convenience function combining getWindowApp and getApplitudes.
     * First, this calls getApplitudes() on the cell accursed by the window.
     * Then, it calls getWindowApp() to find out which applitude this is
     * associated to. It looks through the applitudes from the first call,
     * searching for this app. If it's found, it changes places with the first
     * app in the list, so that the 'prefered' applitude is always found at
     * index 0. If it's not found, setWindowApp() is called with the first
     * element of the array.
     * If the array returned by getApplitudes() is empty, the "safety net" 
     * applitude (ZigZag, showing the VanishingRaster) is returned.
     * @returns Never null, never less than one element. The first element is
     *          always equals to what getWindowApp() would return (after the
     *          call).
     * @deprecated Do it by hand. Don't set win app.
     */
    static public ZZCell[] getWindowApplitudes(ZZCell win) {
	pa("Call to deprecated getWindowApplitudes!");
		
	ZZCell[] apps = getApplitudes(ZZCursorReal.get(win));

	if(apps.length == 0) {
	     ZZCell c = getFallback(win.getSpace());
	     setWindowApp(win, c);
	     return new ZZCell[] { c };
	}
	
	ZZCell pref = getWindowApp(win);
	
	if(apps[0].equals(pref)) return apps;
	for(int i=1; i<apps.length; i++) {
	    if(apps[i].equals(pref)) {
		apps[i] = apps[0];
		apps[0] = pref;
		return apps;
	    } 
	}
	
	setWindowApp(win, apps[0]);
	return apps;
    }

    /** Get the flob view associated with an applitude.
     * Currently this just reads the View param, but later this may change
     * e.g. so that you can rotate through the views of an applitude.
     */
    static public FlobView getView(ZZCell app) {
	ZZCell vc = ZZDefaultSpace.findInheritableParam(app.s("d.1"), "View");
	if(vc != null) vc = vc.s("d.1");
	if(vc == null)
	    throw new ZZError("No view found for applitude "+app);
	ZOb zob = ZZDefaultSpace.readZOb(vc);
	if(zob != null && zob instanceof FlobView) {
	    return (FlobView)zob;
	} else {
	    ZZLogger.log("ApplitudeMgr.getView: invalid type: "+zob);
	    return new NullView();
	}
    }

    /** Change applitude of window according to clicked flob.
     * This is called by ZZKeyBindings1 whenever there was a click.
     */
    static public void clicked(ZZCell win, ZZScene xi, Point pt) {
	Flob[] fs = new Flob[1];
	((FlobSet)xi).getObjectAt(pt.x, pt.y, fs);
	Flob f = fs[0];
	if(f == null || f.applitude == null) return;
	setWindowApp(win, f.applitude);
    }

    /** Get the applitude bindings for this window.
     * This first checks the applitude currently prefered by this window,
     * then finds the bindings cursor for that applitude, then resolves
     * that cursor and returns the result.<br>
     * If the bindings cursor doesn't exist, it is created. If no applitude
     * is set or something else goes wrong, though, null is returned.
     */
    static public ZZCell getAppBindsForWin(ZZCell win) {
	ZZCell app = getWindowApp(win);
	if(app == null) return null;
	ZZCell binds = win.intersect("d.app-bind", 1, app, "d.clone", 1);
	ZZCell c = null;
	if(binds != null) c = ZZCursorReal.get(binds);
	if(c != null) return c;

	// CREATE
	c = ZZDefaultSpace.findInheritableParam(app.s("d.1"), "Bindings");
	if(c == null) return null;
	c = c.s("d.1");
	if(c == null) return null;
	if(binds == null) {
	    binds = app.zzclone();
	    win.insert("d.app-bind", 1, binds);
	}
	ZZCursorReal.set(binds, c);
	return c;
    }

    static public void setAppBindsForWin(ZZCell win, ZZCell to) {
	ZZCell app = getWindowApp(win);
	if(app == null) {
	    ZZLogger.log("Argh: Tried to set app binds for win without app");
	    return;
	}
	ZZCell curs = win.intersect("d.app-bind", 1, app, "d.clone", 1);
	if(curs == null) {
	    curs = app.zzclone();
	    win.insert("d.app-bind", 1, curs);
	}
	ZZCursorReal.set(curs, to);
    }

    /** Move to next or previous applitude for this window. */
    static public void rotateWindowApp(ZZCell win, int dir) {
	ZZCell app = getWindowApp(win);
	if(dir < 0) {
	    app = app.s("d.2", -1);
	    if(app.s("d.2", -1) == null) app = app.h("d.2", 1);
	} else {
	    if(app.s("d.2") != null) app = app.s("d.2");
	    else app = app.h("d.2").s("d.2");
	}
	setWindowApp(win, app);
	pa("Set win "+win+" to app "+app);
    }
}
