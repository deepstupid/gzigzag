/*   
PrimitiveSpace.java
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
 * Written by Benja Fallenstein, heavily based on code by Rauli Ruohonen
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.Mediaserver;
import java.util.*;

/** A hard-coded space containing primitives.
 *  Abstract convenience superclass.
 *  XXX doc
 */

public abstract class PrimitiveSpace extends AbstractSpace implements GIDSpace {
public static final String rcsid = "$Id: PrimitiveSpace.java,v 1.15 2002/03/15 23:35:40 bfallenstein Exp $";

    /** Get a primitive in this space, by ID.
     * Implement this in subclasses.
     */
    public abstract Object getPrimitive(String id);



    // INTERNALS

    public Map getRealDims() { throw new UnsupportedOperationException(); }

    protected final Cell home = new Cell(this, "home-cell");

    protected final NullDim dim = new NullDim(this);
    protected class NullDim extends AbstractDim implements VStreamDim {
	public NullDim(Space space) { super(space); }
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
	public void insertAfterCell(Cell at, Cell rank) {
	    throw new UnsupportedOperationException();
	}
	public void removeRange(Cell first, Cell last) {
	    throw new UnsupportedOperationException();
	}
	public void iterate(org.gzigzag.vob.CharRangeIter i, Cell stream,
			    Map extra) {
	}
	public void notifyTransclusion(Cell start, int length) {
	    throw new UnsupportedOperationException();
	}
	public void dumpVStreamInfo(Cell c) {
	}
    }
    protected void verifyExistence(String id) {
	if (id.indexOf(":") != -1)
	    throw new ZZImpossibleCellException("Cell id '"+id+
						"' contains ':'!");
	if (getPrimitive(id) == null)
	    throw new ZZImpossibleCellException("Cell "+id+" does not exist");
    }
    protected void verifyExistence(Cell c) {
	if (c.space != this)
	    throw new IllegalArgumentException("FullSpace given a cell with"+
					       " wrong space: "+c.space);
	verifyExistence(c.id);
    }

    public Cell getHomeCell() { return home; }
    public Cell getCell(String id) {
	verifyExistence(id);
	return new Cell(this, id);
    }
    public Dim getDim(Cell name) {
	// verifyExistence(name.id);
	return dim;
    }
    public Dim getCloneDim() {
	return getDim(Dims.d_clone_id);
    }
    public Cell N(Cell c) {
	verifyExistence(c);
	throw new UnsupportedOperationException();
    }
    public Cell N(Cell c, Dim dim, int dir, Obs o) {
	verifyExistence(c);
	NullDim foo = (NullDim)dim;
	throw new UnsupportedOperationException();
    }
    public void delete(Cell c) {
	verifyExistence(c);
	throw new UnsupportedOperationException();
    }
    public void setSpan(Cell c, Span s) {
	verifyExistence(c);
	throw new UnsupportedOperationException();
    }
    public Span getSpan(Cell c, Obs o) {
	verifyExistence(c);
	return null;
    }
    public void setText(Cell c, String s) {
	verifyExistence(c);
	throw new UnsupportedOperationException();
    }
    public Object getJavaObject(Cell c, Obs o) {
	verifyExistence(c);
	return getPrimitive(c.id);
    }
    public String getText(Cell c, Obs o) {
	verifyExistence(c);
	return this.getClass().getName() + "." + c.id;
    }
    public Mediaserver.Id getLastId() {
	byte[] klass, id;
	klass = this.getClass().getName().getBytes();
	id = new byte[klass.length+1];
	id[0] = (byte)0xFF;
	System.arraycopy(klass, 0, id, 1, klass.length);
	return new Mediaserver.Id(id);
    }
    public Cell transcopy(Cell c) {
	throw new UnsupportedOperationException();
    }
}
