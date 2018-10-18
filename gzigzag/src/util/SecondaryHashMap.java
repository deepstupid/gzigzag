/*
SecondaryHashMap.java
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

public class SecondaryHashMap extends AbstractFlatHashMap {

    public Object get(Object key) {

	// The next part is the code in GZigZag that is run
	// most frequently. It is VITAL that it remain very fast;
	// if you need to change it make sure you really really
	// know what you're doing --Tjl

	int hash = key.hashCode();
	int firstHash = hash % size;
	if(firstHash < 0) firstHash = -firstHash; // Hash codes can be negative
	Object curKey = keys[firstHash];
	if(curKey == null) return null;
	if(curKey.equals(key)) return values[firstHash];

	// This is the end of the hottest code path. We can relax now.
	// We don't calculate the second hash code before this or anything
	// else because we just don't have time.

	// Not really secondary hashing, simply linear chaining.
	do {
	    firstHash ++; firstHash %= size;
	    curKey = keys[firstHash];
	    if(curKey == null) return null;
	    if(curKey.equals(key))
		return values[firstHash];
	} while(true);

    }

    void extend() {
	resize(size * 3 + 1, size);
    }

    public Object put(Object key, Object value) {

	if(value == null) remove(key);

	// Again, the first code path is very hot.

	int hash = key.hashCode();
	int firstHash = hash % size;
	if(firstHash < 0) firstHash = -firstHash;
	Object curKey = keys[firstHash];
	if(curKey == null) {
	    keys[firstHash] = key;
	    values[firstHash] = value;
	    if(entries++ > entryLimit)
		extend();
	    return null;
	}
	if(curKey.equals(key)) {
	    //keys[firstHash] = key; // ??? XXX Maybe not needed
	    Object prev = values[firstHash];
	    values[firstHash] = value;
	    return prev;
	}

	// Now, relax again.
	
	do {
	    firstHash ++; firstHash %= size;
	    curKey = keys[firstHash];
	    if(curKey == null) {
		keys[firstHash] = key;
		values[firstHash] = value;
		if(entries++ > entryLimit)
		    extend();
		return null;
	    }
	    if(curKey.equals(key)) {
		//keys[firstHash] = key;
		Object prev = values[firstHash];
		values[firstHash] = value;
		return prev;
	    }
	} while(true);

    }

    public Object remove(Object key) {
	// This is horribly tricky to get right.
	// We have to go through the whole chain.
	return null;
    }
}
