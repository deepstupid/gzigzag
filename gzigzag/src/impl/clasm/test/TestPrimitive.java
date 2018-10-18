/*   
TestPrimitive.java
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

/** Test case for the clasm Primitive class
 */

public class TestPrimitive extends ClasmTestCase {
public static final String rcsid = "$Id: TestPrimitive.java,v 1.5 2001/08/09 15:52:30 bfallenstein Exp $";

    public TestPrimitive(String s) { super(s); }
    
    public void setUp() {
        super.setUp();
    }

    /** Assert that Callable.asXXX(null) returns null correctly.
     *  (except in the case of asInt and isTrue obviously, which don't return
     *  Objects and cannot be null)
     *  <p>
     *  Currently asCallable(null) is not tested either. I think we might
     *  want it to throw an exception, but not sure...
     */
    public void testNull() throws ClasmException {
	assertEquals(null, Callable.asString(null));
	assertEquals(null, Callable.asCell(null));
    }

    public void testAdd() throws ClasmException {
	c1.setText("7");
	Object[] args = new Object[] { c1, "5" };
	assertEquals(12, Callable.asInt(prim.add.call(args)));
    }

    public void testSubstring() throws ClasmException {
	Primitive substr = new Primitive() {
		public String exec(String s, int start, int end) {
		    return s.substring(start, end);
		}
	    };
	c1.setText("3");
	assertEquals("fi", 
		     Callable.asString(prim.substr.call(arg("stofika", c1, 
							    "5"))));
    }

}




