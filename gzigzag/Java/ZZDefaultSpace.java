/*   
ZZDefaultSpace.java
 *    
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
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
/*
 *	`He looks as though someone hit him on the head,' said Chidder.
 *	`No-one did, did they?'
 *	 	Arthur shook his head. Teppic's face was locked in a gentle
 *	grin. Whatever his eyes were focused on wasn't occupying the usual
 *	set of dimensions.
 *		Terry Pratchett, Pyramids, p.78
 */

package org.gzigzag;
import java.awt.*;
import java.util.*;

/** A "simple" class to create a default space starting at a cell.
 * NOT FINISHED. 
 * <p>
 * This class is kind of a center for things to be changed later.
 * It could be thought of as a bad design but the very point is that 
 * in order to find the change-sensitive places, you grep the source
 * for ZZDefaultSpace and find out what places do what.
 * <p>
 * It is not meant to remain here once things start settling down - probably.
 * Or possibly something like it will, defining a schema somewhere for things,
 * and other places just reference the schema - adaptive programming, anyone?
 * Actually, after we bootstrap, it'll definitely be gone.
 */

public class ZZDefaultSpace {
public static final String rcsid = "$Id: ZZDefaultSpace.java,v 1.146 2001/06/13 11:29:06 wikikr Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    public static final String dimListDimen = "d.2";

    public static final String sysList = "d.system";

    // XXX See if abstracting this out would make sense...
    static class ZP {
	    class StrCell {
		String name; ZZCell val;
		StrCell(String name, ZZCell val) { this.name = name; this.val = val; }
	    }
	    String name; 
	    String type;
	    Vector pars = new Vector();
	    ZP(String name, String type) { this.name = name; this.type = type; }
	    void ap(String name) {
		pars.addElement(new String[] {name});
	    }
	    void ap(String name, String val) {
		pars.addElement(new String[] {name, val});
	    }
	    void ap(String name, ZZCell val) {
		pars.addElement(new StrCell(name, val));
	    }
	    void ap(String name, String val, String val2) {
		pars.addElement(new String[] {name, val, val2});
	    }
	    void inh(ZZCell inhfrom) {
		pars.addElement(inhfrom);
	    }
	    ZZCell ins(ZZCell c) {
		ZZCell h = c.N("d.2", 1);
		ZZCell ret = h;
		h.setText(name); h = h.N("d.1", 1);
		h.setText(type); h = h.N("d.1", 1);
		for(Enumeration e = pars.elements();
		    e.hasMoreElements(); ) {
		    Object el = e.nextElement();
		    if(el instanceof String[]) {
			String[] a = (String[])el;
			h = h.N("d.2", 1);
			h.setText(a[0]);
			ZZCell cur = h;
			for(int i=1; i<a.length; i++) {
			    cur = cur.N("d.1", 1); cur.setText(a[i]);
			}
		    } else if(el instanceof StrCell) {
			StrCell sc = (StrCell)el;
			h = h.N("d.2", 1);
			h.setText(sc.name);
			h.connect("d.1", 1, sc.val);
		    } else {
			ZZCell inhfrom = (ZZCell)el;
			h = h.N("d.2", 1);
			inhfrom.h("d.3", 1).connect("d.3", 1, h);
		    }
		}
		return ret;
	    }
	}

    /**  Foo
     * @deprecated
     */
    static public void create(ZZCell root, boolean discold) {
	// Delete previous cursors
	ZZCell syscurs = root.findText(sysList,1,"SysCursors");
	if(syscurs != null) {
		ZZCell mycln = syscurs.s("d.1", 1);
		while(mycln != null) {
		    mycln.h("d.clone", -1)
			.excise("d.cursor");
		    mycln = mycln.s("d.2", 1);
		    if(mycln.s("d.1", -1) == syscurs)
			break;
		}
	} else {
		ZZCell clientcell1 = root.findText(
			sysList,1,"ClientCell");
		ZZCell orig = clientcell1;
		p("RMCC: "+clientcell1);
		while(clientcell1!=null) {
		    ZZCell myc = 
		     clientcell1.s("d.cursor-cargo",-1);
		    p("RMCC: "+clientcell1 + " "+myc);
		    if(myc!=null)
			myc.delete();
		    clientcell1=clientcell1.
			s("d.view",1);
		    if(clientcell1==orig) break;
		}
	}

	// Initialize
	ZZCell dl = root.getHomeCell();
	if(dl.getText().equals("")) dl.setText("Home");
	if(discold) {
	    dl.disconnect(sysList,1);
	    dl.disconnect("d.2",1);
	}
	
	ZZCell slstart = dl.N(sysList, 1);
	
	ZZDefaultSpace.create(slstart);
    }

