/*   
GridView.java
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

/** A simple view, showing the files as a list, but broken into rows/columns.
 *  I.e., when you reach the end of a row/column, you start a new row/col.
 */

public class GridView extends FileBrowserView {
    /** Whether to layout the files in columns.
     *  Default is rows, which is like the "big icons" view of common file
     *  browsers. Columns is like the "small icons" view (but has nothing to
     *  do with icon size, it's just what usual file browsers do in these
     *  views).
     */
    boolean columns;
    public GridView(boolean columns) { this.columns = columns; }
		
    public void build(VobPlacer into, File dir, FileVobFactory fact,
		      FileSorter sorter) {
	File[] files = FileInfo.get(dir).getContents();
	
	sorter.sort(files);	
	
	/** X- and Y-Space between vobs. */
	final int xspace = 5, yspace = 2;
	
	int y = yspace;
	int x = xspace;
	int maxw = 0;
	java.awt.Dimension size = into.getSize();

	for(int i=0; i<files.length; i++) {
	    FileVob v = fact.create(files[i]);
	    int w, h;
	    if(columns) {
		w = v.getWidth();
		h = v.getHeight();
	    } else {
		// In non-columns (rows), we just reverse w and h
		w = v.getHeight();
		h = v.getWidth();
	    }
	
	    int maxheight = columns ? size.height : size.width;
	    if(y + 2*yspace + h > maxheight) {
		y = yspace;
		x += maxw + xspace;
		maxw = 0;
	    }
			
	    if(columns)
		into.put(v, 1, x, y, w, h);
	    else
		into.put(v, 1, y, x, h, w);
	    y += yspace + h;
	    if(w > maxw) maxw = w;
	}
    }
}



