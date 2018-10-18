/*   
TrivialVobScene.java
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

/** A more trivial implementation of VobScene.
 *  The subscene implementation is very incomplete: e.g., the vobs in a
 *  subscene aren't rendered at the same time, but together with the vobs
 *  in the main scene which have the same depth.
 * <p>
 */

public class TrivialVobScene implements ExtensibleVobScene,
                                        DecoratableVobScene {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println("TrivialVobScene: "+s); }
    static final void pa(String s) { System.out.println("TrivialVobScene: "+s); }

    public TrivialVobScene(Dimension d) {
	size = d;
    }

    Dimension size;
    public void setSize(Dimension d) { size = d; }
    public Dimension getSize() { return size; }

    /** A class holding a vob, and its position on the screen. 
     */
    class Vobling {
	Vob vob;
	int x, y, w, h, depth;
	
	Vobling(Sub sub, Vob vob, int depth, int x, int y, int w, int h) { 
	    this.sub = sub; this.vob = vob; this.depth = depth;
	    this.x = x; this.y = y; this.w = w; this.h = h;
	}
	
	/** The subscene this vob is in. Null if main vobscene. */
	Sub sub;
	
	/** The coordinates we interpolate to. */
	int ix, iy, iw, ih;
	boolean interpolate = false;
	
	/** Set to false if we interpolate outside our subvobset. */
	boolean interpolateClip;
	
	/** The next vobling in the linked list of voblings with the same key.
	 */
	Vobling next = null;
	
	/** Render this vob at fraction 0. */
	void render(Graphics g, Vob.RenderInfo info) {
		vob.render(g, x, y, w, h, false, info);
	}
	
	/** Render this vob at the given fraction. */
	void render(Graphics g, Vob.RenderInfo info, float f0) {
		Shape clip = null;
		if(sub != null && g != null && (f0 == 0 || interpolateClip)) {
		    clip = g.getClip();
		    sub.setClip(g, info);
		}
		if(f0 == 0)
		    vob.render(g, x, y, w, h, false, info);
		else if(interpolate) {
		    float f1 = 1-f0;
		    vob.render(g, (int)(0.5 + f1 * x + f0 * ix), 
				  (int)(0.5 + f1 * y + f0 * iy), 
				  (int)(0.5 + f1 * w + f0 * iw), 
				  (int)(0.5 + f1 * h + f0 * ih), false, info);
		}
		if(sub != null && g != null)
		    g.setClip(clip);
	}
	
	/** Get the interpolated coordinates for this vob. 
	 *  @see Vob.RenderInfo.getInterpCoords
	 */
	boolean getInterpCoords(float f0, Vob.Coords writeInto) {
	    int subx=0, suby=0, subw=size.width, subh=size.height;
	    boolean visible = true;
	    if(sub != null) {
		sub.vobling.getInterpCoords(f0, writeInto);
		if(!writeInto.visible) visible = false;
		else {
		    subx = writeInto.x; suby = writeInto.y;
		    subw = writeInto.width; subh = writeInto.height;
		}
	    }
	    if(f0 == 0) {
		writeInto.x = x; writeInto.y = y;
		writeInto.width = w; writeInto.height = h;
		writeInto.depth = depth;
		if(visible && 
		   x + w/2 > subx && x + w/2 < subx + subw &&
		   y + h/2 > suby && y + h/2 < suby + subh)
		    writeInto.visible = true;
		else
		    writeInto.visible = false;
		return writeInto.visible;
	    } else if(interpolate) {
		float f1 = 1-f0;
		writeInto.x = (int)(0.5 + f1 * x + f0 * ix);
		writeInto.y = (int)(0.5 + f1 * y + f0 * iy);
		writeInto.width = (int)(0.5 + f1 * w + f0 * iw);
		writeInto.height = (int)(0.5 + f1 * h + f0 * ih);
		writeInto.depth = depth;
                if(visible &&
		   writeInto.x + writeInto.width/2 > subx && 
		   writeInto.x + writeInto.width/2 < subx + subw &&
                   writeInto.y + writeInto.height/2 > suby && 
		   writeInto.y + writeInto.height/2 < suby + subh)
                    writeInto.visible = true;
                else
                    writeInto.visible = false;
		return writeInto.visible;
	    } else
		return false;
	}
    }

    /** All voblings in one vector. 
     */
    ArrayList voblings = new ArrayList();

    /** The voblings by depth: an array of vectors, one vector for each depth.
     *  @see ensureDepth
     */
    ArrayList[] byDepth = new ArrayList[10];

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

    /** The index hooks.
     */
    ArrayList indexHooks = new ArrayList();

    /** The decoration for this vobscene. */
    Vob decor;

    public void setDecor(Vob decor) { this.decor = decor; }

    public final void ensureDepth(int n) {
	if(byDepth.length <= n) {
	    ArrayList[] nu = new ArrayList[n + 1];
	    if(byDepth != null)
		System.arraycopy(byDepth, 0, nu, 0, byDepth.length);
	    byDepth = nu;
	}
    }

    public void put(Vob vob, int depth, int x, int y, int w, int h) {
	put(null, vob, depth, x, y, w, h);
    }

    public void put(Sub sub, Vob vob, int depth, int x, int y, int w, int h) {
        Vobling v = new Vobling(sub, vob, depth, x, y, w, h);

	if(vob instanceof Sub) {
	    ((Sub)vob).vobling = v;
	    // ARGH: translate coords of all vobs inside that Sub
	    for(Iterator iter = voblings.iterator(); iter.hasNext();) {
		Vobling v2 = (Vobling)iter.next();
		if(v2.sub == vob) {
		    v2.x += x; v2.y += y;
		}
	    }
	}

	voblings.add(v);
	
	ensureDepth(depth);
	if(byDepth[depth] == null)
	    byDepth[depth] = new ArrayList();
	byDepth[depth].add(v);
	
	if(vob.key != null) {
	    v.next = (Vobling)byKey.get(vob.key);
	    byKey.put(vob.key, v);
	}
    }

    public final Vobling lookup(Object key) {
	if(key != null)
	    return (Vobling)byKey.get(key);
	else
	    return nullKey;
    }
    public final Vobling lookupVob(Vob vob) {
	for(Vobling v = lookup(vob.key); v != null; v = v.next) {
	    if(v.vob == vob)
		return v;
	}
	throw new Error("Vob not found");
    }

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
	lookupVob(vob).getInterpCoords(0, writeInto);
    }

    public Iterator keys() {
	return byKey.keySet().iterator();
    }
    public Iterator vobs() { 
	return new Iterator() {
	    Iterator iter = voblings.iterator();
	    public boolean hasNext() { return iter.hasNext(); }
	    public Object next() { 
		Vobling v = (Vobling)iter.next();
		if(v == null) return null;
		return v.vob;
	    }
	    public void remove() { throw new Error("Not impl"); }
	};
    }

    public Vob getVobAt(int x, int y) {
	p("Get vob at: "+x+" "+y);
	Vob.Coords c = new Vob.Coords();
	for(int d=0; d<byDepth.length; d++) {
	   if(byDepth[d] == null) continue;
	   p("...depth "+d);
	   for(Iterator iter=byDepth[d].iterator(); iter.hasNext();) {
		Vobling v = (Vobling)iter.next();
		if(v.vob instanceof Sub) continue; // XXX
		if(!v.getInterpCoords(0, c)) continue;
		int dx = x - c.x, dy = y - c.y;
		if(dx >= 0 && dx <= c.width && dy >= 0 && dy <= c.height) {
		    p("return vob: "+v.vob);
		    return v.vob;
		}
	   }
	}
	p("return null :(");
	return null;
    }
    public VobBox createSubScene(Object key, BoxStyle style, int w, int h) {
	return new Sub(key, new Dimension(w, h));
    }

    public void prepareInterpolation(VobScene vs0) {
	// XXX change VobScene so that we can interpolate to any scene:
	//     requires knowledge about in which subscene vobs are
	TrivialVobScene vs = (TrivialVobScene)vs0;
		
	/** The vobs in the other vobscene which we interpolate to. 
	 */
	HashMap used = new HashMap();
	Vob.Coords r = new Vob.Coords();
	
	/** First, try to interpolate in the same subvobset.
	 */
	for(Iterator iter = voblings.iterator(); iter.hasNext();) {
	    Vobling v = (Vobling)iter.next();
	    v.interpolate = false;
	    if(v.vob.key == null) continue;
	
	    /** The vobs we could interpolate to (same key). */
	    Vob vob = vs.get(v.vob.key);
	    for(; vob != null; vob = vs.getNext(v.vob.key, vob)) {
		Sub sub = vs.lookupVob(vob).sub;
		if(v.sub == null && sub != null) continue;
		if(v.sub != null) {
		    if(sub == null) continue;
		    if(!v.sub.key.equals(sub.key)) continue;
		}
		
		if(used.get(vob) == null) {
		    used.put(vob, vob);
		    v.interpolate = true;
		    v.interpolateClip = true;
		    vs.getCoords(vob, r);
		    v.ix = r.x; v.iy = r.y; v.iw = r.width; v.ih = r.height;
		    break;
		}
	    }
	}
	
	/** Second, try to interpolate what we haven't interpolated yet.
	 *  Note: the interpolate flag is now set in the voblings; we only
	 *  consider vobs which have interpolate set to false.
	 */
	for(Iterator iter = voblings.iterator(); iter.hasNext();) {
	    Vobling v = (Vobling)iter.next();
	    if(v.vob.key == null) continue;
	    if(v.interpolate) continue;
	
	    /** The vobs we could interpolate to (same key). */
	    Vob vob = vs.get(v.vob.key);
	    while(vob != null) {
		if(used.get(vob) == null) {
		    used.put(vob, vob);
		    v.interpolate = true;
		    v.interpolateClip = false;
		    vs.getCoords(vob, r);
		    v.ix = r.x; v.iy = r.y; v.iw = r.width; v.ih = r.height;
		    break;
		}
		vob = vs.getNext(v.vob.key, vob);
	    }
	}

	curInterp = vs;
    }

    public void render(Graphics g, final Color fg, final Color bg, 
		       final VobScene interpTo, final float fract) {

	Vob.RenderInfo info = new Vob.RenderInfo() {
	    public Color getBgColor() { return bg; }
	    public float getBgFract() { return 0; }
	    public Color getMixedFgColor() { return fg; }
	    public boolean isFast() { return false; }
	    public boolean getInterpCoords(Vob vob, Vob.Coords writeInto) {
	        return lookupVob(vob).getInterpCoords(fract, writeInto);
	    }
	};

	if(interpTo != null && interpTo != curInterp)
	    prepareInterpolation(interpTo);

	if(decor != null)
	    decor.render(g, 0, 0, size.width, size.height, false, info);
	
	for(int d = byDepth.length-1; d >= 0; d--) {
	    if(byDepth[d] == null) continue;
	    for(Iterator iter = byDepth[d].iterator(); iter.hasNext();) {
		Vobling v = (Vobling)iter.next();
		v.render(g, info, fract);
	    }
	}
    }

    public void addHook(VobPlacer hook) {
	indexHooks.add(hook);
    }

    public VobPlacer getIndexHook(Class type) {
	for(int i=0; i<indexHooks.size(); i++) {
	    if(indexHooks.get(i).getClass().equals(type))
		return (VobPlacer)indexHooks.get(i);
	}
	return null;
    }

    /** A subscene of this scene. */
    class Sub extends VobBox {
	Dimension size;
	Sub(Object key, Dimension size) { super(key); this.size = size; }
	public void put(Vob vob, int depth, int x, int y, int w, int h) {
	    TrivialVobScene.this.put(this, vob, depth, x, y, w, h);
	}
	public java.awt.Dimension getSize() { return size; }
	public java.util.Iterator keys() { return null; }
	public java.util.Iterator vobs() { return null; }
	public Vob get(Object key) { return null; }
	public Vob getNext(Object key, Vob prev) { return null; }
	public void getCoords(Vob vob, Vob.Coords writeInto) {}
	public Vob getVobAt(int x, int y) { return null; }

	// VobScene
	public void render(Graphics g, Color fg, Color bg, 
			    VobScene interpTo, float fract) {}
	// Vob
	public void render(Graphics g, int x, int y, int w, int h,
			   boolean boxDrawn, RenderInfo info) {}
	public VobBox createSubScene(Object key, BoxStyle style, int w, int h) {
	    return null;
	}

	Vobling vobling;
	
	Vob.Coords coords = new Vob.Coords();
	void setClip(Graphics g, RenderInfo info) {
	    info.getInterpCoords(this, coords);
	    g.clipRect(coords.x, coords.y, coords.width, coords.height);
	}
    }

}
