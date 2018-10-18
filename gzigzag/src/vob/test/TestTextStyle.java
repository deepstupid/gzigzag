/*   
TestTextStyle.java
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
import java.awt.*;

/** A unit test for LinebreakableVobChain.
 */

public class TestTextStyle extends TestCase {
String rcsid = "$Id: TestTextStyle.java,v 1.2 2001/08/04 09:12:57 bfallenstein Exp $";

    public TestTextStyle(String s) { super(s); }

    static class Style extends TextStyle {
	Style(ScalableFont f) { super(f, null); }
	public void render(java.awt.Graphics g,
			   char[] chars, int offs, int len, int scale,
			   int x, int y, int w, int h, boolean boxDrawn,
			   Vob.RenderInfo info) {}
    }

    Style s1, s2;
    ScalableFont f1, f2;
    int size1 = 22, size2 = 34;
    public void setUp() {
	f1 = new ScalableFont("serif", Font.PLAIN, size1);
	f2 = new ScalableFont("sans-serif", Font.BOLD, size2);
	ScalableFont f1a = new ScalableFont("serif", Font.PLAIN, 12),
	             f2a = new ScalableFont("sans-serif", Font.BOLD, 18);
	s1 = new Style(f1a); s2 = new Style(f2a);
    }

    public void testGetScale() {
	String s = "Schnack: blah!";
	int w1 = f1.getFontMetrics(1000).stringWidth(s),
	    h1 = f1.getFontMetrics(1000).getHeight(),
	    w2 = f2.getFontMetrics(1000).stringWidth(s),
	    h2 = f2.getFontMetrics(1000).getHeight();
	
	int scale1 = s1.getScale(s, w1, h1),
	    scale2 = s2.getScale(s, w2, h2);

	assertEquals(size1, s1.font.getFont(scale1).getSize());
	assertEquals(size2, s2.font.getFont(scale2).getSize());
    }

}

