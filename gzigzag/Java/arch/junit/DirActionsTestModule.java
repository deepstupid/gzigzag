/*   
DirActionsTestModule.java
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
package org.gzigzag.module;
import org.gzigzag.test.TestModuleDirActions;
import java.awt.*;
import org.gzigzag.*;

/** Helper class for TestModuleDirActions.
 *  @see org.gzigzag.test.TestModuleDirActions
 */

public class DirActionsTestModule {
public static final String rcsid = "$Id: DirActionsTestModule.java,v 1.1 2001/04/18 21:40:51 bfallenstein Exp $";

    private static final void pa(String s) { System.out.println(s); }

    public static ZZModule module = new ZZModule() {
        public void action(String id,
	    ZZCell code, 
	    ZZCell target,
	    ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
	
	    if(id.equals("STORE")) {
		ZZDefaultSpace.storeDirActionWaiting(view.getViewcell(), 
						"DirActionsTestModule.TEST");
	    }
        }

        public void dirAction(String id, ZZCell win, ZZCell accursed, 
			      String vdim, int dir, ZZCell dataWin,
			      ZZCell ctrlWin, ZZCell code) {
	    if(id.equals("TEST")) {
		TestModuleDirActions.dim = vdim;
		TestModuleDirActions.dir = dir;
	    } else
		throw new ZZError("Unknown DirActionsTestModule dirAction: "+id);
        }
    };

}
