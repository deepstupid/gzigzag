/*   
TextBeamer.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
 * Written by and Tuomas Lukka
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.awt.*;
import java.util.*;

/** A decoration which draws beams between TextSpanVobs.
 */

public class TextBeamer extends Vob {
String rcsid = "$Id: TextBeamer.java,v 1.1 2001/10/21 12:58:12 tjl Exp $";

    class Chain {
	TextSpanVob start1 /*, end1 */;
	int start1Ind, end1Ind;
	Chain next1; // with same start1.

	TextSpanVob start2 /*, end2 */;
	int start2Ind, end2Ind;
	Chain next2;

	/** Distance from beamcenter.
	 */
	int dist;

	Chain root;// choose colors according to root, but
		   // everything else is as before.

	Chain findRoot() {
	    if(root == null) return this;
	    root = root.findRoot();
	    return root;
	}
    }

    Chain[] chains;
    HashMap chainColors = new HashMap();
    HashMap darkerChainColors = new HashMap();

    int howmany = 6;

    Color[] colors = org.gzigzag.util.ColorUtil.fadingColors_solid(howmany);
    Color[] darkercolors = 
	    org.gzigzag.util.ColorUtil.fadingColors_solid_darker(howmany);

    /** The coordinates of the center of the beams - i.e.mouse cursor.
     */
    int cx, cy;

    public void decorate(VobScene scene) {
	ArrayList tchains = new ArrayList();
	HashMap t1 = new HashMap(),
		t2 = new HashMap(); // by start1, end1

	// Build an index.
	// XXX This is why separate index objects are useful:
	// animators now have to duplicate this work.
	SpanSet set = new SimpleSpanSet();
	for(Iterator i = scene.vobs(); i.hasNext();) {
	    Vob vob = (Vob)i.next();
	    if(!(vob instanceof TextSpanVob)) continue;
	    set.addSpan(((SpanVob)vob).getSpan(), vob);
	}
	// Then, build the maximal chains.
	HashMap done = new HashMap();
	for(Iterator i = scene.vobs(); i.hasNext();) {
	    Vob vob = (Vob)i.next();
	    if(!(vob instanceof TextSpanVob)) continue;
	    TextSpanVob t = (TextSpanVob)vob;
	    done.put(t,t);
	    for(Iterator ind=set.overlaps(t.span).iterator(); ind.hasNext();) {
		TextSpanVob ov = (TextSpanVob)ind.next();
		if(ov == t) continue;
		System.out.println("Overlap: "+t+" "+ov);
		if(done.get(ov) != null) continue;
		// found a real overlap.
		// For now, we just put the two as a chain.
		Chain c = new Chain();
		c.start1 = t;
		// c.end1 = t;
		c.start1Ind = 0;
		c.end1Ind = (int)t.span.length();

		c.start2 = ov;
		// c.end2 = ov;
		c.start2Ind = 0;
		c.end2Ind = (int)ov.span.length();
		// Should really do the whole chain...
		tchains.add(c);

		c.next1 = (Chain)t1.get(c.start1);
		t1.put(c.start1, c);
		c.next2 = (Chain)t2.get(c.start2);
		t2.put(c.start2, c);
	    }
	}

	chains = new Chain[tchains.size()];
	chains = (Chain[])tchains.toArray(chains);

	for(int i=0; i<chains.length; i++) {
	    TextSpanVob prev1 = chains[i].start1.prev;
	    if(prev1 == null) continue;
	    TextSpanVob prev2 = chains[i].start2.prev;
	    if(prev2 == null) continue;
	    Chain ch = (Chain)t1.get(chains[i].start1.prev);
	    while(ch != null) {
		if(ch.start2 == prev2) {
		    chains[i].root = (ch.root == null ? ch : ch.root);
		    break;
		}
		ch = ch.next1;
	    }
	    ch = (Chain)t2.get(chains[i].start2.prev);
	    while(ch != null) {
		if(ch.start1 == prev2) {
		    chains[i].root = (ch.root == null ? ch : ch.root);
		    break;
		}
		ch = ch.next2;
	    }
	}
	// Pointer-hop all pointers to chain roots.
	// hope no loops...
	for(int i=0; i<chains.length; i++) {
	    chains[i].findRoot();
	}
    }

    public void putInto(VobPlacer placer) {
	Dimension size = placer.getSize();
	if(size == null) size = new Dimension(1000, 1000);
	placer.put(this, 1000, 0, 0, size.width, size.height);
    }

    static public Color getSpanColor() {
        return new Color(
            (float)(Math.random() + 1) / 2,
            (float)(Math.random() + 1) / 2,
            (float)(Math.random() + 1) / 2
        );
    }

