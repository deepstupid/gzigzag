/*   
FText.java
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

package org.gzigzag;
import java.awt.*;
import java.util.*;

/** A class representing some formatted text.
 * The text is represented as FText.Part objects, which 
 * contain a single string of text in a given font, from a given cell
 * (possibly with an offset and length).
 * These parts may be created by any view.
 * @see FTextLayouter
 */

public class FText {
public static final String rcsid = "$Id: FText.java,v 1.23 2001/04/17 11:25:45 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    public static abstract class Part {
	public Font f; public FontMetrics fm;
	public Color bg, fg;
	public String s; public int n;
	
	/** Make a flob showing this part on the screen. */
	public abstract Renderable rend(int x, int y, int d, Renderable prev);
	
	/** Create another instance of the same class. */
	public abstract Part subpart(int first, int last);
	
	public Part[] split(int at) { return new Part[] { to(at), from(at) }; }
	public Part to(int at) { return subpart(0, at); }
	public Part from(int at) { return subpart(at, n); }
	
	public int getStrX(int at) {
	    return fm.stringWidth(s.substring(0, at)); 
	}
	public int getHit(int x) {
	    return ZZUtil.findStringHit(s, x, fm, true);
	}
	public int getHitX(int x) { return getStrX(getHit(x)); }
	
	private int wd = -1;
	public int width() {
	    if(wd == -1) wd = fm.stringWidth(s);
	    return wd;
	}
	
	private int ht = -1;
	public int height() {
	    if(ht == -1) ht = fm.getHeight(); 
	    return ht;
	}

	public static String join(Part[] ps) {
	    StringBuffer sb = new StringBuffer();
	    for(int i=0; i<ps.length; i++) sb.append(ps[i].s);
	    return sb.toString();
	}
	public static int width(Part[] ps) {
	    int res = 0;
	    for(int i=0; i<ps.length; i++)
		res += ps[i].width();
	    return res;
	}
	public static int height(Part[] ps) {
	    int res = 0;
	    for(int i=0; i<ps.length; i++) {
		int h = ps[i].height();
		if(h > res) res = h;
	    }
	    return res;
	}
	public static int len(Part[] ps) {
	    int res = 0;
	    for(int i=0; i<ps.length; i++) res += ps[i].n;
	    return res;
	}
    }

    public static class CellPart extends Part {
	public ZZCell c;
	public Object handle;
	public int offs;
	
	public CellPart(ZZCell c, Object handle, int offs, int n, Font f, 
			FontMetrics fm, Color bg, Color fg) {
	    this.c = c; this.offs = offs; this.f = f; this.fm = fm;
	    this.handle = handle;
	    this.bg = bg; this.fg = fg;
	    if(n < 0) {
		s = c.t().substring(offs);
		this.n = s.length();
	    } else {
		s = c.t().substring(offs, offs+n);
		this.n = n;
	    }
	}
	public CellPart(ZZCell c, int offs, int n, Font f, 
			FontMetrics fm, Color bg, Color fg) {
	    this(c,c,offs,n,f,fm,bg,fg);
	}
	public Part subpart(int first, int last) {
	    CellPart p = new CellPart(c, handle, offs+first, last-first, f, fm, bg, fg);
	    p.s = s.substring(first, last);
	    return p;
	}
	
	public Renderable rend(int x, int y, int d, Renderable prev0) {
	    SplitCellFlob1 prev = null, parent = null, res;
	    if(prev0 instanceof SplitCellFlob1) {
		prev = (SplitCellFlob1)prev0;
		parent = prev.parent != null ? prev.parent : prev;
		if(!parent.c.equals(c))
		    parent = null;
	    }
	    res = new SplitCellFlob1(x, y, d, width(), height(),
				     c, s, f, fm, parent, offs, n, bg);
	    res.handle = handle;
	    if(prev != null) res.setPrev(prev);
	    return res;
	}
    }

    /** A cell part showing cursor colors in the background. */
    public static class CursorCellPart extends CellPart {
	public CursorCellPart(ZZCell c, Object handle, int offs, int n, Font f, 
			FontMetrics fm, Color bg, Color fg) {
	    super(c, handle, offs, n, f, fm, bg, fg);
	}
	public CursorCellPart(ZZCell c, int offs, int n, Font f, 
			FontMetrics fm, Color bg, Color fg) {
	    super(c, offs, n, f, fm, bg, fg);
	}
	public Renderable rend(int x, int y, int d, Renderable prev) {
	    SplitCellFlob1 sf = (SplitCellFlob1)super.rend(x, y, d, prev);
	    sf.showCursor();
	    return sf;
	}
    }

    public static class StringPart extends Part {
	public StringPart(String s, Font f, FontMetrics fm,
			 Color bg, Color fg) {
	    this.s = s; this.f = f; this.fm = fm; this.bg = bg; this.fg = fg;
	    n = s.length();
	}
	public Part subpart(int first, int last) {
	    return new StringPart(s.substring(first, last), f, fm, bg, fg);
	}
	
	public Renderable rend(int x, int y, int d, Renderable prev) {
	    int my = y - fm.getDescent() + height();
	    TextDecor res = new TextDecor(x, my, s, fg, f);
	    res.d = d;
	    return res;
	}
    }

    public Part[] parts;

    public FText(Part[] ps) { parts = ps; }
    public FText(Vector v) {
	parts = new Part[v.size()];
	for(int i=0; i<parts.length; i++) parts[i] = (Part)v.elementAt(i);
    }
		
    public String get() { return Part.join(parts); }
    public String get(int at, int n) { return get().substring(at, at+n); }

    // return { part, offs }
    public int[] pos(int at) {
	int here = 0;
	for(int i=0; i<parts.length; i++) {
	    Part p = parts[i];
	    here += p.n;
	    if(here >= at) {
		return new int[] { i, p.n-(here-at) };
	    }
	}
	return new int[] { parts.length-1, parts[parts.length-1].n };
    }

    /** Return lines laid out at a given width.
     * The dimensions of the array are lines[line][part]
     */
    public Part[][] getLines(int width) {
	return FirstMatchBreaker.stupidBreak(this, width);
    }

    public static int findLine(Part[][] lines, int offs) {
	int here = 0;
	for(int i=0; i<lines.length; i++) {
	    here += Part.len(lines[i]);
	    if(here >= offs) return i;
	}
	return lines.length-1;
    }

    /** Capitalize the sentences in the whole FText. */
    public void capitalize() {
	Capitalizer c = new Capitalizer();
	for(int i=0; i<parts.length; i++)
	    parts[i].s = c.cap(parts[i].s);
    }
}
