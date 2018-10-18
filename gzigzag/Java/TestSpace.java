/*   
TestSpace.java
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
 * Written by Tuomas Lukka
 */

package org.gzigzag;
import java.io.*;

public class TestSpace {
    static void p(String s) { System.out.println(s); }
    
    static public void test(ZZSpace s) {
	ZZCell c1 = s.getHomeCell();
	ZZCell c2 = c1.N("d.1", 1);
	c2.setText("FOO");
	p("Id: "+c2.getID());
	p("Text: "+c2.getText());
	if(!("FOO".equals(c2.getText())))
	    throw new ZZError("Not FOO");
    }
    public static void main(String[] argv) {
	// try {
	    File f = new File("/tmp/zzspacetest"+Math.random());
	    p("File: "+f);
	    ZZSpace space = new ZZCacheDimSpace(new DirStreamSet(f));
	    test(space);
	// } catch(Exception e) {
	//     ZZLogger.exc(e);
	// }
	System.exit(0);
    }
}



