/*   
ZZClang1.java
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
package org.gzigzag.clang;
import org.gzigzag.*;
import java.util.*;

/** A primitive test version of Clang.
 * NOT YET FINISHED! 
 * <p>
 * XXX - make use stack frames in the structure 
 */

public class ZZClang1 implements ZZClangContext /* , ZZClangOp */ {
public static final String rcsid = "$Id: ZZClang1.java,v 1.5 2000/10/18 14:35:31 tjl Exp $";
	public static final boolean dbg = true;
	static final void p(String s) { if(dbg) System.out.println(s); }

	Hashtable specials;
	Hashtable symtab;
	ZZCursor cursor;
	ZZCell viewspex;

	/** Execute a script in response to user action.
	 * This means that the language can have special key words to
	 * pass as parameters the key as a cell with a span etc.
	 */
	synchronized public void execUserAction(ZZCell code, ZZCell viewspex,
		ZZCell clicked, String key) {
	    try {
		specials = new Hashtable();
		if(key!=null) specials.put("UserAction.keyString", key);
		if(clicked!=null) 
		    specials.put("UserAction.clickedCell", clicked);
		execEnv(code, viewspex);
	    } finally {
		specials = null;
		
	    }
	}

	/** Execute a script in a fresh environment.
	 */
	synchronized public void execEnv(ZZCell code, ZZCell viewspex2) {
	    symtab = new Hashtable();
	    viewspex = viewspex2;
	    ZZCursor realcursor = new ZZCursorReal(viewspex);
	    // Speed & space hack
	    cursor = new ZZCursorVirtual(realcursor.get());

	    exec(code);

	    realcursor.set(cursor.get());
	}

	/** Execute starting from a cell.
	 * Should not be used from outside except in special cases.
	 */
	synchronized public void exec(ZZCell code) {
	    p("CLANG exec "+code);

	    for(ZZCell current = code;
		current != null; 
		current = current.s("d.2", 1)) {
		// Find op.
		ZZClangOp op = (ZZClangOp)funcs.get(current.getText());
		p("OP: op");
		if(op==null)
		    throw new ZZError("Unknown op '"+current.getText()+ "'");
		op.exec(current, cursor, this);
	    }

	}

/*
	// XXX allow VAR for local variables!
	public void exec(ZZCell c, ZZCell it, ZZCell[] params) {

		// First, read the prototype and set the params
		ZZCell[] p = c.readRank("d.1", 1, false);
		if(p.length > params.length)
			throw new ZZError("Insufficient params");
		for(int i=0; i<p.length; i++) {
			h.put(p[i].h("d.clone", -1), params[i]);
		}
		p("Call real exec");
		exec(c.s("d.2", 1), ctxt, h);
	}
    */

	Hashtable coords = new Hashtable();
	{
	    coords.put("X", new Integer(1));
	    coords.put("Y", new Integer(2));
	    coords.put("Z", new Integer(3));
	    coords.put("W", new Integer(4));
	    coords.put("HOME", new Integer(-1));
	}

	// Implementation of ZZClangContext interface.

	public ZZCursor getCursor() { return cursor; }
	public ZZCell getViewspex() { return viewspex; }
	public Object getSpecial(Object param) { return specials.get(param); }

// XXX Use something like this to get other params as cells / cursors!
/*
			if(par==null) {
			    String s = p[i].getText();
			    Integer ind = (Integer)coords.get(s);
			    if(ind!=null) {
				if(ind.intValue()==-1) 
				    par = p[i].getSpace().getHomeCell();
				else {
				    par = ctxt.getDim(ind.intValue()).get();
				}
			    }
			}
    */

	public ZZCell paramAsCell(ZZCell var) {
	    ZZCell par = 
		    (ZZCell)symtab.get(
			    var.h("d.clone",-1));
	    if(par==null) return var;
	    ZZCell res = ZZCursorReal.get(par);

	    // If it has an expression on d.2, execute.
	    ZZCell oc = var.s("d.2", 1);
	    if(oc==null)
		return res;

	    // save cursor
	    ZZCursor tmp = cursor;
	    try {
		cursor = new ZZCursorVirtual(res);
		exec(oc);
		res = cursor.get();
	    } finally {
		cursor = tmp;
	    }
	    return res;
	}
	public ZZCursor paramAsCursor(ZZCell var) {
	    ZZCell par = 
		    (ZZCell)symtab.get(
			    var.h("d.clone",-1));
	    if(par==null) return null;
	    ZZCell oc = var.s("d.2", 1);
	    if(oc != null) 
		throw new ZZError("Can't have a cursor with an expression");
	    return new ZZCursorReal(par);
	}
	public void execParam(ZZCell param) {
	    exec(param);
	}


	Hashtable funcs = new Hashtable();
	/** WILL BE DEPRECATED. */
	public void addOp(ZZClangOp op) {
		p("Addop "+op.name());
		funcs.put(op.name(), op);
	}

	// public void 
}
