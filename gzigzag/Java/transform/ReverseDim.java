/*   
ReverseDim.java
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
 * Written by Tuukka Hastrup
 */
package org.gzigzag.transform;
import java.util.*;
import org.gzigzag.*;

/** ZZspace dimension changer - reverse positive and negative directions.
 */

public class ReverseDim {
String rcsid = "$Id: ReverseDim.java,v 1.6 2000/10/18 14:35:32 tjl Exp $";
    public static final boolean dbg = false;
    void p(String s) { if (dbg) System.out.println(s); }
    String dim;
    ZZSpace space;
/** Constructs direction changer for a dimension in a space
 *  @param d            The dimension
 *  @param s            The space to convert
 */
    public ReverseDim(String d, ZZSpace s) {
	dim = d;
	space = s;
    }

/** Performs the actual transformation. All connections along the dimension 
 *  are disconnected and new ones connected in reverse direction.
 */
    public void transform() {
        Enumeration e=space.cells();
        ZZCell c;
	Hashtable donecells = new Hashtable(); // Cells on the reversed ranks
        while(e.hasMoreElements()) { // Do ranks
            c = (ZZCell)e.nextElement();
            if(c.s(dim, -1)==null) {
		if (c.s(dim, 1)!=null) {
		    if(!donecells.containsKey(c)) {
		        swapRank(c, donecells);
		        p("Swapped rank beginning from cell #"+c.getID());
		    }
		    else
			p("Rank beginning from cell #"+c.getID()+" already done");
		}
		else
		    p("Cell #"+c.getID()+" not placed on "+dim);
		donecells.put(c, c); // This cell is not in loop
	    } // Negwards connected cells will be done later
        }
	e = space.cells();
	while(e.hasMoreElements()) { // Do loops
	    c = (ZZCell)e.nextElement();
	    if(!donecells.containsKey(c)) { // If it wasn't rank, then it's loop
		c.disconnect(dim, -1); // Cut into a rank
		c.connect(dim, 1, swapRank(c, donecells)); // Flip and join
	    }
	}
    }

    // Swap the cells in a rank. Return the new headcell.
    protected ZZCell swapRank(ZZCell head, Hashtable done)
    {
	ZZCell newrank = head; // The latest cell in the new rank
	ZZCell curr = head.s(dim, 1); // The head of the old one
	head.disconnect(dim, 1);
	ZZCell next;
	while (curr != null) {
	    next = curr.s(dim, 1);
	    if(next!=null) {
		curr.disconnect(dim, 1);
	    }
	    curr.connect(dim, 1, newrank);
	    done.put(curr, curr);
	    newrank = curr;
	    curr = next;
	};
	return newrank;
    }
}

