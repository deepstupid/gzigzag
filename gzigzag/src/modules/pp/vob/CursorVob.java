/*   
CursorVob.java
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
 * Written by Vesa Kaihlavirta
 */
package org.gzigzag.modules.pp.vob;
import org.gzigzag.*;
import java.awt.*;
		

public class CursorVob extends org.gzigzag.vob.Vob {
public static final String rcsid = "$Id: CursorVob.java,v 1.5 2002/03/05 11:28:58 vegai Exp $";

    public CursorVob(Object key) {
	super(key);
    }
    
    public void render(Graphics g, 
		       int x, int y, int w, int h,
		       boolean boxDrawn,
		       RenderInfo info
		       ) {
	// -x%2 to round down to odd number of pixels
	g.drawLine(x, y + h/2, 
		   x + w -w%2, y + h/2);
	g.drawLine(x + w/2, y, 
		   x + w/2, y + h -h%2);
    }
}
