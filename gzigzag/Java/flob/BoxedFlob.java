/*   
BoxedFlob.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2001, Benja Fallenstein
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
 * Written by Tuomas Lukka, adapted by Benja Fallenstein
 */

package org.gzigzag;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** A flob which gets rendered by FlobSet with a background.
 *  XXX support for different shapes than rectangular?
 *  XXX replace Colorer entirely by this?
 *  XXX renderer must know whether background is transparent!
 *  XXX move needsBox here and default to true?!? replace by instanceof Boxed?
 */

public class BoxedFlob extends Flob implements Colorer {
public static final String rcsid = "$Id: BoxedFlob.java,v 1.1 2001/03/20 11:08:36 ajk Exp $";
    public static final boolean dbg = true;
//    static final void p(String s) { if(dbg) System.out.println(s); }
//    static final void pa(String s) { System.out.println(s); }

// XXX need this?    boolean drawBorder = true;

    public boolean needsBox() { return true; }

    int nsolids = 0;
    Color[] solids;
    protected Color bg = Color.white;

    public Object hit(int x0, int y0) {
	if( x0 >= x && y0 >= y && x0 < x+w && y0 < y+h )
	    return c;
	return null;
    }

    public void setBg(Color c){ if( c != null ) bg = c; }

    /** Adds one more solid color to be drawn inside
     * the cell.
     */
    public boolean addColor(Color c) {
	if(solids == null || nsolids >= solids.length) {
	    Color[] n= new Color[nsolids + 10];
	    if(solids != null) System.arraycopy(solids, 0, n, 0, nsolids);
	    solids = n;
	}
	solids[nsolids++] = c;
	return false;
    }
    /** The currently put solid colors.
     * null = none. There may be null references near the end
     * of the array. Mostly useful for checking for nullness.
     */
    public Color[] getSolidColors() { return solids; }

    public BoxedFlob(int x, int y, int d, int w, int h, 
	    ZZCell c) {
	super(x, y, d, w, h, c);
    }
}


