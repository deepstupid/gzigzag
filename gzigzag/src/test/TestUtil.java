/*   
Test.java
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

package org.gzigzag.test;
import junit.framework.*;
import org.gzigzag.*;

/** Some utility routines for tests.
 */

public class TestUtil {
public static final String rcsid = "$Id: TestUtil.java,v 1.1 2001/05/09 13:49:44 tjl Exp $";

    public static void append(TextScrollBlock sb, String s) {
	try {
	    for(int i=0; i<s.length(); i++)
		sb.append(s.charAt(i));
	} catch(ImmutableException e) {
	    throw new Error("Immutable! "+e);
	}
    }
}



