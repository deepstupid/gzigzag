/*   
SingleFileView.java
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

/** A view showing the current directory (a single file).
 */

public class SingleFileView extends FileBrowserView {
    public void build(VobPlacer into, File dir, FileVobFactory fact,
		      FileSorter sorter) {
	java.awt.Dimension size = into.getSize();
	FileVob v = fact.create(dir);
	int w = v.getWidth(), h = v.getHeight();
	into.put(v, 1, 5, size.height/2 - h/2, w, h);
    }
}

