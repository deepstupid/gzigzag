/*   
TestVariable.java
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

/** Test case for the clasm Variable class. 
 */

public class TestVariable extends ClasmTestCase {
public static final String rcsid = "$Id: TestVariable.java,v 1.1 2001/07/02 16:38:06 bfallenstein Exp $";

    public TestVariable(String s) { super(s); }
    
    Variable var;

    public void setUp() {
	super.setUp();
	var = new Variable();
    }

    public void testVariable() throws ClasmException {
	assertEquals(null, var.call(new Object[] {}));
	assertEquals(7, Callable.asInt(var.call(new Object[] { "7" })));
	assertEquals(7, Callable.asInt(var.call(new Object[] {})));
        assertEquals(9, Callable.asInt(var.call(new Object[] { "9" })));
        assertEquals(9, Callable.asInt(var.call(new Object[] {})));
    }

}




