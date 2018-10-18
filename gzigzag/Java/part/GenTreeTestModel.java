/*   
GTreePartTestModel.java
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

/** A simple model to test GenTreePart.
 * The same model as ImpliedTreePart has, except that the depth 
 * numbers are reversed and are 0, 1, 2 and 3. 
 * The actual cells in the stream are not included in the tree, only
 * the section marker cells.
 * @see GenTreePart
 */

public class GenTreeTestModel implements GenTreePartModel {
public static final String rcsid = "$Id: GenTreeTestModel.java,v 1.1 2000/11/16 00:25:10 tjl Exp $";
    public static boolean dbg = true;
    final static void p(String s) { if(dbg) System.out.println(s); }
    final static void pa(String s) { System.out.println(s); }

    public int depth() {
	return 4;
    }

    public int depth(ZZCell c) {
	if(c.s("d.1") == null)
	    return -1;
	// So the int it 0..3
	return Integer.parseInt(c.getText());
    }

    public ZZCell getNext(int mindepth, ZZCell c, int steps) {
	if(steps == 0) return c;
	c = c.s("d.1");
	int dir = (steps > 0 ? 1 : -1);
	ZZCell lasthdr = null;
	while(steps != 0 && c != null) {
		c = c.s("d.2", dir);
		if(c == null) return null;
		lasthdr = c.s("d.1", -1);
		if(lasthdr == null) continue;
		if(depth(lasthdr) < mindepth) continue;
		steps -= dir;
	}
	if(c == null) return null;
	return lasthdr;
    }

    public void disconnectNeg(ZZCell c) {
	p("Model: disconnectneg "+c);
	c.h("d.1", 1).disconnect("d.2", -1);
    }

    public void connect(ZZCell c, ZZCell d) {
	p("Model: connect "+c+" "+d);
	c.h("d.1", 1).h("d.2", 1).connect("d.2", d.h("d.1", 1));
    }

    public void setDepth(ZZCell c, int d) {
	if(depth(c) == 0) return;
	c.setText(""+d);
    }

}
