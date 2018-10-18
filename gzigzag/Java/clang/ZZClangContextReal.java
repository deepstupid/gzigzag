/*   
ZZClangContextReal.java
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

class EUNRNBRRV {
}

/** A real viewspecs -context object for Clang.
 */

/*
public class ZZClangContextReal implements ZZClangContext {
String rcsid = "$Id: ZZClangContextReal.java,v 1.5 2000/10/18 14:35:31 tjl Exp $";

    ZZCell viewCell;
    ZZClangContextReal(ZZCell vc) { viewCell = vc; }

    public ZZCursor getCursor() { return new ZZCursorReal(viewCell); }
    public String getDimString(int n) { return getDim(n).get().getText(); }

    public ZZCursor getDim(int n) {
	ZZCell d = viewCell;
	for(int i=0; i<n && d!=null; d=d.s("d.dims", 1)) {}
	if(d==null) return null;
	return new ZZCursorReal(d);
    }

    public ZZCell getViewcell() { return viewCell; }

    public ZZClangContext getSubcontext(ZZCell startCursor) {
	return new ZZClangContextVirtual(this, startCursor);
    }
}

*/
