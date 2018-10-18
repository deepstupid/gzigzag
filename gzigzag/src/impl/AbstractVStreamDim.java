/*
AbstractVStreamDim.java
 *    
 *    Copyright (c) 2002, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */

package org.gzigzag.impl;
import java.util.*;
import org.gzigzag.*;

/** Abstract impl of some VStreamDim methods in terms of ordinary
 *  Dim methods.
 */

public abstract class AbstractVStreamDim extends AbstractDim
    implements VStreamDim {
String rcsid = "$Id: AbstractVStreamDim.java,v 1.5 2002/03/16 18:35:18 bfallenstein Exp $";

    public AbstractVStreamDim(Space s) {
	super(s);
    }

    public void insertAfterCell(Cell at, Cell rank) {
        //if(rank == null) return;

        Cell end = h(rank, 1);
        Cell next = s(at);

        if(next != null)
            disconnect(at, 1);

        connect(at, rank);

        if(next != null)
            connect(end, next);
    }

    public void removeRange(Cell first, Cell last) {
        Cell before = s(first, -1);
        Cell after = s(last);

	disconnect(first, -1);
	disconnect(last, 1);

	if(!h(last).equals(first)) {
	    connect(before, first);
	    connect(last, after);
	    throw new ZZError("removeRange arguments do not form a range: "+
			      first+", "+last);
	}

        if(before != null && after != null)
            connect(before, after);
    }

    //void iterate(org.gzigzag.vob.CharRangeIter i, Cell stream,
    //Map extra);

    //public void notifyTransclusion(Cell start, int length, boolean update);
    
    public void dumpVStreamInfo(Cell c) {
	System.err.println("No vstream info to dump.");
    }
}
