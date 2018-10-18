/*   
Nile1Text.java
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

/** A library of routines for operating on streams with Nile1 text model.
 * @see Nile1
 * @deprecated Use Nile2Unit
 */

public abstract class Nile1Text {
public static final String rcsid = "$Id: Nile1Text.java,v 1.9 2000/12/07 00:37:19 tjl Exp $";
    public static boolean dbg = true;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

	// Stream.Iterator iter = Stream.getIterator(start, "d.nile");

    static public int START = 1;
    static public int END = -1;
    static public int ANY = 0;

    static boolean isWordBreak(Stream.Iterator iter, int edge) {
	char cur = iter.getChar();
	if(cur == -1) return true;
	if(!iter.move(-1)) return true;
	char prev = iter.getChar();
	iter.move(1);
	return isWordBreak(prev, cur, edge);
    }

    static boolean isWordBreak(char prev, char cur, int edge) {
	boolean ret = isWordBreak0(prev, cur, edge);
	p("IsWordBreak: '"+prev+"' '"+cur+"' "+edge+ " RETURNING:   "+ret);
	return ret;
    }
    static boolean isWordBreak0(char prev, char cur, int edge) {
	boolean curlet = Character.isLetterOrDigit(cur);
	boolean curws = Character.isWhitespace(cur);
	boolean prevlet = Character.isLetterOrDigit(prev);
	boolean prevws = Character.isWhitespace(prev);

	if(!curlet && !curws) {
	    if(edge != END) return true; // punctuation -> break start
	    return prevlet || !prevws; // punct -> br end if prev ltr or punct
	} 
	if(prevlet && curlet) return false;
	if(prevlet && curws) return edge != START;
	// --> prevlet == false
	if(!prevws) { // previous was punctuation ->
	     if(edge != START) return true;
	     return curlet; // Starting edge only if this is a letter.
	}
	if(prevws && curws) return false;
	if(prevws && curlet) return edge != END;
	// shouldn't get here
	throw new ZZError("isWordBreak logic error!");
    }

    /** Find the nsteps:th next wordbreak.
     * Always alters iter.
     * <p>
     * Characters are split into three categories:
     * letter-or-digit, whitespace and other.
     * whitespace is always a word boundary (but there is no
     * word boundary between two whitespace characters).
     * The characters in the other category are words by themselves.
     * So basically there is a word break at character c, if
     * <ul>
     *  <li> c is the first character in the stream.
     *  <li> c is one beyond the last character in the stream
     *  <li> c is punctuation
     *  <li> c is of a different category (see above) than 
     *       the character just before it.
     * </ul>
     * @param nsteps The number of steps, a positive or negative number.
     * @param edge Which edge of the word do we want: START, END or ANY.
     */
    static public boolean wordEdge(Stream.Iterator iter, 
				boolean includeThis, int nsteps,
				int edge) {
	if(nsteps == 0) throw new ZZError("Can't step 0 words");
	int dir = (nsteps > 0 ? 1 : -1);
	for(; nsteps != 0; nsteps -= dir) {
	    if(!wordEdge0(iter, includeThis, dir, edge))
		return false;
	    includeThis = false;
	}
	return true;
    }
	    
    static private boolean wordEdge0(Stream.Iterator iter, 
				boolean includeThis, int dir,
				int edge) {
	char prev = 0;
	char cur = iter.getChar();

	if(dir > 0) {
	    if(includeThis) {
		if(!iter.move(-dir)) return true;
		prev = iter.getChar();
		iter.move(dir);
		if(isWordBreak(prev, cur, edge)) return true;
	    }
	    while(iter.move(dir)) {
		prev = cur;
		cur = iter.getChar();
		if(cur == -1) return true; // Ran into end of text.
		if(isWordBreak(prev, cur, edge)) return true;
	    }
	    return (edge != START);
	} else {
	    if(!includeThis) {
		if(!iter.move(dir)) return false;
		cur = iter.getChar();
	    }
	    while(iter.move(dir)) {
		prev = cur;
		cur = iter.getChar();
		if(isWordBreak(cur, prev, edge)) {
		    iter.move(-dir);
		    return true;
		}
	    }
	    return (edge != END);
	}
    }

    static public boolean sentenceEdge(Stream.Iterator iter,
			boolean includeThis, int nsteps, int edge) {
	if(nsteps == 0) throw new ZZError("Can't step 0 words");
	int dir = (nsteps > 0 ? 1 : -1);
	for(; nsteps != 0; nsteps -= dir) {
	    if(!sentenceEdge0(iter, includeThis, dir, edge))
		return false;
	    includeThis = false;
	}
	return true;
    } 

    static public boolean sentenceEdge0(Stream.Iterator iter,
			boolean includeThis, int dir, int edge) {
	if(edge == 0) 
	    throw new ZZError("Looking for either sentence edge not implemented");
	if(edge == START) {
	    // If we cannot move, we're at the beginning.
	    if(!iter.move(-1)) 
		if(dir < 0 || includeThis) return includeThis;
	    // Back out
	    iter.move(1);
	    // Back up two, one by one, and then search non-inclusive.
	    iter.move(-1);
	    iter.move(-1);
	    if(iter.find(dir, includeThis, "  ")) {
		iter.move(2);
		return true;
	    } else {
		// There is a start in the beginning, but not at the end.
		iter.end(dir);
		return (dir < 0);
	    }
	} else {
	    if(!iter.move(1)) return includeThis;
	    iter.move(-1);
	    // Here we just find because end is at the found text
	    if(iter.find(dir, includeThis, "  ")) 
		return true;
	    else {
		iter.end(dir);
		// There is an end in the end, but not at the beginning.
		return (dir > 0);
	    }
	}
    } 

