/*   
Address.java
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

/** An address in a stable media stream.
 * Currently just a silly pointer but will be later extended
 * to include full tumblers.
 * Format: streamid.offset, where streamid is an alphanumeric string
 * identifying a stream.
 */

public class Address {
public static final String rcsid = "$Id: Address.java,v 1.3 2000/09/19 10:32:00 ajk Exp $";
	public static final boolean dbg = false;
	static final void p(String s) { if(dbg) System.out.println(s); }

	Scroll scroll;
	String stream;
	long offset;
	public static Address parse(String s) {
		StringTokenizer st = new StringTokenizer(s, ".");
		if(st.countTokens() != 2) {
			throw new ZZError("Wrong number of toks in '"+s+"'");
		}
		Address a = new Address();
		a.stream = st.nextToken();
		a.offset = Long.parseLong(st.nextToken());
		return a;
	}

	public static Address streamOffs(String s, long offs) {
		Address a = new Address();
		a.stream = s;
		a.offset = offs;
		return a;
	}
	public static Address scrollOffs(Scroll s, long offs) {
		// return s.getId() + "." + offs;
		Address a = new Address();
		a.stream = s.getId();
		a.offset = offs;
		return a;
	}

	public String getStream() { return stream; }
	public Scroll getScroll(ZZSpace s) { 
		if(scroll==null) scroll = Scroll.obtain(s, stream);
		return scroll;
	}
	public long getOffs() { return offset; }

	public String toString() {
		return stream.toString() + "." + offset;
	}

	public boolean lessThan(Address a) {
	    int c = stream.compareTo(a.stream);
	    if(c < 0) return true; 
	    if(c > 0) return false;
	    if(offset < a.offset) return true;
	    return false;
	}

	public Address addOffs(int o) {
	    Address a = new Address();
	    a.scroll = scroll;
	    a.stream = stream;
	    a.offset = offset + o;
	    return a;
	}

}

