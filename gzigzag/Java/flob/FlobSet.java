/*   
FlobSet.java
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
package org.gzigzag;
import java.util.*;
import java.awt.*;

/** A set of flobs, i.e.things that are rendered to screen associated
 * with cells.
 * Flobs are stored with depths, which are stably sorted in the end 
 * so that the flobs placed later are on top.
 */

public class FlobSet implements ZZScene {
public static final String rcsid = "$Id: FlobSet.java,v 1.41 2001/03/18 17:50:16 bfallenstein Exp $";

    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    Dimension size;
    public Dimension getSize() {
	return size;
    }
    Color fg, bg;
    Component cpt;

    public SceneFlob sflob = null;
    public BoxType box = new SimpleBoxType();

    public FlobSet(Dimension size, Color foreground, Color background, 
		Component cpt) {
	this.size = size;
	this.bg = background;
	this.fg = foreground;
	this.cpt = cpt;
    }
    public Component getComponent() { return cpt; }
    public Color getForeground() { return fg; }
    public Color getBackground() { return bg; }


    private Hashtable c2ph;
    private Hashtable nph; // Hashtable for flobs w/o parents

    private final Hashtable getPH(Flob parent) {
	if(parent == null)
	    return nph;
	Hashtable res = (Hashtable)c2ph.get(parent);
	if(res == null) 
	    c2ph.put(parent, res=new Hashtable());
	return res;
    }

    private int nflobs;
    private Flob[] flobs = new Flob[30];
    boolean fsorted = false;
    boolean ftagged = false;

    
    private int nrend;
    /** Decorations.
     */
    private Renderable[] rends = new Renderable[10];
    boolean rsorted = false;

    private int maxdepth = 0;
    
    public void add(Renderable r) {
	// Check: if it happens to be a flob, then put
	// it as a flob.
	if(r instanceof Flob) {
	    add((Flob)r);
	    return;
	}
	
	if(r.d > maxdepth) maxdepth = r.d;
	if(nrend >= rends.length) {
	    Renderable[] n = new Renderable[nrend + 50];
	    System.arraycopy(rends, 0, n, 0, nrend);
	    rends = n;
	}
	rends[nrend++] = r;
	rsorted = false;
    }

    /** Add a flob into the flobset.
     * Note that flobPath might not be set yet.
     * It is therefore used only when the flobs are being sorted.
     */
    public void add(Flob f) {
	// p("AddFlob "+f.x+" "+f.y+" "+f.w+" "+f.h+" "+f.d);
	if(f.d <= 0) 
	    throw new ZZError("Negative or zero depth for flob!");
	if(f.d > maxdepth) maxdepth = f.d;
	// if(f.c != null) 
	//    getPH(f.flobPath).put(f.c, f);
	if(nflobs >= flobs.length) {
	    Flob[] n = new Flob[nflobs + 100];
	    System.arraycopy(flobs, 0, n, 0, nflobs);
	    flobs = n;
	}
	flobs[nflobs++] = f;
	fsorted = false;
	ftagged = false;
	if(sflob != null && f instanceof SceneFlob)
	    ((SceneFlob)f).parent = sflob;
    }

    public final void tag() {
	if(ftagged) return;
	c2ph = new Hashtable(); nph = new Hashtable();
	for(int i=0; i<nflobs; i++) {
	    Flob f = flobs[i];
	    if(f.c != null) {
	        Hashtable h = getPH(f.getParent());
	        Object o = h.get(f.c);
	        if(o == null) {
		    h.put(f.c, f);
	        } else if(o instanceof Flob) {
		    Vector v = new Vector();
		    v.addElement(o);
		    v.addElement(f);
		    h.put(f.c, v);
	        } else {
		    ((Vector)o).addElement(f);
	        }
	    }
	}
	ftagged = true;
    }

    public final void sort() { sortf(); sortr(); }
    public final void sortf() { 
	if(fsorted) return;
	// pa("SORTING");
	// dump();
	Renderable.depthSort(flobs, nflobs); 
	fsorted = true;
	// dump();
    }
    public final void sortr() { 
	if(rsorted) return;
	Renderable.depthSort(rends, nrend); 
	rsorted = true;
    }

