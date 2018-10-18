/*   
ZZDimension.java
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

/** Dimension-centric implementation.
 * A ZZDimension represents a single dimension of a space.
 * It is called with the cell objects that simply contai.
 * <p>
 * Some of the routines are given here so that they may be optimized.
 * The absolute core routines that every subclass must implement 
 * are the abstract routines <b>s, connect and disconnect</b>.
 */

public abstract class ZZDimension {
public static final String rcsid = "$Id: ZZDimension.java,v 1.25 2001/01/24 08:32:16 veparkki Exp $";

    /** The space this dimension is affiliated with. */
    protected ZZDimSpace space;
    public void setSpace(ZZDimSpace s) { space = s; }

    /** The observer trigger for this dimension.
     */
    protected ZZObsTrigger triggers = new ZZObsTrigger();

    // These three routines need to be implemented

    /** Get another cell <I>steps</I> steps on the dimension from c.
     * @param c     The cell
     * @param steps Number of steps, can be negative.
     */
    public abstract ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o); 
    /** Connect the two cells in this dimension, in order.
     */
    public abstract void connect(ZZCellHandle c, ZZCellHandle d);
    /** Disconnect the cell in the given direction.
     */
    public abstract void disconnect(ZZCellHandle c, int dir);

    // Really throw out the old functions and 
    // make them so that no-one can override them.

/*
    final public String s(String c, int steps, ZZObs o) 
	{ throw new ZZError("Old dim API"); } 
    final public void connect(String c, String d)
	{ throw new ZZError("Old dim API"); } 
    final public void disconnect(String c, int dir)
	{ throw new ZZError("Old dim API"); } 
 */


    /** The rest of the operations from here can be overridden
     * for optimizations, but do it is not required: the above operations
     * are all that is really needed.
     */

    /** Get headcell.
     */

    // XXX loops
    public ZZCellHandle h(ZZCellHandle c, int dir, ZZObs o) {
	ZZCellHandle orig = c;
	ZZCellHandle prev = c;
	while((c=s(c, dir, o))!=null) {
		prev = c;
		// Return lexically maximum id.
		if(c.equals(orig)) {
		    // throw new ZZInfiniteLoop("CIRCULAR HEAD");
		    ZZCellHandle lid = c;
		    while((c=s(c ,dir, o))!=null
			&& !c.equals(orig)) {
			if(c.id.compareTo(lid.id) > 0) lid = c;
		    }
		    return lid;
		}
	}
	return prev;
    }

    public ZZCellHandle h(ZZCellHandle c, int dir) {
	return h(c, dir, null);
    }

    final void connect(ZZCellHandle c, int dir, ZZCellHandle d) {
	    if(dir<0) 
		    connect(d,c);
	    else
		    connect(c,d);
    }

    public void insert(ZZCellHandle c, int dir, ZZCellHandle d) {
	    ZZCellHandle p = s(d, 1);
	    ZZCellHandle m = s(d, -1);
	    //System.out.println("c = " + c);
	    //System.out.println("d = " + d);
	    //System.out.println("p = " + p);
	    //System.out.println("m = " + m);

	    if(p!=null)
		disconnect(p, -1);
	    if(m!=null)
		disconnect(m, 1);
	    if(p!=null && m!=null)
		connect(m, p);
	    ZZCellHandle o = s(c, dir);
	    if(o!=null)
		disconnect(c, dir);
	    
	    connect(c,dir,d);
	    if(o!=null)
		connect(d,dir,o);
    }

    /** Remove the given cell from this dimension.
     */
    public void excise(ZZCellHandle c) {
	    ZZCellHandle p = s(c, 1);
	    ZZCellHandle m = s(c, -1);
	    disconnect(c, 1);
	    disconnect(c, -1);
	    if(p!=null && m!=null)
		    connect(m, p);
    }

    public void hop(ZZCellHandle c, int steps) {
	if(steps == 0) return;
	ZZCellHandle n = s(c, steps);
	if(n == null ) return;
	insert(n, (steps > 0 ? 1 : -1), c);
    }

    /** Find the text in a cell. 
     * This routine is here so that it may be optimized if desired.
     * If it is, then the main space object must remember to update 
     * this dimension if it keeps a cache in a hash etc.
     */
    public ZZCellHandle findText(ZZCellHandle c, int dir, String txt) {
	    ZZCellHandle cur = s(c, 1);
	    while(cur != null && cur != c && 
		!cur.getText().equals(txt))
		    cur = s(cur, 1);
	    if(cur==c) return null;
	    return cur;
    }

    public ZZCellHandle s(ZZCellHandle c, int steps) {
	return s(c, steps, null);
    }

    // Same goes for these, obviously
    // ZZCell intersect(ZZCell c, int dir, ZZDimension d2, ZZCell c2, int dir2);
    // ZZCell[] intersectAll(ZZCell c, int dir, ZZDimension d2, ZZCell c2, int dir2);




}
