/*   
ZZPrimitiveActions.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson, Tuomas Lukka and Vesa Parkkinen 
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
import java.awt.*;

/** A really simple mock script engine. 
 * This class is intended for single, fixed commands that are
 * in reality coded in Java in this file. It is only here so that the Java
 * client can be used standalone.
 * <p>
 * Even worse, it is string-based: it doesn't hassle you about structure,
 * it simply takes the string in the cell and executes a corresponding command.
 * <p>
 * The one nifty thing is the module support: 
 * Given a function name like ZZMbox.FOO
 * it will first dynamically load the class org.gzigzag.module.ZZMbox
 * (this will later be probably xanadu.zz.module), and run its action
 * routine (see ZZModule and ZZMbox docs for details).
 */

public class ZZPrimitiveActions implements ZZExec {
public static final String rcsid = "$Id: ZZPrimitiveActions.java,v 1.147 2001/06/13 11:29:06 wikikr Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }
    static final void pa(String s) { ZZLogger.log(s); }
    public ZZPrimitiveActions() {}

    public static java.io.PrintWriter textlog;

    ZZCell findPar(ZZCell view, String param) {
	    ZZCell cur = view.s("d.2", 1);
	    while(cur!=null) {
		    if(cur.getText().equals(param))
			    return cur.s("d.1", 1);
		    cur = cur.s("d.2", 1);
	    }
	    return null;
    }
    int getDimstrAsInt(String param) {
	    if(param.equals("X")) return 0;
	    else if(param.equals("Y")) return 1;
	    else if(param.equals("Z")) return 2;
	    return -1;
    }
    String getDim(ZZCell view, String param) {
	    ZZCell c = findPar(view, param);
	    if(c!=null) 
		return c.getText();
	    int ind=getDimstrAsInt(param); 

	    return ZZDefaultSpace.getDimList(view)[ind];
    }
    String D(ZZCell view, String param) {
	    return getDim(view, param);
    }

    void exchangeCells(ZZCell view1, ZZCell view2) {
	String[] ds1 = ZZDefaultSpace.getDimList(view1);
	String[] ds2 = ZZDefaultSpace.getDimList(view2);
	Hashtable ds = new Hashtable();
	ds.put(ds1[0], ds1[0]);
	ds.put(ds1[1], ds1[1]);
	ds.put(ds2[0], ds2[0]);
	ds.put(ds2[1], ds2[1]);
	ZZCell c1 = EditCursor.get(view1);
	ZZCell c2 = EditCursor.get(view2);
	for(Enumeration e = ds.keys(); 
	    e.hasMoreElements(); ) {
	    String d = (String)e.nextElement();
	    ZZCell n1 = c1.s(d, -1); c1.disconnect(d, -1);
	    ZZCell n2 = c2.s(d, -1); c2.disconnect(d, -1);
	    if(n2 != null) c1.connect(d, -1, n2);
	    if(n1 != null) c2.connect(d, -1, n1);
	    n1 = c1.s(d, 1); c1.disconnect(d, 1);
	    n2 = c2.s(d, 1); c2.disconnect(d, 1);
	    if(n2 != null) c1.connect(d, 1, n2);
	    if(n1 != null) c2.connect(d, 1, n1);
	}
    }

    // Set view cursor
    void svc(ZZCell view, ZZCell to) {
	if(to == null || view == null) return;
	EditCursor.set(view, to);
    }

    ZZCell findOtherView(ZZCell view) {
	ZZCell oth = view.s("d.ctrlview", -1);
	if(oth == null)
	    oth = view.s("d.ctrlview", 1);
	if(oth == null) {
	    System.out.println("CNCTPAIR OTHER NOT FOUND");
	}
	return oth;
    }

    /** Move the cursor.
     * @return Whether a timestamp would be reasonable.
     */
    boolean move(ZZCell view, ZZCell viewCursor, String vdim, int dir,
	    ZZCell realView, ZZCell realCtrl, ZZCell code) {
	if ( viewCursor == null ) return false;
	String[] ss = ZZDefaultSpace.getDirActionWaitingAndClear(view);
	if(ss!=null) {
	    String s = ss[0];
	    
	    int ind = s.indexOf(".");
            if(ind != -1) {
		// A module call. 
		// Load the module and call the action
		// routine.
		ZZModule m = ZZModule.getModule(s.substring(0, ind));
		if(m!=null)
		    m.dirAction(s.substring(ind+1), view, viewCursor,
				vdim, dir, realView, realCtrl, code);
		else
		    callbackFailed(s, "MODULE NOT FOUND");

		return false;   // XXX Stamp?
	    } else if(s.equals("CNCTI")) {
		    cnct(view, viewCursor, true, vdim, dir);
	    } else if(s.equals("NEW")) {
		    viewCursor.N(vdim, dir); 
//	    } else if(s.equals("NEWANDEDIT")) {
//		    svc(view, viewCursor.N(vdim, dir)); 
//		    switchcursmode(view);
//			XXX Problem: this doesn't switch keybindings mode
	    } else if(s.equals("EXCISE")) {
		    viewCursor.excise(vdim);
	    } else if(s.equals("DISC")) {
		    viewCursor.disconnect(vdim, dir); 
	    } else if(s.equals("HOP")) {
		    viewCursor.hop(vdim, dir); 
	    } else if(s.equals("CLONE")) {
		    ZZCell clone = EditCursor.get(realView).zzclone();
		    viewCursor.insert(vdim, dir, clone);
	    } else if(s.equals("CLONECTRL")) {
		    ZZCell clone = EditCursor.get(realCtrl).zzclone();
		    viewCursor.insert(vdim, dir, clone);
	    } else if(s.equals("CNCTPAIR")) {
		    smartconnect(viewCursor, vdim, dir, EditCursor.get(
					findOtherView(view)));
	    } else if(s.equals("CNCTC")) {
		    cnctcln(view, viewCursor, true, vdim, dir);
	    } else if(s.equals("CNCT")) {
		    cnct(view, viewCursor, false, vdim, dir);
	    } else if(s.equals("SORTALPH")) {
		    ZZUtil.sortRank(viewCursor, vdim, dir, 
			new ZZUtil.Comparator() {
			    public int compare(ZZCell c1, ZZCell c2) {
				return c1.getText().compareTo(c2.getText());
			    }
			}, true);
	    } else if(s.equals("END")) {
		    svc(view, viewCursor.h(vdim, dir));
	    } else if(s.equals("MONOCHUG")) {
		if(ss[1] == null || ss[1].equals("")) {
		    p("Move: "+s+" "+vdim+" "+dir);
		    ZZDefaultSpace.storeDirActionWaiting(view, s);
		    ZZDefaultSpace.storeDirWaiting(view, vdim, dir);
		    return false;
		}
		// monochug operation
		int ddir = (ss[2].equals("+") ? 1 : -1);
		String ddim = ss[1];
		ZZCell targ = viewCursor.s(ddim, ddir);
		if(targ == null) return false;
		targ = targ.s(vdim, dir);
		if(targ == null) return false;
		if(targ.s(ddim, -ddir) != null)
		    return false;
		viewCursor.disconnect(ddim, ddir);
		viewCursor.connect(ddim, ddir, targ);
	    } else if(s.equals("FINDTEXT")) {
		String tf = ZZDefaultSpace.getInbuf(view.getSpace(), false);
		if(tf == null || tf.equals("")) return false;
		for(ZZCell c = viewCursor.s(vdim, dir);
		    c != null && c != viewCursor;
		    c = c.s(vdim, dir)) {
		    if(c.getText().indexOf(tf) >= 0) {
			svc(view, c);
			return true;
		    }
		}
	    }
	    return true;
	} else {
	    String s = ZZDefaultSpace.getInbuf(view.getSpace(), true);
	    /*
	     * * DO NOT DO IT. TOO CONFUSING FOR USERS.
	    if(s != null && !s.equals("")) {
		ZZCell c = view.getSpace().getCellByID(s);
		if(c == null) {
		    throw new ZZError("No such cell: '"+s+"'...");
		}
		smartconnect(viewCursor, vdim, dir, c);
	    }
	    */
	    svc(view, viewCursor.s(vdim, dir));
	    // Do timestamp: it's a motion.
	    return true;
	}
    }
    
// or only mark, if ...
    void mrkmv(ZZCell v, ZZCell vc, String dim, int dir) {
	    // New way: in the view
	    synchronized(v.getSpace()) {
		    ZZCell m = v.s("d.mark", 1);
		    boolean had = false;
		    ZZCell m0 = v;
		    while(m!=null) {
			    if(m.h("d.mark-set",-1)
				==vc) { had = true; break; }
			    m0 = m;
			    m = m.s("d.mark", 1);
		    }
		    if(had) {
			    // Delete the whole relation cell.
			    // The ranks collapse around it just right.
			    m.delete(); 
		    } else {
			    ZZCell mark = vc.N("d.mark-set", 1);
			    m0.connect("d.mark", mark);
		    }
	    }
	    if(dim != null && dir != 0)
		move(v, vc, dim, dir, null, null, null);
    }

