/*   
ZZCell.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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
/*
 * 	Well, he'd been in enough cells. There were ways to handle these 
 *	things. The important thing was to be direct. He got up and banged
 *	on the bars until the warder sauntered along the corridor.
 *		`Yes, mate?'
 *		`I just want to get things sorted out,' said Rincewind. `It's
 *	not as though I've got time to waste, okay?'
 *		`Yep?'
 *		`Is there any chance that you're going to fall asleep 
 *	in a chair opposite this cell with your keys fully exposed on a table
 *	in front of you?'
 *		- Terry Pratchett, The Last Continent, p.267
 */
package org.gzigzag;
import java.util.*;

/** An abstract class to represent a ZigZag cell.
 * <p>
 * There is no unique cell to Java object mapping, so there may be
 * several equivalent Java objects representing the same cell.
 * Use the <b>equals</b> method for comparisons.
 */

public abstract class ZZCell {
public static final String rcsid = "$Id: ZZCell.java,v 1.77 2001/04/18 23:38:28 bfallenstein Exp $";

    /** Obtain the unique ID for this cell.
     * Note that this method <I>may</I> throw an exception 
     * if the space this cell is in does not support cell IDs.
     */
    public String getID() {
	throw new NullPointerException("This cell type has no IDs!");
    }

    /** Create a new cell connected to this cell.
     * @param dim 	Dimension to create the cell along
     * @param dir	The direction, either positive or negative
     */
    public final ZZCell N(String dim, int dir) {
	return N(dim, dir, null, 0);
    }
    public final ZZCell N(String dim) {
	return N(dim, 1, null, 0);
    }

    /** Get a new cell associated with this cell (ie.same slice)
     */
    public abstract ZZCell N();
     
    /** Get or create a new cell connected to this cell.
     * A convenience function which first tries to use getNeighbour
     * and if that returns null, creates a new neighbour.
     * @param dim 	Dimension to create the cell along
     * @param dir	The direction, either positive or negative
     * @param obs	The observer
     */
    public ZZCell getOrNewCell(String dim, int dir, ZZObs o) {
	ZZCell r;
	if((r = s(dim, dir, o)) != null) return r;
	return N(dim, dir, o);
    }
    public ZZCell getOrNewCell(String dim, int dir) {
	return getOrNewCell(dim, dir, null);
    }
    public ZZCell getOrNewCell(String dim) {
	return getOrNewCell(dim, 1, null);
    }
    /** Create a new cell connected to this cell and attach an observer 
     * to it
     * atomically.
     * @param dim 	Dimension to create the cell along
     * @param dir	The direction, either positive or negative
     * @param obs	The observer
     */
    public final ZZCell N(String dim, int dir, ZZObs o) {
	return N(dim, dir, o, 0);
    }
    /** Create a new cell connected to this cell 
     * to it
     * atomically.
     * @param dim 	Dimension to create the cell along
     * @param dir	The direction, either positive or negative
     * @param flags	The flags to guide the type of the new cell
     */
    public final ZZCell N(String dim, int dir, long flags) {
	return N(dim, dir, null, flags);
    }
    /** Create a new cell connected to this cell and attach an observer 
     * to the connection
     * atomically.
     * <p>
     * NOTE: this routine MUST insert the cell, not just blindly
     * delete the former connection.
     * @param dim 	Dimension to create the cell along
     * @param dir	The direction, either positive or negative
     * @param obs	The observer
     * @param flags	The flags to guide the type of the new cell
     */
    public abstract ZZCell N(String dim, int dir, ZZObs o, long flags);


    /** Delete this cell. */
    public abstract void delete();
    /** Delete this cell from a rank along a dimension. */
    public void excise(String dim) {
     synchronized(getSpace()) {
	ZZCell cm = s(dim, -1);
	ZZCell cp = s(dim, 1);
	if(cm!=null && cp!=null) {
	    cm.disconnect(dim, 1);
	    cp.disconnect(dim, -1);
	    cm.connect(dim, cp);
	}	
	else if(cm!=null)
	    cm.disconnect(dim, 1);
	else if(cp!=null)
	    cp.disconnect(dim, -1);
     }
    }
    /** Connect this cell to another along a dimension. 
     * If there is a former connection,
     * a ZZConnectWouldBreakError is thrown.
     * @param dim	Dimension to connect along
     * @param to	Cell to connect to. This cell will end up poswards from us.
     */
    public abstract void connect(String dim, ZZCell to);
    /** Connect this cell to another along a dimension in a specified 
     * direction. 
     * If there is a former connection,
     * a ZZConnectWouldBreakError is thrown.
     * @param dim	Dimension to connect along
     * @param dir	The direction, either positive or negative
     * @param to	Cell to connect to.
     */
    public void connect(String dim, int dir, ZZCell to) {
	if(dir > 0) {
	    connect(dim, to);
	} else {
	    to.connect(dim, this);
	}
    }

