/*   
FilterDim.java
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

/** An abstract superclass for dimension wrappers.
 *  A minimal AbstractDim implementation that proxies calls to a base dim.
 *  Also is a CopyableDim and needs to be given one, too.
 */

public abstract class FilterDim extends AbstractDim implements CopyableDim {
public static final String rcsid = "$Id: FilterDim.java,v 1.2 2002/03/13 18:15:32 bfallenstein Exp $";

    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }

    public final CopyableDim base;
    public FilterDim(CopyableDim base) {
	super(base.getSpace());
	this.base = base;
    }

    public Cell s(Cell c, int steps, Obs o) {
	return base.s(c, steps, o);
    }

    public void connect(Cell c, Cell d)
        throws ZZAlreadyConnectedException {
	base.connect(c, d);
    }

    public void disconnect(Cell c, int dir) {
	base.disconnect(c, dir); 
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

    public Dim makeCopy(Space s, ObsTrigger o) {
        return base.makeCopy(s, o);
    }
}

