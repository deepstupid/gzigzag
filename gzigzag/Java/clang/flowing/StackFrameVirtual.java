/*   
StackFrameVirtual.java
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
import java.util.*;

/** A virtual implementation for Flowing Clang stack frames.
 */

public class StackFrameVirtual extends StackFrame {
String rcsid = "$Id: StackFrameVirtual.java,v 1.5 2000/11/16 20:33:13 bfallenstein Exp $";

    Hashtable values = new Hashtable();
    static final Object NULL = new Object();
    StackFrameVirtual caller = null, called = null;
    ZZCell pos;

    public StackFrameVirtual() {}
    public StackFrameVirtual(StackFrameVirtual frame) { caller = frame; }
    
    public Object getSingle(ZZCell c) {
	Object res = values.get(c);
	return res == NULL ? null : res;
    }
    public void setSingle(ZZCell c, Data d, int at) {
	values.put(c, d.o(at) == null ? NULL : d.o(at));
    }
    public ZZCell getPos() { return called == null ? pos : called.getPos(); }
    public void setPos(ZZCell c) { pos = c; }
    public StackFrame call() { return called = new StackFrameVirtual(this); }
    public boolean parentof(StackFrame sf) {
	if(called == null) return false;
	return (sf == called) || (called.parentof(sf));
    }
    public StackFrame ret() {
	pos = null;
	if(caller != null) caller.called = null;
	return caller;
    }
    public boolean equals(StackFrameVirtual frame) { return this == frame; }
}


