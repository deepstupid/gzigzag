/*   
Nile3Pos.java
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
 * Written by Benjamin Fallenstein
 */

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;

/** A character-level iterator object that knows about
 * paragraphs for a Nile stream.
 * This object explicitly assumes no modifications to the 
 * stream while it is working.
 * The position is between the characters, not on a character.
 * Actually, it is <em>between (a char | a para boundary) and
 * (a char | a para boundary)</em>, i.e., it can be at the beginning or end
 * of a para or between two paras.
 */

public final class Nile3Pos implements Cloneable {
public static final String rcsid = "$Id: Nile3Pos.java,v 1.1 2001/01/07 18:59:06 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    /** Returned by type() or read(), this denotes end/beginning of stream. */
    public static final int END = -1;
    /** Returned by type() or read(), this denotes a paragraph break. */
    public static final int PARA = -2;
    /** Returned by type(), this means punctuation. */
    public static final int PUNCT = -3;
    /** Returned by type(), this means alphanumeric character. */
    public static final int ALPHA = -4;
    /** Returned by type(), this means whitespace. */
    public static final int WS = -5;
    /** Returned by read(), this means special thing (to type, it's PUNCT). */
    public static final int SPECIAL = -6;

    ZZCell c;
    int offs;
    
    // Cache these for speed.
    // Note: ctxt and cspan are only valid if NOT (isStruct || isSpecial)!
    int clen;
    String ctxt;
    Span cspan;

    // Is this cell a struct cell, a text object?
    private boolean isStruct, isSpecial;

    public Nile3Pos(Nile3Pos pos) { set(pos); }
    public Nile3Pos(ZZCursor pos) { set(pos); }
    Nile3Pos() {}

    public ZZCursor get() { return new ZZCursorVirtual(c, offs); };


    private void load(ZZCell c) {
	this.c = c;
	isStruct = (c.s("d.nile-struct", 1) != null);
	isSpecial = (c.s("d.nile-special", 1) != null);
	
	if(isStruct || isSpecial)
	    clen = 1;
	else {
	    ctxt = c.getText();
	    cspan = c.getSpan();
	    clen = ctxt.length();
	}
    }

    public void set(Nile3Pos pos) {
	load(pos.c);
	offs = pos.offs;
    }
    public void set(ZZCursor pos) {
	load(pos.get());
	offs = pos.getOffs();
	if(offs == pos.NO_OFFSET) offs = 0;
    }

    /** Internal convenience: try to insert with bias, if can't, return */
    private boolean insert(String s, int side, int bias) {
	if(!bias(bias)) return false;
	if(cspan != null) return false;
	
	c.setText(ctxt.substring(0, offs) + s + ctxt.substring(offs));
	ZZCursorReal.adjustCursors(c, offs, s.length());
	if(side > 0) offs += s.length();
	return true;
    }

    public void insert(String s, int side) {
	if(insert(s, side, 1)) return;
	if(offs == 0)
	    if(insert(s, side, -1)) return;
	split(false, 1);
	load(c.N("d.nile", -1));
	c.setText(s);
	offs = (side < 0) ? (0) : (clen);
    }
    
    public void insert(Nile3Part what, int side) {
	if(!what.isStream())
	    throw new ZZError("Can't insert non-stream Nile3Part!");
	split(false, side);
	ZZCell other = c.s("d.nile", -side);
	c.disconnect("d.nile", -side);
	c.connect("d.nile", -side, what.side(side).c);
	other.connect("d.nile", side, what.side(-side).c);
    }

    public boolean delete(int bias) {
	// First, ensure we are in the same cell as the character to delete.
	if(!bias(bias)) return false;
	
	// Then, a special case check (also for isStruct and isSpecial cells)
	if(clen == 1) {
	    ZZCell from = c;
	    if(c.s("d.nile") != null) {
		load(c.s("d.nile"));
		offs = 0;
	    } else if(c.s("d.nile", -1) != null) {
		load(c.s("d.nile", -1));
		offs = clen;
	    } else {
		load(c.N("d.nile"));
		offs = 0;
	    }
	    Enumeration e = ZZCursorReal.getPointers(from);
	    while(e.hasMoreElements()) {
		ZZCell curs = (ZZCell)e.nextElement();
		ZZCursorReal.setcursor(curs, c);
		if(ZZCursorReal.getOffs(curs) != ZZCursorReal.NO_OFFSET)
		    ZZCursorReal.setOffs(curs, offs);
	    }
	    from.delete();
	    return true;
	}
	
	if(cspan == null) {
	    if(bias < 0) offs--;
	    c.setText(ctxt.substring(0, offs) + ctxt.substring(offs));
	    ZZCursorReal.adjustCursors(c, offs, -1);
	} else {
	    split(false, bias);
	    if(bias > 0) c.setSpan(cspan.subSpan(1));
	    else c.setSpan(cspan.subSpan(0, clen-1));
	}
	load(c);
	return true;
    }

