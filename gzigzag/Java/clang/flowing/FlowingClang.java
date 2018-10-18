/*   
FlowingClang.java
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
 * Written for Heraclitus Clang by Tuomas Lukka
 * Adaption for Flowing Clang by Benjamin Fallenstein
 */
package org.gzigzag.flowing;
import org.gzigzag.*;
import java.util.*;

/** A simple (imperative) clang passing values dataflow-like.
 * Kind of a cross between a dataflow ("graphical") programming language and
 * an assembler. On the one hand we have sequences of instructions, each with
 * zero or more in- and output variables; variables are set and read;
 * jmp-style branching.
 * <p>
 * However, variables can only be set in one place
 * * XXX not any more -- explain XXX
 * (they can be set more than
 * once by executing the same instruction multiple times). All references to
 * the variable are clones; the original (rootclone) is the place where the
 * variable is set. This means when you view d.clone, you can see how the
 * data "flows" through the program (hence the language's name).
 * <p>
 * Execution strictly proceeds on d.xeq. There are no blocks: when there's
 * nothing more on d.xeq, the call returns. Until then, the return values
 * (if any) have to be set. Primitives are called by putting their ID into
 * a cell, putting the input values negwards and the variables to take the
 * output poswards on d.1 from it. (XXX use a less string-based behavior?)
 * Subroutines are called by cloning their maincell. Parameter passing and
 * results work the same as with primitives.
 * <p>
 * Primitives and subroutines are passed cells, and return cells. If in
 * the "input zone" of for a cell or subroutine (neg on d.1) there's a
 * cell which isn't cloned (and thus cannot be read as a variable, because
 * it cannot have a value assigned), it is taken as literal and passed to
 * the subroutine. Cloned cells (variables) are dereferenced in the stack frame
 * for the current routine, which means they have to be set. What's passed to
 * the subroutine or primitive is the cell which was formerly assigned to the
 * variable, not the variable cell itself.
 * <p>
 * To create a subroutine, create a non-clone cell with the routine's name.
 * (Later, you clone this into other code.) Put its input variables
 * negwards on d.1. Then put the subroutine on d.xeq. When you're done,
 * clone the result variable(s) from the point where you set them in the
 * routine to the right of the routine's maincell.
 * <p>
 * To branch, create a cell with a question mark ("?") in a strip, put a value
 * (variable or literal, but the latter doesn't make too much sense) in its
 * "input zone" (neg on d.1), which contains a boolean value ("true" or "false"
 * -- everything else is an error), and put a cell in what would usually be
 * its "output zone" (pos on d.1). From there, hang a new strip. If the
 * condition is true, execution proceeds on the new strip; if the condition
 * is false, execution proceeds on the old strip.
 * <p>
 * Blank cells in a strip are usually just ignored. However, a blank cell at
 * the end of a strip works like a goto statement: if there's something
 * poswards on d.1, the execution pointer jumps to the endcell on d.1
 * and proceeds execution on d.xeq. This is used when, after branching
 * because of some condition, you want to unify your strips (i.e., you
 * make a blank cell on the end of each one, connect them on d.1, and
 * proceed programming on the posmost cell on d.1).
 * <p>
 * Loops can be constructed by looping ZZ cells. You should take care to
 * have a condition somewhere in the loop which can turn true. (XXX break
 * infinite loops somehow!)
 * <p>
 * The language only uses the d.1, d.xeq, and d.clone dimensions. The
 * combinations d.1/d.xeq, d.1/d.clone, d.1/d.xeq/d.clone, d.1/d.clone/d.xeq
 * all make sense to view, especially in the row and column views.
 */

