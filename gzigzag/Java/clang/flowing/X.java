/*   
X.java
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
 * The "X" (Cell Text) primitive set for Flowing clang.
 */

public class X extends PrimitiveSet {
public static final String rcsid = "$Id: X.java,v 1.4 2000/10/20 15:16:54 bfallenstein Exp $";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }

    public Primitive get(String id) {
	if(id.equals("SET"))
	    return new Set();
	if(id.equals("+"))
	    return new Concat();
	if(id.equals("="))
	    return new Equals();
	if(id.equals("LEN"))
	    return new Len();
	if(id.equals("PART"))
	    return new Part();
	if(id.equals("FIND"))
	    return new Find();
	if(id.equals("PUICOPY"))
	    return new PUICopy();
	if(id.equals("PUIPASTE"))
	    return new PUIPaste();
	if(id.equals("LOG"))
	    return new Log();
	return null;
    }

    static public class Set extends Primitive {
	protected Data execute(Data params) {
	    count(params, 2);
	    params.c(0).setText(params.s(1));
	    return null;
	}
    }

    static public class Concat extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2) miscount();
	    String s = "";
	    for(int i=0; i<params.len(); i++) {
		s = s + params.s(i);
	    }
	    return new Data(s);
	}
    }

    static public class Equals extends Primitive {
	protected Data execute(Data params) {
	    count(params, 2);
	    boolean res = params.s(0).equals(params.s(1));
	    return new Data(new Boolean(res));
	}
    }

    static public class Len extends Primitive {
	protected Data execute(Data params) {
	    count(params, 1);
	    return new Data(new Integer(params.s(0).length()));
	}
    }

    static public class Part extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2 || params.len() > 3) miscount();
	    String txt = params.s(0); int start = params.i(1);
	    if(params.len() == 2) {
		txt = txt.substring(start);
	    } else {
		txt = txt.substring(start, params.i(2));
	    }
	    return new Data(txt);
	}
    }

    static public class Find extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2 || params.len() > 3) miscount();
	    String txt = params.s(0), sub = params.s(1);
	    int start;
	    if(params.len() == 3) start = params.i(2);
	    else start = 0;
	    return new Data(new Integer(txt.indexOf(sub, start)));
	}
    }

    static public class PUICopy extends Primitive {
	protected Data execute(Data params) {
	    count(params, 1);
	    ZZUtil.puiCopy(params.s(0));
	    return null;
	}
    }

    static public class PUIPaste extends Primitive {
	public Data execute(Data params, ZZSpace space) {
	    count(params, 0);
	    return new Data(ZZUtil.puiGetClipText());
	}
    }

    static public class Log extends Primitive {
	protected Data execute(Data params) {
	    count(params, 1);
	    ZZLogger.log(params.s(0));
	    return null;
	}
    }
}
