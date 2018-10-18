/*   
TestStringSearchers.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
import org.gzigzag.*;
import junit.framework.*;
import java.util.*;

public class TestStringSearchers extends ZZTestCase {
public static final String rcsid = "$Id: TestStringSearchers.java,v 1.1 2001/10/15 17:24:03 tjl Exp $";

    public TestStringSearchers(String name) { super(name); }

    void assertEmpty(Collection c) {
	if(c != null)
	    assertTrue(c.size() == 0);
    }

    public void testInitial1() {
	InitialStringSearcher s1 = new InitialStringSearcher();

	assume(s1.search("FOO"), null);

	s1.addString("FOOBAR", new Integer(42));

	assume(s1.search("FOO"), new int[] {42});


    }

}
