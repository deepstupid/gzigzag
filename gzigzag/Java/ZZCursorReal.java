/*   
ZZCursorReal.java
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

/** A real cursor in a structure.
 * This class also implements the commonly used
 * static methods for convenience.
 */

public class ZZCursorReal extends ZZCursor {
String rcsid = "$Id: ZZCursorReal.java,v 1.33 2001/04/17 15:47:09 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    ZZCell ccell;
    public ZZCursorReal(ZZCell start) {
	// ccell = start.h("d.cursor-cargo", -1);
	// XXX ?? Which one is right?
	ccell = start;
    }
    public ZZCell get() { return get(ccell); }
    public void set(ZZCell c) { set(ccell, c); }
    public int getOffs() { return getOffs(ccell); }
    public void setOffs(int i) { setOffs(ccell, i); }

    /** Get the cell the given cursor cell or cargo cell is pointing to.
     */
    static public final ZZCell get(ZZCell start) {
	// XXX Maybe should be less allowing...
	return start.h("d.cursor-cargo", -1).
		h("d.cursor-list", -1).s("d.cursor", -1);
    }

    /** Set the given cursor cell to point to a cell.
     */
    static public void setcursor(ZZCell cur, ZZCell c) {
	removeFromCursorList(cur);
	if (c != null) addToCursorList(cur, c);
	if(cur.s("d.cursor-sub") != null) cur.disconnect("d.cursor-sub", 1);
	trigger(cur);
    }

    /** Set the given cursor cargo to point to a cell.
     * If a cursor is cargoed already, use it. If no cursor is cargoed,
     * create one.
     */
    static public void setcargo(ZZCell cargo, ZZCell c) {
	if(cargo.s("d.cursor-cargo", -1) == null)
	    cargo.N("d.cursor-cargo", -1);
	setcursor(cargo.h("d.cursor-cargo", -1), c);
    }

    /** Set the given cursor cell or cursor cargo cell to point to a cell.
     * This routine has an interesting heuristic about creating
     * a cell on <code>d.cursor-cargo</code>: if 
     * the cell is already connected negwards on <code>d.cursor</code> or
     * <code>d.cursor-list</code>,
     * no cursor cargo cell is created, otherwise it is.
     * <p>
     * XXX should this be deprecated in favor of setcursor and setcargo?
     */
    static public final void set(ZZCell start, ZZCell c) {
	// A rather intricate check: if there's nothing on d.cursor-cargo,
	// create it but only if we're not going negwards on d.cursor or
	// d.cursor-list already.
	
	if(start.s("d.cursor-list", -1) == null &&
	   start.s("d.cursor", -1) == null) {
	    setcargo(start, c);
	} else
	    setcursor(start, c);
    }

    static public void set(ZZCell c, ZZCursor curs) {
	set(c, curs.get());
	setOffs(c, curs.getOffs());
    }

    /** Create a new cursor cell pointing to another cell.
     * Usually you use set to create a new cell, but sometimes you don't want
     * to attach a cargo cell on d.cursor-cargo; create just creates a new
     * cell on d.cursor and returns it.
     */
    static public final ZZCell create(ZZCell c) {
	ZZCell cur = c.N();
	addToCursorList(cur, c);
	return cur;
    }

    /** Attach a cursor to another one on d.cursor-cargo.
     * The first cell is removed from its current d.cursor-cargo rank and
     * inserted poswards behind the second one. Thus, the first cursor must
     * be a cursor cargo cell, not a real cursor cell (connected negwards 
     * along d.cursor).
     */
    static public final void attach(ZZCell which, ZZCell where) {
	if(which.s("d.cursor", -1) != null ||
	   which.s("d.cursor-list", -1) != null)
	    throw new ZZError("which is a real cursor cell");
	where.insert("d.cursor-cargo", 1, which);
	trigger(which);
    }

    /** Get the color associated with this cursor in the structure.
     */
    public java.awt.Color getColor() { return getColor(ccell); }

