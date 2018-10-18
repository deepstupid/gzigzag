/*   
SimpleSpanSet.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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

/** A simple, very slow span set.  XXX Rename to Trivial
 */

public class SimpleSpanSet implements SpanSet {
String rcsid = "$Id: SimpleSpanSet.java,v 1.5 2001/10/01 16:45:04 tjl Exp $";

    ArrayList v1 = new ArrayList();
    ArrayList v2 = new ArrayList();

    /** Add a span.
     */
    public void addSpan(Span s, Object obj) {
	v1.add(s);
	v2.add(obj);
    }

    /** Get the objects corresponding to the spans entered that overlap
     * the given span. XXX Should be enumeration?
     */
    public Collection overlaps(Span s) {
	ArrayList res = new ArrayList();
	for(int i=0; i<v1.size(); i++) {
	    if(((Span)v1.get(i)).intersects(s))
		res.add(v2.get(i));
	}
	return res;
    }

    public Collection spans() {
	return Collections.unmodifiableCollection(v1);
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("SimpleSpanSet\n");
	for(int i=0; i<v1.size(); i++) {
	    buf.append("  "+v1.get(i)+" => "+v2.get(i)+"\n");
	}
	return buf.toString();
    }
}



