/*   
TestClang1.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka
 */
package org.gzigzag.module;
import org.gzigzag.*;
import org.gzigzag.clang.*;
import java.util.*;
import java.awt.*;

class ELIFJSEF {
}

/** A small tester for Clang.
 */


public class TestClang1  {
public static final String rcsid = "$Id: TestClang1.java,v 1.9 2000/10/18 14:35:32 tjl Exp $";
	public static final boolean dbg = true;
	static final void p(String s) { if(dbg) System.out.println(s); }
	static final void pa(String s) { System.out.println(s); }

	static public ZZModule module = new ZZModule() {
		public void action(String id,
                                   ZZCell code, 
                                   ZZCell target,
                                   ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
                    ZZCell viewCursor = ZZCursorReal.get(view.getViewcell());
                    p("TESTCLANG ACTION!");
                    if(id.equals("1")) {
                        test1(viewCursor);
                    } else {
                        pa("UNKNOWN TESTCLANG1 COMMAND "+id);
                    }
                    p("TESTCLANG ACTION RETURNS!");
		}
	};

	static void test1(ZZCell curs) {

		ZZClang1 clang = new ZZClang1();
		ZZClangOp[] ops = ZZClangOps.getOps1();
		for(int i = 0; i < ops.length; i++) 
			clang.addOp(ops[i]);

		ZZCell main = curs.N("d.2", 1);
		// Goal of script: connect these two.
		ZZCell c1 = main.N("d.1", 1);
		c1.setText("c1");
		ZZCell c2 = c1.N("d.1", 1);
		c1.setText("c2");

		ZZCell func = main.N("d.2", 1);
		func = func.N("d.1", 1);
		func.setText("func");
		ZZCell par = func.N("d.1", 1);
		par.setText("c");
		ZZCell cmd = func.N("d.2", 1);
		cmd.setText("connect");
		ZZCell param = cmd.N("d.1", 1);
		par.insert("d.clone", 1, param);
		param = param.N("d.1", 1);
		param.setText("d.2");
		param = param.N("d.1", 1);
		par.insert("d.clone", 1, param);
		param = param.N("d.2", 1);
		param.setText("step");
		param = param.N("d.1", 1);
		param.setText("d.1");
		param = param.N("d.1", 1);
		param.setText("1");

		p("TESTCLANG CLANGEXEC!");

		// clang.exec(func, null, new ZZCell[] { c1 });


	}

}

