/*   
Zaubertrank.java
 *    
 *    Copyright (c) 2000, Benjamin Fallenstein
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
package org.gzigzag.module;
import org.gzigzag.*;
import java.awt.*;

/** The module for the Zaubertrank applitude.
 */

public class Zaubertrank {
public static final String rcsid = "$Id: Zaubertrank.java,v 1.1 2001/04/02 20:29:05 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    static public ZZModule module = new ZZModule() {
    public void action(String id,
	    ZZCell code, 
	    ZZCell target,
	    ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
	Flob[] flob = new Flob[1];
	if(pt != null) ((FlobSet)xi).getObjectAt(pt.x, pt.y, flob);
	org.zaubertrank.Actions.action(view.getViewcell(), id, flob[0]);
    }
    public ZOb newZOb(String id) { 
	if(id.equals("Simple"))
	    return new org.zaubertrank.SimpleView();
	else if(id.equals("Parsed"))
	    return new org.zaubertrank.ParsedView();
	
	return null; 
    }
    };
}
