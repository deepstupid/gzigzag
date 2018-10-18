/*   
ZZSpacePart.java
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

/** <b>EXPERIMENTAL:</b> A portion of a ZZ space.
 * This is a way to have some cells virtual, i.e. existing only
 * when called upon, and whose connections to other cells are algorithmic,
 * not stored.
 * <p>
 * For example, it is possible to make a huge tree-like calendar 
 * with thousands of years at nanosecond precision using these ideas:
 * the cell's ID in that part stores the data about the moment and
 * the dimensions give the movement from that cell to other cells.
 * <p>
 * Currently, cell IDs in part foo look like foo:... where ... is whatever
 * string that part wants to use.
 * Dimensions are like d.foo:... where ... is the name in the part.
 */

public abstract class ZZSpacePart {
public static final String rcsid = "$Id: ZZSpacePart.java,v 1.8 2001/03/08 13:37:16 ajk Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

    /** The space part id. This is prepended to all the IDs of the cells
     * within this part. (DELIM??!!XXX)
     */
    public final String id;

    /** The space this part is a part of.
     */
    public final ZZDimSpace space;

    public ZZSpacePart(ZZSpace space, String id) {
	this.space = (ZZDimSpace)space;
	this.id = id;
    }

    /** Obtain a special dimension for this part.
     * The dimension name has the prefix stripped off.
     * This function may return null.
     */
    abstract public ZZDimension getDim(String name);

    /** The home cell for this space part.  This is useful in case
     * it's not connected to anywhere in the space proper. */
    public String homeID() { return null; }

    public String getText(ZZCellHandle c) { return ""; }
    public Span getSpan(ZZCellHandle c) { return null; }
    public void setContent(ZZCellHandle c, Object o) {
	// do nothing
    }

    public Object parseID(String id) {
	int ind = id.indexOf(':');
	if(ind < 0) throw new SyntaxError("No delimiter in ID");
	return parseIDPart(id.substring(ind+1));
    }
    abstract public Object parseIDPart(String idPart);

    public String toString() { return id; }

    public String generateID(Object parsed) {
	String rv = id + ":" + generateIDPart(parsed);
        return rv;
    }
    abstract public String generateIDPart(Object parsed);

    public ZZDimSpace.DimCell getCellByID(String s) { return null; }

    public void postCommitHook() {}
}

