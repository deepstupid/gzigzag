/*   
Cell.java
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

/** A class to represent a ZigZag cell.
 * <p>
 * This class contains convenience methods for accessing and editing
 * cells, but more complicated operations should be used through the
 * specialized dimensions.
 * <p>
 * There is not necessarily
 * a unique cell to Java object mapping, so there may be
 * several equivalent Java objects representing the same cell.
 * Use the <b>equals</b> method for comparisons.
 * <p>
 * The cell is identified by the <code>Space space</code> and the
 * <code>String id</code> objects. (Ids are interned-- see 
 * <code>String.intern()</code>-- to make comparisons fast.) 
 * <p>
 * There
 * are some additional fields for optimization (namely, the 
 * <code>hashCode</code>, <code>spacepart</code>, <code>inclusionObject</code>,
 * and <code>inclusionIndex</code> fields. Different space types
 * may use these in different ways.
 * <p>
 * The intention is that Space.getCell(id) always results in 
 * exactly equivalent cells: all the inclusionObjects etc are the same.
 * @see Space
 * @see Dim
 */

public class Cell {
public static final String rcsid = "$Id: Cell.java,v 1.44 2002/03/26 19:01:03 bfallenstein Exp $";

    // Fields defining the cell
    public final Space space;
    public final String id;

    // Fields used for optimization
    private final int hashCode;

    public final Spacepart spacepart;
    public final Object inclusionObject;
    public final int inclusionIndex;

    /** Create a <code>Cell</code> object not in a spacepart.
     */
    public Cell(Space space, String id) {
	this.space = space; this.id = id.intern();
	this.hashCode = id.hashCode();
	this.spacepart = null;
	this.inclusionObject = null;
	this.inclusionIndex = -1;
    }

    /** Create a <code>Cell</code> object in a spacepart.
     */
    public Cell(Space space, String id, Spacepart spacepart,
		Object inclusionObject, int inclusionIndex) {
        this.space = space; this.id = id.intern();
        this.hashCode = id.hashCode();
        this.spacepart = spacepart;
        this.inclusionObject = inclusionObject;
        this.inclusionIndex = inclusionIndex;
    }

    /** Create a new cell connected to this cell.
     * @param dim 	Dimension to create the cell along
     * @param dir	The direction, either positive or negative
     */
    final public Cell N(Cell dim, int dir) {
	return N(dim, dir, null);
    }
    final public Cell N(Cell dim) {
	return N(dim, 1, null);
    }
    final public Cell N(Dim dim, int dir) {
	return N(dim, dir, null);
    }
    final public Cell N(Dim dim) {
	return N(dim, 1, null);
    }

    /** Get a new cell associated with this cell (i.e., same slice)
     */
    final public Cell N() { 
	return space.N(this); 
    }
     
    /** Get or create a new cell connected to this cell.
     * A convenience function which first tries to use getNeighbour
     * and if that returns null, creates a new neighbour.
     * @param dim 	Dimension to create the cell along
     * @param dir	The direction, either positive or negative
     * @param obs	The observer
     */
    final public Cell getOrNew(Dim dim, int dir, Obs o) {
	// Do not trigger observer if cell is not there.
	if(s(dim, dir) != null) return s(dim, dir, o);
	return N(dim, dir, o);
    }
    final public Cell getOrNew(Dim dim, int dir) {
	return getOrNew(dim, dir, null);
    }
    final public Cell getOrNew(Dim dim) {
	return getOrNew(dim, 1, null);
    }
    final public Cell getOrNew(Cell dim, int dir, Obs o) {
	return getOrNew(space.getDim(dim), dir, o);
    }
    final public Cell getOrNew(Cell dim, int dir) {
	return getOrNew(dim, dir, null);
    }
    final public Cell getOrNew(Cell dim) {
	return getOrNew(dim, 1, null);
    }
    /** Create a new cell connected to this cell and attach an observer 
     * to it
     * atomically.
     * @param dim 	Dimension to create the cell along
     * @param dir	The direction, either positive or negative
     * @param obs	The observer
     */
    final public Cell N(Cell dim, int dir, Obs o) {
	return space.N(this, dim, dir, o);
    }
    final public Cell N(Dim dim, int dir, Obs o) {
	return space.N(this, dim, dir, o);
    }

    /** Delete this cell. */
    final public void delete() { 
	space.delete(this); 
    }

    /** Delete this cell from a rank along a dimension. */
    final public void excise(Cell dim) {
	excise(space.getDim(dim));
    }
    final public void excise(Dim dim) {
	dim.excise(this);
    }


    /** Connect this cell to another along a dimension. 
     * If there is a former connection,
     * a ZZAlreadyConnectedException is thrown.
     * @param dim	Dimension to connect along
     * @param to	Cell to connect to. This cell will 
     *			end up poswards from us.
     */
    final public void connect(Cell dim, Cell to) 
	throws ZZAlreadyConnectedException {
	connect(space.getDim(dim), to);
    }
    final public void connect(Dim dim, Cell to) 
	throws ZZAlreadyConnectedException {
	dim.connect(this, to);
    }


