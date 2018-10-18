/*   
T.java
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
 * The "T" (Space Transformation) primitive set for Flowing clang.
 */

public class T extends PrimitiveSet {
public static final String rcsid = "$Id: T.java,v 1.4 2000/10/18 14:35:31 tjl Exp $";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }

    public Primitive get(String id) {
	if(id.equals("GET"))
	    return new Get();
	if(id.equals("SET"))
	    return new Set();
	if(id.equals("STEP") || id.equals("s"))
	    return new Step();
	if(id.equals("END") || id.equals("h"))
	    return new End();
	if(id.equals("FIND"))
	    return new Find();
	if(id.equals("ORIGINAL"))
	    return new Original();
	if(id.equals("CROSS"))  // == intersect
	    return new Cross();
	if(id.equals("HOMECELL"))
	    return new HomeCell();
	if(id.equals("CONNECT"))
	    return new Connect();
	if(id.equals("INSERT"))
	    return new Insert();
	if(id.equals("DISCONNECT"))
	    return new Disconnect();
	if(id.equals("HOP"))
	    return new Hop();
	if(id.equals("NEW") || id.equals("N"))
	    return new New();
	if(id.equals("CLONE"))
	    return new Clone();
	if(id.equals("DELETE"))
	    return new Delete();
	if(id.equals("SAME"))
	    return new Same();
	return null;
    }

    static public class Get extends Primitive {
	protected Data execute(Data params) {
	    count(params, 1);
	    return new Data(ZZCursorReal.get(params.c(0)));
	}
    }

    static public class Set extends Primitive {
	protected Data execute(Data params) {
	    count(params, 2);
	    ZZCursorReal.set(params.c(0), params.c(1));
	    return null;
	}
    }

    static public class Step extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2 || params.len() > 3) miscount();
	    int steps = readsteps(params, 2);
	    String dim = params.s(1);
	    return new Data(params.c(0).s(dim, steps));
	}
    }

    static public class End extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2 || params.len() > 3) miscount();
	    String dim = params.s(1);
	    return new Data(params.c(0).h(dim, readsteps(params, 2)));
	}
    }

    static public class Find extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 3 || params.len() > 4) miscount();
	    String dim = params.s(1), txt; int dir;
	    if(params.len() == 3) {
		dir = 1;
		txt = params.s(2);
	    } else {
		dir = readsteps(params, 2);
		txt = params.s(3);
	    }
	    ZZCell c = params.c(0).findText(dim, dir, txt);
	    return new Data(c);
	}
    }

    static public class Original extends Primitive {
	protected Data execute(Data params) {
	    count(params, 1);
	    return new Data(params.c(0).getRootclone());
	}
    }

    static public class Cross extends Primitive {        // i.e., intersect
	protected Data execute(Data params) {
	    if(params.len() != 4 && params.len() != 6) miscount();
	    ZZCell c0, c1;
	    String dim0, dim1;
	    int dir0, dir1;

	    if(params.len() == 4) {
		c0 = params.c(0); c1 = params.c(2);
		dim0 = params.s(1); dim1 = params.s(3);
		dir0 = 1; dir1 = 1;
	    } else {
		c0 = params.c(0); c1 = params.c(3);
		dim0 = params.s(1); dim1 = params.s(4);
		dir0 = readsteps(params, 2); dir1 = readsteps(params, 5);
	    }
	    p("CROSS: "+c0.getText()+" "+dim0+" "+dir0+" "+
			c1.getText()+" "+dim1+" "+dir1);
	    return new Data(c0.intersect(dim0, dir0, c1, dim1, dir1));
	}
    }

    static public class HomeCell extends Primitive {
	public Data execute(Data params, ZZSpace space) {
	    count(params, 0);
	    return new Data(space.getHomeCell());
	}
    }

    static public class Connect extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 3 || params.len() > 4) miscount();
	    if(params.len() == 3)
	        params.c(0).connect(params.s(1), params.c(2));
	    else
		params.c(0).connect(params.s(1), readsteps(params, 2),
				    params.c(3));
	    return null;
	}
    }

    static public class Insert extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 3 || params.len() > 4) miscount();
	    if(params.len() == 3)
	        params.c(0).insert(params.s(1), 1, params.c(2));
	    else
		params.c(0).insert(params.s(1), readsteps(params, 2),
				   params.c(3));
	    return null;
	}
    }

    static public class Disconnect extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2 || params.len() > 3) miscount();
	    params.c(0).disconnect(params.s(1), readsteps(params, 2));
	    return null;
	}
    }

    static public class Hop extends Primitive {
	protected Data execute(Data params) {
	    if(params.len() < 2 || params.len() > 3) miscount();
	    params.c(0).hop(params.s(1), readsteps(params, 2));
	    return null;
	}
    }

    static public class New extends Primitive {
	public Data execute(Data params, ZZSpace space) {
	    if(params.len() == 1 || params.len() > 3) miscount();
	    if(params.len() == 0)
		return new Data(space.getHomeCell().N());
	    else {
		String dim = params.s(1);
		return new Data(params.c(0).N(dim,readsteps(params, 2)));
	    }
	}
    }

    static public class Clone extends Primitive {
	protected Data execute(Data params) {
	    count(params, 1);
	    return new Data(params.c(0).zzclone());
	}
    }

    static public class Delete extends Primitive {
	protected Data execute(Data params) {
	    count(params, 1);
	    params.c(0).delete();
	    return null;
	}
    }

    static public class Same extends Primitive {
	protected Data execute(Data params) {
	    count(params, 1);
	    return params;
	}
    }
}
