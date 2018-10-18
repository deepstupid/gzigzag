/*   
ZZSlicedDimSpace.java
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
import org.gzigzag.*;
import java.util.*;

/** A space consisting of primitive slices.
 * NOTE: the current code does not support changing slice IDs
 * *AT ALL*.
 */

class EHSEFKUHESMFKMEFKLSEF { }

/*

public class ZZSlicedDimSpace extends ZZDimSpace {
public static final String rcsid = "$Id: ZZSlicedDimSpace.java,v 1.10 2000/11/07 23:07:33 tjl Exp $";
	
    static final String getOrigID(String c) {
	    return c.substring(c.indexOf('-')+1);
    }
    static final int getSliceID(String c) {
	    return Integer.parseInt(c.substring(0,c.indexOf('-')));
    }
    static final String getConvID(int sli, String ores) {
	    return ""+sli+"-"+ores;
    }

    ZZDimSpace[] slices;

    protected DimCell getNewCell(String id) {
	int sl = getSliceID(id);
	String or = getOrigID(id);
	DimCell ocell = slices[sl].getNewCell(or);
	id = getConvID(sl, ocell.getID());
	return (DimCell)getCellByID(id);
    }
    protected DimCell getNewCell() {
	throw new ZZError("Can't make new cells from nothing in sliced space");
    }

    public ZZSlicedDimSpace(ZZDimSpace[] s) {
	slices = s;
    }
    protected String getText(String id) {
	int sl = getSliceID(id);
	String or = getOrigID(id);
	ZZCell c = slices[sl].getCellByID(or);
	return c.getText();
    }

    protected Span getSpan(String id) {
	int sl = getSliceID(id);
	String or = getOrigID(id);
	ZZCell c = slices[sl].getCellByID(or);
	Span s = c.getSpan();
	return s;
    }
    protected void setText(String id, Object cont) {
	int sl = getSliceID(id);
	String or = getOrigID(id);
	ZZCell c = slices[sl].getCellByID(or);
	if(cont instanceof Span) {
	    c.setSpan((Span)cont);
	} else {
	    c.setText((String)cont);
	}
    }

    public ZZCell getHomeCell() {
	return getCellByID(getConvID(0, slices[0].getHomeCellID()));
    }

    public ZZDimension createDimension(String s) {
	if(s.equals("d.preflets")) return null;
	if(s.equals("d.slices")) return new SlicedHomes(this);
	if(s.equals("d.slicesame")) return new SlicedSame(this);
	ZZDimension[] dims = new ZZDimension[slices.length];
	for(int i=0; i<slices.length; i++)
	    dims[i] = slices[i].d(s);
	if(s.equals("d.cursor")) return new SlicedCursor(this, dims, "d.preflets");
	return new Sliced(this, dims);
    }

    // We delegate scroll creation to s.0
    // XXX Should scrolls be created to slices?
    public StringScroll getStringScroll() {
	return slices[0].getStringScroll();
    }

    public StringScroll getStringScroll(String name) {
	return slices[0].getStringScroll(name);
    }
}

*/