    /** Creates a new system list for the space.
     * If an old system list exists, it is placed onto the d.2 list,
     * connected poswards from d.1, at cell "OldSysList".
     */
    static public void create(ZZCell root) { 

	ZZCell oldSys = null;
	if(root.s("d.system") != null) {
	    ZZCell onsys = root.s(sysList); 
	    root.disconnect(sysList, 1);
	    ZZCell on2 = root.s("d.2");
	    root.disconnect("d.2", 1);
	    ZZCell os = root.N("d.2");
	    os.setText("OldSysList");
	    oldSys = os.N("d.1");
	    if(on2 != null)
		oldSys.connect("d.2", on2);
	    if(onsys != null)
		oldSys.connect(sysList, onsys);
	}

	ZZCell d2last = root.N("d.2", 1);
	d2last.setText("Actions");
	ZZCell act = d2last.N("d.1", 1);
	act.setText("");
	ZZPrimitiveActions.putDefCmds(act);

	ZZCell vcur = root;
	
	/*
	 * Applitudes
	 */
	ZZCell apps = ApplitudeMgr.putDefApps(root);
	d2last.insert("d.2", 1, apps); d2last = apps;

	/*
	 * Bindings
	 */
	ZZCell bindings = vcur.N(sysList, 1);
	d2last.insert("d.2", -1, bindings);
	bindings.setText("Bindings");
	
	ZZCell editbinds = vcur.N(sysList, 1);
	d2last.insert("d.2", -1, editbinds); d2last = bindings;
	editbinds.setText("EditBindings");
	
	ZZPrimitiveActions.putDefBinds(bindings.N("d.1", 1), 
				       editbinds.N("d.1", 1));

	/*
	 * Views. Demo inheritance with depth.
	 * the views are cloned from the long list
	 * to the short. This makes it easy for the user
	 * also to clone and delete views from the active list.
	 * (once more, code written on a train... I think more
	 * than half of gzigzag has been coded on train trips
	 * but haven't kept count, unfortunately).
	 */
	// only on d.2, not systemlist
	ZZCell frasters = d2last = d2last.N("d.2", -1); 
			    // Before bindings, 
				    // but bindings have to be done before this code
				    // (it refers to them)
	frasters.setText("AllViews");
	
	ZZCell selfrasters = vcur = vcur.N(sysList, 1);
	frasters.insert("d.2", -1, selfrasters);
	selfrasters.setText("Views");
	{
	    ZZCell cur = frasters.N("d.1", 1);
	    ZZCell depth = cur.N("d.1", 1).N("d.2", 1);
	    depth.setText("depth"); depth.N("d.1", 1).setText("5");

	    ZP zp = new ZP("Vanishing", "VanishingView");
	    zp.inh(depth);
	    cur = zp.ins(cur);
	    // Make it a ring and insert negwards to get correct order.
	    selfrasters.insert("d.1", 1, cur.zzclone());
	    selfrasters = selfrasters.s("d.1", 1);
	    selfrasters.connect("d.2", 1, selfrasters);

	    zp = new ZP("StretchVanishing", "VanishingView");
	    zp.ap("varsize", "true");
	    zp.inh(depth);
	    cur = zp.ins(cur);
	    selfrasters.insert("d.2", -1, cur.zzclone());

	    zp = new ZP("WeirdVanishing", "VanishingView");
	    zp.ap("varsize", "true");
	    zp.ap("revper", "1.05");
	    zp.ap("gap", "1", "1");
	    zp.ap("halign", "-1");
	    zp.inh(depth);
	    cur = zp.ins(cur);
	    selfrasters.insert("d.2", -1, cur.zzclone());

	    zp = new ZP("Row", "RowColView");
	    zp.ap("row", "true");
	    cur = zp.ins(cur);
	    selfrasters.insert("d.2", -1, cur.zzclone());

	    zp = new ZP("Column", "RowColView");
	    zp.ap("row", "false");
	    cur = zp.ins(cur);
	    selfrasters.insert("d.2", -1, cur.zzclone());

	    zp = new ZP("Quad", "VanishingView");
	    zp.ap("centermul", "3", "5");
	    zp.inh(depth);
	    cur = zp.ins(cur);
	    selfrasters.insert("d.2", -1, cur.zzclone());

	    zp = new ZP("RankText", "RankTextView");
	    zp.ap("width", "250");
	    ZZCell binds = bindings.s("d.1", 2).zzclone();
	    zp.ap("databindings", binds);
	    binds = P(binds, "Left", "VSTRMCRSR -");
	    binds = P(binds, "Right", "VSTRMCRSR +");
	    binds = P(binds, "Enter", "VSTRMSPLIT");
	    binds = P(binds, "Ctrl-Left", "VSTRMJUMP -");
	    binds = P(binds, "Ctrl-Right", "VSTRMJUMP +");
	    cur = zp.ins(cur);

	    zp = new ZP("Clock", "ClockView");
	    zp.ap("allow_ellipse", "true");
	    zp.ap("show_flob_dim", "true");
	    cur = zp.ins(cur);
	    selfrasters.insert("d.2", -1, cur.zzclone());

	    zp = new ZP("VTree", "TreeView");
	    zp.ap("initmul", "1.6");
	    zp.ap("maxdepth", "4");
	    zp.ap("gap", "3", "6");
	    zp.ap("shrink", "0.7", "0.6");
	    zp.ap("treelines", "false");
	    zp.ap("depthhorizontal", "false");
	    cur = zp.ins(cur);

	    zp = new ZP("HTree", "TreeRaster");
	    zp.ap("initmul", "1.6");
	    zp.ap("maxdepth", "4");
	    zp.ap("gap", "3", "6");
	    zp.ap("shrink", "0.7", "0.6");
	    zp.ap("treelines", "false");
	    zp.ap("depthhorizontal", "true");
	    cur = zp.ins(cur);

	    zp = new ZP("VTree with lines", "TreeView");
	    zp.ap("initmul", "1.6");
	    zp.ap("maxdepth", "4");
	    zp.ap("gap", "3", "6");
	    zp.ap("shrink", "0.7", "0.6");
	    zp.ap("treelines", "true");
	    zp.ap("depthhorizontal", "false");
	    cur = zp.ins(cur);

	    zp = new ZP("HTree with lines", "TreeView");
	    zp.ap("initmul", "1.6");
	    zp.ap("maxdepth", "4");
	    zp.ap("gap", "3", "6");
	    zp.ap("shrink", "0.7", "0.6");
	    zp.ap("treelines", "true");
	    zp.ap("depthhorizontal", "true");
	    cur = zp.ins(cur);

	    zp = new ZP("Matrix", "ManyToManyView");
	    cur = zp.ins(cur);
	
	    zp = new ZP("Parallel text", "ParallelTextView");
	    cur = zp.ins(cur);

            zp = new ZP("Circuit", "ICView");
	    binds = bindings.s("d.1", 1).zzclone();
	    zp.ap("databindings", binds);
	    binds = P(binds, "MouseClicked1", "MOUSESETDIMOTHER X");
	    binds = P(binds, "MouseClicked3", "MOUSESETDIMOTHER Y");
	    binds = P(binds, "MouseClicked2", "MOUSESETDIMOTHER Z");
	    cur = zp.ins(cur);

            zp = new ZP("Compass", "CompassView");
            cur = zp.ins(cur);

	    zp = new ZP("Cursor", "CursorView");
	    cur = zp.ins(cur);

            zp = new ZP("Presentation", "Prez.R");
            cur = zp.ins(cur);

	    zp = new ZP("Notemap Star", "Notemap.Star");
	    binds = bindings.s("d.1", 1).zzclone();
	    zp.ap("databindings", binds);
	    binds = P(binds, "n", "Notemap.NEW");
	    binds = P(binds, "-", "Notemap.CNCT");
	    cur = zp.ins(cur);
	
	    zp = new ZP("Combined", "CombinedView");
	    zp.ap("appbindings");
	    binds = bindings.s("d.1", 1).zzclone();
	    zp.ap("databindings", binds);
	    binds = P(binds, "Ctrl-Q", "QUIT");
	    binds = P(binds, "Ctrl-S", "COMMIT");
	    binds = P(binds, "Ctrl-V", "RASTER +");
	    binds = P(binds, "Ctrl-Alt-V", "RASTER -");
	    binds = P(binds, "Ctrl-A", "APPLITUDE +");
	    binds = P(binds, "Ctrl-Alt-A", "APPLITUDE -");
	    cur = zp.ins(cur);

	    zp = new ZP("Multi", "MultiView");
	    zp.ap("appbindings");
	    binds = bindings.s("d.1", 1).zzclone();
	    zp.ap("databindings", binds);
	    binds = P(binds, "Ctrl-Q", "QUIT");
	    binds = P(binds, "Ctrl-S", "COMMIT");
	    binds = P(binds, "Ctrl-V", "RASTER +");
	    binds = P(binds, "Ctrl-Alt-V", "RASTER -");
	    binds = P(binds, "Ctrl-A", "APPLITUDE +");
	    binds = P(binds, "Ctrl-Alt-A", "APPLITUDE -");
	    cur = zp.ins(cur);

	    zp = new ZP("MultiFocus", "MultiFocusView");
	    zp.ap("appbindings");
	    binds = bindings.s("d.1", 1).zzclone();
	    zp.ap("databindings", binds);
	    binds = P(binds, "MouseMoved1", "MULTIFOCUSCHG");
	    cur = zp.ins(cur);



            zp = new ZP("Edit", "SimpleTextView");
            cur = zp.ins(cur);

	}

	/*
	 * CellViews
	 */
	ZZCell selffactories = vcur = vcur.N(sysList, 1);
	d2last.insert("d.2", 1, vcur); d2last = vcur;
	selffactories.setText("CellViews");
	ZZCell ffactories = vcur = vcur.N(sysList, 1);
	d2last.insert("d.2", 1, vcur); d2last = vcur;
	ffactories.setText("AllCellViews");
	{
	    ZZCell rcur = ffactories.N("d.1", 1);
	    ZZCell first = rcur;
	    rcur.setText("Standard flob factory"); 
	    {	// defaults
		ZZCell ac = rcur.N("d.1", 1);
		ac.setText("CellFlobFactory1");
		ac = ac.N("d.1", 1);
		ac = P(ac, "bg", "0xffffff");
	    }
	    selffactories.insert("d.1", 1, rcur.zzclone());
	    selffactories = selffactories.s("d.1", 1);
	    selffactories.connect("d.2", 1, selffactories);

	    ZZCell std = rcur;

	    rcur = rcur.N("d.2", 1);
	    rcur.setText("Ball&stick"); 
	    {	// defaults
		ZZCell ac = rcur.N("d.1", 1);
		ac.setText("CellFlobFactory1");
		ac = ac.N("d.1", 1);
		ac = P(ac, "ball", "true");
	    }
	    selffactories.insert("d.2", 1, rcur.zzclone());

	    rcur = rcur.N("d.2", 1);
	    rcur.setText("Non-enlarging"); 
	    {	// defaults
		ZZCell ac = rcur.N("d.1", 1);
		ac.setText("CellFlobFactory1");
		ac = ac.N("d.1", 1);
		ac = P(ac, "enlargefont", "false");
	    }
	    rcur = rcur.N("d.2", 1);
	    rcur.setText("Black background"); 
	    {	// defaults
		ZZCell ac = rcur.N("d.1", 1);
		ac.setText("CellFlobFactory1");
		ac = ac.N("d.1", 1);
		ac = P(ac, "bg", "0x0");
	    }
	    rcur = rcur.N("d.2", 1);
	    rcur.setText("Different font"); 
	    {	// defaults
		ZZCell ac = rcur.N("d.1", 1);
		ac.setText("CellFlobFactory1");
		ac = ac.N("d.1", 1);
		ac = P(ac, "font", "Serif", "20");
	    }
	    selffactories.insert("d.2", 1, rcur.zzclone());

	    rcur = rcur.N("d.2", 1);
	    rcur.setText("Multiline"); 
	    {	// defaults
		ZZCell ac = rcur.N("d.1", 1);
		ac.setText("FTextCellView");
		ac = ac.N("d.1", 1);
	    }
	    selffactories.insert("d.2", 1, rcur.zzclone());

	    rcur = rcur.N("d.2", 1);
	    rcur.setText("Idiosync"); 
	    {	// defaults
		ZZCell ac = rcur.N("d.1", 1);
		ac.setText("IdiosyncraticFlobFactory");
		ac = ac.N("d.1", 1);
		ac = P(ac, "def", std.zzclone());
	    }
	    selffactories.insert("d.2", 1, rcur.zzclone());


	}

	/*
	 * Dimension lists
	 */
	ZZCell dimLists = vcur = vcur.N(sysList, 1);
	d2last.insert("d.2", 1, vcur); d2last = vcur;
	dimLists.setText("DimLists");
	ZZCell dlcur = dimLists;

	String[][] dimarrs = new String[][] {
	    {"d.1", "d.2", "d.3", "d.system",
					/* "d.handle",  */
		    /* "d.cursor-cargo", "d.cursor-list", "d.cursor", */
		    /* "d.view", */
	     "d.clone", "d.cellcreation", 
	     /* "d.byfield", "d.headers", "d.slices",  */
	     "d.masterdim", "version:list", "version:home"
	     } /* , 
	    {"d.1", "d.2", "d.3", "d.system",
		"d.handle", 
	     "d.cursor-cargo", "d.cursor-list", "d.cursor",
	     "d.view", "d.clone", "d.photo", 
	     "d.mark", "d.mark-set","d.cellcreation",
	     "d.byfield", "d.headers", "d.slices",
	     "d.masterdim" }
	   */
	};
	for(int i=0; i<dimarrs.length; i++) {
	    dlcur = dlcur.N("d.1", 1);
	    ZZCell first = dlcur;
	    for(int j=0; j<dimarrs[i].length; j++) {
		    dlcur.setText(dimarrs[i][j]);
		    dlcur = dlcur.N("d.2", 1);
	    }
	    dlcur.connect("d.2", 1, first);
	    dlcur.delete(); // A good place to make sure
	    // delete works for all spaces ;)
	    dlcur = first;
	}
	p("FINAL DL: "+dimLists+" "+dimLists.s("d.1",1));



	/*
	 * Input - delete if you don't want to see the cell id.
	 */
	ZZCell input = vcur = bindings.N(sysList, 1);
	input.setText("Input");
	input.N("d.1", 1);

	/*
	 * Clientcell
	 */
	ZZCell client = vcur = vcur.N(sysList, 1);
	client.setText("ClientCell");

	/*
	 * System cursors list
	 */
	
	ZZCell syscurs = vcur = vcur.N(sysList, 1);
	syscurs.setText("SysCursors");

	// ZZUtil.dumpSubSpace(syscurs.getSpace().getHomeCell(),
	// 	    new String[] { "d.2", "d.system" });
	newViewPair(syscurs.getSpace());
    }

