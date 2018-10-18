/*   
SpanSpacepart.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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

/** A (somewhat strange) spacepart containing span cells.
 * @see Spacepart
 */

public class SpanSpacepart extends EmptySpacepart {
public static final String rcsid = "$Id: SpanSpacepart.java,v 1.6 2002/03/16 19:45:43 bfallenstein Exp $";

    public static final InclusionType VSTREAM_PART = new InclusionType();

    public SpanSpacepart(SimpleSpanSpace space) {
	super(space, null, "", VSTREAM_PART);
    }

    public boolean exists(String id) {
	// XXX
	int x = id.lastIndexOf("$");
	if(x < 0) return false;
	int y = id.lastIndexOf(";");
	String block = id.substring(y + 1, x);
	return ((SimpleSpanSpace)getSpace()).scrolls.containsKey(block);
    }
    public boolean exists(Object obj, int idx) {
	return true;
    }

    // for any string s, only one Ref may exist!
    public static final class Ref {
	public final String ref;
	public final Cell tid;
	public final TextScrollBlock block;
	
	private Ref(String ref, Cell tid, TextScrollBlock block) {
	    this.ref = ref;
	    this.tid = tid;
	    this.block = block;
	}
    }

    private Map refs = new HashMap();
    public Ref getRef(String s) {
	Ref r = (Ref)refs.get(s);
	if(r == null) {
	    int x = s.indexOf(';');
	    if(x == -1) System.err.println(s);
	    String tid = s.substring(0, x);
	    String block = s.substring(x+1, s.length()-1);
		
	    r = new Ref(s, space.getCell(tid), 
			((SimpleSpanSpace)space).getTextScroll(block));
	    refs.put(s, r);
	}
	return r;
    }

    public Cell getCell(String id) {
	int x = id.lastIndexOf("$") + 1;
	return getCell(getRef(id.substring(0, x)),
		       Integer.parseInt(id.substring(x)));
    }
    public Cell getCell(Object obj, int idx) {
	Ref ref = (Ref)obj;
	return new Cell(space, ref.ref + idx, this, ref, idx);
    }
}
