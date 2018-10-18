/*   
BeamDecor.java
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
import java.awt.event.*;
import java.util.*;

/** A class for drawing beam decorations.
 * Beams are currently irregular octagons.
 * NOTE: Beams are rendered in reverse order: the first beam put in here
 * becomes the topmost.
 */

public class BeamDecor extends CoordDecor {
public static final String rcsid = "$Id: BeamDecor.java,v 1.4 2000/09/19 10:31:59 ajk Exp $";
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    Color[] colors;

    /** A simple class that knows how to efficiently build
     * these things.
     */
    static public class Builder extends CoordDecor.Builder {
	Color []col;
	public Builder(FlobSet into) {
	    super(into);
	}

	protected void makeRoom(int l) {
	    if(col != null && nth >= col.length)
		endl();
	    super.makeRoom(l);
	}

	protected void alloc() {
	    super.alloc();
	    col = new Color[np/16];
	}
	/** Make a beam.
	 * Coords: first 8x, then 8y.
	 */
	public final void b(int []xy, Color c) {
	    makeRoom(16);
	    p("beam: "+p+" "+col+" "+curp+" "+nth);
	    for(int i=0; i<16; i++)
		p[curp+i] = xy[i];
	    col[nth] = c;
	    curp += 16;
	    nth++;
	}
	protected Renderable create() {
	    Color[] col2 = col;
	    col = null;
	    return new BeamDecor(p, np, d, col2);
	}

    }

    public BeamDecor(int[] coords, int n, int d, Color[] cols) {
	super(coords, n, d);
	this.colors = cols;
    }


    public void render(Graphics g) {
	Color old = null;
	old = g.getColor();

	int[] x = new int[8];
	int[] y = new int[8];
	for(int i=(n-16)-n%16; i>=0; i-=16) {
	    for(int k = 0; k<8; k++) 
		x[k] = coords[i+k];
	    for(int k = 0; k<8; k++) 
		y[k] = coords[i+8+k];

	    g.setColor(colors[i/16]);
	    g.fillPolygon(x, y, 8);
	}

	g.setColor(old);
    }

}
