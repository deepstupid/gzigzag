/*
AbstractFlatHashMap.java
 *
 *    Copyright (c) 2001, Tuomas Lukka
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
import java.util.*;

/** A flat hash class.
 * Because of the Java overheads associated with objects,
 * we can afford to make a flat hashtable (which does NOT have
 * the overhead of an object per entry) large enough to more
 * than cover the difference. This is especially so on Kaffe,
 * where the overhead associated with object creation is considerable.
 * <p>
 * However, there is one considerable downside: Java always checks
 * the array bounds on array access, which means that while creating
 * a new entry is fast, putting a new value into an existing entry
 * or accessing entries can actually be slower (and is, according
 * to benchmarks).
 */

public abstract class AbstractFlatHashMap extends 
	org.gzigzag.util.AbstractHashMap {

    int INITIAL_CAPACITY = 5501;

    int size = INITIAL_CAPACITY;
    int entries = 0;
    int entryLimit = INITIAL_CAPACITY / 3;

    Object[] keys = new Object[INITIAL_CAPACITY];
    Object[] values = new Object[INITIAL_CAPACITY];

    boolean resizing = false;

    protected void resize(int nsize, int nentrylimit) {
	if(resizing)
	    throw new Error("Resize while resizing: HELP!");
	resizing = true;
	Object[] nkeys = keys;
	Object[] nvalues = values;
	entryLimit = nentrylimit;
	keys = new Object[nsize];
	values = new Object[nsize];
	for(int i=nkeys.length-1; i>=0; i--)
	    if(keys[i] != null)
		put(keys[i], values[i]);
	this.size = nsize;
	resizing = false;
    }

    public int size() {
	return entries;
    }

    public Set keySet() {
	throw new Error("Not implemented");
    }

    

}
