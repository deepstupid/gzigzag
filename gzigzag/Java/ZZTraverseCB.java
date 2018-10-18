/*   
ZZSpace.java
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
/** An interface for creating a ZZ space through traversal.
 * Cells are represented by their IDs.
 */

public interface ZZTraverseCB {
String rcsid = "$Id: ZZTraverseCB.java,v 1.6 2000/09/19 10:31:58 ajk Exp $";

	/** Start the traversal from the given home cell ID.
	 */
	void start(String home);
	/** Insert the new cell.
	 */
	void cell(String cur, String dim, int dir, String which,
		String content);
	/** Connect the two given cells. */
	void connect(String c1,String dim, int dir, String c2);
	/** That's all, folks. */
	void finish();
}
