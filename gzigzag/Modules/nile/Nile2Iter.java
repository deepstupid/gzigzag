/*   
Nile2Iter.java
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

/** A character-level iterator object that knows about
 * paragraphs for a Nile stream.
 * This object explicitly assumes no modifications to the 
 * stream while it is working.
 */

public final class Nile2Iter implements Cloneable {
public static final String rcsid = "$Id: Nile2Iter.java,v 1.8 2000/12/20 22:26:35 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    private ZZCell c;
    private int offs;

    // Cache these for speed.
    private String ctext;
    private int clen;

    private void load(ZZCell c) { 
	this.c = c; 
	ctext = c.getText(); 
	clen = ctext.length();
    }

    private void load(ZZCell c, int offs) { 
	load(c);
	if(offs == -1) this.offs = clen;
	else this.offs = offs;
	move(0);
    }

    private void reload() {
	load(c);
	move(0);
    }

    static private boolean isStruct(ZZCell c) {
	if(c == null)
	    throw new ZZError("Null isStruct?");
	return c.s("d.nile-struct") != null;
    }

    Nile2Iter() { }
    public Nile2Iter(ZZCursor curs) { set(curs); }

    public void set(ZZCursor curs) {
	load(curs.get()); 
	this.offs = curs.getOffs();
	// Ensure positive bias.
	move(0);
    }

    public void set(Nile2Iter iter) {
	load(iter.c);
	offs = iter.offs;
    }

    /** Move n characters forwards or backwards within the current
     * paragraph.
     * If the offset given is out of bounds, moves as far as possible
     * and returns false, otherwise returns true.
     */
    public boolean move(int n) {
	n += offs;
	offs = 0;

	// Internally, we like to maintain positive bias always.
	// Thus, the greater-OR-EQUALS sign.
	while(n >= clen) {
	    n -= clen;
	    ZZCell nc = c.s("d.nile");
	    if(nc == null || isStruct(nc)) {
		// Only situation for negative bias: end of paragraph.
		offs = clen;
		if(n > 0) return false;
		return true;
	    }
	    load(nc);
	}
	while(n < 0) {
	    ZZCell nc = c.s("d.nile", -1);
	    if(nc == null || isStruct(nc)) {
		offs = 0;
		return false;
	    }
	    load(nc);
	    n += clen;
	}

	offs = n;
	return true;
    }

    /** Get the character at current offset.
     * Returns -1 if out of bounds.
     */
    public int cur() {
	if(offs >= clen || offs < 0) return -1;
	return ctext.charAt(offs);
    }
    public int get() { return cur(); }

    public int next() {
	if(!move(1)) return -1;
	return cur();
    }
    
    public int prev() {
	if(!move(-1)) return -1;
	return cur();
    }

    public int get(int at) {
	ZZCell cache = c;
	int offscache = offs;
	int ret = -1;
	if(move(at)) {
	    ret = cur();
	}
	if(c != cache)
	    load(cache);
	offs = offscache;
	return ret;
    }

    /** Move n paragraphs, moving as little as possible.
     * This means that if we move backwards, we will be at the end
     * of the paragraph and vice versa.
     */
    public boolean shiftPara(int n) {
	// XXX What about empty paragraphs? A LOT of trouble...
	if(n == 0) return true;
	int dir = (n > 0 ? 1 : -1);
	ZZCell nc = c;
	while(n != 0) {
	    nc = nc.s("d.nile", dir);
	    if(nc == null) return false;
	    if(isStruct(nc)) n -= dir;
	}
	// Now we are at the paragraph cell.
	// Need to take one step.
	nc = nc.s("d.nile", dir);
	if(nc == null) {
	    return false; // XXX Should set position!
	    // throw new ZZError("AUGH! Para shifting null next");
	}
	load(nc);
	offs = (dir < 0 ? clen : 0);
	return true;
    }

    /** Shift to paragraph beginning or end.
     */
    public boolean paraEnd(int dir) {
	ZZCell nc = c;
	ZZCell prev = nc;
	while(nc != null && !isStruct(nc)) {
	    prev = nc;
	    nc = nc.s("d.nile", dir);
	}
	if(nc == null) {
	    nc = prev;
	} else {
	    nc = nc.s("d.nile", -dir);
	}
	load(nc);
	offs = (dir < 0 ? 0 : clen);
	return true;
    }

