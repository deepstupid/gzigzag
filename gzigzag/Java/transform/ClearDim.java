/*   
ClearDim.java
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

/** ZZspace dimension clearer - disconnects every connection along a dimension
 */

public class ClearDim {
String rcsid = "$Id: ClearDim.java,v 1.4 2000/10/18 14:35:32 tjl Exp $";
    String dim; 
    ZZSpace space;
/** Constructs dimension changer for a space
 *  @param od		The old dimension
 *  @param nd		The new dimension
 *  @param s		The space to convert
 */
    public ClearDim(String d, ZZSpace s) {
	dim = d;
	space = s;
    }

/** Performs the actual transformation. All connections along the
 *  dimension are simply disconnected.
 */
    public void transform() {
	Enumeration e=space.cells();
	ZZCell c, n;
	while(e.hasMoreElements()) {
	    c = (ZZCell)e.nextElement();
	    if(c.s(dim, 1)!=null) {
	        c.disconnect(dim, 1);
	    }
	}
    }
}
