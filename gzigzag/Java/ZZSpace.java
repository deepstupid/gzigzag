/*   
ZZSpace.java
 *    
 *    Copyright (c) 1999, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka and Tuukka Hastrup
 */
/*
 * 	`He's got a good memory, you've got to grant him that,' said
 *    Didactylos. `Show him some more scrolls.'
 *	`How will we know he's remembered them?' Urn demanded, unrolling
 *    a scroll of geometrical theorems. `He can't read! And even if he 
 *    could read, he can't write!'
 *	`We shall have to teach him.'
 *		- Terry Pratchett, Small Gods, p.214
 */

package org.gzigzag;
import java.util.*;
/** A simple ZZ interface for a ZZ space.
 * This abstract class defines all the elementary operations
 * for a ZZ space. Note though, that many operations are done to
 * <b>cells</b> and are defined in ZZCell.java.
 * <p>
 * Also included are default operations for space. These can be overridden
 * in concrete implementations. Note how we can leave undecided witch 
 * classes and objects are responsible for some operations, as it's only
 * needed that the space contains all specified information.
 * <p>
 * The dimensions are listed in the space itself, on two ranks from 
 * the Home cell, along d.masterdim which includes all dimensions and
 * d.userdim which excludes the system dimensions that are generally
 * not used by the user (such as d.cursor, d.cursor-cargo). 
 * (XXX d.userdim not yet)
 * <p>
 * The event loop mechanism is included in this object since the
 * event loop is per-space. Currently, the way to prevent events
 * from propagating is to lock this object using synchronized(){}.
 * The relationship of this synchronization to remote objects is not
 * yet clear, as well as interaction with slices and such
 * so <b>this synchronization may change in the future
 * to a pair of freeze and thaw method calls</b>.
 */

public abstract class ZZSpace {
public static final String rcsid = "$Id: ZZSpace.java,v 1.22 2001/02/23 12:20:32 bfallenstein Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) ZZLogger.log(s); }
    protected static void pa(String s) { ZZLogger.log(s); }

    // CORE FUNCTIONALITY OF ZZSPACE

    /** Obtain the unique ID for this space.
     * Note that this method <I>may not</I> throw an exception. 
     * if the space not have an ID.
     */
    public String getIDOrNull() {
        return null;
    }

    /** Obtain the unique ID for this space.
     * Note that this method <I>may</I> throw an exception,
     * if the space not have an ID.
     */
    public String getID() {
        String rv = getIDOrNull();
        if (rv == null) {
            throw new NullPointerException("This space has no valid ID");
        }
        return rv;
    }

    /** Set the unique ID for this space.
     * Note that this method <I>may</I> throw an exception
     * if the space does not support space IDs.
     */
    public void setID(String id) {
        throw new ZZError("Setting space ID not supported by this space");
    }

     /** Get the home cell of this space.
     * The home cell is where everything begins */
    abstract public ZZCell getHomeCell();

    /** Get a cell by its ID.
     */
    public ZZCell getCellByID(String s) {
	throw new ZZError("Not implemented for this space");
    }

    /** Get the appendable string scroll of this space.
     */
    public StringScroll getStringScroll() {
	return null;
    }
    /** Get the string scroll with the given identifier.
     * (XXX???)
     */
    public StringScroll getStringScroll(String name) {
	return null;
    }

    /** Find all cells whose span overlaps sp.
     */
    public ZZCell[] overlaps(Span sp) {
	return null;
    }


    // TIMESTAMP AND UNDO.
    // THESE NEED NOT DO ANYTHING.

    /** Timestamp the space.
     */
    public int stamp() {
	return -1;
    }
    /** Undo to the previous timestamp.
     */
    public void undo() {
    }
    /** Redo to the next timestamp.
     */
    public void redo() {
    }
    /** Commit the changes.
     * Commits may define their own timestamps i.e. after commit the
     * micro-level timestamps may not be accessible.
     */
    public int commit() {
	return -1;
    }


    // TRAVERSING

    /** Returns enumeration over the cells in this space - also in slices.
     */
    public Enumeration cells() {
	return new Enumeration() {
	    ZZCell next = getHomeCell();
	    ZZCell slice = next; // Homecell of current slice
	    public boolean hasMoreElements() {
		if(next == null)
		     return false;
		return true;
	    }
	    public Object nextElement() {
		if(next == null)
			throw new NoSuchElementException();
		ZZCell ret = next;
		next = next.s(d.cellcreation, 1);
		if(next == null) // End of this slice
			slice = next = 
			    slice.s(d.slices, 1);
		return ret;
	    }
	};
    }

