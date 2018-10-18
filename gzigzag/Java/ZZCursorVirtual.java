/*   
ZZCursorVirtual.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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

/** A virtual cursor.
 * Used by clang to optimize away some puts and gets, e.g. to postpone
 * setting the view cursor in the structure until the end of the routine.
 * XXX Clang definition needs to be VERY specific about what timings
 * are guaranteed in storing things to the structure.
 */
public class ZZCursorVirtual extends ZZCursor {
String rcsid = "$Id: ZZCursorVirtual.java,v 1.7 2000/11/25 00:36:33 tjl Exp $";
    ZZCell val;
    int offs;

    public ZZCursorVirtual(ZZCell startVal) {
	val = startVal;
	offs = NO_OFFSET;
    }
    public ZZCursorVirtual(ZZCell startVal, int startOffset) {
	val = startVal;
	offs = startOffset;
    }
    public ZZCursorVirtual(ZZCursor curs) {
	val = curs.get();
	offs = curs.getOffs();
    }

    static public ZZCursorVirtual createFromReal(ZZCell curs) {
	return new ZZCursorVirtual(ZZCursorReal.get(curs),
				    ZZCursorReal.getOffs(curs));
    }

    public ZZCell get() { return val; }
    public void set(ZZCell c) { val = c; }
    public int getOffs() { return offs; }
    public void setOffs(int i) { offs = i; }
}


