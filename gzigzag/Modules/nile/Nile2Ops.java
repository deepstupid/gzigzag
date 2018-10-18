/*   
Nile2Ops.java
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
package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;

/** A library of routines for operating on Nile streams.
 * @see Nile1
 */

public abstract class Nile2Ops {
public static final String rcsid = "$Id: Nile2Ops.java,v 1.9 2001/01/26 13:53:27 ajk Exp $";
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    // static Nile2Unit unit = new Nile2Unit.Sentence();

    static public void saveCursor(ZZCell view) {
	ZZCell s = view.getOrNewCell("d.nile-save", 1);
	if(ZZCursorReal.get(s) == null)
	    ZZCursorReal.set(s, new ZZCursorReal(view));
    }
    static public void clearSavedCursor(ZZCell view) {
	ZZCell s = view.getOrNewCell("d.nile-save", 1);
	if(ZZCursorReal.get(s) != null)
	    ZZCursorReal.set(s, (ZZCell)null);
    }
    static public ZZCursor getSavedCursor(ZZCell view) {
	ZZCell s = view.getOrNewCell("d.nile-save", 1);
	return new ZZCursorReal(s);
    }

    static public void setCursor(Nile2Unit unit, ZZCell view, ZZCursor to) {
	p("Set cursor: "+unit+" "+view+" "+to);
	if(to == null) {
	    // Saves only when there is nothing there before.
	    saveCursor(view);
	    to = getSavedCursor(view);
	} else {
	    clearSavedCursor(view);
	}
	Nile2Iter iter = giter(to);
	unit.start(iter, -1, true);
	curs(view).set(iter.getCursor());
	unit.end(iter, 1, true);
	endcurs(view).set(iter.getCursor());
    }

    static public void moveCursor(Nile2Unit unit, ZZCell view, int dir) {
	clearSavedCursor(view);
	Nile2Iter iter;
	if(dir > 0) {
	    iter = giter(endcurs(view));
	    if(!unit.insertPos(iter, 1, true)) return;
	} else {
	    iter = giter(curs(view));
	    if(!unit.insertPos(iter, -1, false)) return;
	}
	curs(view).set(iter.getCursor());
	unit.end(iter, 1, false);
	endcurs(view).set(iter.getCursor());
    }

    static public void adjust(Nile2Unit unit, ZZCell view, int end, int dir) {
	clearSavedCursor(view);
	Nile2Iter iter = giter(curs(view));
	Nile2Iter enditer = giter(endcurs(view));
	if(end > 0) {
	    if(!unit.end(enditer, dir, false)) return;
	} else {
	    if(!unit.start(iter, dir, false)) return;
	}
	if(!iter.isOrdered(enditer))
	    return;
	curs(view).set(iter.getCursor());
	endcurs(view).set(enditer.getCursor());
    }

    static public void hop(Nile2Unit unit, ZZCell view, int dir) {
	clearSavedCursor(view);
	Nile2Iter iter = giter(curs(view));
	Nile2Iter enditer = giter(endcurs(view));
	// XXX Checkpoint for undo!
	Nile2Iter pt = unit.cut(iter, enditer);

	unit.insertPos(pt, dir, false);

	unit.paste(pt, iter, enditer);

	curs(view).set(iter.getCursor());
	endcurs(view).set(enditer.getCursor());
    }

    /** Tunnel text from one nile window to another. 
     * The text can be copied or cut.
     */
    static public void tunnel(Nile2Unit unit, ZZCell from, ZZCell to, 
			      int dir, boolean copy) {
	clearSavedCursor(from); clearSavedCursor(to);
	Nile2Iter fiter = giter(curs(from));
	Nile2Iter fenditer = giter(endcurs(from));
	Nile2Iter titer = giter(curs(to));
	Nile2Iter pt = null;
	
	// XXX Checkpoint for undo!
	if(copy)
	    unit.copy(fiter, fenditer);
	else
	    pt = unit.cut(fiter, fenditer);
	
	// Insert exactly where we are -- is this o.k.?
	// unit.insertPos(titer, dir, true);
	
	unit.paste(titer, fiter, fenditer);
	
	curs(to).set(fiter.getCursor());
	endcurs(to).set(fenditer.getCursor());
	
	if(!copy) {
	    unit.start(pt, -1, false);
	    curs(from).set(pt.getCursor());
	    unit.end(pt, 1, true);
	    endcurs(from).set(pt.getCursor());
	}
    }

