/*   
SpanView.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag.client;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import org.gzigzag.impl.Cursor;
import org.gzigzag.vob.*;
import java.util.*;
import java.awt.*;

/** A view for image spans' connections.
 */

public class SpanView implements View {
public static final String rcsid = "$Id: SpanView.java,v 1.5 2002/03/25 20:33:08 tjl Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }

    VobVanishingClient main = new VobVanishingClient();

    VobVanishingClient side = new VobVanishingClient();
    {
	side.vanishing.initmul = 0.9f;
	side.vanishing.raster.depth = 2;
	side.usezoom = false;
    }

    static public void uncacheSpanSet() { cachedSpanset = null; }
    static SpanSet cachedSpanset; // XXX blech

    public void render(VobScene into, Cell window) {
	Cell center = Cursor.get(window);
	Dimension d = into.getSize();
	// main.resetCellConnector();
	// side.resetCellConnector();
	main.cellConnector.restart();
	side.cellConnector.restart();
	main.render(into, window, d.width*1/3, d.height/2);
	Span s = center.getSpan();
	if(s != null)
	{
	    if(cachedSpanset == null)  // XXX static...
		cachedSpanset = ((SimpleTransientSpace)center.space).getSpanSet();
	    Collection c = cachedSpanset.overlaps(s);
	    int n = c.size();

	    int i = 1;
	    for(Iterator iter = c.iterator(); iter.hasNext(); ) {
		Cell cell = (Cell)iter.next();
		if(cell.equals(center)) continue;
		side.render(into, window, cell, d.width * 4 / 5, 
				(int)(d.height * (float)i / (n+1)));
		// XXX Draw line between centers!
		i++;
	    }
	}
    }
}