    // XXX Combine the following two.
    // Connect
    void cnct(ZZCell v, ZZCell vc, boolean inclCurs, String dim, int dir) {
	// Also clears mark.
	ZZCell[] cs;
	synchronized(v.getSpace()) {
	    cs = ZZDefaultSpace.getMark(v);
	    ZZDefaultSpace.clearMark(v);
	    ZZCell cur = (inclCurs ? vc : null);
	    for(int i=0; i<cs.length; i++) {
		if(cur != null)
		    cur.connect(dim, dir, cs[i]);
		cur = cs[i];
	    }
	}
    }

    // Clone, then connect
    void cnctcln(ZZCell v, ZZCell vc, boolean inclCurs, String dim, int dir) {
	// Also clears mark.
	ZZCell[] cs;
	synchronized(v.getSpace()) {
	    cs = ZZDefaultSpace.getMark(v);
	    ZZDefaultSpace.clearMark(v);
	    ZZCell cur = (inclCurs ? vc : null);
	    for(int i=0; i<cs.length; i++) {
		ZZCell to = cs[i].h("d.clone",1).N("d.clone",1);
		if(cur != null)
		    cur.connect(dim, dir, to);
		cur = to;
	    }
	}
    }

    // Rotate a dimension
    void chugdim(ZZCell v, String dx, int dir) {
    synchronized(v.getSpace()) {
	    int nth=getDimstrAsInt(dx);
	    ZZCell cur = v.s("d.dims", nth+1);
	    ZZCell cd = ZZCursorReal.get(cur);
	    cd = cd.s(ZZDefaultSpace.dimListDimen, dir);
	    if(cd != null) 
		ZZCursorReal.set(cur, cd);
    }
    }

    // Rotate a dimension
    void setdim(ZZCell v, String dx, ZZCell dimc) {
    synchronized(v.getSpace()) {
	    int nth=getDimstrAsInt(dx);
	    ZZCell cur = v.s("d.dims", nth+1);
	    ZZCursorReal.set(cur, dimc);
    }
    }


    // Severs connections to other dimensions / cursors. Good??
    void setdims(ZZCell v, ZZCell s) {
	    while(true) {
		    v = v.s("d.dims", 1);
		    if(v==null) break;
		    // s.insert("d.dim", -1, v);
		    ZZDefaultSpace.setDimFromCell_sever(v, s);
		    ZZCell sn = s.s("d.2", 1);
		    if(sn!=null) s = sn;
	    }
    }

    void hopraster(ZZCell v, int dir) {
	pa("HOPRASTER "+dir);

	ZZCell rc = ZZDefaultSpace.findInheritableParam(v, "View");
	if(rc != null) {
	    ZZCell r = ZZCursorReal.get(rc);
	    r = r.s("d.2", dir);
	    if(r!=null)
		ZZCursorReal.set(rc, r); 
	}
    }

    void hopffactory(ZZCell v, int dir) {
	pa("HOPFFACT "+dir);
	ZZCell rc = ZZDefaultSpace.findInheritableParam(v, "CellView");
	if(rc != null) {
	    ZZCell r = ZZCursorReal.get(rc);
	    r = r.s("d.2", dir);
	    if(r!=null)
		ZZCursorReal.set(rc, r); 
	}
    }

    void clonelink(ZZCell from, String dim, ZZCell[] toclone) {
	    ZZCell cur = from;
	    for(int i=0; i<toclone.length; i++) {
		    ZZCell n = toclone[i].N("d.clone", 1);
		    cur.connect(dim, n);
		    cur = n;
	    }
    }

    void do_goto(ZZCell view) {
	String s = ZZDefaultSpace.getInbuf(view.getSpace(), true);
	// WouldBreak if inbuf is empty, so we don't go there 
	if( s == null || s.equals(""))
	    return;
	ZZCell c = view.getSpace().getCellByID(s);
	if(c == null) {
	    throw new ZZError("No such cell: '"+s+"'...");
	}
	EditCursor.set(view, c);
    }

    // CURSEL: Cursor selection
    // SELEVATE: Cursel along d.cursor-list instead of SysCursors list
    void cursel(ZZCell view, int dir, boolean selevate) {
        ZZCell cur = view.h("d.cursor-cargo", -1);
	ZZCell curcln = cur.s("d.clone", 1);
        ZZCell c;
        if(selevate) {
            c = cur.s("d.cursor-list", dir);
            if(c == null)
                return;
        } else {   // XXX check for "kind" of cursor (active, personalized)
	    ZZCell cln = curcln;
	    LoopDetector l = new LoopDetector();
	    while(true) {
		cln = cln.s("d.2", dir);
		l.detect(cln);
	        if(cln == null) return;
		c = cln.h("d.clone", -1);
	        if(c != null && (c.s("d.cursor-list", -1) != null
		                 || c.s("d.cursor", -1) != null))
		    break;
	    };
	}
        
        ZZCursorReal.attach(view, c);
    }