    static public void newViewPair(ZZSpace s) {

	/*
	 * The two default views.
	 * Suitable for 1024x768
	 */
	ZZCell dimlist = findDefaultDimlist(s.getHomeCell());

	ZZCell client = findOnSystemlist(s, "ClientCell", false);
	client = client.h("d.view", 1);

	ZZCell ctrlv = client.N("d.view", 1);
	ZZCell datav = ctrlv.N("d.view", 1);

	ctrlv.insert("d.ctrlview", 1, datav);

	ZZCell home = s.getHomeCell();
	ZZCell syscurs = findOnSystemlist(s, "SysCursors", false);
	ZZCell ctrlcur = newSysCursor(syscurs, home, 
		ZZUtil.perturb(Color.green));
	ZZCell datacur = newSysCursor(syscurs, home, 
		ZZUtil.perturb(new Color(150,150,255)));

	ZZCursorReal.attach(ctrlv, ctrlcur);
	ZZCursorReal.attach(datav, datacur);
	
	ZZCell defDims = findDefaultDimlist(s.getHomeCell());
	setDimsFromList(ctrlv, defDims, true);
	setDimsFromList(datav, defDims, true);

	defaultViewConnect(ctrlv);
	defaultViewConnect(datav);

	newCanvas(s, null, "Ctrl",
		  new Rectangle(0, 0, 512, 450),
		  ZZUtil.perturb(Color.orange), ctrlv);
	newCanvas(s, null, "Data",
		  new Rectangle(512, 0, 512, 450),
		  ZZUtil.perturb(Color.gray.brighter()), datav);

	
    }

