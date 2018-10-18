/*   
ZZLocalDimension.java
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

/** Dimension-centric implementation of a locally 
 * stored dimension. 
 */
public class ZZLocalDimension extends ZZDimension {
public static final String rcsid = "$Id: ZZLocalDimension.java,v 1.14 2000/11/30 08:44:32 ajk Exp $";
	public static final boolean dbg = true;
        final static void p(String s) { if(dbg) ZZLogger.log(s); }
        final static void pa(String s) { ZZLogger.log(s); }

	/** Connections to the positive direction. */
	Hashtable cp = new Hashtable();
	/** Connections to the negative direction. */
	Hashtable cm = new Hashtable();

	public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
		String s = c.id;
		if(o != null) triggers.addObs(s, o);
		if(steps>0)
			while(steps-- > 0 && s != null) {
			    s = (String)cp.get(s);
			    if(o != null) triggers.addObs(s, o);
			}
		else
			while(steps++ < 0 && s != null) {
			    s = (String)cm.get(s);
			    if(o != null) triggers.addObs(s, o);
			}

		if(s==null) return null;
		return (ZZCellHandle)space.getCellByID(s);
	}
	public void connect(ZZCellHandle c, ZZCellHandle d) {
		disconnect(c, 1);
		disconnect(d, -1);
		/* XXX: This is from 0.3 branch, don't know if this
		  should be chosen instead of the above 
	    if(cp.get(c.id) != null ||
	       cm.get(d.id) != null)
		    throw new ZZConnectWouldBreakError("localdim");
		*/
		cp.put(c.id, d.id);
		cm.put(d.id, c.id);
		triggers.chg(c.id);
		triggers.chg(d.id);
	}
	public void disconnect(ZZCellHandle c, int dir) {
		String o;
		if(dir>0) {
			o = (String)cp.get(c.id);
			if(o!=null) {
				cp.remove(c.id);
				cm.remove(o);
			}
		} else {
			o = (String)cm.get(c.id);
			if(o!=null) {
				cm.remove(c.id);
				cp.remove(o);
			}
		}
		triggers.chg(c.id);
	}

}
