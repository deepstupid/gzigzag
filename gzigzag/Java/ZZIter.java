/*   
ZZIter.java
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
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

/** Help for iterating along ranks in various ways.
 */

public class ZZIter {
public static final String rcsid = "$Id: ZZIter.java,v 1.3 2000/11/01 00:39:06 tjl Exp $";
    public static final boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }

    /** An callback interface for iterating along a rank with
     * the cell number.
     */
    public interface NIter {
	/** Do whatever you like with this cell, which is the nth from
	 * wherever we began.
	 * @return True, if the iteration is to continue (in this direction).
	 */
	boolean go(ZZCell c, int nth);
    }

    /** Go along a rank, starting from a cell, interleaving alternate
     * directions.
     * @param c The cell to start from.
     * @param included Whether the first cell is also included.
     * @param d The dimension to go along.
     * @param fdir The direction to take the first step in.
     * @param iter The callback object.
     */
    public static void alternate(ZZCell c, boolean included,
		String d, int fdir, NIter iter) {
	if(c == null) return;
	int i = 0;
	if(included) 
	    if(!iter.go(c, 0)) return;
	boolean in = true;
	boolean op = true;
	ZZCell inc = c;
	ZZCell opc = c;
	while(in || op) {
	    i++;
	    if(in) {
		inc = inc.s(d, fdir);
		if(inc == null) 
		    in = false;
		else 
		    if(!iter.go(inc, fdir * i))
			in = false;
	    }
	    if(op) {
		opc = opc.s(d, -fdir);
		if(opc == null)
		    op = false;
		else
		    if(!iter.go(opc, -fdir * i))
			op = false;
	    }
	}
    }

    /** A cell enumeration, with index.
     */
    public interface NEnum {
	/** Get the next cell.
	 */
	ZZCell nextCell();
	/** Get the index of the last returned cell from nextCell.
	 */
	int nth();
	/** Whether there are more cells.
	 */
	boolean more();
	/** Stop advancing in the current direction.
	 */
	void stop();
    }

    /** Return an enumeration that alternates between the directions.
     * @param c The cell to start from.
     * @param included Whether the first cell is also included.
     * @param d The dimension to go along.
     * @param fdir The direction to take the first step in.
     */
    public static NEnum alternate(final ZZCell c, final boolean included, final String d, final int fdir) {
	return new NEnum() {
	    /** Next cell in p or n direction.
	     */
	    ZZCell nextn = c.s(d, -fdir), nextp = c.s(d, fdir);

	    int ind = 0;

	    boolean started;

	    public ZZCell nextCell() {
		if(nextn == null && nextp == null) return null;
		if(!started) {
		    started = true;
		    if(included) return c;
		}
		// Trick: always change ind, only after see if we have something
		// to return. If not, recurse and get the next one from the other side.
		if(ind <= 0) {
		    ind = -ind;
		    ind ++;
		    if(nextp == null)
			return nextCell();
		    ZZCell ret = nextp;
		    nextp = nextp.s(d, fdir);
		    return ret;
		}
		if(ind > 0) {
		    ind = -ind;
		    if(nextn == null)
			return nextCell();
		    ZZCell ret = nextn;
		    nextn = nextn.s(d, -fdir);
		    return ret;
		}
		return null;
	    }
	    public int nth() {
		return fdir * ind;
	    }
	    public void stop() {
		if(ind >= 0) nextp = null;
		else nextn = null;
	    }
	    public boolean more() {
		return nextn != null || nextp != null;
	    }
	};
    }
}



