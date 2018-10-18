/*   
Cursor.java
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
package org.gzigzag.impl;
import org.gzigzag.*;

/** Cursors implemented in the zigzag structure.
 * Cursors are the ZigZag equivalent to pointers. A cursor points to a cell.
 * Starting from Benja's text vstream design, cursors are slightly different:
 * they can point either to a given character in a given cell or simply to a given 
 * cell.
 * <p>
 * Cursors operate on several different dimensions.
 * <p>
 * This class encapsulates also the as-yet-not-completely-made decision
 * of how a cursor after the last character in a cell should be represented.
 * This is a tricky case, since all the options have good and bad sides.
 * <ul>
 * 	<li> A sentinel cell, cell$end or cell$-1 at the very end.
 * 	     this is relatively clean, but making connects fail
 * 	     after that cell is difficult, as is creating a new rank
 * 	     and putting it in a cell. This moves the bookkeeping into
 * 	     the actual operations that do something to streams' ends.
 * 	<li> (The currently used method)
 * 		Equating a cursor after the end of
 * 		the stream as being at the beginning of the first cell
 * 		(of whose stream this is, and thus doesn't have a character
 * 		 in it).
 * 		Good: this works correctly
 * 		Bad: it's ugly as hell and motion commands are now the
 * 		place where the complexity ends up in being at.
 *       <li> (Benja:) Not storing cursors that <em>stay at</em> the end of
 *            the stream, and generally not allowing cursors with a posward
 *            bias. Thinking more about this, it's not clear to me for what
 *            we would need them, since we do not store markup with cursors:
 *            we have cells in the stream that denote beginning and end of
 *            markup. If nobody sees an important use for them, we could
 *            get rid of 'em and only allow cursors with a negward bias, i.e.
 *            cursors that always stay <em>after</em> a specific character.
 *            Then we can use the code we have already and only need to remove
 *            some cases from it. (Note: this doesn't interfere with
 *            multidirectional text: for right-to-left, for example, "negward"
 *            means "to the right.") When inserting, only the cursor we are
 *            inserting at would be moved.
 * </ul>
 * <p>
 * The encapsulation of this feature is through
 * <ul>
 *  	<li> VStreamDim.iterate() where the extra parameter contains the cursor
 * 		cell used to show the cursor offset. The VStream dimension
 * 		will then work with Cursor to do the right thing.
 * 	<li> Cursor.getVStreamCellBefore() which returns the cell right before
 * 		the current cursor. This routine does the right thing for
 * 		cursors at the end of the line, as well as for cursors
 * 		at the beginning.
 * 	<li> Cursor.setVStreamCellBefore()
 * </ul>
 * 2)
 */

public class Cursor {
public static final String rcsid = "$Id: Cursor.java,v 1.16 2001/10/18 00:07:49 tjl Exp $";

    /** Get the current cell the given cursor is at.
     * If this cursor is positioned on a character, the cell containing
     * the vstream (its headcell on the vstream dimension) is returned.
     * On the other hand, if this cursor is positioned on a cell,
     * that cell is returned.
     */
    public static Cell get(Cell cursor) {
	if(getSide(cursor) == 0)
	    return cursor.h(Dims.d_cursor_cargo_id).h(Dims.d_cursor_list_id).
		s(Dims.d_cursor_id, -1);
	else
	    return getPosition(cursor).h(Dims.d_vstream_id);
    }

    /** The inverse to <code>setCargo</code>.
     */
    public static Cell getCargo(Cell cargo) {
	return cargo.h(Dims.d_cursor_cargo_id, true);
    }

    public static void setCargo(Cell cargo, Cell cursor) {
	cargo.excise(Dims.d_cursor_cargo_id);
	set(cargo, null);
	if(cursor != null)
	    try {
		cursor.insert(Dims.d_cursor_cargo_id, 1, cargo);
	    } catch(ZZAlreadyConnectedException e) {
		throw new ZZError("Something REALLY weird happened." + e);
	    }
    }

    /** Make the given cursor point to the given cell.
     * After a call to this function, the cursor points to the given cell,
     * no character.
     * @param cursor The cursor cell
     * @param to The cell that the cursor should point to.
     */
    public static void set(Cell cursor, Cell to) {
	set(cursor, to, 0);
    }

    static String[] dirStrings = { "-1", "0", "1" };

