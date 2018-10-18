/*   
FrameScreen.java
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
 * Written by Rauli Ruohonen, Antti-Juhani Kaijanaho and Tuomas Lukka
 */
package org.gzigzag.client;
import org.gzigzag.*;
import java.awt.*;
import java.awt.event.*;

/** A single output window in a Java AWT Frame.
 */

public class FrameScreen extends Screen {
public static final String rcsid = "$Id: FrameScreen.java,v 1.1 2001/12/14 20:39:47 tuukkah Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }
    private static void out(String s) { System.out.println(s); }
    protected Frame zzFrame;

    public FrameScreen(Client client, Cell screen) {
	super(client, screen);
	zzFrame = new Frame(screen.t()+" - GZigZag");
	addComponentListener(new ComponentAdapter() {
		public void componentMoved(ComponentEvent e) {
		}
		public void componentResized(ComponentEvent e) {
		}
	    });
    }

    public void die() { 
	zzFrame.dispose(); 
    }
    public void chg() {
	super.chg();
	zzFrame.setTitle(screenCell.t()+" - GZigZag");
	if (zzFrame.getComponentCount() != 1) {
	    zzFrame.removeAll();
	    zzFrame.add(this);
	    zzFrame.pack();
	    zzFrame.show();
	    // super.chg(); // Try if doing it again would help...
	}
    }

    protected void setLocation(int x, int y, int w, int h) {
	/* Probably not needed
	   Rectangle r = zzFrame.getBounds();
	   if (r.x == x && r.y == y && r.width == w && r.height == h)
	       return;
	   p("SetBounds: "+r+" to "+x+" "+y+" "+w+" "+h);
	*/

	this.setSize(w, h);
	zzFrame.setLocation(x, y);
    }

    /* Sorry, don't know why we need these -Tuukka
    public int hashCode() { return System.identityHashCode(this); }
    public boolean equals(Object o) { return this == o; }
    */

    public Frame getFrame() {
	return zzFrame;
    }
}
