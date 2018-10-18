/*   
TestEarlyTermination.java
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

/** Test Archimedes' early terminations.
 *  @see org.gzigzag.clang.EarlyTermination
 */

public class TestEarlyTermination extends TestCase {
public static final String rcsid = "$Id: TestEarlyTermination.java,v 1.4 2001/06/16 09:52:05 tjl Exp $";

    private static final void pa(String s) { System.out.println(s); }

    public TestEarlyTermination(String s) { super(s); }

    ZZDimSpace sp = new ZZDimSpace();
    ZZCell home = sp.getHomeCell();
    Namespace empty = new Namespace(sp);

    /** Currently only a very simple test, because there's no catch primitive
     *  (yet).
     */
    public void testEarlyTermination() {
	ZZCell termination = home.N();
	termination.setText("Function terminated early.");
	
	Expression add = new Expression(sp, "+");
	add.set("d.1", -1, 7);
	Expression term = add.create("d.1", 1, "terminate");
	term.set("d.1", 1, termination);
	
	Function f = new Function(sp, add);
	
	Expression e = new Expression(sp, f);
	
	boolean raised = false;
	try {
	    Archimedes.evaluateExpression(e, empty);
	} catch(EarlyTermination et) {
	    assertEquals(et.cell, termination);
	    raised = true;
	}
	
	assertTrue("No early termination raised", raised);
    }
}
