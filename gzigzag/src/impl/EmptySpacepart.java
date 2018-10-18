/*   
EmptySpacepart.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
 *    Portions copyright (c) 2001, Rauli Ruohonen
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

/** A spacepart containing no cells, no connections, and only dimensions
 *  with no connections. Generally meant to be subclassed by spaceparts that
 *  implement only a part of the <code>Spacepart</code> interface.
 *  <p>
 *  Attempts to edit the spacepart will throw exceptions.
 */

public class EmptySpacepart extends AbstractSpacepart {
public static final String rcsid = "$Id: EmptySpacepart.java,v 1.1 2001/08/26 12:26:56 bfallenstein Exp $";

    public EmptySpacepart(Space space, Cell base, String separator,
			  InclusionType inclusionType) {
	super(space, base, separator, inclusionType);
	nullDim = new NullDim();
    }


    protected final NullDim nullDim;
    protected class NullDim extends AbstractDim {
        public NullDim() { super(EmptySpacepart.this.space); }
        public Cell s(Cell c, int steps, Obs o) {
            verifyExistence(c);
            return null;
        }
        public void connect(Cell c, Cell d) {
            verifyExistence(c);
            verifyExistence(d);
            throw new UnsupportedOperationException();
        }
        public void disconnect(Cell c, int dir) {
            verifyExistence(c);
            throw new UnsupportedOperationException();
        }
        public boolean isCircularHead(Cell c, Obs o) {
            verifyExistence(c);
            return false;
        }
        public void addRealNegSides(Set to) {
        }
    }
    protected void verifyExistence(Cell c) {
        if(c.space != this)
            throw new IllegalArgumentException("FullSpace given a cell with"+
                                               " wrong space: "+c.space);
        if(!exists(c.id))
	    throw new IllegalArgumentException("Cell "+c.id+" doesn't exist!");
    }


    public boolean exists(String id) {
	return false;
    }
    public boolean exists(Object obj, int idx) {
	return false;
    }

    public Cell getCell(String id) {
	throw new IllegalArgumentException("Cell "+id+" doesn't exist!");
    }
    public Cell getCell(Object obj, int idx) {
	throw new IllegalArgumentException("Cell doesn't exist: "+obj+", "+idx);
    }


    public Dim getDim(Cell c) {
	return nullDim;
    }

    public Cell N() {
	throw new UnsupportedOperationException("editing");
    }

    public Cell s(Cell dim, Cell c, int steps, Obs o) {
	return null;
    }
    public void connect(Cell dim, Cell c, Cell d) {
        throw new UnsupportedOperationException("editing");
    }
    public void disconnect(Cell dim, Cell c, int dir) {
        throw new UnsupportedOperationException("editing");
    }
    public Cell h(Cell dim, Cell c, int dir, Obs o) {
	return c;
    }
    public void insert(Cell dim, Cell c, int dir, Cell d) {
        throw new UnsupportedOperationException("editing");
    }
    public void insertRank(Cell dim, Cell c, int dir, Cell d) {
        throw new UnsupportedOperationException("editing");
    }
    public void excise(Cell dim, Cell c) {
        throw new UnsupportedOperationException("editing");
    }
    public void exciseRange(Cell dim, Cell from, Cell to) {
        throw new UnsupportedOperationException("editing");
    }
    public void hop(Cell dim, Cell c, int steps) {
        throw new UnsupportedOperationException("editing");
    }


    public void setSpan(Cell c, Span s) {
        throw new UnsupportedOperationException("editing");
    }
    public void setText(Cell c, String s) {
        throw new UnsupportedOperationException("editing");
    }
}
