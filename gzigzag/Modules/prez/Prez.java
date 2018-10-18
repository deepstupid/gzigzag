/*   
Prez.java
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
import java.awt.*;
import java.util.*;
import java.io.*;

/** A simple presentation module.
 */

public class Prez {
public static final String rcsid = "$Id: Prez.java,v 1.4 2000/09/19 10:32:03 ajk Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    static public ZZModule module = new ZZModule() {
    public void action(String id,
	    ZZCell code, 
	    ZZCell target,
	    ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
    }
    public ZOb newZOb(String id) { 
	if(id.equals("R"))
	    return new PrezRaster();
	
	return null; 
    }
    };
}
