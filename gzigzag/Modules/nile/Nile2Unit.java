/*   
Nile2Unit.java
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

/** A nile unit of division.
 */

public abstract class Nile2Unit {
public static final String rcsid = "$Id: Nile2Unit.java,v 1.5 2000/12/17 16:32:24 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }


    /** The previous or next position where
     * this type of unit may be inserted.
     */
    public abstract boolean insertPos(Nile2Iter iter, int dir, 
			boolean includeThis);

    /** The previous or next beginning position of this type of
     * unit.
     */
    public abstract boolean start(Nile2Iter iter, int dir, boolean includeThis);

    /** The previous or next ending position of this type of unit.
     */
    public abstract boolean end(Nile2Iter iter, int dir, boolean includeThis);

    /** Cut the stream.
     * This routine modifies the start and to iterators to point
     * to the beginning and end of the modified stream.
     * @return The pasted-together cutaway point, from which 
     *		the insert position can be searched with !includeThis.
     * 	       
     */
    public abstract Nile2Iter cut(Nile2Iter start, Nile2Iter to);

    /** Copy a part of the stream.
     * First every cell is spanified, then a new stream with the same spans
     * is created.
     * This routine modifies the start and to iterators to point
     * to the beginning and end of the copied stream.
     * XXX any special copying rules to obey, like in cut?
     */
    public void copy(Nile2Iter start, Nile2Iter to) {
	this.start(start, -1, true);
	this.end(to, 1, true);
	Nile2Iter.copy(start, to);
    }

    public abstract void paste(Nile2Iter at, Nile2Iter what, Nile2Iter whatEnd);


    static public class Char extends Nile2Unit {
	public boolean insertPos(Nile2Iter iter, int dir, 
			    boolean includeThis) {
	    if(!includeThis)
		if(!iter.move(dir)) 
		    if(!iter.shiftPara(dir))
			return false;
	    return true;
	}
	public boolean start(Nile2Iter iter, int dir, 
		    boolean includeThis) {
	    if(!includeThis)
		if(!iter.move(dir)) 
		    if(!iter.shiftPara(dir))
			return false;
	    if(iter.get() == -1) return false;
	    return true;
	}

	public boolean end(Nile2Iter iter, int dir, 
		    boolean includeThis) {
	    if(!includeThis)
		if(!iter.move(dir)) 
		    if(!iter.shiftPara(dir))
			return false;
	    if(iter.get(-1) == -1) return false;
	    return true;
	}

	public Nile2Iter cut(Nile2Iter start, Nile2Iter to) {
	    Nile2Iter after = to.cut();
	    start.cut();
	    start.join(after);
	    return start;
	}

	public void paste(Nile2Iter at, Nile2Iter what, Nile2Iter whatEnd) {
	}
    }

    
    /** The word and sentence base class.
     * Words and sentences obey the same rules for the insert point.
     */
    static public abstract class WordOrsentence extends Nile2Unit {
	public abstract boolean isStart(Nile2Iter iter);
	public abstract boolean isEnd(Nile2Iter iter);
	public abstract int nspacesAtEnd();

	// As documented,
	// word insert points are word starting points, as well as
	// paragraph's ending points.
	public boolean insertPos(Nile2Iter iter, int dir, 
				boolean includeThis) {
	    return startOrIns(iter, dir, includeThis, true);
	}

	public boolean start(Nile2Iter iter, int dir, boolean includeThis) {
	    return startOrIns(iter, dir, includeThis, false);
	}

	public boolean startOrIns(Nile2Iter iter, int dir, boolean includeThis,
		boolean insert) {
	    // This way we can always assume includeThis later.
	    if(!includeThis)
		if(!iter.move(dir)) {
		    if(!iter.shiftPara(dir))
			return false;
		    // If we were looking for an insert point, we just
		    // found it...
		    if(dir < 0 && insert) return true;
		}
	    // Can optimize if necessary...
	    while(true) {
		if(isStart(iter)) return true;
		if(!iter.move(dir)) {
		    if(dir > 0 && insert) return true;
		    if(!iter.shiftPara(dir)) return false;
		    if(dir < 0 && insert) return true;
		}

	    }
	}