    /** Connect this cell to another along a dimension in a specified 
     * direction. 
     * If there is a former connection,
     * a ZZAlreadyConnectedException is thrown.
     * @param dim	Dimension to connect along
     * @param dir	The direction, either positive or negative
     * @param to	Cell to connect to.
     */
    final public void connect(Cell dim, int dir, Cell to) 
	    throws ZZAlreadyConnectedException {
	connect(space.getDim(dim), dir, to);
    }
    final public void connect(Dim dim, int dir, Cell to)
	    throws ZZAlreadyConnectedException {
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
    final public void insert(Cell dim, int dir, Cell what) 
	throws ZZAlreadyConnectedException {
	insert(space.getDim(dim), dir, what);
    }
    final public void insert(Dim dim, int dir, Cell what)
        throws ZZAlreadyConnectedException {
	dim.insert(this, dir, what);
    }

    /** Disconnect another cell from this cell.
     * @param dim	Dimension to disconnect along
     * @param dir	The direction, either positive or negative
     */
    final public void disconnect(Cell dim, int dir) {
	disconnect(space.getDim(dim), dir);
    }
    final public void disconnect(Dim dim, int dir) {
	dim.disconnect(this, dir);
    }

    /** Set the content of this cell as a Span.
     * @param text	The new text, as a span pointing to a stable scroll
     */
    final public void setSpan(Span text) {
	space.setSpan(this, text);
    }

    /** Set the content of this cell as text.
     * XXX Need to think about potential other types: int etc.
     * @param text	The new text
     */
    final public void setText(String text) {
	space.setText(this, text);
    }

    /** Find the neighbour of this cell along a dimension
     * and attach an observer to this relation
     * atomically.
     * @param dim 	Dimension to go along
     * @param steps	The direction, either positive or negative
     * @param obs	The observer
     * @return 	The neighbour, or null if none.
     */
    final public Cell s(Cell dim, int steps, Obs o) {
	return space.getDim(dim).s(this, steps, o);
    }
    final public Cell s(Dim dim, int steps, Obs o) {
	return dim.s(this, steps, o);
    }

    /** Find the neighbour of this cell along a dimension
     * atomically.
     * @param dim 	Dimension to go along
     * @param dir	The direction, either positive or negative
     * @return 	The neighbour, or null if none.
     */
    final public Cell s(Cell dim, int dir) {
	return s(dim,dir,null);
    }
    final public Cell s(Dim dim, int dir) {
	return s(dim,dir,null);
    }

    /** Find the next cell along a dimension.
     * Always moves poswards.
     * @param dim 	Dimension to go along
     * @return 	The neighbour, or null if none.
     */
    final public Cell s(Cell dim) {
	return s(dim,1,null);
    }
    final public Cell s(Dim dim) {
	return s(dim,1,null);
    }
    final public Cell s(Cell dim, Obs o) {
	return s(dim,1,o);
    }
    final public Cell s(Dim dim, Obs o) {
	return s(dim,1,o);
    }

    /** Find the headcell of a rank.
     * @param dim 	Dimension to go along
     * @param dir	The direction, either positive or negative
     * @return 	The headcell
     */
    final public Cell h(Cell dim, int dir) {
	return h(dim, dir, false, null);
    }
    final public Cell h(Cell dim, int dir, Obs o) {
	return h(dim, dir, false, o);
    }
    final public Cell h(Cell dim, boolean ensuremove) {
	return h(dim, -1, ensuremove, null);
    }
    final public Cell h(Cell dim, int dir, boolean ensuremove) {
	return h(dim, dir, ensuremove, null);
    }
    /** Find the headcell of a rank.
     * The headcell is the most negative cell so this call is equivalent
     * to h(dim, -1).
     */
    final public Cell h(Cell dim) {
	return h(dim, -1, false, null);
    }
    final public Cell h(Dim dim) {
	return h(dim, -1, false, null);
    }
    final public Cell h(Dim dim, int dir) {
	return h(dim, dir, false, null);
    }

    /** Find the headcell of a rank.
     * Always returns a cell, even if rank is looping.
     * Looking for null going poswards from the headcell might be infinite
     * and needs the user to explicitly detect loops.
     * Just in case of synch problems, it is recommended that the user check
     * both for equality to the headcell <em>and</em> use a 
     * LoopDetector, in case the loop changed.
     * @param dim 	Dimension to go along
     * @param dir	The direction, either positive or negative
     * @param ensuremove If true, this function returns null if
     *			the given cell already is the headcell.
     * @return 	The headcell
     */
    final public Cell h(Cell dim, int dir, boolean ensuremove,
	    Obs o) {
	return h(space.getDim(dim), dir, ensuremove, o);
    }

    final public Cell h(Dim dim, int dir, boolean ensuremove,
	    Obs o) {
	Cell r = dim.h(this, dir, o);
	if(ensuremove && r == this) return null;
	return r;
    }


    /** Returns the text contained in the cell.
     * A null return means that the cell has been deleted.
     * XXX Really? What about empty cells / image spans etc.
     */
    final public String t() { return t(null); }
    final public String t(Obs o) { return space.getText(this, o); }

    /** Returns the stable media reference in the cell.
     * A null return means either that the cell has been deleted
     * or that the text inside is not a reference.
     * @param lang	The requested language.
     */
    final public Span getSpan() { return getSpan(null); }
    final public Span getSpan(Obs o) { return space.getSpan(this, o); }

    /** Tests whether this cell is the same as another cell.
     * There may be more than one Java object representing the same cell
     * so it is important to use this method instead of the '==' operator
     * to test for equality.
     */
    public boolean equals(Object c) {
	if(this == c) return true;
	if(c == null) return false;
	if(!(c instanceof Cell)) return false;
	Cell cell = (Cell)c;
	if(cell.space != space) return false;
	// Since cells use interned id strings, we can do this.
	return cell.id == id;
	// return cell.id.equals(id);
    }

    public int hashCode() {
	// We'll assume that cells will not be stored 
	// with cells from other spaces too often...
	// return id.hashCode();
	return hashCode;
    }

    /** Hops the cell along the dimension.
     * This means that all other connections remain the
     * same but that this cell changes places in the
     * rank given by the dimension.
     * This operation may be given interesting meanings when e.g.
     * hopping over the end of a rank.
     * @param dim	The dimension to hop along
     * @param steps	The number of places to hop. May be negative,
     *			in which case the cell will hop negwards.
     */
    final public void hop(Cell dim, int steps) {
	hop(space.getDim(dim), steps);
    }
    final public void hop(Dim dim, int steps) {
	dim.hop(this, steps);
    }

    final public Cell getHomeCell() {
	return space.getHomeCell();
    }

    final public Cell zzclone() {
	Dim cl = space.getCloneDim();
	return space.N(cl.h(this, 1), cl, 1, null);
    }
    /** Clone this cell in the same slice as a given cell.
     */
    final public Cell zzclone(Cell slice) {
	Dim cl = space.getCloneDim();
	Cell end = cl.h(this, 1);
	Cell nu = space.N(slice);
	Cell firstroot = nu.getRootclone();
	end.connect(cl, 1, nu);
	if(!nu.getRootclone().equals(getRootclone()))
	    throw new ZZError("ARGH: zzclone(Cell) didn't work: "+
			      getRootclone() + " != " + nu.getRootclone()+"\n"+
			      "Nu was: "+nu+" in "+nu.space+"\n"+
			      "Slice was: "+slice+" in "+slice.space+"\n"+
			      "This is: "+this+" in "+space+"\n"+
			      "End was: "+end+" in "+end.space+"\n"+
			      "Nu's negconn was: "+nu.s(cl, -1)+"\n"+
			      "Nu's head was: "+nu.h(cl, -1)+"\n"+
			      "Firstroot was: "+firstroot);
	return nu;
    }

    final public Cell getRootclone() { return getRootclone(null); }
    final public Cell getRootclone(Obs o) {
	return space.getRootclone(this, o);
    }

    /** Return a stringized form of this cell.
     * This form is unstable and its use for anything except
     * human-readable debugging is not recommended.
     * <p>
     * Currently the format is: The cell ID in single quotes
     * and the text content after that, in parentheses,
     * for example <code>'53' (Text in cell)</code>.
     * <p>
     * (Changed now to exclude id - still thinking about it...)
     * (Benja: changed it back because it's a pain in the neck for tests and
     *  Tuomas isn't there to ask. We can change it again later (when IDs get
     *  longer), but at this point in time this is better-- too many test
     *  cells do not have content and thus render as a simple and misleading
     *  "(null)".)
     * <p>
     * Ok, here's a solution: nothing should depend on it.
     * we'll read the format from a property.
     */
    public String toString() {
	switch(cellFormat) {
	case TEXT_ONLY:
	    return "(" + t() + ")";
	case ID_AND_TEXT:
	    return "'"+id + "' (" + t() + ")";
	case ID_ONLY:
	    return "'"+id+"'";
	}
	throw new Error("Unknown cell stringization format "+cellFormat);
    }

    static public final int TEXT_ONLY = 0;
    static public final int ID_AND_TEXT = 1;
    static public final int ID_ONLY = 2;

    static public int cellFormat = ID_AND_TEXT;
    static {
	String s = System.getProperty("gzigzag.cellstr");
	if(s == null) {
	} else if(s.equals("textonly"))
	    cellFormat = TEXT_ONLY;
	else if(s.equals("idonly"))
	    cellFormat = ID_ONLY;
    }

    /** Get the Java object associated with this cell, if any.
     *  Currently the manner in which Java objects are associated with
     *  cells is implementation-defined by the space.
     */
    final public Object getJavaObject() { return getJavaObject(null); }
    final public Object getJavaObject(Obs o) {
	return space.getJavaObject(this, o);
    }

}

