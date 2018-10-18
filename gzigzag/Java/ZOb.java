/*   
ZOb.java
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

/** An object whose data is read from the structure.
 * This is an interface implemented by all the objects generated from
 * .zob files by <code>zob2java.pl</code>.
 */

public interface ZOb {
    /** Read the parameters for this ZOb.
     * Returns a string describing the errors encountered, or null if
     * everything went fine..
     */
    String readParams(ZZCell c);
}
 
