/*   
AbstractDim.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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

/** Abstract implementation of <code>Dim</code> with standard implementations
 *  for most methods.
 * Some of the basic routines are given here so that they may be optimized.
 * The absolute core routines that every subclass must implement 
 * are the abstract routines <b>s, connect and disconnect</b>.
 * There are also other routines which may be optimizable but they
 * should be implemented as separate classes.
 *
 * <p>
 * Note that some of the methods are defined using each other; 
 * to avoid infinite loops (a la java.awt.FontMetrics) you have
 * to implement at least one s and one h.
 * @see TestSimpleDim
 */

public abstract class AbstractDim implements Dim {
public static final String rcsid = "$Id: AbstractDim.java,v 1.4 2001/09/20 17:52:57 tjl Exp $";

    /** The space this dimension is affiliated with. */
    public final Space space;

    public AbstractDim(Space space) {
	this.space = space;
    }

    public final Space getSpace() {
	return space;
    }

    public final Cell getCell(String id) {
	return space.getCell(id);
    }

    public abstract Cell s(Cell c, int steps, Obs o);
    public abstract void connect(Cell c, Cell d) 
	throws ZZAlreadyConnectedException;
    public abstract void disconnect(Cell c, int dir);

    /** Get the set of cell ids connected poswards on this dimension.
     *  Only the 'real' cells which should be saved to the file, merged etc.,
     *  not connections inferred by the computer.
     *  <p>
     *  The dimension <strong>must</strong> add cell ids, i.e.
     *  <code>String</code>s, <strong>not</strong> <code>Cell</code>s.
     *  <p>
     *  XXX doc better!
     */
    public abstract void addRealNegSides(Set set);


    /** The rest of the operations from here can be overridden
     * for optimizations, but do it is not required: the above operations
     * are all that is really needed.
     */

    /** Connect two cells in direction given.
     * This is trivially implemented by this class and cannot
     * be overridden.
     */
    public final void connect(Cell c, int dir, Cell d) 
		throws ZZAlreadyConnectedException {
	    if(dir<0) 
		    connect(d,c);
	    else
		    connect(c,d);
    }

    public Cell s(Cell c, int steps) {
	return s(c, steps, null);
    }

    public Cell s(Cell c) {
	return s(c, 1, null);
    }

    public abstract boolean isCircularHead(Cell c, Obs o);

    /** Get headcell.
     * This function <em>must</em> return a cell always, even with circular
     * ranks.
     * In case of a circular rank, picking a cell depends on dimension.
     * Ted recommends that it be user-pickable, especially for
     * normal (user-modifiable) dimensions.
     */
    public Cell h(Cell c, int dir, Obs o) {
	Cell orig = c;
	Cell prev = c;
	while((c=s(c, dir, o))!=null) {
                prev = c;
		if(c.equals(orig)) {
		    do {
			c = s(c, dir, null);
			if(isCircularHead(c, o))
			   return c;
		    } while(!c.equals(orig));
		    throw new ZZError("circular rank without head: "+orig);
		}
	}
	return prev;
    }

    public Cell h(Cell c, int dir) {
	return h(c, dir, null);
    }

    public Cell h(Cell c) {
	return h(c, -1, null);
    }


    public void insert(Cell c, int dir, Cell d) 
	throws ZZAlreadyConnectedException {
        if(s(d) != null)
            throw new ZZAlreadyConnectedException(d+", poswards to "+s(d));
        if(s(d, -1) != null)
            throw new ZZAlreadyConnectedException(d+", negwards to "+s(d, -1));

	Cell o = s(c, dir);
	if(o!=null)
	    disconnect(c, dir);
	
	connect(c,dir,d);
	if(o!=null)
	    connect(d,dir,o);
    }

    /** Remove the given cell from this dimension.
     */
    public void excise(Cell c) {
	Cell p = s(c, 1);
	Cell m = s(c, -1);
	disconnect(c, 1);
	disconnect(c, -1);

	if(p!=null && m!=null)
	    connect(m, p);
    }

    public void hop(Cell c, int steps) {
	if(steps == 0) return;
	Cell n = s(c, steps);
	if(n == null ) return;
	excise(c);
	try {
	    insert(n, (steps > 0 ? 1 : -1), c);
	} catch(ZZAlreadyConnectedException e) {
	    throw new ZZError("Something REALLY weird happened." + e);
	}
    }

    public void insertRank(Cell c, int dir, Cell d) {
	if(d.s(this, -1) != null)
	    throw new IllegalArgumentException("not a headcell: "+d);

	Cell first, last;
	if(dir > 0) {
	    first = d; 
	    last = h(d, 1);
	} else {
	    first = h(d, 1);
	    last = d;
	}
        
        Cell o = s(c, dir);
        if(o!=null)
            disconnect(c, dir);

        connect(c,dir,first);
        if(o!=null)
            connect(last,dir,o);
    }

    public void exciseRange(Cell neg, Cell pos) {
	if(!h(neg, -1).equals(h(pos, -1)))
	    throw new IllegalArgumentException("not on same rank: "+
					       neg+", "+pos);

        Cell p = s(pos, 1);
        Cell m = s(neg, -1);
        disconnect(pos, 1);
        disconnect(neg, -1);

	if(!h(pos, -1).equals(neg)) {
	    if(m != null)
		connect(m, neg);
	    if(p != null)
		connect(pos, p);
	    throw new IllegalArgumentException("wrong order: "+neg+", "+pos);
	}

        if(p!=null && m!=null)
            connect(m, p);
    }

    /** Iterate starting from the given cell.
     * The iterator stops when going back over to c
     * on circular ranks, or at end of ranks for normal ranks.
     * Start from the headcell to go over all cells on the rank
     * once, independent of whether it is circular or not.
     */
    public Iterator iterator(final Cell c) {
	return new Iterator() {
	    Cell cur = c;
	    Cell n = s(c, 1);
	    boolean cReturned;
	    public boolean hasNext() {
		return (cur != null && (!c.equals(cur) || !cReturned));
	    }
	    public Object next() {
		Cell ret = cur;
		cur = n;
		n = s(n, 1);
		if(ret == c) cReturned = true;
		return ret;
	    }
	    public void remove() {
		throw new UnsupportedOperationException();
	    }
	};
    }

    public void canonicalizeCells() {
    }
}
