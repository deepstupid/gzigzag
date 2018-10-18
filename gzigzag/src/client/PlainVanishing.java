/*
PlainVanishing.java
 *    
 *    Copyright (c) 1999-2002, Ted Nelson and Tuomas Lukka
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

/** A plain (no-constraint) version of vanishing view, useful as a building
 * block for imagespan views.
 * <p>
 * This class uses the VanishingClient interface to enquire about its context.
 * <p>
 * <b>If you want to add anything to this class, PLEASE check with Tuomas first.
 * He is very angry about having had to clean this beautiful, simple implementation
 * once.
 * </b>
 */

public class PlainVanishing {
public static final String rcsid = "$Id: PlainVanishing.java,v 1.11 2002/03/25 16:59:45 jvk Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }

    // XXX Assume single-threaded

    public float[] shrink = new float[] {0.9f, 0.9f, 0.9f, 0.9f};

    public float initmul = 1.6f;

    public float xgapmult = 1.5f;
    public float ygapmult = 1.5f;


    VanishingClient client;

    public BFRaster raster = new BFRaster();

    int ndims;

    public void render(VanishingClient client, Cell center, int px, int py, Dim[] dims) {
	this.client = client;
	ndims = 3;
	if(ndims > dims.length) ndims = dims.length;

	raster.read(center, dims);

	paint(0, px, py, 0, 0, initmul, 0, 0, 0, 0, 0);

	connect(0, 0);

    }

    Dimension dim = new Dimension();

    /** Go through the raster produced in render recursively
     * and paint.
     * The recursive passthrough is the most convenient for vanishing-type 
     * views.
     */
    protected void paint(int index, int x, int y, int d, int rdepth, 
			float fract, float xalign, float yalign,
			int pdx, int pdy, float rot) {
	if(raster.cells[index] == null) return;
	if(!raster.used[index]) return;
	if(rdepth >= raster.depth) return;
	rdepth ++;

	Object v = client.getVobSize(raster.cells[index], fract, 
		    (index == 0 ? VanishingClient.CENTER : 0),
		dim);

	// connection length taken into account
	int w = (int)(dim.width*xgapmult) / 2;
	int h = (int)(dim.height*ygapmult) / 2;

	double c = Math.cos(rot);
	double s = Math.sin(rot);

	x += c * (-xalign) * w + s * (-yalign) * h;
	y += c * (-yalign) * h - s * (-xalign) * w;

	client.place(raster.cells[index], v, fract, 
		    x-dim.width/2, y-dim.height/2,
			    x + dim.width/2, y + dim.height/2, d, rot);

	
	if(ndims > 0) {
	    paint(raster.ptrs[index], x + w, y, d + 40, rdepth, fract * shrink[0],
		    -1.0f, 0, 1, 0, 0);
	    paint(raster.ptrs[index]+1, x - w, y, d + 40, rdepth, fract * shrink[0],
		    1.0f, 0, -1, 0, 0);
	}
	if(ndims > 1) {
	    paint(raster.ptrs[index]+2, x, y + h, 
			(int)(d + 50 + d * 0.05), rdepth, fract * shrink[1],
		    0, -1.0f, 0, 1, 0);
	    paint(raster.ptrs[index]+3, x, y - h, 
		    (int)(d + 50 + d * 0.05), rdepth, fract * shrink[1],
		    0, 1.0f, 0, -1, 0);
	}
	if(ndims > 2) {
	    paint(raster.ptrs[index]+4, x + w, y + h, 
		    (int)(d + 60 + d * 0.1), rdepth, fract * shrink[1],
		    -1.0f, -1.0f, 0, 0, 0);
	    paint(raster.ptrs[index]+5, x - w, y - h, 
		    (int)(d + 60 + d * 0.1), rdepth, fract * shrink[1],
		    1.0f, 1.0f, 0, 0, 0);
	}

    }

    protected void connect(int index, int rdepth) {
	if(raster.cells[index] == null) return;
	if(!raster.used[index]) return;
	if(rdepth >= raster.depth) return;
	rdepth ++;

	// we only do connections in the positive direction to avoid duplication.
	// the raster contains all the necessary info.
	int i = raster.ptrs[index];

	int i2;

	if(ndims > 0 && raster.cells[i] != null)
		client.connect(raster.cells[index], raster.cells[i], 1, 0);
	if(ndims > 1 && raster.cells[i+2] != null)
		client.connect(raster.cells[index], raster.cells[i+2], 0, 1);
	if(ndims > 2 && raster.cells[i+4] != null)
		client.connect(raster.cells[index], raster.cells[i+4], 1, 1);


	if(ndims > 0) {
	    connect(i, rdepth);
	    connect(i+1, rdepth);
	}
	if(ndims > 1) {
	    connect(i+2, rdepth);
	    connect(i+3, rdepth);
	}
	if(ndims > 2) {
	    connect(i+4, rdepth);
	    connect(i+5, rdepth);
	}
	
    }
}