	public boolean end(Nile2Iter iter, int dir, boolean includeThis) {
	    if(!includeThis)
		if(!iter.move(dir)) {
		    if(!iter.shiftPara(dir)) return false;
		    // In forwards dir, just go to the beginning
		    // of the next paragraph.
		    // This way, cutting from the last insertpos until
		    // end() results in just the paragraph break being
		    // removed, which is just right.
		    return true;
		}
	    while(true) {
		if(isEnd(iter)) return true;
		if(!iter.move(dir)) 
		    if(!iter.shiftPara(dir))
			return false;
	    }
	}

	public Nile2Iter cut(Nile2Iter start, Nile2Iter end) {
	    // Find first non-space character.
	    int nws = this.firstNonWs(start);
	    this.p("Cut Firstnonws: "+nws + " '"+(char)nws+"'");
	    boolean startPunct = this.punct(nws);

	    // cut all the space after end.
	    int ncut = 0;
	    Nile2Iter endclone = (Nile2Iter)end.clone();
	    while(this.ws(end.get())) { end.move(1); ncut ++; }
	    // If we happen to have space before us, count that too!
	    int nspace = 0;
	    while(this.ws(endclone.prev())) nspace++;

	    Nile2Iter rstart = null; 
	    if(ncut + nspace == 0 || startPunct) {
		// If no space at end,
		// we must cut space from before this, if it
		// is a single space.
		if(this.ws(start.get(-1)) && !this.ws(start.get(-2)))
		    (rstart = (Nile2Iter)start.clone()). move(-1);
		else
		    if(this.ws(start.get(-1)) && startPunct) {
			(rstart = (Nile2Iter)start.clone()). move(-1);
			// For the if down later
			ncut = 1;
		    }
	    }
	    // Now, here's the cut. We take away the stuff from
	    // start to end and then remove from rstart
	    // We must cut starting from the end so as not to disturb
	    // things.
	    Nile2Iter after = end.cut();
	    Nile2Iter first = start.cut();
	    Nile2Iter before = (Nile2Iter)start.clone();
	    start.set(first); // We want to return this in start.
	    // Finally, cut the other stuff.
	    if(rstart != null) {
		rstart.cut();
		before = rstart;
	    }
	    before.join(after);
	    // This is a bit special rule.
	    if(startPunct && ncut + nspace > 0)
		before.insertInCell(" ");
	    return before;
	}

	public void paste(Nile2Iter at, Nile2Iter what, Nile2Iter whatEnd) {
	    // Check whether what has punctuation in the start...
	    int nws = this.firstNonWs(what);
	    this.p("Paste Firstnonws: "+nws + " '"+(char)nws+"'");
	    boolean startPunct = this.punct(nws);
	    Nile2Iter after = null;

	    if(dbg) {
		this.p("PASTE!!!");
		at.dumpStream();
		what.dumpStream();
	    }


	    if(startPunct) {
		// If there is punctuation, we must start it with
		// exactly the spaces that were there, UNLESS
		// we are at the beginning of the sentence, in which
		// case we shall have two spaces.

		if(this.ws(at.get(-1)) && this.ws(at.get(-2))) {
		    // Ok, now it's difficult. 
		    // Leave these spaces, but remove one in the 
		    // beginning of 
		    what.adjustSpaceStart(0);
		    after = at.cut();
		} else {
		    if(this.ws(at.get(-1))) {
			at.move(-1);
			after = at.cut();
			after.move(1);
			after = after.cut();
		    } else {
			after = at.cut();
		    }
		}
	    } else {
		// pasting words is a bit tricky.
		int prev = at.get(-1);
		int cur = at.get();
		// Where do we want to ensure we have space.
		boolean spaceEnd = false, spaceStart = false;
		// If we are at start of paragraph, we ensure that
		// a space is in the end.
		if(prev == -1) spaceEnd = true;
		else if(this.punct(cur)) {
		    // If we are at punctuation, we put a space after
		    // inserted text is there was a space before.
		    if(this.ws(prev)) spaceEnd = true;
		    else spaceStart = true;
		}
		else spaceEnd = true;

		what.adjustSpaces(spaceStart ? 1 : 0,
				  spaceEnd ? nspacesAtEnd() : 0);
				  

		after = at.cut();
	    }
	    at.join(what);
	    whatEnd.set(what);
	    whatEnd.streamEnd(1);
	    whatEnd.join(after);
	    whatEnd.set(after);
	}
    }

