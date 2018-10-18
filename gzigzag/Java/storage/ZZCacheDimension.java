/*   
ZZCacheDimension.java
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
package org.gzigzag;
import java.util.*;

/** Dimension-centric implementation of a locally 
 * cached dimension. 
 * This class may be used as a cache to
 * a dimension stored on disk, and includes facilities for tracking
 * the changes.
 * XXX Look into replacing hashtable with a more memory-saving constructions?
 * <p>
 * This, uglily, shares some code with ZZLocalDimension but making
 * this an inheritance relationship would have been difficult, and
 * the point is to keep ZZLocalDimension a truly simple basic
 * example implementation of a dimension.
 * <p>
 * The mechanism for the undolist is quite interesting:
 * we save each connect / disconnect operation with the two cells,
 * so we know the inverse of each operation. 
 * We can then, at commit time, build a hash table by taking the first
 * operation and seeing if it cancels an earlier operation (undolist does
 * commits in reverse order) and if it does, we do nothing. 
 * This allows the user to move the cursor without wasting storage on all
 * positions.
 * Interestingly, although we store all connections bidirectionally as 
 * connect/disconnect pairs in the undolist, we cannot save them like that
 * after compression: they must be saved as single-sided disconnects and
 * pair connects.
 */
public class ZZCacheDimension extends ZZDimension implements ZZDimStorer {
public static final String rcsid = "$Id: ZZCacheDimension.java,v 1.11 2001/02/23 08:33:37 ajk Exp $";
    public static final boolean dbg = true;
    final static void p(String s) { if(dbg) System.out.println(s); }
    final static void pa(String s) { System.out.println(s); }

    UndoList undo;
    public void setUndoList(UndoList l) { undo = l; }

    protected boolean readonly;

    public ZZCacheDimension() { this(false); }
    public ZZCacheDimension(boolean readonly) {
        this.readonly = readonly;
    }

    /** Connections to the positive direction. */
    Hashtable cp = new Hashtable();
    /** Connections to the negative direction. */
    Hashtable cm = new Hashtable();

    /** The earlier connection on each cell's poswards or negwards
     * direction at commit time.
     * This list is created at commit time by reversing all operations
     * so we can then compare these values and the current values to
     * see which have changed.
     * Hmm.. do we really even need comm?
     */
    Hashtable comp;
    Hashtable comm;

    /** A null marker for the hashtables. */
    static final Object nolla = new Object();

    synchronized public void startCommit() {
        if (readonly) throw new ZZError("readonly dimension");
	comp = new Hashtable();
	comm = new Hashtable();
    }

    synchronized public void endCommit(ZZDimStorer sto) {
        if (readonly) throw new ZZError("readonly dimension");
	for(Enumeration e = comp.keys(); 
	    e.hasMoreElements();) {
	    String id = (String)e.nextElement();
	    Object oidto = comp.get(id);
	    String idto = (oidto == nolla ? null : (String)oidto);
	    String idwasto = (String)cp.get(id);
	    // If it's the same, then nevermind.
	    if(idto == idwasto || (
		idto != null && idto.equals(idwasto))) 
		    continue;
	    if(idwasto != null)
		sto.storeConnect(id, idwasto);
	    else 
		sto.storeDisconnect(id, 1);
	    
	}
	for(Enumeration e = comm.keys(); 
	    e.hasMoreElements();) {
	    String id = (String)e.nextElement();
	    Object oidto = comm.get(id);
	    String idto = (oidto == nolla ? null : (String)oidto);
	    String idwasto = (String)cm.get(id);
	    // If it's the same, then nevermind.
	    if(idto == idwasto || (
		idto != null && idto.equals(idwasto))) 
		    continue;
	    if(idwasto == null)
		sto.storeDisconnect(id, -1);
	    // Not here: we only call connect once.
	    // else 
	    // 	sto.connect(id, -1, idto);
	    
	}
	comp = null;
	comm = null;
    }

    ZZCellHandle ch(Object o) {
	if(o instanceof ZZCellHandle) return (ZZCellHandle)o;
	return (ZZCellHandle)space.getCellByID((String)o);
    }
    String cid(Object o) {
	if(o instanceof ZZCellHandle) return ((ZZCellHandle)o).id;
	return (String)o;
    }

