/*   
AwtNileView.java
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
 * Nile1View.java written by Tuomas Lukka, 
 * AwtNileView - localization for Awtool by Kimmo Wideroos 
 */
package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;
import java.io.*;

/** A view for nile streams.
 */

public class AwtNileView extends Nile1View {
public static final String rcsid = "";
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    public AwtNileView() {  
        init__zob();
    }

    public Rectangle placeParagraph(FlobSet into, 
	FText ft, ZZCell para,Rectangle origrect, int depth,
	int direction, int initY, int middleOffs) {
	beginningSentence[0] = true;
	beginningSentence[1] = false;
	// Margins.
	Rectangle rect = pmarg(origrect, para);
	Rectangle ret = ((FTextLayouter)ftextlayouter).place(into, ft, rect,
				    depth,
				    direction, initY, middleOffs, false, 
				    beginningSentence);
	// Now, do the cool thing with the section headings: include
	// previous and next section headings of the same level here.
	if(Nile2Iter.getLevel(para) > 0) {

	ZZCell strcell = para.s("d.nile-struct");
	if(strcell == null) return ret;

	for(int dir = -1; dir <= 1; dir += 2) {
	    ZZCell prevsec = strcell.s("nile1str:breadth",dir);
	    if(prevsec == null) continue;
	    prevsec = prevsec.h("nile1str:depth").s("d.nile-struct", -1);
	    if(prevsec == null) continue;
	    beginningSentence[0] = true;
	    beginningSentence[1] = false;
	    FText nft = paragraph(prevsec, Nile2Iter.findStruct(prevsec, 1, false),
				    null, null, null);
	    ((FTextLayouter)ftextlayouter).place(into, nft, rect, 200,
			    dir, ret.y + (dir < 0 ? 0 : ret.height), 
			    0, false, beginningSentence);
	}
	}
	return ret;
    }


    public void raster(FlobSet into, ZZCell view, ZZCell accursed,
                              Rectangle rect, int depth) {
	int[] offs = new int[1];

	int lmargin = 18;

	LoopDetector ld = new LoopDetector();

	ZZCursor selectEnd = null;
	ZZCell sele = view.s("d.nile-sel");
	if(sele != null) {
	    selectEnd = ZZCursorVirtual.createFromReal(sele);
	}

	ZZCell spans = accursed;
	if(spans == null) return;

	// Do the center paragraph first.
	ZZCell firstpara = Nile2Iter.findStruct(spans, -1, true);
	ZZCell nextpara = Nile2Iter.findStruct(spans, 1, false);

	FText ft = paragraph(firstpara, nextpara, new ZZCursorReal(view), 
				offs, selectEnd);

	Dimension size = new Dimension(rect.width, rect.height);
        //Rectangle rect = new Rectangle(0, 0, size.width, size.height);
	
	Rectangle ctr = placeParagraph(into, ft, firstpara, rect, depth, 
	    0, rect.y+rect.height/2,
	    ZZCursorReal.getVisualTextOffset(view));

	// Go into positive direction.
	Rectangle r = ctr;
	while(nextpara != null) {
	    ld.detect(nextpara);
	    ZZCell next = Nile2Iter.findStruct(nextpara, 1, false);
	    ft = paragraph(nextpara, next, null, null, selectEnd);

	    r = placeParagraph(into, ft, nextpara, rect, depth,
		1, r.y+r.height + paraskip, 0);

	    nextpara = next;

	    if(r.y + r.height > size.height) break;
	}

	// And negative.
	r = ctr;
	while(true) {
	    ZZCell prev = firstpara;
	    firstpara = Nile2Iter.findStruct(firstpara, -1, false);
	    if(firstpara == null) break;
	    ld.detect(firstpara);

	    ft = paragraph(firstpara, prev, null, null, null);

	    beginningSentence[0] = true;
	    r = placeParagraph(into, ft, firstpara, rect, depth,
		-1, r.y - paraskip, 0);

	    if(r.y < 0) break;
	}
	// into.dump();
	Flob cursf = into.findFlob(null, spans);
        // add condition: if ciew is active, then show cursor
	boolean ns = false;
	if(Awt.focusView != null)
	    ns = AwtNile.nileStatus(Awt.focusView);
	if(cursf != null && ns) 
	    ((SplitCellFlob1)cursf).addCurs(into, ZZCursorReal.getOffs(view), 
				    Color.red);
    }
}


// vim: set syntax=java :
