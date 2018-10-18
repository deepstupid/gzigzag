/*   
TestCharArrayVobFactory.java
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

public class TestCharArrayVobFactory extends TestCase {
String rcsid = "$Id: TestCharArrayVobFactory.java,v 1.2 2001/08/05 14:40:29 tjl Exp $";

    public TestCharArrayVobFactory(String s) { super(s); }

    CharArrayVobFactory fact;
    LinebreakableChain chain;

    public void setUp() {
	TextStyle style = new RawTextStyle(new ScalableFont("serif", 0, 12),
					   java.awt.Color.black);
	fact = new CharArrayVobFactory(style);
    }

    /** Return the text of the <code>n</code>th box in the chain.
     *  Assumes the box is a CharArrayVob-- we're testing CharArrayVobFactory,
     *  after all.
     */
    String text(int n) {
        CharArrayVob v = (CharArrayVob)chain.getBox(n);
        return new String(v.chars, v.offs, v.len);
    }

    /** Return whether there is glue
     * before the <code>n</code>th box in the chain.
     *  Only looks at the glue length and returns whether that's non-zero.
     */
    boolean glue(int n) {
        return chain.getGlue(n, chain.GLUE_LENGTH) != 0;
    }

    public void testCAVobFactory() {
        char[] c = "foo bar".toCharArray();
        fact.range(null, c, 0, 3);
        fact.range(null, c, 0, 3);
        fact.object(WordBreaker.WORD_GLUE);
        fact.range(null, c, 1, 5);
        fact.range(null, c, 5, 5);
	chain = fact.getChain();
	
	assertEquals(false, glue(0));
	assertEquals("foo ", text(0));
	assertEquals(false, glue(1));
	assertEquals("foo ", text(1));

	assertEquals(true, glue(2)); // before 2nd

	assertEquals("oo ba", text(2));
	assertEquals(false, glue(3));
	assertEquals("a", text(3));
	assertEquals(false, glue(4));

	assertEquals(4, chain.length());
    }

}
