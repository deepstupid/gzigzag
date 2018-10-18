/*   
AbstractSpace.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka and Tuukka Hastrup
 */

package org.gzigzag;

/** An abstract implementation of <code>Space</code>.
 */

public abstract class AbstractSpace implements Space {
public static final String rcsid = "$Id: AbstractSpace.java,v 1.2 2001/10/04 06:42:22 tjl Exp $";

    abstract public Cell getHomeCell();
    abstract public Cell getCell(String id);
    abstract public Dim getDim(Cell name);

    public Dim getDim(String s) {
	return getDim(getCell(s));
    }

    abstract public Dim getCloneDim();

    public Cell getRootclone(Cell c, Obs o) {
	return getCloneDim().h(c, -1, o);
    }

    public Cell getRootclone(Cell c) {
	return getRootclone(c, null);
    }

    public final Cell N() {
	return N(getHomeCell());
    }

    // Delegated from Cell

    abstract public Cell N(Cell c);
    abstract public Cell N(Cell c, Dim dim, int dir, Obs o);
    final public Cell N(Cell c, Cell dim, int dir, Obs o) {
	return N(c, getDim(dim), dir, o);
    }
    abstract public void delete(Cell c);


    abstract public void setSpan(Cell c, Span s);
    abstract public Span getSpan(Cell c, Obs o);

    abstract public void setText(Cell c, String s);
    abstract public String getText(Cell c, Obs o);

    public Object getJavaObject(Cell c, Obs o) {
	return null;
    }

    /** Whether a cell with the given ID exists in this space.
     *  Default: return true.
     */
    public boolean exists(String id) {
	return true;
    }

    public org.gzigzag.mediaserver.Mediaserver getMediaserver() {
	return null;
    }

    public Cell getMSBlockCell(String msid, Cell cell) {
	return null;
    }
}
