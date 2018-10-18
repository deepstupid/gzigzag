/*   
ClasmTestPrimitives.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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

package org.gzigzag.impl.clasm;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import junit.framework.*;

/** Test case for the clasm Primitive class
 */

public class ClasmTestPrimitives {
public static final String rcsid = "$Id: ClasmTestPrimitives.java,v 1.4 2001/07/08 17:23:15 bfallenstein Exp $";

    Primitive add = new FunctionalPrimitive() {
	    public int exec(int a, int b) { return a + b; }
	};
    Primitive substr = new FunctionalPrimitive() {
	    public String exec(String s, int start, int end) {
		return s.substring(start, end);
	    }
	};
    Primitive block = new ImperativePrimitive() {
	    public Object call(Object[] params) throws ClasmException {
		Object res = null;
		for(int i=0; i<params.length; i++)
		    res = Callable.value(params[i]);
		return res;
	    }
	};

}




