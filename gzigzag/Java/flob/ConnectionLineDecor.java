/*   
ConnectionLineDecor.java
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

/** A decoration of lines connecting flobs to each other.
 * Interpolation is done by this class. The coordinates are
 * given as points inside the flob's rectangle.
 */

public class ConnectionLineDecor extends Flob {
public static final String rcsid = "$Id: ConnectionLineDecor.java,v 1.5 2000/09/19 10:31:59 ajk Exp $";
    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    /** A class that incrementally builds these.
     */
    static public class Builder {
	FlobSet into;
	int curp; int np;
	Flob[] flobs;
	int[] p;
	Color col;
	int d;
	public Builder(FlobSet into, Color col) {
	    this.into = into;
	    this.col = col;
	}
	public final void startl(int n, int d) {
	    if(curp != 0) endl();
	    this.d = d;
	    curp = 0;
	    np = n;
	    p = null;
	    flobs = null;
	}
	public final void l(Flob f0, int x0, int y0, Flob f1, int x1, int y1) {
	    if(p==null) {
		p = new int[np * 4];
		flobs = new Flob[np * 2];
	    } else if(curp >= np) {
		endl();
		p = new int[np * 4];
		flobs = new Flob[np * 2];
		curp = 0;
	    }
	    flobs[curp*2 + 0] = f0;
	    flobs[curp*2 + 1] = f1;
	    p[curp*4 + 0] = x0;
	    p[curp*4 + 1] = y0;
	    p[curp*4 + 2] = x1;
	    p[curp*4 + 3] = y1;
	    curp ++;
	}
	public void endl() {
	    if(p==null) return;
	    into.add(new ConnectionLineDecor(flobs, p, curp, col, d));
	    p = null; flobs = null; curp = 0;
	}
    }

    Color color;
    int n;
    Flob[] flobs;
    int[] coords; // 256th parts

    public ConnectionLineDecor(Flob[] flobs, int[] coords, int n, int d) {
	this(flobs, coords, n, null, d);
    }

    /** Usual constructor.
     * @param coords The coordinates for the lines, given as
     *  	     quadruplets x1, y1, x2, y2.
     */
    public ConnectionLineDecor(Flob[] flobs, int[] coords, int n, Color color, int d) {
	// XXX Is size 0x0 right?
	super(0, 0, d, 0, 0, null);
	this.flobs = flobs;
	this.coords = coords;
	this.n = n;
	this.color = color;
    }

    public void render(Graphics g) {
	Color old = null;
	if(color != null) {
	    old = g.getColor();
	    g.setColor(color);
	}
	g.setColor(Color.black);
	p("Drawing flobconns");

	for(int i=0; i<n; i++) {
	    Flob f1 = flobs[i*2];
	    Flob f2 = flobs[i*2+1];
	    p("Draw: "+
		f1.x+" "+f1.y+" "+f1.w+" "+f1.h+" "+
		f2.x+" "+f2.y+" "+f2.w+" "+f2.h+" "+
		coords[i*4]+" "+
		coords[i*4+1]+" "+
		coords[i*4+2]+" "+
		coords[i*4+3]+" ");
	    g.drawLine(
		    f1.x + (f1.w * coords[i*4])/256, 
		    f1.y + (f1.h * coords[i*4+1])/256, 
		    f2.x + (f2.w * coords[i*4+2])/256, 
		    f2.y + (f2.h * coords[i*4+3])/256
		    );
	}

	if(old != null)
	    g.setColor(old);
    }

    public void renderInterp(Graphics g, float fract) {
	Color old = null;
	if(color != null) {
	    old = g.getColor();
	    g.setColor(color);
	}
	g.setColor(Color.black);

	for(int i=0; i<n; i++) {
	    Flob f1 = flobs[i*2];
	    Flob f1i = f1.interpTo;
	    Flob f2 = flobs[i*2+1];
	    Flob f2i = f2.interpTo;
	    int c1x = f1.x + (f1.w * coords[i*4])/256;
	    int c1y = f1.y + (f1.h * coords[i*4+1])/256;
	    int c2x = f2.x + (f2.w * coords[i*4+2])/256; 
	    int c2y = f2.y + (f2.h * coords[i*4+3])/256;
	    int ic1x = f1i.x + (f1i.w * coords[i*4])/256;
	    int ic1y = f1i.y + (f1i.h * coords[i*4+1])/256;
	    int ic2x = f2i.x + (f2i.w * coords[i*4+2])/256; 
	    int ic2y = f2i.y + (f2i.h * coords[i*4+3])/256;

	    g.drawLine(
		    (int)(c1x + fract * (ic1x - c1x)),
		    (int)(c1y + fract * (ic1y - c1y)),
		    (int)(c2x + fract * (ic2x - c2x)),
		    (int)(c2y + fract * (ic2y - c2y))
		    );
	}

	if(old != null)
	    g.setColor(old);
    }

}

