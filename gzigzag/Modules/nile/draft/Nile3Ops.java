/*   
Nile3Ops.java
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

public abstract class Nile3Ops {
public static final String rcsid = "$Id: Nile3Ops.java,v 1.1 2001/01/07 18:59:06 bfallenstein Exp $";
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    static public void saveCursor(ZZCell win) {
	ZZCell s = win.getOrNewCell("d.nile-save", 1);
	if(ZZCursorReal.get(s) == null)
	    ZZCursorReal.set(s, new ZZCursorReal(win));
    }
    static public void clearSavedCursor(ZZCell win) {
	ZZCell s = win.getOrNewCell("d.nile-save", 1);
	if(ZZCursorReal.get(s) != null)
	    ZZCursorReal.set(s, (ZZCell)null);
    }
    static public ZZCursor getSavedCursor(ZZCell win) {
	ZZCell s = win.getOrNewCell("d.nile-save", 1);
	return new ZZCursorReal(s);
    }

    static public void setCursor(Nile3Unit unit, ZZCell win, ZZCursor to) {
	Nile3Pos pos = new Nile3Pos(to);
	setPart(win, unit.extend(pos));
    }

    static public void moveCursor(Nile3Unit unit, ZZCell win, int dir) {
	Nile3Part part = getPart(win);
	if(!unit.step(part, dir)) return;
	setPart(win, part);
    }

    static public void adjust(Nile3Unit unit, ZZCell win, int end, int dir) {
	Nile3Part part = getPart(win);
	if(!unit.adjust(part, end, dir)) return;
	setPart(win, part);
    }

    static public void hop(Nile3Unit unit, ZZCell win, int dir) {
	Nile3Part part = getPart(win);
	Nile3Pos pos = part.side(dir);
	if(!unit.insertPos(pos, dir, part, false)) return;
	unit.move(part, pos, false);
	setPart(win, part);
    }

    /** Tunnel text from one nile window to another. 
     * The text can be copied or cut.
     */
    static public void tunnel(Nile3Unit unit, ZZCell from, ZZCell to, 
			      boolean copy) {
	Nile3Part part = getPart(from);
	Nile3Pos pos = getPart(to).start;
	if(!unit.insertPos(pos, -1, part, true)) return;
	unit.move(part, pos, copy);
	setPart(to, part);
    }

    static public boolean insert(ZZCell win, String key) {
	if(key == null)
	    return false;
	if(key.length() != 1)
	    return false;
	Nile3Pos pos = getPart(win).start;
	pos.insert(key, 1);
	setPos(win, pos);
	return true;
    }

    static public void backspace(Nile3Unit unit, ZZCell win) {
	Nile3Part orig = getPart(win);
	Nile3Part part = new Nile3Part(orig.start, orig.start);
	unit.edge(part.start, -1, -1, false);
	unit.edge(part.end, 1, -1, true);
	unit.cut(part);
    }

    static public void del(Nile3Unit unit, ZZCell win) {
	Nile3Pos pos = unit.cut(getPart(win));
	setPos(win, pos);
    }

    static public void breakParagraph(Nile3Unit unit, ZZCell win) {
	Nile3Part part = getPart(win);
	part.start.breakParagraph();
    }


    /** Get a whole Nile stream as a string, properly capitalized.
     *  Converts paragraph breaks (==struct cells) to double newlines.<br>
     * XXX a) It's no good this calls Nile1View.<br>
     * XXX b) Do this for a Nile3Part (a selection), not only
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
    static public Nile3Part getPart(ZZCell win) {
	ZZCursorReal start = new ZZCursorReal(win);
	if(start.getOffs() == start.NO_OFFSET)
	    start = new ZZCursorReal(win.getOrNewCell("d.nile-save"));
	ZZCursorReal end = new ZZCursorReal(win.getOrNewCell("d.nile-sel"));
	return new Nile3Part(start, end);
    }

    static public void setPart(ZZCell win, Nile3Part part) {
	ZZCursor start = part.start.get(), end = part.end.get();
	ZZCursorReal.set(win, start.get());
	// if(ZZCursorReal.getOffs(win) != ZZCursorReal.NO_OFFSET)
	    ZZCursorReal.setOffs(win, start.getOffs());
	ZZCursorReal.set(win.getOrNewCell("d.nile-save"), start);
	ZZCursorReal.set(win.getOrNewCell("d.nile-sel"), end);
    }

    static public void setPos(ZZCell win, Nile3Pos pos) {
	setPart(win, new Nile3Part(pos, pos));
    }
}