    /** Insert a cell next to this one in the rank.
     * Both connections of the what cell along the specified dimension are
     * deleted before inserting it.
     * @param dim	Dimension to connect along
     * @param dir	The direction, either positive or negative
     * @param what	Cell to insert
     */
    public abstract void insert(String dim, int dir, ZZCell what);

    /** Disconnect a connection from this cell.
     * @param dim	Dimension to disconnect along
     * @param dir	The direction, either positive or negative
     */
    public abstract void disconnect(String dim, int dir);

    /** Set the text contained in the cell.
     * @param text	The new text
     */
    public abstract void setText(String text);

    /** Set the text contained in the cell to a pointer.
     * @param text	The new text, as a span pointing to a stable scroll
     */
    public abstract void setSpan(Span text);

    /** Get a reference to the ZZSpace object describing the whole space */
    public abstract ZZSpace getSpace();

    /** Find the neighbour of this cell along a dimension
     * attach an observer to this relation
     * atomically.
     * @param dim 	Dimension to go along
     * @param dir	The direction, either positive or negative
     * @param obs	The observer
     * @return 	The neighbour, or null if none.
     */
    public abstract ZZCell s(String dim, int dir, ZZObs o);

    /** Find the neighbour of this cell along a dimension.
     * atomically.
     * @param dim 	Dimension to go along
     * @param dir	The direction, either positive or negative
     * @return 	The neighbour, or null if none.
     */
    public ZZCell s(String dim, int dir) {
	return s(dim,dir,null);
    }

    /** Find the next cell along a dimension.
     * Always moves poswards.
     * @param dim 	Dimension to go along
     * @return 	The neighbour, or null if none.
     */
    public ZZCell s(String dim) {
	return s(dim,1,null);
    }

    /** Find the headcell of a rank.
     * XXX The default implementation does not handle circular ranks correctly!
     * @param dim 	Dimension to go along
     * @param dir	The direction, either positive or negative
     * @return 	The headcell
     */
    public ZZCell h(String dim, int dir) {
	return h(dim, dir, false, null);
    }
    public ZZCell h(String dim, int dir, ZZObs o) {
	return h(dim, dir, false, o);
    }
    public ZZCell h(String dim, boolean ensuremove) {
	return h(dim, -1, ensuremove, null);
    }
    public ZZCell h(String dim, int dir, boolean ensuremove) {
	return h(dim, dir, ensuremove, null);
    }
    /** Find the headcell of a rank.
     * The headcell is the most negative cell so this call is equivalent
     * to h(dim, -1).
     */
    public ZZCell h(String dim) {
	return h(dim, -1, false, null);
    }

    /** Find the headcell of a rank.
     * XXX The default implementation does not handle circular ranks correctly!
     * HOWEVER, all code must be prepared for this function to return a cell
     * that is just some cell in a looping rank. This means that a while loop
     * looking for null going poswards from the headcell might be infinite
     * and needs the user to explicitly detect loops.
     * Just in case of synch problems, it is recommended that the user check
     * both for equality to the headcell <em>and</em> use a 
     * LoopDetector, in case the loop changed.
     * @param dim 	Dimension to go along
     * @param dir	The direction, either positive or negative
     * @return 	The headcell
     */
    public abstract ZZCell h(String dim, int dir, boolean ensuremove,
	    ZZObs o);


