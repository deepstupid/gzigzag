/*   
AwtSetRelation.java
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


class AwtSetRelation extends AwtRelation{
    public static boolean dbg = false;

    static ZZCell make(ZZCell from, ZZCell to) {
        return make(from, to, AwtDim.d_layerset, AwtDim.d_member);
    }

    static ZZCell next(ZZCell c) {
	return next(c, AwtDim.d_layerset);
    }

    static ZZCell last(ZZCell c) {
	return last(c, AwtDim.d_layerset);
    }

    static ZZCell source(ZZCell c) {
	return source(c, AwtDim.d_layerset);
    }

    static ZZCell target(ZZCell c) { 
        return target(c, AwtDim.d_member);
    }
}

