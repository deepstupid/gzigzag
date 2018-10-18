/*   
FullSpace.java
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
 * Written by Rauli Ruohonen
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import java.util.*;

/** An immutable space containing cells with every possible String as ID.
 *  There are no connections between any of the cells, and none can be created.
 *  This, being immutable, is of course not persistent; it is used to get
 *  cells to serve as tags for cell identities.
 *  @see Id
 */
public class FullSpace extends AbstractSpace {
public static final String rcsid = "$Id: FullSpace.java,v 1.15 2002/03/15 23:35:39 bfallenstein Exp $";

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
       /* Can't do this, either: home-id:... must exist
	if (id.indexOf(":") != -1)
	    throw new ZZImpossibleCellException("Cell id '"+id+
						"' contains ':'");
	*/
	/* // XXX Can't do these right now (we have home-blocks, too)
	  int idx = id.indexOf("-");
	  if (idx == -1)
	  throw new ZZImpossibleCellException("Cell id '"+id+
	  "' doesn't contain '-'");
	  idx = id.substring(idx+1).indexOf("-");
	  if (idx != -1)
	  throw new ZZImpossibleCellException("Cell id '"+id+"' contains "+
	  "more than one '-'");*/
    }
    protected void verifyExistence(Cell c) {
	if (c.space != this)
	    throw new IllegalArgumentException("FullSpace given a cell with"+
					       " wrong space: "+c.space);
	verifyExistence(c.id);
    }

    public FullSpace() {}
    public Cell getHomeCell() { return home; }
    public Cell getCell(String id) {
	verifyExistence(id);
	return new Cell(this, id);
    }
    public Dim getDim(Cell name) {
	//verifyExistence(Id.get(name).id);
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
    public String getText(Cell c, Obs o) {
	verifyExistence(c);
	return null;
    }
    public Object getJavaObject(Cell c, Obs o) {
	verifyExistence(c);
	return null;
    }
}
