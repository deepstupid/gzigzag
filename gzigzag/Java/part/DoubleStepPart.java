/*   
DoubleStepPart.java
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

/** <b>EXPERIMENTAL:</b> A dimension double-stepper part.
 * This part, unlike most, has none of its own cells but simply
 * provides for each dimension a doubling dimension.
 */

public class DoubleStepPart extends ZZROStrSpacePart {
public static final String rcsid = "$Id: DoubleStepPart.java,v 1.1 2000/11/06 12:39:39 tjl Exp $";
    public static final boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }

    public DoubleStepPart(ZZSpace space, String id) {
	super(space, id);
    }

    public String homeID() { 
	return "1";
    }

    public String getText(ZZCellHandle c) {
	return c.id.substring(c.id.length()-1);
    }

    public ZZDimension getDim(String name) {
	return new Dbl(space.d(name));
    }

    public class Dbl extends ZZRODimension {
	ZZDimension dim;
	public Dbl(ZZDimension dim) {
	    this.dim = dim;
	}
	public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
	    ZZCellHandle hd = dim.s(c, steps*2, o);
	    p("Dbl: Step "+steps+" "+c+" "+hd);
	    return hd;
	}
    }
}