    /** Get a list of the dimensions in the space.
     * This function will likely change in the future as it is possible
     * that dimensions become cells instead of strings.
     */ 
    public String[] dims() {
	Vector dims = new Vector();
	ZZCell c = getHomeCell().s(d.masterdim, 1);
	while(c != null) {
		dims.addElement(c.getText());
		c = c.s(d.masterdim, 1);
	}
	String[] ret = new String[dims.size()];
	for(int i = 0;i<dims.size();i++)
	    ret[i] = (String)dims.elementAt(i);
	return ret;
//	    return (String[])(dims.toArray(new String[dims.size()]));
    }


    // OBSERVING

    /** Freeze the space. No events are dispatched while 
     * the space is frozen. 
     */
    public void freeze() { }

    /** Thaw the space. No events are dispatched while 
     * the space is frozen. 
     */
    public void thaw() { }

    /** Remove all the cells from the observation list of this observer */
    abstract public void rmAllObs(ZZObs o);


    // VARIOUS

    /** Is this space readonly? FIXME: not used here */
    public /* final */ boolean readonly;

    public ZZSpace() { this(false); }
    public ZZSpace(boolean readonly) {
        this.readonly = readonly;
    }

    /** Finds the headcells of all ranks on dim
     * that are longer than one cell in length */
    public abstract ZZCell[] findLongRankHeads(String dim);


    // HANDLING OF MASTER DIM LIST

    /** Called when a new dimension is instantiated. It might be here already,
     *  however, as dimensions are instantiated again when the space is loaded
     *  from disk.
     *  @return		true iff dimension was truly new
     */
    protected boolean updateMasterDimList(String newdim) {
	if(!validDim(newdim))
	    return false;
	ZZCell p = getHomeCell().s(d.masterdim, 1);
	while(p!=null) { 
	    if(p.getText().equals(newdim))
		return false;
	    p = p.s(d.masterdim, 1);
	}
	ZZCell c = getHomeCell().N(d.masterdim, 1);
	c.setText(newdim);
	return true;
    }

    /** Recreate the master dimension list. */
    public void recreateMasterDimList() {
        throw new ZZError("recreateMasterDimList not implemented for this space");
    }

    /** Returns an enumeration of posward
     * connections on this dimension.
     * XXX Should change to be just ZZCells!!!
     */
    public Enumeration posconns(String dim) {
    	class ConnEnum implements Enumeration {
	    public ConnEnum(String dim) {
		this.dim = dim;
		next = findNext();
	    }
	    String dim;
	    Enumeration cells = cells();
	    ZZConnection next;
	    public boolean hasMoreElements() {
		if(next != null) 
		    return true;
		else
		    return false;
	    }
	    public Object nextElement() {
	    	if(next == null)
		    throw new NoSuchElementException();
		ZZConnection ret = next;
		next = findNext();
		return ret;
	    }
	    public ZZConnection findNext() {
	        while(cells.hasMoreElements()) {
		    ZZCell c1 = (ZZCell) cells.nextElement();

		    ZZCell c2 = c1.s(dim, 1);
		    if(c2 != null)
		    	return new ZZConnection(c1, dim, 1, c2);
		}
		return null;
	    }
	}
	return new ConnEnum(dim);
    }

    /** Whether a dimension name is acceptable.
     */
    public boolean validDim(String d) {
        if(d==null) return false;
        if(d.equals("")) return false;
        return true;
    }

    /** Class Dims acts as a structure containing concrete string representation
     *  of the special dimensions. It's not static so it can vary 
     *  between spaces. Also, these strings could be initialized in the 
     *  constructor to allow runtime setting.
     * <p>
     * This is going away once dimensions are cells, not strings.
     */
    class Dims {
	public final String cellcreation = "d.cellcreation";
        public final String slices = "d.slices";
	public final String masterdim = "d.masterdim";
        public final String clone = "d.clone";
    }

    /** A nice hack to make it possible to use d.* names. Contains all 
     *  dimensions witch have special meaning and are thus referenced 
     *  to in this class.
     */ 
    Dims d = new Dims();
}
