/*   
SlicedSame.java
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
package org.gzigzag;
import org.gzigzag.*;
import java.util.*;

/** A simple dimension to connect cells with the same ID in sliced
 * spaces together.
 */

class ELIJSEFIEHSFSHFSEF { }

/*
public class SlicedSame extends ZZRODimension {
    ZZSlicedDimSpace ss;
    SlicedSame(ZZSlicedDimSpace ss) {
	this.ss = ss;
    }

    public String s(String c, int steps, ZZObs o) {
	int si = ss.getSliceID(c);
	String s = ss.getOrigID(c);
	si += steps;
	if(si < 1 || si >= ss.slices.length)
	    return null;
	if(ss.slices[si].getCellByID(s) == null) return null;
	return ss.getConvID(si, s);
    }
}
*/
