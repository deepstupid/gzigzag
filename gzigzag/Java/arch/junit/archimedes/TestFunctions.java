/*   
TestFunctions.java
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

public class TestFunctions extends TestCase {
public static final String rcsid = "$Id: TestFunctions.java,v 1.3 2001/06/16 09:52:05 tjl Exp $";

    private static final void pa(String s) { System.out.println(s); }

    public TestFunctions(String s) { super(s); }

    ZZDimSpace sp = new ZZDimSpace();
    ZZCell home = sp.getHomeCell();
    Namespace empty = new Namespace(sp);

    /** Test a function without parameters. */
    public void testTrivialFunction() {
	ZZCell main = home.N();
	main.setText("ten plus two");
	
	Expression mul = new Expression(sp, "+");
	mul.connect(main);
	mul.set("d.1", -1, 10);
	mul.set("d.1", 1, 2);
	
	Expression func = new Expression(sp, main);
	
	int result = Data.i(Archimedes.evaluateExpression(func, empty));
	assertTrue(result == 12);
    }

    public void testFunction() {
	ZZCell main = home.N();
	main.setText("three times");
	ZZCell var = main.N("d.1", 1);
	Namespace.makeVariable(var);
	
	Expression mul = new Expression(sp, "*");
	mul.connect(main);
	mul.set("d.1", -1, 3);
	mul.create("d.1", 1, var);
	
	Namespace nam = new Namespace(sp);
	nam.put(var, 10);
	int r0 = Data.i(Archimedes.evaluateExpression(mul, nam));
	assertTrue(r0 == 30);
	
	Expression thrice = new Expression(sp, main);
	thrice.set("d.1", 1, 7);
	
	int result = Data.i(Archimedes.evaluateExpression(thrice, empty));
	assertTrue(result == 21);
    }

    public void testCascadingFunctions() {
	Expression mul = new Expression(sp, "*");
	Function mfn = new Function(sp, mul);
	mul.create("d.1", -1, mfn.getParam("d.1", 1));
	mul.create("d.1",  1, mfn.getParam("d.1", 1));
	
	Expression add = new Expression(sp, "+");
	Function afn = new Function(sp, add);
	add.create("d.1", -1, afn.getParam("d.1", 1));
	add.create("d.1",  1, afn.getParam("d.1", 1));
	
	Expression eq = new Expression(sp, "==");
	Function fn = new Function(sp, eq);
	
	Expression mexp = eq.create("d.1", -1, mfn);
	mexp.create("d.1", 1, fn.getParam("d.1", 1));
	
	Expression aexp = eq.create("d.1",  1, afn);
	aexp.create("d.1", 1, fn.getParam("d.1", 1));
	
	Expression one =   new Expression(sp, fn); one.set("d.1", 1, 1);
	Expression two =   new Expression(sp, fn); two.set("d.1", 1, 2);
	Expression three = new Expression(sp, fn); three.set("d.1", 1, 3);
	
	assertTrue(!Data.b(Archimedes.evaluateExpression(one, empty)));
	assertTrue(Data.b(Archimedes.evaluateExpression(two, empty)));
	assertTrue(!Data.b(Archimedes.evaluateExpression(three, empty)));
    }
}
