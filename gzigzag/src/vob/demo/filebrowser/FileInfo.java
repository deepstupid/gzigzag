/*   
FileInfo.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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
package org.gzigzag.vob.demo;

import java.io.*;
import java.util.*;

/** A class caching info from the filesystem.
 */

public final class FileInfo {
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) System.out.println(s); }
    protected static void pa(String s) { System.out.println(s); }

    public File f;
    public boolean isDir;
    public long lastModified, length;
    
    /** The contents of this directory (if it is one). */
    private File[] files;

    /** An empty array of files to put into <code>files</code> for non-dirs. */
    private static File[] empty = {};
    
    FileInfo(File f) {
	this.f = f;
	isDir = f.isDirectory();
	lastModified = f.lastModified();
	length = f.length();
    }

    /** Get the contents if this is a directory.
     *  If this isn't a directory, returns an empty array.
     */
    public File[] getContents() {
	if(files != null) return files;
	String[] names = f.list();
	if(names == null) {
	    files = empty;
	    return empty;
	}

	files = new File[names.length];
	for(int i=0; i<files.length; i++)
	    files[i] = new File(f, names[i]);

	return files;
    }

    private static HashMap fileinfos = new HashMap();
    
    static public FileInfo get(File f) {
	Object cached = fileinfos.get(f);
	if(cached == null) {
	    FileInfo res = new FileInfo(f);
	    fileinfos.put(f, res);
	    return res;
	}
	return (FileInfo)cached;
    }

    /** Refresh the information about the file system. */
    static public void refresh() {
	fileinfos = new HashMap();
    }
}




