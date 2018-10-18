/*   
SplitCellFlob1.java
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
import java.awt.event.*;
import java.util.*;

/** A cell, possibly split over a line break.
 * The broken parts are chained as a linked list.
 * The cell of this flob is zero if this is not the first in chain.
 * The text is rendered in the lowest part, just so that all of it
 * is inside the rectangle.
 * <p>
 * XXX Rework the nextPart, parent, next, prev relation!
 */

public class SplitCellFlob1 extends Flob implements SpanFlob {
public static final String rcsid = "$Id: SplitCellFlob1.java,v 1.39 2001/05/10 21:32:47 raulir Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    // The XOR modes.
    static public final int XOR_NO = 0;
    static public final int XOR_LINE = 1;
    static public final int XOR_FRAME = 2;
    static public final int XOR_SOLID = 3;

    public int xormode = XOR_NO;


    /** Whether to clip before drawing the contents of this flob. */
    public boolean doClip = false;

    /** The next part of this flob.  */
    SplitCellFlob1 nextPart;

    /** The first of this chain. */
    public SplitCellFlob1 parent;

    /** Index of first character.  */
    public int start;

    /** Number of characters.  */
    public int n;

    /** The text in this flob (substring of c.getText()).  
     * This text is allowed to be modified from outside to allow e.g. for
     * firstcap. Doing that in any way that does not retain the number
     * and offsets of characters <em>will</em> screw up beams and many other
     * things so don't do that ;)
     */
    public String txt;

    /** The cell which is returned by hit(). */
    public Object handle;

    public SpanFlob prev, next;
    public SpanFlob getPrev() { return prev; }
    public SpanFlob getNext() { return next; }
    public void setPrev(SpanFlob sf) { 
	prev = sf;
	if(prev != null) prev.setNext(this);
    }
    public void setNext(SpanFlob sf) { next = sf; }

    public Color bg, fg = Color.black;

    Font f;
    FontMetrics fm;

    /** Set the background of this flob to the color of the topmost cursor. */
    public void showCursor() {
            // Only take topmost cursor's color
            ZZCell curs = getCell().s("d.cursor", 1);
	    if(curs != null) {
		bg = ZZCursorReal.getColor(curs);
		if(bg == null)
		    pa("No cursor color!");
	    } else
		p("No cursors on this cell.");
    }

    static boolean intostr = false;
    public String toString() {
	if(intostr) return super.toString();
	intostr = true;
	String ret = super.toString() + "(SPLCF1 bg="+bg+
	  "Parent: ("+parent+") stn: "+start+" "+n+" Txt:'"+txt+"' Cell:"+c
	  +" next: "+nextPart
	+ " )";
	intostr = false;
	return ret;
    }

    public SplitCellFlob1(int x, int y, int d, int w, int h,
	ZZCell c, Font f, FontMetrics fm, SplitCellFlob1 parent,
	    int start, int n) {
        this(x, y, d, w, h, c, f, fm, parent, start, n, null);
    }

    public SplitCellFlob1(int x, int y, int d, int w, int h,
	ZZCell c, Font f, FontMetrics fm, SplitCellFlob1 parent,
	    int start, int n, Color bg) {
	this(x, y, d, w, h, c,
	    c.getText().substring(start, start+n),
	    f, fm, parent, start, n, bg);
    }
	
    public SplitCellFlob1(int x, int y, int d, int w, int h,
	ZZCell c, String txt, Font f, FontMetrics fm, SplitCellFlob1 parent,
	    int start, int n, Color bg) {

	super(x, y, d, w, h, (parent==null ? c : null));
	p("SplitCellFlob: "+x+" "+y+" "+d+" "+w+" "+h+" "+c+" "+f+" "+
	    fm+" "+parent+" "+start+" "+n+" "+bg);
	this.f = f; this.fm = fm;
	if(parent != null) {
	    SplitCellFlob1 prev = parent;
	    while (prev.nextPart != null) prev = prev.nextPart;
	    prev.nextPart = this;
	    this.parent = (parent.parent == null ? parent: parent.parent);
	    if(!this.parent.c.equals(c))
		throw new ZZError(
		    "Parent and child splitcellflob must have same cell: "+c+
			" "+parent.c);
	}
	this.start = start; this.n = n;
	this.txt = txt;
        this.bg = bg;
	this.handle = getCell();
	p("Created splitcellflob: "+this);
    }

