/*   
SimpleVobScene.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
package org.gzigzag.vob;
import java.awt.*;
import java.util.*;

/** A trivial implementation of VobScene.
 */

public class SimpleVobScene implements VobScene {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.out.println(s); }

    public SimpleVobScene() {
	this(200);
    }

    /**
     *  @param size the exact number of vobs to ensure this scene to hold
     *  @see #ensureSize(int, boolean)
     */
    public SimpleVobScene(int size) {
	ensureSize(size,true);
	ensureSubSize(20,true);
	n = 1;
	vobs[0] = subs[0] = new Sub(null, 0);
	insubs[0] = -1;
    }

    /** Current size of scene area
     *  @see VobPlacer#getSize()
     */
    Dimension size;
    /** Sets size of the scene area
     *  @see VobPlacer#getSize()
     */
    public void setSize(Dimension d) { size = d; }
    public Dimension getSize() { return size; }

    /** Interpolate between two integers.
     * Returns i0 * f0 + i1 * f1 rounded to the nearest integer.
     * Just shorthand.
     */
    protected final int i(int i0, int i1, float f0, float f1) {
	// XXX Negative coordinates done wrong.
	return Math.round(f0 * i0 + f1 * i1);
    }

    /** Make this VobScene have space to contain at least n vobs. 
     * It is never necessary to call this routine from outside the VobScene
     * but it may be desirable if the amount or approximate amount of vobs
     * is known beforehand - this way, the VobScene does not need to reallocate
     * its internal arrays as often.
     * @param n The size to ensure
     * @param exact Whether n is known to be a maximum or it is simply
     * 			a value that is known to be exceeded.
     */
    public void ensureSize(int n, boolean exact) {
	if(vobs == null || vobs.length < n) {
	    // Big decision: how much.
	    int all = 0;
	    if(exact)
		all = n;
	    else
		all = vobs.length * 3;
	    if(all < n) all = n;
	    Vob[] nvobs = new Vob[all];
	    int[] ncoords = new int[all*4];
	    int[] ndepths = new int[all];
	    int[] ninsubs = new int[all];
	    if(vobs != null) {
		System.arraycopy(vobs, 0, nvobs, 0, vobs.length);
		System.arraycopy(coords, 0, ncoords, 0, coords.length);
		System.arraycopy(depths, 0, ndepths, 0, depths.length);
		System.arraycopy(insubs, 0, ninsubs, 0, insubs.length);
	    }
	    vobs = nvobs;
	    coords = ncoords;
	    depths = ndepths;
	    insubs = ninsubs;
	}
    }

    /** Make this VobScene have space to contain at least n subvobsets.
     */
    public void ensureSubSize(int n, boolean exact){
	if(subs.length < n) {
	    int all = 0;
	    if(exact)
		all = n;
	    else
		all = subs.length * 3 ;
	    if(all < n) all = n;
	    Sub[] nsubs = new Sub[all];
	    System.arraycopy(subs, 0, nsubs, 0, subs.length);
	    subs = nsubs;
	}
    }

    /** Current amount of vobs. 
     */
    int n;

    /** The vobs themselves.
     */
    Vob[] vobs;

    /** The indices to interpolate towards in the other vobset.
     */
    int[] interps;
    SimpleVobScene curInterp;

    /** The coordinates of the vobs, stored as sets of fours.
     * The first ones are 0,0,w,h for the whole scene. (XXX not yet)
     */
    int[] coords;

    /** The depths of the vobs.
     */
    int[] depths;

    /** Which sets the vobs are in. 0 = main level.
     * insubs[0] = -1.
     */
    int[] insubs;

    /** The sorted indices of the vobs.
     */
    int[] inds;

    /** The subvobsets, by index. A flat index of all levels.
     * subs[0] = the global subvobset, representing everything.
     * That is here for the convenience, so that rendering the whole
     * vobset and subvobsets uses the same code.
     */
    Sub[] subs = new Sub[20];
    int nsubs;

    /** The current set we are putting things into.
     */
    int curSub = 0;

    Vob.RenderInfo curRenderInfo;

    final class Sub extends VobBox {
	/** The unique number of this subvobset.
	 */
	int index;

	/** The length in inds = number of ALL contained vobs.
	 */
	int length;

	/** The length in direct descendants.
	 */
	int directlength;

	/** The index to depth-sorted inds.
	 */
	int indIndex;

	Sub(Object key) { 
	    super(key); 
	    index = ++curSub;
	}

	Sub(Object key, int index) { 
	    super(key); 
	    this.index = index;
	}

	public void put(Vob vob, int depth, int x, int y, int w, int h) {
	    SimpleVobScene.this.put(vob, depth, x, y, w, h);
	}

	public java.awt.Dimension getSize() { return null; }

	public java.util.Iterator keys() { return null; }
	public java.util.Iterator vobs() { return null; }


	public Vob get(Object key) { return null; }
	public Vob getNext(Object key, Vob prev) { return null; }
	public void getCoords(Vob vob, Vob.Coords writeInto) { }
	public Vob getVobAt(int x, int y) { 
	    for(int i=0; i<directlength; i++) {
		int ind = inds[indIndex + i];
		if(coords[4*ind] <= x &&
		   coords[4*ind+1] <= y  &&
		   coords[4*ind] + coords[4*ind+2] >= x &&
		   coords[4*ind+1] + coords[4*ind+3] >= y)
		    return vobs[ind];
	    }
	    return null;
	}

	// VobScene
	public void render(Graphics g, Color fg, Color bg, 
			    VobScene interpTo, float fract) {

	    if(fract == 0) {
		for(int i=directlength-1; i>=0; i--) {
		    int ind = inds[indIndex + i];
		    vobs[ind].render(g, 
			    coords[4*ind], 
			    coords[4*ind+1], 
			    coords[4*ind+2], 
			    coords[4*ind+3],
				    false, curRenderInfo);
		}
	    } else {
		float f1 = 1-fract;
		float f0 = fract;
		int[] coords2 = curInterp.coords;
		for(int i=n-1; i>=0; i--) {
		    int ind = inds[i];
		    int intind = interps[ind];
		    if(intind < 0) continue;
		    vobs[ind].render(g, 
			i(coords[4*ind],   coords2[4*intind  ], f1, f0),
			i(coords[4*ind+1], coords2[4*intind+1], f1, f0),
			i(coords[4*ind+2], coords2[4*intind+2], f1, f0),
			i(coords[4*ind+3], coords2[4*intind+3], f1, f0),
				    false, curRenderInfo);
		}
	    }

	}

	public boolean animUseful(VobScene interpTo) {
	    int[] coords2 = curInterp.coords;
	    for(int i=n-1; i>=0; i--) {
		for(int nth=0; nth<4; nth++) {
		    int ind = inds[i];
		    int intind = interps[ind];
		    if(intind < 0) continue;
		    int diff = coords[4*ind + nth] - coords2[4*intind + nth];
		    diff = diff*diff;
		    if(diff > 100) return true;
		}
	    }
	    return false;
	}

	// Vob
	public void render(Graphics g, 
				    int x, int y, int w, int h,
				    boolean boxDrawn,
				    RenderInfo info
				    ) {
	}

	// This is a bit troublesome because we put() this before
	// it is really inserted.
	// However, SimpleVobScene.put() looks at whether the sub to be put
	// is the current one (pop recursion) or a new one.
	public VobBox createSubScene(Object key, BoxStyle style, int w, int h) {
	    int ind = nsubs;
	    ensureSubSize(++nsubs, false);
	    subs[ind] = new Sub(key);
	    subs[ind].index = ind;

	    int i = n;
	    ensureSize(++n, false);
	    vobs[i] = subs[ind];
	    insubs[i] = curSub;
	    curSub = ind;

	    return subs[ind];
	}

	public void setInterpCoords(VobScene interpTo, float fract) {
	    float f1 = 1-fract;
	    float f0 = fract;
	    int[] coords2 = curInterp.coords;
	    for(int i=n-1; i>=0; i--) {
		int ind = inds[i];
		int intind = interps[ind];
		if(intind < 0) continue;
		coords[4*ind+0] = 
		    i(coords[4*ind],   coords2[4*intind  ], f1, f0);
		coords[4*ind+1] = 
		    i(coords[4*ind+1], coords2[4*intind+1], f1, f0);
		coords[4*ind+2] = 
		    i(coords[4*ind+2], coords2[4*intind+2], f1, f0);
		coords[4*ind+3] = 
		    i(coords[4*ind+3], coords2[4*intind+3], f1, f0);
	    }
	}

	public void dump(String prefix) {
	    pa(prefix+"Subvob");
	    for(int i=0; i<directlength; i++) {
		int ind = inds[indIndex + i];
		pa(prefix+"Vob "+i+" ind "+ind+" depth "+depths[ind]);
		if(vobs[ind] instanceof Sub) {
		    ((Sub)vobs[ind]).dump(prefix+"  ");
		} else {
		    p(prefix+" is vob "+vobs[ind]);
		}
	    }
	}
    }

    final public void put(Vob vob, int depth, int x, int y, int w, int h) {
	if(curSub != 0)
	    throw new Error("May not insert into main set while subvobset on");
	put(vob, depth, x, y, w, h, 0);
    }

    final public 
	  void put(Vob vob, int depth, int x, int y, int w, int h, int set) {
	int i = n;
	ensureSize(++n, false);
	vobs[i] = vob;
	depths[i] = depth;
	coords[4*i+0] = x;
	coords[4*i+1] = y;
	coords[4*i+2] = w;
	coords[4*i+3] = h;
	insubs[i] = set;
	putHash(i, vob);
    }

    public Vob get(Object key) {
	if(key == null) return null;
	int ind = findIndex(key, -1);
	if(ind < 0) return null;
	return vobs[ind];
    }

