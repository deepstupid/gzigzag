/*   
DirStreamSet.java
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
 * Written by Tuomas Lukka, locking by Antti-Juhani Kaijanaho
 */
package org.gzigzag;
import java.util.*;
import java.io.*;

/** A directory implementation of StreamSet
 */

public class DirStreamSet extends StreamSet {
public static final String rcsid = "$Id: DirStreamSet.java,v 1.5 2000/09/18 14:28:22 tjl Exp $";
    File dir;

    public InputStream getInputStream(String id) {
	try {
	    File f = new File(dir, id);
	    if(!f.exists()) return null;
	    if(!f.canRead()) throw new ZZFatalError("Can't read file");
	    return new BufferedInputStream(
		new FileInputStream(f), 4096);
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    throw new ZZFatalError("File problem");
	}
    }

    public OutputStream getAppendStream(String id) {
	try {
	    File f = new File(dir, id);
	    return new BufferedOutputStream(
		new FileOutputStream(f.getPath(), true));
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    throw new ZZFatalError("File problem");
	}
    }
    public boolean exists(String id) {
	File fn = new File(dir, id);
	return fn.exists();
    }

    public Writable getWritable(String id) {
	if(id == null || id.equals(""))
	    throw new ZZError("Can't use writables with null or empty name");
	try {
	    File fn = new File(dir, id);
	    final RandomAccessFile f = new RandomAccessFile(fn, "rw");
	    return new FileWritable(f) ;
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    throw new ZZError(" "+e);
	}
    }

    public DirStreamSet(File dir0) {
	try {
	    dir = dir0;
	    if(dir.exists() && !dir.isDirectory()) {
		throw new ZZError("MUST BE DIRECTORY!!!! "+dir);
	    }
	    if(!dir.exists()) {
		dir.mkdirs();
	    }
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    throw new ZZError(" "+e);
	}
    }
}