    public SplitCellFlob1(int x, int y, int d, int w, int h,
	ZZCell c, Font f, FontMetrics fm, SplitCellFlob1 parent,
	    int start, int n, int xormode) {
        this(x, y, d, w, h, c, f, fm, parent, start, n);
    }

    public SplitCellFlob1(int x, int y, int d, int w, int h,
	ZZCell c, Font f, FontMetrics fm, SplitCellFlob1 parent,
	    int start, int n, int xormode, Color bg) {
	this(x, y, d, w, h, c, f, fm, parent, start, n, bg);
	this.xormode = xormode;
    }

    public final ZZCell getCell() {
	return (parent==null ? c : parent.c);
    }

    public Span getSpan() {
	Span sp = getCell().getSpan();
	if(sp == null) return null;
	// XXX test for samity
	return sp.subSpan(start, start+n);
    }

    public Rectangle getRectangle(Span s) {
	Span t = getCell().getSpan();
	int so = (int)(s.getStart().getOffs() - t.getStart().getOffs());
	int eo = (int)(s.getEnd().getOffs() - t.getStart().getOffs());
	int sw = fm.stringWidth(txt.substring(0, so));
	int ew = fm.stringWidth(txt.substring(0, eo));
	return new Rectangle(x + sw, y, ew-sw, h);
    }
    public Rectangle getRectangle(int offs, int end) {
	int sw = fm.stringWidth(txt.substring(0, offs));
	int ew = fm.stringWidth(txt.substring(0, end));
	return new Rectangle(x + sw, y, ew-sw, h);
    }

    /* Very quick hack to interpolate from and to non-span flobs:
     * let this read render(g0, x, y, d, w, h) instead of render(g0). Not at
     * all what the interpolation SHOULD look like, but better than the
     * nothing we've had before.
     */
    public void render(Graphics g0, int x, int y, int d, int w, int h) {
	int my = y + h - fm.getDescent();
	// p("SpliCellFlob drawString: '"+txt+"' "+x+" "+my+" "+fg+" "+bg);

	Graphics g = g0.create();
	Color bu = g.getColor();
	if (doClip) g.clipRect(x, y, w, h);
	g.setFont(f);
	if(bg != null) {
	    g.setColor(bg);
	    g.fillRect(x, y, w, h);
	}
	if(fg != null) g.setColor(fg);
	else g.setColor(bu);
	p("SpliCellFlob drawString: '"+txt+"' "+x+" "+my+" "+fg+" "+bg);
	g.drawString(txt, x, my);

	g.dispose();

	if(nextPart != null)
	    nextPart.render(g0);
    }

    /** Render an interpolated version.
     * This is the trickiest routine of this class since we <em>do</em>
     * want to get the interpolation behaviour just right so that all text
     * pieces flow to where they are supposed to.
     */
    public void renderInterp(Graphics g0, float fract) {
	if(interpTo==null) return;
	if(!(interpTo instanceof SplitCellFlob1)) {
	    super.renderInterp(g0, fract);
	    return;
	}
	SplitCellFlob1 t = this;
	SplitCellFlob1 r = (SplitCellFlob1)interpTo;

	Graphics g = g0.create();
	Shape clip = g.getClip();
	if(fg != null) g.setColor(fg);

	// Render the correct fragment of the two and advance.
	while(t!=null && r!=null) {
	    if(t.start+t.n < r.start) { t = t.nextPart; continue; }
	    if(t.start > r.start+r.n) { r = r.nextPart; continue; }
	    int st = 0;
	    int en = 0;
	    if(t.start < r.start) st = r.start;
		else st = t.start;
	    if(t.start+t.n < r.start+r.n) en = t.start+t.n;
		else en = r.start+r.n;
	    int tx = t.getStrX(st);
	    int rx = r.getStrX(st);
	    int x = (int)(tx + (rx-tx)*fract);
	    int ty = t.y + t.h - t.fm.getDescent();
	    int ry = r.y + r.h - r.fm.getDescent();
	    int y = (int)(ty + (ry-ty)*fract);
	    int tw = t.getStrX(en) - tx;
	    int rw = r.getStrX(en) - rx;
	    int w = (int)(tw + (rw-tw)*fract);
	    int realy = (int)(t.y + (r.y-t.y)*fract);
	    if (doClip) {
		g.clipRect(x, realy, w, t.h);
	    }
	    if(bg != null) {
		Color col = g.getColor();
		g.setColor(bg);
		g.fillRect(x, realy, tw, t.h);
		g.setColor(col);
	    }
	    g.setFont(t.f);
	    g.drawString(t.txt.substring(st-t.start, en-t.start), x, y);
	    if (doClip) g.setClip(clip);
	    if(t.start+t.n < r.start+r.n) t = t.nextPart;
	    else r = r.nextPart;
	}
	g.dispose();
    }

