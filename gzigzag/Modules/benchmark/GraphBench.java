/*   
GraphBench.java
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

package org.gzigzag.benchmark;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** Benchmark various graphics stuff.
 */

public class GraphBench {
public static final String rcsid = "$Id: GraphBench.java,v 1.3 2001/04/25 09:36:46 tjl Exp $";
    static private void p(String s) { System.out.println(s); }

    final Frame frame = new Frame();
    {
	frame.setSize(100,100);
	frame.show();
    }
    final Font font = new Font("SansSerif", Font.PLAIN, 18);
    final FontMetrics fontmetrics = frame.getFontMetrics(font);
    ZZBench b = new ZZBench();

    /** To be overridden by subclasses.
     * Some graphics implementations, notably Java2D, require
     * setting flags or hints in the Graphics object to improve
     * rendering. We abstract the flags in this routine
     * which all the rendering tests call.
     */
    public void setFlags(Graphics g) { 
    }

    static final int X(int i) {
	return 50 + (i % 3) * 10;
    }

    static final int Y(int i) {
	return 50 + (i % 5) * 10;
    }

    static final Color c(int i) {
	i %= 2;
	if(i==0) return Color.blue;
	else if(i==1) return Color.red;
	else return Color.green;
    }

    Canvas c = new Canvas();
    {
	c.setSize(300,800);
	frame.add(c);
	frame.pack();
    }

    Image getImage(int w, int h) {
	return c.createImage(w,h);
    }

    public void runRender(final Graphics g) {
	g.setFont(font);
	setFlags(g);
	b.run(new Object[] {
	    "DRAWTEXT6", "",
		new Runnable() {
		    int i = 0;
		    public void run() {
			g.setColor(c(i));
			g.drawString("abcdef", X(i), Y(i));
			i++;
		    }
		},
	    "DRAWTEXT20", "",
		new Runnable() {
		    int i = 0;
		    public void run() {
			g.setColor(c(i));
			g.drawString("abcdefghijklmnopqrst", X(i), Y(i));
			i++;
		    }
		},
	});
    }

    public void runColor() {
	b.run(new Object[] {
	    "CREATECOLORCONST", "",
		new Runnable() { 
		    public void run() {
			Color c = new Color(0x425364);
		    }
		},
	    "CREATECOLORMEM", "",
		new Runnable() { 
		    int i;
		    public void run() {
			Color c = new Color(i);
		    }
		}
		});
    }


    int col = 0x531674;

    public void run() {
	runColor();
	p("Into image 300x300");
	runRender(getImage(300,300).getGraphics());
	p("Direct");
	runRender(c.getGraphics());
	b.run(new Object[] {
	    "FNTMETR6", "FontMetrics",
		new Runnable() { 
			    public void run() { 
				int i = fontmetrics.stringWidth("abcdef");  } },
	    "FNTMETR20", "FontMetrics 20 chars",
		new Runnable() { 
		    public void run() { 
			int i = fontmetrics.stringWidth("abcdefghijklmnopqrst");  } },
	});
    }

    public static void main(String[] argv) {
	new GraphBench().run();
	System.exit(0);
    }
}
