/*   
Space.java
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
 * Written by Tuomas Lukka and Tuukka Hastrup
 */

package org.gzigzag;

/** A ZZ space - a set of cells and connections.
 */

public interface Space {
String rcsid = "$Id: Space.java,v 1.22 2001/10/04 06:42:22 tjl Exp $";

    /** Get the home cell of this space.
     * The home cell is where everything begins.
     */
    Cell getHomeCell();

    /** Get a cell by its ID.
     */
    Cell getCell(String id);

    /** Get a dimension by cell.
     * The same Dim object should be returned when this is
     * called with Cells that are clones of each other.
     */
    Dim getDim(Cell name);

    /** Get a dimension by cell id.
     */
    Dim getDim(String s);

    /** Get the clone dimension of this space.
     */
    Dim getCloneDim();

    /** Get the root clone of a cell.
     */
    Cell getRootclone(Cell c, Obs o);

    Cell getRootclone(Cell c);

    /** Create a new span containing the given character.
     * The cell argument is for use when the space is in several
     * slices and it is desirable to separate the texts somewhat.
     */
    // TextSpan getNewSpan(Cell c, char c);

    /** Create a new cell connected to the home slice.
     * Defined to be equivalent to N(getHomeCell()).
     */
    Cell N();


    // Delegated from Cell

    Cell N(Cell c);
    Cell N(Cell c, Dim dim, int dir, Obs o);
    Cell N(Cell c, Cell dim, int dir, Obs o);

    void delete(Cell c);


    void setSpan(Cell c, Span s);
    Span getSpan(Cell c, Obs o);

    void setText(Cell c, String s);
    String getText(Cell c, Obs o);

    // XXX Think
    // CellTexter getCellTexter();

    /** Get the Java object associated with this cell.
     *  XXX currently implementation-defined, except that it must return
     *  null if no object is associated with the cell.
     */
    Object getJavaObject(Cell c, Obs o);

    /** Whether a cell with the given ID exists in this space.
     */
    boolean exists(String id);

    /** Get the instance of mediaserver associated with this space.
     */
    org.gzigzag.mediaserver.Mediaserver getMediaserver();

    /** Get the cell representing the given mediaserver block
     * in the same slice as cell.
     */
    Cell getMSBlockCell(String msid, Cell cell);

}
