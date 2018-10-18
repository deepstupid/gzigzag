/*   
Primitive.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2000-2001, Benjamin Fallenstein
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
 * Written by Benjamin Fallenstein
 */
package org.zaubertrank;
import org.gzigzag.*;

/** A primitive for use in the zaubertrank.
 */

public abstract class Primitive {
String rcsid = "$Id: Primitive.java,v 1.1 2001/04/02 20:29:05 bfallenstein Exp $";

    // In subclasses, override one of the following two:
    protected Object eval(Data params) { return null; }
    public Object eval(Data params, ZZSpace space) {
	return eval(params);
    }

    public abstract int args();

}


