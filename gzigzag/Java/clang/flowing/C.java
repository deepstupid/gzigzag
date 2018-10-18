/*   
C.java
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
 * The "C" (Number Crunching) primitive set for Flowing clang.
 */

public class C extends PrimitiveSet {
public static final String rcsid = "$Id: C.java,v 1.2 2000/10/13 13:52:24 bfallenstein Exp $";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }

    public Primitive get(String id) {
	if(id.equals("+"))
	    return new Plus();
	if(id.equals("-"))
	    return new Minus();
	if(id.equals("*"))
	    return new Mul();
	if(id.equals("/"))
	    return new Div();
	if(id.equals("="))
	    return new Compare(0);
	if(id.equals(">"))
	    return new Compare(-2);
	if(id.equals("<"))
	    return new Compare(+2);
	if(id.equals(">="))
	    return new Compare(-1);
	if(id.equals("<="))
	    return new Compare(+1);
	return null;
    }

    static public class Plus extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2) miscount();
	    int res = params.i(0);
	    for(int i=1; i<params.len(); i++) {
		res += params.i(i);
	    }
	    return new Data(new Integer(res));
	}
    }

    static public class Minus extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2) miscount();
	    int res = params.i(0);
	    for(int i=1; i<params.len(); i++) {
		res -= params.i(i);
	    }
	    return new Data(new Integer(res));
	}
    }

    static public class Mul extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2) miscount();
	    int res = params.i(0);
	    for(int i=1; i<params.len(); i++) {
		res *= params.i(i);
	    }
	    return new Data(new Integer(res));
	}
    }

    static public class Div extends Primitive {
	protected Data execute(Data params) {
	    count(params, 2);
	    return new Data(new Integer(params.i(0) / params.i(1)));
	}
    }

    static public class Compare extends Primitive {
	int weight;
	public Compare(int weight) { this.weight = weight; }
	
	protected Data execute(Data params) {
	    count(params, 2);
	    int i0, i1;
	    if(weight > 0) { i0 = params.i(0); i1 = params.i(1); }
	    else { i0 = params.i(1); i1 = params.i(0); }

	    boolean res;
	    if(weight==0) res = i0 == i1;
	    else if(weight*weight == 4) res = i0 < i1;
	    else res = i0 <= i1;
	    return new Data(new Boolean(res));
	}
    }
}
