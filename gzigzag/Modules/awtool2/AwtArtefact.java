/*   
AwtArtefact.java
 *    
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
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
 * Written by Kimmo Wideroos
 */
 
package org.gzigzag.module;
import org.gzigzag.*;
import java.awt.*;
import java.util.*;

/** AwtArtefact acts as an awt note interface to zz-structure
 *  It can be used also indepentent from the structure, the 
 *  object can be later rollbacked into zz-structure.
 */

public class AwtArtefact implements AwtDraggable, AwtResizable, AwtAccursable {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    private ZZCell cell = null;
    private double x;
    private double y;
    private double width;
    private double height;
    // is external structure up-to-date
    private boolean valueSync;

    public AwtArtefact(ZZCell c) {
        init(c);
        double[] coord = getCoord(c);
        double[] dim = getDimension(c);
        this.x = coord[0];
        this.y = coord[1];
        this.width = dim[0];
        this.height = dim[1];
	cell=c;
        valueSync = true;
    }

    public AwtArtefact(double[] coord, double[] dimension, ZZCell c) {
        this.x = coord[0];
        this.y = coord[1];
        this.width = dimension[0];
        this.height = dimension[1];
	cell=c;
	rollback(c);
    }

    public AwtArtefact(double[] coord, double[] dimension) {
        this.x = coord[0];
        this.y = coord[1];
        this.width = dimension[0];
        this.height = dimension[1];
	valueSync = false;
    }

    public ZZCell getCell() { return cell; }

    static public double[] getCoord(ZZCell c) {
        return new double[] {Double.valueOf(c.s("d.1",1).getText()).doubleValue(),
                             Double.valueOf(c.s("d.1",2).getText()).doubleValue()};
    }

    static public double[] getDimension(ZZCell c) { 
	return new double[] {Double.valueOf(c.s("d.1",3).getText()).doubleValue(), 
                             Double.valueOf(c.s("d.1",4).getText()).doubleValue()}; 
    }

    static public void setCoord(double x, double y, ZZCell c) {
        ZZCell tc;
	if(c.s("d.1") == null) init(c);
        tc = c.s("d.1", 1);
	tc.setText(""+x);
        tc = c.s("d.1", 2);
	tc.setText(""+y);
    }

    static public void setDimension(double w, double h, ZZCell c) { 
        ZZCell tc;
	if(c.s("d.1") == null) init(c);
        tc = c.s("d.1", 3);
	tc.setText(""+w);
        tc = c.s("d.1", 4);
	tc.setText(""+h);
    }

    static public boolean valid(ZZCell c) { 
        if(c.s(AwtDim.d_member, -1) != null) return true;
        return false;
    }

    public double[] getCoord() { 
	return new double[] { x, y }; 
    }

    public double[] getDimension() { 
	return new double[] { width, height }; 
    }

    public void setCoord(double x, double y) {
	this.x = x;
	this.y = y;
	valueSync = false;
    }

    public void setDimension(double w, double h) { 
        this.width = w;
        this.height = h; 
	valueSync = false;
    }

    /* update values in structure
     */
    public void release() {
	// XXX should throw exception here
	if(cell==null) return;
	setCoord(x, y, cell);
	setDimension(width, height, cell);
	AwtNile.initSpanCursors(cell);
	valueSync = true;
    }

    public void rollback(ZZCell c) {   
        init(c);
	cell=c;
	release();
    }

    public boolean getUpdateStatus() {
	return valueSync;
    }

    public static void clone_style(ZZCell origin, ZZCell target) {
	if(origin.s(AwtDim.d_style) == null) return;
	if(target.s(AwtDim.d_style) != null) {
	    target.s(AwtDim.d_style).delete();
	}
	target.connect(AwtDim.d_style, 1, origin.s(AwtDim.d_style).zzclone());
    }

    private static void init(ZZCell c) {
        ZZCell imgs, nc = c;
        for(int i = 0; i<4; i++)
            nc = nc.getOrNewCell("d.1");
	if(c.s(AwtDim.d_style) != null) return;
	imgs = AwtUtil.findLocals("Images", c.getSpace().getHomeCell(), false);
	if(imgs == null) return;
	c.connect(AwtDim.d_style, 1, imgs.s("d.1", 2).zzclone());
    } 

}