    static private void defaultViewConnect(ZZCell vc) {
	ZZCell c = vc.N("d.2", 1);
	c.setText("View");
	ZZCursorReal.set(c, findDefaultFView(c.getHomeCell()));
	ZZCursorReal.setColor(c, ZZUtil.perturb(Color.red));
	c = c.N("d.2", 1);
	c.setText("CellView");
	ZZCursorReal.set(c, findDefaultFFactory(c.getHomeCell()));
	ZZCursorReal.setColor(c, ZZUtil.perturb(new Color(0xff8000)));
	c = c.N("d.2", 1);
	c.setText("ShowCursColor");
	c.N("d.1", 1).setText("false");
	c = c.N("d.2", 1);
	c.setText("CursorRecovery");
	ZZCursorReal.attach(c, vc);
    }

    static public void setDimsFromList(ZZCell vc, ZZCell dimlist,
	    boolean setcols) {
	int n = 3;
	ZZCell dc = vc;
	for(int i=0; i<n; i++) {
	    dc = dc.getOrNewCell("d.dims", 1);
	    ZZCursorReal.set(dc, dimlist);
	    if(setcols) ZZCursorReal.setColor(dc, ZZUtil.perturb(Color.pink));
	    dimlist = dimlist.s("d.2", 1);
	}
    }

