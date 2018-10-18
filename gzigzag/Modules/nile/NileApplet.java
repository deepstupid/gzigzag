/*   
NileApplet.java
 *    
 *    Copyright (c) 1999, Ted Nelson and Tuomas Lukka
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
 * Written by Benjamin Fallenstein
 */

package org.gzigzag;

import java.awt.*;
import java.applet.*;
import java.io.*;
import java.util.*;

/** An applet showing the split Nile view.
 * This just takes a lot of code from ZZApplet -- bad style. Well, this is
 * just an intermediate: When we start making real demos using the Dump
 * stuff, ZZApplet probably needs to be rewritten, and NileApplet will be
 * totally obsolete.
 */

public class NileApplet extends Applet {
    public static final String rcsid = "$Id: NileApplet.java,v 1.4 2001/01/03 16:47:50 raulir Exp $";
    
    public void init() {
	ZZSpace space = new ZZCacheDimSpace(new DummyStreamSet());
	ZZCell home=space.getHomeCell();
	home.N("d.1").setText("First text");
	home.N("d.1").setText("Second text");

	ZZDefaultSpace.create(home);
	setLayout(new BorderLayout());
	ZZWindows.init(space,this);

	ZZCell topl = ZZWindows.startCell().s("d.1", 1)
			.s("d.2", 1);
	ZZCell a = topl.s("d.1", 2);
	ZZCell b = topl.s("d.2", 1).s("d.1", 1);
	ZZCursorReal.set(a, home.s("d.1", 1));
	ZZCursorReal.set(b, home.s("d.1", 2));
	ZZCursorReal.setColor(a,Color.red);
	ZZCursorReal.setColor(b,Color.yellow);
	org.gzigzag.module.SplitNileDemo.updateSpace(a, a, b);

	ZZObsTrigger.runObsQueue();
    }
}
