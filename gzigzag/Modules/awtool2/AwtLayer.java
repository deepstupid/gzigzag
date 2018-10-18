/*   
AwtLink.java
 *    
 *    Copyright (c) 2000-2001 Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2000-2001 Kimmo Wideroos
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
 * Written by Kimmo Wideroos (inspired by Benjamin Fallenstein's notemap module)
 */

/** an abstract class for layers
*/

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

public class AwtLayer implements AwtAccursable {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    private ZZCell cell = null;

    public AwtLayer(ZZCell c) { cell=c; } 

    public ZZCell getCell() { return cell; }

    public static boolean valid(ZZCell c) { 
	if(!c.h(AwtDim.d_artefact).equals(c)) return true;
	return false;
    }

    // add target 'artefact' to 'layerset' 
    public static void addArtefact(ZZCell layerset, ZZCell artefact) {
	ZZCell ls = layerset.h(AwtDim.d_layerset, 1).N(AwtDim.d_layerset);
	ls.connect(AwtDim.d_member, 1, artefact);
    }

    public static ZZCell createNewArtefact(ZZCell layerset) {
	ZZCell ac = layerset.h(AwtDim.d_artefact).N(AwtDim.d_artefact).
	    zzclone();
	ZZCell ls = layerset.h(AwtDim.d_layerset, 1).N(AwtDim.d_layerset);
	ls.connect(AwtDim.d_member, 1, ac);
	return ac;
    }

    public static ZZCell createNewResultArtefact(ZZCell op) {
	return op.h(AwtDim.d_result, 1).N(AwtDim.d_result);
    }

    public static ZZCell[] getArtefacts(ZZCell layerset) {
        Vector aVec = new Vector();
        ZZCell trg;

	for(ZZCell ll = AwtSetRelation.next(layerset); ll!=null; ll = AwtSetRelation.next(ll)) {
	    trg = AwtSetRelation.target(ll);
            aVec.addElement(trg);
	}

	ZZCell[] artefs = new ZZCell[aVec.size()];
	for(int i = 0; i<artefs.length; i++) {
	    artefs[i] = (ZZCell)aVec.elementAt(i);
	}
	return artefs;
    }
    
    public static ZZCell getLayer(ZZCell artef) {
	ZZCell lc=artef.s(AwtDim.d_member, -1);
	if(lc==null) return null;
	return lc.h(AwtDim.d_layerset);
    }

    // check whether 'artefact' is a member of 'layerset'
    public static boolean isMember(ZZCell layerset, ZZCell member) {
        return true ? layerset.intersect(AwtDim.d_layerset, 1, member,
                                         AwtDim.d_member, -1) != null : false;
    } 
    
}
