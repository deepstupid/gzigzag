/*   
Spacepart.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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
package org.gzigzag;
import java.util.*;

/** An abstract implemenatation of the <code>Spacepart</code> interface.
 * @see Spacepart
 */

public abstract class AbstractSpacepart implements Spacepart {
public static final String rcsid = "$Id: AbstractSpacepart.java,v 1.7 2002/03/11 23:31:42 bfallenstein Exp $";

    protected final Space space;
    protected final Cell base;
    protected final String separator;
    protected final InclusionType inclusionType;

    public AbstractSpacepart(Space space, Cell base, String separator,
			     InclusionType inclusionType) {
	this.space = space;
	this.base = base;
	this.separator = separator;
	this.inclusionType = inclusionType;
    }


    public final Space getSpace() {
	return space;
    }

    public final Cell getBase() {
	return base;
    }

    public final String getSeparator() {
	return separator;
    }

    public final InclusionType getInclusionType() {
	return inclusionType;
    }



    public final String stripBase(String id) {
	return id.substring(base.id.length() + separator.length());
    }

    public final String addBase(String id) {
	return base.id + separator + id;
    }

    public Cell N(Cell c, Cell dim, int dir, Obs o) {
	Cell n = N();
	insert(dim, c, dir, n); // XXX Obs ?!? what should they do ?!?
	return n;
    }

    public Span getSpan(Cell c, Obs o) {
	return null;
    }
    public void setSpan(Cell c, Span s) {
	throw new UnsupportedOperationException("setSpan not supported");
    }
    public String getText(Cell c, Obs o) {
	return null;
    }
    public void setText(Cell c, String s) {
	throw new UnsupportedOperationException("setText not supported");
    }
}