    protected void depthColor(Graphics g, int depth) {
	// XXX Foreground adjustable?
	if(depth > maxdepth) 
	    throw new ZZError("Too strange. "+depth+" "+maxdepth);
	int fract = (256*(depth-1)) / (maxdepth + maxdepth / 10);
	Color ncol = ZZUtil.mix(fg, bg, fract);
	g.setColor(ncol);
	// p("Color: "+fract+" "+d+" "+maxdepth+" "+ncol);
    }

    public void render(Graphics g) {
	render(g, g);
    }
    public void render(Graphics g, Graphics orig) {

	// ZZDrawing.instance.setAlpha(g, (float)0.5);
	ZZDrawing.instance.setDefaults(g);
	ZZDrawing.instance.setQuality(g);

	int f=nflobs-1;
	int r=nrend-1;
	int oldd = -1;
	sort();
	// p("StartColors");
	while(f >= 0 || r >= 0) {
	    // Stagger the two arrays. Always render decorations of the same
	    // depth last
	    while(f >= 0 && 
	     ( r < 0 || rends[r].d <= flobs[f].d)) {
	        if(flobs[f].d != oldd)
		    depthColor(g, oldd = flobs[f].d);
		if(!flobs[f].needsBox())
		    flobs[f--].render(g, orig);
		else {
		    Flob fl = flobs[f--];
		    box.renderBg(g, fl);
		    fl.render(g, orig);
		    box.renderFrame(g, fl);
		}
	    }
	    while(r >= 0 && 
	     ( f < 0 || rends[r].d > flobs[f].d)) {
	        if(rends[r].d != oldd)
		    depthColor(g, oldd = rends[r].d);
		rends[r--].render(g, orig);
	    }
	}
	renderDC(g);
    }

    FlobSet interpPrepared = null;
    public void renderInterp(Graphics g, ZZScene other0, 
			     float fract) {

	ZZDrawing.instance.setDefaults(g);

	sort();
	FlobSet other = (FlobSet)other0;
	if(interpPrepared != other) {
	    prepareInterp(other);
	}
	renderInterp(g, fract);
    }

    public void renderInterp(Graphics g, float fract) {
	int oldd = -1;
	for(int i=nflobs-1; i>=0; i--) {
	    if(flobs[i].d != oldd)
		depthColor(g, oldd = flobs[i].d);
	    if(flobs[i].interpTo == null || !flobs[i].needsBox())
		flobs[i].renderInterp(g, fract);
	    else {
		// XXX the interpolation path isn't necessarily linear!
		box.renderBg(g, flobs[i], fract);
		flobs[i].renderInterp(g, fract);
		box.renderFrame(g, flobs[i], fract);
	    }
	}
    }

    public boolean isInterpUseful(ZZScene other0) {
	FlobSet other = (FlobSet)other0;
	return prepareInterp(other);
    }

    /** Find flobs for this parent and cell.
     * @returns null if no flobs are found (empty array would be inefficient)
     */
    public Flob[] findFlobs(Flob parent, ZZCell c) {
	tag();
	Hashtable hash = getPH(parent);
	Object o = hash.get(c);
	if(o == null) return null;
	else if(o instanceof Flob) return new Flob[] { (Flob)o };
	else if(o instanceof Flob[]) return (Flob[])o;
	
	// Now, we have a Vector. Convert to array.
	Vector v = (Vector)o;
	Flob[] res = new Flob[v.size()];
	for(int i=0; i<res.length; i++) res[i] = (Flob)v.elementAt(i);
	
	// tag() only puts Vectors. Now put the converted array.
	hash.put(c, res);
	return res;
    }

    /** True if from's center is nearer to test's than other's. */
    static boolean isNearer(Flob from, Flob test, Flob other) {
	int fx = from.x + from.w/2, fy = from.y + from.h/2;
	int tx = test.x + test.w/2, ty = test.y + test.h/2;
	int ox = other.x + other.w/2, oy = other.y + other.h/2;
	return ((fx-tx)*(fx-tx) + (fy-ty)*(fy-ty))
	     < ((fx-ox)*(fx-ox) + (fy-oy)*(fy-oy));
    }
	

