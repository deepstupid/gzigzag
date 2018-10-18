/*   
GZZ1Ugliness.java
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
import org.gzigzag.*;
import org.gzigzag.mediaserver.Mediaserver;
import java.util.*;
import java.io.*;

/** A class for merge that makes sure a cell with a given Id exists in another
 *  space. If the cell does not exists, a new one <em>with the same id</em>
 *  is created; in the case of a span transclusion cell, that whole span
 *  is transcluded.
 */

class GZZ1Ugliness {
String rcsid = "$Id: GZZ1Ugliness.java,v 1.10 2002/03/15 18:44:16 bfallenstein Exp $";

    PermanentSpace readFrom, writeTo;

    GZZ1Ugliness(PermanentSpace readFrom, PermanentSpace writeTo) {
	this.readFrom = readFrom;
	this.writeTo = writeTo;
    }

    /** Make sure that a cell with id <code>s</code> exists in
     *  <code>writeTo</code>.
     */
    void makeExist(String s) {
	if(s.indexOf("$") < 0) {
	    if(writeTo.exists(s)) return;
	    writeTo.gzz1_NewCell(s);
	    writeTo.newCells.add(s);
	} else {
	    try {
	    String tid = s.substring(0, s.lastIndexOf(";"));
	    if(writeTo.getTranscludedSpans(tid) != null) return;

            for(Iterator i = readFrom.getTranscludedSpans(tid).iterator();
		i.hasNext(); ) {
		Span1D sp = (Span1D)i.next();
		if(sp == null)
		    throw new ZZError("ARGH. No span at: "+s);
		String block = s.substring(s.lastIndexOf(";") + 1,
					   s.lastIndexOf("$"));
		Mediaserver.Id blockId = new Mediaserver.Id(block);
		makeExist(tid);
		writeTo.gzz1_transcludeSpan(writeTo.getCell(tid), blockId,
					    sp.offset(), sp.offset()+sp.length()-1);
		writeTo.transcludedSpans.put(tid, sp);
		
		// XXX ???
		//VStreamDim oldvs = (VStreamDim)writeTo.prevDims.get(Id.stripHome(Dims.d_vstream_id.id));
		//int first = sp.offset(), last = first + sp.length() - 1;
		//if(sp.length() > 1)
		//    oldvs.connectRange(tid, sp.getScrollBlock(), first, last);
	    }
	    } catch(ScrollBlockManager.CannotLoadScrollBlockException e) {
		throw new ZZError(e.getMessage());
	    }
	}
    }
}
