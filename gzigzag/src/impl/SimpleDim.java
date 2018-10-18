/*   
SimpleDim.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
package org.gzigzag.impl;
import org.gzigzag.*;
import java.util.*;

/** A simple, modifyable ZZ dimension.
 */

public class SimpleDim extends AbstractDim implements CopyableDim {
public static final String rcsid = "$Id: SimpleDim.java,v 1.5 2002/03/13 16:45:37 bfallenstein Exp $";
    public static final boolean dbg = true;
    // final static void p(String s) { if(dbg) ZZLogger.log(s); }
    // final static void pa(String s) { ZZLogger.log(s); }

    ObsTrigger trigger;

    public SimpleDim(Space space, ObsTrigger trigger) {
	super(space);
	this.trigger = trigger;
    }

    /** Key used for Obstrigger in positive direction.
     * The hashtable cp cannot be used directly as the key;
     * the content would be used, not the container.
     */
    final Object keyPos = new Object();
    final Object keyNeg = new Object();

    /** Connections to the positive direction. */
    HashMap cp = new HashMap();
    /** Connections to the negative direction. */
    HashMap cm = new HashMap();

    /** Headcells on looping ranks.
     *  If loopheads.get(c) != null, c is the headcell of a looping rank.
     */
    HashMap loopheads = new HashMap();

    public Cell s(Cell c, int steps, Obs o) {
	if(steps>0)
		while(steps-- > 0 && c != null) {
		    if(o != null) trigger.addObs(o, keyPos, c);
		    c = (Cell)cp.get(c);
		}
	else
		while(steps++ < 0 && c != null) {
		    if(o != null) trigger.addObs(o, keyNeg, c);
		    c = (Cell)cm.get(c);
		}
	return c;
    }

    public void connect(Cell c, Cell d) throws ZZAlreadyConnectedException {
	if(c.space != space || d.space != space)
	    throw new ZZWrongSpaceError("Dim: "+space+" "+c.space+" "+d.space);
	if(cp.get(c) != null ||
	   cm.get(d) != null)
	    throw new ZZAlreadyConnectedException("simpletransient",
						  c, s(c, 1), s(d, -1), d);
	if(h(c, -1, null).equals(d))
	    // we're closing a looping rank
	    loopheads.put(c, c);
	cp.put(c, d);
	cm.put(d, c);

	trigger.chg(keyPos, c);
	trigger.chg(keyNeg, d);
    }

    public void disconnect(Cell c, int dir) {
	Cell o;
	if(dir>0) {
		o = (Cell)cp.get(c);
		if(o!=null) {
		    if(loopheads.containsKey(h(c, -1, null)))
			loopheads.remove(h(c, -1, null));
		    cp.remove(c);
		    cm.remove(o);
		    
		    trigger.chg(keyPos, c);
		    trigger.chg(keyNeg, o);
		}
	} else {
		o = (Cell)cm.get(c);
		if(o!=null) {
		    if(loopheads.containsKey(h(c, -1, null)))
			loopheads.remove(h(c, -1, null));
		    cm.remove(c);
		    cp.remove(o);

		    trigger.chg(keyNeg, c);
		    trigger.chg(keyPos, o);
		}
	}
    }

    public boolean isCircularHead(Cell c, Obs o) {
	// obs!
	return loopheads.containsKey(c);
    }

    /** Make a copy of this dimension.
     */
    public Dim makeCopy(Space space, ObsTrigger trigger) {
	SimpleDim d = new SimpleDim(space, trigger);
	d.cp = (HashMap)cp.clone();
	d.cm = (HashMap)cm.clone();
	d.loopheads = (HashMap)loopheads.clone();
	return d;
    }

    /** Make the diff for two versions.
     * Important invariant: same cell is given only once, except
     * for prev-time.
     */
    public SimpleDimChangeList diff(SimpleDim from) {
	SimpleDimChangeList into = new SimpleDimChangeList();
	HashMap cpn = (HashMap)cp.clone();
	// First, the ones from the old one
	for(Iterator i = from.cp.keySet().iterator(); i.hasNext(); ) {
	    Cell neg = (Cell)i.next();
	    Cell prevpos = (Cell)from.cp.get(neg);
	    Cell pos = (Cell)cpn.get(neg);
	    cpn.remove(neg);
	    if(prevpos.equals(pos)) continue;
	    Cell prevneg = null;
	    if(pos != null) prevneg = (Cell)from.cm.get(pos);
	    if(prevpos != null && cm.get(prevpos) == null) {
		into.addChange(null, null, neg, prevpos);
	    }
	    into.addChange(neg, prevpos, prevneg, pos);
	}
	// Then, the ones that weren't there.
	for(Iterator i = cpn.keySet().iterator(); i.hasNext(); ) {
	    Cell neg = (Cell)i.next();
	    // prevpos = null
	    Cell pos = (Cell)cpn.get(neg);
	    Cell prevneg = (Cell)from.cm.get(pos);
	    into.addChange(neg, null, prevneg, pos);
	}

	into.from = from;
	into.to = this;
	return into;
    }

    public void addRealNegSides(Set to) {
	for(Iterator i = cp.keySet().iterator(); i.hasNext(); )
	    to.add(((Cell)i.next()).id);
    }

    public void canonicalizeCells() {
	Set s = new HashSet(cp.keySet());
	s.addAll(cm.keySet());
	for(Iterator i = s.iterator(); i.hasNext(); ) {
	    Cell c = (Cell)i.next();
	    Cell canon = space.getCell(c.id);
	    if(canon != c) {
		// a bit tricky, because by definition c.equals(canon).
		// we can't just put the new one in place, we have
		// to edit the key too.
		Cell c2 = (Cell)cm.get(c);
		if(c2 != null) {
		    cm.remove(c);
		    cm.put(canon, c2);
		    cp.put(c2, canon);
		}
		c2 = (Cell)cp.get(c);
		if(c2 != null) {
		    cp.remove(c);
		    cp.put(canon, c2);
		    cm.put(c2, canon);
		}
	    }
	}

    }
}
