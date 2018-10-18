/*   
AwtOpAND.java
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

class AwtOpAND implements AwtOp {
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }


    public ZZCell execute(ZZCell c) {
	ZZCell set1, set2, newSet, sl1, trg1;
	set1 = AwtLinkRelation.source(c);
	set2 = AwtLinkRelation.target(c);
	
	newSet = AwtLayer.createNewResultArtefact(c);

	sl1 = AwtSetRelation.next(set1);

	for(; sl1!=null; sl1 = AwtSetRelation.next(sl1)) {
	    trg1 = AwtSetRelation.target(sl1);
	    if(AwtLayer.isMember(trg1, set2)) {
		//sl1 is member of set1 and set2, add it into 'newSet'
		AwtLayer.addArtefact(newSet, trg1);
	    }
	}
	AwtUtil.launchView(newSet);
        
        return newSet;
    }
}
    