    /** Create a new cursor and clone it to the system cursor list.
     * Return the cursor cell.
     */
    static public ZZCell newSysCursor(ZZCell syscurs, ZZCell c, Color co) {
	ZZCell curcln;
	if(syscurs.s("d.1", 1) == null) {
	    curcln = syscurs.N("d.1", 1);
	    curcln.connect("d.2", 1, curcln);
	} else
	    curcln = syscurs.s("d.1", 1).N("d.2", -1);
	
	ZZCell cur = ZZCursorReal.create(c);
	ZZCursorReal.setColor(cur, co);
	cur.insert("d.clone", 1, curcln);
	return cur;
    }


    static private ZZCell P(ZZCell c, String s) {
	c = c.N("d.2", 1);
	c.setText(s);
	return c;
    }

    static private ZZCell P(ZZCell c, String s, ZZCell par) {
	c = c.N("d.2", 1);
	c.setText(s);
	c.connect("d.1", 1, par);
	return c;
    }

    static private ZZCell P(ZZCell c, String s, String v1) {
	c = c.N("d.2", 1);
	c.setText(s);
	ZZCell a = c.N("d.1", 1);
	a.setText(v1);
	return c;
    }
    static private ZZCell P(ZZCell c, String s, String v1, String v2) {
	c = c.N("d.2", 1);
	c.setText(s);
	ZZCell a = c.N("d.1", 1);
	a.setText(v1);
	a = a.N("d.1", 1);
	a.setText(v2);
	return c;
    }
    static private ZZCell P(ZZCell c, String s, String v1, String v2, String v3) {
	c = c.N("d.2", 1);
	c.setText(s);
	ZZCell a = c.N("d.1", 1);
	a.setText(v1);
	a = a.N("d.1", 1);
	a.setText(v2);
        a = a.N("d.1", 1);
	a.setText(v3);
	return c;
    }

    static public ZZCell findDefaultDimlist(ZZCell root) {
	return root.findText("d.2", 1, "DimLists").s("d.1", 1);
    }
    static public ZZCell findDefaultFView(ZZCell root) {
	return root.findText("d.2", 1, "Views").s("d.1", 1);
    }
    static public ZZCell findDefaultFFactory(ZZCell root) {
	return root.findText("d.2", 1, "CellViews").s("d.1", 1);
    }