    /** Count the length of the rank.
     * @param dim 	Dimension to go along
     * @return 	length of the rank.
     */
    public int getRankLength(String dim) {
	ZZCell cur = this;
	ZZCell next;
	int i = 0;
	while((next=cur.s(dim,1))!=null) {
	    
	    if(next == this)
		throw new ZZInfiniteLoop("CIRCULAR HEAD");
	    cur = next;
	    i++;
	}
	cur = this;
	while((next=cur.s(dim,-1))!=null) {
	    
	    if(next == this)
		throw new ZZInfiniteLoop("CIRCULAR HEAD");
	    cur = next;
	    i++;
	}
	return i+1;
    }
    
    
    /** Find the next intersection of two ranks.
     * XXX The default implementation does not handle circular ranks correctly!
     * @param dim 	Dimension to go along from this cell
     * @param dir	The direction, either positive or negative, from this cell
     * @param cell2 The other cell
     * @param dim2 	Dimension to go along from the other cell
     * @param dir2	The direction, either positive or negative, from the other cell
     */
    public ZZCell intersect(String dim, int dir, ZZCell cell2, String dim2, int dir2) {
	synchronized(getSpace()) {
	    // If we knew how to find the shorter one...
	Hashtable h = new Hashtable();
	ZZCell cur = this;
	while(cur!=null) {
		h.put(cur,cur);
		cur = cur.s(dim,dir);
	}
	cur = cell2;
	while(cur!=null) {
		if(h.get(cur) != null) {
			return cur;
		}
		cur = cur.s(dim2,dir2);
	}
	return null;
     }
    }

    /** Find all intersections of two ranks.
     * XXX The default implementation does not handle circular ranks correctly!
     * @param dim 	Dimension to go along from this cell
     * @param dir	The direction, either positive or negative, from this cell
     * @param cell2 The other cell
     * @param dim2 	Dimension to go along from the other cell
     * @param dir2	The direction, either positive or negative, from the other cell
     */
    public ZZCell[] intersectAll(String dim, int dir, ZZCell cell2, String dim2, int dir2) {
     synchronized(getSpace()) {
	// If we knew how to find the shorter one...
	Hashtable h = new Hashtable();
	ZZCell cur = this;
	while(cur!=null) {
		h.put(cur,cur);
		cur = cur.s(dim,dir);
	}
	Vector v = new Vector();
	cur = cell2;
	while(cur!=null) {
		if(h.get(cur) != null) {
			v.addElement(cur);
		}
		cur = cur.s(dim2,dir2);
	}
	ZZCell[] r = new ZZCell[v.size()];
	for(int i=0; i<r.length; i++) {
		r[i] = (ZZCell)v.elementAt(i);
	}
	return r;
     }
    }

    /** Returns the text contained in the cell.
     * A null return means that the cell has been deleted.
     */
    public String getText() { return getText(null); }
    public abstract String getText(ZZObs o);

    /** Returns the stable media reference in the cell.
     * A null return means either that the cell has been deleted
     * or that the text inside is not a reference.
     * @param lang	The requested language.
     */
    public Span getSpan() { return getSpan(null); }
    public abstract Span getSpan(ZZObs o);

    /** Returns the names of the dimensions on which this cell
     * is connected. XXX May be subject to change.
     */
    public abstract String[] getRankNames();

    /** Returns an array containing all connections leaving this cell. */
    public ZZConnection[] getConns()
    {
	String[] dims = getSpace().dims();
	int dn = dims.length;

	Vector conns = new Vector();
	ZZCell c;

	for(int di = 0;di<dn;di++) {
		for(int d=-1;d<=1;d+=2) {
			c = s(dims[di], d);
			if(c!=null)
				conns.addElement(new ZZConnection(this, dims[di], d, c));
		}
	}

        ZZConnection[] ret = new ZZConnection[conns.size()];
        for(int i = 0;i<conns.size();i++)
                ret[i] = (ZZConnection)conns.elementAt(i);
        return ret;
//JDK1.2 return (ZZConnection[])(conns.toArray(new ZZConnection[conns.size()]));
    }


    /** Tests whether this cell is the same as another cell.
     * There may be more than one Java object representing the same cell
     * so it is important to use this method instead of the '==' operator
     * to test for equality.
     */
    public abstract boolean equals(Object c);
    public abstract int hashCode();

    /** Reads a rankful of cells and returns an array.
     * @param dim	The dimension along which to read the rank
     * @param dir	The direction in which to read the rank.
     * @param includeThis	Whether to include this 
     * A convenience function to allow the developer to grab
     * a whole rank in an array, starting at the current cell. 
     * Thus, in a rank A-B-C-D-E, calling C.readRank(d.1, 1, false)
     * yields D-E, and so on.
     */
    public ZZCell[] readRank(String dim, int dir, boolean includeThis) {
	return readRank(dim, dir, includeThis, null);
    }

