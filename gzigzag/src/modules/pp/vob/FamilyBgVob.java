/*   
FamilyBgVob.java
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

public class FamilyBgVob extends BgVob {
public static final String rcsid = "$Id: FamilyBgVob.java,v 1.4 2002/03/02 17:43:33 vegai Exp $";
    
    public FamilyBgVob(Cell cell, 
		       int x0, int x1, int y0, int y1, int zoom) {
	super(cell, x0, x1, y0, y1, zoom);
    }
    
    public void render(Graphics g,
		       int x, int y, int w, int h,
		       boolean boxDrawn,
		       RenderInfo info
		       ) {
	// Clear background
	super.render(g, x, y, w, h, boxDrawn, info);
	
    }
    
}

