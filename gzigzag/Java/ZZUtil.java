/*   
ZZUtil.java
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
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

/** Some general routines that don't fit anywhere else.
 */

public class ZZUtil {
public static final String rcsid = "$Id: ZZUtil.java,v 1.37 2001/06/09 09:24:28 wikikr Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    /** Set a number of strings onto a rank.
     * Overwrites existing cells and creates new for the rest.
     */
    static public void putStrings(ZZCell start, String dim, boolean incStart,
			String[] strs) {
	for(int i=0; i<strs.length; i++) {
	    if(i!=0 || !incStart) start = start.N(dim, 1);
	    start.setText(strs[i]);
	}
    }

    /** Set a number of integers onto a rank.
     * Overwrites existing cells and creates new for the rest.
     */
    static public void putInts(ZZCell start, String dim, boolean incStart,
			int[] ns) {
	for(int i=0; i<ns.length; i++) {
	    if(i!=0 || !incStart) start = start.getOrNewCell(dim, 1);
	    start.setText(""+ns[i]);
	}
    }


    /** Get a number of strings from a rank.
     */
    static public String[] getStrings(ZZCell start, String dim, 
				      boolean incStart,
				      int nmin, int nmax, ZZObs obs) {
	String[] r = new String[nmax];
	int i=0;
	for(; i<nmax; i++) {
	    if(i!=0 || !incStart) {
		start = start.s(dim, 1, obs);
		if(start == null) {
		    if(i >= nmin) break;
		    throw new ZZError("Not enough strings: "+i);
		}
	    }
	    p("GetStrs: "+start.getID());
	    r[i] = start.getText(obs);
	}
	if(i == nmax) return r;
	String[] r2 = new String[i];
	System.arraycopy(r, 0, r2, 0, i);
	return r2;
    }
    static public String[] getStrings(ZZCell start, String dim, 
				      boolean incStart,
				      int nmin, int nmax) {
	return getStrings(start,dim,incStart,nmin,nmax,null);
    }

    static public int[] getInts(ZZCell start, String dim, boolean incStart,
		    int nmin, int nmax,ZZObs obs) {
	String[] s = getStrings(start, dim, incStart, nmin, nmax, obs);
	int[] r = new int[s.length];
	for(int i=0; i<r.length; i++) {
	    p("GetI: "+s[i]);
	    r[i] = Integer.parseInt(s[i]);
	}
	return r;
    }
    static public int[] getInts(ZZCell start, String dim, boolean incStart,
		    int nmin, int nmax) {
	return getInts(start,dim,incStart,nmin,nmax,null);
    }

    /** Mix two colors at a given fraction.
     * 0 = c1, 256 = c2.
     */
    static public Color mix(Color c1, Color c2, int fract) {
	if(fract < 0) fract = 0;
	if(fract > 256) fract = 256;
	int p = 256-fract;
	int m = fract;
	return new Color(
	    (c1.getRed()*p + c2.getRed()*m) / 256,
	    (c1.getGreen()*p + c2.getGreen()*m) / 256,
	    (c1.getBlue()*p + c2.getBlue()*m) / 256);
    }

    /** Give a subtly randomly shaded version of the given color.
     */
    static public Color perturb(Color c1) {
	return new Color(
	    pff(c1.getRed()),
	    pff(c1.getGreen()),
	    pff(c1.getBlue())
	);
    }

    /** Perturb an integer in the range 0..255.
     */
    static int pff(int f) {
	int offs = (int)(50 * (Math.random() - 0.5));
	f += offs;
	if(f < 0) f = 0;
	if(f > 255) f = 255;
	return f;
    }

    /** Find the index of the given string that is closest to 
     * the given X coordinate using FontMetrics.
     */
    static public int findStringHit(String txt, int x, FontMetrics fm,
				    boolean alwaysmin) {
	if(x <= 0) return 0;
	int min = 0; int max = txt.length();
	int lmin = 0; int lmax = fm.stringWidth(txt);
	while(max-min > 1) {
	    int cur = (max+min) / 2;
	    int curlen = fm.stringWidth(txt.substring(0, cur));
	    p("Iter: "+cur+" "+curlen+" "+max+" "+lmax+" "+min+" "+lmin);
	    if(curlen > x) {
		max = cur;
		lmax = curlen;
	    } else {
		min = cur;
		lmin = curlen;
	    }
	    // XXX potential for infinite loop for weird fontmetrics?
	}
	// now we should have it...
	int mx = lmax - x;
	int mn = x - lmin;
	if(mn < mx || alwaysmin) 
	    return min;
	else
	    return max;
    }

    static public int findStringHit(String txt, int x, FontMetrics fm) {
	return findStringHit(txt, x, fm, false);
    }

