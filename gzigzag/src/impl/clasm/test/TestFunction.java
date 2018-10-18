/*   
TestFunction.java
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

package org.gzigzag.impl.clasm;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import java.util.HashMap;
import junit.framework.*;

/** Test case for the clasm Function class.
 */

public class TestFunction extends ClasmTestCase {
public static final String rcsid = "$Id: TestFunction.java,v 1.8 2001/07/30 17:54:04 tjl Exp $";

    { dbg = false; }

    public TestFunction(String s) { super(s); }
    
    public void setUp() {
        super.setUp();
    }

    public void testSimpleFunction() throws Exception {
	space.setJavaObject(c1.N(dprim), prim.add);

	Cell fun = space.N();
	Cell par = fun.N(d2);
	Cell body = c1.zzclone();
	fun.connect(ddef, 1, body);

	body.N(d2).setText("1");
	body.N(d2).connect(d1, 1, par.zzclone());

	Function inc = new Function(fun);
	
	assertEquals(7, Callable.asInt(inc.call(arg("6"))));
	assertEquals(900, Callable.asInt(inc.call(arg("899"))));
    }

    /** Test a higher-order function and callback.
     *  Create a function incrementing a variable it is given,
     *  then create a function executing a callback three times, then
     *  increment a variable three times.
     */
    public void testIncFunctionAndCallback() throws Exception {
        space.setJavaObject(c1.N(dprim), prim.add);

        Cell fun = space.N();
        Cell par = fun.N(d2);
	Cell body = fun.N(ddef);
	body.connect(d1, 1, par.zzclone());

	Cell add = c1.zzclone();
	body.N(d2).connect(d1, 1, add);

	add.N(d2).setText("1");
	add.N(d2).connect(d1, 1, par.zzclone().N(d1, -1));

        Function incvar = new Function(fun);
	Cell incvar_fun = fun;

	Variable var = new Variable("7");
	incvar.call(arg(new ConstantExpression(var)));
	assertEquals(8, Callable.asInt(var.call(arg())));


	// now for the three-times callback function

	fun = space.N();
	par = fun.N(d2).N(d1); // a callback parameter
	body = c2.zzclone();
	fun.connect(ddef, 1, body);
	space.setJavaObject(c2.N(dprim), prim.block);
	
	for(int i=0; i<3; i++)
	    body.N(d2).connect(d1, 1, par.zzclone().N(d1, -1));

	Function thrice = new Function(fun);
	Cell thrice_fun = fun;
	p("thr "+thrice_fun);

	// thrice.call(arg(new CallExpression(new Primitive() { public void exec() { System.out.println("Hello world!"); } }, arg())));

	// now the expression calling that
	
	Cell exp = thrice_fun.zzclone();
	Cell inc_exp = incvar_fun.zzclone();
	exp.N(d2).connect(d1, 1, inc_exp);
	inc_exp.connect(d2, 1, c5.zzclone());

	HashMap stackframe = new HashMap();
	stackframe.put(c5, var);

	p(exp+", "+exp.getRootclone()+", "+thrice_fun);
	new CallExpression(exp, stackframe).eval();

        assertEquals(11, Callable.asInt(var.call(arg())));
    }

    /** Assign 2 to a local var, add to itself, and return. */
    public void testLocalVariables() throws Exception {
        space.setJavaObject(c1.N(dprim), prim.add);
	space.setJavaObject(c2.N(dprim), prim.block);

        Cell fun = space.N();
        Cell var = fun.N(d1).N(d2);
        Cell body = c2.zzclone();
	fun.connect(ddef, 1, body);

	Cell varset = var.zzclone();
	body.N(d2).connect(d1, 1, varset);
	varset.N(d2).setText("2");

        Cell add = c1.zzclone();
        body.s(d2).N(d2).connect(d1, 1, add);
	add.insert(d2, 1, var.zzclone().N(d1, -1));
	add.insert(d2, 1, var.zzclone().N(d1, -1));

        Function four = new Function(fun);

        assertEquals(4, Callable.asInt(four.call(arg())));
    }

}




