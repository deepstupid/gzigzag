/*   
Dim.java
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

/** A single ZZ dimension.
 * A dimension connects cells to two directions. It is
 * <b>vital</b> that the identity
 * <pre>
 *	s(c,dir) == null || s(c,dir).s(c,-1) == c
 * </pre>
 * holds at each time: this is the underpinning of the ZZ thinking.
 * Of course, the connections may change over time but the identity
 * should still stay true.
 * @see AbstractDim, TestSimpleDim
 */

public interface Dim {
String rcsid = "$Id: Dim.java,v 1.20 2002/03/10 01:16:23 bfallenstein Exp $";

    /** Get the space this dimension is affilated with. */
    Space getSpace();

    /** Delegate to space. */
    Cell getCell(String id);

    /** Get another cell <I>steps</I> steps on the dimension from c.
     * @param c     The cell
     * @param steps Number of steps, can be negative.
     */
    Cell s(Cell c, int steps, Obs o);

    /** Connect the two cells in this dimension, in order.
     * This method is not allowed to break existing connections.
     * @throws ZZAlreadyConnectedException If one of the cells is 
     *		already connected. 
     */
    void connect(Cell c, Cell d) throws ZZAlreadyConnectedException;

    /** Disconnect the cell in the given direction.
     * It is not an error to disconnect something that is not connected.
     */
    void disconnect(Cell c, int dir);

    /** Get the set of cell ids connected poswards on this dimension.
     *  Only the 'real' cells which should be saved to the file, merged etc.,
     *  not connections inferred by the computer.
     *  <p>
     *  The dimension <strong>must</strong> add cell ids, i.e.
     *  <code>String</code>s, <strong>not</strong> <code>Cell</code>s.
     *  <p>
     *  XXX doc better!
     * XXX Change API to add cells, not ids!!
     */
    void addRealNegSides(Set set);


    /** Connect two cells in direction given.
     */
    void connect(Cell c, int dir, Cell d) 
	throws ZZAlreadyConnectedException;


    Cell s(Cell c, int steps);

    Cell s(Cell c);

    boolean isCircularHead(Cell c, Obs o);

    /** Get headcell.
     * This function <em>must</em> return a cell always, even with circular
     * ranks.
     * In case of a circular rank, picking a cell depends on dimension.
     * Ted recommends that it be user-pickable, especially for
     * normal (user-modifiable) dimensions.
     */
    Cell h(Cell c, int dir, Obs o);
    Cell h(Cell c, int dir);
    Cell h(Cell c);

    void insert(Cell c, int dir, Cell d)
	throws ZZAlreadyConnectedException;


    /** Remove the given cell from this dimension.
     */
    void excise(Cell c);


    void hop(Cell c, int steps);


    /** Insert a whole rank inside another rank.
     *  @param c The cell the rank is to be inserted at.
     *  @param d The headcell of the rank to be inserted.
     *  @throws IllegalArgumentException if <code>d</code> has a negwards
     *          connection, i.e. is not a headcell, or on a looping rank.
     */
    void insertRank(Cell c, int dir, Cell d);


    /** Excise a whole range of cells from a rank.
     *  Makes the excised range an independent, non-looping rank.
     *  @param from The negmost cell in the range to be excised.
     *  @param to The posmost cell in the range to be excised.
     *  @throws IllegalArgumentException if <code>from</code> and 
     *                                   <code>to</code> are not on the same
     *                                   rank, or not in the correct order.
     *                                   (XXX Behavior then?)
     */
    void exciseRange(Cell from, Cell to);


    /** Iterate starting from the given cell.
     * The iterator stops when going back over to c
     * on circular ranks, or at end of ranks for normal ranks.
     * Start from the headcell to go over all cells on the rank
     * once, independent of whether it is circular or not.
     */
    Iterator iterator(Cell c);

    /** Canonicalize the cells of this dimension.
     * XXX This is a part of the ugly hack to make 
     * Cell.inclusionObject work with CompoundSpace.
     * Because we may make several connections when including
     * a space with the non-canonical Cell objects which are 
     * slower to handle, this function may be called afterwards 
     * to request the Cell objects that this Dim contains
     * to be canonicalized using Space.getCell(cell.id).
     * <p>
     */
    void canonicalizeCells();
}