    public void streamEnd(int dir) {
    // XXX When going to beginning, may set pos to struct!
	ZZCell h = c.h("d.nile", dir);
	paraEnd(dir);
    }

    /** Get the current position with given bias.
     * @param bias Whether we are looking into positive or negative
     *		   direction. If the cursor is at an edge,
     *		   this affects which cell is returned.
     */
    public ZZCursor getCursor(int bias) {
	// XXX Check para?
	if(bias < 0 && offs == 0) {
	    ZZCell nc = c.s("d.nile", -1);
	    return new ZZCursorVirtual(nc, nc.getText().length());
	}
	return new ZZCursorVirtual(c, offs);
    }
    public ZZCursor getCursor() { return getCursor(1); }


    /** Cut the stream at this location.
     * This pointer remains pointing to the prior space,
     * and it returns a iter at the beginning of the new stream.
     */
    public Nile2Iter cut() {
	move(0);
	Nile2Iter ni = new Nile2Iter();
	if(offs == 0) {
	    ZZCell prev = c.getOrNewCell("d.nile", -1);
	    prev.disconnect("d.nile", 1);
	    ni.load(c, 0);
	    load(prev);
	    offs = clen;
	    return ni;
	}
	if(offs == clen) { // must be very end.
	    ZZCell next = c.getOrNewCell("d.nile", 1);
	    ni.load(next, 0);
	    c.disconnect("d.nile", 1);
	    return ni;
	}
	ZZCell spl = split(c, offs, true);
	c.disconnect("d.nile", 1);
	reload();
	ni.load(spl, 0);
	return ni;
    }

    // XXX Cursors easily cut out!

    public void join(Nile2Iter to) {
	c.h("d.nile", 1).connect("d.nile", to.c.h("d.nile", -1));
	reload();
    }

    public void adjustSpaceStart(int nstart) {
	if(dbg) dumpStream();
	Nile2Iter ctr = (Nile2Iter)clone();
	int ncur = 0;
	while(Nile2Unit.ws(ctr.get())) {
	    ncur ++; ctr.move(1);
	}
	if(nstart < ncur) {
	    move(ncur-nstart);
	    // XXX Cursors could go wild.
	    set(cut());
	} else if(nstart > ncur) {
	    StringBuffer buf = new StringBuffer();
	    while(nstart-- > ncur) buf.append(' ');
	    ZZCell n = c.N("d.nile", -1);
	    n.setText(buf.toString());
	    load(n, 0);
	}
    }

    public void adjustSpaceEnd(int nend) {

	Nile2Iter ctr = (Nile2Iter)clone();
	ctr.streamEnd(1);
	int ncur = 0;
	while(Nile2Unit.ws(ctr.get(-1))) {
	    ncur ++; ctr.move(-1);
	}
	if(nend < ncur) {
	    ctr.move(nend); // Move back forwards.
	    // XXX Cursors could go wild.
	    ctr.cut();
	} else if(nend > ncur) {
	    StringBuffer buf = new StringBuffer();
	    while(nend-- > ncur) buf.append(' ');
	    ZZCell n = ctr.c.h("d.nile", 1).N("d.nile", 1);
	    n.setText(buf.toString());
	}
	if(dbg) dumpStream();
    }

    public void adjustSpaces(int nstart, int nend) {
	p("Adjustspaces: "+nstart+" "+nend);
	adjustSpaceStart(nstart);
	adjustSpaceEnd(nend);

    }

    public Object clone() { 
	try {
	    return super.clone(); 
	} catch(CloneNotSupportedException e) {
	    ZZLogger.exc(e);
	    return null;
	}
    }

    public void dumpStream() {
	pa("Dumping Nile2Iter stream: at offs: "+offs);
	dump(c);
    }

    /** Returns true if this &lt;= it. 
     * Throws an error if not in same stream.
     */
    public boolean isOrdered(Nile2Iter it) {
	if(c.equals(it.c)) {
	    return offs <= it.offs;
	} else {
	    if(c.findCell("d.nile", 1, it.c)) return true;
	    if(!c.findCell("d.nile", -1, it.c))
		throw new ZZError("IsOrdered: not on same stream!");
	    return false;
	}
    }

    public void insertInCell(String s) {
	ZZCell spl = split(c, offs, true);
	spl = spl.N("d.nile", -1);
	spl.setText(s);
	load(spl, -1);
    }