    public void setBeamCenter(int x, int y) {
	cx = x; cy = y;
    }

    public void render(Graphics g, 
		       int xc, int yc, int w, int h,
		       boolean boxDrawn, RenderInfo info) {
	Color backup = g.getColor();
	int[] x = new int[8];
	int[] y = new int[8];
	// System.out.println("Draw bbeams");
	Vob.Coords r0 = new Vob.Coords();
	Vob.Coords r1 = new Vob.Coords();
	sort(info);
	int i = chains.length-1;
	for(; i >= 0; i--) {
	    Chain ch = chains[i];
	    Color color;
	    Chain root = (ch.root == null ? ch : ch.root);
	    if((color = (Color)chainColors.get(root)) == null) continue;
	    info.getInterpCoords(ch.start1, r0);
	    info.getInterpCoords(ch.start2, r1);
	    // System.out.println("Draw: "+ch.dist+" "+r0+" "+r1);

	    Rectangle a=null;

	    // left side
	    if(r0.x < r1.x) { a = r0; } else { a = r1; }
	    x[0] = a.x; y[0] = a.y;
	    x[1] = a.x; y[1] = a.y+a.height;
	    // bottom side
	    if(r0.y+r0.height > r1.y+r1.height) { a = r0; } else { a = r1; }
	    x[2] = a.x; y[2] = a.y+a.height;
	    x[3] = a.x+a.width; y[3] = a.y+a.height;
	    // right side
	    if(r0.x+r0.width > r1.x+r1.width) { a = r0; } else { a = r1; }
	    x[4] = a.x+a.width; y[4] = a.y+a.height;
	    x[5] = a.x+a.width; y[5] = a.y;
	    // top
	    if(r0.y < r1.y) { a = r0; } else { a = r1; }
	    x[6] = a.x+a.width; y[6] = a.y;
	    x[7] = a.x; y[7] = a.y;


//	    g.drawLine(icoords1.x, icoords1.y, icoords2.x, icoords2.y);
	    g.setColor(color);
	    g.fillPolygon(x, y, 8);
	}
	// Draw the actual linked backgroungs in darker
	for(i=chains.length-1; i >= 0; i--) {
	    Chain ch = chains[i];
	    Color color;
	    Chain root = (ch.root == null ? ch : ch.root);
	    if((color = (Color)darkerChainColors.get(root)) == null) continue;
	    info.getInterpCoords(ch.start1, r0);
	    info.getInterpCoords(ch.start2, r1);

	    g.setColor(color);
	    g.fillRect(r0.x, r0.y, r0.width, r0.height);
	    g.fillRect(r1.x, r1.y, r1.width, r1.height);
	}
	g.setColor(backup);
    }

    int calcDist(int cx, int cy, Rectangle r) {
	int rx = r.x + r.width/2;
	int ry = r.y + r.height/2;
	return (cx-rx)*(cx-rx) + (cy-ry)*(cy-ry) * 4;
    }

    /** Sort the chains by minimum distance from beamcenter.
     */
    void sort(RenderInfo info) {
	// System.out.println("Sort "+cx+" "+cy);
	Vob.Coords r = new Vob.Coords();
	for(int i=0; i<chains.length; i++) {
	    Chain ch = chains[i];
	    info.getInterpCoords(ch.start1, r);
	    chains[i].dist = calcDist(cx, cy, r);
	    info.getInterpCoords(ch.start2, r);
	    int d2 = calcDist(cx, cy, r);
	    if(d2 < chains[i].dist)
		chains[i].dist = d2;
	}
	qsort(0,chains.length);
	chainColors.clear();
	darkerChainColors.clear();
	int col = 0;
	for(int i=0; i<chains.length; i++) {
	    Chain root = (chains[i].root == null ? chains[i] : chains[i].root);
	    if(chainColors.get(root) == null) {
		chainColors.put(root, colors[col]);
		darkerChainColors.put(root, darkercolors[col]);
		col++;
		if(col >= colors.length) break;
	    }
	}
    }

    void qsort(int start, int end) {
	// System.out.println("Qsort "+start+" "+end);
	if(start >= end-1) return;
	int median = chains[(start+end-1)/2].dist;
	int i = start;
	int j = end-1;
	OUTER: while(j > i) {
	    while(chains[i].dist < median) {
		i++;
		if(i >= j) break OUTER;
	    }
	    while(chains[j].dist > median) {
		j--;
		if(i >= j) break OUTER;
	    }
	    Chain tmp = chains[i];
	    chains[i] = chains[j];
	    chains[j] = tmp;
	    i++;
	    j--;
	}
	if(i==start || i == end-1) return;
	qsort(start, i);
	qsort(i, end);
    }

    public TextBeamer() { super(null); }

}
	
