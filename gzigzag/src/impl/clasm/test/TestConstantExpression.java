/*   
TestConstantExpression.java
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
import junit.framework.*;

/** Test case for the clasm ConstantExpression class.
 */

public class TestConstantExpression extends ClasmTestCase {
public static final String rcsid = "$Id: TestConstantExpression.java,v 1.1 2001/07/02 19:01:08 bfallenstein Exp $";

    public TestConstantExpression(String s) { super(s); }
    
    public void setUp() {
        super.setUp();
    }

    public void testConstantExpression() throws ClasmException {
	Expression e1 = new ConstantExpression("xyz");
	assertEquals("xyz", e1.eval());
	assertEquals("xyz", Callable.value(e1));

	Expression e2 = new ConstantExpression(e1);
	assertEquals(e1, e2.eval());
	assertEquals(e1, Callable.value(e2));
    }

}




