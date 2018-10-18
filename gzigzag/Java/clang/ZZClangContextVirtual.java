/*   
ZZClangContextVirtual.java
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
package org.gzigzag.clang;
import org.gzigzag.*;

class EKMVLSVMEVSE {
}

/** A virtual viewspecs -context object for Clang. The main cursor
 * is virtualized, the others are still global - XXX IS THIS RIGHT?
 */

/*

public class ZZClangContextVirtual implements ZZClangContext {
String rcsid = "$Id: ZZClangContextVirtual.java,v 1.4 2000/09/19 10:31:58 ajk Exp $";

    ZZCursor cursor;
    ZZClangContext parent;
    ZZClangContextVirtual(ZZClangContext orig, ZZCell start) { 
	cursor = new ZZCursorVirtual(start);
	parent = orig;
    }


    public ZZCursor getCursor() { return cursor; }
    public String getDimString(int n) { return getDim(n).get().getText(); }

    public ZZCursor getDim(int n) { 
	return parent.getDim(n);
    }

    public ZZCell getViewcell() { return null; }

    public ZZClangContext getSubcontext(ZZCell startCursor) {
	return new ZZClangContextVirtual(this, startCursor);
    }
}

*/