    /** The abstracted operation of connecting two cells.
     * Parameters: cell1 cell2.
     * The cells are explicitly assumed to be not connected before
     * this.
     */
    protected class Connect implements UndoList.Op {
	public void undo(Object[] list, int nth) {
	    String c1 = (String)list[nth+1];
	    String c2 = (String)list[nth+2];
	    if(!s((ZZCellHandle)space.getCellByID(c1), 1, null).id.equals(c2))
		throw new ZZError(
		  "Error when undoing change: disconnect assertion broken");
	    cp.remove(c1);
	    cm.remove(c2);
	    triggers.chg(c1);
	    triggers.chg(c2);
	}
	public void redo(Object[] list, int nth) {
	    String c1 = (String)list[nth+1];
	    String c2 = (String)list[nth+2];
	    if(cp.get(c1) != null ||
	       cm.get(c2) != null)
		throw new ZZError(
		   "Error when redoing change: unconnected assertion broken");
	    cp.put(c1, c2);
	    cm.put(c2, c1);
	    triggers.chg(c1);
	    triggers.chg(c2);
	}
	public void commit(Object[] list, int nth) {
	    String c1 = (String)list[nth+1];
	    String c2 = (String)list[nth+2];
	    comp.put(c1, nolla);
	    comm.put(c2, nolla);
	}
    }


    /** The abstracted operation of disconnecting two cells.
     * Parameters: cell1 cell2.
     */
    protected class Disconnect implements UndoList.Op {
	public void undo(Object[] list, int nth) {
	    String c1 = (String)list[nth+1];
	    String c2 = (String)list[nth+2];
	    // Note we can't use disconnect() since that would make
	    // new undolist entries.
	    if(cp.get(c1) != null ||
	       cm.get(c2) != null)
		throw new ZZError(
		   "Error when undoing change: unconnected assertion broken");
	    cp.put(c1, c2);
	    cm.put(c2, c1);
	    triggers.chg(c1);
	    triggers.chg(c2);
	}
	public void redo(Object[] list, int nth) {
	    String c1 = (String)list[nth+1];
	    String c2 = (String)list[nth+2];
	    if(!s((ZZCellHandle)space.getCellByID(c1), 1, null).id.equals(c2))
		throw new ZZError(
		  "Error when redoing change: disconnect assertion broken");
	    cp.remove(c1);
	    cm.remove(c2);
	    triggers.chg(c1);
	    triggers.chg(c2);
	}
	public void commit(Object[] list, int nth) {
	    String c1 = (String)list[nth+1];
	    String c2 = (String)list[nth+2];
	    comp.put(c1, c2);
	    comm.put(c2, c1);
	}
    }

    Connect myconnect = new Connect();
    Disconnect mydisconnect = new Disconnect();

    public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
	String s = c.id;
	if(o != null) triggers.addObs(s, o);
	if(steps>0)
	    while(steps-- > 0 && s != null) {
		s = (String)cp.get(s);
		if(o != null && s!=null) triggers.addObs(s, o);
	    }
	else
	    while(steps++ < 0 && s != null) {
		s = (String)cm.get(s);
		if(o != null && s!=null) triggers.addObs(s, o);
	    }
	if(s==null) return null;
	return (ZZCellHandle)space.getCellByID(s);
    }

    public void connect(ZZCellHandle c0, ZZCellHandle d0) {
        if (readonly) throw new ZZError("readonly dimension");
	String c = c0.id;
	String d = d0.id;
	disconnect(c0, 1);
	disconnect(d0, -1);
	cp.put(c, d);
	cm.put(d, c);
	undo.add(myconnect, c, d);
	triggers.chg(c);
	triggers.chg(d);
    }

    public void disconnect(ZZCellHandle c0, int dir) {
        if (readonly) throw new ZZError("readonly dimension");
	String c = c0.id;
	String o;
	if(dir>0) {
	    o = (String)cp.get(c);
	    if(o!=null) {
		    undo.add(mydisconnect, c, o);
		    cp.remove(c);
		    cm.remove(o);
	    }
	} else {
	    o = (String)cm.get(c);
	    if(o!=null) {
		    undo.add(mydisconnect, o, c);
		    cm.remove(c);
		    cp.remove(o);
	    }
	}
	triggers.chg(c);
	if(o!=null) triggers.chg(o);
    }

    // ZZDimStorer implementation

    public void storeConnect(String a, String b) {
	cp.put(a, b);
	cm.put(b, a);
    }
    public void storeDisconnect(String a, int dir) {
	Hashtable h = (dir > 0 ? cp : cm);
	h.remove(a);
    }
    
}
