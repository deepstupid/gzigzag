/*   
AwtLinkCache.java
 *    
 *    Copyright (c) 2000-2001 Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2000-2001 Kimmo Wideroos
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
 * Written by Kimmo Wideroos (inspired by Benjamin Fallenstein's notemap module)
 */

/** collection which caches link info
*/

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;

public class AwtLinkCache extends AbstractCollection {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    public class LinkWodgeIterator implements Iterator {
	Enumeration stuff;
	LinkWodgeIterator(Enumeration e) {
	    stuff = e;
	}
	public boolean hasNext() {
	    return stuff.hasMoreElements();
	}
	public Object next() throws NoSuchElementException {
	    return stuff.nextElement();
	}
	public void remove() throws UnsupportedOperationException {
	    throw new UnsupportedOperationException();
	}
    }

    public final class LinkWodge {
	public ZZCell cell;
	public double[] from, to;
	LinkWodge(ZZCell cell, double[] f, double[] t) {
	    this.cell=cell;
	    from = f;
	    to = t;
	}
    }

    private Hashtable cache;

    public AwtLinkCache() { cache = new Hashtable(); }
    public AwtLinkCache(ZZCell layer) { 
	cache = new Hashtable(); 
	ZZCell[] ltrees, as;
	as = AwtLayer.getArtefacts(layer);
	Hashtable treeh = new Hashtable();
	for(int i=0; i<as.length; i++) {
	    ltrees = AwtLinkRelation.getLinktrees(as[i]);
	    for(int j=0; j<ltrees.length; j++) {
		treeh.put(ltrees[j], ltrees[j]);
	    }
	}
	for(Enumeration e=treeh.elements(); e.hasMoreElements(); ) {
	    ZZCell t = (ZZCell)e.nextElement();
	    //pa("tree:"+t);
	    addTree(t);
	}
    }

    public boolean add(ZZCell c, double[] from, double[] to) { 
	LinkWodge lv = 
	    new LinkWodge(c, from, to);
	cache.put(lv, lv);
	return true;
    }

    public double[] addTree(ZZCell tree) {
	double[] mp;
	int i;
	ZZCell[] lcs = tree.readRank("d.2", 1, false, null);
	// end point vectors
	double[][] epvs = new double[lcs.length][];
	double c, d, m, cm, d_2;

	for(i=0; i<lcs.length; i++) {
	    epvs[i] = getEndPoint(lcs[i].h(AwtDim.link));
	} 

	mp = middle(epvs);

	for(i=0; i<epvs.length; i++) {
	    // if link root, continue
	    if(epvs[i].length == 2) continue;
	    for(int j=0; j<2; j++) {
		c = epvs[i][j];
		d = epvs[i][j+2];
		d_2 = d*0.5;
		m = mp[j];
		cm = c-m;
		if(cm < -d_2/2) epvs[i][j] += d_2;
		if(cm > d_2/2) epvs[i][j] -= d_2;
	    }
	} 
	// recount
	mp = middle(epvs);

	add(tree, mp, mp);
	for(i=0; i<epvs.length; i++) {
	    add(lcs[i], mp, epvs[i]);
	}

	return mp;
    } 

    public Iterator iterator() {
	return new LinkWodgeIterator(cache.elements());
    }

    public int size() { return cache.size(); }

    static public double[] middle(double[][] coord) {
	double mx = 0, my = 0;
	for(int i=0; i<coord.length; i++) {
	    mx += coord[i][0];
	    my += coord[i][1];
	}
	mx /= coord.length;
	my /= coord.length;
	return new double[] {mx, my};



    }


    /* return endpoint (inds 0, 1) and optionally, if
     * an artefact, dimension (inds 2, 3).
     */
    private double[] getEndPoint(ZZCell la) {
	//pa("endpoint:"+la);
	if(AwtLinkRelation.isLinkRoot(la)) 
	    return addTree(la);
	else {
	    // 'cd' coord{ind=0,1} & dim{2,3}
	    double[] cd = new double[4];
	    System.arraycopy(AwtArtefact.getCoord(la), 0, cd, 0, 2);
	    System.arraycopy(AwtArtefact.getDimension(la), 0, cd, 2, 2);
	    return cd;
	}
    }

}



