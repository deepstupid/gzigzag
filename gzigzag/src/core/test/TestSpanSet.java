/*   
TestSpanSet.java
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

package org.gzigzag;
import junit.framework.*;
import org.gzigzag.impl.TransientTextScroll;
import java.util.*;
import org.gzigzag.util.*;

/** Tests for SpanSets.
 * Contrary to most other core tests, this uses a class in impl/
 * because reimplementing that for this test would be too much.
 */

public abstract class TestSpanSet extends ZZTestCase {
public static final String rcsid = "$Id: TestSpanSet.java,v 1.4 2001/10/15 17:24:02 tjl Exp $";

    public TestSpanSet(String name) { super(name); }
    
    public SpanSet s = getSpanSet();

    public abstract SpanSet getSpanSet();

    TextScrollBlock s1 = new TransientTextScroll();
    TextScrollBlock s2 = new TransientTextScroll();
    TextScrollBlock s3 = new TransientTextScroll();

    Span[] spans = new Span[20];

    void append(String s, TextScrollBlock to) throws Exception {
	for(int i=0; i<s.length(); i++) {
	    to.append(s.charAt(i));
	}
    }

    Span sp(TextScrollBlock sb, int start, int n) {
	return ((TextSpan)sb.getCurrent()).subSpan(start, start+n);
    }

    void a(TextScrollBlock sb, int start, int n, Object o) {
	s.addSpan(sp(sb, start, n), o);
    }

    public void setUp() throws Exception {
	append("foo bar baz flip fie dbvuhr  rih bdroij rgh", s1);
	append("ksef sef vklhr vkdrij rhg4ig94y 7835 483474 53", s2);
	append("45 43 54395743763976 39476 349634986348963 4634647", s3);
    }

    public void testOne() {
	a(s1, 4, 1, new Integer(1));
	assume(s.overlaps(sp(s1, 1, 2)), new int[] {});
	assume(s.overlaps(sp(s1, 1, 10)), new int[] {1});
    }
    

}



