/*   
Colorer.java
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

/** An interface for coloring cell background -like things.
 * The information about how  many colors are appropriate should
 * move separately from this.
 */

public interface Colorer {
String rcsid = "$Id: Colorer.java,v 1.1 2000/10/26 18:15:05 tjl Exp $";
    /** Add the given color to the set of colors to be shown in the background,
     * or whatever.
     * It is assumed that the colors come more significant first, so that
     * Colorers are free to take any number of the first ones and ignore
     * the rest.
     * @return Whether the rest of the colors will be ignored.
     */
    boolean addColor(Color c);
}
