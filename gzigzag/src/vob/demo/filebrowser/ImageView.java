/*   
ImageView.java
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
import java.awt.Dimension;

/** View showing a single image.
 */

public class ImageView extends FileBrowserView {
     public void build(VobPlacer into, File dir, FileVobFactory fact,
		      FileSorter sorter) {
	 Dimension size = into.getSize();
	 into.put(new ImageVob(dir, dir), 1, 0, 0, size.width, size.height);
    }
}

