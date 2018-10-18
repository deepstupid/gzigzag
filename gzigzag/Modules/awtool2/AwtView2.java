/*   
AwtView2.java
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
 * Written by Kimmo Wideroos
 */

/** View for a(ssociative) writing tool
*/

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

public class AwtView2 extends Awt {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    AwtNoteFlobFactory noteFlobFact = new AwtNoteFlobFactory();
    AwtLinkFlobFactory linkFlobFact = new AwtLinkFlobFactory();

    // for several views we need this
    static private Hashtable linkcaches = new Hashtable();

    // Used for checking whether links between artefacts
    // is handled already.
    private class Pair {
	Object a; Object b;
	public Pair(Object a, Object b) {
	    this.a = a; 
	    this.b = b;
	}
	public int hashCode() {
	    return a.hashCode() ^ b.hashCode();
	}
	public boolean equals(Object o) {
	    Pair p = (Pair)o;
	    return (a==p.a && b==p.b) || (b==p.a && a==p.b);
	}
    }


    public void raster(FlobSet into, FlobFactory fact,
	    ZZCell view, String[] dims, ZZCell accursed) {

	Awt.focusView = view;
       
        ZZCell awtlocals = accursed;

        //pa("[AwtView2] accursed cell: "+accursed);

	AwtMetrics M = getActiveMetrics(view);

        Dimension d = into.getSize();
        if(!M.RealViewEquals(d)) {
            M.setRealView(d, true);
            M.cellRepr();
        } 

        noteFlobFact.setMetrics(M);
        linkFlobFact.setMetrics(M);
        
        noteFlobFact.setDragging(dragObj);

        boolean nileOn = false;
        ZZCell curb = view.h("d.bind", 1, true);
        if(curb!=null) {
            String mode = ZZCursorReal.get(curb).getText();
            if(mode.indexOf("Nile") >= 0)
                nileOn = true; 
        }

	AwtCursors awtcursors = new AwtCursors(accursed);
        noteFlobFact.setCursors(awtcursors);

        ZZCell input1 = awtcursors.get(AwtCursors.c_input1);
        ZZCell input2 = awtcursors.get(AwtCursors.c_input2);

        ZZCell[] artefs = AwtLayer.getArtefacts(accursed);
        ZZCell aCell;

        double[] vp;
        int depth;

        for(int i=0; i<artefs.length; i++) {
            aCell = artefs[i];
            
            if(aCell.equals(input1)) {
                depth = 1; 
            } else 
            if(aCell.equals(input2)) { 
                depth = 10; 
            } else { depth = 100; }

            vp = AwtArtefact.getCoord(aCell);
            noteFlobFact.centerFlob(into, aCell, aCell, 1, vp, 0, 0, depth);
        }

	noteFlobFact.makeFlob(into, accursed, accursed, 1, 6, 6, 1, 180, 50);

        // render links


	AwtLinkCache linkcache;
	if(view.equals(refreshLinkCache) || 
	   !linkcaches.containsKey(view)) {
	    //pa("update linkcache");
	    linkcache = new AwtLinkCache(accursed);
	    linkcaches.put(view, linkcache);
	    refreshLinkCache = null;
	} else {
	    //pa("read cached linkcache");
	    linkcache = (AwtLinkCache)linkcaches.get(view);
	}

	if(dragObj == null && resizingObj == null && creatingArtef == false) {
	    Iterator li = linkcache.iterator();
	    AwtLinkCache.LinkWodge lw;
	    for(; li.hasNext(); ) {
		lw = (AwtLinkCache.LinkWodge)(li.next());
		linkFlobFact.makeFlob(into, lw.cell, lw.cell, 1, 
				      lw.from[0], lw.from[1], 2000, 
				      lw.to[0], lw.to[1]);
	    }
	}

	// trasclusions
	SimpleBeamer2 sb = new SimpleBeamer2();
	sb.decorate(into, "", null);

        if(M.GridOn) {
            AwtUtil.renderGrid(into, M, -1.0, -1.0, 1.0, 1.0, Color.gray, 10);
        } else {
	    LineDecor.Builder ldb = new LineDecor.Builder(into, Color.gray);
	    ldb.startl(4, 2000);
	    ldb.l(10000, 10000, 10000, 10000);
	    ldb.endl();
	}

        return;
    }
}
