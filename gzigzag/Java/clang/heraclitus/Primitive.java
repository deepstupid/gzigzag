/*   
Primitive.java
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
package org.gzigzag.heraclitus;
import org.gzigzag.*;

/** An Heraclitus Clang primitive.
 * A primitive, just like a subroutine, executes one cell with another
 * as a parameter, and returns another cell.
 */

public interface Primitive {
String rcsid = "$Id$";
    /** Execute this primitive.
     * @param instance The cell instance (clone) that was originally
     *			requested to be executed.
     * @param param The parameter given to the execution.
     */
    ZZCell execute(ZZCell instance, ZZCell param);
}