    public boolean renderXORLine(Graphics g, int cx, int cy) {
	ZZCursorVirtual vc = (ZZCursorVirtual)hit(cx, cy);
	if(vc==null) return false;
	g.setColor(Color.black);
	g.setXORMode(Color.white);
	int xo = fm.stringWidth(txt.substring(0, vc.getOffs()-start));
	g.drawLine(x+xo, y, x+xo, y+h);
	return true;
    }

    public void renderXORFrame(Graphics g, boolean left, boolean right) {
	// XXX instead of using f.h-1 & f.y+1, just don't create overlapping
	// XXX SplitCellFlobs any more?
	g.setColor(Color.black);
	g.setXORMode(Color.white);
	SplitCellFlob1 f = parent != null ? parent : this;
	if(left) g.drawLine(f.x, f.y+1, f.x, f.y+f.h-1);
	while(true) {
	    g.drawLine(f.x, f.y+1, f.x+f.w, f.y+1);
	    g.drawLine(f.x, f.y+f.h-1, f.x+f.w, f.y+f.h-1);
	    if(f.nextPart != null) f = f.nextPart;
	    else break;
	}
	if(right) g.drawLine(f.x+f.w, f.y+1, f.x+f.w, f.y+f.h-1);
    }

    public void renderXORSolid(Graphics g) {
	g.setColor(Color.black);
	g.setXORMode(Color.white);
	SplitCellFlob1 f = parent != null ? parent : this;
	for(; f != null; f = f.nextPart) {
	    g.fillRect(f.x, f.y, f.w, f.h);
	}
    }

    public boolean renderXOR(Graphics g, int cx, int cy) {
	if(!insideRect(cx, cy)) return false;
	if(xormode == XOR_NO) return true; // XXX or false?
	else if(xormode == XOR_LINE) return renderXORLine(g, cx, cy);
	else if(xormode == XOR_FRAME) renderXORFrame(g, true, true);
	else if(xormode == XOR_SOLID) renderXORSolid(g);
	else throw new ZZError("SplitCellFlob1: Unknown XOR mode");
	return true;
    }

    /** Get the x-coordinate of a given letter.
     * @param ind The index, given as starting from the beginning of the cell.
     * 		Must satisfy start &lt;= ind &lt;= start+n
     */
    public int getStrX(int ind) {
	ind -= start;
	return x + fm.stringWidth(txt.substring(0,ind));
    }

    public Object hit(int x, int y) {
	if(!insideRect(x, y)) return null;
	if(!handle.equals(getCell()))
	    return handle;
	if(xormode == XOR_FRAME || xormode == XOR_SOLID) 
	    return new ZZCursorVirtual(getCell(), ZZCursorVirtual.NO_OFFSET);
	int ind = ZZUtil.findStringHit(txt, x-this.x, fm) + start;
	return new ZZCursorVirtual(getCell(), ind);
    }

    /** Add a line cursor at the given index into the given flobset.
     */
    public void addCurs(FlobSet into, int ind, Color c) {
	if(parent != null)
	    parent.addCurs0(into, ind, c);
	else
	    addCurs0(into, ind, c);
    }
    private void addCurs0(FlobSet into, int ind, Color c) {
	if(ind >= start && ind <= start + n) {
	    int x = getStrX(ind) - 1;
	    into.add(new LineDecor(x, y, x, y+h, 
		    c, d));
	}
	if(nextPart != null)
	    nextPart.addCurs0(into, ind, c);
    }

}