    /** Append a command-type thing. Strings are put in cells, cells are 
     * cloned.
     */
    static public ZZCell appendCommand(ZZCell cur, 
	    String cdim, String pdim, Object[] objs) {
	ZZCell tmp = null;
	if(objs[0] instanceof String) 
	    (cur = cur.N(cdim, 1)).setText((String)objs[0]);
	else {
	    tmp = ((ZZCell)objs[0]).N("d.clone", 1);
	    cur.connect(cdim, 1, tmp);
	    cur = tmp;
	}
	ZZCell prev = cur;
	for(int i=1; i<objs.length; i++) {
	    if(objs[i] instanceof String)
		(prev = prev.N(pdim, 1)).setText((String)objs[i]);
	    else {
		tmp = ((ZZCell)objs[i]).N("d.clone", 1);
		prev.connect(pdim, 1, tmp);
		prev = tmp;
	    }
	}
	return cur;
    }
    static public ZZCell appendCommand(ZZCell cur, Object[] objs) {
	return appendCommand(cur, "d.2", "d.1", objs);
    }

    static private Clipboard getPUIClipboard() {
	return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    static public void puiCopy(String str) {
	p("PuiCOPY '"+str+"'");
	Clipboard clipboard = getPUIClipboard();
	StringSelection contents = new StringSelection(str);
	clipboard.setContents(contents, new ClipboardOwner() {
	    public void lostOwnership(Clipboard cb, Transferable t) {}
	});
    }

    /** Copy to PUI clipboard.
     * Copy a cell's contents to the clipboard of the surrounding PUI.
     * There is no corresponding cut function because only the text is
     * copied, not the connections, so cut wouldn't delete the cell, but
     * only empty its contents; I see no reason for this operation.
     * <p>
     * If a dimension is given, then contents of this rank are concatenated.
     */
    static public void puiCopy(ZZCell from, String dim)
    {
	StringBuffer cont = new StringBuffer(from.getText());
	if(dim != null) {
	    ZZCell cur = from.s(dim, 1);
	    while(cur != null) { // XXX Use LoopDetector
		String s = cur.getText();
		if(s.equals(""))
		    cont.append('\n');
		else
		    cont.append(' ').append(s);
		cur = cur.s(dim, 1);
	    }
	}
	puiCopy(cont.toString());
    }

    static public void puiCopy(ZZCell from) {
	puiCopy(from.getText());
    }

    static public String puiGetClipText()
    {
	Clipboard clipboard = getPUIClipboard();
	Transferable content = clipboard.getContents(new Object());
	String s = "";
	if(content != null) {
	    try {
		s = (String) content.getTransferData(DataFlavor.stringFlavor);
	    } catch (Exception e) {
		ZZLogger.exc(e);
		return null;
	    }
	}
	return s;
    }

    /** Paste from PUI clipboard.
     * Change a cell's contents to the (text) contents in the clipboard 
     * of the surrounding PUI. Return true if action succeeded; return
     * false if not (because the clipboard didn't contain text).
     * <p>
     * XXX does nothing if cell is span (but what *should* be done?)
     */
    static public boolean puiPaste(ZZCell to) {
	if(to.getSpan() != null) return false;
	p("PuiPASTE");
	String s = puiGetClipText();
	if(s == null) return false;
	to.setText(s);
	return true;
    }

    /** An interface which establishes an ordering (partial or full, 
     * depending on context) between cells.
     */
    public interface Comparator {
	/** Compare the things represented by the two cells to each other.
	 * @return 0 if equal, 1 if c1 greater, -1 if c2 greater.
	 */
	int compare(ZZCell c1, ZZCell c2);
    }
    
    /* after sorting */
    static private void sameToClone(ZZCell c1, String dim, int dir, ZZCell c2, 
			       Comparator comp){
	//pa("CLONING\n");
	ZZCell cur = c1;
	for ( cur = c1.s(dim,dir); cur != null;){
	    if ( c1 == c2 ) break;
	    ZZCell next = cur.s(dim, dir);
	    
	    if ( comp.compare(c1,cur) == 0 ){
		c1.insert( "d.clone", 1, cur ); 
		cur.excise(dim);
		//pa("CLONED\n");
	    } else {
		c1 = cur;
	    }
	    cur = next;
	    
	}
    }
    
    static private int partition(ZZCell[] v, int l, int r, Comparator s){
        int i = l-1;
        int j = r;
        int len = v.length;
        ZZCell p = v[r];
        //if ( p  == null ) return 0;
        ZZCell tmp;
        while( true ){
            while(s.compare(v[++i], p) < 0 ){
                if ( i >= len ) break;
            }
            while(s.compare(p, v[--j]) < 0 ){
                if ( j <=0 ) break;
            }
            if ( i >= j ) break;

            tmp = v[i];
            v[i] = v[j];
            v[j] = tmp;
        }
	
        tmp = v[i];
        v[i] = v[r];
        v[r] = tmp;
        
        return i;
    } 
    
    
    static private void qs(ZZCell[] table, int l, int r,Comparator comp){
	if ( l < r ) {
	    int i = partition(table,l,r,comp);
	    if ( i == r ) { i--; }
	    qs(table,l,i,comp);
	    qs(table,i+1,r,comp);
	}	
    }
    
    // yet another sorting algorithm
    // this isn't too good, feel free to make a better one
    static private void qsRange(ZZCell c1, String dim, int dir, ZZCell c2,
				Comparator comp)  {
	if(c1 == c2) return;
	
	if ( c1 == null || c2 == null ) return;
	// calculate the size of the table
	
	ZZCell first = c1;
	int len = 1, i = 0;
	for( ; c1 != c2; len++){
	    c1 = c1.s(dim,dir);
	    if ( c1 == null ) return;
	}
	if ( len < 1 ) return;
	
	// read to table
	ZZCell[] table = new ZZCell[len];
	c1 = first;
	for( i = 0; i < len; i++){
	    table[i] = c1;
	    c1 = c1.s(dim,dir);
	}
	
	qs( table,0,len-1, comp );
	
	c1 = first;
	
	if( c1 != table[0])
	    c1.insert(dim, -dir, table[0]);

	for(i = 1 ; i < len; i++){
	    c1.insert(dim, dir, table[i]);
	    c1 = table[i];
	}
    }

    static private void sortRange(ZZCell c1, String dim, int dir, ZZCell c2,
				  Comparator comp)  {
	
	//	p("SortRange: "+c1.getText()+" "+c2.getText());
	if(c1 == c2) return;
	
	ZZCell first = c1;
	ZZCell last = c2;
	ZZCell cur;
	for(cur = c1.s(dim, dir); cur != null;) {
	    // p("cur: "+cur.getText());
	    ZZCell next = cur.s(dim, dir);
	    int cp = comp.compare(c1, cur);
	    if(cp > 0) {
		// p("move");
		if(cur == c2) last = c2.s(dim, -dir);
		c1.insert(dim, -dir, cur);
		if(first == c1) first = cur;
	    }
	    
	    if(cur == c2) break;
	    cur = next;
	}
	if(first != c1) 
	    sortRange(first, dim, dir, c1.s(dim, -dir), comp);
	if(c1 != last)
	    sortRange(c1.s(dim, dir), dim, dir, last, comp);
	
    }

    /** Sort a rank with a given comparator.
     */
    static public void sortRank(ZZCell c, String dim, int dir,
				Comparator comp, boolean includeThis) {
	sortRank( c, dim, dir, comp, includeThis, false);
    }
    
    static public void sortRank(ZZCell c, String dim, int dir,
				Comparator comp, 
				boolean includeThis, boolean clone) {
	ZZCell c1;
  	if(!includeThis)
	    c1 = c.s(dim, dir);
	else c1 = c;
	if(c==null) return;
	
	//long t0 = System.currentTimeMillis();
	//sortRange(c1, dim, dir,  c.h(dim, dir), comp);
  	qsRange(c1, dim, dir,  c.h(dim, dir), comp);
	//pa("TIME USED SORTING = " + (System.currentTimeMillis() - t0) );
	
	if(!includeThis)
	    c1 = c.s(dim, dir);
	else c1 = c;
	
	if ( clone )
	    sameToClone(c1, dim, dir, c.h(dim, dir), comp);
    }
    

    /** Show the dimension cross of a flob view. 
     */
    static public void showFlobDims(FlobSet into,
			FlobFactory fact, ZZCell view, int n) {

	Dimension s = fact.getSize(null, 1);
	ZZCell dim = view.s("d.dims", 1);
	ZZCell[] cdims = new ZZCell[3];
	int i=0;
	for(; dim != null && i < n; i ++) {
	    cdims[i] = ZZCursorReal.get(dim);
	    if(cdims[i] == null)
		throw new ZZError("No dimension");
	    dim = dim.s("d.dims", 1);
	}
	Flob fl = fact.makeFlob(into, cdims[0], cdims[0], 1, 
		s.width, 0,          1, s.width, s.height);
	fl.flobPath = "dim";

	if(n <= 1) return;

	fl = fact.makeFlob(into, cdims[1], cdims[1], 1, 
		    0, s.height, 1, s.width, s.height);
	if(n > 2) {
	    fl = fact.makeFlob(into, cdims[2], cdims[2], 1, 
		    s.width, s.height,   1, s.width, s.height);
	    fl.flobPath = "dim";
	}
	int wh = s.width / 2;
	int hh = s.height / 2;

	into.add(new LineDecor(
            new int[] {
	        wh, hh, wh, s.height,
	        wh, hh, s.width, s.height,
	        wh, hh, s.width, hh
	    },
            12,
	    Color.red,
            1
        ));
    }


    /** Create a natural-looking spline between two vectors.
     * Returns an array of alternating x and y coordinates of
     * a polyline.
     * XXX Contains many constants that are just fixed.
     * XXX Needs to be prettified a bit.
     */
    static public int[] bulgeCurve(
	    int x0, int y0, int dx0, int dy0,
	    int x1, int y1, int dx1, int dy1, int NPTS) {
	// Formula: x = t*(x0 + c*t*v_0) + (1-t) * (x1 + c*(1-t)*v_1)
	p("bulge: "+x0+" "+y0+" "+dx0+" "+dy0+" "+x1+" "+y1+" "+
	    dx1+" "+dy1+" ");
	int dx = (dx0 - dx1) / 2;
	int dy = (dy0 - dy1) / 2;
	int dotp = dx * (x1-x0) + dy * (y1-y0);
	int det = dx * (y1-y0) - dy * (x1-x0);
	// if(dotp < 0 && 
	int[] res = new int[NPTS*2];
	// Polynomial for factor.
	// a*x + b*x^3 is the polynomial through (-1,-1), (0,0) and (1,1)
	// whose derivative at -1 and 1 is zero.
	// Thus, it is suitable for curving.
	float a = (float)1.5;
	float b = (float)-0.5;
	float tense = (float)20.0/NPTS;
	for(int i=0; i<NPTS; i++) {
	    float d = (i/(float)(NPTS-1));
	    float dp = d*2 - 1;
	    float fact = (float)(0.5 + 0.5 * (a*(dp) + b*(dp*dp*dp)));
	    p(" "+dp+" "+fact);
	    res[2*i] = (int)(
		((1-fact) * (x0 + dx * tense * d)) +
		(fact * (x1 - dx * tense * (1-d)))
	    );
	    res[2*i+1] = (int)(
		((1-fact) * (y0 + dy * tense * d)) +
		(fact * (y1 - dy * tense * (1-d)))
	    );
	}
	return res;
    }


    /** Move a point in a direction and return new point. */
    static public Point movePoint(Point p, int xa, int ya, int dist) {
	// XXX use real distance
	Point q = new Point(p.x, p.y);
	q.x += xa * dist;
	q.y += ya * dist;
	return q;
    }


    static public void dumpSubSpace(ZZCell c, String[] dims) {
	// XXX VERY naive.
	Hashtable done = new Hashtable();
	Stack stk = new Stack();
	stk.push(c);
	while(!stk.empty()) {
	    ZZCell cur = (ZZCell)stk.pop();
	    if(cur == null) continue;
	    if(done.get(cur) != null) continue;
	    done.put(cur, cur);
	    System.out.println("C: "+cur);
	    for(int i=0; i<dims.length; i++) {
		System.out.println("   + "+dims[i]+"\t"+cur.s(dims[i]));
		System.out.println("   - "+dims[i]+"\t"+cur.s(dims[i], -1));
		stk.push(cur.s(dims[i]));
		stk.push(cur.s(dims[i], -1));
	    }
	}
    }

    /** Get the distance of two cells along a dimension.
     *  Get the number of steps one must walk to get from <code>from</code>
     *  to <code>to</code>. It is an error if the two cells aren't on one
     *  rank on the given dimension, or if the rank loops.
     */
    public static int getDistance(ZZCell from, ZZCell to, String dim) {
	// Ensure both cells are on the same rank.
	ZZCell fhead = from.h(dim), thead = to.h(dim);
	if(fhead.s(dim, -1) != null)
	    throw new ZZError("getDistance on looping rank: "+from+", "+to);
	if(!fhead.equals(thead))
	    throw new ZZError("getDistance on cells which aren't on the "+
			      "same rank: "+from+", "+to);

	int n = 0;
	for(ZZCell d = from; !to.equals(d); d = d.s(dim)) {
	    if(d == null) { n = -1; break; }
	    n++;
	}
	
	if(n < 0) {
	    n = 0;
	    for(ZZCell d = from; !to.equals(d); d = d.s(dim, -1)) {
		if(d == null) throw new ZZError("ARGH! getDistance bug!");
		n--;
	    }
	}
	
	return n;
    }
}





