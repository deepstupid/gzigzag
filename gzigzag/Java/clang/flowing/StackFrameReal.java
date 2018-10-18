/*   
StackFrameReal.java
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
 * Written by Benjamin Fallenstein
 */
package org.gzigzag.flowing;
import org.gzigzag.*;

/** A structural implementation for Flowing Clang stack frames.
 */

public class StackFrameReal extends StackFrame {
String rcsid = "$Id: StackFrameReal.java,v 1.6 2000/11/16 20:33:13 bfallenstein Exp $";

    public ZZCell main;

    public StackFrameReal(ZZSpace sp) { main = sp.getHomeCell().N(); }
    public StackFrameReal(ZZCell main) { this.main = main; }
    public StackFrameReal(StackFrameReal caller) {
	this(caller.main.N("d.2", 1));
    }

    public Object getSingle(ZZCell c) {
	ZZCell where = main.intersect("d..flowing-frame", 1, 
			           c, "d..flowing-data", 1);
	if(where == null) return null;
	ZZCell acc = ZZCursorReal.get(where);
	if(acc != null) return acc;
	else return where;
    }
    public void setSingle(ZZCell c, Data d, int at) {
	ZZCell where = main.intersect("d..flowing-frame", 1, 
			           c, "d..flowing-data", 1);
	if(where == null) {
	    where = main.N("d..flowing-frame", 1);
	    c.insert("d..flowing-data", 1, where);
	}
	if(d.o(at) instanceof ZZCell)
	    ZZCursorReal.setcursor(where, d.c(at));
	else {
	    ZZCursorReal.setcursor(where, null);
	    where.setText(d.s(at));
	}
    }

    public ZZCell getPos() { return ZZCursorReal.get(main.h("d.2")); }
    public void setPos(ZZCell c) { ZZCursorReal.setcursor(main, c); }

    public StackFrame call() { return new StackFrameReal(this); }
    public boolean parentof(StackFrame sf) {
	if(!(sf instanceof StackFrameReal)) return false;
	ZZCell find = ((StackFrameReal)sf).main;
	for(ZZCell c = main; c != null && c != main; c = c.s("d.2")) {
	    if(c.equals(find)) return true;
	}
	return false;
    }
    public StackFrame ret() {
	ZZCell caller = main.s("d.2", -1);
	setPos(null);
	if(caller != null) {
	    caller.disconnect("d.2", +1);
	    return new StackFrameReal(caller);
	} else
	    return null;
    }
    public void delete() {
	ZZCell pos = main;
	while(pos != null) {
	    ZZCell nextrow = pos.s("d.2");
	    while(pos != null) {
		ZZCell next = pos.s("d..flowing-frame");
		pos.delete();
		pos = next;
	    }
	    pos = nextrow;
	}
    }
    public boolean equals(StackFrameReal f) { return main.equals(f.main); }
}