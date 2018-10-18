/*
CharRangeIter.java
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
 * Written by Benja Fallenstein
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.util.*;
import java.awt.*;

/** A filter that adds anchor vobs when it receives an
 *  <code>object()</code> callback with the connection cell.
 */

public class ConnectionFilter extends CharRangeIter.Filter {
String rcsid = "$Id: ConnectionFilter.java,v 1.1 2001/10/21 12:58:12 tjl Exp $";

    public ConnectionFilter(CharRangeIter iter, TextStyle style,
			    BuoyPlacer placerLeft, 
			    BuoyPlacer placerRight,
			    CellVobFactory cvf) {
	super(iter);
	this.style = style;
	this.placerLeft = placerLeft;
	this.placerRight = placerRight;
	this.cvf = cvf;
    }

    TextStyle style;
    BuoyPlacer placerLeft;
    BuoyPlacer placerRight;
    CellVobFactory cvf;

    boolean marked = false;

    public void range(Object tag, char[] chars, int from, int to) {
	marked = false;
	super.range(tag, chars, from, to);
    }

    /** Make a buoy representing <code>c</code> or whatever <code>c</code>
     *  is in.
     */
    public BuoyPlacer.Buoy makeBuoy(Cell c) {
	Cell stream = c.h(Dims.d_vstream_id);
	Vob v = cvf.new CellVob(c, style.font.getFont(1000), 
				style.font.getFontMetrics(1000), null);
	return new SimpleBuoy(100, 40, 40, 20, true, 1, v);
    }

    public void object(Object o) {
	if(o instanceof Cell) {
	    Cell c = (Cell)o;
	    Cell d = c.s(Dims.d_link_id, -1), e = c.s(Dims.d_link_id, 1);
	    if((d != null && !d.equals(c))) {
		super.object(new BuoyAnchor(c, style, marked, placerLeft,
					    makeBuoy(d)));
	    }
	    if((e != null && !e.equals(c) && !e.equals(d))) {
		super.object(new BuoyAnchor(c, style, marked, placerRight,
					    makeBuoy(e)));
	    }
	}

        if(o == CURSOR)
            marked = true;
        else
            marked = false;

	super.object(o);
    }
}

