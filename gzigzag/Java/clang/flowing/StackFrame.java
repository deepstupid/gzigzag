/*   
StackFrame.java
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

/** A stack frame for Flowing Clang.
 */

public abstract class StackFrame {
String rcsid = "$Id: StackFrame.java,v 1.5 2000/11/16 20:33:13 bfallenstein Exp $";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }

    // METHODS TO BE IMPLEMENTED BY SUBCLASSES

    /** Get the value associated with a cell. */
    public abstract Object getSingle(ZZCell c);

    /** Set the value associated with a cell.
     *  @param d	The Data object in which the value to be set is found.
     *  @param at	The value's position in the Data object.
     */
    public abstract void setSingle(ZZCell c, Data d, int at);

    /** Get position of instruction pointer. */
    public abstract ZZCell getPos();
    /** Set position of instruction pointer. */
    public abstract void setPos(ZZCell c);

    /** Create a lower-level stackframe. */
    public abstract StackFrame call();

    /** Return true if this stack frame is parent of given stack frame. */
    public abstract boolean parentof(StackFrame sf);

    /** Return to the next higher stackframe (disconnects this (lower) frame).
     *  Also sets instruction pointer to null, but keeps the values associated
     *  with cells, so that the returned data can be read. Call delete() when
     *  you're done with the values.
     */
    public abstract StackFrame ret();

    /** Delete the cells associated with the stackframe, if any.
     *  This renders the values of a structural stackframe inaccessible.
     */
    public void delete() {}

    // METHODS StackFrame PROVIDES

    private static final ZZCell[] reverse(ZZCell[] arr) {
	ZZCell[] res = new ZZCell[arr.length];
	for(int i=0; i<arr.length; i++) res[arr.length-i-1] = arr[i];
	return res;
    }

    public Data get(ZZCell c, int dir) {
	p("Start getting for "+c.getText()+" ("+c.getID()+")");
	ZZCell[] cells = c.readRank("d.1", dir, false);
	if(dir<0) cells = reverse(cells);
	Object[] result = new Object[cells.length];
	p("Getting "+cells.length+" values");
	for(int i=0; i<cells.length; i++) {
	    if(cells[i].s("d.clone", -1) != null) {
		p("Getting: " + cells[i].getText());
		result[i] = getSingle(cells[i].getRootclone());
		if(result[i] == null)
		    throw new ZZError("Value for " + cells[i].getText() +
				      " not yet set");
	    } else {
		p("Getting literal");
		result[i] = cells[i];
	    }
	    p("Got "+result[i]);
	}
	p("Finish getting");
	return new Data(result);
    }
		
    public void put(ZZCell c, Data params, int dir) {
	p("Start putting for "+c.getText()+" ("+c.getID()+")");
	Data pars = params != null ? params : new Data();
	ZZCell[] cells = c.readRank("d.1", dir, false);
	if(dir<0) cells = reverse(cells);
	p("Putting "+cells.length+" values");
	if(pars.len() < cells.length)
	    throw new ZZError("Pars: " + pars.len() +
			      " < cells: " + cells.length);
	for(int i=0; i<cells.length; i++) {
	    p("Putting: "+pars.s(i)+" into: "+cells[i].getText());
	    setSingle(cells[i].getRootclone(), pars, i);
	}
	p("Finish putting");
    }

}