    static public class Word extends WordOrsentence {
	public boolean isStart(Nile2Iter iter) { return this.isWordStart(iter); }
	public boolean isEnd(Nile2Iter iter) { return this.isWordEnd(iter); }
	public int nspacesAtEnd() { return 1; }
    }

    static public class Sentence extends WordOrsentence {
	public boolean isStart(Nile2Iter iter) { return this.isSentenceStart(iter); }
	public boolean isEnd(Nile2Iter iter) { return this.isSentenceEnd(iter); }
	public int nspacesAtEnd() { return 2; }
    }

    static public class Paragraph extends Nile2Unit {
	// A paragraph may be inserted at the end of any paragraph,
	// or in the very beginning of the stream.
	// That requires special handling because
	// Nile2Iter can't get there.
	public boolean insertPos(Nile2Iter iter, int dir, 
				boolean includeThis) {
	    // XXX First, check if we *are* there already.

	    if(!end(iter, dir, includeThis)) {
		if(dir > 0) return false;
		ZZCursor curs = iter.getCursor(1);
		curs.set(curs.get().h("d.nile"));
		curs.setOffs(0);
		iter.set(curs);
	    }
	    return true;
	}
	public boolean start(Nile2Iter iter, int dir, boolean includeThis) {
	    if(!includeThis) {
		if(!iter.move(dir)) {
		    // We were at the beginning
		    if(!iter.shiftPara(dir)) 
			return false;
		}
	    }
	    return iter.paraEnd(-1);
	}
	public boolean end(Nile2Iter iter, int dir, boolean includeThis) {
	    if(!includeThis) {
		if(!iter.move(dir)) {
		    // We were at the beginning
		    if(!iter.shiftPara(dir)) 
			return false;
		}
	    }
	    return iter.paraEnd(1);
	}

	public Nile2Iter cut(Nile2Iter start, Nile2Iter end) {
	    return null;
	}
	public void paste(Nile2Iter at, Nile2Iter what, Nile2Iter whatEnd) {
	}
    }

    // Something non-ws following to ws's.
    static public boolean isSentenceStart(Nile2Iter iter) {
	int cur = iter.get();
	if(cur == -1 || ws(cur)) return false;
	int prev = iter.get(-1);
	if(prev == -1) return true;
	if(!ws(prev)) return false;
	int prev2 = iter.get(-2);
	if(prev2 == -1) return true;
	if(!ws(prev2)) return false;
	return true;
    }

    // Something non-ws following to ws's.
    static public boolean isSentenceEnd(Nile2Iter iter) {
	int prev = iter.get(-1);
	if(prev == -1 || ws(prev)) return false;
	int cur = iter.get();
	if(cur == -1) return true;
	if(!ws(cur)) return false;
	int next = iter.get(1);
	if(next == -1) return true;
	if(!ws(next)) return false;
	return true;
    }

    static public boolean isWordStart(Nile2Iter iter) {
	int cur = iter.get();
	if(punct(cur)) {
/*	    if(lett(punct.get(-1)) && lett(punct.get(1))) {
		// Punctuation surrounded by two letters is NOT
		// a word boundary.
		return false;
	    } else  XXX Must test in other cases!!! */

		return true;
	} 

	int prev = iter.get(-1);
	// This should also cover buffer start.
	if(lett(cur) && !lett(prev)) return true;
	return false;
    }
    static public boolean isWordEnd(Nile2Iter iter) {
	int prev = iter.get(-1);
	if(punct(prev)) 
/*
	    if(lett(punct.get(-2)) && lett(punct.get(0))) {
		// Punctuation surrounded by two letters is NOT
		// a word boundary.
		return false;
	    } else XXX Must test!!!! */
		return true;

	int cur = iter.get();
	if(lett(prev) && !lett(cur)) return true;
	return false;
    }

    // Shorthand.
    // Exactly one of these must return true for every character.
    // except -1, when all are false.
    static boolean ws(int c) { return Character.isWhitespace((char)c); }
    static boolean punct(int c) { 
	if(c == -1) return false;
	return (!Character.isWhitespace((char)c)) && 
	(!Character.isLetterOrDigit((char)c));
    }
    static boolean lett(int c) { return Character.isLetterOrDigit((char)c); }

    static int firstNonWs(Nile2Iter iter) {
	int ch = 0;
	// Use index, don't move.
	for(int i=0; (ch=iter.get(i)) != -1; i++) {
	    if(!ws(ch)) return ch;
	}
	return -1;
    }

}
