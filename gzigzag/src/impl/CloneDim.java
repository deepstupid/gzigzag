/*   
CloneDim.java
*    
*    Copyright (c) 2001, Benja Fallenstein
*    Copyright (c) 2001, Rauli Ruohonen
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
import java.util.*;
import org.gzigzag.*;

/** A clone dimension.
 *  This is based on another dimension and proxies all requests to it, except
 *  it doesn't allow cloning from the ID spacepart.
 *  <p>
 *  Also removes cells from SimpleTransient's rootclone cache when they
 *  are connected or disconnected.
 */

public class CloneDim extends AbstractDim {
    public static final String rcsid = "$Id: CloneDim.java,v 1.7 2002/03/13 18:15:32 bfallenstein Exp $";

    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }

    public final Dim base;
    public CloneDim(Dim base) {
        super(base.getSpace());
        this.base = base;
    }
    public Cell s(Cell c, int steps, Obs o) {
        return base.s(c, steps, o);
    }
    public void connect(Cell c, Cell d)
        throws ZZAlreadyConnectedException {
        if(c.id.startsWith("home-id:"))
            throw new ZZError("cannot clone a cell from the ID spacepart");
        base.connect(c, d);
        Cell x = d;
        try {
	    for(; x!=null && !x.equals(c); x = s(x)) {
		p("Remove from rootclone cache: "+x);
		((SimpleTransientSpace)space).rootcloneCache.remove(x);
	    }
        } catch(Error e) {
            if(dbg) {
                pa("Exception while removing from rootclone cache.");
                pa("I was at: "+x);
                e.printStackTrace();
            }

            // XXX Uncomment this and fix resulting exceptions:
            // throw e;
        }
    }
    public void disconnect(Cell c, int dir) {
        Cell d = (dir > 0) ? s(c, dir) : c;

        base.disconnect(c, dir); 

        Cell h = h(d);
        boolean f = true; // first iteration
        for(Cell x = h; x!=null && (f || !x.equals(h)); x = s(x)) {
            ((SimpleTransientSpace)space).rootcloneCache.remove(x);
            f = false;
        }
    }
    public boolean isCircularHead(Cell c, Obs o) {
        return base.isCircularHead(c, o);
    }
    public void addRealNegSides(Set to) {
        base.addRealNegSides(to);
    }

    public void canonicalizeCells() {
        base.canonicalizeCells();
    }
}