    static public ZZCell findDefaultViewInherit(ZZCell view) {
	    return view.getSpace().getHomeCell().findText("d.2", 1, "ViewParams").s("d.1", 1);
    }
    static public ZZCell findCursorForView(ZZCell view) {
	    return ZZCursorReal.get(view);
    }

    /** Add a dimension to the default dimension list of the space.
     * Should be used only by modules undergoing testing.
     * To be subsumed by the applitude mechanism.
     */
    static public void addDimensionToDefaultDimlist(ZZSpace sp, String dim) {
	ZZCell d = findDefaultDimlist(sp.getHomeCell());
	p("D: " + d);
	// check if dim already exists
	if ( d.findText("d.2", 1, dim) != null ) return;
	d = d.N("d.2", -1);
	d.setText(dim);
    }

    // Nowadays use cursor
    static public ZZCell getDimFromCell(ZZCell dc) {
	return ZZCursorReal.get(dc);
    }
    static public void setDimFromCell(ZZCell dc, ZZCell dim) {
	ZZCursorReal.set(dc, dim);
    }

    static public void setDimFromCell_sever(ZZCell dc, ZZCell dim) {
	dc.excise("d.cursor-cargo");
	ZZCursorReal.setcargo(dc, dim);
    }
    static public void setDims(ZZCell dc, ZZCell[] dims) {
	for(int i=0; i<dims.length; i++) {
	    dc = dc.getOrNewCell("d.dims", 1);
	    ZZCursorReal.set(dc, dims[i]);
	}
    }

    static public String[] getDimList(ZZCell vc) {
	ZZCell n = vc.s("d.dims", 1);
	int i;
	for(i=0; n!=null; n=n.s("d.dims", 1)) i++;
	String[]res = new String[i];
	n = vc.s("d.dims", 1);
	for(i=0; n!=null; n=n.s("d.dims", 1)) 
	    res[i++] = ZZCursorReal.get(n).getText();
	return res;
    }

    static public ZZCell[] getMark(ZZCell vc) {
	Vector v = new Vector();
	ZZCell cur = vc;
	while(true) {
		// cur.addObs(oMark);
		cur = cur.s("d.mark", 1);
		if(cur==null) break;
		// XXX POTENTIAL BUG: SHOULD ALSO OBSERVE
		ZZCell mk = cur.h("d.mark-set", -1);
		v.addElement(mk);
	}
	ZZCell[] r = new ZZCell[v.size()];
	for(int i=0; i<r.length; i++)
		r[i] = (ZZCell)v.elementAt(i);
	return r;
    }

    static public void clearMark(ZZCell vc) {
	ZZCell cur = vc;
	cur = cur.s("d.mark", 1);
	ZZCell prev;
	while(cur!=null) {
		prev = cur;
		cur = cur.s("d.mark", 1);
		prev.delete();
	}
    }

    /** Returns the same cell, possibly dereferenced.
     * One of the important design issues with Clang and
     * the clang-path subset is that a cell may be either 
     * something directly (or cloned) or a pointer (via d.cursor-cargo
     * and d.cursor). 
     * <p>
     * This function should be used to access those cells 
     * so that the exact mechanism (dimensions etc.) may be later changed.
     * <p>
     * The return value may be null in the case that there is a
     * d.cursor-cargo attached to the cell
     */
    static public ZZCell getPossDerefCell(ZZCell c) {
	    ZZCell cur = c;
	    // XXX LoopDetector
	    while(true) {
		ZZCell m = ZZCursorReal.get(c);
		if(m==null) return cur;
		cur = m;
	    }
    }

    // XXX NOTE: THIS GOES NEGWARDS - SPACE <-> SCROLL MAPPING
    // MUST BE SAVED BETWEEN RESTARTS
    static public ZZCell findScrollCell(ZZCell root, String id) {
	    ZZCell c = root;
	    c = c.findText("d.2", -1, "Scrolls");
	    if(c==null) throw new ZZError("No scrolls!");
	    c = c.findText("d.1", 1, id);
	    return c;
    }

    // Currently: go along 
    static public ZZCell findInheritableParam(ZZCell start, String id) {
	ZZCell n = start;
	while(n!=null) {
	    if(n.getText().equals(id)) return n;
	    ZZCell h; h = n.h("d.3", -1);
	    if(h!=null && h!=n) {
		h = findInheritableParam(h, id);
		if(h!=null) return h;
	    }
	    n = n.s("d.2", 1);
	}
	return null;
    }

    static public Font findInheritableParamFont(ZZCell start, String id,
	Font def) {
	ZZCell fo = findInheritableParam(start, id);
	if(fo!=null) {
	    fo = fo.s("d.1",1);
	    String name = fo.getText();
	    fo = fo.s("d.1",1);
	    int size = Integer.parseInt(fo.getText());
	    return new Font(name,Font.PLAIN,size);
	}
	return def;
    }

