/*   
AwtOperators.java
 *    
 *    Copyright (c) 2000-2001 Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2000-2001 Benjamin Fallenstein
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
 * Written by Kimmo Wideroos
 */

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;

public class AwtOperators {
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    static public Hashtable bOperators = new Hashtable();
    
    static Hashtable operators = new Hashtable(); 

    static {
        bOperators.put("AND", (Object)new AwtOpAND());
        bOperators.put("OR", (Object)new AwtOpOR());
        //bOperators.put("XOR", (Object)new AwtOpXOR());
    }

    static AwtOp get(String op) {
	return (AwtOp)bOperators.get(op);
    }
}
    



