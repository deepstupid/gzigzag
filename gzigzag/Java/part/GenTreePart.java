/*   
GenTreePart.java
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

/** <b>EXPERIMENTAL:</b> A tree implied by a sequence, done in a generic way.
 * This is a tree implied by a structure, but in a generic way: an interface,
 * GTreePartModel, is used to provide the tree structure.
 * <p>
 * I <b>still</b> don't know whether Parsed helps...
 * @see GenTreePart
 */

public class GenTreePart extends ZZSpacePart {
public static final String rcsid = "$Id: GenTreePart.java,v 1.2 2000/11/16 21:01:56 tjl Exp $";

    static public class Parsed {
	String orig;
	int depth;
    }

    GenTreePartModel model;

    Depth dd = new Depth();
    Breadth db = new Breadth();

    public GenTreePart(ZZSpace space, String id, GenTreePartModel model) {
	super(space, id);
	this.model = model;
    }

    public ZZDimension getDim(String name) {
	if(name.equals("depth")) return dd;
	if(name.equals("breadth")) return db;
	return null;
    }

    public class Depth extends ZZRODimension {
	public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
	    if(steps == 0) return c;

	    int level;
	    int maxlevel ;

	    if(c.part == GenTreePart.this) {
		Parsed par = (Parsed)c.parsedID;
		level = par.depth;
		c = (ZZCellHandle)this.space.getCellByID(par.orig);
		maxlevel = model.depth(c); 
	    } else {
		// It's either beginning or also end of rank.
		level = model.depth(c);
		if(level == -1) return null;
		maxlevel = level;
	    }
	    if(maxlevel < 0) return null;
	    level -= steps;
	    if(level < 0) return null;
	    if(level > maxlevel) return null;
	    if(level == maxlevel) return c;
	    return GenTreePart.this.space.getCellByID(
			GenTreePart.this, 
			level + "-" + c.id);
	    
	}
    }

    public class Breadth extends ZZDimension {
	public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
	    if(steps == 0) return c;
	    ZZCell searchFrom;
	    int level;
	    if(c.part == GenTreePart.this) {
		Parsed par = (Parsed)c.parsedID;
		level = par.depth;
		c = (ZZCellHandle)this.space.getCellByID(par.orig);
	    } else {
		level = model.depth(c); 
		if(level < 0) return null;
	    }

	    ZZCell n = model.getNext(level, c, steps);
	    if(n == null) return null;
	    int lastlevel = model.depth(n);

	    // Return our cell: the cell we ended up at may be at higher level.
	    if(lastlevel > level) {
		return GenTreePart.this.space.getCellByID(
			    GenTreePart.this, 
			    level + "-" + n.getID());
	    } else {
		return (ZZCellHandle)n;
	    }
	}

	public void disconnect(ZZCellHandle c, int dir) {
	    // Only do it negwards
	    if(dir > 0) {
		ZZCellHandle c2 = s(c, 1);
		if(c2 != null)
		    disconnect(c2, -1);
		return;
	    }
	    // Get headcell
	    ZZCell ct = dd.h(c, -1);
	    model.disconnectNeg(ct);
	}
	public void connect(ZZCellHandle c, ZZCellHandle d) {
	    if(s(c, 1) != null) 
		throw new ZZConnectWouldBreakError("In gentreedepth");
	    if(s(d, -1) != null) 
		throw new ZZConnectWouldBreakError("In gentreedepth2");
	    ZZCell c1 = dd.h(c, -1);
	    ZZCell d1 = dd.h(d, -1);
	    model.connect(c1, d1);
	}
    }


    public String getText(ZZCellHandle c) {
	return "+";
    }
    public void setContent(ZZCellHandle c, Object o) {
    }

    public Object parseIDPart(String idPart) {
	int min = idPart.indexOf("-");
	Parsed ret = new Parsed();
	ret.orig = idPart.substring(min+1);
	ret.depth = Integer.parseInt(idPart.substring(0, min));
	return ret;
    }

    public String generateIDPart(Object parsed) {
	Parsed p = (Parsed)parsed;
	return p.depth+"-"+p.orig;
    }
    
}