    /** Reads a rankful of cells and returns an array.
     * @param dim	The dimension along which to read the rank
     * @param dir	The direction in which to read the rank.
     * @param includeThis	Whether to include this 
     * @param o	An observer to attach to all the cells
     * A convenience function to allow the developer to grab
     * a whole rank in an array, starting at the current cell. 
     * Thus, in a rank A-B-C-D-E, calling C.readRank(d.1, 1, false)
     * yields D-E, and so on.
     */
    public ZZCell[] readRank(String dim, int dir, boolean includeThis,
	    ZZObs o) {
	synchronized(getSpace()) {
	    LoopDetector l = new LoopDetector();
	    ZZCell cur = this;
	    Vector v = new Vector();
	    if(!includeThis) {
		cur = cur.s(dim, dir, o);
	    }
	    while(cur != null) {
		v.addElement(cur);
		l.detect(cur);
		cur = cur.s(dim, dir, o);
	    }

	    ZZCell[] r = new ZZCell[v.size()];
	    for(int i=0; i<r.length; i++)
		r[i] = (ZZCell)v.elementAt(i);
	    return r;
	}
    }

    /** Reads the headcells of a given rank along another
     * dimension.
     * @param dim	The dimension along which to read the rank
     * @param dir	The direction in which to read the rank.
     * @param includeThis	Whether to include this 
     * @param dimh	The dimension along which to read the headcells.
     * @param dirh	The direction in which to read the headcells..
     * @param o	An observer to attach to all the cells
     * A convenience function to grab the headcells of a rank
     * in an array. Useful for relcells.
     */
    public ZZCell[] readRankHeadcells(String dim, int dir, boolean 
	    includeThis, String dimh, int dirh, ZZObs o) {
    synchronized(getSpace()) {
	ZZCell[] r = readRank(dim, dir, includeThis, o);
	for(int i=0; i<r.length; i++)
		r[i] = r[i].getHeadcell(dimh, dirh, o);
	return r;
    }
    }

    /** Hops the cell along the dimension.
     * This means that all other connections remain the
     * same but that this cell changes places in the
     * rank given by the dimension.
     * This operation may be given interesting meanings when e.g.
     * hopping over the end of a rank.
     * @param dim	The dimension to hop along
     * @param count	The number of places to hop. May be negative,
     *			in which case the cell will hop negwards.
     */
    public abstract void hop(String dim, int count);
    // {
    // synchronized(getSpace()) {
// 	ZZCell n = s(dim, dir);
// 	if(n==null) return;
// 	n.insert(dim, dir, this);
//     }
//     }

    /** Obtain a global ID for this cell.
     * Global IDs will likely play an important rule in the future
     * but currently they have no specific use.
     * <p>
     * Is this the correct implementation?
     */
    public String getGlobalID() {
        if (getID().indexOf("@") != -1) return getID();
	return getID()+"@"+getSpace().getID();
    }

    /** Find a cell with the given contents.
     */
    public ZZCell findText(String dim, int dir, String txt) {
	return findText(dim, dir, txt, null);
    }
    public ZZCell findText(String dim, int dir, String txt, ZZObs o) {
	ZZCell cur = s(dim, dir);
	while(cur!=null && cur!=this && !cur.getText(o).equals(txt)) {
		cur = cur.s(dim,dir,o);
	}
	if(cur==this) return null;
	return cur;
    }

    /** Whether another cell is on the given rank in the given dir.
     */
    public boolean findCell(String dim, int dir, ZZCell c) {
	if(this.equals(c)) return true;
	ZZCell i = this.s(dim, dir);
	while(i != null && !i.equals(c) && !i.equals(this)) i = i.s(dim, dir);
	if(i==null) return false;
	return i.equals(c);
    }


    public ZZCell getHomeCell() {
	return getSpace().getHomeCell();
    }


    public ZZCell zzclone() {
	return getHeadcell("d.clone", 1).newCell("d.clone", 1);
    }

    public ZZCell getRootclone() {
	return getHeadcell("d.clone", -1);
    }

    // This may become the new API of choice..
    public final String t() { return getText(); }