    public void smartconnect(ZZCell c1, String dim, int dir, ZZCell c2) {
	if(c1.s(dim, dir) != null) {
	    // If has a neighbour and c2 unconnected, insert
	    if(c2.s(dim, -1) == null &&
	       c2.s(dim, 1) == null) {
		c1.insert(dim, dir, c2);
	    }
	    System.out.println("Can't connect: conflicting connections exist");
	    return;
	}
	c1.connect(dim, dir, c2);
    }

    public void switchcursmode(ZZCell view) {
	ZZCursorReal.set(view, ZZCursorReal.get(view));
	ZZCursorReal.setOffs(view, 0);
    }
    public void switchcursmodeoff(ZZCell view) {
	ZZCursorReal.set(view, ZZCursorReal.get(view));
	ZZCursorReal.setOffs(view, ZZCursorReal.NO_OFFSET);
    }

    // XXX should this throw a ZZError?
    public void callbackFailed(String c, String s) {
	System.out.println("'"+c+"' failed: "+s);
    }
    
    public void execCallback(ZZCell code, 
			     ZZCell target, 
			     ZZView zzdv, ZZView zzcv, 
			     String key,
			     Point pt, 
			     ZZScene xi)
    {
	ZZCell dataView = zzdv.getViewcell();
	ZZCell dataCursor = EditCursor.get(dataView);
	ZZCell ctrlView = (zzcv==null ? null : zzcv.getViewcell());
	ZZCell ctrlCursor = (zzcv==null ? null : EditCursor.get(ctrlView));
	ZZCell view, viewCursor;
	ZZCell otherView;
	ZZView zzv;

	p("EXCB " + code + " "+code.getSpace());
	p("PAR: "+zzdv+" "+zzcv);
	int ind;
	// ZZCell target = clicked;
	// if(target==null)
	//     target = viewCursor;
	synchronized(code.getSpace()) {
	    boolean fastany = true;
	    /** The default is to clear the 
	     * action cells after any action. Setting this
	     * flag causes that not to happen. Useful when you want
	     * to wait for the next command after a command.
	     */
	    boolean dontClearAction = false;
	    boolean dontStamp = false;
	    String c = code.getText();
	    p("CODE: '"+c+"'");
	    // pa("CODE: '"+c+"'");

            if((ind = c.indexOf("."))!=-1) {
		// A module call. 
		// Load the module and call the action
		// routine.
		ZZModule m = ZZModule.getModule(c.substring(0, ind));
		if(m!=null)
		    m.action(c.substring(ind+1),
				code, target,
				zzdv, zzcv, 
				key, pt, xi);
		else
		    callbackFailed(c, "MODULE NOT FOUND");

		return;

		// XXX Stamp?
	    }

	    StringTokenizer st = new StringTokenizer(c);
	    String[] toks = new String[st.countTokens()];
	    for(int i=0; i<toks.length; i++)
		    toks[i] = st.nextToken();

	    String dx = null;
	    String vdim = null;
	    int dir = 0;
	    int sndedge = -1;

	    if (toks.length == 0) {
                    return;
	    }
	
	    // Converting the deprecated tokens with explicite view
	    // XXX should be removed at some point; default usec to false then
	    boolean usec = true;  // "use control view?"
	    if(toks[0].equals("CTRL"))
		toks[0] = "CURS";
	    else if(toks[0].equals("SETCDIMS"))
		toks[0] = "SETDIMS";
	    else if(toks[0].equals("CDIM"))
		toks[0] = "DIM";
	    else if(toks[0].equals("CRASTER"))
		toks[0] = "RASTER";
	    else if(toks[0].equals("CTRLGOTO"))
		toks[0] = "GOTO";
	    else if(toks[0].equals("CFETCH"))
	        toks[0] = "FETCH";
	    else
		usec = false;
		
	    if(toks.length > 1) {
		    if(toks[1].equals("CTRL")) { usec=true; }
			
		    if(toks[1].equals("+")) { dx=null; dir = 1; }
		    if(toks[1].equals("-")) { dx=null; dir = -1; }

		    if(toks[1].equals("C+")) { dx=null; dir = 1;  usec=true; }
		    if(toks[1].equals("C-")) { dx=null; dir = -1; usec=true; }

		    if(toks[1].equals("X+")) { dx="X"; dir = 1; }
		    if(toks[1].equals("X-")) { dx="X"; dir = -1; }
		    if(toks[1].equals("Y+")) { dx="Y"; dir = 1; }
		    if(toks[1].equals("Y-")) { dx="Y"; dir = -1; }
		    if(toks[1].equals("Z+")) { dx="Z"; dir = 1; }
		    if(toks[1].equals("Z-")) { dx="Z"; dir = -1; }

		    if(toks[1].equals("X")) { dx="X"; dir = 0; }
		    if(toks[1].equals("Y")) { dx="Y"; dir = 0; }
		    if(toks[1].equals("Z")) { dx="Z"; dir = 0; }

		    if(toks[1].equals("CX+")) { dx="X"; dir = 1;  usec=true; }
		    if(toks[1].equals("CX-")) { dx="X"; dir = -1; usec=true; }
		    if(toks[1].equals("CY+")) { dx="Y"; dir = 1;  usec=true; }
		    if(toks[1].equals("CY-")) { dx="Y"; dir = -1; usec=true; }
		    if(toks[1].equals("CZ+")) { dx="Z"; dir = 1;  usec=true; }
		    if(toks[1].equals("CZ-")) { dx="Z"; dir = -1; usec=true; }

		    if(toks[1].equals("T0+")) { sndedge = 0; dir = 1; }
		    if(toks[1].equals("T0-")) { sndedge = 0; dir = -1; }
		    if(toks[1].equals("T1+")) { sndedge = 1; dir = 1; }
		    if(toks[1].equals("T1-")) { sndedge = 1; dir = -1; }
	    }

	    if(usec && ctrlView == null) {
		callbackFailed(c, "NO CONTROL VIEW");
		ZZDefaultSpace.getDirActionWaitingAndClear(dataView);
		// Do not timestamp taking the operation out, either,
		// because putting it in was not stamped.
		// XXX Some strange bug?
		code.getSpace().stamp();
		return;
	    }

	    if(!usec) {
		view = dataView;
		otherView = ctrlView;
		viewCursor = dataCursor;
		zzv = zzdv;
	    } else {
		view = ctrlView;
		otherView = dataView;
		viewCursor = ctrlCursor;
		zzv = zzcv;
	    }
	
	    if(dx != null)
		vdim = D(view, dx);
	    if(target != null)
		viewCursor = target;

	    if(     toks[0].equals("NEW") ||
		    toks[0].equals("NEWANDEDIT") ||
		    toks[0].equals("EXCISE") ||
		    toks[0].equals("DISC") ||
		    toks[0].equals("CNCTI") ||
		    toks[0].equals("CNCTPAIR") ||
		    toks[0].equals("CLONE") ||
		    toks[0].equals("CLONECTRL") ||
		    toks[0].equals("CNCT") ||
		    toks[0].equals("CNCTC") ||
		    toks[0].equals("HOP") ||
		    toks[0].equals("MONOCHUG") ||
		    toks[0].equals("SORTALPH") ||
		    toks[0].equals("FINDTEXT") ||
		    toks[0].equals("END")) {

		// XXX Should this be implemented differently, so that the
		//     dir doesn't need to be stored in the structure?
		ZZDefaultSpace.storeDirActionWaiting(view, toks[0]);
		dontClearAction = true;
		if (dx != null) {
		    move(view, viewCursor, vdim, dir, dataView, ctrlView, code);
		    // Move has commands that take more than one dir.
		} else {
		    dontStamp = true;
		}
	    } else if(toks[0].equals("CRSR")) {
		dontStamp = 
		   ! move(view, viewCursor, vdim, dir, dataView, ctrlView, code);
		// Move has commands that take more than one dir.
		dontClearAction = true;
	    } else if(toks[0].equals("EXCHANGECELLS")) {
		exchangeCells(dataView, ctrlView);
	    } else if(toks[0].equals("ORIGINAL")) {
		svc(view, viewCursor.h("d.clone", -1));
	    } else if(toks[0].equals("NEXTCLONE")) {
		ZZCell foo = viewCursor.s("d.clone", 1);
		if(foo != null) svc(view, foo);
	    } else if(toks[0].indexOf("MOUSE") == 0) {
		Object ob = xi.getObjectAt(pt.x, pt.y);
		ZZCursor cc=null;
		ZZCell cell=null;
		boolean processed=false;

		if(ob instanceof ZZCursor) {
		    cc = (ZZCursor)ob;
		    cell=cc.get();
		} else if(ob instanceof ZZCell) {
		    cell=(ZZCell)ob;
		} else if(ob instanceof ZZCursor[]) {
		    ZZCursor[] cs = (ZZCursor[])ob;
		    if(toks[0].equals("MOUSESETSTACK")) {
			p("MOUSESETSTACK with stack "+cs+" length "+cs.length);
			EditCursor.setStack(view, cs);
			processed = true;
		    } else {
			cc = cs[0];
			cell = cs[0].get();
		    }
		} // else return;

		if(toks[0].equals("MOUSETEMPHILIGHT")) {
		    // highlight pointed cell and its clones
                    ZZGroupHighlightCache.reset();
		    if(cell != null) {
			ZZCell hl = cell.h("d.clone");
			for(; hl != null; hl = hl.s("d.clone") )
			ZZGroupHighlightCache.add(hl);
		    } else {
			ZZGroupHighlightCache.add(cell);
		    }
		    ZZUpdateManager.chg();
		    return;
                }		

		if(cell==null) return;

		else if(toks[0].equals("MOUSESETC")) {
		    ZZCursorReal.set(view, cell);
		    fastany = false;
		}
		else if(toks[0].equals("MOUSESETSTACK") && !processed) {
		    p("MOUSESETSTACK on non-stack, but cell "+cell);
		    ZZCursorReal.set(view, cell);
		}
		else if(toks[0].equals("MOUSESETCTXT")) {
		    if(cc==null) return;
		    svc(view, cell);
		    EditCursor.setOffs(view, cc.getOffs());
		    fastany = false;
		}
		else if(ctrlCursor!=null && c.equals("MOUSEEXEC")) {
		    pa("MOUSEEXEC");
		    this.execCallback(ctrlCursor, cell,
				      zzv, zzcv, null, null, null);
		       
		}
		else if(toks[0].equals("MOUSEMARK")) {
		    mrkmv(view, cell, null, 0);
		    fastany = false;
		} else if(toks[0].equals("MOUSESETDIMOTHER")) {
		    setdim(otherView, dx, cell);
		} else {
		    callbackFailed(c, "Unknown MOUSE code");
		    return;
		}
	    }
	    else if(ctrlCursor!=null && c.equals("EXEC")) {
	    // This is an interesting one: it just means "execute 
	    // whatever cell is visible in the control view.
	    // So we call ourselves...
	    // XXX inherit xi really?
	        ZZCommand comm = ZZCommand.getCommand(ctrlCursor);
	        if(comm != null)
		    comm.execCallback(viewCursor,
			zzdv, zzcv, null, pt, xi);
		else
		    this.execCallback(ctrlCursor, viewCursor,
			zzdv, zzcv, null, pt, xi);
	    }
	    else if(c.equals("HERA1")) {
		// Execute the first, primitive calling sequence of
		// a Heraclitus Clang primitive poswards on d.2 from
		// the command cell
		ZZCell hc = code.s("d.2", 1);
		if(hc == null) throw new ZZError("No heraclitus1 prim!");
		ZZCell curspar = code.N();
		ZZCell curs1 = code.N();
		ZZCell curs2 = curs1.N("d.1", 1);
		ZZCursorReal.set(curs1, ctrlView);
		ZZCursorReal.set(curs2, dataView);
		ZZCursorReal.set(curspar, curs1);
		try {
		    p("Enter heraclitus: ");
		    org.gzigzag.heraclitus.HeraclitusClang.execute(
			hc, curspar);
		    p("Exit heraclitus");
		} finally {
		    ZZCursorReal.delete(curs1);
		    ZZCursorReal.delete(curs2);
		    ZZCursorReal.delete(curspar);
		    curs1.delete();
		    curs2.delete();
		    curspar.delete();
		}
	    }
	
	    else if(toks[0].equals("MARK"))
		    mrkmv(view, viewCursor, dx, dir);
	    else if(toks[0].equals("DIM"))
		    chugdim(view, dx, dir);
	    else if(toks[0].equals("SETDIMS"))
		    setdims(view, viewCursor);
	    else if(toks[0].equals("RASTER"))
		    hopraster(view, dir!=0 ? dir : 1);
	    else if(toks[0].equals("FFACTORY"))
		    hopffactory(view, dir!=0 ? dir : 1);
	    else if(toks[0].equals("HOME")) {
		    ZZCell home = view.getSpace().getHomeCell();
		    ZZCursorReal.set(view, home);
	    }
	    else if(toks[0].equals("HOMECLR")) {
		    ZZCell home = view.getSpace().getHomeCell();
		    ZZCell datarec = ZZDefaultSpace
			.findInheritableParam(dataView, "CursorRecovery");
		    if(datarec!=null) ZZCursorReal.attach(dataView, datarec);
		    ZZCell ctrlrec = ZZDefaultSpace
			.findInheritableParam(ctrlView, "CursorRecovery");
		    if(ctrlrec!=null) ZZCursorReal.attach(ctrlView, ctrlrec);
		    ZZCursorReal.set(dataView, home);
		    ZZCursorReal.set(ctrlView, home);
		    ZZCell dimlist =
			    ZZDefaultSpace.findDefaultDimlist(home);
		    setdims(dataView, dimlist);
		    setdims(ctrlView, dimlist);

		    ZZDefaultSpace.clearMark(dataView);

	    } else if(toks[0].equals("DELETESCRAM")) {
		    ZZCell mv = null;
		    mv = viewCursor.s(D(view, "X"), -1);
		    if(mv == null) 
			mv = viewCursor.s(D(view, "X"), +1);
		    if(mv == null) 
			mv = viewCursor.s(D(view, "Y"), -1);
		    if(mv == null) 
			mv = viewCursor.s(D(view, "Y"), +1);
		    if(mv == null) 
			mv = viewCursor.s(D(view, "Z"), -1);
		    if(mv == null) 
			mv = viewCursor.s(D(view, "Z"), +1);
		    if(mv == null) 
			mv = view.getSpace().getHomeCell();
		    svc(view, mv);
		    viewCursor.delete();
	    }

	    else if(toks[0].equals("QUIT")) {
		// Commit changes to space here 
		viewCursor.getSpace().commit();
		SafeExit.exit(0);
	    }
	    else if(toks[0].equals("APPENDINPUT")) {
		if(key != null && key.length() == 1)
		    ZZDefaultSpace.appendInbuf(viewCursor.getSpace(), key);
		dontStamp = true;
	    } else if(toks[0].equals("BACKSPACEINPUT")) {
		ZZDefaultSpace.appendInbuf(viewCursor.getSpace(), null);
		dontStamp = true;
	    } else if(toks[0].equals("CLRINPUT")) {
		ZZDefaultSpace.getInbuf(viewCursor.getSpace(), true);
		dontStamp = true;
	    } else if(toks[0].equals("GOTO"))
		do_goto(view);

	    // Sound span adjustment. These will be useful
	    // in a more general way later on.
	    else if(toks[0].equals("SNDL")) {
	        Span sp = viewCursor.getSpan();
		ZZCell n = viewCursor.s("d.2", sndedge*2-1);
	        Span nsp = n.getSpan();
		if(sp!=null && nsp!=null) {
		        Span[] sps;
			if(sndedge==0) {
			    sps = nsp.adjustEdgeLocked(100000000*dir, sp, 500000000);
			    n.setSpan(sps[0]);
			    viewCursor.setSpan(sps[1]);
			} else {
			    sps = sp.adjustEdgeLocked(100000000*dir, nsp, 500000000);
			    viewCursor.setSpan(sps[0]);
			    n.setSpan(sps[1]);
			}
		} else if(sp!=null)
		    	viewCursor.setSpan(
				sp.adjustEdge(sndedge, 100000000 * dir));
	    } else if(toks[0].equals("SNDS")) {
	        Span sp = viewCursor.getSpan();

		if(sp!=null)
		    	viewCursor.setSpan(
				sp.adjustEdge(sndedge, 100000000 * dir));
	    } else if(toks[0].equals("SNDSPLIT")) {
		ZZCell n = viewCursor.N("d.2", 1);
	        Span sp = viewCursor.getSpan();
		if(sp!=null) {
		    Span[] sps = sp.splitInHalf();
		    viewCursor.setSpan(sps[0]);
		    n.setSpan(sps[1]);
		}
	    } else if(toks[0].equals("SNDJOIN+")) {
		ZZCell o = viewCursor.s("d.2", 1);
		if(o!=null) {
		    Span sp1 = viewCursor.getSpan();
		    Span sp2 = o.getSpan();
		    if(sp1!=null && sp2!=null) {
			Span spj = sp1.join(sp2, 500000000);
			if(spj!=null) {
				viewCursor.setSpan(spj);
				// XXX ??? Try to absorb links?
				o.delete();
			}
		    }
		}
	    } else if(toks[0].equals("SWAPCRSR")) {
		ZZCell view2 = findOtherView(view);
		ZZCell view2c = EditCursor.get(view2);
		svc(view, view2c);
		svc(view2, viewCursor);
	    } else if(toks[0].equals("FETCH")) {
		svc(findOtherView(view), viewCursor);
	    } else if(toks[0].equals("PUICOPY")) {
		ZZUtil.puiCopy(viewCursor);
	    } else if(toks[0].equals("PUIPASTE")) {
		ZZUtil.puiPaste(viewCursor);
	    } else if(toks[0].equals("PUICOPYLINE")) { // Copy along y-dim
		ZZUtil.puiCopy(viewCursor, ZZCursorReal.get(
					    dataView.s("d.dims", 1)
						    .s("d.dims", 1)
					   ).getText());
	    } else if(toks[0].equals("SWITCHCURSMODE")) {
		switchcursmode(view);
		dontStamp = true;
	    } else if(toks[0].equals("SWITCHOFFCURSMODE")) {
		switchcursmodeoff(view);
            } else if(toks[0].equals("CTXTHOME")) {
                if (EditCursor.getOffs(view) == EditCursor.NO_OFFSET) return;
                EditCursor.setOffs(view, 0);
                dontStamp = true;
            } else if(toks[0].equals("CTXTEND")) {
                if (EditCursor.getOffs(view) == EditCursor.NO_OFFSET) return;
                EditCursor.setOffs(view, viewCursor.getText().length());
                dontStamp = true;                                                           } else if(toks[0].equals("CTXTSOL")) {  // SOL = start of line
                if (EditCursor.getOffs(view) == EditCursor.NO_OFFSET) return;
                int cur = EditCursor.getOffs(view);
                String line = viewCursor.getText();
                if (cur >= line.length()) cur = line.length()-1;
                if (line.charAt(cur) == '\n') --cur;
                for (int i = cur; i  >= 0; --i) {
                    p("seeking SOL: charAt = " + line.charAt(i));
                    if (line.charAt(i) == '\n') {
                        cur = i+1;
                        break;
                    }
                    if (i == 0) {
                        cur = i;
                        break;
                    }
                }
                if (cur > line.length()) cur = line.length();
                if (cur < 0) cur = 0;
                EditCursor.setOffs(view, cur);
                dontStamp = true;                                   
            } else if(toks[0].equals("CTXTEOL")) {  // EOL = start of line
                if (EditCursor.getOffs(view) == EditCursor.NO_OFFSET) return;
                int cur = EditCursor.getOffs(view);
                String line = viewCursor.getText();
                for (int i = cur; i < line.length(); ++i) {
                    if (line.charAt(i) == '\n') {
                        cur = i;
                        break;
                    }
                    if (i == line.length() - 1) {
                        cur = line.length();
                        break;
                    }
                }
                if (cur > line.length()) cur = line.length();
                if (cur < 0) cur = 0;
                EditCursor.setOffs(view, cur);
                dontStamp = true;                                  
	    } else if(toks[0].equals("CTXTCRSR")) {
		int cur = EditCursor.getOffs(view);
		if(cur == EditCursor.NO_OFFSET) return;
		cur += dir;
		if(cur < 0) cur = 0;
		if(cur > viewCursor.getText().length())
		    cur = viewCursor.getText().length();
		EditCursor.setOffs(view, cur);
		dontStamp = true;
	    } else if(toks[0].equals("CTXTINSERT") ||
		      toks[0].equals("CTXTBKSP") ||
		      toks[0].equals("CTXTDEL")) {
		dontStamp = true;
		if(viewCursor.getSpan() != null) {
		    System.out.println("Can't modify span");
		    return;
		}
		int cur = EditCursor.getOffs(view);
		// Make sure it fits.
		if(cur > viewCursor.getText().length())
		    cur = viewCursor.getText().length();
		if(cur == EditCursor.NO_OFFSET) return;
		StringBuffer s = new StringBuffer(viewCursor.getText());
               if(toks[0].equals("CTXTINSERT"))  {
                    p("CTXTINSERT: key = " + key);
                    String ik = key;
                    if (ik != null && ik.equals("Enter")) ik = "\n";
                    // Don't insert e.g. "Shift"
                    if(ik != null && ik.length() == 1) {
                        if(textlog != null) {
                            textlog.print(ik);
                            // flush all the time so nothing is lost
                            // in a crash
                            textlog.flush();
                        }
                        s.insert(cur, ik);
                        cur += 1;
                    }                                                 
		} else if(toks[0].equals("CTXTBKSP")) {
		    if(cur > 0) {
			String str = s.toString();
			// XXX delete requires JDK 1.2
			s = new StringBuffer( str.substring(0, cur-1) );
			s.append(str.substring(cur));

			cur -= 1;
		    }
		} else if(toks[0].equals("CTXTDEL")) {
		    if(cur < viewCursor.getText().length()) {
		    	String str = s.toString();
			s = new StringBuffer( str.substring(0, cur) );
			s.append(str.substring(cur+1));
		    }
		}
		viewCursor.setText(s.toString());
		EditCursor.setOffs(view, cur);
            } else if(toks[0].equals("CURSEL"))
                cursel(view, dir, false);
            else if(toks[0].equals("SELEVATE"))
                cursel(view, dir, true);
	    else if(toks[0].equals("CreateCursors")) {
		ZZCell h = view.getSpace().getHomeCell();
		ZZCell sc = ZZDefaultSpace
		    .findOnSystemlist(view.getSpace(), "SysCursors", false);
		if(sc != null) {
		    ZZDefaultSpace.newSysCursor(sc, h, Color.blue.brighter());
		    ZZDefaultSpace.newSysCursor(sc, h, Color.yellow.darker());
		    ZZDefaultSpace.newSysCursor(sc, h, Color.red.darker());
		    ZZDefaultSpace.newSysCursor(sc, h, new Color(180, 0, 255));
		} else
		    callbackFailed(c, "SysCursors list required");
            } else if(toks[0].equals("NEWVIEWS")) {
		// Create a new view pair.
		ZZDefaultSpace.newViewPair(view.getSpace());
            } else if(toks[0].equals("EDITVIEW") || toks[0].equals("HELP")) {
		boolean help = toks[0].equals("HELP");
                ZZCell home = dataView.getSpace().getHomeCell();
                ZZCell rl = home.findText("d.2", 1, "AllViews"); 
                rl = rl.s("d.1", 1);
                rl = rl.findText("d.2", 1, "Edit");
                if (rl == null) throw new ZZError("Edit view not found");
                rl = rl.getOrNewCell("d.clone", 1);
		ZZCell cursCell;
		if(help) {
		    cursCell = home.N();
		    ZZCursorReal.set(cursCell, 
				     viewCursor.getOrNewCell("d.help", 1));
		} else
		    cursCell = dataView;
		
		ZZCell win = ZZDefaultSpace.newToplevelView(dataView.getSpace(),
			help ? "Help" : "Edit",
			help ? 100 : 512, help ? 100 : 449, 
			help ? 400 : 512, help ? 400 : 100,
			rl, null, // XXX Defaultbind?
			null, cursCell,
			null,
			Color.white); // XXX PREFERENCE!!!
	    } else if(toks[0].equals("APPLITUDE")) {
		ApplitudeMgr.rotateWindowApp(view, dir);
            } else if(toks[0].equals("MULTIFOCUSCHG")) {
                Flob[] fs = new Flob[1];
                ((FlobSet)xi).getObjectAt(pt.x, pt.y, fs);
                Flob f = fs[0];
                if(f == null || f.applitude == null) {
		    // no hilighted group
		    ZZGroupHighlightCache.reset();
		    return;
		}
                ZZCell app = f.applitude;
                ApplitudeMgr.setWindowApp(view, app);
		ZZCell appbinds = ApplitudeMgr.getAppBindsForWin(view);
                // app's own mouse moved 
	        ZZCell binding = ZZDefaultSpace.
		    findInheritableParam(appbinds, "MouseMoved1");
                if(binding == null) return;
                ZZCell bc = binding.s("d.1", 1);
		new ZZPrimitiveActions().execCallback(
		        bc, 
		        target,
			zzdv, zzcv,
			key, pt, xi
		     );
            } else if(toks[0].equals("GETSPACEID")) {
                target.setText(target.getSpace().getID());
            } else if(toks[0].equals("SETSPACEID")) {
                target.getSpace().setID(target.getText());
	    } else if(toks[0].equals("UPDATEWINS")) {
		ZZWindows.update();
	    } else if(toks[0].equals("ACCURSE")) {
		// Make the cell accursed in the other window a cursor to the
		// cell accursed in window for which the action occured.
		ZZCursorReal.set(EditCursor.get(otherView), viewCursor);
	    } else if(toks[0].equals("UNDO")) {
		code.getSpace().undo();
		dontStamp = true;
	    } else if(toks[0].equals("REDO")) {
		code.getSpace().redo();
		dontStamp = true;
	    } else if(toks[0].equals("COMMIT")) {
		code.getSpace().commit();
	    }
	    else
		callbackFailed(c, "UNKNOWN CODE");

	    p("ZZPrimitiveActions: postlude: "+fastany+" "+zzv+" "+
		dontClearAction);

	    if(fastany)
		ZZUpdateManager.setFast(zzv);

	    // Clear the waiting dir action
	    if(!dontClearAction)
		ZZDefaultSpace.getDirActionWaitingAndClear(view);

	    if(!dontStamp) 
		code.getSpace().stamp();
	}
    }

