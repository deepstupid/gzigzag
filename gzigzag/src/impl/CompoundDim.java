/*   
CompoundSpace.java
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
 * Written by Benja Fallenstein, based on code by Rauli Ruohonen
 */

package org.gzigzag.impl;
import java.util.*;
import org.gzigzag.*;
import org.gzigzag.vob.CharRangeIter;

class CompoundDim extends AbstractDim implements CompoundSpaceDim {
    public final CopyableDim base;
    public final Cell identity;
    final CompoundSpace cs;
    public CompoundDim(CompoundSpace cs, Cell identity, ObsTrigger trig) {
        super(cs);
        this.cs = cs;
        this.identity = identity;
        this.base = new UndoableDim(new SimpleDim(space, trig), cs.undo,
                                    Id.stripHome(Id.get(identity).id));
    }
    public CopyableDim getBase() { return base; }
    public Cell s(Cell c, int steps, Obs o) {
	if(steps > 1) {
	    for(int i=0; i<steps; i++) {
		if(c == null) return null;
		c = s(c, 1, o);
	    }
	    return c;
	} else if(steps == 0) {
	    return c;
	} else if(steps < -1) {
	    for(int i=0; i>steps; i--) {
		if(c == null) return null;
		c = s(c, -1, o);
	    }
	    return c;
	}

	if(c == null)
	    return null;

	Cell b = base.s(c, steps, o);
	if(b != null) return b;
	
	// if(c.id.indexOf(cs.separator) > -1) {
	if(c.inclusionObject != null)
	    if(c.spacepart == null || c.spacepart instanceof SliceSpacepart) {
		Cell res = cs.getSpaceNF(c).getDim(identity).s(cs.detranslate(c), steps, o);
		if(res == null) return null;
		else return cs.translate(res);
	    }
	return null;
    }
    public void connect(Cell c, Cell d)
	throws ZZAlreadyConnectedException {
	if(!cs.ghostcells && (s(c, 1, null) != null || s(d, -1, null) != null))
	    throw new ZZAlreadyConnectedException("X",
						  c, s(c,1), s(d,-1) ,d);
	Space cspace = cs.getSpace(c), dspace = cs.getSpace(d);
	if(cspace == null || cspace != dspace ||
	   !cs.isEditable(cspace))
	    base.connect(c, d);
	else
	    cs.detranslate(c).connect(identity, cs.detranslate(d));
    }
    public void disconnect(Cell c, int dir) {
	Space cspace = cs.getSpace(c);
	if(cspace == null || base.s(c, dir, null) != null)
	    base.disconnect(c, dir);
	else if(cs.isEditable(cspace))
	    cs.detranslate(c).disconnect(identity, dir);
	else
	    throw new ZZError("included space: cannot disconnect");
    }
    
    // XXX *ARGH*!!! HOW should this work?
    public boolean isCircularHead(Cell c, Obs o) {
	if(c.id.indexOf(cs.separator) > -1)
	    return cs.getSpaceNF(c).getDim(identity).isCircularHead(cs.detranslate(c), o);
	else
	    return base.isCircularHead(c, o);
    }
    
    public void addRealNegSides(Set to) {
	getBase().addRealNegSides(to);
	for(Iterator i = cs.getSpaces().iterator(); i.hasNext();) {
	    Space s = (Space)i.next();
	    HashSet set = new HashSet();
	    s.getDim(identity).addRealNegSides(set);
	    for(Iterator i2 = set.iterator(); i2.hasNext();) 
		to.add(cs.translate(s, (String)i2.next()));
	}
    }
    
    public void canonicalizeCells() {
	base.canonicalizeCells();
    }
}

