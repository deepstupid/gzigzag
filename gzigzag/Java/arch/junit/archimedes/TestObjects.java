/*   
TestObjects.java
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

/** Test for the Archimedes Clang Procedural Level
 */

public class TestObjects extends TestCase {
public static final String rcsid = "$Id: TestObjects.java,v 1.3 2001/06/16 09:52:05 tjl Exp $";

    private static final void pa(String s) { System.out.println(s); }

    public TestObjects(String s) { super(s); }

    ZZDimSpace sp = new ZZDimSpace();
    ZZCell home = sp.getHomeCell();
    Namespace empty = new Namespace(sp);

    public void testNamespace() {
	Namespace sf = new Namespace(sp);
	
	ZZCell var1 = home.N(), var2 = home.N();
	
	sf.put(var1, 12);
	sf.put(var2, 24);
	
	assertTrue(Data.i(sf.get(var1)) == 12);
	assertTrue(Data.i(sf.get(var2)) == 24);
	
	sf.put(var2, var1);
	sf.put(var1, home);
	
	assertEquals(sf.getcell(var1), home);
	assertEquals(sf.getcell(var2), var1);

	sf.put(var1, 11);
	sf.put(var1, 14);
	sf.put(var2, 31);
	
	assertTrue(sf.getint(var1) == 14);
	assertTrue(sf.getint(var2) == 31);
	assertEquals(sf.getstr(var2), "31");
	
	assertTrue(Namespace.isVariable(var1));
	assertTrue(Namespace.isVariable(var2));
	assertTrue(!Namespace.isVariable(home));
	assertTrue(!Namespace.isVariable(sf.main));
	assertTrue(!Namespace.isVariable(sf.get(var2)));
	
	sf.put(var1, true);
	sf.put(var2, "yoh");
	
	assertTrue(sf.getbool(var1));
	assertEquals(sf.getstr(var1), "true");
	assertEquals(sf.getstr(var2), "yoh");
	
	sf.put(var2, var1);
	sf.put(var1, home);
	
	assertEquals(sf.getcell(var1), home);
	assertEquals(sf.getcell(var2), var1);
    }

    /** Test (7*8) + (4*4) == 56 + 16 == 72 using Expression objects. */
    public void testExpression() {
	Expression add = new Expression(sp, "+");
	Expression left = add.create("d.1", -1, "*");
	Expression right = add.create("d.1", 1, "*");
	
	left.set("d.1", -1, 7);
	left.set("d.1", 1, 8);
	right.set("d.1", -1, 4);
	right.set("d.1", 1, 4);
	
	int result = Data.i(Archimedes.evaluateExpression(add, empty));
	assertEquals(result, 72);
    }

    public void testMakeVariable() {
	ZZCell c = home.N();
	assertTrue(!Namespace.isVariable(c));
	Namespace.makeVariable(c);
	assertTrue(Namespace.isVariable(c));
	Namespace.makeVariable(c);
	assertTrue(Namespace.isVariable(c));
    }

    public void testParser() {
	Function f = new Function(sp, "test", new Expression(sp, "+"));
	ZZCell v0 = f.getParam("d.1", -1);
	ZZCell v1 = f.getParam("d.1", 1);
	ZZCell v2 = f.getParam("d.1", 2);

	Expression e = new Expression(sp, f.main);
	e.set("d.1", -1, 12);
	e.set("d.1", 1, 17);
	e.set("d.1", 2, 23);

	Namespace n = new Namespace(sp);
	Parser.parse(f.main, e.main, new Namespace(sp), n);

	assertTrue(n.getint(v0) == 12);
	assertTrue(n.getint(v1) == 17);
	assertTrue(n.getint(v2) == 23);
    }

    public void testThunk() {
	ZZCell c = home.N(); c.setText("44");
	Thunk t = new Thunk(sp, c);
	assertEquals(t.getint(), 44);
	assertEquals(t.getint(), 44);
		
	Expression e = new Expression(sp, "+");
	e.set("d.1", -1, 5);
	e.set("d.1", 1, 5);
	
	t = new Thunk(sp, e);
	
	assertEquals(t.getint(), 10);
	assertEquals(t.getint(), 10);
	
	e = new Expression(sp, "incparam");
	e.set("d.1", 1, 77);
	
	t = new Thunk(sp, e);
	
	assertEquals(t.getint(), 78);
	assertEquals(t.getint(), 78);
	assertEquals(t.getint(), 78);
    }

    /** Test the primitive list managed by AllPrimitives. */
    public void testPrimitiveList() {
	ZZCell c = AllPrimitives.getCell(sp, "+");
	ZZCell d = AllPrimitives.getCell(sp, "*");
	
	assertTrue(c.s("d.primitive", -1) != null);
	assertTrue(d.s("d.primitive", -1) != null);
	
	assertEquals(c.h("d.primitive"), d.h("d.primitive"));
	assertEquals(c.t(), "+");
	assertEquals(d.t(), "*");
	assertEquals(c, AllPrimitives.getCell(sp, "+"));
    }

}
