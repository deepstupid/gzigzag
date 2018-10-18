/*   
TestClasmPrimitiveSet1.java
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
import org.gzigzag.mediaserver.Mediaserver;
import junit.framework.*;

/** Test case for clasm 
 */

public class TestClasmPrimitiveSet1 extends ClasmTestCase {
public static final String rcsid = "$Id: TestClasmPrimitiveSet1.java,v 1.5 2002/03/10 01:16:23 bfallenstein Exp $";

    public TestClasmPrimitiveSet1(String s) { super(s); }

    ClasmPrimitiveSet1 set = new ClasmPrimitiveSet1();
    
    public void setUp() {
        super.setUp();
    }

    public void testClassName() throws ClasmException {
        assertEquals("org.gzigzag.impl.clasm.ClasmPrimitiveSet1",
                     set.getClass().getName());
        Mediaserver.Id id = set.getLastId();
        byte[] bytes = id.getBytes();
        assertEquals((byte)0xFF, bytes[0]);
        assertEquals("org.gzigzag.impl.clasm.ClasmPrimitiveSet1",
                     new String(bytes, 1, bytes.length-1));
    }

    public void testInclusion() throws ClasmException, java.io.IOException {
        String prefix = "home-id:" + set.getLastId().getString() + "-";

        PermanentSpace s = new PermanentSpace(TestImpl.zms);
        Cell c = s.N();
        s.include(c, set);

        Cell cadd = s.getIncludedCell(c, Id.space.getCell(prefix + "add"));
        FunctionalPrimitive padd = (FunctionalPrimitive)cadd.getJavaObject();
        assertNotNull("getJavaObject() was null", padd);
    }
}



