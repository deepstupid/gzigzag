/*   
TestCloning.java
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

package org.gzigzag.impl;
import org.gzigzag.*;
import junit.framework.*;

/** Test zzclone() and getRootclone(), as well as getDim for clones.
 */

public class TestCloning extends TestCase {
public static final String rcsid = "$Id: TestCloning.java,v 1.2 2001/07/08 23:21:19 bfallenstein Exp $";

    public TestCloning(String s) { super(s); }
    
    public void testCloning() {
	Space s = new SimpleTransientSpace();

	Cell root = s.N(); root.setText("root");
	assertEquals(root, root.getRootclone());

	Cell c1 = root.zzclone(); c1.setText("c1");
	assertEquals(root, root.getRootclone());
	assertEquals(root, c1.getRootclone());

	Cell c2 = root.zzclone(); c2.setText("c2");
	Cell c3 = c1.zzclone(); c3.setText("c3");
        assertEquals(root, root.getRootclone());
        assertEquals(root, c1.getRootclone());
        assertEquals(root, c3.getRootclone());
        assertEquals(root, c3.getRootclone());
    }

    public void testClonedDims() throws Exception {
	Space s = new SimpleTransientSpace();
	
	Cell dim = s.N();
	Cell c1 = s.N(), c2 = c1.N(dim);
	Cell dim2 = dim.zzclone();
	assertEquals(c2, c1.s(dim2));
    }
}




