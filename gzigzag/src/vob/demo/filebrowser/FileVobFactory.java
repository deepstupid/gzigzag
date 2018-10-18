/*   
FileVobFactory.java
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

/** A factory creating file vobs.
 *  This is needed because file vobs need a lot of context that is not easily
 *  constructed.
 */

public abstract class FileVobFactory {
	public abstract FileVob create(File f);
	public FileVob put(VobPlacer into, File f, int depth, int x, int y) {
	    FileVob v = create(f);
	    into.put(v, depth, x, y, v.getWidth(), v.getHeight());
	    return v;
	}
}