    /** Find the flob in this flobset which is nearest to a flob in another
     * flobset -- i.e., f is supposed NOT to be in this flobset. Distances
     * are measured center to center.
     * @param f The flob to search from -- should NOT be in this set.
     * @param parent The parent of the flob to find -- must be in THIS set.
     */
    public Flob findNearest(Flob f, Flob parent) {
	Flob[] flobs = findFlobs(parent, f.c);
	if(flobs == null) return null;
	Flob result = null;
	for(int i=0; i<flobs.length; i++) {
	    Flob g = flobs[i];
	    if(result == null || isNearer(f, g, result))
		result = g;
	}
	if(result != null) return result;
	else return null;
    }

    /** For compatibility
     * deprecated Use findFlobs, findNearest or something.
     */
    public Flob findFlob(String path, ZZCell c) {
	Flob[] flobs = findFlobs(null, c);
	if(flobs == null) return null;
	return flobs[0];
    }

    /** Prepare for interpolation
     * with another flobset. Usually
     * used only internally.
     */
    boolean prepareInterp(FlobSet f2) {
	if(interpPrepared == f2) return true;
	// p("Flobset: prepareInterp "+this+" "+f2);
	boolean useful = false;
	for(int i=0; i<f2.nflobs; i++)
	    f2.flobs[i].interpTo = null;
	// First set to null -- avoid animation errors
	for(int i=0; i<nflobs; i++) flobs[i].interpTo = null;
	for(int iter=0; iter<5; iter++) {
	    for(int i=0; i<nflobs; i++) {
		if(flobs[i].c == null) continue;
		Flob parent = flobs[i].getParent();
		Flob f = f2.findNearest(flobs[i], 
				    parent != null ? parent.interpTo : null);
		if(f==null ||
		   (f.interpTo != null && isNearer(f, f.interpTo, flobs[i])) ||
		   (parent != null && parent.interpTo == null)) continue;
		flobs[i].interpTo = f;
		f.interpTo = flobs[i];
		useful = (useful || flobs[i].isInterpUseful());
	    }
	}
	f2.interpPrepared = this;
	interpPrepared = f2;
	return useful;
    }

    /** Prepare for interpolation with another flobset, 
     * but only moving given flobs.
     * Used to effect level-of-detail for SceneFlobs.
     * Note that this also only modifies the current flobset
     * and gives the current flobset the pointers...
     */
    public void prepareInterp(FlobSet f2, Flob[] which) {
	// p("Flobset: prepareInterp LIMITED "+this+" "+f2+" "+which);
	interpPrepared = f2;
	for(int i=0; i<nflobs; i++) flobs[i].interpTo = null;
	for(int i=0; i<which.length; i++) {
	    if(which[i].c == null) continue;
	    // Parents not used here...
	    Flob f = f2.findNearest(which[i], null);
	    if(f==null) continue;
	    which[i].interpTo = f;
	}
    }

    public Object getObjectAt(int x, int y) { 
	return getObjectAt(x, y, null);
    }

    public Object getObjectAt(int x, int y, Flob[] fres) { 
	sortf();
	for(int i=0; i<nflobs; i++) {
	    Object o = flobs[i].hit(x, y);
	    if(o != null) {
		if(fres != null) fres[0] = flobs[i];
		return o;
	    }
	}
	return null;
    }

    public void renderXOR(Graphics g, int x, int y) {
	Flob[] which = new Flob[1];
	Object co = getObjectAt(x, y, which);
	if(co == null) return;
	which[0].renderXOR(g, x, y);
    }

    public void dump() {
	pa("FlobSet dump: "+nflobs+" "+nrend);
	for(int i=0; i<flobs.length; i++) {
	    if(flobs[i] == null) {
		pa(" Flob null!");
		continue;
	    }
	    pa(" Flob "+flobs[i].x+" "+flobs[i].y+" "+
		flobs[i].w+" "+flobs[i].h+" "+flobs[i].d+" "+flobs[i]);
	}
	for(int i=0; i<rends.length; i++) {
	    if(rends[i] == null) {
		pa(" Renderable null!");
		continue;
	    }
	    pa(" Renderable "+ rends[i].d+" "+rends[i]);
	}
    }

