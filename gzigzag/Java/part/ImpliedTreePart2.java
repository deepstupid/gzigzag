/*   
ImpliedTreePart2.java
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

/** <b>EXPERIMENTAL:</b> A tree implied by a sequence.
 * <b>Second version, to test the parsed space part stuff.</b>
 * A proof of concept of having a straight
 * stream of material with "tags" connected next to it imply
 * a tree.
 * <p>
 * Connect a bunch of stuff on d.2 and add cells with numbers connected
 * negwards on d.1, then view on imptree:depth and imptree:breadth
 * <p>
 * The number is the level of the heading, reverse from HTML 
 * (1 is next to text, 2 is next above etc).
 * For a heading of level N, N-1 cells are generated.
 * The IDs of the generated cells are of the form
 * k-ID where k is the level from the text up.
 * So a heading of level 4 (cell ID 26) would have the following
 * cells connected along depth: 26, i:3-26, i:2-26 and i:1-26.
 * <p>
 * One problem of space parts: changing the section depth deletes cells,
 * and cursors on those cells should be notified!
 */

public class ImpliedTreePart2 extends ZZSpacePart {
public static final String rcsid = "$Id: ImpliedTreePart2.java,v 1.1 2000/11/13 11:59:48 tjl Exp $";

    static public class Parsed {
	String orig;
	int depth;
    }

    public ImpliedTreePart2(ZZSpace space, String id) {
	super(space, id);
    }

    public String getText(ZZCellHandle c) {
	return c.id.substring(c.id.length()-1);
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
    
    Depth dd = new Depth();
    Breadth db = new Breadth();

    public ZZDimension getDim(String name) {
	if(name.equals("depth")) return dd;
	if(name.equals("breadth")) return db;
	return null;
    }

    public class Depth extends ZZRODimension {
	public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
	    if(steps == 0) return c;

	    int level;

	    if(c.part == ImpliedTreePart2.this) {
		Parsed par = (Parsed)c.parsedID;
		level = par.depth;
		c = (ZZCellHandle)this.space.getCellByID(par.orig);
	    } else {
		// It's either beginning or end of rank.
		if(c.s("d.1", 1) != null) {
		    level = posno(c);
		} else {
		    if(steps > 0) return null;
		    c = (ZZCellHandle)c.s("d.1", -1);
		    if(c == null) return null;
		    level = 0;
		}
	    }
	    int maxlevel = posno(c);
	    if(maxlevel < 0) return null;
	    level -= steps;
	    if(level < 0) return null;
	    if(level == 0) return (ZZCellHandle)c.s("d.1", 1);
	    if(level > maxlevel) return null;
	    if(level == maxlevel) return c;
	    return ImpliedTreePart2.this.space.getCellByID(
			ImpliedTreePart2.this, 
			level + "-" + c.id);
	    
	}
    }

    int posno(ZZCell c) {
	try {
	    return Integer.parseInt(c.getText().trim());
	} catch(NumberFormatException e) {
	    ZZLogger.log("Not a number! '"+c.getText()+"' "+c+" "+e);
	    return -1;
	}
    }

    public class Breadth extends ZZDimension {
	public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
	    if(steps == 0) return c;
	    ZZCell searchFrom;
	    int level;
	    if(c.part == ImpliedTreePart2.this) {
		Parsed par = (Parsed)c.parsedID;
		level = par.depth;
		c = (ZZCellHandle)this.space.getCellByID(par.orig);
	    } else {
		if(c.s("d.1", 1) == null)
		    return (ZZCellHandle)c.s("d.2", steps);
		level = posno(c); 
		if(level < 0) return null;
	    }
	    searchFrom = c.s("d.1");
	    if(searchFrom == null) return null;

	    int dir = (steps > 0 ? 1 : -1);
	    int lastlevel = -1;
	    ZZCell lasthdr = null;
	    while(steps != 0) {
		searchFrom = searchFrom.s("d.2", dir);
		if(searchFrom == null) return null;
		lasthdr = searchFrom.s("d.1", -1);
		if(lasthdr == null) continue;
		if((lastlevel = posno(lasthdr)) < level) continue;
		steps -= dir;
	    }

	    // Return our cell: the cell we ended up at is at higher level.
	    if(lastlevel > level) {
		return ImpliedTreePart2.this.space.getCellByID(
			    ImpliedTreePart2.this, 
			    level + "-" + lasthdr.getID());
	    } else {
		return (ZZCellHandle)lasthdr;
	    }
	}
	public void disconnect(ZZCellHandle c, int dir) {
	    if(dir > 0) {
		ZZCellHandle c2 = s(c, 1);
		if(c2 != null)
		    disconnect(c2, -1);
		return;
	    }
	    ZZCell ct = dd.h(c, 1);
	    ct.disconnect("d.2", -1);
	}
	public void connect(ZZCellHandle c, ZZCellHandle d) {
	    if(s(c, 1) != null) 
		throw new ZZConnectWouldBreakError("In impliedtreedepth");
	    if(s(d, -1) != null) 
		throw new ZZConnectWouldBreakError("In impliedtreedepth2");
	    ZZCell c1 = dd.h(c, 1).h("d.2", 1);
	    ZZCell d1 = dd.h(d, 1).h("d.2", -1);
	    c1.connect("d.2", d1);
	}
    }

}