    public void insert(String str) {
	ZZCell n = c;
	if(isStruct(c)) {
	    load(c.s("d.nile"), -1);
	    ZZCell d = n.N("d.nile");
	    d.setText(str);
	    ZZLogger.log("AUGH! Insert at struct cell!");
	    // throw new ZZError("Inserting");
	} else if((offs == 0 || offs == clen) && c.getSpan() == null) {
	    if(offs == clen) {
		c.setText(c.getText() + str);
		load(c, -1);
	    } else { // In this case, this iter doesn't move ;)
		ZZCell prev = c.s("d.nile", -1);
		if(isStruct(prev) || prev == null)
		    c.N("d.nile", -1).setText(str);
		else
		    prev.setText(prev.getText() + str);
	    }
	} else {
	    if(offs > 0 && offs < clen) split(c, offs, false);
	    ZZCell d = c.N("d.nile", offs > 0 ? 1 : -1);
	    d.setText(str);
	    load(d, -1);
	}
    }

    /** Make a cell's content into a span. */
    static public Span spanify(ZZCell c0) {
	Span sp = c0.getSpan();
	if(sp != null) return sp;
	
        StringScroll sc = c0.getSpace().getStringScroll();
	String s = c0.getText();
        long soffs = sc.append(s);
        sp = Span.create(
            Address.scrollOffs(sc, soffs),
            Address.scrollOffs(sc, soffs + s.length() - 1)
        );
        c0.setSpan(sp);
	return sp;
    }

    /** Split the given cell into two at the offset offset.
     * The original cell will remain the first.
     * Returns the cell that is after the split.
     * If offset is zero or greater than or equal to the number of characters
     * in c, nothing is done and either the current cell or the next cell is
     * returned.
     * <p>
     * The most special case involves doing this at the very end of the stream,
     * in which case an empty new cell is created and returned.
     * @param c The cursor to split at
     * @param always Whether to split even if the offset is at the beginning
     * 			or end of a cell. Used e.g. for inserting a paragraph.
     * @return Never null.
     */
    static public ZZCell split(ZZCell c0, int offs, boolean always) {
	// XXX Adjust cursors!!!
	if(always) {
	    ZZCell c1 = c0.N("d.nile");
	    if(c0.s("d.nile-transcluded") != null) {
		c0.insert("d.nile-transcluded", 1, c1);
		c0.insert("d.nile-transcluded-split", 1, c1);
	    }
	    Span sp = c0.getSpan();
	    if(sp == null) {
		String s = c0.getText();
		c0.setText(s.substring(0, offs));
		c1.setText(s.substring(offs));
	    } else {
		c0.setSpan(sp.subSpan(0, offs));
		c1.setSpan(sp.subSpan(offs));
	    }
	    return c1;
	} else {
	    if(offs == 0) return c0;
	    if(offs == c0.getText().length())
		return c0.getOrNewCell("d.nile");
	    return split(c0, offs, true);
	}
    }

    /** Internal convenience: make a connection and a ring if it isn't yet
     * This is part of a cheat to make jumping between transclusions work:
     * we connect all transclusions of a cell on d.nile-transcluded, even
     * if the transcluded spans do not overlap, and jump between them.
     * Works because currently, a cell can contain only one span.
     * XXX what to do when splitting ???
     * XXX if we cheat anyway, is it more appropriate to use zz clones?
     */
    static private void transcluded(ZZCell from, ZZCell to) {
	from.insert("d.nile-transcluded", 1, to);
	if(from.s("d.nile-transcluded", -1) == null)
	    from.connect("d.nile-transcluded", -1, to);
    }


