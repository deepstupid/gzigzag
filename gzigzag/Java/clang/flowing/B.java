/*   
B.java
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
import java.util.*;

/*
 * The "B" (Binary Logic) primitive set for Flowing clang.
 */

public class B extends PrimitiveSet {
public static final String rcsid = "$Id: B.java,v 1.2 2000/10/13 13:52:24 bfallenstein Exp $";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }

    public Primitive get(String id) {
	if(id.equals("AND") || id.equals("&"))
	    return new And();
	if(id.equals("OR") || id.equals("|"))
	    return new Or();
	if(id.equals("NOT") || id.equals("!"))
	    return new Not();
	if(id.equals("XOR"))
	    return new Xor();
	if(id.equals("NOTNULL"))
	    return new NotNull();
	return null;
    }

    static public class And extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2) miscount();
	    boolean res = params.b(0);
	    for(int i=1; i<params.len(); i++) {
		res = res && params.b(i);
	    }
	    return new Data(new Boolean(res));
	}
    }

    static public class Or extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2) miscount();
	    boolean res = params.b(0);
	    for(int i=1; i<params.len(); i++) {
		res = res || params.b(i);
	    }
	    return new Data(new Boolean(res));
	}
    }

    static public class Not extends Primitive {
	protected Data execute(Data params) {
	    count(params, 1);
	    boolean res = !params.b(0);
	    return new Data(new Boolean(res));
	}
    }

    static public class Xor extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2) miscount();
	    boolean res = params.b(0);
	    for(int i=1; i<params.len(); i++) {
		boolean b = params.b(i);
		res = (!res && b) || (res && !b);
	    }
	    return new Data(new Boolean(res));
	}
    }

    static public class NotNull extends Primitive {
	protected Data execute(Data params) {
	    count(params, 1);
	    boolean res = params.o(0) != null;
	    return new Data(new Boolean(res));
	}
    }
}
