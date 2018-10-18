/*   
CopyDim.java
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
 * Written by Tuukka Hastrup
 */
package org.gzigzag.transform;
import java.util.*;
import org.gzigzag.*;

/** ZZspace dimension copier - duplicate connections of a dimension into
 * another dimension
 */

public class CopyDim {
String rcsid = "$Id: CopyDim.java,v 1.4 2000/10/18 14:35:32 tjl Exp $";
    String olddim, newdim;
    ZZSpace space;
/** Constructs dimension changer for a space
 *  @param od		The old dimension
 *  @param nd		The new dimension
 *  @param s		The space to convert
 */
    public CopyDim(String od, String nd, ZZSpace s) {
	olddim = od;
	newdim = nd;
	space = s;
    }

/** Performs the actual transformation. All connections along the old
 *  dimension are recreated along the new dimensions.
 */
    public void transform() {
	Enumeration e=space.cells();
	ZZCell c, n;
	while(e.hasMoreElements()) {
	    c = (ZZCell)e.nextElement();
	    n = c.s(olddim, 1);
	    if(n!=null) {
	        c.connect(newdim, 1, n);
	    }
	}
    }
}