    /** Transcopy the part of the stream between two iters.
     * Sets the iters to point to the beginning and the end of the transcopied
     * stream.
     */
    static public void copy(Nile2Iter start, Nile2Iter to) {
	if(!start.isOrdered(to)) {
	    Nile2Iter tmp = to;
	    to = start; start = tmp;
	}

	// First, spanify all cells.
	ZZCell d = start.c;
	while(true) {
	    if(!isStruct(d)) spanify(d);
	    p("Spanified "+d+" is span "+d.getSpan());
	    if(d == to.c) break;
	    d = d.s("d.nile");
	}
	
	// Next, create the new stream.
	ZZCell first, last;
	if(start.c.equals(to.c)) {
	    first = start.c.N();
	    last = first;
	    first.setSpan(start.c.getSpan().subSpan(start.offs, to.offs));
	    transcluded(start.c, first);
	} else {
	    first = start.c.N();
	    first.setSpan(start.c.getSpan().subSpan(start.offs));
	    transcluded(start.c, first);
	    
	    ZZCell prev = first;
	    ZZCell here = start.c.s("d.nile");
	    while(here != to.c) {
		prev = prev.N("d.nile");
		prev.setSpan(here.getSpan());
		transcluded(prev, here);
		here = here.s("d.nile");
	    }
	    
	    last = prev.N("d.nile");
	    last.setSpan(to.c.getSpan().subSpan(0, to.offs));
	    transcluded(to.c, last);
	}
	
	// Finally, set the iters.
	start.set(new ZZCursorVirtual(first, 0));
	to.set(new ZZCursorVirtual(last, last.getText().length() - 1));
    }

    static public void dump(ZZCell start) {
	 if(start == null) {
	    pa("Nile stream Null start!");
	    return;
	 }
	 ZZCell init = start.h("d.nile", -1);
	 pa("Nile stream! "+start);
	 boolean first = true;
	 for(ZZCell c = init; c != null; c=c.s("d.nile")) {
	    pa((c.s("d.nile-struct") != null ? "***S*** " : "   ") +
		    "	Cell "+c+"   "+
		    (c.equals(start) ? " <----------------START" : ""));
	    if(!first && c.equals(init)) {
		pa("		WAS A LOOOOOOOP!!!!!!!!!!");
		throw new ZZFatalError("LOOPING NILE STREAM! BAD BAD TROUBLE!");
	    }
	    first = false;
	 }
    }

    static String[] levels = new String[] {
	"P", "H3", "H2", "H1"
    };

    static int getMaxLevel() { return levels.length - 1; }

    // XXX Should use clones.
    /** Set the level of a structural cell.
     */
    static void setStrLevel(ZZCell str, int level) {
	if(level < 0 || level > levels.length) 
	    throw new ZZError("Request to set invalid level! "+level);
	str.setText(levels[level]);
    }

    /** The level of structure the given structure cell is.
     * Note that the levels are a bit interesting: it returns
     * zero for paragraphs, 1 for H6, 2 for H5 and so on.
     * -1 is returned for cells that are not at a structural border.
     */
    static int getStrLevel(ZZCell str) {
	String txt = str.getText();
	for(int i=0; i<levels.length; i++)
	    if(txt.equals(levels[i])) return i;
	throw new ZZError("Invalid nile-struct "+txt);
    }


    /** Find an angle in the rank, i.e.the next cell on a rank with 
     * a connection into a certain direction.
     */
    static ZZCell findAngle(ZZCell c, String dim, int dir, 
		    String adim, int adir, boolean includeThis) {
	LoopDetector ld = new LoopDetector();
	if(includeThis && c.s(adim, adir) != null) return c;
	c = c.s(dim, dir);
	while(c != null) {
	    if(c.s(adim, adir) != null) return c;
	    ld.detect(c);
	    c = c.s(dim, dir);
	}
	return null;
    }

    /** Find the next or previous cell that is a structural level element.
     */
    static ZZCell findStruct(ZZCell c, int dir, boolean includeThis) {
	ZZCell ret = findAngle(c, "d.nile", dir, "d.nile-struct", 1, includeThis);
	if(ret == null && dir == -1) {
	    if(!includeThis && c.s("d.nile", -1) == null)
		return null;
	    return c.h("d.nile", -1);
	}
	return ret;
    }


    /** Break a paragraph at the given position.
     * Sets this position to the first position in the possibly
     * empty new paragraph.
     */
    public void breakParagraph() {
	ZZCell n = split(c, offs, true);
	load(n, 0);
	// INsert the real structural cell between.
	n = n.N("d.nile", -1);
	ZZCell str = n.N("d.nile-struct");
	setStrLevel(str, 0); // XXX Lists...
    }


    /** Get the level of a paragraph.
     * @param c Any cell in the paragraph - does not need to be the first.
     */
    static public int getLevel(ZZCell c) {
	ZZCell para = findStruct(c, -1, true);
	ZZCell str = para.s("d.nile-struct");
	if(str == null) return 6;
	return getStrLevel(str);
    }



}