//----------------------------------------------------
// 		HASH INDEX

    /** The hash indices of the vobs.
     */
    int[] hashinds = new int[2000];

    /** Find the index of the given object in the given set.
     */
    int findIndex(Object key, int set) {
	if(key == null) return -1;
	int start = key.hashCode() % hashinds.length;
	if(start < 0) start += hashinds.length;
	while(hashinds[start] != 0) {
	    if(key.equals(vobs[hashinds[start]-1].key)) {
		if(set != -1 &&
		    insubs[hashinds[start]] != set)
			continue;
		return hashinds[start]-1;
	    }
	    start ++;
	    start %= hashinds.length;
	}
	return -1;
    }

    /** Find the index of the given Vob.
     */
    int findIndex(Vob vob) {
	if(vob.key == null || true
	) {
	    // XXX SLOW!
	    for(int i=0; i<n; i++)
		if(vobs[i] == vob) return i;
	}
	return -1;
    }

    void putHash(int i, Vob vob) {
	if(vob.key == null) return;
	if(n > hashinds.length / 2)
	    rehash(n * 4);
	int start = vob.key.hashCode() % hashinds.length;
	if(start < 0) start += hashinds.length;
	// linear probing
	while(hashinds[start] != 0) {
	    start ++;
	    start %= hashinds.length;
	}
	hashinds[start] = i+1;
    }

    void rehash(int size) {
	hashinds = new int[size];
	for(int i=0; i<n; i++) 
	    putHash(i, vobs[i]);
    }

    public Vob getNext(Object key, Vob prev) {
	throw new Error("Not implemented");
    }


