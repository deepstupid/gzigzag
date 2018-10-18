/*   
BinaryTreePart.java
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

/** <b>EXPERIMENTAL:</b> A full binary tree spacepart demo.
 * Supports two local dimensions: depth and breadth.
 */

public class BinaryTreePart extends ZZROStrSpacePart {
public static final String rcsid = "$Id: BinaryTreePart.java,v 1.3 2000/11/06 12:39:39 tjl Exp $";

    public BinaryTreePart(ZZSpace space, String id) {
	super(space, id);
    }

    public String homeID() { 
	return "1";
    }

    public String getText(ZZCellHandle c) {
	return c.id.substring(c.id.length()-1);
    }

    public ZZDimension getDim(String name) {
	if(name.equals("depth")) return new Depth();
	if(name.equals("breadth")) return new Breadth();
	return null;
    }

    public class Depth extends ZZRODimension {
	public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
	    // We can go one step
	    if(c.part == null && c.id.equals("1") && steps > 0) {
		c = this.space.getCellByID(BinaryTreePart.this, "1", null);
		steps --;
	    }

	    if(c.part != BinaryTreePart.this) return null;

	    while(steps > 0 && c != null) {
		c = (ZZCellHandle)this.space.getCellByID(c.id+"0");
		steps--;
	    } 
	    while(steps < 0 && c != null) {
		if(c.part != BinaryTreePart.this) return null;
		int len = c.id.length();
		if(c.id.charAt(len-1) == '0') 
		    c = (ZZCellHandle)this.space.getCellByID(c.id.substring(0, len-1));
		else if(c.id.equals(id+":"+1))
		    c = (ZZCellHandle)this.space.getCellByID("1");
		else
		    c = null;
		steps++;
	    }
	    return c;
	}
    }

    public class Breadth extends ZZRODimension {
	public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
	    if(c.part != BinaryTreePart.this) return null;

	    if(steps == 0) return c;
	    if(steps == 1) {
		int len = c.id.length();
		if(c.id.charAt(len-1) == '0') 
		    return (ZZCellHandle)this.space.getCellByID(c.id.substring(0, len-1)+"1");
	    } else if(steps == -1) {
		int len = c.id.length();
		if(c.id.charAt(len-1) == '1' &&
		   c.id.charAt(len-2) != ':') 
		    return (ZZCellHandle)this.space.getCellByID(c.id.substring(0, len-1)+"0");
	    }
	    return null;
	}
    }

}

