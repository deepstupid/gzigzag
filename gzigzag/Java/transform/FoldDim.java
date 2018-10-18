/*   
FoldDim.java
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
 * Written by Benjamin Fallenstein
 * (Borrowing heavily from ReverseDim.java by Tuukka Hastrup)
 */
package org.gzigzag.transform;
import java.util.*;
import org.gzigzag.*;

/** "Fold" a dimension into two new ones
 * Take one start and two result dimensions. Target dim
 */

public class FoldDim {
String rcsid = "$Id: FoldDim.java,v 1.4 2000/10/18 14:35:32 tjl Exp $";
    public static final boolean dbg = false;
    void p(String s) { if (dbg) System.out.println(s); }
    void pa(String s) { System.out.println(s); }

    String dim, hdim; // Headcell dim
    ZZSpace space;

    public FoldDim(String d, String h, ZZSpace s) {
	dim=d; hdim=h; space=s;
    }

    public void transform() {
	Enumeration e=space.cells();
	ZZCell c;
	Hashtable donecells = new Hashtable(); // Cells on the folded ranks
        while(e.hasMoreElements()) { // Do ranks
            c = (ZZCell)e.nextElement();
            if(c.s(dim, -1)==null) {
		if (c.s(dim, 1)!=null) {
		    if(!donecells.containsKey(c)) {
			ZZCell next = c.s(dim, 1);
			c.disconnect(dim, 1); c.connect(hdim, 1, next);
			donecells.put(c, c);
			for(; next != null; next=next.s(dim, 1))
			    donecells.put(next, next);
		        p("Folded rank beginning from cell #"+c.getID());
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
	while(e.hasMoreElements()) { // Detect loops
	    c = (ZZCell)e.nextElement();
	    if(!donecells.containsKey(c)) { // If it wasn't rank, then it's loop
		pa("FoldDim couldn't convert cell on loop, #"+c.getID()+
		   ", '"+c.getText()+"'");
	    }
	}
    }
}