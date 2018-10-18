/*
BFRaster.java
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

/** A class representing a portion of space read out in a breadth-first
 * way.
 */

public class BFRaster {
public static final String rcsid = "$Id: BFRaster.java,v 1.3 2002/03/25 14:06:23 jvk Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }


    /** The cells of the raster.
     */
    public Cell[] cells = new Cell[200];

    /** The index of the "children" of a given cell.
     */
    public int[] ptrs = new int[cells.length];

    /** Whether a given index cell is "used", i.e.is to be 
     * painted here or was painted earlier.
     */
    public boolean[] used = new boolean[cells.length];

    public int depth = 5;

  
    public void read(Cell center, Dim[] dims) {
	boolean done = false;
	int ndims = dims.length;
	while(!done) {
	    // Create raster; loop until we have a large enough array
	    try {
		// try to create raster, not minding array overflows
		Set s = new HashSet(); // Cells we have gone through
		cells[0] = center;
		used[0] = true;
		int prevcell0 = 0; // first of previous layer of cells
		int prevcell1 = 1; // one past previous cells
		s.add(cells[0]);
		for(int i=0; i<depth; i++) {
		    // Expand the raster one depth step
		    int curcell = prevcell1;
		    for(int index = prevcell0; index<prevcell1; index++) {
			ptrs[index] = -1;
			// Add all neighbours of the cell into raster
			if(cells[index] == null) continue;
			if(!used[index]) continue;
			ptrs[index] = curcell;
			for(int d = 0; d<ndims; d++) {
			    // Add positive and negative neighbour
			    cells[curcell] = cells[index].s(dims[d], 1);
			    used[curcell] = !s.contains(cells[curcell]);
			    if(used[curcell]) s.add(cells[curcell]);
			    curcell++;
			    cells[curcell] = cells[index].s(dims[d], -1);
			    used[curcell] = !s.contains(cells[curcell]);
			    if(used[curcell]) s.add(cells[curcell]);
			    curcell++;
			}
		    }
		    prevcell0 = prevcell1;
		    prevcell1 = curcell;
		}
		
		done = true;
	    } catch(ArrayIndexOutOfBoundsException e) {
		// Expand the arrays we are using
		// We really hope this exception doesn't come from elsewhere.
		cells = new Cell[cells.length*2];
		ptrs = new int[cells.length];
		used = new boolean[cells.length];
	    }
	}
    }
}
