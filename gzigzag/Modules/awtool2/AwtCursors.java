/*   
AwtCursors.java
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
 * Written by Kimmo Wideroos
 */

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** Awt cursors. Supports both view wide and global cursors.
 */

public class AwtCursors {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    public static AwtCursorIdentifier c_input1 = 
        new AwtCursorIdentifier(Color.red, "input1"); //, true);
    public static AwtCursorIdentifier c_input2 = 
        new AwtCursorIdentifier(Color.gray, "input2"); //, true);
    /*public static AwtCursorIdentifier c_layer = 
        new AwtCursorIdentifier(Color.orange, "layer", false);
    */
    /*
    public static AwtCursorIdentifier c_metrics = 
        new AwtCursorIdentifier(Color.white, "metrics", false);
    */
    private ZZCell awtlocals = null;

    public AwtCursors(ZZCell awtlocals) {
        this.awtlocals = awtlocals;
    } 

    private static ZZCell getSharedCursors(ZZSpace sp) {
	ZZCell awtglobals, cursors;
	awtglobals = ZZDefaultSpace.
	    findOnSystemlist(sp, "AwtGlobals", true).getOrNewCell("d.1");
	cursors = awtglobals.findText("d.2", 1, "Cursors");
	if(cursors == null) {
	    cursors = awtglobals.N("d.2");
	    cursors.setText("Cursors");
	}
	return cursors.getOrNewCell("d.1");
    }
    /*
    private static ZZCell getLocalCursors(ZZCell awtlocals) {
	ZZCell cursors;
	cursors = awtlocals.findText(AwtDim.d_locals, 1, "Cursors");
	if(cursors == null) {
	    cursors = awtlocals.N(AwtDim.d_locals);
	    cursors.setText("Cursors");
	}
	return cursors.getOrNewCell("d.1");	
    }
    */
    private static ZZCell getCursorCell(AwtCursorIdentifier ci, 
					ZZCell cursors) {
	ZZCell cur;

        for(cur=cursors; cur != null; cur = cur.s("d.2")) {
            if(cur.getText().equals(ci.id()) &&
               ZZCursorReal.getColor(cur).toString().equals(ci.colorString()))
                return cur;
        }
        return null;
    }

    private static ZZCell newCursorCell(AwtCursorIdentifier ci, 
					ZZCell cursors) {
	ZZCell newc = cursors.N("d.2");
	newc.setText(ci.id());
	return newc;
    }

    public ZZCell get(AwtCursorIdentifier ci) {
        return get(ci, this.awtlocals);
    }

    public static ZZCell get(AwtCursorIdentifier ci, ZZCell awtlocals) {
	ZZCell cur = null, cursors;

	//if(ci.shared()) {
	ZZSpace sp = awtlocals.getSpace();
	cursors = getSharedCursors(sp);
	//} else {
	//cursors = getLocalCursors(awtlocals);
	//}

	cur = getCursorCell(ci, cursors);
	if(cur==null) return null;
        return ZZCursorReal.get(cur);
    }
    /*
    public void set(AwtCursorIdentifier ci, ZZCell c) {
        set(ci, c, this.awtlocals);
    }
    */

    /* @ci        identifier of cursor to be set
     * @c         target cell
     * @awtlocals root cell for local parameters (view wide parameters)
     */
    /*    public static void set(AwtCursorIdentifier ci, ZZCell c, ZZCell awtlocals) {
     */
   public static void set(AwtCursorIdentifier ci, ZZCell c) {
	ZZCell cur = null, cursors;

	//if(ci.shared()) {
	ZZSpace sp = c.getSpace();
	cursors = getSharedCursors(sp);
	//} else {
	//    cursors = getLocalCursors(awtlocals);
	//}
	cur = getCursorCell(ci, cursors);
	if(cur == null) cur = newCursorCell(ci, cursors);
        ZZCursorReal.set(cur, c);
	ZZCursorReal.setColor(cur, ci.color());
    }
}






