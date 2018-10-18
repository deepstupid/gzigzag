/*   
TestPrimitives.java
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

public class TestPrimitives extends TestCase {
public static final String rcsid = "$Id: TestPrimitives.java,v 1.4 2001/06/16 09:52:05 tjl Exp $";

    private static final void pa(String s) { System.out.println(s); }

    public TestPrimitives(String s) { super(s); }

    static public ZZCell evaluateExpression(ZZCell main, ZZCell context) {
	return Archimedes.evaluateExpression(new Expression(main), 
					     new Namespace(context));
    }
    static public ZZCell evaluateExpression(Expression exp, Namespace context) {
	return Archimedes.evaluateExpression(exp, context);
    }

    ZZDimSpace sp = new ZZDimSpace();
    ZZCell home = sp.getHomeCell();
    Namespace empty = new Namespace(sp);

    public void testAddition() {
	ZZCell main = home.N();
	main.setText("+");
	main.N("d.1", -1).setText("7");
	main.N("d.1",  1).setText("4");

	Callable p = AllPrimitives.add;
	int result = Data.i(p.evaluate(new Expression(main), null));
	assertEquals(result, 11);
	
	p = AllPrimitives.get(main);
	result = Data.i(p.evaluate(new Expression(main), null));
	assertEquals(result, 11);
	
	result = Data.i(evaluateExpression(main, null));
	assertEquals(result, 11);
    }

    public void testMultiplication() {
	ZZCell main = home.N();
	main.N("d.1", -1).setText("6");
	main.N("d.1",  1).setText("7");
	main.setText("*");

	Callable p = AllPrimitives.mul;
	int result = Data.i(p.evaluate(new Expression(main), null));
	assertEquals(result, 42);

	p = AllPrimitives.get(main);
	result = Data.i(p.evaluate(new Expression(main), null));
	assertEquals(result, 42);
	
	result = Data.i(evaluateExpression(main, null));
	assertEquals(result, 42);
    }

    /** Test a := (7+3). */
    public void testAssignment() {
	Namespace context = new Namespace(sp);
	
	Expression assign = new Expression(sp, ":=");
	ZZCell a = assign.getParam("d.1", -1);
	Expression add = assign.create("d.1", 1, "+");
	
	add.set("d.1", -1, 7);
	add.set("d.1", 1, 3);
	
	evaluateExpression(assign, context);
	
	assertTrue(context.getint(a) == 10);
    }

    public void testStringAssignment() {
	Namespace context = new Namespace(sp);
	Expression assign = new Expression(sp, ":=");
	
	ZZCell a = assign.getParam("d.1", -1);
	assign.set("d.1", 1, "TEST");
	
	evaluateExpression(assign, context);
	
	assertEquals(context.getstr(a), "TEST");
    }

    /** Test <, <=, ==, >=, >, != */
    public void testCompare() {
	Expression eq = new Expression(sp, "==");
	Expression lt = new Expression(sp, "==");
	Expression gt = new Expression(sp, "==");
	
	eq.set("d.1", -1, 4);
	eq.set("d.1", 1, 4);
	lt.set("d.1", -1, 3);
	lt.set("d.1", 1, 5);
	gt.set("d.1", -1, 6);
	gt.set("d.1", 1, -2);
	
	assertTrue(Data.b(evaluateExpression(eq, empty)));
	assertTrue(!Data.b(evaluateExpression(lt, empty)));
	assertTrue(!Data.b(evaluateExpression(gt, empty)));
	
	eq.main.setText("!="); lt.main.setText("!="); gt.main.setText("!=");

	assertTrue(!Data.b(evaluateExpression(eq, empty)));
	assertTrue(Data.b(evaluateExpression(lt, empty)));
	assertTrue(Data.b(evaluateExpression(gt, empty)));
	
	eq.main.setText("<="); lt.main.setText("<="); gt.main.setText("<=");

	assertTrue(Data.b(evaluateExpression(eq, empty)));
	assertTrue(Data.b(evaluateExpression(lt, empty)));
	assertTrue(!Data.b(evaluateExpression(gt, empty)));
	
	eq.main.setText("<"); lt.main.setText("<"); gt.main.setText("<");

	assertTrue(!Data.b(evaluateExpression(eq, empty)));
	assertTrue(Data.b(evaluateExpression(lt, empty)));
	assertTrue(!Data.b(evaluateExpression(gt, empty)));
	
	eq.main.setText(">="); lt.main.setText(">="); gt.main.setText(">=");

	assertTrue(Data.b(evaluateExpression(eq, empty)));
	assertTrue(!Data.b(evaluateExpression(lt, empty)));
	assertTrue(Data.b(evaluateExpression(gt, empty)));
	
	eq.main.setText(">"); lt.main.setText(">"); gt.main.setText(">");

	assertTrue(!Data.b(evaluateExpression(eq, empty)));
	assertTrue(!Data.b(evaluateExpression(lt, empty)));
	assertTrue(Data.b(evaluateExpression(gt, empty)));
    }

    /** Test the incparam hack.
     *  It's important this works right; otherwise, the 
     *  TestArchimedesObjects.testThunk() test won't be reliable.
     */
    public void testIncparam() {
	Expression e = new Expression(sp, "incparam");
	e.set("d.1", 1, 45);
	assertEquals(Data.i(e.getParam("d.1", 1)), 45);
	
	evaluateExpression(e, empty);
	assertEquals(Data.i(e.getParam("d.1", 1)), 46);
	
	evaluateExpression(e, empty);
	assertEquals(Data.i(e.getParam("d.1", 1)), 47);
	
	evaluateExpression(e, empty);
	assertEquals(Data.i(e.getParam("d.1", 1)), 48);
    }

    /** Test the install() routine of AllPrimitives.BinaryOp objects. */
    public void testBinaryOpInstaller() {
	Expression e = new Expression(sp, "*");
	ZZCell mul = e.main.getRootclone();

	assertTrue(mul.s("d.1", -1) != null);
	assertTrue(mul.s("d.1") != null);
	assertEquals(mul.s("d.1", -1).getRootclone(), mul.s("d.zt-text").getRootclone());
	assertEquals(mul.s("d.1").getRootclone(), mul.s("d.zt-text", 3).getRootclone());
	assertEquals(mul.s("d.zt-text", 2).t(), " * ");
	assertEquals(mul.s("d.zt-text", 4), null);
	
	e = new Expression(sp, "<=");
	ZZCell lte = e.main.getRootclone();

	assertTrue(lte.s("d.1", -1) != null);
	assertTrue(lte.s("d.1") != null);
	assertEquals(lte.s("d.1", -1).getRootclone(), lte.s("d.zt-text").getRootclone());
	assertEquals(lte.s("d.1").getRootclone(), lte.s("d.zt-text", 3).getRootclone());
	assertEquals(lte.s("d.zt-text", 2).t(), " <= ");
	assertEquals(lte.s("d.zt-text", 4), null);
    }

    /** Test whether the terminate primitive raises the appropriate exception.
     *  For a more sophisticated test of early termination, see the
     *  TestEarlyTermination class.
     */
    public void testTerminate() {
	ZZCell termination = home.N();
	Expression e = new Expression(sp, "terminate");
	e.set("d.1", 1, termination);
	
	boolean raised = false;
	try {
	    Archimedes.evaluateExpression(e, empty);
	} catch(EarlyTermination et) {
	    raised = true;
	    assertEquals(et.cell, termination);
	}
	
	assertTrue("The terminate primitive didn't raise an EarlyTermination.",
	       raised);
    }
}
