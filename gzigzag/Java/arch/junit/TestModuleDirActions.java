/*   
TestModuleDirActions.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
package org.gzigzag.test;
import org.gzigzag.*;
import org.gzigzag.module.*;
import junit.framework.*;
import java.awt.*;
import java.awt.event.*;

/** A JUnit test for dir actions in modules.
 *  Dir actions are actions that take a direction as their parameter, like
 *  NEW. This class tests the new (as of 2001-04-18) functionality that allows
 *  modules to use this technique.
 *  <p>
 *  It needs the org.gzigzag.DirActionsTestModule class.
 *  @see org.gzigzag.module.DirActionsTestModule
 */

public class TestModuleDirActions extends TestCase {
public static final String rcsid = "$Id: TestModuleDirActions.java,v 1.1 2001/04/18 21:40:51 bfallenstein Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) ZZLogger.log(s); }
    protected static void pa(String s) { ZZLogger.log(s); }

    public TestModuleDirActions(String s) { super(s); }

    ZZDimSpace sp = new ZZDimSpace();
    ZZCell home = sp.getHomeCell();
    {
	ZZDefaultSpace.create(home);
    }
    ZZCell win = ZZDefaultSpace.findOnSystemlist(sp, "Windows", false)
			.s("d.1").s("d.2").s("d.1", 2);


    ZZView zzv = new ZZView() {
	public boolean reraster() { return false; }
	public void paintNow(float fract) {}
	public ZZCell getViewcell() { return win; }
    };
    {
	ZZUpdateManager.addView(zzv);
    }

    /** Set by DirActionsTestModule. */
    public static String dim;
    public static int dir;

    public static ZZExec exc = new ZZPrimitiveActions();

    public void testModuleDirActions() {
	dim = null; dir = 0;
	ZZCell c = home.N();
	
	ZZDefaultSpace.storeDirActionWaiting(home, "DirActionsTestModule.TEST");
	c.setText("CRSR X-");
	exc.execCallback(c, home, zzv, zzv, "", null, null);
	assertEquals(dim, "d.1");
	assertEquals(dir, -1);
	
	dim = null; dir = 0;
	
	exc.execCallback(c, home, zzv, zzv, "", null, null);
	assertEquals(dim, null);
	assertEquals(dir, 0);
	
	c.setText("DirActionsTestModule.STORE");
	exc.execCallback(c, home, zzv, zzv, "", null, null);
	c.setText("CRSR Z+");
	exc.execCallback(c, home, zzv, zzv, "", null, null);
	assertEquals(dim, "d.3");
	assertEquals(dir, 1);
    }
    
}