    static public float[] paramFloatA(ZZCell start, String id, float[] def,
	    int nmin) {
	ZZCell p = findInheritableParam(start, id);
	if(p==null) return def;
	ZZCell[] r = p.readRank("d.1", 1, false);
	if(r.length < nmin) return def;
	float[] arr = new float[r.length];
	for(int i=0; i<r.length; i++) {
	    try {
		arr[i] = (Float.valueOf(r[i].getText())).floatValue();
	    } catch(NumberFormatException e) {
		p("Invalid number!");
	    }
	}
	return arr;
    }
    static public int[] paramIntA(ZZCell start, String id, int[] def,
	    int nmin) {
	ZZCell p = findInheritableParam(start, id);
	if(p==null) return def;
	ZZCell[] r = p.readRank("d.1", 1, false);
	if(r.length < nmin) return def;
	int[] arr = new int[r.length];
	for(int i=0; i<r.length; i++) {
	    try {
		arr[i] = Integer.parseInt(r[i].getText());
	    } catch(NumberFormatException e) {
		p("Invalid number!");
	    }
	}
	return arr;
    }

    /** Finds the given name on the system list of a space.
     * If create is set, creates the cell if it doesn't exist.
     */
    static public ZZCell findOnSystemlist(ZZSpace s, 
					String id, boolean create) {
	ZZCell c = s.getHomeCell();
	c = c.findText(sysList, 1, id);
	if(c==null && create) {
	    p("Create on system list: '"+id+"'");
	    c = s.getHomeCell().h(sysList, 1).N(sysList, 1);
	    c.setText(id);
	}
	return c;
    }

    /** Finds the given name on the system list of a space.
     * If create is set, creates the cell if it doesn't exist.
     */
    static public ZZCell findOnClientlist(ZZSpace s, 
					String id, boolean create) {
	return findOnSystemlist(s,id,create);
    }

    public static void storeDirActionWaiting(ZZCell c, String id) {
	ZZCell nac = findOnClientlist(c.getSpace(), "NextAction", true);
	nac = nac.getOrNewCell("d.1", 1, null);
	nac.setText(id);
    }

    static void storeDirWaiting(ZZCell c, String dim, int dir) {
	ZZCell nac = findOnClientlist(c.getSpace(), "NextAction", true);
	nac = nac.getOrNewCell("d.1", 1, null);
	nac = nac.getOrNewCell("d.1", 1, null);
	nac.setText(dim);
	nac = nac.getOrNewCell("d.1", 1, null);
	nac.setText(dir < 0 ? "-" : "+");
    }

    static String[] getDirActionWaitingAndClear(ZZCell c) {
	ZZCell nac = findOnClientlist(c.getSpace(), "NextAction", true);
	nac = nac.getOrNewCell("d.1", 1, null);
	String s = nac.getText();
	if(s.equals("")) return null;
	String[] res = new String[3];
	res[0] = s;
	nac.setText("");
	
	nac = nac.getOrNewCell("d.1", 1, null);
	res[1] = nac.getText();
	nac.setText("");
	nac = nac.getOrNewCell("d.1", 1, null);
	res[2] = nac.getText();
	nac.setText("");

	return res;
    }
    // XXX Should create a ZZWindow class for this?
    public static void newCanvas(ZZSpace s,ZZCell parent,String id,
				 Rectangle bounds,Color bg,ZZCell viewcell) {
	if(parent==null)
	    parent=findOnClientlist(s,"Windows",true).getOrNewCell("d.1",1);
	ZZCell c = parent.N("d.2",1);
	if(bg!=null) c.N("d.color",1).setText(String.valueOf(bg.getRGB()));
	c.setText(id);
	c.insert("d.1",1,viewcell);
	c=c.N("d.1",1);
	c.setText("Canvas");
	ZZWindows.setBounds(c,bounds);
    }
    static public ZZCell newToplevelView(ZZSpace s, String id, 
	    int x, int y, int w, int h,
	    ZZCell flobRasterList, ZZCell bindings, 
	    ZZCell cursorCell, ZZCell cursorCargo,
	    ZZCell[] dims,
	    Color bg) {

	ZZCell cc = findOnSystemlist(s, "ClientCell", false);
	ZZCell vc = cc.h("d.view", 1).N("d.view",1 );
	vc.setText(id);
	if(cursorCargo != null) {
	    cursorCargo.insert("d.cursor-cargo", 1, vc);
	} else {
	    vc.N("d.cursor-cargo", -1);
	    ZZCursorReal.set(vc, (cursorCell == null ? s.getHomeCell() :
				  cursorCell));
	}

	ZZCell frl = vc.N("d.2", 1);
	frl.setText("View");
	ZZCursorReal.set(frl, flobRasterList);

	ZZCell c = frl.N("d.2", 1);
	c.setText("CellView");
	ZZCursorReal.set(c, findDefaultFFactory(c.getHomeCell()));
	ZZCursorReal.setColor(c, ZZUtil.perturb(new Color(0xff8000)));

	if(bindings != null) {
	    ZZCell bc = vc.N("d.bind", 1);
	    ZZCursorReal.set(bc, bindings);
	}
	if(dims == null) {
	    dims = new ZZCell[3];
	    ZZCell n = findDefaultDimlist(s.getHomeCell());
	    for(int i=0; i<dims.length; i++) {
		dims[i] = n; n = n.s("d.2", 1);
	    }
	}
	if(dims != null) setDims(vc, dims);

	newCanvas(s, null, id, new Rectangle(x, y, w, h), bg, vc);
	return vc;
    }

