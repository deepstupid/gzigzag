/*   
ClasmTestCase.java
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
import java.io.*;

/** A superclass conviniently initializing stuff for Clasm test cases.
 */

public abstract class ClasmTestCase extends TestCase {
public static final String rcsid = "$Id: ClasmTestCase.java,v 1.12 2001/07/16 17:55:33 bfallenstein Exp $";

    protected boolean dbg = true;
    protected void p(String s) { if(dbg) System.out.println(s); }

    static public Object[] arg() { return Callable.noparam; }
    static public Object[] arg(Object o) { return new Object[] { o }; }
    static public Object[] arg(Object o1, Object o2) { 
	return new Object[] { o1, o2 }; 
    }
    static public Object[] arg(Object o1, Object o2, Object o3) {
        return new Object[] { o1, o2, o3 };
    }
    static public Object[] arg(Object o1, Object o2, Object o3, Object o4) {
        return new Object[] { o1, o2, o3, o4 };
    }

    static public ClasmTestPrimitives prim = new ClasmTestPrimitives();


    public ClasmTestCase(String s) { super(s); }

    ClasmTestSpace space;

    /** d.1, d.2, d.function-definition, d.primitive-binding. */
    Dim d1, d2, ddef, dprim;

    /** Cells for arbitrary use. */
    Cell c1, c2, c3, c4, c5, c6;

    public void setUp() {
	space = new ClasmTestSpace();

	d1 = space.getDim(ClasmDims.d_call);
	d2 = space.getDim(ClasmDims.d_params);
	ddef = space.getDim(ClasmDims.d_def);
	dprim = space.getDim(ClasmDims.d_prim);

	c1 = space.N(); c2 = space.N(); c3 = space.N();
	c4 = space.N(); c5 = space.N(); c6 = space.N();
	
	c1.setText("c1"); c2.setText("c2"); c3.setText("c3");
	c4.setText("c4"); c5.setText("c5"); c6.setText("c6");
    }

}




