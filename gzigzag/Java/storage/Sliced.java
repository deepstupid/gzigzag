/*   
Sliced.java
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
import java.util.*;

class FEIHFSEHFSHFSMEKFMESKUFMSEF { }

/** EXPERIMENTAL, NOT FUNCTIONAL: A dimension created by splicing 
 * together several other
 * dimensions. Inter-dimension connections are stored locally and
 * in preflets.
 * <p>
 * This class extends ZZLocalDimension, which is used for caching 
 * accesses.
 * <p>
 * What makes things easier here is that the slice definition guarantees
 * that all ranks that do not come from s.0 remain untouched. All alterations
 * are insertions of ranks from the other slices
 * in the middle of ranks from s.0 (of course, removing preflets and the like)
 */

/*
public class Sliced extends ZZDimension {
public static final String rcsid = "$Id: Sliced.java,v 1.8 2000/11/07 23:07:34 tjl Exp $";

    ZZDimension[] slices;

    ZZSlicedDimSpace sds;

    Sliced(ZZSlicedDimSpace ss, ZZDimension[] s) {
	sds = ss;
	slices = s;
    }

    public String s(String c, int steps, ZZObs o) {
	String oid = sds.getOrigID(c);
	int sli = sds.getSliceID(c);
	ZZDimension slice = slices[sli];
	String ores = slice.s(oid, steps, o);
	if(ores==null) return null;
	return sds.getConvID(sli, ores);
    }
    public String h(String c, int dir, ZZObs o) {
	String oid = sds.getOrigID(c);
	int sli = sds.getSliceID(c);
	ZZDimension slice = slices[sli];
	String ores = slice.h(oid, dir, o);
	if(ores==null) return null;
	return sds.getConvID(sli, ores);
    }
    public void connect(String c, String d) {
	int ci = sds.getSliceID(c);
	int di = sds.getSliceID(d);
	// Silently fail
	if(sds.getSliceID(c) != sds.getSliceID(d))
	    return;
	String cor = sds.getOrigID(c);
	String dor = sds.getOrigID(d);
	slices[ci].connect(cor, dor);
    }
    public void disconnect(String c, int dir) {
	String oid = sds.getOrigID(c);
	int sli = sds.getSliceID(c);
	slices[sli].disconnect(oid, dir);
    }
}
*/
