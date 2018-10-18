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
package org.gzigzag.util;
import org.gzigzag.*;
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
 * elements and length. Nothing is guaranteed if the content of the loop 
 * varies even slightly. However, it is guaranteed that the loop is
 * detected once the loop has settled into a constant loop.
 * <p>
 * The Object.equals method is used for the comparisons.
 */

public final class LoopDetector {
public static final String rcsid = "$Id: LoopDetector.java,v 1.3 2001/10/02 17:18:52 tjl Exp $";
    Object obj = null;
    int count = 0;
    int nth = 10;
    public void reset() { nth = 10; count = 0; obj = null; }
    /** Throws an exception if the parameter is a part of a looping sequence.
     */
    public void detect(Object o) throws InfiniteLoopException {
	if(isLooping(o))
		throw new InfiniteLoopException("Infinite loop detected: "+o+" Obj: "+obj+" count: "+count+" "+nth);
    }

    /** Throws an error if bla bla bla.
     */
    public void detectError(Object o) {
	if(isLooping(o))
	    throw new Error("Infinite loop detected: "+o+" Obj: "+obj+" count: "+count+" "+nth);
    }
    

    /** Returns true if the parameter is a part of a looping sequence.
     */
    public boolean isLooping(Object o) {
	if(obj != null && o.equals(obj)) return true;
	if(count-- < 0 || obj == null) {
	    nth *= 2;
	    count = nth;
	    obj = o;
	}
	return false;
    }
}
