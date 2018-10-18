/*   
SimpleDimChangeList.java
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
package org.gzigzag.impl;
import org.gzigzag.*;
import java.util.*;

/** A list of changes to a single simple-dimension between two versions.
 * Simply a list of connects and disconnects.
 * For convenience, the previous connection targets are included.
 * <p>
 * Also includes methods for chunking the changes.
 */

public class SimpleDimChangeList {
public static final String rcsid = "$Id: SimpleDimChangeList.java,v 1.9 2001/07/17 20:39:02 tjl Exp $";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    public Dim from, to;
    

    /** The quarks, i.e.changed connections.
     * Format: groups of 4 cells,
     * negend, negend's previous connection, posend's previous connection,
     * posend.
     * <p>
     * <b>Important: 
     *  Assumes that input is compressed: only one record per cell.
     * <pre>
     * (benja) btw, I don't understand the "compressed" comment in the simple changelist doc
     *	(tuomasl) if you change something and change it again
     *	(tuomasl) only the last state should be on the list
     * </pre>
     * </b>
     *
     */
    public Cell[] quarks;
    int nquarks;
    int[] hashtable; // linear probing by cell id

    void alloc(int n) {
	Cell[] nquarks = new Cell[4*n];
	if(quarks != null) System.arraycopy(quarks, 0, nquarks, 0, quarks.length);
	quarks = nquarks;
    }

    /** Add a new change.
     * XXX --- should we make this into its own interface?
     */
    public void addChange(Cell neg, Cell prevpos, Cell prevneg, Cell pos) {
	if(quarks == null || quarks.length == 0) alloc(20);
	if(quarks.length/4 == nquarks) alloc(nquarks*2);
	quarks[nquarks*4 + 0] = neg;
	quarks[nquarks*4 + 1] = prevpos;
	quarks[nquarks*4 + 2] = prevneg;
	quarks[nquarks*4 + 3] = pos;
	nquarks ++;
    }

}
