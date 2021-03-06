/*   
ImmutableException.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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

/** An operation tried to modify an object that is not changeable.
 * (Possibly any more: object such as TextScrollBlock become
 * immutable at some point).
 */

public class ImmutableException extends ZZException {
public static final String rcsid = "$Id: ImmutableException.java,v 1.1 2001/04/20 07:58:26 tjl Exp $";
	public ImmutableException(String s) { super(s); }
}

