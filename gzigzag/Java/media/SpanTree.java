/*   
SpanTree.java
 *    
 *    Copyright (c) 1999, Ted Nelson and Tuomas Lukka
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
import java.util.*;

/** A structure for putting spans in and obtaining the 
 * matching spans. Not actually a tree yet XXX!
 */

public class SpanTree implements SpanSet {
public static final String rcsid = "$Id: SpanTree.java,v 1.4 2000/09/19 10:32:00 ajk Exp $";
    Vector v = new Vector();
    Vector vo = new Vector();
    public void addSpan(Span s, Object uo) {
	v.addElement(s);
	vo.addElement(uo);
    }
    public Object[] overlaps(Span s) {
	Vector res = new Vector();
	int ind = 0;
	for(Enumeration e = v.elements(); e.hasMoreElements();) {
	    Span s0 = (Span)e.nextElement();
	    if(s.overlaps(s0)) {
		res.addElement(vo.elementAt(ind));
	    }
	    ind++;
	}
	Object[] r = new Object[res.size()];
	for(int i=0; i<r.length; i++) 
	    r[i] = res.elementAt(i);
	return r;
    }
}
