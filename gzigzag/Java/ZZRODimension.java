/*   
ZZRODimension.java
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

/** A read-only dimension.
 * This class simply extends ZZDimension by implementing the
 * abstract methods that would modify the dimension by code that
 * simply throws an exception.
 * The only method that subclasses will need to override is s.
 */

public abstract class ZZRODimension extends ZZDimension {
public static final String rcsid = "$Id: ZZRODimension.java,v 1.10 2000/11/05 19:11:13 tjl Exp $";
	public abstract ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o); 
	public void connect(ZZCellHandle c, ZZCellHandle d) {
		throw new ZZError("Read-only dimension"); 
	}
	public void disconnect(ZZCellHandle c, int dir) {
		throw new ZZError("Read-only dimension"); 
	}
	public void excise(ZZCellHandle c) {
	    // Do nothing.
	}
}
