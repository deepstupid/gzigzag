/*   
GLCellConnector.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
package org.gzigzag.gfx;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class GLCellConnector extends GLVob {
public static final String rcsid = "$Id: GLCellConnector.java,v 1.3 2002/03/23 20:58:34 tjl Exp $";
    public static void p(String s) { System.out.println(s); }

    GZZGL.SmoothConnector nur;

    int w;
    int h;

    /** A triple of objects useful as a hash key.
     */
    static class TripleKey {
	Object key1, key2, key3;
	public TripleKey(Object k1, Object k2, Object k3) {
	    key1 = k1;
	    key2 = k2;
	    key3 = k3;
	}
	public int hashCode() {
	    return key1.hashCode() ^ (2*key2.hashCode()) ^ 
		    (4*key3.hashCode());
	}
	public boolean equals(Object o) {
	    if(!(o instanceof TripleKey)) return false;
	    TripleKey ot = (TripleKey)o;
	    return ot.key1.equals(key1) && ot.key2.equals(key2) &&
		ot.key3.equals(key3);
	}
    }

    TripleKey trip;

    public GLCellConnector(Object key,
			   GZZGL.TexRect start, float startb,
			   GZZGL.TexRect end, float endb,
			int x1l, int y1l, int x1r, int y1r, 
			int x1li, int y1li, int x1ri, int y1ri, 
			int x2l, int y2l, int x2r, int y2r, 
			int x2li, int y2li, int x2ri, int y2ri 
			) {
	super(key);

	nur = GZZGL.createSmoothConnector(start, startb, end, endb, 
				 x1l, y1l, x1r, y1r, x1li, y1li, x1ri, y1ri, 
				 x2l, y2l, x2r, y2r, x2li, y2li, x2ri, y2ri);
    }

    public int addToList(int[] list, int curs, int coordsys1,
		    int coordsys2) {
	curs = nur.addToList(list, curs, coordsys1, coordsys2);
	return curs;
    }
    

    int getWidth() { return w; }
    int getHeight() { return h; }
}


