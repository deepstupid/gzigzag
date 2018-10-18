/*
VirtualEditCursor.java
*
*    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
package org.gzigzag;
import java.util.*;

/** A stack of edit cursors, not in the ZZ structure.
 *  Note that this is not a stack proper: the top element is freely
 *  accessible, and peek() returns the first element <em>under</em> the
 *  top element (or null if there's only the top element). Also, the top
 *  may never become null. It's thus more like a stack with an additional
 *  field for the top element; and actually, that's exactly how it's
 *  implemented. (It was implemented differently before, but I ran into
 *  problems with that ;) )
 *  @see EditCursor
 */


public final class VirtualEditCursor {
String rcsid = "$Id: VirtualEditCursor.java,v 1.3 2001/04/23 12:12:06 bfallenstein Exp $";

    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    Stack s = new Stack();
    ZZCursorVirtual top;

    public VirtualEditCursor(ZZCell c) {
        top = new ZZCursorVirtual(c);
    }
    public VirtualEditCursor(ZZCell c, int offs) {
	top = new ZZCursorVirtual(c, offs);
    }
    public ZZCell get() { return top.get(); }
    public void set(ZZCell c) { top.set(c); }
    public int getOffs() { return top.getOffs(); }
    public void setOffs(int i) { top.setOffs(i); }
    public void pop() { top = (ZZCursorVirtual)s.pop(); }
    public void push(ZZCell c) { 
	s.push(top); 
	top = new ZZCursorVirtual(c); 
    }
    public void push(ZZCursor curs) {
	s.push(top);
	top = new ZZCursorVirtual(curs);
    }
    public ZZCursor[] getStack() { 
	ZZCursor[] res = new ZZCursor[s.size()+1];
	res[res.length-1] = top;
	for(int i=res.length-2; i>=0; i--)
	    res[i] = (ZZCursorVirtual)s.elementAt(i);
	return res;
    }
    public ZZCell peek() { return ((ZZCursorVirtual)s.peek()).get(); }
    public int peekOffs() { return ((ZZCursorVirtual)s.peek()).getOffs(); }
    public void popall() { while(!s.empty()) s.pop(); }
    public int size() { return s.size() + 1; }
}