//--------------------------------------------------------
// 		SORTING

    /** Sort vobs of the subscene in vobs[ind] and place sorted
     * indices into indsind.
     * Uses recursion in an interesting way.
     */
    final int sortLevel(int ind, int length, int indsind) {
	int curinds;
	int curend = indsind + length;
	int nind = 0;
	int indstart = indsind;
	for(int i=0; i<length; i++) {
	    inds[indsind ++] = ind+i;
	    if(vobs[ind+i] instanceof Sub) {
		Sub s = (Sub)(vobs[ind+i]);
		s.directlength = 
		    sortLevel(ind+i+1, s.length, curend - s.length);
		s.indIndex = curend - s.length;
		curend -= s.length;
		i += s.length;
	    } 
	}
	qsort(indstart, indsind-indstart);
	return indsind - indstart;
    }

    void depthSort() {
	if(inds != null && inds.length == n) return;
	inds = new int[n];
	Sub topSub = ((Sub)vobs[0]);
	topSub.length = n-1;
	// topSub.directlength = 
	sortLevel(0, n, 0);
    }

    private void qsort(int min, int n) {
	if(n <= 1) return;
	// First, find the median of the first, middle and end members.
	int i1 = min; 
	int i2 = min+(n-1)/2;
	int i3 = min+n-1;
	int tmp;
	if(cmp(i1, i2) > 0)  {tmp = i1; i1 = i2; i2 = tmp; }
	if(cmp(i2, i3) > 0)  {tmp = i2; i2 = i3; i3 = tmp; }
	if(cmp(i1, i2) > 0)  {tmp = i1; i1 = i2; i2 = tmp; }
	// i2 is the median
	int median = i2;

	// swap the median to the lowest place
	tmp = inds[min];
	inds[min] = inds[median];
	inds[median] = tmp;

	median = min;
	int i = min + 1;
	int j = min + n -1;

	OUTER: while(j > i) {
	    while(cmp(i, median) < 0) {
		i++;
		if(i >= j) break OUTER;
	    }
	    while(cmp(j, median) > 0) {
		j--;
		if(i >= j) break OUTER;
	    }
	    tmp = inds[i];
	    inds[i] = inds[j];
	    inds[j] = tmp;
	}

	if(cmp(median, j) > 0) {
	    tmp = inds[j];
	    inds[j] = inds[median];
	    inds[median] = tmp;
	}

	qsort(min, j-min);
	qsort(j, min+n-j);
    }

    private final int cmp(int ind1, int ind2) {
	ind1 = inds[ind1];
	ind2 = inds[ind2];
	if(depths[ind1] > depths[ind2]) return 1;
	if(depths[ind1] < depths[ind2]) return -1;
	if(ind1 > ind2) return 1;
	if(ind1 < ind2) return -1;
	return 0;
    }

