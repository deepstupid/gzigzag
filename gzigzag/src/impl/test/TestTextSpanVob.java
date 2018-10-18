/*   
TestTextSpanVob.java
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

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.awt.*;
import junit.framework.*;

public class TestTextSpanVob extends TestCase {
public static final String rcsid = "$Id: TestTextSpanVob.java,v 1.4 2001/08/02 10:09:02 tjl Exp $";

    public TestTextSpanVob(String name) { super(name); }

    TextScrollBlock sb1;
    TextSpanVob vb;
    TextStyle style;
    TextSpan sp;

    LinebreakableChain lb;
    LinebreakableChain.GlueStyle gs;

    public void setUp() {
	style = new RawTextStyle(
	    new ScalableFont("SansSerif", 0, 15),
	    Color.black
	);

	sb1 = new TransientTextScroll();
	org.gzigzag.test.TestUtil.append(sb1, "a cdefgh jkl nop");

	sp = (TextSpan)sb1.getCurrent();
	sp = (TextSpan)sp.subSpan(2, 8);

	vb = new TextSpanVob(null, sp, style);
	vb = (TextSpanVob)vb.getVob(1000);

	lb = new LinebreakableChain();
	gs = new LinebreakableChain.GlueStyle(style);
    }

    public void testInit() {
	assertEquals("cdefgh", sp.getText());
    }

    public void testSpan() {
	assertEquals(2, sp.getRelativeStart(sp.subSpan(2,5)));
	assertEquals(5, sp.getRelativeEnd(sp.subSpan(2,5)));
    }

    Rectangle r = new Rectangle();

    public void scaleTest(int scale) {
	vb = (TextSpanVob)vb.getVob(scale);
	assertEquals(scale, vb.getScale());
	int w1 = style.getX("cd", scale, 2);
	int w2 = style.getX("efg", scale, 3);
	vb.getPartCoords(100, 200, 300, 400, sp.subSpan(2,5), r);
	assertEquals(100+w1, r.x);
	assertEquals(200, r.y);
	assertEquals(w2, r.width);
	assertEquals(400, r.height);
    }

    public void testScale1000() { scaleTest(1000); }
    public void testScale500() { scaleTest(500); }
    public void testScale100() { scaleTest(100); }

    public void testAddToChain() {
	TextSpanVob.addToChain(lb, null, (TextSpan)sb1.getCurrent(),
			    style, gs);
	assertEquals("Chain length", 4, lb.length());
    }

    public void testAddToChainSpace() {
	TextSpanVob.addToChain(lb, null,
	    (TextSpan) ((TextSpan)(sb1.getCurrent())).subSpan(1,2),
			    style, gs);
	assertEquals("Chain length", 0, lb.length());
    }

}