public class FlowingClang {
public static final String rcsid = "$Id: FlowingClang.java,v 1.7 2000/11/16 20:33:13 bfallenstein Exp $";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }

    public static final String dim = "d.xeq";

    static Hashtable pss = new Hashtable();
    static PrimitiveSet findPrimitiveSet(String set) {
	Object o = pss.get(set);
	if(o != null) return (PrimitiveSet)o;
	try {
	    o = Class.forName("org.gzigzag.flowing."+set).newInstance();
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    return null;
	}
	if(!(o instanceof PrimitiveSet)) return null;
	pss.put(set, o);
	return (PrimitiveSet)o;
    }

    static Hashtable prims = new Hashtable();
    static Primitive findPrimitive(String s) {
	Object o = prims.get(s);
	if(o != null) return (Primitive)o;

	int ind = s.indexOf(".");
	if(ind < 0) return null;
	String set = s.substring(0, ind);
	String id = s.substring(ind+1);
	PrimitiveSet ps = findPrimitiveSet(set);
	Primitive prim = ps.get(id);
	if(prim != null)
	    prims.put(id, prim);
	return prim;
    }

    public static Data run(ZZCell c, Data d) { return run(c, d, false); }

    public static Data run(ZZCell c0, Data d, boolean real) {
	p("Flowing Clang run: "+c0.getText());
	ZZCell c = c0.getRootclone();
	StackFrame frame;
	if(real) frame = new StackFrameReal(c.getSpace());
	else frame = new StackFrameVirtual();
	frame.setPos(c);
	frame.put(c, d, -1);
	StackFrame f = frame;
	try {
	    while(f != null) f = step(f, true);
	} catch(Throwable t) {
	    ZZLogger.exc(t, "Exception in Flowing Clang run. ");
	    return null;
	}
	return frame.get(c, +1);
    }

    public static ZZCell start(ZZCell c0, Data d) {
	StackFrameReal frame = new StackFrameReal(c0.getSpace());
	frame.setPos(c0);
	frame.put(c0, d, -1);
	return frame.main;
    }

    public static StackFrame jump(StackFrame frame) {
	StackFrame f = frame;
	do {
	    f = step(f, true);
	} while(frame.parentof(f));
	return f;
    }

    public static StackFrame step(StackFrame frame, boolean throwerrs) {
	ZZCell cur = frame.getPos().s(dim);
	if(cur == null) {			// RETURN
	    p("Flowing clang return step");
	    StackFrame ret = frame.ret();
	    if(ret == null) return null;
	    ZZCell rpos = ret.getPos();
	    ret.put(rpos, frame.get(rpos.getRootclone(), +1), +1);
	    frame.delete();
	    return ret;
	}
	
	p("Flowing Clang step: "+cur.getText()+" ("+cur.getID()+")");
	ZZCell root = cur.h("d.clone", -1, true);
	String s = cur.getText();
	if(s.equals("") && root==null) {	// GOTO
	    frame.setPos(cur.h("d.1", 1));
	    return frame;
	}
	
	Data d = frame.get(cur, -1);		// IF
	if(s.equals("?")) {
	    Primitive.count(d, 1);
	    if(d.b(0)) cur = cur.h("d.1", 1);
	    frame.setPos(cur);
	    return frame;
	}
	
	Primitive p = findPrimitive(s);
	if(p != null) {				// PRIMITIVE
	    try {
		d = p.execute(d, cur.getSpace());
	    } catch(ZZError e) {
		if(throwerrs) throw e;
		ZZLogger.exc(e, "Exception in Flowing Clang step. ");
		return frame;
	    }
	    frame.put(cur, d, +1);
	    frame.setPos(cur);
	    return frame;
	} else if(root != null) {		// FUNCTION CALL
	    frame.setPos(cur);
	    StackFrame called = frame.call();
	    called.put(root, d, -1);
	    called.setPos(root);
	    return called;
	} else {				// UNKNOWN
	    if(throwerrs)
		throw new ZZError("Not a primitive at " + cur.getID() + ": " +
				  cur.getText());
	    ZZLogger.log("Flowing Clang step cannot be executed: Not a " +
			 "primitive at " + cur.getID() + ": " + cur.getText());
	    return frame;
	}
    }
}

