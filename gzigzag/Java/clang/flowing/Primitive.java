/*   
Primitive.java
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
 * Written by Benjamin Fallenstein
 */
package org.gzigzag.flowing;
import org.gzigzag.*;

/** An Flowing Clang primitive.
 */

public abstract class Primitive {
String rcsid = "$Id: Primitive.java,v 1.3 2000/10/13 13:52:24 bfallenstein Exp $";

    // In subclasses, override one of the following two:
    protected Data execute(Data params) { return null; }
    public Data execute(Data params, ZZSpace space) {
	return execute(params);
    }

    static public void count(Data pars, int n) { 
	if(n != pars.len()) miscount();
    }
    static public void miscount() { throw new ZZError("Wrong parameter count"); }

    /** Read a direction or step count from a parameter array.
     * If the pos is out of the bounds of the array, +1 is assumed, so that
     * the dir can optionally be left out.
     * <p>
     * (This, obviously, is just a convenience function.)
     */
    static int readsteps(Data pars, int pos) {
	if(pos < pars.len()) {
	    String stepstr = pars.s(pos);
	    if(stepstr.equals("+"))
		return 1;
	    else if(stepstr.equals("-"))
		return -1;
	    else
		return pars.i(pos);
	} else {
	    return 1;
	}
    }
}


