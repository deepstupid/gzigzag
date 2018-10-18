/*   
TestWordBreaker.java
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.vob;
import junit.framework.*;
import java.util.*;

/** A unit test for WordBreaker.
 */

public class TestWordBreaker extends TestCase {
String rcsid = "$Id: TestWordBreaker.java,v 1.2 2001/08/05 10:15:03 bfallenstein Exp $";

    public TestWordBreaker(String s) { super(s); }

    class Iter extends CharRangeIter {
	ArrayList got = new ArrayList();
	public void range(Object tag, char[] chars, int first, int last) {
	    got.add(new String(chars, first, last - first + 1));
	}
	public void object(Object ob) {
	    got.add(ob);
	}
    }

    public void testWordBreaker() {
	Iter i = new Iter();
	WordBreaker w = new WordBreaker(i);
	char[] c = "foo bar".toCharArray();
	Object o = new Object();
	w.range(null, c, 0, 3);
	w.range(null, c, 0, 3);
	w.object(o);
	w.range(null, c, 1, 5);
	w.range(null, c, 5, 5);

	Object[] got = i.got.toArray();
	Object WG = WordBreaker.WORD_GLUE;

	assertEquals("foo ", got[0]);
	assertEquals(WG, got[1]);
	assertEquals("foo ", got[2]);
	assertEquals(o, got[3]);
	assertEquals(WG, got[4]);
	assertEquals("oo ", got[5]);
	assertEquals(WG, got[6]);
	assertEquals("ba", got[7]);
	assertEquals("a", got[8]);

	assertEquals(9, got.length);
    }
}
