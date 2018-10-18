/*   
RankList.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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

package org.gzigzag.impl;
import org.gzigzag.*;
import java.util.*;

/** An immutable implementation of List representing a rank of cells.
 *  Any cell on the rank can be passed to the constructor, but the list
 *  always starts with the headcell on the rank. In the case of a looping
 *  rank, the list is considered to end at the first cell negwards from the
 *  headcell.
 *  <p>
 *  Modifications made to the space
 *  result in undefined behavior. :( (FIXME: throw errors?)
 */

public class RankList extends AbstractSequentialList {
public static final String rcsid = "$Id: RankList.java,v 1.5 2001/07/25 10:17:02 bfallenstein Exp $";
    static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }

    /** The headcell of this rank. */
    Cell head;

    /** The dimension. */
    Dim dim;

    public RankList(Cell onRank, Dim dim) {
	this.dim = dim;
	head = onRank.h(dim);
    }

    public RankList(Cell onRank, Cell dim) {
	this.dim = onRank.space.getDim(dim);
	head = onRank.h(dim);
    }

    public ListIterator listIterator(int index) {
	if(index == 0)
	    return new CellIterator(null);

	index--;
	Cell pos = head.s(dim, index);
	if(pos == null)
	    throw new IndexOutOfBoundsException(""+index);
	return new CellIterator(pos);
    }

    public int size() {
	int i = 1;
	for(Cell c = head.s(dim); c != null && !c.equals(head); c = c.s(dim))
	    i++;
	return i;
    }




    protected class CellIterator implements ListIterator {
	/** The cell <em>before</em> the virtual cursor.
	 *  If null, the virtual cursor is before <code>head</code>.
	 *  @see head
	 */
	Cell pos;

	protected CellIterator(Cell pos) {
	    this.pos = pos;
	}

	public void add(Object o) {
	    throw new UnsupportedOperationException();
	}
	
	public boolean hasNext() {
	    if(pos == null) return true;
	    return pos.s(dim) != null && !pos.s(dim).equals(head);
	}
	
	public Object next() {
	    if(pos == null)
		return (pos = head);
	    else if(hasNext())
		return (pos = pos.s(dim));
	    else
		throw new NoSuchElementException("after "+pos+" on "+dim);
	}

	public boolean hasPrevious() {
            return pos != null;
	}

        public Object previous() {
	    if(pos != null) {
		Cell result = pos;
		if(pos.equals(head))
		    pos = null;
		else
		    pos = pos.s(dim, -1);
		return result;
            } else
                throw new NoSuchElementException("before "+pos+" on "+dim);
        }

	public void remove() {
	    throw new UnsupportedOperationException();
	}

	public void set(Object o) {
	    throw new UnsupportedOperationException();
	}

	public int previousIndex() {
	    int i = -1;
	    for(Cell c = pos; c != null && !c.equals(head); c = c.s(dim, -1))
		i++;
	    return i;
	}

        public int nextIndex() {
	    return previousIndex() + 1;
        }
    }
}
