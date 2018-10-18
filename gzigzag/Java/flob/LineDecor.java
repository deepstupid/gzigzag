/*   
LineDecor.java
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

public class LineDecor extends CoordDecor {
public static final String rcsid = "$Id: LineDecor.java,v 1.9 2000/10/26 18:15:58 tjl Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    Color color;

    /** A simple class that knows how to efficiently build
     * these things.
     */
    static public class Builder extends CoordDecor.Builder {
	Color col;
	public Builder(FlobSet into, Color col) {
	    super(into);
	    this.col = col;
	}
	/** Make a line.
	 */
	public final void l(int x0, int y0, int x1, int y1) {
	    makeRoom(4);
	    p[curp] = x0;
	    p[curp+1] = y0;
	    p[curp+2] = x1;
	    p[curp+3] = y1;
	    curp += 4;
	}
	protected Renderable create() {
	    p("Create linedecor "+p+" "+curp+" "+col+" "+d);
	    return new LineDecor(p, curp, col, d);
	}

    }

    public LineDecor(int[] coords, int n, int d) {
	this(coords, n, null, d);
    }

    /** Usual constructor.
     * @param coords The coordinates for the lines, given as
     *  	     quadruplets x1, y1, x2, y2.
     */
    public LineDecor(int[] coords, int n, Color color, int d) {
	super(coords, n, d);
	this.color = color;
    }

    public LineDecor(int x1, int y1, int x2, int y2, Color color, int d) {
	super(null, 4, d);
	this.coords = new int[] {
	    x1, y1, x2, y2
	};
	this.color = color;
	this.d = d;
    }

    public void render(Graphics g) {
	Color old = null;
	if(color != null) {
	    old = g.getColor();
	    g.setColor(color);
	}

	for(int i=0; i+3<n; i+=4) {
	    g.drawLine(coords[i], coords[i+1],
		    coords[i+2], coords[i+3]);
	}

	if(old != null)
	    g.setColor(old);
    }

}
