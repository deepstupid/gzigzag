/*   
TestUtil.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
package org.gzigzag.util;
import java.io.*;
import junit.framework.*;

/** Some useful routines for tests.
 */

public class TestingUtil {
public static final String rcsid = "$Id: TestingUtil.java,v 1.2 2002/02/19 15:16:29 bfallenstein Exp $";

    static public File tmpFile(File dir) {
	while(true) {
	    String name = "tmp"+System.currentTimeMillis()+"."+
				(int)(Math.random()*10000);
	    File t = new File(dir, name);
	    if(!t.exists())
		return t;
	}
    }

    static public void deltree(File f) {
	if(f.isDirectory()) {
	    String[] s = f.list();
	    for(int i=0; i<s.length; i++)
		deltree(new File(f, s[i]));
	}
	f.delete();
    }

}
