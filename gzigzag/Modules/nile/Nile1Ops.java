/*   
Nile1Ops.java
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

/** A library of routines for operating on Nile1 streams.
 * @see Nile1
 * @deprecated Use Nile2Ops.
 */

public abstract class Nile1Ops {
public static final String rcsid = "$Id: Nile1Ops.java,v 1.16 2000/12/07 00:37:19 tjl Exp $";
    public static boolean dbg = true;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

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
    static public ZZCell split(ZZCursor c, boolean always) {
	// XXX Adjust cursors!!!
	ZZCell c0 = c.get();
	int offs = c.getOffs();
	if(always) {
	    ZZCell c1 = c0.N("d.nile");
	    String s = c0.getText();
	    c0.setText(s.substring(0, offs));
	    c1.setText(s.substring(offs));
	    return c1;
	} else {
	    if(offs == 0) return c0;
	    if(offs == c0.getText().length())
		return c0.getOrNewCell("d.nile");
	    return split(c, true);
	}
    }

    /** Break a paragraph at the given position.
     * Returns the first cell of the (possibly empty) new paragraph.
     */
    static public ZZCell breakParagraph(ZZCursor c) {
	ZZCell n = split(c, true);
	if(n.s("d.nile-struct") != null)
	    throw new ZZError("split structure already!");
	ZZCell str = n.N("d.nile-struct");
	setStrLevel(str, 0);
	return n;
    }

