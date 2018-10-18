/*   
UndoList.java
 *    
 *    Copyright (c) 2000-2002, Ted Nelson and Tuomas Lukka
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

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.util.*;

/** An operation on the write-cache list that can be undone
 * or committed.
 * At commit time, the operations can be given to a GZZ1Cache that merges
 * them before writing them to the disk.
 * <p>
 * The operations are cleverly stored to minimize memory use: 
 * an array of Object refs, some of which are operations and after
 * them their parameters. The parameters are not stored in the operation
 * objects in order to avoid creating a new object instance per undoable
 * operation.
 * <p>
 * NOTE: this means that a parameter to an operation CANNOT IMPLEMENT
 * UndoList.Op itself! That would break the chain. It would need to be
 * e.g. wrapped into an array.
 * <p>
 * So the orders are: undo in reverse, redo and commit in original order.
 */

public class UndoList {
String rcsid = "$Id: UndoList.java,v 1.3 2002/03/27 08:32:55 bfallenstein Exp $";
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }
    final static void pa(String s) { System.out.println(s); }

    /** A GID-maker.
     *  Creates GIDs acceptable by a GZZ1Handler from Cells or String ids.
     *  <p>
     *  Has to convert temporary to permanent IDs.
     */
    static public abstract class GIDMaker {
	public abstract byte[] make(String s);
	public byte[] make(Cell c) {
	    return make(c.id);
	}
    }

    /** An undoable operation.
     * Classes implementing this class can either be real
     * operations or simply classes of operations that take
     * their parameters from the Object array, in order
     * to save space and use only one instance of the Op object.
     * @see UndoList
     * @see ZZCacheDimension
     */
    public interface Op {
	/** Undo the operation.
	 */
	void undo(Object[] list, int nth);
	/** Redo the operation.
	 */
	void redo(Object[] list, int nth);
	/** Commit the operation.
	 * The operation need not be undoable after a commit.
	 */
	void commit(Object[] list, int nth, GIDMaker gid, GZZ1Handler hdl) throws java.io.IOException;
    }

    Object[] ops = new Object[2000];
    int nops = 0;

    int stamp0;

    int[] stamps = new int[64];
    int nstamps = 0;

    int nundoes = 0;

    boolean active = true;

    UndoList(int inittime) {
	stamp0 = inittime;
	makeStampRoom(1);
	stamps[nstamps++] = 0;
    }

    /** Place a timestamp into the operation list.
     * Undo and redo work on stamped boundaries.
     * If no operations since last stamp, returns last stamp.
     */
    public int stamp() {
	p("STAMP!");
	if(nundoes != 0) return -1;
	if(stamps[nstamps-1] == nops)
	    return stamp0 + nstamps-1;
	makeStampRoom(1);
	stamps[nstamps++] = nops;
	return stamp0 + nstamps-1;
    }

    /** Commit the changes up to the present.
     * Returns the new timestamp.
     */
    public int commit(GIDMaker gid, GZZ1Handler hdl) throws java.io.IOException {
	finalizeUndo();

	int stampno = stamp();
	for(int i=0; i<nops; i++) {
	    if(ops[i] instanceof Op)
		((Op)ops[i]).commit(ops, i, gid, hdl);
	}
	
	nops = 0;
	nstamps = 0;
	nundoes = 0;
	stamp0 = stampno;
	stamps[nstamps++] = 0;
	return stampno;
    }

    /** Undo operations until the previous timestamp, if possible.
     * After committing, undo won't work until you stamp again.
     */
    public void undo() {
	try {
	    int curop = stamps[nstamps-1 - nundoes];
	    p("Undo: "+curop+" "+nstamps+" "+nundoes);
	    // Nothing to do.
	    if(curop == 0) {
		ZZLogger.log("Can't undo further");
		return; 
	    }
	    int prevop = stamps[nstamps-1 - nundoes - 1];
	    for(int i = curop - 1; i >= prevop; i--) {
		if(ops[i] instanceof Op) {
		    p("Undo: "+i+" "+ops[i]);
		    ((Op)ops[i]).undo(ops, i);
		}
	    }
	    nundoes += 1;
	} catch(Exception e) {
            ZZLogger.exc(e);
	    throw new ZZError("Exception while undoing");
	}
    }

    /** Redo operations until the next timestamp, after undo has been
     * used. Any changes meanwhile make this impossible (branches later??).
     */
    public void redo() {
	if(nundoes <= 0) {
	    ZZLogger.log("Nothing to redo");
	    return;
	}
	try {
	    int curop = stamps[nstamps-1 - nundoes];
	    int nextop = stamps[nstamps-1 - nundoes + 1];
	    for(int i = curop; i < nextop; i++) {
		if(ops[i] instanceof Op) {
		    p("Undo op: "+i);
		    ((Op)ops[i]).redo(ops, i);
		}
	    }
	    nundoes -= 1;
	} catch(Exception e) {
	    throw new ZZError("Exception while undoing");
	}

    }

    /** Finalize the undo: the system will not be able to redo changes 
     * that happened after this.
     */
    public void finalizeUndo() {
	if(nundoes == 0) return;
	p("FINALIZE UNDO! "+nundoes);
	nops = stamps[nstamps-1 - nundoes];
	nstamps -= nundoes;
	nundoes = 0;
    }

    protected final void makeRoom(int n) {
	if(nops + n >= ops.length) {
	    Object[] opsnew = new Object[ops.length * 2];
	    System.arraycopy(ops, 0, opsnew, 0, nops);
	    ops = opsnew;
	}
    }

    public void setActive(boolean active) {
	this.active = active;
    }

    public boolean getActive() {
	return this.active;
    }

    protected final void makeStampRoom(int n) {
	if(nstamps + n >= stamps.length) {
	    int[] stampsnew = new int[stamps.length * 2];
	    System.arraycopy(stamps, 0, stampsnew, 0, nstamps);
	    stamps = stampsnew;
	}
    }

    public void add(Op o1, Object o2, Object o3) {
	if(!active) return;
	if(nundoes != 0) finalizeUndo();
	makeRoom(3);
	int i = nops;
	ops[nops++] = o1;
	ops[nops++] = o2;
	ops[nops++] = o3;
	//p("Add 3op: "+i+" "+o1+" "+o2+" "+o3);
    }
    public void add(Op o1, Object o2, Object o3, Object o4) {
	if(!active) return;
	if(nundoes != 0) finalizeUndo();
	makeRoom(4);
	int i = nops;
	ops[nops++] = o1;
	ops[nops++] = o2;
	ops[nops++] = o3;
	ops[nops++] = o4;
	//p("Add 4op: "+i+" "+o1+" "+o2+" "+o3+" "+o4);
    }

}
