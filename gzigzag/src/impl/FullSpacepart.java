/*   
FullSpacepart.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
 *    Portions copyright (c) 2001, Rauli Ruohonen
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
package org.gzigzag.impl;
import org.gzigzag.*;
import java.util.*;

/** A non-editable spacepart containing a cell for any non-empty String id.
 *  Inclusion id is the ID inside the Spacepart, as an interned String.
 */

public class FullSpacepart extends EmptySpacepart {
public static final String rcsid = "$Id: FullSpacepart.java,v 1.1 2002/03/13 13:33:20 bfallenstein Exp $";

    public FullSpacepart(Space space, Cell base, String separator,
                          InclusionType inclusionType) {
        super(space, base, separator, inclusionType);
    }

    public boolean exists(String id) {
	return !stripBase(id).equals("");
    }
    public boolean exists(Object obj, int idx) {
	if(idx != 0)
	    return false;
	if(!(obj instanceof String))
	    return false;
	return !(obj.equals(""));
    }

    public Cell getCell(String id) {
	if(!exists(id))
	    throw new IllegalArgumentException("Cell "+id+" doesn't exist!");
	return new Cell(space, id, this, stripBase(id).intern(), 0);
    }
    public Cell getCell(Object obj, int idx) {
	if(!exists(obj, idx))
	    throw new IllegalArgumentException("Cell doesn't exist: "+obj+", "+idx);
	String id = (String)obj;
	return new Cell(space, addBase(id), this, id.intern(), 0);
    }

    public String getText(Cell c) {
	return (String)c.inclusionObject;
    }
}
