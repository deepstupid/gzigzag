/*   
LoopDetector.java
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

/** A loop detector.
 * Uses a simple constant-space algorithm that will detect any loop before 
 * it's cycled through 3 times, except very small loops.  
 * Example use:
 * <pre>
 * 	LoopDetector l = new LoopDetector();
 *	while((x = iter(x)) != null) {
 *		l.detect(x);
 *		// ... do something with x.
 *	}
 * </pre>
 * Note: this only works for deterministic loops that always have the same
 * elements and length.
 * <p>
 * The Object.equals method is used for the comparisons.
 */

public final class LoopDetector {
public static final String rcsid = "$Id: LoopDetector.java,v 1.6 2000/11/02 13:03:08 tjl Exp $";
    Object obj = this;
    int count = 0;
    int nth = 10;
    public void reset() { count = 0; obj = null; }
    /** Throws an error if the parameter is a part of a looping sequence.
     */
    public void detect(Object o) {
	if(isLooping(o))
		throw new ZZInfiniteLoop("Infinite loop detected");
    }
    /** Returns true if the parameter is a part of a looping sequence.
     */
    public boolean isLooping(Object o) {
	if(o.equals(obj)) return true;
	if(count-- < 0) {
	    nth *= 2;
	    count = nth;
	    obj = o;
	}
	return false;
    }
}