    /** Get the color associated with the given cursor in the structure.
     */
    static public final java.awt.Color getColor(ZZCell start) {
	ZZCell hc = start.h("d.cursor-cargo", -1);
	if(hc.s("d.cursor-sub") != null) return null;
	hc = hc.h("d.cursor-sub");
	ZZCell col = hc.s("d.1", 1);
	if(col == null)
	    return null;
	try {
	    return new java.awt.Color(Integer.parseInt(col.getText()));
	} catch(NumberFormatException e) {
	    return null;
	}
    }

    /** Get the color associated with the given cursor, or white.
     * Return Color.white if no color is associated in the structure;
     * getColor would return null.
     */
    static public final java.awt.Color getColorOrWhite(ZZCell start) {
	java.awt.Color c = getColor(start);
	if(c == null) return java.awt.Color.white;
	return c;
    }
    
    /** Set the color associated with the given cursor in the
     * structure.
     * XXX Move cursor color to another dimension...
     */
    static public final void setColor(ZZCell start, java.awt.Color c) {
	ZZCell hc = start.h("d.cursor-cargo", -1);
	// if(hc.s("d.1", 1) != null)
	//     throw new Error("Can't set color of cursor: something's there.");
	hc.getOrNewCell("d.1",1).setText(String.valueOf(c.getRGB()));
    }

    /** Get the color of the first cursor associated with this cell.
     *  If there is no colored cursor accursing this cell, return null.
     */
    static public final java.awt.Color getAccursedColor(ZZCell c) {
	Enumeration e = ZZCursorReal.getPointers(c);
	while(e.hasMoreElements()) {
	    java.awt.Color col = ZZCursorReal.getColor((ZZCell)e.nextElement());
	    if(col != null) return col;
	}
	return null;
    }

    /** Set the offset of the given cursor.
     */
    static public void setOffs(ZZCell c, int i) {
	String s = null;
	if(i == NO_OFFSET)
	    s = "";
	else if(i < 0) {
	    throw new ZZError("Trying to set negative cursor offset!");
	} else
	    s = String.valueOf(i);
	c.h("d.cursor-cargo", -1).setText(s);
	trigger(c);
    }

    /** Get the offset of the given cursor.
     */
    static public int getOffs(ZZCell c) {
	String s = c.h("d.cursor-cargo").getText();
	if(s==null || s.equals("")) return NO_OFFSET;
	try {
	    int res = Integer.parseInt(s);
	    if(res < 0)  {
		ZZLogger.log(
		    "Serious: negative cursor offset. Resetting. "+c+" "+res);
		setOffs(c, NO_OFFSET);
		return NO_OFFSET;
	    }
	    return res;
	} catch(NumberFormatException e) {
	}
	return NO_OFFSET;
    }

    /** Delete extra cells associated only with the given cursor.
     * After this, deleting the cell will remove all traces.
     */
    static public void delete(ZZCell c) {
	if(c.s("d.cursor-cargo", 1) != null) return;
	c = c.s("d.cursor-cargo", -1);
	if(c.s("d.cursor-cargo", -1) != null) return;
	// Now, c is the cell on the cursor list with no other cargo,
	// and is not the original cell.
	removeFromCursorList(c);
	c.delete();
    }

    /** Find cursors that point to the given cell. 
     * This enumeration goes through the cells on the cursor list,
     * note that each of those may have cursor cargo attached with
     * them. There is not yet a function to go through the cursor
     * cargoes.
     */
    static public Enumeration getPointers(final ZZCell c) {
        class PointerEnum implements Enumeration {
            ZZCell cur = c.s("d.cursor", 1);
	    { 
		p("PointerEnum created with " + c + ", cur = " + cur);
	    }
            
            public PointerEnum(ZZCell d) {
                cur = d.s("d.cursor", 1);
            }
            
            public boolean hasMoreElements() {
                boolean rv = cur != null;
                p("PointeEnum: hasMoreElements:" + rv);
                return rv;
            }

            public Object nextElement() {
                if (cur == null) throw new NoSuchElementException();
                Object rv = cur;
                cur = cur.s("d.cursor-list", 1);
                p("PointerEnum: rv = " + rv + ", cur = " + cur);
                return rv;
            }
        }
        return new PointerEnum(c);
    }
    
