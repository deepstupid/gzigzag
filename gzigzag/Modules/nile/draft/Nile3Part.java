/*   
Nile3Part.java
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

/** A Nile span, with a start and an end.
 * This object explicitly assumes no modifications to the 
 * stream while it is working.
 */

public final class Nile3Part implements Cloneable {
public static final String rcsid = "$Id: Nile3Part.java,v 1.1 2001/01/07 18:59:06 bfallenstein Exp $";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    public final Nile3Pos start = new Nile3Pos();
    public final Nile3Pos end = new Nile3Pos();

    public Nile3Part(Nile3Pos start, Nile3Pos end) {
	this.start.set(start); this.end.set(end);
	check();
    }
    public Nile3Part(ZZCursor start, ZZCursor end) {
	p(start + " " + end);
	this.start.set(start); this.end.set(end);
	check();
    }

    public Nile3Pos side(int dir) {
	if(dir < 0) return start;
	else return end;
    }

    public Nile3Pos cut() {
	if(isStream())
	    return null; // XXX should this throw an error?
	check();
	start.split(false, 1);
	end.split(false, -1);
	Nile3Pos before = (Nile3Pos)start.clone();
	Nile3Pos after = (Nile3Pos)end.clone();
	if(!before.bias(-1) || !after.bias(1))
	    throw new ZZError("HELP! End of stream after split! XXX");
	start.c.disconnect("d.nile", -1);
	end.c.disconnect("d.nile", 1);
	before.c.connect("d.nile", 1, after.c);
	return before;
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

    public Nile3Part copy() {
	check();
	start.split(false, 1);
	end.split(false, -1);
	ZZCell c = start.c;
	ZZCell first = c.N(), last = first;
	while(true) {
	    last.setSpan(spanify(c));
	    if(c.equals(end.c)) break;
	    c = c.s("d.nile");
	    last = last.N("d.nile");
	}
	return new Nile3Part(
	    new ZZCursorVirtual(first, 0),
	    new ZZCursorVirtual(last, last.getText().length()));
    }
    
    /** Return true if there's nothing before or after this. */
    public boolean isStream() {
	return start.end(-1) && end.end(1);
    }

    /** Check the integrity of this Nile3Part: start &lt;= end? */
    public void check() {
	boolean good = true;
	if(start.c.equals(end.c)) {
	    good = start.offs <= end.offs;
	    p(start.offs + " " + end.offs);
	} else {
	    good = start.c.findCell("d.nile", 1, end.c);
	    p(start.c + " " + end.c);
	}
	if(!good)
	    throw new ZZError("Malformed Nile3Part stream! XXX");
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