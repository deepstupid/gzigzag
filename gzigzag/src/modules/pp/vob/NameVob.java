/*   
NameVob.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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
 * Written by Tuukka Hastrup
 */
package org.gzigzag.modules.pp.vob;
import java.awt.*;
    
public class NameVob extends org.gzigzag.vob.Vob {
public static final String rcsid = "$Id: NameVob.java,v 1.4 2002/03/02 17:43:33 vegai Exp $";

    /** Padding included in vob width and height */
    int pad;
    String s;
    Font f;
    FontMetrics fm;
    public Rectangle clip;
    final boolean dbg=true;

    public NameVob(Object key, String s, Font f, FontMetrics fm, int pad) {
	super(key);
	this.s = s;
	this.f = f;
	this.fm = fm;
	this.pad = pad;
    }
    
    public void render(Graphics g, 
		       int x, int y, int w, int h,
		       boolean boxDrawn,
		       RenderInfo info
		       ) {
	Rectangle oldClip = null;
	
	if(clip != null) {
	    oldClip = g.getClipBounds();
	    g.setClip(clip);
	}
	
	if(dbg) {
	    Color oldcolor = g.getColor();
	    g.setColor(Color.pink);
	    
	    g.drawRect(x, y, w, h);
	    g.setColor(oldcolor);
	}
	if(f != null)
	    g.setFont(f);
	
	// p("Draw string: clip = "+clip+" x y = "+x+" "+y+" old: "+oldClip);
	if(s != null)
	    g.drawString(s, x + pad/2, y + fm.getAscent()+pad/2);
	if(oldClip != null)
	    g.setClip(oldClip);
	
    }
}



