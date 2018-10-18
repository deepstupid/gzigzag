/*   
Nile3Unit.java
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
 * Written by Benjamin Fallenstein, based on Nile2Unit by Tuomas Lukka
 */

package org.gzigzag.module;
import org.gzigzag.*;

/** A nile unit of division.
 * Spaces between units may be non-zero-width. Units may be zero-width, if
 * neighbouring units are separated by non-zero spaces.
 */

public abstract class Nile3Unit {
public static final String rcsid = "$Id: Nile3Unit.java,v 1.2 2001/04/05 13:27:30 tjl Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }


    public static class Char extends Nile3Unit {
	public boolean isEdge(Nile3Pos pos, int dir) {
	    return !pos.boundary(dir);
	}
    }

    public static class Word extends Nile3Unit {
	/** An alphanumeric word. */
	static final int AWORD = 1;
	/** A punctuation word, e.g. a smilie. */
	static final int PWORD = 2;
	/** An A- or PWORD. */
	static final int WORD = AWORD | PWORD;
	/** A piece of left-bound punctuation, like commas. */
	static final int LPUNCT = 4;
	/** A piece of right-bound punctuation, like opening brackets. */
	static final int RPUNCT = 8;
	/** A piece of left- or right-bound punctuation. */
	static final int BPUNCT = LPUNCT | RPUNCT;
	/** A paragraph boundary or the end of the stream. */
	static final int BOUNDARY = 16;
	
	/** Not a correct combination of words. */
	static final int BAD = 0;
	/** Correct combination iff no space in between. */
	static final int NSP = 1;
	/** Correct combination iff space in between. */
	static final int SP = 2;
	
	/** The type of a joining position. */
	public static final int jointype(int lefttype, int righttype) {
	    if(lefttype & WORD > 0 && righttype & WORD > 0)
		return SP;
	    else if(lefttype == AWORD && righttype == LPUNCT)
		return NSP;
	    else if(lefttype == AWORD && righttype == RPUNCT)
		return SP;
	    else if(lefttype == RPUNCT && righttype == AWORD)
		return NSP;
	    else if(lefttype == LPUNCT && righttype == AWORD)
		return SP;
	    else if(lefttype & BPUNCT > 0 && righttype == lefttype)
		return NSP;
	    else if(lefttype == LPUNCT && righttype == RPUNCT)
		return SP;
	    else if(lefttype == BOUNDARY && righttype == BOUNDARY)
		return NSP;
	    else if(lefttype == BOUNDARY && righttype & WORD > 0)
		return NSP;
	    else if(lefttype & WORD > 0 && righttype == BOUNDARY)
		return NSP;
	    else if(lefttype == LPUNCT && righttype == BOUNDARY)
		return NSP;
	    else if(lefttype == BOUNDARY && righttype == RPUNCT)
		return NSP;
	    else
		return BAD;
	}
	
	/** Find out the types of the words next to a word boundary. 
	 * This may only be called on what is known to be a word boundary.
	 * Else, its behavior is undefined.
	 */
	public static final int[] wordtype(Nile3Pos pos, int dir) {
	    int[] res = new int[2];
			
	    int lch = pos.type(-1), rch = pos.type(1);
	    boolean lp = pos.punct(-1), rp = pos.punct(1);
	    Nile3Pos lpos, rpos;
	    if(lp) { 
		lpos = (Nile3Pos)pos.clone(); 
		while(lpos.punct(-1)) lpos.move(-1);
	    }
	    if(rp) { 
		rpos = (Nile3Pos)pos.clone(); 
		while(rpos.punct(1)) rpos.move(1);
	    }
	
	/*
	    if(!lp && !rp) {
		if(pos.
	*/
	}
	
	public void cleanJoin(Nile3Pos pos, int lefttype, int righttype) {
	    int type = jointype(lefttype, righttype);
	    if(jointype == NSP) {
		while(pos.ws(-1)) pos.delete(-1);
		while(pos.ws(1)) pos.delete(1);
	    } else if(jointype == SP) {
		if(!pos.ws(-1) && !pos.ws(1)) pos.insert(" ", 1);
	    } else {
		p("Nile3Unit.Word: Detected bad joining in cleanJoin");
	    }
	}
		
        public boolean isEdge(Nile3Pos pos, int dir) {
	    if(pos.punct(-dir)) return true;
	    if(pos.bws(dir) && pos.alpha(-dir)) return true;
	    return false;
        }

	/** Can what be inserted at pos? */
	/*
	public boolean isInsertPos(Nile3Pos pos, Nile3Part what) {
	    if(!pos.boundary(1) && !isEdge(pos, -1)) return false;
	    if(what.start.punct(1) && !what.start.bws(-1)) {
	    }
	    if(what.end.punct(-1) && !what.end.bws(1)) {
		if(
	    }
	    if(jointype(pos.-1, what.start.1) == BAD) return false;
	    if(jointype(pos.1, what.end.-1) == BAD) return false;
	}
	*/
    }

