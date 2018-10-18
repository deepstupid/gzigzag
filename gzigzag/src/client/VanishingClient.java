/*
VanishingClient.java
 *    
 *    Copyright (c) 2002, Tuomas Lukka
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
package org.gzigzag.client;
import org.gzigzag.impl.*;
import org.gzigzag.impl.Cursor;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.util.*;
import java.awt.*;

/** An interface abstracting some things away from the vanishing view.
 */
public interface VanishingClient {
    final int CENTER = 1;

    Object getVobSize(Cell c, float fract, int flags, Dimension into);
    void place(Cell c, Object o, float fract, int x0, int y0, int x1, int y1,
		int depth, float rot);

    /** There should be a connection between the given cells.
     * If one of the cells hasn't yet been placed, this means that 
     * a stub in that direction should be drawn.
     */
    void connect(Cell c1, Cell c2, int dx, int dy);
}