//---------------------------------------------------------------


    public java.util.Iterator keys() {
        throw new Error("Not implemented");
    }

    public java.util.Iterator vobs() { 
	return new Iterator() {
	    int ind = 0;
	    public boolean hasNext() {
		return (ind < n);
	    }
	    public Object next() {
		return vobs[ind++];
	    }
	    public void remove() { throw new Error("Not impl"); }
	};
    }

    public void getCoords(Vob vob, Vob.Coords out) {
	int i = findIndex(vob);
	out.x = coords[4*i + 0];
	out.y = coords[4*i + 1];
	out.width = coords[4*i + 2];
	out.height = coords[4*i + 3];
	out.depth = depths[i];
    }

    public Vob getVobAt(int x, int y) {
	depthSort();
	return ((Sub)vobs[0]).getVobAt(x, y);
    }

    // Subscenes have coordinates temporarily.
    public VobBox createSubScene(Object key, BoxStyle style, int w, int h) {
	return ((Sub)vobs[0]).createSubScene(key, style, w, h);
    }

    private void setInterp(VobScene interpTo) {
	if(interpTo != null && interpTo != curInterp) {
	    if(curInterp != interpTo) {
		curInterp = (SimpleVobScene)interpTo;
		interps = new int[n];
		for(int i=0; i<n; i++) {
		    interps[i] = curInterp.findIndex(vobs[i].key, -1);
		}
	    }
	}
    }

    /** Prepare rendering. Useful for ensuring that first
     * interpolated rendering doesn't take more time than
     * the others and cause a jump.
     */
    public void prepareRender(VobScene interpTo) {
	depthSort();
	setInterp(interpTo);
    }

    public void render(Graphics g, final Color fg, final Color bg, 
		    VobScene interpTo, float fract) {
	depthSort();
	final boolean fast = (fract != 0);

	curRenderInfo = new Vob.RenderInfo() {
	    public Color getBgColor() { return bg; }
	    public float getBgFract() { return 0; }
	    public Color getMixedFgColor() { return fg; }
	    public boolean isFast() { return fast; }
	    public boolean getInterpCoords(Vob vob, Vob.Coords writeInto) {
		return false;
	    }
	};
	setInterp(interpTo);
	((Sub)vobs[0]).render(g, fg, bg, interpTo, fract);
    }

    public boolean animUseful(VobScene to) {
	depthSort();
	setInterp(to);
	return ((Sub)vobs[0]).animUseful(to);
    }

    /** Set the coordinates of the vobs to the ones interpolated
     * with the given scene.
     */
    public void setInterpCoords(VobScene interpTo, float fract) {
	depthSort();
	setInterp(interpTo);
	((Sub)vobs[0]).setInterpCoords(interpTo, fract);
    }

    public void dump() {
	pa("VOBSCENE: ");
	((Sub)vobs[0]).dump("  ");
    }
 
}