    /** A callback interface for iterating over the Flobs
     *  in a FlobSet in depth order.
     */
    public static abstract class DepthIter {
	public void act(Flob[] flobs, int start, int n) {}
	/* @param in The scene flob all these flobs are in. */
	public void act(Flob[] flobs, int start, int n, SceneFlob in) {
	    act(flobs, start, n);
	}
    }

    /** Iterate a routine over blocks of same-depth flobs.
     *  @param flat Whether to include flobs inside SceneFlobs.
     */
    public void iterDepth(DepthIter di, boolean frontFirst, boolean flat) {
	if(nflobs <= 0) return;
	if(flobs == null) throw new Error("NULL FLOBS");
	if(flobs[0] == null) throw new Error("NULL FIRST FLOB");
	sortf();
	if(flobs == null) throw new Error("NULL FLOBS AFTER SORT");
	if(flobs[0] == null) throw new Error("NULL FIRST FLOB AFTER SORT");
	if(frontFirst) {
	    int cur = 0; int curd = flobs[0].d;
	    for(int i=0; i<nflobs; i++) {
		if(flat && flobs[i] instanceof SceneFlob) {
		    di.act(flobs, cur, i-cur, sflob);
		    ((SceneFlob)flobs[i]).getFlobSet().iterDepth(di, true, true);
		    cur = i; curd = flobs[i].d;
		} else if(curd != flobs[i].d ) {
		    di.act(flobs, cur, i-cur, sflob);
		    cur = i;
		    curd = flobs[i].d;
		}
	    }
	    di.act(flobs, cur, nflobs-cur);
	} else {
	    int cur = nflobs-1; int curd = flobs[nflobs-1].d;
	    for(int i=nflobs-1; i>=0; i--) {
		if(flat && flobs[i] instanceof SceneFlob) {
		    di.act(flobs, i+1, cur-i, sflob);
		    ((SceneFlob)flobs[i]).getFlobSet().iterDepth(di, true, true);
		    cur = i; curd = flobs[i].d;
		} if(curd != flobs[i].d) {
		    di.act(flobs, i+1, cur-i, sflob);
		    cur = i;
		    curd = flobs[i].d;
		}
	    }
	    di.act(flobs, 0, cur+1);
	}
    }

    /** Iterate a routine over blocks of same-depth flobs.
     *  Does not include flobs inside SceneFlobs.
     */
    public void iterDepth(DepthIter di, boolean frontFirst) {
	iterDepth(di, frontFirst, false);
    }

    DragCursor dc; int dcx, dcy;
    public void setDragCursor(DragCursor dc, int x, int y) {
	this.dc = dc; this.dcx = x; this.dcy = y;
    }
    public interface DragCursor {
	boolean accept(Object o);
    }

    /** Render the current dragCursor.
     */
    private void renderDC(Graphics g) {
	if(dc == null) return;
	Flob[] which = new Flob[1];
	Object co = getObjectAt(dcx, dcy, which);
	if(co == null) return;
	if(!dc.accept(co)) return;
	which[0].renderXOR(g, dcx, dcy);
    }

    public int fetch(FlobSet from, int x, int y, int d) {
	return fetch(from, x, y, d, null);
    }

    /** Fetch the contents of another FlobSet and translate coordinates.
     * Takes all flobs and rends, adds x, y and do to their coordinates,
     * and puts them in this set. This is used to combine views.
     * @param Whenever a flob's applitude field contains null, it is set
     *        to this value (which may be null in itself).
     * @returns The highest depth used (or d, if there weren't any flobs).
     */
    public int fetch(FlobSet from, int x, int y, int d, ZZCell applitude) {
	int res = d;
	for(int i=0; i<from.nflobs; i++) {
	    Flob f = from.flobs[i];
	    f.x += x; f.y += y; f.d += d;
	    if(f.d > res) res = f.d;
	    if(f.applitude == null) f.applitude = applitude;
	    add(f);
	}
	for(int i=0; i<from.nrend; i++) {
	    Renderable r = from.rends[i];
	    if(!r.translate(x, y)) continue;
	    r.d += d;
	    if(r.d > res) res = r.d;
	    add(r);
	}
	return res;
    }

    /** Get a new FlobSet like this, just with a different size.
     * Used to combine views.
     */
    public FlobSet create(Dimension size) {
	return new FlobSet(size, fg, bg, cpt);
    }

}