    static public boolean insert(ZZCell view, String key) {
	clearSavedCursor(view);
	if(key == null)
	    return false;
	if(key.length() != 1)
	    return false;
	Nile2Iter it = giter(curs(view));
	it.insert(key);
	ZZCursor c = it.getCursor();
	curs(view).set(c);
	endcurs(view).set(c);
	return true;
    }

    static public void backspace(Nile2Unit unit, ZZCell view) {
	clearSavedCursor(view);
	Nile2Iter iter = giter(curs(view));
	if(!unit.insertPos(iter, -1, false)) return;
	Nile2Iter enditer = (Nile2Iter)iter.clone();
	if(!unit.end(enditer, 1, false)) return;
	Nile2Iter cut = unit.cut(iter, enditer);
	if(cut == null)
	    throw new ZZError("Couldn't cut!");
	setCursor(unit, view, cut.getCursor());
    }

    static public void del(Nile2Unit unit, ZZCell view) {
	// XXX if nothing is selected -- delete unit right from ins cursor
	unit.cut(giter(curs(view)), giter(endcurs(view)));
    }

    static public void breakParagraph(Nile2Unit unit, ZZCell view) {
	clearSavedCursor(view);
	Nile2Iter it = giter(curs(view));
	it.breakParagraph();
	setCursor(unit, view, it.getCursor());
    }

    static public void traverseTranscopies(ZZCell view, int dir) {
	clearSavedCursor(view);
	ZZCell c0 = ZZCursorReal.get(view);
	ZZCell c = c0.s("d.nile-transcluded");
	if(c == null) {
	    ZZLogger.log("Could not traverse transcopies because accused cell "
		       + "IS not transcopied");
	    return;
	}
	ZZCell h0 = c0.h("d.nile-transcluded-split", true);
	if(h0 != null) {    // jump over splits
	    while(h0.equals(c.h("d.nile-transcluded-split"))) {
		if(c.equals(c0))
		    throw new ZZError("XXX! All transcluded cells are "
				    + "splits of the same cell! HELP!");
		c = c.s("d.nile-transcluded");
	    }
	}
	ZZCursorReal.setOffs(view, 0);
	ZZCursorReal.set(view, c);
    }



    /** Get a whole Nile stream as a string, properly capitalized.
     *  Converts paragraph breaks (==struct cells) to double newlines.<br>
     * XXX a) It's no good this calls Nile2View, and it's even worse it does
     *        *line-breaking* to capitalize! I.e. move capitalization elsewhere
     *        (ZZUtil)<br>
     * XXX b) Do this for the text between two iters (a selection), not only
     *        for a whole stream.<br>
     * XXX c) Does this belong in this class? (It seemed to fit?)
     * 
     * @param inStream Some cell inside the stream to be read.
     */
    static public String stringify(ZZCell inStream) {
	ZZCell hd = inStream.h("d.nile");
	Nile1View n1v = new Nile1View();
	n1v.init__zob();
	String s = "";
	ZZCell tail = Nile2Iter.findStruct(hd, 1, false);
	while(hd != null) {
	    FText ft = n1v.paragraph(hd, tail, null, null, null);
	    ft.capitalize();
	    s = s + FText.Part.join(ft.parts);
	    hd = tail;
	    if(hd != null) tail = Nile2Iter.findStruct(hd, 1, false);
	    s = s + "\n\n";
	}
	return s;
    }




    // Shorthand...
    static public ZZCursorReal curs(ZZCell view) {
	return new ZZCursorReal(view);
    }
    static public Nile2Iter giter(ZZCursor c) {
	return new Nile2Iter(c);
    }

    static public ZZCursorReal endcurs(ZZCell view) {
	ZZCell end = view.getOrNewCell("d.nile-sel");
	return new ZZCursorReal(end);
    }

}