    public static class Sentence extends Nile3Unit {
        public boolean isEdge(Nile3Pos pos, int dir) {
	    if(pos.ws(dir)) return false;
	    if(pos.boundary(-dir)) return true;
	    if(!pos.move(-dir)) return false;
	    if((pos.bws(-dir)) && pos.ws(dir)) return true;
	    pos.move(dir);
	    return false;
        }
    }

    // Ugh! Paragraph is *inefficient*! 
    // XXX is this a problem? in very long paragraphs maybe?
    public static class Paragraph extends Nile3Unit {
        public boolean isEdge(Nile3Pos pos, int dir) {
	    return pos.boundary(-dir);
        }
    }


    public boolean step(Nile3Part part, int n) {
	int abs = (n < 0) ? (-n) : (n);
	int dir = (n < 0) ? (-1) : (1);
	Nile3Pos pos = part.side(dir), other = part.side(-dir);
	for(int i=0; i<abs; i++) {
	    other.set(pos);
	    if(!edge(other, -dir, dir, true)) return false;
	    pos.set(other);
	    if(!edge(pos, dir, dir, false)) return false;
	}
	return true;
    }

    public boolean adjust(Nile3Part part, int side, int n) {
	int abs = (n < 0) ? (-n) : (n);
	int dir = (n < 0) ? (-1) : (1);
	for(int i=0; i<abs; i++) {
	    if(!edge(part.side(side), side, dir, false)) return false;
	}
	return true;
    }

    public Nile3Part extend(Nile3Pos pos) {
	Nile3Part res = new Nile3Part(pos, pos);
	if(!extend(res)) return null;
	return res;
    }

    public boolean extend(Nile3Part part) {
	if(!edge(part.start, -1, -1, true)) return false;
	if(!edge(part.end, 1, 1, true)) return false;
	return true;
    }

    public boolean delete(Nile3Part part) {
	cleanUp(part.delete());
    }

    protected void cleanUp(Nile3Pos pos) {}
    
    /** Move a Nile3Part to a position. */
    public void move(Nile3Part what, Nile3Pos where, int moveDir, 
		     boolean includeThis, boolean copy) {
	// First, prepare the part.
	if(!extend(what)) return null;
	if(copy) what = what.copy();
	else rmSpace(what.cut());
	
	// Then, prepare the pos.
	if(!insertPos(pos, moveDir, what, includeThis)) return null;
	
	// Finally, move.
	Nile3Pos res = paste(what, where);
	rmSpace(res);
	return res;
    }


    /** The previous or next position where this type of unit may be inserted.
     * By standard, return edge(pos, -1, dir, includeThis).
     */
    protected boolean insertPos(Nile3Pos pos, int dir, Nile3Part what,
			        boolean includeThis) {
	if(!pos.move(!includeThis, moveDir)) return false;
	do {
	    if(pos.boundary(1) || isEdge(pos, -1)) return true;
	} while(pos.move(moveDir));
	return false;
    }

    /** Is this an edge of this type of unit in the given direction? 
     * Positive direction means there follows such a unit after this point;
     * negative direction menas this point is preceded by such a unit.
     */
    protected abstract boolean isEdge(Nile3Pos pos, int dir);

    public boolean edge(Nile3Pos pos, int edgeDir, int moveDir, 
			boolean includeThis) {
	if(!pos.move(!includeThis, moveDir)) return false;
	do {
	    if(isEdge(pos, edgeDir)) return true;
	} while(pos.move(moveDir));
	return false;
    }

    /** The previous or next beginning position of this type of unit.
     * @returns false if we bump into end of string.
     */
    public boolean start(Nile3Pos pos, int dir, boolean includeThis) {
	return edge(pos, -1, dir, includeThis);
    }

    /** The previous or next ending position of this type of unit.
     * @returns false if we bump into end of string.
     */
    public boolean end(Nile3Pos pos, int dir, boolean includeThis) {
	return edge(pos, 1, dir, includeThis);
    }

    /** Modify the selection so that it makes sense to the user. */
    public boolean select(Nile3Part part, boolean extend) {
	boolean res = start(part.start, extend ? -1 : 1, true);
	res = res && end(part.end, extend ? 1 : -1, true);
	return res; 
    }

    /** Modify the selection for moving it around (cut, copy) */
    public boolean prepare(Nile3Part part) {
	return true;
    }

    /** Cut the stream.
     * This routine modifies the Nile3Part to point
     * to the cut stream, and returns the cut position.
     * @return The pasted-together cutaway point, from which 
     *		the insert position can be searched with !includeThis.
     */
    public Nile3Pos cut(Nile3Part part) { return part.cut(); }

    /** Copy a part of the stream.
     * First every cell is spanified, then a new stream with the same spans
     * is created.
     * This routine modifies the start and to iterators to point
     * to the beginning and end of the copied stream.
     * XXX any special copying rules to obey, like in cut?
     */
    public Nile3Part copy(Nile3Part what) {
	this.start(what.start, -1, true);
	this.end(what.end, 1, true);
	return what.copy();
    }

    protected void paste(Nile3Pos at, Nile3Part what) { at.insert(what, 1); }
}
