/*   
VobPlacer.java
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
package org.gzigzag.vob;

/** A scene into which Vobs are placed.
 */

public interface VobPlacer {
    /** Place a <code>Vob</code> onto this scene. Coordinates of the scene's
     *  designated area run from (0,0) to (w-1, h-1).
     *  @param vob   the vob to be included
     *  @param depth value for use in depth sorting
     *  @param x     vob top-left corner x-coordinate
     *  @param y     vob top-right corner y-coordinate
     *  @param w     vob width in pixels
     *  @param h     vob height in pixels
     */
    void put(Vob vob, int depth, int x, int y, int w, int h);
    /** Determines size of the area designated for the <code>Vob</code>s.
     *  At least at the moment doesn't include margins.
     *  @return dimensions of the scene area in pixels
     */
    java.awt.Dimension getSize();
}

