/*   
Function.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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

package org.gzigzag.client;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import java.util.*;

/** A function object that caches its results.
 */

public class CachedFunction implements Function {

    Function f;

    HashMap cache = new HashMap();

    class Entry implements Obs {
	Object key;
	Object value;
	List observers;
	void addObs(Obs o) { 
	    if(observers == null) observers = new ArrayList();
	    observers.add(o); 
	}
	public void chg() {
	    cache.remove(key);
	}
    }

    public CachedFunction(Function f) {
	this.f = f;
    }

    public Object apply(Stepper s) {
	Object key = s.getImmutable();
	Entry value = (Entry)cache.get(key);
	if(value == null) {
	    value = new Entry();
	    value.key = key;
	    // XXX Ugly -- we know the immutable thing is the cell...
	    Stepper observingStepper = 
		new DirectStepper((Cell)s.getImmutable(), value);
	    value.value = f.apply(observingStepper);
	    cache.put(key, value);
	}
	if(s instanceof DirectStepper) {
	    Obs o = ((DirectStepper)s).getObs();
	    value.addObs(o);
	}
	return value.value;
    }
}
