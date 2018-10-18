/*   
Span.java
 *    
 *    Copyright (c) 1999, Ted Nelson and Tuomas Lukka
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
import java.util.*;

/** An address span in the stable media streams.
 * Currently just a silly pointer but will be later extended
 * to include full tumblers.
 * <p>
 * Spans are immutable, just like Strings: all the verb-like method
 * like join and split return new Span objects.
 */

public class Span {
public static final String rcsid = "$Id: Span.java,v 1.9 2001/03/11 19:14:38 veparkki Exp $";
    public static final boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    Address a1; 
    Address a2;

    public Address getStart() { return a1; }
    public Address getEnd() { return a2; }
    public long getStartOffs() { return a1.offset; }
    public long getEndOffs() { return a2.offset; }

    public static Span parse(String str) {
	    StringTokenizer st = new StringTokenizer(str, ":");
	    if(st.countTokens() != 2) 
		    throw new ZZError("Wrong number of ':' toks in '"+str+"'");
	    Span s = new Span();
	    s.a1 = Address.parse(st.nextToken());
	    s.a2 = Address.parse(st.nextToken());
	    if(!s.a1.getStream().equals(s.a2.getStream()))
		    throw new ZZError("Must be same stream for now '" + str
			    +"' '"+s.a1.getStream()+"' '"+s.a2.getStream()+"'");
	    return s;
    }

    public static Span create(Address i1, Address i2) {
	    Span s = new Span();
	    s.a1 = i1; s.a2 = i2;
	    return s;
    }

    public String toString() {
	    return a1+":"+a2;
    }

    public boolean overlaps(Span s) {
	if(a2.lessThan(s.a1) || s.a2.lessThan(a1)) return false;
	return true;
    }

    /** The string pointed to by the span.
     */
    public String getString(ZZCell c)  {
	Scroll scr //= c.getSpace().getStringScroll(a1.getStream());
	    = Scroll.obtain((c==null?null:c.getSpace()), 
			    a1.getStream());
	    p("Scroll: "+scr);
	    if(scr instanceof StringScroll) {
		StringScroll s = (StringScroll) scr;
		int o1 = (int)a1.getOffs();
		int o2 = (int)a2.getOffs();
		if(o1 > o2) {
		    // This is allowed
		    if(o1-o2 == 1) return "";
		    // This is not
		    throw new ZZError("Too negative span "+ this);
		}
		return s.getString(a1.getOffs(), 
			(int)(o2-o1)+1);
	    } if(scr instanceof ByteScroll) {
		    ByteScroll s = (ByteScroll)scr;
		    return s.getString(a1.getOffs(), 
			    (int)(a2.getOffs()-a1.getOffs())+1);
	    }
	    if(scr instanceof CharScroll) {
		    CharScroll s = (CharScroll)scr;
		    return s.getString(a1.getOffs(), 
			    (int)(a2.getOffs()-a1.getOffs())+1);
	    }
	    if(scr instanceof SoundScroll) {
		    return "SND " + toString();
	    }
	    return "???";
    }

    public Span adjustEdge(int edge, long units) {
	Address adj = (edge==0?a1:a2);
	adj = Address.streamOffs(adj.getStream(), adj.getOffs()+units);
	Address x = (edge==0?adj:a1);
	Address y = (edge==1?adj:a2);
	return create(x,y);
    }
    public Span[] adjustEdgeLocked(long units, Span other, long allowfudge) {
	Address adj = a2;
	adj = Address.streamOffs(adj.getStream(), adj.getOffs()+units);
	long d = adj.getOffs() - other.a1.getOffs();
	if(d<0) d = -d;
	if(d>allowfudge)
	    return new Span[] {
		create(a1, adj), other
	    };
    	return new Span[] {
	    	create(a1, adj),
		create(Address.streamOffs(adj.getStream(), adj.getOffs()+1),
		             other.a2)
	};
	
    }
    public Span[] splitInHalf() {
	long off = (a1.getOffs() + a2.getOffs()) / 2;
	return new Span[] {
	    create(a1, Address.streamOffs(a1.getStream(), off-1)),
	    create(Address.streamOffs(a1.getStream(), off), a2),
	};
    }
    public Span join(Span next, long allowfudge) {
	if(! a2.getStream().equals(next.a1.getStream()))
		return null;
	long diff = a2.getOffs() - next.a1.getOffs();
	if(diff<0) diff = -diff;
	if(diff > allowfudge) 
	    return null;
	return create(a1, next.a2);
    }

    /** Does exactly what substring does to String.
     * This is in opposition to the other span routines 
     * in which the last address is given, not one beyond!
     */
    public Span subSpan(int o1, int o2) {
	return create(a1.addOffs(o1), a1.addOffs(o2-1));
    }
    public Span subSpan(int o1) {
	return create(a1.addOffs(o1), a2);
    }

    /** Whether this span can be joined with the other span.
     */
    public boolean isAppendable(Span s) {
	return a2.getStream().equals(s.a1.getStream())
	    && a2.getOffs() == s.a1.getOffs()-1;
    }

    // XXX Check?
    public Span append(Span s) {
	return create(a1, s.a2);
    }
    

    public long length() {
	return a2.getOffs() - a1.getOffs() + 1;
    }

}
