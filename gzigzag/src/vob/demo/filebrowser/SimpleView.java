/*   
SimpleView.java
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
import org.gzigzag.vob.*;

import java.io.*;

/** A simple view, showing the files as a list under each other.
 */

public class SimpleView extends FileBrowserView {
    public void build(VobPlacer into, File dir, FileVobFactory fact,
		      FileSorter sorter) {
	String[] names = dir.list();
	if(names == null)
	    throw new Error("null names-- not a directory?");
	
	File[] files = new File[names.length];
	for(int i=0; i<files.length; i++)
	    files[i] = new File(dir, names[i]);
	
	sorter.sort(files);	
	
	int y = 10;

	/** Y-Space between vobs. */
	final int yspace = 2;
	
	for(int i=0; i<names.length; i++) {
	    FileVob v = fact.put(into, files[i], 1, 10, y);
	    y += yspace + v.getHeight();
	}
    }
}

