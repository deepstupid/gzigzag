/*   
Level.java
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
import java.awt.*;

/** Primitive actions for use with LevelRaster.
 *  XXX join with LevelRaster?
 */

public class Level {
public static final String rcsid = "$Id: Level.java,v 1.6 2000/11/07 12:38:27 ajk Exp $";
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    // A "direction" in the ZZ space
    static class DimVector {
	String dim;
	int dir;
	DimVector rev;  // The reverse vector
	
	DimVector(String dim, int dir) {
	    this.dim = dim; this.dir = dir; rev = new DimVector(this);
	}
	DimVector(DimVector rev) { dim=rev.dim; dir=-rev.dir; this.rev=rev; }
	
	ZZCell get(ZZCell c) { return c.s(dim, dir); }
	ZZCell end(ZZCell c) { return c.h(dim, dir); }
	ZZCell end(ZZCell c, boolean ensuremove) { 
	    return c.h(dim, dir, ensuremove);
	}
	ZZCell create(ZZCell c) { return c.N(dim, dir); }
	ZZCell[] rank(ZZCell c, boolean includeThis) {
	    return c.readRank(dim, dir, includeThis);
	}
    }


    /** A set of dimension vectors: linkdim, targetdim and tunneldim.
     */
    static class DimSet {
	DimVector linkdim;
	DimVector targetdim;
	DimVector tunneldim;
	DimSet rev;
	
	DimSet(String dims[]) {
	    linkdim = new DimVector(dims[0], 1);
	    targetdim = new DimVector(dims[1], 1);
	    tunneldim = new DimVector(dims[2], 1);
	    rev = new DimSet(this);
	}
	
	DimSet(DimSet rev) {
	    linkdim = rev.targetdim.rev;
	    targetdim = rev.linkdim.rev;
	    tunneldim = rev.tunneldim.rev;
	    this.rev = rev;
	}
	
	DimSet(ZZCell viewc) {
	    ZZCell[] dimcurs = viewc.readRank("d.dims", 1, false);
	    linkdim = new DimVector(
		ZZCursorReal.get(dimcurs[0]).getText(), 1);
	    targetdim = new DimVector(
		ZZCursorReal.get(dimcurs[1]).getText(), 1);
	    tunneldim = new DimVector(
		ZZCursorReal.get(dimcurs[2]).getText(), 1);
	    rev = new DimSet(this);
	}
	
	boolean islink(ZZCell c) {
	    return targetdim.get(c) != null || rev.targetdim.get(c) != null;
	}
    }

    static class Performer {
	ZZCell viewc, c;
	DimSet ds;
	boolean lnk;
	String act;
	
	Performer(ZZCell viewc) {
	    this.viewc = viewc; c = ZZCursorReal.get(viewc);
	    ds = new DimSet(viewc);
	    lnk = ds.islink(c);
	    // XXX why doesn't this work:
	    // act = ZZDefaultSpace.getDirActionWaitingAndClear(viewc);
	    ZZCell nac = ZZDefaultSpace
		.findOnClientlist(c.getSpace(), "NextAction", true);
	    nac = nac.getOrNewCell("d.1", 1, null);
	    act = nac.getText();
	    nac.setText("");
	}
	
	public void perf(ZZCell to, DimVector dim) { perf(to, dim, c); }
	
	public void perf(ZZCell to, DimVector dim, ZZCell from) {
	    // XXX Extend to HOP and NEW!
	    // HOP just uses dim; NEW needs to check if link AFTER INSERT
	    if(act.equals(""))	    
	        ZZCursorReal.set(viewc, to);
	    else if(act.equals("HOP")) {
		if(!lnk || dim == null) return;
		to.insert(dim.dim, dim.dir, from);
	    } else if(act.equals("NEW")) {
		if(dim == null) return;
		ZZCell nc = dim.create(from);
		if(ds.islink(nc)) {
		    if(ds.targetdim.get(nc) == null) ds.targetdim.create(nc);
		    else ds.rev.targetdim.create(nc);
		}
	    } else
		System.out.println("Level module can't do "+act);
	}
	
	public void dir(int d, int b) {
	    DimSet ds = d < 0 ? this.ds.rev : this.ds;
	    ZZCell to;
	    if(lnk) {
		if(b == 0) { perf(ds.targetdim.end(c), null); return; }
		else if(b < 0) {
		    to = ds.targetdim.get(c);
		    if(to != null && ds.targetdim.get(to) != null)
			perf(to, ds.targetdim);
		} else {
		    to = ds.targetdim.rev.get(c);
		    if(to != null) perf(to, ds.targetdim.rev);
		}
	    } else {
		if(b < 0) {
		    perf(ds.linkdim.get(c), ds.linkdim);
		} else if(b == 0) {
		    to = ds.tunneldim.get(c);
		    ZZCell[] rnk = ds.linkdim.rank(c, false);
		    if(to == null && rnk.length > 0) 
			to = rnk[rnk.length / 2];
		    perf(to, ds.tunneldim);
		} else {
		    to = ds.linkdim.end(c);
		    perf(to, ds.linkdim, to);
		}
	    }
	}
    }

    static public ZZModule module = new ZZModule() {
	public void action(String id,
		ZZCell code, 
		ZZCell target,
		ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {

	    Performer p = new Performer(view.getViewcell());
					
	    if(id.equals("UpLeft")) p.dir(-1, -1);
	    else if(id.equals("Up")) p.dir(-1, 0);
	    else if(id.equals("UpRight")) p.dir(-1, 1);
	    else if(id.equals("DownLeft")) p.dir(1, -1);
	    else if(id.equals("Down")) p.dir(1, 0);
	    else if(id.equals("DownRight")) p.dir(1, 1);
	    else System.out.println("Level module: Unknown code '"+id+"'");
	}
		
	public ZOb newZOb(String id) {
	    if(id.equals("Raster")) return new LevelView();
	    return null;
	}
    };
}