    /** Set the given cursor to point to the given side of the
     * given cell.
     * The dir parameter controls the side of the cell on which the cursor is
     * placed.
     * @param cursor The cursor cell
     * @param to The cell that the cursor should point to.
     * @param dir The side of the cell this cursor should point to.
     * 		Must be -1, 0 or 1.
     */
    public static void set(Cell cursor, Cell to, int dir) {
	cursor = cursor.h(Dims.d_cursor_cargo_id);
	if(cursor.s(Dims.d_cursor_list_id, -1) == null) {
	    Cell oldtarget = cursor.s(Dims.d_cursor_id, -1);
	    Cell nextcur = cursor.s(Dims.d_cursor_list_id);
	    cursor.disconnect(Dims.d_cursor_id, -1);
	    if(oldtarget != null && nextcur != null)
		try {
		    oldtarget.insert(Dims.d_cursor_id, 1, nextcur);
		} catch(ZZAlreadyConnectedException e) {
		    nextcur.excise(Dims.d_cursor_id);
		    try {
			oldtarget.insert(Dims.d_cursor_id, 1, nextcur);
		    } catch(ZZAlreadyConnectedException f) {
			throw new ZZError("Argh! Argh! Argh! "+f);
		    }
		    throw new ZZError("Error in the cursor structure: The " +
				      "next cursor in the list of cursors " +
				      "is already connected on d.cursor. "+
				      "The connection was severed, but the "+
				      "current operation was aborted." + e);
		}
	}
	cursor.excise(Dims.d_cursor_list_id);
	if(to == null) {
	    if(dir != 0)
		throw new IllegalArgumentException("dir must be 0 if to is null");
	    return;
	}

	try {
	    to.connect(Dims.d_cursor_id, cursor);
	} catch(ZZAlreadyConnectedException e) {
	    try {
		to.s(Dims.d_cursor_id).h(Dims.d_cursor_list_id, 1).
		    insert(Dims.d_cursor_list_id, 1, cursor);
	    } catch(ZZAlreadyConnectedException f) {
		throw new ZZError("ARGH!!!!" + f);
	    }
	}

	if(dir != 0 || cursor.s(Dims.d_cursor_params_id) != null) {
	    Cell paramCell = cursor.getOrNew(Dims.d_cursor_params_id);
	    // Only set if it is changed.
	    if(!paramCell.t().equals(dirStrings[dir + 1]))
		paramCell.setText(dirStrings[dir + 1]);
	}
    }


    /** Get the side this cursor is on.
     *  See zzspec.
     *  <p>
     *  +1 if the cursor is on the positive side of a cell; -1 if the cursor
     *  is on the negative side of a cell; 0 if the cursor accurses not a
     *  position in a media stream, but just a plain cell.
     *  <p>
     *  XXX Have a better method than text!
     */
    public static int getSide(Cell cursor) {
        cursor = cursor.h(Dims.d_cursor_cargo_id);
	Cell side = cursor.s(Dims.d_cursor_params_id);
	if(side == null) return 0;
	int n;
	try {
	    n = Integer.parseInt(side.t());
	} catch(NumberFormatException e) {
	    e.printStackTrace();
	    throw new ZZError("Invalid cursor side: '"+side.t()+"': "
				+e.getMessage());
	}
	return n;
    }

    /** Return the VStream character cell before the cursor.
     * This routine is done there to encapsulate the stream end behaviour.
     * If the returned cell is manipulated properly, the side of the cursor
     * shouldn't actually matter.
     */
    public static Cell getVStreamCellBefore(Cell cursor) {
	Cell pos = getPosition(cursor);
	if(pos == null) return null;
	int side = getSide(cursor);
	if(side == 1) 
	    return pos;
	Cell step = pos.s(Dims.d_vstream_id, -1);
	if(step != null) return step;
	// In this case, the cursor is at the very end of the vstream.
	return pos.h(Dims.d_vstream_id, 1);
    }

    public static void setVStreamCellBefore(Cell cursor, Cell to) {
	setVStreamCellBefore(cursor, -1, to);
    }

    /** 
     * @param side The side of the resulting cursor. If -1, the cursor
     * 			is attached to the cell AFTER the currend one,
     * 			and vice versa.
     */
    public static void setVStreamCellBefore(Cell cursor, int side, Cell to) {
	if(side == 1) {
	    set(cursor, to, side);
	    return;
	}
	Cell step = to.s(Dims.d_vstream_id, 1);
	if(step != null) {
	    set(cursor, step, side);
	    return;
	}
	set(cursor, to.h(Dims.d_vstream_id, -1), side);
    }

    public static void moveOnVStream(Cell cursor, Cell dim, int steps) {
	moveOnVStream(cursor, cursor.space.getDim(dim), steps);
    }

    /** Move this cursor on the vstream, using the given dimension
     * if the cursor goes off the end of the cell.
     * Returns the new accursed cell.
     * XXX needs tests!
     */
    public static void moveOnVStream(Cell cursor, Dim dim, int steps) {
	// XXX ??? Maybe slow at some point...
	while(steps > 1) { moveOnVStream(cursor, dim, 1); steps--; }
	while(steps < -1) { moveOnVStream(cursor, dim, -1); steps++; }
	Cell curCell = get(cursor);
	Cell pos = getPosition(cursor);
	if(pos == null) return ; // XXX Should we make a position?
	Cell npos = pos.s(Dims.d_vstream_id, steps);
	if(steps > 0) {
	    if(pos.equals(curCell)) {
		// Special case: at the very end of stream
		curCell = curCell.s(dim, 1);
		if(curCell != null)
		    setVStreamCellBefore(cursor, curCell);
	    } else {
		if(npos == null) {
		    // Went to end
		    set(cursor, curCell, -1);
		} else {
		    set(cursor, npos, -1);
		}
	    }
	} else {
	    if(pos.equals(curCell)) {
		set(cursor, curCell.h(Dims.d_vstream_id, 1), -1);
	    } else {
		if(npos.equals(curCell)) {
		    curCell = curCell.s(dim, -1);
		    if(curCell != null) 
			set(cursor, curCell, -1); // end of preceding
		}
		set(cursor, npos, -1);
	    }
	}
    }

    /** For cursors on a character inside a cell, return the vstream cell
     * of the character pointed to.
     * XXX This is not a useful routine
     * @return The vstream cell the cursor points at, or, if none, null.
     */
    private static Cell getPosition(Cell cursor) {
	if(getSide(cursor) == 0)
	    return null;
        return cursor.h(Dims.d_cursor_cargo_id).h(Dims.d_cursor_list_id).
            s(Dims.d_cursor_id, -1);
    }
}
