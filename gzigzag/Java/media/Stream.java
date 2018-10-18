/*   
Stream.java
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
import java.util.*;

/** Some routines to handle plain text streams.
 */

public class Stream {
public static final String rcsid = "$Id: Stream.java,v 1.9 2000/12/04 13:01:10 tjl Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    static public boolean move(ZZCursor curs, String dim, int nchars) {
	return move(curs, dim, nchars, 1);
    }

    /** Move by an offset in a stream string.
     * The last position of the stream is actually one beyond
     * the last character.
     * @param curs The cursor to move
     * @param dim The dimension the stream is along
     * @param nchars The number of characters to move. May be
     * 			positive or negative.
     * @param lookingDir The direction you want to look from 
     * 			the cursor. Matters when the cursor
     *			is at the edge of two cells and
     * 			positive here means to put it into the
     *			beginning of the next cell and negative
     *			to the end of the preceding cell.
     * @return false if operation goes past beginning or end.
     */
    static public boolean move(ZZCursor curs, String dim, int nchars,
			    int lookingDir) {
	int o = curs.getOffs() + nchars;
	ZZCell nc = curs.get();
	if(nc == null)
	    throw new ZZError("Moving stream cursor!");
	while(nc != null && o < 0) {
	    nc = nc.s(dim, -1);
	    if(nc != null) 
		o += nc.getText().length();
	}
	ZZCell prev = null;
	while(nc != null && o > nc.getText().length() - 
		    (lookingDir > 0 ? 1 : 0)) {
	    o -= nc.getText().length();
	    prev = nc;
	    nc = nc.s(dim);
	}
	if(nc == null) { 
	    if(o != 0) 
		return false;
	    else {
		nc = prev;
		o = nc.getText().length();
	    }
	}
	if(o < 0)
	    throw new ZZError("WHAT? WHAT? WHAT? Neg offset in move");
	curs.set(nc); curs.setOffs(o);
	return true;
    }

    /** Find a position along a stream with the given string.
     * Currently relatively inefficient, unless the cells are relatively
     * long.
     */
    static public boolean find(ZZCursor from, String dim, int dir,
			boolean includeThis, String text) {
	// First, see if there is a match inside the current cell,
	// done carefully.
	ZZCell c = from.get();
	int offs = from.getOffs();

	p("Find: "+c+" "+offs+" "+dim+" "+dir+" "+includeThis+" '"+text+"'");

	String txt = c.getText();
	char first = text.charAt(0);

	if(!includeThis) offs += dir;

	if(dir > 0) {
	    while(c != null) {
		while((offs = txt.indexOf(first, offs)) >= 0) {
		    if(isMatch(c, offs, text, dim, from))
			return true;
		    offs ++;
		}
		c = c.s(dim, dir);
		if(c != null) {
		    txt = c.getText();
		    offs = 0;
		}
	    }
	} else {
	    while(c != null) {
		p("Findnegloop: "+c);
		while((offs = txt.lastIndexOf(first, offs)) >= 0) {
		    p("Findnegloop: try "+ offs);
		    if(isMatch(c, offs, text, dim, from))
			return true;
		    offs --;
		}
		c = c.s(dim, dir);
		if(c != null) {
		    txt = c.getText();
		    offs = txt.length()-1;
		}
	    }
	}

	p("Not found!");

	// Then, we're looking at whole cells the rest of the way.
	return false;
    }

    /** Check to see whether the text in the stream is equal to this text.
     */
    static public boolean isMatch(ZZCell c, int offs, String text, String dim) {
	return isMatch(c, offs, text, dim, null);
    }

    /** Check whether the match is correct and set the cursor if it is.
     */
    static boolean isMatch(ZZCell c, int offs, String text,
			String dim, ZZCursor from)
    {
	p("Match: "+c+" "+offs+" '"+text+"' "+dim);
	p("Matching csubstr: '"+c.getText().substring(offs)+"'");
	ZZCell origc = c;
	int origoffs = offs;
	while(c != null) {
	     String ct = c.getText().substring(offs);
	     if(ct.length() >= text.length()) {
		if(ct.substring(0, text.length()).equals(text)) {
		    if(from != null) {
			from.set(origc);
			from.setOffs(origoffs);
		    }
		    p("TRUE!");
		    return true;
		} else return false;
	     }
	     if(!text.substring(0, ct.length()).equals(ct)) return false;
	     text = text.substring(ct.length());
	     c = c.s(dim);
	     offs = 0;
	}
	return false;
    }

    static public String getString(ZZCursor curs, String dim, int n) {
	ZZCell c = curs.get();
	int offs = curs.getOffs();
	StringBuffer res = new StringBuffer();
	while(n > 0) {
	    String txt = c.getText();
	    if(offs > 0) txt = txt.substring(offs);
	    if(n <= txt.length()) {
		res.append(txt.substring(0, n));
		return res.toString();
	    } 
	    res.append(txt);
	    n -= txt.length();
	    offs = 0;
	    c = c.s(dim);
	    if(c == null) return null;
	}
	return null;
    }

    /** A class for moving along the stream, collecting characters.
     * Part of the interface might be abstracted to an interface like
     * java.text.CharacterIterator, but in a slightly more restricted subset.
     * <p>
     * This class considers the end of the stream the location when it
     * can't return any characters any more + 1.
     */
    static public class Iterator {
	final ZZCursorVirtual c;
	final String dim;

	Iterator(ZZCursorVirtual c, String dim) { this.c = c; this.dim = dim; }

	public void set(ZZCursor c) { this.c.set(c); }

	/** Get the current iterator position.
	 * @return The current position. The position should not be modified.
	 */
	public ZZCursor get() { return c; }

	public boolean move(int nsteps) {
	    if(!Stream.move(c, dim, nsteps)) return false;
	    if(c.get().s(dim) == null)
		// XXX Is this right?
		if(c.get().getText().length() <= c.getOffs())
		    return false;
	    return true;
	}

	public boolean move(int nsteps, int lookingDir) {
	    if(!Stream.move(c, dim, nsteps, lookingDir)) return false;
	    if(c.get().s(dim) == null)
		if(c.get().getText().length() <= c.getOffs())
		    return false;
	    return true;
	}

	public void start() {
	    c.set(c.get().h(dim));
	    c.setOffs(0);
	}

	public void end() {
	    ZZCell cl = c.get().h(dim, 1);
	    c.set(cl);
	    c.setOffs(cl.getText().length());
	}

	public void end(int dir) {
	    if(dir < 0) start(); else end();
	}

	public boolean find(int dir, boolean includeThis, String text) {
	    return Stream.find(c, dim, dir, includeThis, text);
	}

	/** Get the current character.
	 * Returns -1 for the end-of-stream.
	 */
	public char getChar() {
	    return getChar(0);
	}

	public char getChar(int offs) {
	    ZZCursorVirtual c1 = new ZZCursorVirtual(c);
	    // Move even if zero, to get the right
	    // directional position.
	    if(!Stream.move(c1, dim, offs, 1)) return (char)-1;
	    String str = c1.get().getText();
	    if(c1.getOffs() >= str.length()) return (char)-1;
	    return str.charAt(c1.getOffs());
	}

	public String getString(int n, int dir) {
	    // SLOW!!!!
	    ZZCursorVirtual c1 = new ZZCursorVirtual(c);
	    if(dir < 0) {
		if(!Stream.move(c1, dim, -n)) return null;
	    }
	    return Stream.getString(c1, dim, n);
	}
    }

    static public Iterator getIterator(ZZCursor c, String dim) {
	return new Iterator(new ZZCursorVirtual(c), dim);
    }

}