    /** Change the structural level of a paragraph.
     * @param c Any cell in the paragraph - does not need to be the first.
     * @param level The new level.
     */
    static public void setLevel(ZZCell c, int level) {
	ZZCell para = findStruct(c, -1, true);
	ZZCell str = para.s("d.nile-struct");
	if(str == null) return;
	setStrLevel(str, level);
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

    /** Insert the given string into the stream at cursor.
     */
    static public void insert(ZZCursor c, String txt) {
	throw new ZZError("Not implemented");
    }




    /** The level of structure the given structure cell is.
     * Note that the levels are a bit interesting: it returns
     * zero for paragraphs, 1 for H6, 2 for H5 and so on.
     * -1 is returned for cells that are not at a structural border.
     */
    static int getStrLevel(ZZCell str) {
	String txt = str.getText();
	if(txt.equals("MP")) // Middle of paragraph - special level.
	    return -1;
	if(txt.equals("P"))
	    return 0;
	if(txt.equals("H1"))
	    return 6;
	if(txt.equals("H2"))
	    return 5;
	if(txt.equals("H3"))
	    return 4;
	if(txt.equals("H4"))
	    return 3;
	if(txt.equals("H5"))
	    return 2;
	if(txt.equals("H6"))
	    return 1;
	throw new ZZError("Invalid nile-struct "+txt);
    }

    static String[] levels = new String[] {
	"P", "H6", "H5", "H4", "H3", "H2", "H1"
    };

    /** Set the level of a structural cell.
     */
    static void setStrLevel(ZZCell str, int level) {
	if(level < 0 || level > levels.length) 
	    throw new ZZError("Request to set invalid level! "+level);
	str.setText(levels[level]);
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

    /** An editing mode, i.e.the definition of what are the breaks
     * that are being operated on in this mode.
     */
    public interface Mode {
	/** Set the mode cursor, and possibly selection cursor, to the
	 * given cursor.
	 * This is called in response to e.g. mouse click, so the position
	 * may be adjusted to the beginning of the previous unit.
	 */
	void setCursor(ZZCell view, ZZCursor c);

	/** Move the cursor nsteps units.
	 */
	void moveCursor(ZZCell view, int nsteps);

	/** Extend/shrink the selection by n units in given direction.
	 */
	void adjust(ZZCell view, int end, int nsteps);

	/** Hop the selection.
	 */
	void hop(ZZCell view, int nsteps);

	/** Delete the current selection, move cursor backwards or forwerds.
	 */
	// void delete(ZZCell view, int movedir);
    }

    public static class EdgeMode implements Mode {
	Nile1Text.Edger edger;
	EdgeMode(Nile1Text.Edger edger) { this.edger = edger; }

	public void setCursor(ZZCell view, ZZCursor c) {
	    Stream.Iterator iter = giter(c);
	    edger.edge(iter, true, -1, Nile1Text.START);
	    curs(view).set(iter.get());
	    edger.edge(iter, false, 1, Nile1Text.END);
	    endcurs(view).set(iter.get());
	}
	// Move from end of area if forwards, from start if backwards.
	public void moveCursor(ZZCell view, int nsteps) {
	    if(nsteps <= 0) {
		Stream.Iterator iter = giter(curs(view));
		if(!edger.edge(iter, false, nsteps, Nile1Text.START))
		    return;
		curs(view).set(iter.get());
		edger.edge(iter, false, 1, Nile1Text.END);
		endcurs(view).set(iter.get());
	    } else {
		Stream.Iterator iter = giter(endcurs(view));
		if(!edger.edge(iter, false, nsteps, Nile1Text.END))
		    return;
		endcurs(view).set(iter.get());
		edger.edge(iter, false, -1, Nile1Text.START);
		curs(view).set(iter.get());
	    }
	}
	public void adjust(ZZCell view, int end, int nsteps) {
	    ZZCursor c1 = curs(view);
	    ZZCursor c2 = endcurs(view);
	    if(end < 0) {
		Stream.Iterator iter = Stream.getIterator(c1, "d.nile");
		if(!edger.edge(iter, false, nsteps, Nile1Text.START)) 
			return;
		ZZCursor curs = iter.get();
		if(!isOrdered(curs, c2)) return;
		c1.set(curs);
	    } else {
		Stream.Iterator iter = Stream.getIterator(c2, "d.nile");
		if(!edger.edge(iter, false, nsteps, Nile1Text.END)) 
			return;
		ZZCursor curs = iter.get();
		if(!isOrdered(c1, curs)) return;
		c2.set(curs);
	    }
	    // XXX Check that they do not cross...
	}
	public void hop(ZZCell view, int nsteps) {
	    p("Cut out the selection for hop");
	    ZZCell selec = cutSelection(view);
	    if(selec == null) return;

	    ZZCursor c1 = curs(view);

	    p("Now, find place to paste at hopped location");
	    Stream.Iterator iter = Stream.getIterator(c1, "d.nile");
	    edger.edge(iter, false, nsteps, Nile1Text.START);
	    c1.set(iter.get());
	    p("And paste");
	    paste(view, selec, true);
	    
	}
    }
    static Mode wordmode = new EdgeMode(new Nile1Text.WordEdger());
    static Mode sentencemode = new EdgeMode(new Nile1Text.SentenceEdger());

    static public Mode getMode(ZZCell view) {
	// return wordmode;
	return sentencemode;
    }

    // XXX
    static ZZCell adjStart(ZZCell c, int adj) {
	if(adj > 0) {
	    // XXX Easily screws up font change!
	    ZZCursor c1 = new ZZCursorVirtual(c, 0);
	    if(!Stream.move(c1, "d.nile", adj))
		throw new ZZError("ARRRRGH!");
	    if(c1.get().equals(c)) {
		c.setText(c.getText().substring(adj));
	    } else {
		c = split(c1, false);
		c.disconnect("d.nile", -1);
	    }
	} else if(adj < 0) {
	    // XXX Spans
	    String s = "";
	    while(adj++ < 0) s = s + " "; // XXX
	    ZZCell n = c.N("d.nile", -1);
	    n.setText(s);
	}
	return c;
    }

    static ZZCell adjEnd(ZZCell c, int adj) {
	if(adj > 0) {
	    ZZCursor c1 = new ZZCursorVirtual(c, c.getText().length());
	    if(!Stream.move(c1, "d.nile", -adj))
		throw new ZZError("ARRRRGH!");
	    // XXX

	    c = c1.get();
	    c.setText(c.getText().substring(0, c1.getOffs()));
	    c.disconnect("d.nile", 1);
	} else if(adj < 0) {
	    c.setText(c.getText()+" ");
	}
	return c;
    }

    static void adjustSelection(Stream.Iterator start, Stream.Iterator end) {
	Nile1Text.wordEdge(start, true, 1, Nile1Text.START);
	Nile1Text.wordEdge(end, true, -1, Nile1Text.END);
    }

    static ZZCell cutSelection(ZZCell view) {
	ZZCursor curs1 = curs(view);

	p("Cut");
	dump(curs1.get());

	ZZCursor curs2 = endcurs(view);
	if(!isOrdered(curs1, curs2)) return null;
	Stream.Iterator iter1 = Stream.getIterator(curs1, "d.nile");
	Stream.Iterator iter2 = Stream.getIterator(curs2, "d.nile");
	int[] adj = new int[2];
	Nile1Text.adjustCut(iter1, iter2, adj);
	ZZCursor c1 = iter1.get();
	ZZCursor c2 = iter2.get();

	ZZCell after = split(c2, false);
	p("\nAFTER SPLIT:");
	dump(after);

	ZZCell first = split(c1, false);
	p("\nFIRST SPLIT:");
	dump(first);


	ZZCell prev = first.s("d.nile", -1);
	ZZCell last = after.s("d.nile", -1);
	after.disconnect("d.nile", -1);
	first.disconnect("d.nile", -1);

	if(prev != null) prev.connect("d.nile", after);

	p("AFTER RECONNECT");
	dump(prev);

	curs(view).set(after);
	curs(view).setOffs(0);

	// Now, if more characters need to be left out, do it.
	first = adjStart(first, adj[0]);
	last = adjEnd(last, adj[1]);
	if(adj[1] != 0) throw new ZZError("End adj not implemented");

	p("AFTER CUTADJ");
	dump(first);

	return first;
    }

    /** Paste the given stream into the given cursor.
     * The range and the text are adjusted for word-by-word
     * insertion.
     * @param view The viewcell that gives the location to
     *		   paste into. Used later to set the new range,
     *		 	if desired.
     * @param what The first cell of the stream to paste in.
     * @param setRange Whether to set the selection of the given
     *			view to the pasted range afterwards.
     */
    static void paste(ZZCell view, ZZCell what, boolean setRange) {

	ZZCursor v = curs(view);

	p("PASTE");
	dump(v.get());
	p("PASTING WHAT");
	dump(what);

	int[] adj = new int[2];
	Stream.Iterator to = Stream.getIterator(v, "d.nile");
	Stream.Iterator start = giter(new ZZCursorVirtual(what, 0));
	ZZCell last = what.h("d.nile", 1);
	Stream.Iterator end = giter(new ZZCursorVirtual(last, 
		    last.getText().length()));

	Nile1Text.adjustPaste(to, start, end, adj);

	what = adjStart(start.get().get(), adj[0]);
	adjEnd(end.get().get(), adj[1]);

	p("WHAT ADJUSTED");
	dump(what);

	ZZCell after = split(v, false);
	ZZCell before = after.s("d.nile", -1);
	after.disconnect("d.nile", -1);
	after.connect("d.nile", -1, what.h("d.nile", 1));
	if(before != null)
	    before.connect("d.nile", 1, what);

	if(setRange) {
	    v.set(what);
	    v.setOffs(0);

	    start = giter(v);

	    ZZCursor ev = endcurs(view);
	    ev.set(after);
	    ev.setOffs(0);

	    end = giter(ev);

	    adjustSelection(start, end);

	    v.set(start.get());
	    ev.set(end.get());
	}

	p("AFTER EVERYTHING");
	dump(what);
    }

    // XXX Should use the tree for help...
    static public boolean isOrdered(ZZCursor cu1, ZZCursor cu2) {
	ZZCell c1 = cu1.get();
	ZZCell c2 = cu2.get();
	if(c1 == null || c2 == null) return false;
	if(c1.equals(c2))
	    return cu1.getOffs() <= cu2.getOffs();
	for(; c1 != null && !c1.equals(c2); c1 = c1.s("d.nile"));
	if(c1 == null) return false;
	return (c1.equals(c2));
    }

    // Shorthand...
    static public ZZCursorReal curs(ZZCell view) {
	return new ZZCursorReal(view);
    }
    static public Stream.Iterator giter(ZZCursor c) {
	return Stream.getIterator(c, "d.nile");
    }

    static public ZZCursorReal endcurs(ZZCell view) {
	ZZCell end = view.getOrNewCell("d.nile-sel");
	return new ZZCursorReal(end);
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

}
