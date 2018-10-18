/*   
TestTemplate.java
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
package org.gzigzag.clang.test;
import org.gzigzag.*;
import org.gzigzag.clang.*;
import junit.framework.*;

/** Test Archimedes' parameter template.
 */

public class TestTemplate extends TestCase {
public static final String rcsid = "$Id: TestTemplate.java,v 1.3 2001/06/16 09:52:05 tjl Exp $";

    private static final void pa(String s) { System.out.println(s); }

    public TestTemplate(String s) { super(s); }

    ZZDimSpace sp = new ZZDimSpace();
    ZZCell home = sp.getHomeCell();
    Namespace empty = new Namespace(sp);

    public void testParameterTemplate() {
	Expression e1 = new Expression(sp, "+");
	e1.set("d.1", -1, 3);
	
	Expression e2 = e1.create("d.1", 1, "+");
	e2.set("d.1", -1, 7);
	e2.set("d.1", 1, 4);
	
	ZZCell root = e1.main.getRootclone();
	assertTrue("Both + expressions have the same rootclone",
		root.equals(e2.main.getRootclone()));
	
	ZZCell pl = root.s("d.1", -1), pr = root.s("d.1");
	assertNotNull(pl);
	assertNotNull(pr);
	
	ZZCell l1 = e1.getParam("d.1", -1), r1 = e1.getParam("d.1", 1);
	ZZCell l2 = e2.getParam("d.1", -1), r2 = e2.getParam("d.1", 1);
	
	assertEquals(l1, ParamTemplate.rootToExpression(pl, e1));
	assertEquals(l2, ParamTemplate.rootToExpression(pl, e2));
	assertEquals(r1, ParamTemplate.rootToExpression(pr, e1));
	assertEquals(r2, ParamTemplate.rootToExpression(pr, e2));
	
	assertEquals(pl, ParamTemplate.expressionToRoot(l1, e1));
	assertEquals(pl, ParamTemplate.expressionToRoot(l2, e2));
	assertEquals(pr, ParamTemplate.expressionToRoot(r1, e1));
	assertEquals(pr, ParamTemplate.expressionToRoot(r2, e2));
	
	assertEquals(pl, ParamTemplate.superExpressionToRoot(l1, e1));
	assertEquals(pl, ParamTemplate.superExpressionToRoot(l2, e2));
	assertEquals(pr, ParamTemplate.superExpressionToRoot(r1, e1));
	assertEquals(pr, ParamTemplate.superExpressionToRoot(r2, e2));
	
	assertEquals(pr, ParamTemplate.superExpressionToRoot(e2.main, e1));
    }

}
