/*   
Java2DBench.java
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

/** Benchmark Java2D: antialiasing, rotation etc.
 */

public class Java2DBench extends GraphBench {
public static final String rcsid = "$Id: Java2DBench.java,v 1.2 2001/04/01 11:38:49 tjl Exp $";
    static private void p(String s) { System.out.println(s); }

    RenderingHints speedHints = new RenderingHints(null);
    RenderingHints qualityHints = new RenderingHints(null);
    {
	speedHints.put(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_SPEED);

	qualityHints.put(
	    RenderingHints.KEY_TEXT_ANTIALIASING,
	    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	qualityHints.put(
	    RenderingHints.KEY_ANTIALIASING,
	    RenderingHints.VALUE_ANTIALIAS_ON);

	qualityHints.put(RenderingHints.KEY_RENDERING,
			 RenderingHints.VALUE_RENDER_QUALITY);
	qualityHints.put(RenderingHints.KEY_STROKE_CONTROL,
			 RenderingHints.VALUE_STROKE_PURE);

    }

    int cur;
    public void setFlags(Graphics g0) {
	Graphics2D g = (Graphics2D)g0;
	if(cur < 2) {
	    p("ROTATED");
	    g.rotate(0.5);
	} else  {
	    p("STRAIGHT");
	}
	switch(cur % 2) {
//	case 0: 
//	    p("NO HINTS");
//	    break;
	case 0: 
	    p("SPEED");
	    g.setRenderingHints(speedHints);
	    break;
	case 1: 
	    p("QUALITY");
	    g.setRenderingHints(qualityHints);
	    break;
	}
    }
    int maxcase = 4;

    public void run() {
	p("Into image 300x300");
	for(cur = 0; cur <= maxcase; cur++)
	    runRender(getImage(300,300).getGraphics());
	p("Direct");
	for(cur = 0; cur <= maxcase; cur++)
	    runRender(c.getGraphics());
    }

    public static void main(String[] argv) {
	new Java2DBench().run();
	System.exit(0);
    }
}
