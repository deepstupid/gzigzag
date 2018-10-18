/*   
ZZCellScroll.java
 *    
 *    Copyright (c) 1999, Ted Nelson and Tuomas Lukka
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

import java.io.*;

/** A package for representing, reading and writing huge numbers.
 * Humbers are described in Ted Nelson's Literary Machines.
 * The ZZHumber objects are immutable, like the Java String class.
 * XXX Not yet working
 */

// Immutable
public class ZZHumber {
public static final String rcsid = "$Id: ZZHumber.java,v 1.3 2000/09/19 10:32:00 ajk Exp $";
	byte[] bytes; // The raw humber bytes.
	public boolean equals(Object o) {
		ZZHumber h = (ZZHumber) o;
		if(h.bytes.length != bytes.length) return false;
		for(int i=0; i<bytes.length; i++) {
			if(h.bytes[i] != bytes[i]) return false;
		}
		return true;
	}
	byte[] getBytes() { return bytes; }

	public static ZZHumber read(DataInput d) {
		// XXX
		return null;
	}
	/** Returns a humber one greater than the current one */
	ZZHumber next() {
		// XXX
		return null;
	}
}
