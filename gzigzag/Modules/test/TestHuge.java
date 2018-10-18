/*   
TestHuge.java
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
import java.util.*;
import java.awt.*;

public class TestHuge {
public static final String rcsid = "$Id: TestHuge.java,v 1.4 2000/10/18 14:35:32 tjl Exp $";

    static public ZZModule module = new ZZModule() {
	public void action(String id, ZZCell code, 
			   ZZCell target, 
			   ZZView v, ZZView cv,
			   String key, Point pt, 
			   ZZScene xi) {
	    if(id.equals("ADDTREE")) {
		ZZCell h = target.getHomeCell();
		h = h.h("d.1", 1);
		ZZCell n = null;
		int nth = 1;
		for(; h != null; h=h.s("d.2", 1)) {
		    ZZCell m = h.N("d.1", 1);
		    m.setText(""+(nth++));
		    if(n != null) n.connect("d.2", 1, m);
		    n = m.N("d.2", 1);
		    n.setText(""+(nth++));
		}
	    }
	}
    };
}
