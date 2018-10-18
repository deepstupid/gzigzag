
/*   
BuoyView.java
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
package org.gzigzag.modules.pp;
import org.gzigzag.impl.View;
import org.gzigzag.impl.Cursor;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.Rectangle;
import java.awt.Dimension;
import org.gzigzag.modules.pp.vob.*;

/** A central plane and its buoys next to it on each side. At the moment can
 *  use only PlaneView, but should be easy to change if a similar, additional 
 *  <code>render</code> for subviews can be added to the View. 
 */

public class BuoyView implements View {
public static final String rcsid = "$Id: BuoyView.java,v 1.8 2002/03/02 16:34:28 vegai Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }

    // Seed for toArray to get the return type right
    Buoy[] seed = new Buoy[0];

    Buoy2 buoy2 = new Buoy2();

    /** Draw the whole view into a <code>VobScene</code>. 
     */
    public void render(VobScene into, Cell window) {
	Dimension d = into.getSize();

	PlaneView pl = new PlaneView();

	List leftBuoys = new ArrayList(),
	     rightBuoys = new ArrayList();

	// Render the central view and gather the buoys.
	pl.render(into, window, Cursor.get(window),
		d.width / 10, d.height / 15, (int)(d.width / 1.25), d.height*9/10,
		leftBuoys, rightBuoys);

	// Render the left and right buoys
	p("Place left buoys");
	Buoy[] store = (Buoy[])leftBuoys.toArray(seed);
	buoy2.place(into, store, new Rectangle(0, 0, d.width / 8, d.height));

	p("Place right buoys");
	store = (Buoy[])rightBuoys.toArray(seed);
	buoy2.place(into, store, new Rectangle((int)(4.5 * d.width / 5), 0, 
					d.width / 5, d.height));
    }
}

