/*   
DummyStreamSet.java
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
 * Written by Benjamin Fallenstein
 */

package org.gzigzag;

import java.awt.*;
import java.applet.*;
import java.io.*;
import java.util.*;

/** A dummy stream set for ZZ spaces.
 * Save fails, mostly for applets and testing.
 */

public class DummyStreamSet extends StreamSet {
public static final String rcsid = "$Id: DummyStreamSet.java,v 1.2 2001/04/04 18:09:19 tjl Exp $";

    public InputStream getInputStream(String id) { return null; }
    public OutputStream getAppendStream(String id) { return null; }
    public boolean exists(String id) { return false; }

    Hashtable ws = new Hashtable();
    public Writable getWritable(String id) {
	Writable res = (Writable)ws.get(id);
	if(res == null) {
	    res = new MemWritable();
	    ws.put(id, res);
	}
	return res;
    }
}

