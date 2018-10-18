/*
UndoableDim.java
*
*    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
import java.util.*;

/** A dimension wrapper for adding UndoList support to VStreamDims.
 *  NOTE: undoes insertAfterCell(), but not connectRange()! This is
 *  because we cannot handle span transclusions correctly during
 *  committing-- the space has to take care of that.
 *  <p>
 *  XXX Actually, span transclusions should be undoable, too--
 *  GZZ1Cache collapses them nicely and correctly.
 */

public class UndoableVStreamDim extends UndoableDim implements VStreamDim {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.out.println(s); }

    protected VStreamDim vs;

    public UndoableVStreamDim(VStreamDim vs, UndoList undo) {
        super((CopyableDim)vs, undo, Id.stripHome(Dims.d_vstream_id.id));
	this.vs = vs;
    }

    public VStreamDim getBase() {
	return vs;
    }

    public void connect(Cell c, Cell d) { p("connect"); super.connect(c, d);}

    public void disconnect(Cell c, int dir) { p("dis"); super.disconnect(c, dir);}

    public void insertAfterCell(Cell at, Cell rank) { p("insert after cell");
	if(rank == null) return;

	Cell end = vs.h(rank, 1);
	Cell next = vs.s(at);

	vs.insertAfterCell(at, rank);

	if(next != null)
	    undo.add(disconnect, at, next);

	undo.add(connect, at, rank);

	if(next != null)
	    undo.add(connect, end, next);
    }

    public void removeRange(Cell first, Cell last) { p("remove range");
	Cell before = vs.s(first, -1);
	Cell after = vs.s(last);

	vs.removeRange(first, last);

	if(before != null)
	    undo.add(disconnect, before, first);

	if(after != null)
	    undo.add(disconnect, last, after);
    
	if(before != null && after != null)
	    undo.add(connect, before, after);
    }




    // notifyTransclusion is not remembered in the undo list

    public void notifyTransclusion(Cell start, int length) {
	vs.notifyTransclusion(start, length);
    }





    // From here on, only proxy stuff to vs.

    public void iterate(org.gzigzag.vob.CharRangeIter i, Cell stream,
			Map extra) {
	vs.iterate(i, stream, extra);
    }

    public void dumpVStreamInfo(Cell c) {
	vs.dumpVStreamInfo(c);
    }
    
}