    static ZZCell addAct(ZZCell s, String name, String act) {
	    ZZCell n1 = s.N("d.2", 1);
	    n1.setText(name);
	    ZZCell n2 = n1.N("d.1", 1);
	    n2.setText(act);
	    return n2;
    }

    static void addAct(ZZCell s, String name, String act, ZZCell after) {
	ZZCell c = addAct(s, name, act);
	after.insert("d.3", 1, c);
    }

    static void setModeColor(ZZCell mode, Color col) {
	if(col == null) {
	    if(mode.s("d.color") != null)
		mode.disconnect("d.color", 1);
	} else {
	    ZZCell c = mode.getOrNewCell("d.color", 1);
	    c.setText(String.valueOf(col.getRGB()));
	}
    }

    /** Puts a number of default key bindings to the structure.
     * ZZKeyBindings1 structure is assumed (list on d.2 of keys 
     * and on d.1 the responses)
     */
    static public void putDefBinds(ZZCell start, ZZCell editstart) {
	    start.setText("Normal mode");
	    editstart.setText("Text edit bindings");

	    // Apparently, this wasn't actually necessary!
	    /*
	    ZZCell dir = start.N("d.2", 1);
	    dir = dir.N("d.3", -1);
	    dir.setText("Motion");
	    */
	    ZZCell dir = start;

	    ZZCell editmode = start.N("d.1", 1);
	    setModeColor(editmode, Color.white);
	    
	    editmode.setText("Edit mode");
	    editmode.N("d.2").setText("EDITBINDS");
	    addAct(editmode, "Tab", "SWITCHOFFCURSMODE", dir);
	    addAct(editmode, "Esc", "SWITCHOFFCURSMODE", dir);

	    ZZCell curs = editstart;
	    curs.setText("Text edit mode");
            addAct(curs, "Ctrl-A", "CTXTSOL");
            addAct(curs, "Ctrl-E", "CTXTEOL");
            addAct(curs, "End", "CTXTEND");
            addAct(curs, "Home", "CTXTHOME");              
	    addAct(curs, "Right", "CTXTCRSR +");
	    addAct(curs, "Left", "CTXTCRSR -");
	    addAct(curs, "Backspace", "CTXTBKSP");
	    addAct(curs, "Delete", "CTXTDEL");
	    addAct(curs, "MouseClicked1", "MOUSESETCTXT");
	    addAct(curs, "DEFAULT", "CTXTINSERT");


            ZZCell cursel = editmode.N("d.1", 1);
            cursel.setText("Cursel mode");
	    cursel.N("d.color").setText("CURSOR");
	    
            addAct(cursel, " ", "", dir);
            addAct(cursel, "Esc", "", dir);
	    addAct(cursel, "Alt-C", "", dir);
            addAct(cursel, "Left", "CURSEL -");
            addAct(cursel, "Right", "CURSEL +");
            addAct(cursel, "Up", "SELEVATE -");
            addAct(cursel, "Down", "SELEVATE +");

            addAct(cursel, "j", "CURSEL -");
            addAct(cursel, "l", "CURSEL +");
            addAct(cursel, "i", "SELEVATE -");
            addAct(cursel, ",", "SELEVATE +");
        
            addAct(cursel, "s", "CURSEL C-");
            addAct(cursel, "f", "CURSEL C+");
            addAct(cursel, "e", "SELEVATE C-");
            addAct(cursel, "c", "SELEVATE C+");

	    // Quote starts insert buffer mode...
	    ZZCell bufins = cursel.N("d.1", 1);
	    bufins.setText("Quotebuf");
	    setModeColor(bufins, Color.pink);
	    
	    addAct(bufins, "DEFAULT", "APPENDINPUT");
	    addAct(bufins, "Backspace", "BACKSPACEINPUT");
	    addAct(bufins, "\"", "", dir);
	    addAct(bufins, "Esc", "", dir);

	    // Totally temporary
	    addAct(dir, "Tab", "SWITCHCURSMODE", editmode);
	    addAct(dir, "Alt-C", "", cursel);

	    addAct(dir, "\"", "CLRINPUT", bufins);

	    addAct(dir, "l", "CRSR X+");
	    addAct(dir, "j", "CRSR X-");
	    addAct(dir, "i", "CRSR Y-");
	    addAct(dir, ",", "CRSR Y+");
	    addAct(dir, "k", "CRSR Z+");
	    addAct(dir, "K", "CRSR Z-");

	    // Caps locked
	    addAct(dir, "L", "CRSR X+");
	    addAct(dir, "J", "CRSR X-");
	    addAct(dir, "I", "CRSR Y-");

	    addAct(dir, "a", "MONOCHUG");
	    
	    addAct(dir, "f", "CRSR CX+");
	    addAct(dir, "s", "CRSR CX-");
	    addAct(dir, "e", "CRSR CY-");
	    addAct(dir, "c", "CRSR CY+");
	    addAct(dir, "d", "CRSR CZ+");
	    addAct(dir, "D", "CRSR CZ-");

	    // Caps locked
	    addAct(dir, "F", "CRSR CX+");
	    addAct(dir, "S", "CRSR CX-");
	    addAct(dir, "E", "CRSR CY-");
	    addAct(dir, "C", "CRSR CY+");

	    addAct(dir, "Right", "CRSR X+");
	    addAct(dir, "Left", "CRSR X-");
	    addAct(dir, "Up", "CRSR Y-");
	    addAct(dir, "Down", "CRSR Y+");
	
	    addAct(start, "Ctrl-C", "PUICOPY");
	    addAct(start, "Ctrl-Shift-C", "PUICOPYLINE");
	    addAct(start, "Ctrl-V", "PUIPASTE");

	    addAct(start, "-", "CNCTI" /*, dir */);
	    addAct(start, "n", "NEW" /*, dir */);
	    // XXX Problem: must switch to edit mode AFTER arrow
	    // addAct(start, "N", "NEWANDEDIT", editmode);
	    addAct(start, "b", "DISC" /*, dir */);
	    addAct(start, "/", "CNCTPAIR" /*, dir */);
	    addAct(start, "t", "CLONE" /*, dir */);
	    addAct(start, "T", "CLONECTRL" /*, dir */);
	    addAct(start, "o", "ORIGINAL");
	    addAct(start, "O", "ORIGINAL CTRL");
	    addAct(start, "Alt-O", "NEXTCLONE");
	    addAct(start, "Alt-Shift-O", "NEXTCLONE CTRL");
	    addAct(start, "h", "HOP" /*, dir */);
	    addAct(start, "H", "HOP" /*, dir */);

	    addAct(start, "%", "EXCHANGECELLS");

	    addAct(start, "Ctrl-X", "EXCISE");

	    addAct(start, "End", "END" /*, dir */);

	    for(int i=0; i<=9; i++) 
		addAct(start, ""+i, "APPENDINPUT");
	    addAct(start, "Backspace", "BACKSPACEINPUT");

	    addAct(start, "g", "GOTO");
	    addAct(start, "G", "GOTO CTRL");

	    addAct(start, "Esc", "HOMECLR");
	    addAct(start, "Home", "HOME");


	    addAct(start, "v", "RASTER +");
	    addAct(start, "Alt-V", "RASTER -");
	    addAct(start, "V", "RASTER C+");
	    addAct(start, "Alt-Shift-V", "RASTER C-");

	    addAct(start, "F2", "FFACTORY +");
	    addAct(start, "Alt-F2", "FFACTORY -");
	    addAct(start, "Shift-F2", "FFACTORY C+");
	    addAct(start, "Alt-Shift-F2", "FFACTORY C-");
	    

	    addAct(start, "F3", "SORTALPH");

	    addAct(start, "x", "DIM X+");
	    addAct(start, "Alt-X", "DIM X-");
	    addAct(start, "y", "DIM Y+");
	    addAct(start, "Alt-Y", "DIM Y-");
	    addAct(start, "z", "DIM Z+");
	    addAct(start, "Alt-Z", "DIM Z-");


	    addAct(start, "X", "DIM CX+");
	    addAct(start, "Alt-Shift-X", "DIM CX-");
	    addAct(start, "Y", "DIM CY+");
	    addAct(start, "Alt-Shift-Y", "DIM CY-");
	    addAct(start, "Z", "DIM CZ+");
	    addAct(start, "Alt-Shift-Z", "DIM CZ-");

	    addAct(start, "~", "SWAPCRSR");
	    addAct(start, "<", "FETCH");
	    addAct(start, ">", "FETCH CTRL");
	
	    addAct(start, "Ctrl-<", "ACCURSE CTRL");
	    addAct(start, "Ctrl->", "ACCURSE");
	
	    // addAct(start, "Alt-x", "PSHDIM X+");
	    // addAct(start, "Alt-y", "PSHDIM Y+");
	    // addAct(start, "Alt-z", "PSHDIM Z+");


	    

	/*
	    addAct(start, "L", "MARK X+");
	    addAct(start, "J", "MARK X-");
	    addAct(start, "I", "MARK Y-");
	    addAct(start, "<", "MARK Y+");
	*/

	    addAct(start, "Enter", "EXEC");


	    addAct(start, "m", "MARK");

	    addAct(start, "u", "UNDO");
	    addAct(start, "Ctrl-U", "REDO");
	    addAct(start, "Ctrl-S", "COMMIT");

	    addAct(start, "Ctrl-F", "FINDTEXT");

	    addAct(start, "MouseMoved1", "MOUSETEMPHILIGHT");

	    addAct(start, "MouseClicked1", "MOUSESETC");
	    addAct(start, "MouseClicked2", "MOUSEEXEC");
	    addAct(start, "MouseClicked3", "MOUSEMARK");

	    addAct(start, "Delete", "DELETESCRAM");

	    addAct(start, "F1", "HELP");
	    addAct(start, "q", "QUIT");
    }

