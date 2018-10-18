/*   
ZZEvObs.java
 *    
 *    Copyright (c) 1999, Tuomas Lukka
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

package org.gzigzag;
/** A simple class that observes a cell.
 * Note that this class is to be used only for one cell: the two calls
 *   chg(1) chg(2) can be replaced with chg(2). There will in the future be
 * a new class, ZZMultiCellObs that gets a ZZCell[] as its parameter.
 */

public interface ZZEvObs {
String rcsid = "$Id: ZZEvObs.java,v 1.5 2000/09/19 10:31:58 ajk Exp $";
	/** Called when something is changed.
	 */

	void chg();
}


