/*   
LinebreakingUtil.java
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

/** Some more complex helper functions that can be used by linebreakers.
 */

public class LinebreakingUtil {
String rcsid = "$Id: LinebreakingUtil.java,v 1.1 2001/11/03 18:34:43 bfallenstein Exp $";

    public static final int GLUE_LENGTH = 0;
    public static final int GLUE_STRETCH = 1;
    public static final int GLUE_SHRINK = 2;

    /** Compute the stretch factor for a line given the part of the chain that
     ** will form the line, and the width of the line.
     *  XXX This is a GUESS, I don't really know what this thing does. 
     *  Am I right? --Benja
     */
    public static double stretchFactor(LinebreakableChain ch,
				       int start, int end, 
				       int scale, int width) {
	int wid = 0;   // Width
	int gwid = 0;
	int str = 0;   // Stretch
	int shr = 0;   // Shrink
	
	int[] glues = ch.glues;
	for(int i=start; i<end; i++) {
	    wid += ch.boxes[i].getWidth(scale);
	    if(i > start /* || i == 0 */ ) { // XXX Think out and test
		gwid += glues[i*3 + GLUE_LENGTH];
		str += glues[i*3 + GLUE_STRETCH];
		shr += glues[i*3 + GLUE_SHRINK];
	    }
	}

	gwid *= scale; gwid /= 1000;
	str *= scale; str /= 1000;
	shr *= scale; shr /= 1000;

	wid += gwid;
	if(wid < width) {
	    if(shr == 0) return 1000;
	    return (width-wid) / (double)str;
	} else if(wid > width) {
	    if(str == 0) return -1000;
	    return (width-wid) / (double)shr;
	}
	return 0;
    }

    /** Get the badness of fitting the given tokens
     * into the width.
     * XXX Not yet complete information: also need prevention
     * of neighbouring loose lines etc.
     * See TeX source, section 851, 859.
     */
    public static int badness(LinebreakableChain ch,
		              int start, int end, int scale, int width) {
	int str = (int) (1000 * stretchFactor(ch, start, end, scale, width));
	if(str < 0) return -str;
	return str;
    }

    /** Put a line into the given vobPlacer.
     * @param x,y The x and y coordinate of the leftmost point
     *			of the baseline.
     * @param start,end The index of the first and the index after the last
     * 			box to place on the line.
     */
    public static void putLine(LinebreakableChain ch, VobPlacer into, 
			       int x, int y, int w, int d,
			       int start, int end, int scale) {
	double sf = stretchFactor(ch, start, end, scale, w);
	int wid = 0;
	double over = 0;

	HBox[] boxes = ch.boxes;
	int[] glues = ch.glues;
	for(int i=start; i<end; i++) {
	    int curwid = boxes[i].getWidth(scale);
	    int curhei = boxes[i].getHeight(scale);
	    int curdep = boxes[i].getDepth(scale);

	    double dw = curwid;
	    dw += glues[3 * (i+1) + GLUE_LENGTH];
	    if(i < end-1) 
		dw += (sf > 0 ? sf * glues[3 * (i+1) + GLUE_STRETCH] :
			 sf * glues[3 * (i+1) + GLUE_SHRINK]) 
		         // XXX make testcase of both loose and tight lines
			+ over;

	    curwid = (int)dw;
	    over = dw - curwid;

	    into.put(boxes[i].getVob(scale), d,
		    x + wid, y - curhei, curwid, curhei + curdep);
	    boxes[i].setPosition(d, x + wid, y - curhei, curwid, 
				 curhei + curdep);
	    wid += curwid;
	}
    }
}