    static public void putDefTextBinds(ZZCell start) {
	start.setText("Text insert mode");
	ZZCell dir = start;
	addAct(dir, "DEFAULT", "TXTINSERT");
    }
    static public void putDefCmds(ZZCell fromHome) {
	    String [][] foo = {
		    { "NEWVIEWS" },
		    { "TextCloud.TESTWIN" }, 
/* doesn't work currently
		    { "ZZMbox.READMBOX", "ZZMbox.CREATEFLOBWINDOW" },
*/
		    { "NileDemo.UPDATE" },
		    { "SplitNileDemo.UPDATE" },
		    { "CreateCursors" },
		    { "UPDATEWINS" },
                    { "EDITVIEW" } ,
		    { "XML.IMPORT" } /* , 
		    { "---- (obsolete?)" },
		    { "CNCT X+", "CNCT X-" },
		    { "CNCT Y+", "CNCT Y-" },
		    { "CNCTI X+", "CNCTI X-" },
		    { "CNCTI Y+", "CNCTI Y-" },
		    { "CNCTC X+", "CNCTC X-" },
		    { "CNCTC Y+", "CNCTC Y-" },
		    { "SETDIMS", "SETCDIMS" },
		    //{ "RENDER" },
		    { "TestClang1.1" },
		    { "QUIT" }, */
	    };
	    ZZCell curv = fromHome;
	    for(int i=0; i<foo.length; i++) {
		    curv = curv.N("d.2", 1);
		    ZZCell curh = curv;
		    for(int j=0; j<foo[i].length; j++) {
			    if(j>0) curh = curh.N("d.1", 1);
			    curh.setText(foo[i][j]);
		    }
	    }
	    if(true) {
		foo = new String[][]{
		    { "Dumper.Create" },
		    { "Dumper.Read" },
		    { "Dumper.Write" },
		    { "Dumper.Dump" },
		    { "Dumper.Enter" },
		    { "Dumper.ChangeID" },
		    { "HOME" },
		};
		curv = fromHome.N("d.1", 1);
		// curv.setText("Dumping"); // XXX -- set or don't?
					    // (what's less confusing?)
		for(int i=0; i<foo.length; i++) {
		    curv = curv.N("d.2", 1);
		    ZZCell curh = curv;
		    for(int j=0; j<foo[i].length; j++) {
			    if(j>0) curh = curh.N("d.1", 1);
			    curh.setText(foo[i][j]);
		    }
		}
	    }
	    if(false) {
		foo = new String[][]{
		    { "SNDS T0-", "SNDS T0+" },
		    { "SNDS T1+", "SNDS T1-" },
		    { "SNDL T0-", "SNDL T0+" }, // locked to the other one
		    { "SNDL T1+", "SNDL T1-" },
		    { "SNDSPLIT", "SNDJOIN+" },
		    { "ZZPlayer.PLAY", "ZZPlayer.PLAYLOOP", 
		    	"ZZPlayer.PAUSE", "ZZPlayer.STOP" },
		    { "ZZPlayer.CRDEMO" },
		};
		curv = fromHome.N("d.1", 1);
		curv.setText("Sound");
		for(int i=0; i<foo.length; i++) {
		    curv = curv.N("d.2", 1);
		    ZZCell curh = curv;
		    for(int j=0; j<foo[i].length; j++) {
			    if(j>0) curh = curh.N("d.1", 1);
			    curh.setText(foo[i][j]);
		    }
		}
	    }
    }
}
