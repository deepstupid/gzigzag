/*   
AwtRelation.java
 *    
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
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
import java.awt.*;
import java.util.*;


class AwtRelation {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    static ZZCell make(ZZCell from, ZZCell to, String d_src, String d_trg) {
	ZZCell lc = from.h(d_src, 1).N(d_src);
	lc.connect(d_trg, 1, to.h(d_trg));
	return lc;
    }

    static ZZCell next(ZZCell c, String d_src) {
	return c.s(d_src);
    }

    static ZZCell last(ZZCell c, String d_src) {
	return c.h(d_src, 1);
    }

    static ZZCell source(ZZCell c, String d_src) {
	return c.h(d_src);
    }

    static ZZCell target(ZZCell c, String d_trg) {
	return c.h(d_trg, 1);
    }

    static ZZCell result(ZZCell c) {
	return c.s(AwtDim.d_result);
    }
}

