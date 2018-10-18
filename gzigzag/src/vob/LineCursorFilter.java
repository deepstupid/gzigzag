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

package org.gzigzag.vob;
import java.util.*;
import java.awt.*;

/** A filter that adds line cursor vobs when it receives a
 *  <code>CharRangeIter.CURSOR</code> <code>object()</code> callback.
 */

public class LineCursorFilter extends CharRangeIter.Filter {
String rcsid = "$Id: LineCursorFilter.java,v 1.2 2001/08/12 14:28:45 bfallenstein Exp $";

    public LineCursor vob;

    public LineCursorFilter(CharRangeIter iter, TextStyle style, Color col) {
	super(iter);
	vob = new LineCursor(col, style);
    }

    public void object(Object o) {
	if(o == CURSOR)
	    super.object(vob);

	super.object(o);
    }
}
