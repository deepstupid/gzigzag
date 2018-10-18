/*   
ZZError.java
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
/*
 *	Things can happen to browsers in magical libraries that make
 *	having your face pulled off by tentacled monstrosities from 
 * 	the Dungeon Dimensions seem a mere light massage by comparison.
 *		- Terry Pratchett, Mort, p. 153
 */

package org.gzigzag;

/** A relatively recoverable ZigZag error.
 * Later on, this should be subclassed and specialized as necessary.
 * Note that catching this <em>is</em> ok - it's just that normally
 * when it happens, we want to just let it unwind the stack without
 * having to declare "throws ZZException" everywhere.
 */

public class ZZError extends Error {
public static final String rcsid = "$Id: ZZError.java,v 1.4 2000/09/19 10:31:59 ajk Exp $";
	public ZZError(String s) { super(s); }
}
