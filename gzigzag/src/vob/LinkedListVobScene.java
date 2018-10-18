/*   
LinkedListVobScene.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
 *
 *    You may use and distribute under the terms of either the GNU Lesser
 *    General Public License, either version 2 of the license or,
 *    at your choice, any later version. Alternatively, you may use and
 *    distribute under the terms of the XPL.
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
import java.util.*;
import java.awt.*;

/** A VobScene/VobStore implementation based on a linked list of voblings.
 *  Known problems: a) does not support interpolation into a different
 * subvobset-- need tmp vobs for that; b) vobs() isn't implemented yet.
 */

public final class LinkedListVobScene extends VobBox 
				      implements RandomAccessVobScene {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println("LinkedListVobScene: "+s); }
    static final void pa(String s) { System.out.println("LinkedListVobScene: "+s); }

    public LinkedListVobScene(Dimension d) {
	super(null);
	size = d;
	sinfo = new SceneInfo();
    }
    public LinkedListVobScene(LinkedListVobScene parent, Object key,
			      BoxStyle style, int x, int y) {
	super(key);
	size = new Dimension(x, y);
	this.parent = parent;
	this.style = style;
	sinfo = parent.sinfo;
    }

    Dimension size;
    public void setSize(Dimension d) { size = d; }
    public Dimension getSize() { return size; }

    LinkedListVobScene parent;
    BoxStyle style;
    SceneInfo sinfo;
    Depthling first = null;
    Vobling last = null;
    
    
    
    
    /** A class holding a vob, and its position on the screen. 
     */
    static class Vobling {
	Vob vob;
	int x, y, w, h, depth;
	
	Vobling(Vob vob, int depth, int x, int y, int w, int h) { 
	    this.vob = vob; this.depth = depth;
	    this.x = x; this.y = y; this.w = w; this.h = h;
	}
	
	/** The previous and next elements in the linked list of vobs in this
	 *  scene or subscene.
	 */
	Vobling prev, next;
	
	/** The coordinates we interpolate to. */
	int ix, iy, iw, ih;
	boolean interpolate = false;
	
	/** The next vobling in the linked list of voblings with the same key.
	 */
	Vobling nextWithKey = null;
	
	/** Render this vob at fraction 0. */
	void render(Graphics g, Vob.RenderInfo info) {
		vob.render(g, x, y, w, h, false, info);
	}
	
	/** Render this vob at the given fraction. */
	void render(Graphics g, Vob.RenderInfo info, float f0) {
		if(f0 == 0)
		    vob.render(g, x, y, w, h, false, info);
		else if(interpolate) {
		    float f1 = 1-f0;
		    vob.render(g, (int)(0.5 + f1 * x + f0 * ix), 
				  (int)(0.5 + f1 * y + f0 * iy), 
				  (int)(0.5 + f1 * w + f0 * iw), 
				  (int)(0.5 + f1 * h + f0 * ih), false, info);
		}
	}
	
	/** Get the interpolated coordinates for this vob. 
	 *  @see Vob.RenderInfo.getInterpCoords
	 */
	boolean getInterpCoords(float f0, Vob.Coords writeInto) {
	    if(f0 == 0) {
		writeInto.x = x; writeInto.y = y;
		writeInto.width = w; writeInto.height = h;
		writeInto.depth = depth;
		return true;
	    } else if(interpolate) {
		float f1 = 1-f0;
		writeInto.x = (int)(0.5 + f1 * x + f0 * ix);
		writeInto.y = (int)(0.5 + f1 * y + f0 * iy);
		writeInto.width = (int)(0.5 + f1 * w + f0 * iw);
		writeInto.height = (int)(0.5 + f1 * h + f0 * ih);
		writeInto.depth = depth;
		return true;
	    } else
		return false;
	}
    }

    /** The first vobling of a given depth. */
    static class Depthling extends Vobling {
	/** The Depthling for the next depth. */
	Depthling nextDepth;
	
	Depthling(Depthling prev, Depthling next,
		  Vob vob, int depth, int x, int y, int w, int h) {
	    super(vob, depth, x, y, w, h);
	    if(prev != null) prev.nextDepth = this;
	    if(next != null) nextDepth = next;
	}
	
	/** Insert a vob before this depthling. */
	void insert(Vobling v) {
	    if(prev != null) {
		prev.next = v; v.prev = prev;
	    }
	    prev = v; v.next = this;
	}
    }

    /** A class with info about a LinkedListVobScene.
     *  Shared by all LinkedListVobScene objects in a tree.
     */
    static final class SceneInfo {
	/** The voblings, by key of their vob.
	 *  A hashtable of voblings; the voblings form linked lists.
	 */
	HashMap byKey = new HashMap();

	/** The voblings with key null, as a linked list. 
	 */
	Vobling nullKey = null;

	/** The vobscene for which we have prepared an interpolation.  
	 */
	VobScene curInterp = null;
	
	/** The top element of the tree. */
	LinkedListVobScene top;

	/** ARGH: should be set from the outside/be in RenderInfo! */
	float interpFract = 0;
    }

    public void put(Vob vob, int depth, int x, int y, int w, int h) {
	Vobling v;
	
	Depthling prev = null, cur = first;
	while(cur != null && cur.depth <= depth) {
	    prev = cur;
	    cur = cur.nextDepth;
	}
	
	// XXX SIMPLIFY THE FOLLOWING PART !!!!!

	if(cur == null) {
	    // insert at the end of the list
	    Depthling d = new Depthling(prev, null, vob, depth, x, y, w, h);
	    if(last != null) {
	        d.prev = last;
		last.next = d;
	    }
	    last = d;
	    v = d;
	
	    if(prev == null) {
		// insert at the beginning of the list
		first = d;
	    }
	} else if(prev == null || prev.depth < depth) {
	    // insert as depthling
	    Depthling d = new Depthling(prev, cur, vob, depth, x, y, w, h);
	    v = d;
	    cur.insert(d);
	
	    if(prev == null) {
		// insert at the beginning of the list
		first = d;
	    }
	} else {
	    // insert as vobling
	    v = new Vobling(vob, depth, x, y, w, h);
	    cur.insert(v);
	}
	
	if(vob.key != null) {
	    v.nextWithKey = (Vobling)sinfo.byKey.get(vob.key);
	    sinfo.byKey.put(vob.key, v);
	} else {
	    v.nextWithKey = sinfo.nullKey;
	    sinfo.nullKey = v;
	}
    }

    public Vobling lookup(Object key) {
	if(key != null)
	    return (Vobling)sinfo.byKey.get(key);
	else
	    return sinfo.nullKey;
    }
    public Vobling lookupVob(Vob vob) {
	for(Vobling v = lookup(vob.key); v != null; v = v.nextWithKey) {
	    if(v.vob == vob)
		return v;
	}
	throw new Error("Vob not found");
    }

    // XXX get() and getPrev() should operate on this part of the tree, not
    //     on the whole tree at once.

    public Vob get(Object key) {
	Vobling v = lookup(key);
	if(v != null) return v.vob;
	else return null;
    }

    public Vob getNext(Object key, Vob prev) {
	if(prev == null) return get(key);
	for(Vobling v = lookup(key); v != null; v = v.next)
	    if(v.vob == prev) {
		if(v.next == null) return null;
		else return v.next.vob;
	    }
	throw new Error("prev ("+prev+") was not found in list of vobs with "
			+"key "+key);
    }
    
    public void getCoords(Vob vob, Vob.Coords writeInto) {
	// XXX doesn't work if vob is in subscene... !
	lookupVob(vob).getInterpCoords(0, writeInto);
    }

    public Iterator keys() {
	return sinfo.byKey.keySet().iterator();
    }
    public Iterator vobs() {
	return null;
	// recursive enumeration...
    }

    public Vob getVobAt(int x, int y) {
	p("Get vob at: "+x+" "+y);
	Vob.Coords c = new Vob.Coords();
	for(Vobling v = first; v != null; v = v.next) {
	    if(!(v.x <= x && v.y <= y && v.x+v.w >= x && v.y+v.h >= y))
		continue;

	    if(v.vob instanceof VobScene) {
		Vob fromSub = ((VobScene)v.vob).getVobAt(x-v.x, y-v.y);
		if(fromSub != null) {
		    p("return from sub: "+fromSub);
		    return fromSub;
		}
		p("nothing in sub-- return sub");
	    }
	
	    p("return: "+v.vob);
	    return v.vob;
	}
	p("return null :(");
	return null;
    }


    public VobBox createSubScene(Object key, BoxStyle style, int w, int h) {
	return new LinkedListVobScene(this, key, style, w, h);
    }

    public void prepareInterpolation(VobScene vs) {
	/** The vobs in the other vobscene which we interpolate to. 
	 */
	HashMap used = new HashMap();
	Vob.Coords r = new Vob.Coords();
	
	for(Vobling v = first; v != null; v = v.next) {
	    v.interpolate = false;
	    if(v.vob.key == null) continue;
	
	    /** The vobs we could interpolate to (same key). */
	    Vob vob = vs.get(v.vob.key);
	    for(; vob != null; vob = vs.getNext(v.vob.key, vob)) {
		if(used.get(vob) == null) {
		    used.put(vob, vob);
		    v.interpolate = true;
		    vs.getCoords(vob, r);
		    v.ix = r.x; v.iy = r.y; v.iw = r.width; v.ih = r.height;
		    if(vob instanceof LinkedListVobScene
			&& v.vob instanceof VobScene)
			((LinkedListVobScene)vob).prepareInterpolation(
			    (VobScene)v.vob);
		    break;
		}
	    }
	}
    }

    public void render(Graphics g, final Color fg, final Color bg, 
		       final VobScene interpTo, final float fract) {
	p("VobScene.render called. Interp: " + fract);

	Vob.RenderInfo info = new Vob.RenderInfo() {
	    public Color getBgColor() { return bg; }
	    public float getBgFract() { return 0; }
	    public Color getMixedFgColor() { return fg; }
	    public boolean isFast() { return false; }
	    public boolean getInterpCoords(Vob vob, Vob.Coords writeInto) {
	        return lookupVob(vob).getInterpCoords(fract, writeInto);
	    }
	};
	
	if(interpTo != null && interpTo != sinfo.curInterp) {
	    prepareInterpolation(interpTo);
	    sinfo.curInterp = interpTo;
	}
	
	sinfo.interpFract = fract;
	render(g, 0, 0, size.width, size.height, false, info);
    }

    public void render(Graphics g, int x, int y, int w, int h,
		       boolean boxDrawn, RenderInfo info) {
	p("Vob.render called. Interp: " + sinfo.interpFract);
				
	if(style != null) style.renderBg(g, x, y, w, h, info);
	Shape clip = null;
	if(g != null) {
	    g.translate(x, y);
	    clip = g.getClip();
	    g.clipRect(0, 0, w, h);
	}
	
	for(Vobling v = last; v != null; v = v.prev)
	    v.render(g, info, sinfo.interpFract);
	
	if(g != null) {
	    g.setClip(clip);
	    g.translate(-x, -y);
	}
	if(style != null) style.renderBox(g, x, y, w, h, info);
    }
}
