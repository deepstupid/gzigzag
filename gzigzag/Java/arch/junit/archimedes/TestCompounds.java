/*   
TestCompounds.java
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

public class TestCompounds extends TestCase {
public static final String rcsid = "$Id: TestCompounds.java,v 1.3 2001/06/16 09:52:05 tjl Exp $";

    private static final void pa(String s) { System.out.println(s); }

    public TestCompounds(String s) { super(s); }

    static public ZZCell evaluateExpression(ZZCell main, ZZCell context) {
	return Archimedes.evaluateExpression(new Expression(main), 
					     new Namespace(context));
    }

    ZZDimSpace sp = new ZZDimSpace();
    ZZCell home = sp.getHomeCell();
    Namespace empty = new Namespace(sp);

    /** Test a cascading expression, (7+3)*4. */
    public void testMixedArithmetic() {
	ZZCell main = home.N();
	main.setText("*");
	main.N("d.1", 1).setText("4");
	
	ZZCell add = main.N("d.1", -1).N("d.expression", -1);
	add.setText("+");
	add.N("d.1", -1).setText("7");
	add.N("d.1", 1).setText("3");
	
	
	int result = Data.i(evaluateExpression(main, null));
	assertEquals(result, 40);
    }

    /** Test arithmetic getting some variables from a namespace.
     *  Compute (a+3)*b, where a=15 and b=2.
     */
    public void testVariableArithmetic() {
	ZZCell sfcell = home.N();
	Namespace sf = new Namespace(sfcell);
	ZZCell a = home.N(), b = home.N();
	sf.put(a, 15);
	sf.put(b, 2);
	
	ZZCell main = home.N();
	main.setText("*");
	main.N("d.1").connect("d.expression", -1, b.zzclone());
	
	ZZCell add = main.N("d.1", -1).N("d.expression", -1);
	add.setText("+");
	add.N("d.1", -1).connect("d.expression", -1, a.zzclone());
	add.N("d.1").setText("3");
	
	int result = Data.i(evaluateExpression(main, sfcell));
	assertEquals(result, 36);
    }

    /** Test a := 1; b := 0; while (a<=100) { print(a); b := b + a; a := a + 1; }
     */
    public void testSimpleLoop() {
	Expression e = new Expression(sp, "block");
	
	Expression seta = e.create("d.2", 1, ":=");
	ZZCell a = seta.getParam("d.1", -1);
	Namespace.makeVariable(a);
	seta.set("d.1", 1, 1);
	
	Expression setb = e.create("d.2", 2, ":=");
	ZZCell b = setb.getParam("d.1", -1);
	Namespace.makeVariable(b);
	setb.set("d.1", 1, 0);
	
	Expression loop = e.create("d.2", 3, "while");

	Expression cond = loop.create("d.1", -1, "<=");
	cond.create("d.1", -1, a);
	cond.set("d.1", 1, 100);
	
	Expression body = loop.create("d.1", 1, "block");
	
	// Expression print = body.create("d.2", 1, "print");
	// print.create("d.1", 1, a);
	
	Expression incb = body.create("d.2", 2, ":=");
	incb.main.insert("d.1", -1, b.zzclone());
	Expression addb = incb.create("d.1", 1, "+");
	addb.create("d.1", -1, b);
	addb.create("d.1", 1, a);
	
	Expression inca = body.create("d.2", 3, ":=");
	inca.main.insert("d.1", -1, a.zzclone());
	Expression adda = inca.create("d.1", 1, "+");
	adda.create("d.1", -1, a);
	adda.set("d.1", 1, 1);
	
	Namespace context = new Namespace(sp);
	Archimedes.evaluateExpression(e, context);
	
	assertTrue(context.getint(a) == 101);
	assertTrue(context.getint(b) == 5050);
    }
}
