/*   
FTextBreaker.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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

/** A class doing linebreaking of FTexts with the stupid first match algorithm.
 * @see FText
 */

public class FirstMatchBreaker {
public static final String rcsid = "$Id$";
    public static boolean dbg = false;
    private static final void p(String s) { if(dbg) System.out.println(s); }
    private static final void pa(String s) { System.out.println(s); }

    static class Word {
	FText.Part p; int start, end;
	/** Is a line break enforced after this word? */
	boolean forced;
	FText.Part get() { return p.subpart(start, end); }
	int width() { return p.fm.stringWidth(p.s.substring(start, end)); }
	Word(FText.Part p, int s, int e, boolean f) { 
	    this.p=p; start=s; end=e; forced=f;
	}
    }

    /** Internal convenience: concatenate all adjacent words in a vector. */
    private static void concat(Vector v) {
	int i=0;
	while(i+1 < v.size()) {
	    Word w1 = (Word)v.elementAt(i), w2 = (Word)v.elementAt(i+1);
	    if(w1.p == w2.p && w1.end == w2.start) {
		w1.end = w2.end;
		v.removeElement(w2);
	    } else
		i++;
	}
    }

    /** Internal convenience: search a number of tokens in a string
     * Returns the next occurence of any of the tokens after the given 
     * position.
     */
    static int indOf(String s, int pos, char[] toks) {
	for(int i=pos+1; i<s.length(); i++) {
	    char c = s.charAt(i);
	    for(int j=0; j<toks.length; j++)
		if(toks[j] == c) return i;
	}
	return -1;
    }

    /** Prepare an FText for the linebreaker (find possible breaks).
     * Returns a Vector of Word objects.
     */
    public static Vector prepare(FText txt) {
	final char[] delims = new char[] { ' ', '\n' };
	
	if(txt.parts.length == 0)
	    throw new ZZError("FText has length 0");
	Vector v = new Vector();
	p("prepare: Old FText (len "+txt.parts.length+"): "+txt.get());
	
	for(int i=0; i<txt.parts.length; i++) {
	    FText.Part p = txt.parts[i];
	    String s = p.s;
	    int here, last = -1;
	    while((here = indOf(s, last, delims)) >= 0) {
		char c = s.charAt(here);
		v.addElement(new Word(p, last+1, here+1, c == '\n'));
		last = here;
	    }
	    v.addElement(new Word(p, last+1, s.length(), false));
	}

	return v;
    }

    /** Internal convenience: Vector to array. */
    private static FText.Part[] v2a(Vector v) {
	concat(v);
	FText.Part[] res = new FText.Part[v.size()];
	for(int i=0; i<res.length; i++)
	    res[i] = ((Word)v.elementAt(i)).get();
	return res;
    }

    public static FText.Part[][] stupidBreak(FText ftxt, int width) {
	Vector prep = prepare(ftxt); int psize = prep.size();
	Vector lineset = new Vector(), line = new Vector();
	int x = 0, linestart = 0;
	p("FTextBreaker.stupidBreak...");
	for(int i=0; i<psize; i++) {
	    Word w = (Word)prep.elementAt(i);
	    x += w.width();
	    if(!w.forced && x <= width) {
		line.addElement(w);
		continue;
	    }
	    if(i > linestart && x > width) {
		lineset.addElement(v2a(line));
		line.removeAllElements();
		x = w.width(); linestart = i;
	    }
	    line.addElement(w);
	    if(x > width || w.forced) {
		/* w alone doesn't fit on our line, or forced break after w */
		lineset.addElement(v2a(line));
		line.removeAllElements();
		x = 0; linestart = i+1;
	    }
	}
	if(line.size() > 0) lineset.addElement(v2a(line));
	FText.Part[][] res = new FText.Part[lineset.size()][];
	for(int j=0; j<res.length; j++) {
	    res[j] = (FText.Part[])lineset.elementAt(j);
	    if(dbg) pa("	"+(j+1)+". "+new FText(res[j]).get());
	}
	return res;
    }
}
