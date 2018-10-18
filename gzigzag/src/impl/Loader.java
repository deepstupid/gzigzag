/*   
Loader.java
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
 * Written by Benja Fallenstein
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;
import java.util.*;
import java.io.*;

/** Static functionality to load spaces from Mediaserver IDs.

 */

public class Loader {
public static final String rcsid = "$Id: Loader.java,v 1.2 2002/03/10 01:16:23 bfallenstein Exp $";
    static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }

    public static Space load(Mediaserver ms, String s) throws IOException {
	return load(ms, new Mediaserver.Id(s));
    }

    public static Space load(Mediaserver ms, Mediaserver.Id id) 
	     throws IOException {
	Space res;
	byte[] sid = id.getBytes();
	if(sid[0] != (byte)0xFF)
	    res = new PermanentSpace(ms, id);
	else {
	    // This is a fake ID refering to a hard-coded space.
	    try {
		// The bytes after byte 0 form the fully qualified name of
		// a class that implements Space and has a no-arg constructor.
		Class cl = Class.forName(new String(sid, 1, sid.length-1));
		res = (Space)cl.newInstance();
	    } catch(Exception e) {
		e.printStackTrace();
		throw new ZZError("Exception when loading hard-coded space: "
				  + e + ". Printed stack trace.");
	    }
	}
	return res;
    }
}
