/*   
ZaubertrankTemplate.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2001, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.clang;
import org.gzigzag.*;
import java.awt.*;

/** A module interface to the Archimedes Procedural Layer executor/evaluator.
 */

public class ZaubertrankTemplate {
String rcsid = "$Id: ZaubertrankTemplate.java,v 1.7 2001/04/23 21:45:10 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    /** On the zaubertrank text rank, find a clone of the given parameter.
     *  This is used to obtain the cell on the zaubertrank text rank
     *  corresponding to a given root template parameter cell.
     */
    static public ZZCell findParam(ZZCell root, ZZCell par) {
	return root.intersect("d.zt-text", 1, par.getRootclone(), "d.clone", 1);
    }

    /** Move to the next/previous parameter cell on the zaubertrank text rank.
     *  A parameter cell is one that does not contain text directly, but
     *  rather refers to one of the callables parameters (cloning a cell from
     *  the root template). Given a cell on a zaubertrank text rank, we move
     *  poswards or negwards until we find a parameter cell.
     *  <p>
     *  Note: Parameter cells are distinguished from text cells by parameter
     *  cells being clones and text cells being not.
     *  @returns <code>null</code> if no parameter cell was found in that
     *           direction.
     */
    static public ZZCell nextParam(ZZCell par, int dir) {
	ZZCell c = par;
	
	for(c = c.s("d.zt-text", dir); true; c = c.s("d.zt-text", dir)) {
	    if(c == null) return null;
	    if(c.equals(par)) return null; // circular rank!
	    if(!c.equals(c.getRootclone())) break;
	}
	
	return c;
    }

    /** Return the first or last zaubertrank param cell attached to root.
     *  Whether the first or last param is returned depends on dir. This
     *  is used to move "into" an expression, so that if you know the
     *  expression, you can go to its first/last parameter.
     */
    static public ZZCell lastParam(ZZCell root, int dir) {
	ZZCell c = root.h("d.zt-text", dir);
	if(dir > 0 && !c.equals(c.getRootclone())) return c;
	return nextParam(c, -dir);
    }

    /** A static class representing a virtual cursor into the tree structure.
     *  At every time, this accurses a parameter cell of an expression.
     *  If you have a tree: A(B(C, D), E(F)), and start accursing A, then moving
     *  poswards will give you in order: B, C, C, D, D, B, E, F, F, E, A. Note
     *  that you traverse each cell twice (A the first time when you set the
     *  cursor to it).
     *  <p>
     *  Note that the "more intuitive" structure C, D, B, F, E, A (RPN) can be
     *  build on top of this, by writing a wrapper.
     */
    static public final class TreeCursor {
	public VirtualEditCursor cur;
	public Expression e; // the expression; may be null
	public ZZCell par; // the parameter of the expression we're at
	public int side = -1;
	
	public TreeCursor(VirtualEditCursor cur) {
	    this.cur = cur;
	    read();
	}

	/** Read the current state in from the virtual edit cursor. */
	public void read() {
	    par = cur.get();
	    try {
		e = new Expression(cur.peek().h("d.expression"));
	    } catch(java.util.EmptyStackException exc) {
		e = null;
	    }
	}
	
	/** Move in.
	 *  @param side Start at first (-1) or last (+1) parameter?
	 */
	public ZZCell moveIn(int side) {
		ZZCell ehead = par.h("d.expression", true), zpar = null;
		if(ehead != null)
		    zpar = lastParam(ehead.getRootclone(), side);
		    // zpar can be null now if the subexpression has no params
		if(zpar != null) {
		    // move in
		    e = new Expression(ehead);
		    ZZCell rootpar = zpar.getRootclone();
		    par = ParamTemplate.rootToExpression(rootpar, e);
		    cur.push(par);
		    side = side;
		    return par;
		} else
		    return null;
	}
	
	public ZZCell moveOut() {
	    if(e == null)
		// we are at the root element of the tree
		return null;
		
	    // move out: that's much nicer than before virtual edit cursors ;)
	    cur.pop();
	    read();
	    return par;
	}
	
	/** Move left/right. */
	public ZZCell moveSidewards(int dir) {
		final int fwd = dir, back = -dir;
		if(e == null)
		    // we are at the root element of the tree
		    return null;
		ZZCell rootpar = ParamTemplate.expressionToRoot(par, e);
		ZZCell zpar = findParam(e.main.getRootclone(), rootpar);
		zpar = nextParam(zpar, fwd);
		if(zpar != null) {
		    // move left/right
		    rootpar = zpar.getRootclone();
		    par = ParamTemplate.rootToExpression(rootpar, e);
		    cur.set(par);
		    side = back;
		    return par;
		} else
		    return null;
	}
	
	/** Move in the specified direction and return the cell we reach.
	 *  If we hit the end, return null and <em>do not change</em> the
	 *  state of the tree cursor.
	 */
	public ZZCell move(int dir) {
	    // This method works symmetrically, thus instead of thinking about
	    // posward and negward, we think about "forward" and "backward".
	    // Forward is the direction we're moving to.
	    final int fwd = dir < 0 ? -1 : 1;  // forward direction
	    final int back = -fwd;             // backward direction

	    // if we're at the back, move into this branch; if we are at
	    // the front, move to the next branch
	    if(side == back) {
		// move in, or change sides if that's not possible
		if(moveIn(back) == null)
		    // change sides
		    side = fwd;
	    } else {
		// move left/right, or move out if that's not possible
		if(moveSidewards(fwd) == null)
		    if(moveOut() == null)
			return null;
	    }
	    return par;
	}

	/** Move to the next position whose side is negward.
	 *  With this method, you can traverse the tree visiting each element
	 *  only once, i.e. A(B(C, D), E(F)) ==> A, B, C, D, E, F. It simply
	 *  applies move() until it gets a negative side or the end of the
	 *  traversal stream; in the latter case, it returns null.
	 *  XXX take this out again if it proves not to be used
	 */
	public ZZCell moveToNeg(int dir) {
	    ZZCell c = move(dir);
	    LoopDetector d = new LoopDetector();
	    while(side > 0) {
		if(c == null) return null;
		d.detect(c);
		c = move(dir);
	    }
	    return c;
	}

	/** Move in the given direction, until an undefined parameter is found.
	 *  If there is no undefined parameter, move as far as possible and
	 *  return null.
	 *  @returns The undefined parameter, or null when the end of the
	 *           stream is reached.
	 */
	public ZZCell moveToNextUndefined(int dir) {
	    ZZCell c = move(dir);
	    /** Two loop detectors so that seeing a cell from both sides 
	     *  doesn't trigger an InfiniteLoopException. */
	    LoopDetector dp = new LoopDetector(), dn = new LoopDetector();
	    while(c != null) {
		if(side > 0) dp.detect(c);
		else dn.detect(c);
		if(c.t().equals("") && c.s("d.expression", -1) == null)
		    return c;
		c = move(dir);
	    }
	    return null;  // nothing found, end of stream: move() returned null
	}
    }
}
