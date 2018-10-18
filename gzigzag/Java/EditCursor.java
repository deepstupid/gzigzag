/*   
EditCursor.java
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

/** A stack of edit cursors.
 * This class also implements the commonly used static methods.
 * <p>
 * When creating a framework within GZZ, you will often want to support a
 * wide variety of structures for use with that framework. This is because you
 * want users of your framework to be able to use any ZigZag structure suitable
 * to their needs (with some small restrictions), not one you impose on them.
 * Now, a useful design pattern to realize this is the following: To a maincell
 * recognized by the framework, let the user attach information in any way
 * they like; but to the maincell, connect information (in a standardized
 * format) about how to get to all the custom stuff the user has attached.
 * Retrieve the stuff using that standardized information.
 * <p>
 * However, this creates a conflict with the simple ZigZag cursors: when you
 * are accursing a cell the user has attached in some custom way, you do not
 * the way back to the maincell. Plus, you cannot look up the user's
 * description of their structure, because that's connected <em>to the
 * maincell</em>-- the very cell you are searching.
 * <p>
 * The solution is to keep track of how you got here. The EditCursor stack
 * helps you with that: each time you enter a new "level" in the structure,
 * instead of setting the cursor to that cell, you push that cell; when you
 * want to move one level up, you can pop, which returns the next-higher
 * level (hm, that's a bit against what you'd expect from "pop").
 * <p>
 * (possibly EditCursor is a misnomer: this supports stacks of cursors for
 * all sorts of purposes)
 */

public class EditCursor extends ZZCursor {
String rcsid = "$Id: EditCursor.java,v 1.5 2001/04/23 11:38:53 bfallenstein Exp $";

    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    // ZZCURSOR IMPLEMENTATION

    ZZCell ccell;
    public EditCursor(ZZCell ccell) {
	this.ccell = ccell;
    }
    public ZZCell get() { return get(ccell); }
    public void set(ZZCell c) { set(ccell, c); }
    public int getOffs() { return getOffs(ccell); }
    public void setOffs(int i) { setOffs(ccell, i); }
    public void pop() { pop(ccell); }
    public void push(ZZCell c) { push(ccell, c); }
    public void push(ZZCursor curs) { push(ccell, curs); }
    public void setStack(ZZCursor[] cursors) { setStack(ccell, cursors); }
    public void setStack(VirtualEditCursor cur) { setStack(ccell, cur); }
    public VirtualEditCursor getVirtual() { return getVirtual(ccell); }
    public void popall() { popall(ccell); }



    // STATIC INTERFACE

    /** Push a new cell on this edit cursor stack.
     *  This creates a new cursor on the stack and sets it to the given cell.
     */
    static public final void push(ZZCell cur, ZZCell c) {
	ZZCursorReal.setcursor(add(cur), c);
    }

    static public final void push(ZZCell cur, ZZCursor from) {
	ZZCursorReal.setcursor(add(cur), from.get());
	ZZCursorReal.setOffs(cur, from.getOffs());
    }

    /** Pop the topmost cursor from this edit stack.
     *  Raises an error if this would remove the last cursor.
     */
    static public final void pop(ZZCell cur) {
	remove(cur);
    }

    /** Set the whole stack at once, from an array of cursors.
     */
    static public final void setStack(ZZCell cur, ZZCursor[] from) {
	if(from.length == 0)
	    throw new ZZError("Can't set stack from empty array");
	popall(cur);
	ZZCursorReal.set(cur, from[0]);
	for(int i=1; i<from.length; i++)
	    push(cur, from[i]);
    }

    /** Set the whole stack at once, from a virtual edit cursor.
     */
    static public final void setStack(ZZCell cur, VirtualEditCursor from) {
	setStack(cur, from.getStack());
    }

    /** Get a virtual stack with the same contents as this one. */
    static public VirtualEditCursor getVirtual(ZZCell cur) {
	VirtualEditCursor v = new VirtualEditCursor(ZZCursorReal.get(cur));
	ZZCell c = cur.h("d.cursor-cargo");
	for(c = c.s("d.cursor-sub"); c != null; c = c.s("d.cursor-sub"))
	    v.push(ZZCursorReal.get(c));
	return v;
    }

    /** Pop all cursor but the last one. */
    static public void popall(ZZCell cur) {
	ZZCell head = cur.h("d.cursor-cargo");
	if(head.s("d.cursor-sub") != null) head.disconnect("d.cursor-sub", 1);
    }

    /** Get the cell the given edit cursor is pointing to.
     */
    static public final ZZCell get(ZZCell cur) {
	return ZZCursorReal.get(top(cur));
    }

    /** Set the given edit cursor to point to a cell.
     */
    static public final void set(ZZCell cur, ZZCell c) {
	if(cur.s("d.cursor-cargo", -1) == null) cur.N("d.cursor-cargo", -1);
	ZZCursorReal.setcursor(top(cur), c);
    }

    static public void set(ZZCell cur, ZZCursor from) {
	ZZCursorReal.setcursor(top(cur), from.get());
	ZZCursorReal.setOffs(top(cur), from.getOffs());
    }

    /** Set the offset of the given edit cursor stack.
     */
    static public void setOffs(ZZCell cur, int i) {
	ZZCursorReal.setOffs(top(cur), i);
    }

    /** Get the offset of the given edit cursor stack.
     */
    static public int getOffs(ZZCell cur) {
	return ZZCursorReal.getOffs(top(cur));
    }



    // INTERNAL STUFF

    /** Get the cursor cell on the top of this edit cursor stack. 
     *  Topmost == the one pushed last.
     */
    static protected final ZZCell top(ZZCell cur) {
	return cur.h("d.cursor-cargo").h("d.cursor-sub", 1);
    }

    /** Create a new cursor cell on top of this stack. Used in push. */
    static protected final ZZCell add(ZZCell cur) {
	return cur.h("d.cursor-cargo").h("d.cursor-sub", 1).N("d.cursor-sub");
    }

    /** Remove the topmost cursor cell from this stack. Used in pop. */
    static protected final void remove(ZZCell cur) {
	ZZCell top = cur.h("d.cursor-cargo").h("d.cursor-sub", 1, true);
	if(top == null)
	    throw new ZZError("Tried to pop topmost cursor from edit cursor stack");
	top.delete();
    }

}
