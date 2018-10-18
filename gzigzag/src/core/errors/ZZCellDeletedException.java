/*   
ZZCellDeletedException.java
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

package org.gzigzag;

public class ZZCellDeletedException extends RuntimeException {
public static final String rcsid = "$Id: ZZCellDeletedException.java,v 1.1 2001/07/12 13:32:13 bfallenstein Exp $";
	public ZZCellDeletedException(String s) { super(s); }
}
