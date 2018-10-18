/*   
AwtOpOR.java
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

class AwtOpOR implements AwtOp {
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }


    public ZZCell execute(ZZCell c) {
	ZZCell newSet, sl, trg;
	ZZCell[] sets = {AwtLinkRelation.source(c), AwtLinkRelation.target(c)};
	
	newSet = AwtLayer.createNewResultArtefact(c);

        for(int i = 0; i<2; i++) { 
            sl = AwtSetRelation.next(sets[i]);
            for(; sl!=null; sl = AwtSetRelation.next(sl)) {
                trg = AwtSetRelation.target(sl);
                AwtLayer.addArtefact(newSet, trg);
            }
        }

	AwtUtil.launchView(newSet);
        
        return newSet;
    }
}
    



