/*   
SlicedHomes.java
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

/** A simple class to connect the sliced home cells together.
 * There must be a way to move between the spaces, right?
 */

class EKUEHFKUEHFMEUFHMSEFS { }

/*
public class SlicedHomes extends ZZRODimension {
public static final String rcsid = "$Id: SlicedHomes.java,v 1.7 2000/11/07 23:07:34 tjl Exp $";
    ZZSlicedDimSpace ss;

    Hashtable homeinds = new Hashtable();

    SlicedHomes(ZZSlicedDimSpace ss) {
	this.ss = ss;
	for(int i=0; i<ss.slices.length; i++) {
	    homeinds.put(
		ss.getConvID(i, ss.slices[i].getHomeCellID()),
		new Integer(i));
	}
    }

    public String s(String c, int steps, ZZObs o) {
	Integer ind = (Integer)homeinds.get(c);
	if(ind == null) return null;
	int i = ind.intValue() + steps;
	if(i < 0 || i >= ss.slices.length)
	    return null;
	return ss.getConvID(i, ss.slices[i].getHomeCellID());
    }

}
*/
