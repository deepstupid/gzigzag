/*   
SliceSpacepart.java
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

/** A spacepart containing a slice (a different space).
 */

public class SliceSpacepart extends EmptySpacepart {
public static final String rcsid = "$Id: SliceSpacepart.java,v 1.3 2002/03/12 01:07:17 bfallenstein Exp $";

    public final Space subspace;

    public SliceSpacepart(Space space, Cell base, String separator,
			  InclusionType inclusionType, Space subspace) {
	super(space, base, separator, inclusionType);
	this.subspace = subspace;
    }

    public boolean exists(String id) {
	String subid = stripBase(id);
	return subspace.exists(subid);
    }
    public boolean exists(Object obj, int idx) {
	Cell c = (Cell)obj;
	if(c.space == subspace) return true;
	throw new Error("Strange, this should probably never be thrown...XXX"+obj);
    }

    public Cell getCell(String id) {
	String subid = stripBase(id);
	return new Cell(space, id, this, subspace.getCell(subid), 0);
    }
    public Cell getCell(Object obj, int idx) {
	Cell c = (Cell)obj;
	if(c.space != subspace)
	    throw new Error("Cell "+c+" not in space "+subspace+", but in "+c.space);
	if(idx != 0)
	    throw new Error("idx must be 0, bus is "+idx);
	return fromSub(c);
    }


    public Cell fromSub(Cell c) {
	if(c.space != subspace) throw new Error("ARGH!: "+c+" "+c.space+" "+subspace);
        return new Cell(space, addBase(c.id), this, c, 0);
    }
    public Cell toSub(Cell c) {
	Cell d = (Cell)c.inclusionObject;
	if(d.space != subspace) throw new Error("ARGH!: "+d+" "+d.space+" "+subspace);
	return d;
    }


    public Cell N() {
	return fromSub(subspace.N());
    }

    private Dim dim(Cell dim) {
	return subspace.getDim(Id.get(dim));
    }

    public Cell s(Cell dim, Cell c, int steps, Obs o) {
	return fromSub(dim(dim).s(toSub(c), steps, o));
    }
    public void connect(Cell dim, Cell c, Cell d) {
	dim(dim).connect(toSub(c), toSub(d));
    }
    public void disconnect(Cell dim, Cell c, int dir) {
	dim(dim).disconnect(toSub(c), dir);
    }
    public Cell h(Cell dim, Cell c, int dir, Obs o) {
	return fromSub(dim(dim).h(toSub(c), dir, o));
    }
    public void insert(Cell dim, Cell c, int dir, Cell d) {
	dim(dim).insert(toSub(c), dir, toSub(d));
    }
    public void insertRank(Cell dim, Cell c, int dir, Cell d) {
        dim(dim).insertRank(toSub(c), dir, toSub(d));
    }
    public void excise(Cell dim, Cell c) {
        dim(dim).excise(toSub(c));
    }
    public void exciseRange(Cell dim, Cell from, Cell to) {
        dim(dim).exciseRange(toSub(from), toSub(to));
    }
    public void hop(Cell dim, Cell c, int steps) {
        dim(dim).hop(toSub(c), steps);
    }


    public void setSpan(Cell c, Span s) {
	subspace.setSpan(toSub(c), s);
    }
    public Span getSpan(Cell c, Obs o) {
	return subspace.getSpan(toSub(c), o);
    }

    public void setText(Cell c, String s) {
	subspace.setText(toSub(c), s);
    }
    public String getText(Cell c, Obs o) {
	return subspace.getText(toSub(c), o);
    }
}
