/*   
TestZaubertrankTemplate.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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
package org.zaubertrank;
import org.gzigzag.*;
import org.gzigzag.clang.*;
import junit.framework.*;

/** Test the zaubertrank's text template.
 */

public class TestZaubertrankTemplate extends TestCase {
public static final String rcsid = "$Id: TestZaubertrankTemplate.java,v 1.5 2001/04/23 13:18:01 bfallenstein Exp $";

    private static final void pa(String s) { System.out.println(s); }

    public TestZaubertrankTemplate(String s) { super(s); }

    ZZDimSpace sp = new ZZDimSpace();
    ZZCell home = sp.getHomeCell();
    Namespace empty = new Namespace(sp);

    Function f; ZZCell root, par1, zpar1, par2, zpar2;
    Expression e1, e2; ZZCell l1, r1, l2, r2;
    ZaubertrankTemplate.TreeCursor tc;
    ZZCell par0;

    public void setUp() {
	Function f = new Function(sp, "f", new Expression(sp, "+"));
	root = f.main;
	par1 = f.getParam("d.1", -1); zpar1 = par1.zzclone(); par1.setText("par1");
	par2 = f.getParam("d.1", 1); zpar2 = par2.zzclone(); par2.setText("par2");
	
	root.N("d.zt-text").setText("bla");
	root.insert("d.zt-text", 1, zpar2);
	root.N("d.zt-text").setText("ignore this");
	root.N("d.zt-text").setText("ignore this as well");
	root.insert("d.zt-text", 1, zpar1);
	root.N("d.zt-text").setText("ignore even more");
		
	e1 = new Expression(sp, f);
	l1 = e1.getParam("d.1", -1); r1 = e1.getParam("d.1", 1);
	e1.set("d.1", -1, "l1"); r1.setText("r1");
	
	e2 = e1.create("d.1", 1, f);
	l2 = e2.getParam("d.1", -1); r2 = e2.getParam("d.1", 1);
	e2.set("d.1", -1, "l2");
	e2.set("d.1", 1, "r2");

	par0 = home.N(); par0.setText("par0"); e1.connect(par0);

	VirtualEditCursor vc = new VirtualEditCursor(par0);
	vc.push(l1);
        tc = new ZaubertrankTemplate.TreeCursor(vc);
    }

    public void testZaubertrankTemplate() {
	assertEquals("findParam finds the first param in the zttext",
		     zpar1, ZaubertrankTemplate.findParam(root, par1));
	assertEquals("findParam finds the second param in the zttext",
		     zpar2, ZaubertrankTemplate.findParam(root, par2));
	
	assertEquals("nextParam walks from second to first param",
		     zpar2, ZaubertrankTemplate.nextParam(zpar1, 1));
	assertEquals("nextParam walks from first to second param",
		     zpar1, ZaubertrankTemplate.nextParam(zpar2, -1));

	assertEquals("nextParam walks from root to first param",
		     zpar1, ZaubertrankTemplate.nextParam(root, 1));

	assertEquals(null, ZaubertrankTemplate.nextParam(zpar1, -1));
	assertEquals(null, ZaubertrankTemplate.nextParam(zpar2, 1));

	assertEquals(zpar1, ZaubertrankTemplate.lastParam(root, -1));
	assertEquals(zpar2, ZaubertrankTemplate.lastParam(root, 1));
    }

    public void testTreeCursor() {
	assertEquals(par0, tc.move(-1));
	assertEquals(null, tc.move(-1));
	assertEquals(-1, tc.side);

	assertEquals(l1, tc.move(1)); assertEquals(-1, tc.side);
	assertEquals(l1, tc.move(1)); assertEquals(1, tc.side);
	assertEquals(r1, tc.move(1)); assertEquals(-1, tc.side);
	assertEquals(l2, tc.move(1)); assertEquals(-1, tc.side);
	assertEquals(l2, tc.move(1)); assertEquals(1, tc.side);
	assertEquals(r2, tc.move(1)); assertEquals(-1, tc.side);
	assertEquals(r2, tc.move(1)); assertEquals(1, tc.side);
	assertEquals(r1, tc.move(1)); assertEquals(1, tc.side);
	assertEquals(par0, tc.move(1)); assertEquals(1, tc.side);

	// now, the other way around...

	assertEquals(r1, tc.move(-1));
	assertEquals(r2, tc.move(-1));
	assertEquals(r2, tc.move(-1));
	assertEquals(l2, tc.move(-1));
	assertEquals(l2, tc.move(-1));
	assertEquals(r1, tc.move(-1));
	assertEquals(l1, tc.move(-1));
	assertEquals(l1, tc.move(-1));
	assertEquals(par0, tc.move(-1));
	assertEquals(null, tc.move(-1));
	
    }

    public void testNextUndefined() {
	l2.setText(""); // make "undefined"
	assertEquals(null, tc.moveToNextUndefined(-1));
	assertEquals(null, tc.moveToNextUndefined(-1));
	assertEquals(l2, tc.moveToNextUndefined(1));
	assertEquals(null, tc.moveToNextUndefined(-1));

	assertEquals(l2, tc.moveToNextUndefined(1));
	assertEquals(l2, tc.moveToNextUndefined(1));
	assertEquals("end of stream->null", null, tc.moveToNextUndefined(1));
	assertEquals("tc accurses right end", par0, tc.par);
	assertEquals("posward side", 1, tc.side);

	assertEquals(l2, tc.moveToNextUndefined(-1));
	assertEquals(l2, tc.moveToNextUndefined(-1));
	assertEquals(null, tc.moveToNextUndefined(-1));
	
	tc.cur.push(l1); tc.read(); tc.side = -1;
	r1.setText(""); r1.excise("d.expression");
	assertEquals(r1, tc.moveToNextUndefined(1));
    }

}













