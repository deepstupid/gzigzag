/*   
LineInfo.java
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

/** An interface for line breaking algorithms to query about line widths.
 * The LineBreaker interface allows many different line breaking algorithms.
 * This class allows those algorithms to ask about various possible breaks
 * within the line.
 */

public interface LineInfo {
String rcsid = "$Id: LineInfo.java,v 1.5 2000/09/19 10:32:00 ajk Exp $";

    /** The maximum token index.
     */
    int getMax();

    int getCursor();

    int getCenterLine();

    /** Give the best token between x0 and x1 that splits the interval
     * by approximately fract.
     * @return An array: first element is index, second penalty
     */ 
    void split(int x0, int n, int sug, int[] ret);

    /** Give the penalty of fitting the tokens from x0 to x1 on the
     * given line. 
     * @return An array: first element 1000*width fraction, second element 
     *		penalty
     */
    void widthPenalty(int x0, int n, int line, int[] ret);
}

