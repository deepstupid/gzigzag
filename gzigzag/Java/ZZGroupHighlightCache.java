/*   
ZZGroupHighlightCache.java
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
 * Written Kimmo Wideroos
 */

/* Purpose: make it easy to have a group of cells temporarily
 * highlighted. Primitive op MOUSE_MOVED 
 */

package org.gzigzag;
import java.util.*;

public class ZZGroupHighlightCache {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }
    static final void pa(String s) { ZZLogger.log(s); }

    static private Hashtable ag = new Hashtable();
    static private String timestamp = new String();

    // add new highlighted cell (temporal focus, you name it) 
    static public final boolean add(ZZCell c, String ts) {
        if(!timestamp.equals(ts)) return false;
	if(c != null) ag.put(c, c);
	return true;
    }

    // add new highlighted cell (temporal focus, you name it) 
    static public final void add(ZZCell c) {
	if(c != null) ag.put(c, c);
	return;
    }

    // remember to clear before starting to add a new group!
    static public final String reset() {
	ag.clear();
        timestamp = new Long(System.currentTimeMillis()).toString();
        return timestamp;
    }

    // (currently 'addSolidColours' in CellFlobFactory1 use this)
    static public final boolean isMember(ZZCell c) {
	return ag.containsKey(c); 
    }

    // get whole group
    static public final Enumeration get() {
	return ag.elements(); 
    }
}



