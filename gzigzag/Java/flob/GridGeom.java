/*   
GridGeom.java
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
import java.awt.*;

/** An object that describes the geometry of a rectangular grid.
 * The grid is assumed to have a center cell i.e. to have an odd
 * number of cells.
 */

public abstract class GridGeom {
String rcsid = "$Id: GridGeom.java,v 1.1 2000/10/21 23:07:35 tjl Exp $";
    /** Initialize the sizes of this grid.
     * @param normalcell The size of a "normal" cell.
     * @param canvas The size of the whole rectangle this grid is placed
     *               into
     * @return the number of cells in the grid in X and Y directions.
     */
    public abstract Dimension setSizes(Dimension normalcell, Dimension canvas);
    /** Returns the rectangle corresponding to the given cell.
     */
    public final Rectangle getCell(int x, int y) {
	return getCell(x, y, new Rectangle());
    }
    /** Returns the rectangle corresponding to the given cell,
     * placed into the given Rectangle object.
     */
    public abstract Rectangle getCell(int x, int y, Rectangle rect);
}

// vim: set syntax=java :
