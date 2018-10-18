/*   
LinebreakableChain.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
package org.gzigzag.vob;
import java.util.*;

/** A chain with support for linebreaking operations.
 * This chain knows about where lines can be broken, at what cost, and
 * the amount of glue (as in TeX) on a given line.
 * @see Linebreaker, AbstractLinebreaker, SimpleLinebreaker
 */

public class LinebreakableChain {
String rcsid = "$Id: LinebreakableChain.java,v 1.24 2001/10/25 12:31:01 bfallenstein Exp $";

    public static final int GLUE_LENGTH = 0;
    public static final int GLUE_STRETCH = 1;
    public static final int GLUE_SHRINK = 2;

    int nboxes;
    protected HBox[] boxes;

    public LinebreakableChain() { 
	allocCopy(20);
    }

    /** For each Vob in boxes, three values: 
     * natural, plus, minus.
     * This is glue that comes <strong>before</strong> the
     * vob.
     * XXX Should glues also be virtual and have scale?
     * fonts scale non-uniformly, so should glue?
     */
    protected int[] glues; 


    public void addBox(HBox box) {
        ensureBoxes(nboxes+1, false);
        int ind = nboxes++;
        boxes[ind] = box;
        if(ind > 0)
            box.setPrev(boxes[ind-1]);
    }

    public void addGlue(int len, int str, int shr) {
        glues[nboxes*3 + GLUE_LENGTH] += len;
        glues[nboxes*3 + GLUE_STRETCH] += str;
        glues[nboxes*3 + GLUE_SHRINK] += shr;
    }

    /** Return the length of, i.e. the number of HBoxes in, this vob chain. */
    public int length() {
	return nboxes;
    }

    /** Return the <code>n</code>th box in this chain. */
    public HBox getBox(int n) {
	return boxes[n];
    }

    /** Return one of the values of the glue 
     * before the <code>n</code>th box in this chain. 
     *  @param property <code>GLUE_LENGTH</code>, <code>GLUE_STRETCH</code>,
     *                  or <code>GLUE_SHRINK</code>.
     */
    public int getGlue(int n, int property) {
	if(property < 0 || property > 2)
	    throw new IllegalArgumentException("illegal property: "+property);
	return glues[(n * 3) + property];
    }





    /** Expand the arrays to contain space for <code>n</code> boxes.
     *  This is called by <code>ensureBoxes</code> when there is not enough
     *  space in the arrays. It simply creates new, larger arrays and
     *  copies the values from the old arrays into the new ones.
     */
    void allocCopy(int n) {
	HBox[] nboxes = new HBox[n];
	int[] nglues = new int[3*(n+1)];
	if(boxes != null) {
	    System.arraycopy(boxes, 0, nboxes, 0, boxes.length);
	    System.arraycopy(glues, 0, nglues, 0, glues.length);
	}
	boxes = nboxes;
	glues = nglues;
    }

    /** Ensure that at least <code>n</code> boxes fit into the arrays.
     *  Checks whether <code>n</code> is larger than the number of boxes
     *  that currently fit into the arrays, and if so, calls 
     *  <code>allocCopy</code> to increase the arrays' size.
     *  @param accurate If the arrays don't suffice, whether to create
     *  		arrays exactly of size <code>n</code>. Setting this
     *  		to true is only reasonable if it is expected that no
     *  		more than <code>n</code> boxes will be added to
     *  		this chain.
     */
    void ensureBoxes(int n, boolean accurate) {
	if(boxes == null) allocCopy(accurate ? n : n > 50 ? n : 50);
	if(boxes.length < n) {
	    int def = 2 * boxes.length;
	    allocCopy(accurate ? n : n > def ? n : def);
	}
    }




    /** Get the height of a line given the index of the first and the
     *  index after the last box in that line. The height of the line is
     *  simply the maximum of the heights of the individual boxes at
     *  the given scale.
     */
    public int getHeight(int start, int end, int scale) {
	int h = 0;
	for(int i=start; i<end; i++) {
	    int curh = boxes[i].getHeight(scale);
	    if(curh > h) h = curh;
	}
	return h;
    }

    /** Get the depth of a line given the index of the first and the
     *  index after the last box in that line. The depth of the line is
     *  simply the maximum of the heights of the individual boxes at
     *  the given scale.
     */
    public int getDepth(int start, int end, int scale) {
	int d = 0;
	for(int i=start; i<end; i++) {
	    int curd = boxes[i].getDepth(scale);
	    if(curd > d) d = curd;
	}
	return d;
    }





    /** A style of glue.
     *  Constructed from a text style, a glue style represents the kind of
     *  glue that matches the text style (so that a space glue is as wide
     *  as a space of that text style, etc.). When glue needs to be added
     *  to the chain, call <code>GlueStyle.addSpace</code>,
     *  <code>GlueStyle.addCommaSpace</code>, or 
     *  <code>GlueStyle.addSentenceSpace</code>. (All three do the same
     *  thing currently, but subclasses can override the behavior.)
     */
    static public class GlueStyle {
	public GlueStyle(TextStyle st) {
	    spc_g = st.getWidth(" ", 1000);
	    spc_p = spc_g / 2;
	    spc_m = spc_g / 2;
	    if(spc_p == 0) spc_p = 1;
	}
	int spc_g, spc_p, spc_m;
	public void addSpace(LinebreakableChain ch) { 
	    ch.addGlue(spc_g, spc_p, spc_m);
	}
	public void addCommaSpace(LinebreakableChain ch) { 
	    ch.addGlue(spc_g, spc_p, spc_m);
	}
	public void addSentenceSpace(LinebreakableChain ch) { 
	    ch.addGlue(spc_g, spc_p, spc_m);
	}
    }

}