    /** Return a stringized form of this cell.
     * This form is unstable and its use for anything except
     * human-readable debugging is not recommended.
     * Currently the format is: The cell ID in single quotes
     * and the text content after that, in parentheses,
     * for example <code>'53' (Text in cell)</code>.
     */
    public String toString() {
	return "'"+getID() + "' (" + getText() + ")";
    }


    /** Enumerate a (semi)rank.
     * @param dim The dimension along which the (semi)rank is to be enumerated.
     * @return An enumeration of all cells starting from this cell
     * along dim in positive direction.
    */
    public Enumeration enumRank(String dim) {
        return enumRank(dim, 1);
    }

    /** Enumerate a (semi)rank.
     * @param dim The dimension along which the (semi)rank is to be enumerated.
     * @param dir The direction along dim to enumerate.
     * @return An enumeration of all cells starting from this cell
     * along dim in direction dir.
    */
    public Enumeration enumRank(String dim, int dir) {
        class RankEnumeration implements Enumeration {
            ZZCell c;
            String dimension;
            int direction;
            public RankEnumeration(ZZCell c, String dimension, int direction) {
                this.c = c;
                this.dimension = dimension;
                this.direction = direction;
            }
            public boolean hasMoreElements() {
                return c != null;
            }

            public Object nextElement() {
                if (!hasMoreElements()) throw new NoSuchElementException("");
                ZZCell r = c;
                c = c.s(dimension, direction);
                return r;
            }
        };
        return new RankEnumeration(this, dim, dir);
    }

    // Deprecated, old versions of the calls

    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell newCell(String dim, int dir, long flags) {
	return N(dim, dir, flags);
    }
    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell newCell(String dim, int dir, ZZObs o) {
	return N(dim, dir, o);
    }
    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell newCell(String dim, int dir) {
	return N(dim, dir);
    }
    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell newCell() {
	return N();
    }

    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell getNeighbour(String dim, int dir, ZZObs o) {
	return s(dim, dir, o);
    }
    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell getNeighbour(String dim, int dir) {
	return s(dim, dir);
    }
    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell getSteps(String dim, int dir, ZZObs o) {
	return s(dim, dir, o);
    }
    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell getSteps(String dim, int dir) {
	return s(dim, dir);
    }

    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell getHeadcell(String dim, int dir, boolean ensuremove,
	    ZZObs o) {
	return h(dim, dir, ensuremove, o);
    }
    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell getHeadcell(String dim, int dir) {
	return h(dim, dir);
    }
    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell getHeadcell(String dim, int dir, ZZObs o) {
	return h(dim, dir, o);
    }
    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell getHeadcell(String dim, int dir, boolean ensuremove) {
	return h(dim, dir, ensuremove);
    }
    /** Deprecated.
     * @deprecated Use the new one-letter functions instead.
     */
    public final ZZCell getHeadcell(String dim) {
	return h(dim);
    }


    // Deprecated stuff.

    /** A flag passed to newCell meaning that the cell should not be stored
     * persistently. This may enable some optimizations and
     * save some persistent and/or temporary memory space.
     * For example, cells that track which cells are shown in a view
     * through a raster should probably be transient.
     * <p>
     * Note that not all spaces support transient cells so this is
     * only a hint in that context. In getFlags, the meaning is real.
     * @deprecated Usefulness being evaluated
     */
    long FLAG_TRANSIENT = 0x00000001;
    /** A flag meaning that the text stored in this cell is
     * an address.
     * The method getText() will automatically 
     * return the text stored in the cell, whether it is just text
     * or a span of text from a permascroll.
     * If you want the span, use
     * getSpan().
     * For users, a much better way to tell whether a cell is a span is
     * simply <pre> c.getSpan() != null </pre>.
     * @deprecated Usefulness being evaluated
     */
    long FLAG_ADDRESS =   0x00000002;
    /** A flag meaning that the current cell is used only as a label.
     * XXX Think about it a bit more - which operations shall it affect?
     * @deprecated Usefulness being evaluated
     */
    long FLAG_LABEL =   0x00000004;

    /** Get the flags associated with this cell.
     * @deprecated Usefulness being evaluated
     */
    public long getFlags() { return 0; }
    /** Set the flags associated with this cell.
     * @deprecated Usefulness being evaluated
     */
    public void setFlags( long s, long u ) { }

}

