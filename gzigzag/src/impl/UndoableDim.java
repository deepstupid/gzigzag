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

/** A dimension wrapper for adding UndoList support.
 */

public class UndoableDim extends FilterDim {

    protected UndoList undo;
    protected byte[] id;

    public UndoableDim(CopyableDim base, UndoList undo, String id) {
        super(base);
	this.undo = undo;
	this.id = id.getBytes();

	if(undo == null)
	    throw new NullPointerException("undo");
    }

    // 2 params: the connected cells
    protected UndoList.Op connect = new UndoList.Op() {
            public void undo(Object[] list, int nth) {
                Cell c = (Cell)list[nth+1], d = (Cell)list[nth+2];
		base.connect(c, d);
            }
            public void redo(Object[] list, int nth) {
                Cell c = (Cell)list[nth+1], d = (Cell)list[nth+2];
                if(!base.s(c, 1).equals(d))
                    throw new ZZError("Inconsistent redo");
                base.disconnect(c, 1);
            }
            public void commit(Object[] list, int nth, UndoList.GIDMaker gid, GZZ1Handler hdl) {
                Cell c = (Cell)list[nth+1], d = (Cell)list[nth+2];
                GZZ1Handler.SimpleDim sd = hdl.dimSection(id);
                sd.connect(gid.make(c), gid.make(d));
                sd.close();
            }
        };

    // 2 params: the disconnected cells
    protected UndoList.Op disconnect = new UndoList.Op() {
            public void undo(Object[] list, int nth) {
                Cell c = (Cell)list[nth+1], d = (Cell)list[nth+2];
		if(!base.s(c, 1).equals(d))
		    throw new ZZError("Inconsistent undo");
		base.disconnect(c, 1);
            }
            public void redo(Object[] list, int nth) {
                Cell c = (Cell)list[nth+1], d = (Cell)list[nth+2];
		base.connect(c, d);
            }
            public void commit(Object[] list, int nth, UndoList.GIDMaker gid, GZZ1Handler hdl) {
                Cell c = (Cell)list[nth+1], d = (Cell)list[nth+2];
                GZZ1Handler.SimpleDim sd = hdl.dimSection(id);
                sd.disconnect(gid.make(c), gid.make(d));
                sd.close();
            }
        };

    public void connect(Cell c, Cell d)
        throws ZZAlreadyConnectedException {
	super.connect(c, d);
	undo.add(connect, c, d);
    }

    public void disconnect(Cell c, int dir) {
	Cell other = s(c, dir);
	super.disconnect(c, dir);
	if(other != null) {
	    if(dir > 0)
		undo.add(disconnect, c, other);
	    else
		undo.add(disconnect, other, c);
	}
    }

}