    /** Split at the current position.
     * The original cell will remain the first.
     * If side is greater than zero, this pos will be set to the second cell;
     * if side is less than zero, this pos will be set to the first cell.
     * If offs is zero or equal to the number of characters in c, nothing is
     * done and either the current cell or the next cell is returned.
     * <p>
     * The most special case involves doing this at the very end of the stream,
     * in which case an empty new cell is created and returned.
     * @param side The side of the split to make this Nile3Pos point to.
     * @param always Whether to split even if the offset is at the beginning
     * 			or end of a cell. Used e.g. for inserting a paragraph.
     */
    public void split(boolean always, int side) {
	if(always) {
	    ZZCell c1 = c.N("d.nile");
	    if(cspan == null) {
		c.setText(ctxt.substring(0, offs));
		c1.setText(ctxt.substring(offs));
	    } else {
		c.setSpan(cspan.subSpan(0, offs));
		c1.setSpan(cspan.subSpan(offs));
	    }
	    Enumeration e = ZZCursorReal.getPointers(c);
	    while(e.hasMoreElements()) {
		ZZCell p = (ZZCell)e.nextElement();
		int poffs = ZZCursorReal.getOffs(p);
		if(poffs != ZZCursorReal.NO_OFFSET && poffs > offs) {
		    ZZCursorReal.setcursor(p, c1);
		    ZZCursorReal.setOffs(p, poffs-offs);
		}
	    }
	    if(side > 0) { load(c1); offs = 0; }
	} else {
	    if(offs == 0) {
		if(side < 0) {
		    load(c.getOrNewCell("d.nile", -1));
		    offs = clen;
		}
	    } else if(offs >= clen) {
		if(side > 0) {
		    load(c.getOrNewCell("d.nile"));
		    offs = 0;
		}
	    } else
		split(true, side);
	}
    }

    public int type(int dir) {
	int ch = read(dir);
	if(ch < 0) return ch;
	if(Character.isLetterOrDigit((char)ch)) return ALPHA;
	if(Character.isWhitespace((char)ch)) return WS;
	return PUNCT;
    }
    /** Return true if type() would return <code>Nile3Pos.END</code>. */
    public boolean end(int dir) { 
	return type(dir) == END;
    }
    /** Return true if type() would return <code>Nile3Pos.PARA</code>. */
    public boolean para(int dir) { 
	return type(dir) == PARA;
    }
    /** Return true if type() would return <code>Nile3Pos.PUNCT</code>. */
    public boolean punct(int dir) { 
	return type(dir) == PUNCT;
    }
    /** Return true if type() would return <code>Nile3Pos.ALPHA</code>. */
    public boolean alpha(int dir) { 
	return type(dir) == ALPHA;
    }
    /** Return true if type() would return <code>Nile3Pos.WS</code>. */
    public boolean ws(int dir) { 
	return type(dir) == WS;
    }
    /** Return true if boundary, i.e. if end() or para() would return true. */
    public boolean boundary(int dir) {
	int t = type(dir);
	return (t == END) || (t == PARA);
    }
    /** Return true if boundary() or ws() would test true. */
    public boolean bws(int dir) {
	int t = type(dir);
	return (t == WS) || (t == END) || (t == PARA);
    }

    /** Read character in given direction. */
    public int read(int dir) {
	if(!bias(dir)) return END;
	if(isStruct) return PARA;
	if(isSpecial) return SPECIAL;
	return ctxt.charAt((dir < 0) ? (offs - 1) : (offs));
    }

    /** Move n characters forwards or backwards.
     * If the offset given is out of bounds, moves as far as possible
     * and returns false, otherwise returns true. In general, this moves as
     * little as possible, i.e. if n was positive, bias is negative, and if
     * n was negative, bias is positive after call.
     */
    public boolean move(int n) {
	n += offs;
	offs = 0;

	while(n > clen) {
	    n -= clen;
	    ZZCell nc = c.s("d.nile");
	    if(nc == null) {
		// End of stream!
		offs = clen;
		if(n > 0) return false;
		return true;
	    }
	    load(nc);
	}
	while(n < 0) {
	    ZZCell nc = c.s("d.nile", -1);
	    if(nc == null) {
		offs = 0;
		return false;
	    }
	    load(nc);
	    n += clen;
	}

	offs = n;
	return true;
    }
    /** (Convenience:) Move if doMove is true. 
     * This is used by Nile3Unit to make includeThis code simpler.
     */
    public boolean move(boolean doMove, int n) {
	if(doMove) return move(n);
	else return true;
    }

    /** Ensure bias in a given direction.
     * This means that after bias(1), we are in the cell that contains the
     * character in positive direction, and after bias(-1), same in negative
     * direction.<br>
     * If we bump on the end of the stream, return false, else return true.
     */
    boolean bias(int dir) {
	if(offs + dir >= 0 && offs + dir <= clen) return true;
	do {
	    ZZCell nc = c.s("d.nile", dir);
	    if(nc == null) return false;
	    load(nc);
	} while(clen == 0);
	return true;
    }

    public void breakParagraph() {
	split(false, 1);
	c.N("d.nile", -1).N("d.nile-struct").setText("P");
    }

    public Object clone() { 
	try {
	    return super.clone(); 
	} catch(CloneNotSupportedException e) {
	    ZZLogger.exc(e);
	    return null;
	}
    }
}