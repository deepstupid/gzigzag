/*   
TestCallExpression.java
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

/** Test case for the clasm CallExpression class.
 */

public class TestCallExpression extends ClasmTestCase {
public static final String rcsid = "$Id: TestCallExpression.java,v 1.6 2001/07/30 17:54:04 tjl Exp $";

    public TestCallExpression(String s) { super(s); }
    
    HashMap stackframe = new HashMap();

    public void setUp() {
        super.setUp();
    }

    public void testCallExpression() throws ClasmException {
	CallExpression ex = new CallExpression(prim.add,
					       arg("7", "9"));
	assertEquals(16, Callable.asInt(ex.eval()));

	CallExpression ex2 = new CallExpression(prim.add,
						arg(ex, ex));

	assertEquals(32, Callable.asInt(ex2.eval()));
    }

    public void testParsed() throws Exception {
	space.setJavaObject(c1.N(dprim), prim.add);
	c1.N(d2).setText("7");
	c1.N(d2).setText("9");

	space.setJavaObject(c2.N(dprim), prim.add);
	c2.N(d2).setText("14");
	c2.N(d2).connect(d1, c1);

	assertEquals(16, Callable.asInt(new CallExpression(c1, stackframe)));
	assertEquals(30, Callable.asInt(new CallExpression(c2, stackframe)));
    }

    public void testVariables() throws Exception {
	Variable v1 = new Variable("20"), v2 = new Variable();
	stackframe.put(c1.getRootclone(), v1);
	stackframe.put(c2.getRootclone(), v2);

	space.setJavaObject(c3.N(dprim), prim.add);
	c3.insert(d2, 1, c1.zzclone().N(d1, -1));
	c3.N(d2).setText("12");

	Cell set = c2.zzclone();
	set.N(d2).connect(d1, c3);

	Cell read = c2.zzclone();

	assertEquals(32, Callable.asInt(new CallExpression(set, stackframe)));
        assertEquals(32, Callable.asInt(new CallExpression(read, stackframe)));
    }

}




