/*
Buoy.java
 *    
 *    Copyright (c) 2000, Tuomas Lukka
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
import java.util.*;
import java.awt.*;

/** A floating element that can be placed somewhere.
 * The buoys are allowed to size themselves naturally, i.e.
 * the ones away from the center may be smaller by default
 * than the ones near the center.
 */

public interface Buoy {
    Vob getAnchor();
    Dimension getPreferredSize(Dimension dim);
    void put(VobScene into, int depth, int x, int y, int w, int h);
}