    /** Construct and return a ZOb of the given type.
     */
    public static ZOb newZOb(String id) {
    // XXX Figure out if we can cache something.
    // XXX Should ZOb be able to reset? Easier or less easy than
    // recreate? At least ZObs should cache...
	int ind = id.indexOf(".");
	if(ind == -1) {
	    try {
		return (ZOb)Class.forName("org.gzigzag."+id).newInstance();
	    } catch(Exception e) {
		ZZLogger.exc(e);
		return null;
	    }
	} else {
	    // XXX if more than one, use Java class directly, without
	    // package.
	    ZZModule m = ZZModule.getModule(id.substring(0, ind));
		// XXX Should be getZOb
	    ZOb fr = m.newZOb(id.substring(ind+1));
	    if(fr == null)
		pa("Warning: module ZOb '"+id+"' not found.");
	    return fr;
	}
    }

    /** Construct a ZOb corresponding to a cell and read the params.
     * Goes to the rootclone, takes the next cell on d.1 as the ZOb's name,
     * and takes the NEXT cell on d.1 as the head of the list from which to
     * read the structparams.
     */
    public static ZOb readZOb(ZZCell start) {
	ZZCell c = start.h("d.clone", -1).s("d.1", 1);
	if(c == null)
	    throw new ZZError("Error reading ZOb from cell "+start.getText());

	ZOb z = newZOb(c.getText());
	z.readParams(c.s("d.1", 1));
	return z;
    }


    static public void mkCorner(ZZCell c, String[][] s) {
	for(int i=0; i<s.length; i++) {
	    c = c.N("d.2", 1);
	    ZZCell d = c; d.setText(s[i][0]);
	    for(int j=1; j<s[i].length; j++) 
		(d = d.N("d.1", 1)).setText(s[i][j]);
	}
    }

    static private ZZCell findInbufCell(ZZSpace s) {
	ZZCell c = findOnSystemlist(s, "Input", false);
	if( c == null ) return null;
	return c.getOrNewCell("d.1", 1);
    }
    static public String getInbuf(ZZSpace s, boolean clear) {
	ZZCell c = findOnSystemlist(s, "Input", false);
	if(c == null) return null;
	c = c.getOrNewCell("d.1", 1);
	String str = c.getText();
	if(clear) c.setText("");
	return str;
    }
    /** Ugly hack: null backspaces.
     */
    static public void appendInbuf(ZZSpace s, String str) {
	ZZCell c = findInbufCell(s);
	if ( c == null ) return;
	if(str==null) {
	    str = c.getText();
	    if ( str.length() > 0 ) 
	    str = str.substring(0, str.length()-1);
	    c.setText(str);
	} else
	    c.setText(c.getText() + str);
    }

    static public ZZScene getScene(ZZCell view, Component forc) {
	    p("getScene: startflob");
	    Dimension size = forc.getSize();
	    FlobSet fs = new FlobSet(size, forc.getForeground(),
				    forc.getBackground(), forc);
	    ZZCell c = findInheritableParam(view, "View");
	    ZZCell rastname = ZZCursorReal.get(c);

	    ZOb frz = readZOb(rastname);
	    FlobView fr = null;
	    if(frz != null && frz instanceof FlobView) {
		fr = (FlobView)frz ;
	    } else {
		System.out.println("getScene: invalid type: "+frz);
		fr = new NullView();
	    }
	    // XXX Signal error

	    c = findInheritableParam(view, "CellView");

	    FlobFactory rff = null;
	    if(c != null) {
		ZZCell cfact = ZZCursorReal.get(c);

		ZOb ffzob = readZOb(cfact);
		rff = (FlobFactory) ffzob;
	    }

	    p("getScene: raster");
	    fr.raster(fs, rff, view, getDimList(view), ZZCursorReal.get(view));
	    p("getScene: rastered");

	    if(rff != null) {
		Flob rn = rff.placeFlob(fs, rastname, rastname, (float)1.5, size.width, 0, 1, 1, 0);
		rn.flobPath="rastname";
	    }
	    return fs;
    }

    // static void newScroll(ZZCell root, String id, String addr) {
    // }

    static public String getLang(ZZCell window) {
	ZZCell c = findInheritableParam(window, "Language");
	if(c == null) return "en";
	c = c.s("d.1");
	if(c == null) return "en";
	if(c.getText().length() < 2) return "en";
	return c.getText();
    }
}
