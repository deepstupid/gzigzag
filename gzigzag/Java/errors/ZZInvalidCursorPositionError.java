/*   
ZZInvalidCursorPositionError.java
 *    
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
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

/** Tried to move a cursor to a place that can't have cursors on it.
 * @deprecated Cursors can be anywhere, now.
 */

public class ZZInvalidCursorPositionError extends ZZError {
public static final String rcsid = "$Id: ZZInvalidCursorPositionError.java,v 1.4 2000/10/26 18:09:29 tjl Exp $";
	public ZZInvalidCursorPositionError(String s) { super(s); }
}

