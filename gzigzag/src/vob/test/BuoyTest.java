/*
BuoyTest.java
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
import java.awt.*;

public class BuoyTest {
String rcsid = "$Id: BuoyTest.java,v 1.5 2001/10/06 08:11:40 tjl Exp $";

    private static void pa(String s) { System.out.println(s); }

    static public class RectVob extends Vob implements BuoyPlacer.Buoy {
	public RectVob() { super(null); }
	public void render(Graphics g, int x, int y, int w, int h,
			   boolean boxDrawn, RenderInfo info) {
	    g.drawRect(x, y, w, h);
	}
	public int getPrefWidth() { return 20; }
	public int getMinWidth() { return 15; }
	public int getPrefHeight() { return 20; }
	public int getMinHeight() { return 15; }
	public boolean constAspectScalable() { return false; }
	public void put(VobScene into, int x, int y, int w, int h) {
	    into.put(this, 1, x, y, w, h);
	}
    }
    
    static VobScene sc = new TrivialVobScene(new Dimension(300, 300));
    static BuoyPlacer bp = new BuoyPlacer(new Rectangle(0, 0, 150, 400));

    static void place(int x, int y) {
	RectVob v = new RectVob();
	sc.put(v, 1, x-10, y-10, 20, 20);
	bp.add(v, x, y, 1);
    }

    public static void main(String argv[]) {	
	place(170, 230);
	place(160, 160);
	place(200, 210);
	place(250, 180);

	bp.place(sc);
	throw new Error("Fix me for JDK 1.4.0");

	/* In JDK 1.4 beta2 can't construct like this,
	 * because of HeadlessException is thrown by Frame constructor.
	 *
	Frame f = new Frame() {
		public void paint(Graphics g) {
		    sc.render(g, Color.black, Color.white, null, 0.0f);
		}
		public void update(Graphics g) {
		    paint(g);
		}
	    };
	f.setBounds(20, 20, 300, 300);
	f.setVisible(true);
	*/
    }
}
