/*   
Nile1GenTreeModel.java
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
package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;

/** The nile1 structural model for GenTreePart.
 * This implements the nile1 structural model from H1 to P levels as a
 * tree using GenTreePart.
 * @see GenTreePart
 */

public class Nile1GenTreeModel implements GenTreePartModel {
public static final String rcsid = "$Id: Nile1GenTreeModel.java,v 1.3 2000/11/22 00:35:41 tjl Exp $";
    public static boolean dbg = true;
    final static void p(String s) { if(dbg) System.out.println(s); }
    final static void pa(String s) { System.out.println(s); }

    public int depth() {
	return 7;
    }

    public int depth(ZZCell c) {
	if(c.s("d.nile-struct", -1) == null) return -1;
	return Nile1Ops.getLevel(c);
    }

    public ZZCell getNext(int mindepth, ZZCell c, int steps) {
	if(steps == 0) return c;
	c = c.s("d.nile-struct", -1);
	int dir = (steps > 0 ? 1 : -1);
	ZZCell lasthdr = null;
	while(steps != 0 && c != null) {
		c = c.s("d.nile", dir);
		if(c == null) return null;
		lasthdr = c.s("d.nile-struct", 1);
		if(lasthdr == null) continue;
		if(depth(lasthdr) < mindepth) continue;
		steps -= dir;
	}
	if(c == null) return null;
	return lasthdr;
    }

    public void disconnectNeg(ZZCell c) {
	p("Model: disconnectneg "+c);
	c.s("d.nile-struct", -1).disconnect("d.nile", -1);
    }

    public void connect(ZZCell c, ZZCell d) {
	p("Model: connect "+c+" "+d);
	c.s("d.nile-struct", -1).h("d.nile", 1).connect("d.nile", 
						    d.s("d.nile-struct", -1));
    }

    public void setDepth(ZZCell c, int d) {
	if(depth(c) < 0) return;
	Nile1Ops.setLevel(c, d);
    }

}