    /** Adjust the cursors on a cell after text insertion or deletion.
     * This in- or decrements cursors whose offsets are past a given
     * division point. Cursors without offsets remain unchanged.
     * @param acc The cell on which the cursors to be changed are.
     * @param div The index from which on we want offsets to be changed.
     * @param inc The amount by which we want offsets to be changed.
     *            Negative for deletions. Note that offsets won't be set
     *            below div, that is after we've deleted seven characters,
     *            a cursor which was on the third character will be set
     *            where the first char was, not (7-3=)four posns before that.
     */
    static public void adjustCursors(ZZCell acc, int div, int inc) {
	Enumeration e = getPointers(acc);
	while(e.hasMoreElements()) {
	    ZZCell curs = (ZZCell)e.nextElement();
	    int offs = getOffs(curs);
	    if(offs != NO_OFFSET && offs >= div) {
		if(offs + inc < div)
		    setOffs(curs, div);
		else
		    setOffs(curs, offs + inc);
	    }
	}
    }

    /** Find the visual text offset for the cursor.
     * This is purely a convenience routine: if the cursor has an
     * offset, it returns that, but if not, it returns half of the
     * length of text in the accursed cell.
     */
    static public int getVisualTextOffset(ZZCell c) {
	int ret = getOffs(c);
	if(ret == NO_OFFSET)
	    ret = get(c).getText().length() / 2;
	return ret;
    }

    // Private stuff

    static private void removeFromCursorList(ZZCell cur) {
	ZZCell acc = cur.s("d.cursor", -1);
	if(acc != null)
	    cur.disconnect("d.cursor", -1);
	
	ZZCell nb = cur.s("d.cursor-list", 1);
	if(acc != null && nb != null)
	    nb.connect("d.cursor", -1, acc);
		
	cur.excise("d.cursor-list");
    }

    static private void addToCursorList(ZZCell cur, ZZCell c) {
	ZZCell nb = c.s("d.cursor", 1);
	if(nb != null)
	    // insert instead of connect so that when we have headcells on
	    // looping ranks, d.cursor-list can loop. (Dunno if we need this,
	    // but kinda seemed right.)
	    nb.h("d.cursor-list", 1)
		.insert("d.cursor-list", 1, cur);
	else
	    c.connect("d.cursor", 1, cur);
    }

    static private LoopDetector trigdetect = null;
    static private boolean stoptrig = false;

    static private void trigger(ZZCell start) {
	p("Cursor trigger triggered");
	boolean wasnull = false;
	if(trigdetect == null) {
	    wasnull = true;
	    trigdetect = new LoopDetector();
	}
	
	synchronized(trigdetect) {
	try {
	    ZZCell c = start.h("d.cursor-cargo", -1);
	    for(; c != null; c = c.s("d.cursor-cargo", 1)) {
		if(c.s("d..cursor-trigger", -1) == null) continue;
		p(""+c);

		if(trigdetect.isLooping(c)) {
		    ZZLogger.log("Detected trigger loop while trying "+
				 "to set a cursor. Breaking triggering now.");
		    stoptrig = true;
		    break;
		}
		
		ZZCell commcell = c.h("d..cursor-trigger", -1);
		ZZCommand comm = ZZCommand.getCommand(commcell);
		if(comm != null) {
		    p("Calling trigger command: "+commcell.getText());
		    try {
			comm.exec(c);
		    } catch(ZZError e) {
			ZZLogger.exc(e, "Cursor trigger exception: ");
			stoptrig = true;
		    }
		    if(stoptrig) break;
		}
	    }
	} finally {
	    if(wasnull) {
		trigdetect = null;
		stoptrig = false;
	    }
	}
	}
    }
}