    static boolean ws(char c) { return Character.isWhitespace(c); }
    static boolean punct(char c) { 
	return (!Character.isWhitespace(c)) && (!Character.isLetterOrDigit(c));
    }
    static boolean lett(char c) { return Character.isLetterOrDigit(c); }

    /** Adjust the locations to cut text from.
     * Basically, cut one or two spaces after the latter location,
     * and if none, and there is a single space preceding,
     * cut it along.
     * <p>
     * @param leaveout An output parameter: the values in the two-member
     *        		array are set to the number of characters
     *			to leave out of the cut text.
     *			An important meaning is given to negative
     * 			values: a negative value means "add this many
     *			spaces".
     */
    static public void adjustCut(Stream.Iterator iter1,
				 Stream.Iterator iter2,
				 int[] leaveout) {
	leaveout[0] = leaveout[1] = 0;
	// First off, do not adjust if one of them is not word end.
	if(!isWordBreak(iter1, START) || !(isWordBreak(iter2, END))) return;

	char c1 = iter2.getChar(0);
	char c2 = iter2.getChar(1);

	// If there are one or two spaces afterwards, cut them along.
	if(ws(c1)) {
	    iter2.move(1);
	    if(ws(c2))
		iter2.move(1);
	} else {
	    // If there is one character before...
	    char b1 = iter1.getChar(-1);
	    char b2 = iter1.getChar(-2);
	    if(ws(b1) && !ws(b2)) {
		// This is a tricky situation. What we have here is
		// cutting off in
		//    word WORD, word
		// without the comma.
		// The space before WORD needs to be removed.
		// This is what the leaveout means.
		//
		// Additionally, for pasting at the correct boundary
		// to work right, we need to add a space *after* this
		// word when pasting. See adjustPaste.
		iter1.move(-1);
		leaveout[0] = 1;
	    }
	}
    }

    /** The adjustment to a paste operation.
     * @param adjust Analogour to the leaveout parameter of 
     * 			adjustCut. Positive values mean
     *			leaving this many characters out
     *			and negative values mean
     *			adding that many spaces.
     */
    static public void adjustPaste(Stream.Iterator to,
				    Stream.Iterator start,
				    Stream.Iterator end,
				    int[] adjust) {
	adjust[0] = adjust[1] = 0;
	if(!isWordBreak(to, START)) return;

	// If to is at a letter-punct edge, make sure 
	// pasted text starts with space and does not end with one.
	// Otherwise, make sure it ends with space but doesn't start
	// with it.

	char c1 = to.getChar(-1);
	char c2 = to.getChar(0);
	if(lett(c1) && punct(c2)) {
	    // Now, start but don't end with space.
	    if(!ws(start.getChar(0))) adjust[0] = -1;
	    if(ws(end.getChar(-1))) adjust[1] = 1;
	} else {
	    if(ws(start.getChar(0))) adjust[0] = 1;
	    if(!ws(end.getChar(-1))) adjust[1] = -1;
	}
    }

    static public boolean paragraphEdge(Stream.Iterator iter,
	    boolean includeThis, int nsteps, int edge) {
	if(nsteps == 0) throw new ZZError("Can't step 0 paragraphs");
    	int dir = (nsteps > 0 ? 1 : -1);
	for(; nsteps != 0; nsteps -= dir) {
	    if(!paragraphEdge0(iter, includeThis, dir, edge))
		return false;
	    includeThis = false;
	}
	return true;
    } 

    static public boolean paragraphEdge0(Stream.Iterator iter,
	    boolean includeThis, int dir, int edge) {
	ZZCell c = iter.get().get();
	int offs = iter.get().getOffs();
	boolean beg = (offs == 0);
	boolean end = (offs == c.getText().length());
	if(edge == 0)
	    throw new ZZError("Can't look for paragraph end / begin");
	// XXX Should we set the end edge to point to negative
	// direction?
	// if(edge == START) {
	    if(dir > 0) {
		if(!beg || !includeThis) c = c.s("d.nile");
	    } else {
		if(beg && !includeThis) c = c.s("d.nile", -1);
	    }
	    // Now we can go on, using includeThis always.
	    if(c == null) return false;
	    c = Nile1Ops.findStruct(c, dir, true);
	    if(c != null) {
		iter.set(new ZZCursorVirtual(c, 0));
		if(edge == START) 
		    iter.move(0, 1);
		else 
		    iter.move(0, -1);
		return true;
	    } else {
		if(edge == END && dir > 0) {
		    iter.end(); return true;
		}
		return false;
	    }
	// }
    }


    public interface Edger {
	boolean edge(Stream.Iterator iter, boolean includeThis, int nsteps,
			int whichEdge);
    }

    static public class WordEdger implements Edger {
	public boolean edge(Stream.Iterator iter, boolean includeThis, 
		int nsteps, int whichEdge) {
	    return wordEdge(iter, includeThis, nsteps, whichEdge);
	}
    }

    static public class SentenceEdger implements Edger {
	public boolean edge(Stream.Iterator iter, boolean includeThis, 
		int nsteps, int whichEdge) {
	    return sentenceEdge(iter, includeThis, nsteps, whichEdge);
	}
    }

    static public class ParagraphEdger implements Edger {
	public boolean edge(Stream.Iterator iter, boolean includeThis, 
		int nsteps, int whichEdge) {
	    return paragraphEdge(iter, includeThis, nsteps, whichEdge);
	}
    }

}
