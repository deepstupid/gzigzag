/*   
ZZCellHandle.java
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

/** A ZigZag cell, as a handle.
 * This is a structure that is a handle to one cell.
 * All handles to the same cell should be equal to each other.
 * This means that the information contained within is only allowed
 * to depend on the cell ID.
 * <p>
 * The parts marked <b>EXPERIMENTAL</b> are such, and are subject to change.
 * Those parts are related to implementing virtual subparts of spaces.
 * The idea is to keep an Object handy that represents in a more simple
 * form what this cell actually <em>is</em> in that subpart.
 * Whether this is a speedup or a slowdown remains to be seen.
 */

public abstract class ZZCellHandle extends ZZCell {
public static final String rcsid = "$Id: ZZCellHandle.java,v 1.5 2001/02/23 08:33:37 ajk Exp $";

    /** The cell id.
     */
    public final String id;

    /** <b>EXPERIMENTAL:</b> the part of the space that this cell is in.
     */
    public final ZZSpacePart part;

    /** <b>EXPERIMENTAL:</b> the parsed ID (as provided by the space part)
     * of this cell.
     * This is supposed to be simply an easier-to-use representation
     * of the ID string for the spacepart's dimensions to use.
     * For instance, if the spacepart is a matrix, then parsedID could
     * be a java.awt.Point -like object that contains the coordinates
     * parsed out.
     */
    public final Object parsedID;

    public ZZCellHandle(String id, ZZSpacePart part, Object parsedID) {
        //if (id == null) throw new NullPointerException();
	this.id = id;
	this.part = part;
	this.parsedID = parsedID;
    }

    public String getID() {
	return id;
    }

    public boolean equals(Object o) {
	if(this == o) return true;
	if(!(o instanceof ZZCellHandle)) return false;
	ZZCellHandle it = (ZZCellHandle) o;
	return id == it.id || id.equals(it.id);
    }

    public int hashCode() {
	return id.hashCode();
    }
}

