/*   
GLCellVob.java
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

public class GLCellVob extends GLVob {
public static final String rcsid = "$Id: GLCellVob.java,v 1.11 2002/03/29 19:16:06 jvk Exp $";
    public static void p(String s) { System.out.println(s); }

    GZZGL.HorizText text;
    GZZGL.TexturedQuad quad = null;
    GZZGL.ShaderQuad shquad = null;

    static GZZGL.Color black = GZZGL.createColor(0, 0, 0, 1);
    static GZZGL.Color white = GZZGL.createColor(1, 1, 1, 1);

    int w = 100;
    int h = 100;

    public GLCellVob(Object key, String str, GZZGL.TexRect bg, 
			GZZGL.Font font, int w0, int h0) {
	super(key);
	w = w0;
	h = h0;
	if(str != null)
	    text = GZZGL.createHorizText(font, str, w*20/100, h*80/100, -2);
	quad = GZZGL.createTexturedQuad(bg, (int)(-0.1*w), 
					(int)(-0.1*h), 
					(int)(w*1.1), 
					(int)(h*1.1), 0.01f);
    }

    public GLCellVob(Object key, String str, GZZGL.ShaderRect bg, 
			GZZGL.Font font, int w0, int h0, float offs) {
	super(key);
	w = w0;
	h = h0;
	if(str != null)
	    text = GZZGL.createHorizText(font, str, w*20/100, h*80/100, -2);
	p("The offset is " + offs);
	shquad = GZZGL.createShaderQuad(bg, (int)(-0.1*w), 
					(int)(-0.1*h), 
					(int)(w*1.1), 
					(int)(h*1.1), offs);
	p("ShaderQuad created");
    }

    public int addToList(int[] list, int curs, int coordsys1,
		    int coordsys2) {
	// p("GLCellvob addtolist" + curs);
	if (quad != null)
	    curs = quad.addToList(list, curs, coordsys1);
	if (shquad != null)
	    curs = shquad.addToList(list, curs, coordsys1);
	if(text != null) {
	    curs = black.addToList(list, curs);
	    curs = text.addToList(list, curs, coordsys1);
	    curs = white.addToList(list, curs);

	}
	// p("GLCellvob addtolist end" + curs);
	return curs;
    }
    

    int getWidth() { return w; }
    int getHeight() { return h; }
}

