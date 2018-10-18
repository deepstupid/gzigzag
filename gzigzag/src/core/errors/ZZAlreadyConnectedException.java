/*   
ZZAlreadyConnectedException.java
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

/** A connect operation would break an existing connection and is not
 * allowed.
 * <p>
 * There is a constructor that takes the cells which where attempted to
 * be connected. If it is used, the error message looks like this:
 * <code>ZZAlreadyConnectedException: neg (-prevpos) (prevneg-) pos</code>.
 */

public class ZZAlreadyConnectedException extends RuntimeException {
public static final String rcsid = "$Id: ZZAlreadyConnectedException.java,v 1.5 2001/08/13 01:48:45 tjl Exp $";
    public ZZAlreadyConnectedException(String s) { super(s); }
    public ZZAlreadyConnectedException(String s, 
	    Cell neg, Cell prevpos, Cell prevneg, Cell pos) {
	super(s+" \n"+neg+" (-\n"+prevpos+") (\n"+prevneg+"-) \n"+pos);
    }
}

