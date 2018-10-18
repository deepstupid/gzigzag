/*   
Spacepart.java
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
package org.gzigzag;
import java.util.*;

/** A virtual part of a ZigZag space, containing cells and connections.
 *  This is "included" in an "enclosing" space. The cells and connections in
 *  the spacepart are then available in the enclosing space; the enclosing
 *  space can make a connection to a cell in the included part, provided that
 *  that cell isn't connected already, and the connections from that cell to
 *  other cells in the spacepart are then visible in the enclosing space.
 *  This class provides the enclosing space with methods to retrieve
 *  information about the cells in this spacepart and their connections; it
 *  is the responsibility of the enclosing space to ensure that the
 *  connections are visible in it.
 *  <p>
 *  Numerous methods in this class take <code>Cell</code> parameters or 
 *  return <code>Cell</code> values. These <code>Cell</code> objects
 *  always represent cells whose space is the space including this part,
 *  and usually cells from this spacepart (XXX spec exceptions).
 *  @see AbstractSpacepart
 */

public interface Spacepart {
String rcsid = "$Id: Spacepart.java,v 1.10 2002/03/11 23:31:43 bfallenstein Exp $";

    /** A class for marker objects for spacepart inclusion types.
     *  Example: In the <code>Spacepart</code> interface, there is a constant
     *  <code>InclusionType EDITABLE</code>. If a spacepart is of this
     *  inclusion type, <code>part.getInclusionType() == Spacepart.EDITABLE</code>
     *  holds true.
     */
    class InclusionType {}

    /** Inclusion type of non-editable spaceparts.
     *  No connections in a non-editable spacepart can be made or broken.
     *  When making connections to and between unconnected spacepart cells,
     *  the connections will be stored in the enclosing space. Connections
     *  can never be broken.
     */
    InclusionType NON_EDITABLE = new InclusionType();

    /** Inclusion type of editable spaceparts.
     *  Connections can be made and broken; <code>connect</code> calls will
     *  be channeled through to the spacepart if and only if they are between
     *  different cells all of which are inside the spacepart.
     */
    InclusionType EDITABLE = new InclusionType();



    /** Get the space this is a part of. */
    Space getSpace();

    /** Get the inclusion cell for this spacepart.
     */
    Cell getBase();

    /** Get the separator <code>String</code> for this spacepart.
     *  Cell ids from the spacepart are 
     *  <code>getBase().id + getSeparator() + id_in_spacepart</code>.
     */
    String getSeparator();

    /** Get the inclusion type for this spacepart.
     *  This tells the enclosing space how to treat this spacepart. Two
     *  standard inclusion types, <code>EDITABLE</code> and
     *  <code>NON_EDITABLE</code>, are defined in this <code>Spacepart</code>
     *  interface; others can be defined elsewhere.
     */
    InclusionType getInclusionType();

    /** Whether a cell with a given id exists in this spacepart.
     *  @param id The id in the enclosing space, the inclusion cell id
     *            not stripped yet. No tests need to be performed on
     *            the validity of the inclusion cell id, though.
     */
    boolean exists(String id);

    /** Whether a cell with a given inclusion object and index exists in
     *  this spacepart.
     */
    boolean exists(Object object, int idx);

    /** Get a cell from this spacepart, given its id.
     *  Has to cache the <code>Cell</code> object if appropriate.
     *  @param id The id in the enclosing space, the inclusion cell id
     *            not stripped yet. No tests need to be performed on
     *            the validity of the inclusion cell id, though.
     */
    Cell getCell(String id) throws ZZImpossibleCellException;

    /** Get a cell from this spacepart, given its inclusion object and
     *  index. Has to cache the <code>Cell</code> object if appropriate.
     */
    Cell getCell(Object object, int idx) throws ZZImpossibleCellException;

    /** Get the (enclosing space's) dimension corresponding to the given cell
     *  in the spacepart. The returned dimension must operate on cells in
     *  the enclosing space. This is unrelated to the special connections
     *  between the cells in this spacepart, which can be along any dimension;
     *  rather, this returns the dimension represented by a cell in this
     *  spacepart.
     *  <p>
     *  Note about slices: Say the cell is in a slice, but the dimension
     *  has a global identity (as in the standard GZigZag implementation).
     *  The cell may have the id "<code>abc:def;ghi</code>, where
     *  <code>ghi</code> is the global id of the dimension. Now, this method
     *  is completely free to return <code>getSpace().getDim("ghi")</code>,
     *  which is what should happen in the above case.
     *  <p>
     *  XXX should this decide about caching? probably not, but what to do
     *  in the case of a spacepart with an infinite number of dimensions?
     */
    Dim getDim(Cell c);

    /** Create a new cell in this spacepart.
     *  Throws an error if this spacepart is immutable. (We need a
     *  catchable exception for this!)
     */
    Cell N();
    Cell N(Cell c, Cell dim, int dir, Obs o);

    /** Get another cell <I>steps</I> steps on the dimension from c 
     * (<b>required</b>).
     * @param c     The cell
     * @param steps Number of steps, can be negative.
     */
    Cell s(Cell dim, Cell c, int steps, Obs o);
    
    /** Connect the two cells in this dimension, in order (<b>required</b>).
     * This method is not allowed to break existing connections.
     * @throws ZZAlreadyConnectedException If one of the cells is 
     *		already connected. 
     */
    void connect(Cell dim, Cell c, Cell d);

    /** Disconnect the cell in the given direction (<b>required</b>).
     * It is not an error to disconnect something that is not connected.
     */
    void disconnect(Cell dim, Cell c, int dir);

    /** Get headcell.
     * This function <em>must</em> return a cell always, even with circular
     * ranks.
     * In case of a circular rank, picking a cell depends on dimension.
     * Ted recommends that it be user-pickable, especially for
     * normal (user-modifiable) dimensions.
     */
    Cell h(Cell dim, Cell c, int dir, Obs o);

    void insert(Cell dim, Cell c, int dir, Cell d);

    void insertRank(Cell dim, Cell c, int dir, Cell d);

    /** Remove the given cell from the given dimension.
     */
    void excise(Cell dim, Cell c);

    void exciseRange(Cell dim, Cell from, Cell to);

    void hop(Cell dim, Cell c, int steps);



    Span getSpan(Cell c, Obs o);
    void setSpan(Cell c, Span s);

    String getText(Cell c, Obs o);
    void setText(Cell c, String s);
}
