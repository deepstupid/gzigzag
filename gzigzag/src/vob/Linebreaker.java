/*   
Linebreaker.java
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
 * Written by Tuomas Lukka and Benja Fallenstein
 */
package org.gzigzag.vob;
import java.util.*;

/** Interface to a linebreaking algorithm.
 *  A linebreaker is responsible for deciding how a
 *  <code>LinebreakableChain</code> should be broken into lines.
 *  @see org.gzigzag.vob.LinebreakableChain
 */

public interface Linebreaker {
String rcsid = "$Id: Linebreaker.java,v 1.1 2001/11/03 18:35:40 bfallenstein Exp $";

    int GLUE_LENGTH = 0;
    int GLUE_STRETCH = 1;
    int GLUE_SHRINK = 2;

    /** Perform simple line-breaking.
     * @param lines An array of line widths in pixels
     * @param scales an array of the scales of the individual lines
     * @param ctoken The index of the cursor in this lbchain.
     * @param crow The index of the row in lines that the token
     *		indicated by ctoken should land on
     * @param into An array that contains the starts of the lines
     *	     in terms of tokens in this box.
     * @return A configuration of lines as a <code>Broken</code> object.
     */
    Broken breakLines(LinebreakableChain chain,
		      int[] lines, int[] scales, int ctoken, int crow);



    /** A configuration of lines with different scales.
     *  This is basically a list of lines, where each line has: the index
     *  of the first box in that line; the index after the last box in that
     *  line; and the scale at which that line shall be placed.
     *  <p>
     *  In addition, some other information is stored too... XXX doc
     */
    class Broken {
	LinebreakableChain ch;
		
	public int[] lineWidths;
	public int[] lineScales;

	public int firstLine;
	public int endLine;
	public int height;
	/** Difference between current and next line's baseline.
	 */
	public int[] lineHeights; 
	public int[] tokenStarts;

	/** Put this configuration of lines into a <code>VobPlacer</code>.
	 *  This calls <code>LinebreakableChain.putLine</code> for each
	 *  individual line.
	 *  @param x,y The coordinates of the upper left corner.
	 */
	public void putLines(VobPlacer into, 
			    int x, int y, int d) {

	    for(int line = firstLine; line < endLine; line ++) {
		y+= lineHeights[line];
		int scale;
		if(line >= lineScales.length) 
		    scale = lineScales[lineScales.length-1];
		else 
		    scale = lineScales[line];
		
		LinebreakingUtil.putLine(ch, into, x, y, 
					 lineWidths[line], d, 
					 tokenStarts[line], 
					 tokenStarts[line+1], 
					 scale);
	    }

	}
	
    }
}



