/*   
GLVanishingClient.java
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
import org.gzigzag.*;
import org.gzigzag.impl.*;
import org.gzigzag.client.*;

public class GLVanishingClient implements VanishingClient {
    public static void p(String s) { System.out.println(s); }

    GZZGL.Image img[] = { GZZGL.createImage("images/pdl/tcell0.png"),
			  GZZGL.createImage("images/pdl/tcell1.png"),
			  GZZGL.createImage("images/pdl/tcell2.png"),
			  GZZGL.createImage("images/pdl/tcell3.png"),
			  GZZGL.createImage("images/pdl/tcell4.png"),
			  GZZGL.createImage("images/pdl/tcell5.png"),
			  GZZGL.createImage("images/pdl/tcell6.png"),
			  GZZGL.createImage("images/pdl/tcell7.png") } ;
    GZZGL.TexRect bg[] = { GZZGL.createTexRect(img[0]),
			   GZZGL.createTexRect(img[1]),
			   GZZGL.createTexRect(img[2]),
			   GZZGL.createTexRect(img[3]),
			   GZZGL.createTexRect(img[4]),
			   GZZGL.createTexRect(img[5]),
			   GZZGL.createTexRect(img[6]),
			   GZZGL.createTexRect(img[7]) };

    GZZGL.Image img2[] = { GZZGL.createImage("images/pdl/tconn0.png"),
			   GZZGL.createImage("images/pdl/tconn1.png"),
			   GZZGL.createImage("images/pdl/tconn2.png"),
			   GZZGL.createImage("images/pdl/tconn3.png"),
			   GZZGL.createImage("images/pdl/tconn4.png"),
			   GZZGL.createImage("images/pdl/tconn5.png"),
			   GZZGL.createImage("images/pdl/tconn6.png"),
			   GZZGL.createImage("images/pdl/tconn7.png"),
			   GZZGL.createImage("images/pdl/tconn8.png")};
    GZZGL.TexRect conn[] = { GZZGL.createTexRect(img2[0]),
			       GZZGL.createTexRect(img2[1]),
			       GZZGL.createTexRect(img2[2]),
			       GZZGL.createTexRect(img2[3]),
			       GZZGL.createTexRect(img2[4]),
			       GZZGL.createTexRect(img2[5]),
			       GZZGL.createTexRect(img2[6]),
			       GZZGL.createTexRect(img2[7]),
			       GZZGL.createTexRect(img2[8]) };
    int imgtype = 0;

    int w = 100, h = 100;
    GZZGL.Font font = GZZGL.createFont("a010013l.pfb", 60);

    GZZGL.ShaderRect shcell = GZZGL.createShaderRect("images/pdl/turb.32t", 
						     "images/pdl/col.33t",
						     "images/pdl/spots.3t",
						     "images/pdl/cell.2t");


    GLVobScene scene;

    DirectStepper stepper = new DirectStepper(null);

    GZZGL.ClearBgModes clearbg0 = GZZGL.createClearBgModes(0.14f, 0.23f, 0.45f, 1.0f, 0.001f, 100, 1000);
    GZZGL.ClearBgModes clearbg1 = GZZGL.createClearBgModes(0.f, 0.4f, 0.f, 1.0f,
	    0.001f, 100, 1000);
  GZZGL.ClearBgModes clearbg = clearbg0;//GZZGL.createClearBgModes(255/255.f, 203/255.f, 143/255.f, 1.0f);
    

    void restart(GLVobScene scene) {
	this.scene = scene;
	scene.put(clearbg);
    }

    Function cellVobFunction = new Function() {
	public Object apply(Stepper s0) {
	    String c = s0.t();
	    // c = null;
	     p("Create with string "+c);
	     if (imgtype < 8)
		 return new GLCellVob(s0.getImmutable(), c, bg[imgtype], font, w, h);
	     else {
		 int i = s0.getImmutable().hashCode(), j = 0;
		 for(int n=0; n<32; n++) {
		     int bit = (i >>> n) & 1;
		     j += bit << 31-n;
		 }
		 j %= 1869271;
		 return new GLCellVob(s0.getImmutable(),c,shcell,font,w,h,j);
	     }
	}
    };

    public Object getVobSize(Cell c, float fract, int flags, Dimension into) {
	into.width = (int)(w * fract);
	into.height = (int)(h * fract);
	return null;
    }
    public void place(Cell c, Object o, float fract, int x0, int y0, int x1, int y1,
		int depth, float rot) {
	// p("Place "+c+" "+fract+" "+x0+" "+y0+" "+x1+" "+y1+" "+depth);
	stepper.set(c);
	GLCellVob v = (GLCellVob) cellVobFunction.apply(stepper);
	int cs = scene.newAffineCoordSys(x0, y0, 
		(x1-x0) / (float)w, 0, 
		0, (y1-y0) / (float)h,
		depth);
	scene.put(v, cs);
    }
    public void connect(Cell c1, Cell c2, int dx, int dy) {
	if(false)
		return;
	// p("Connect "+c1+" "+c2+" "+dx+" "+dy);
	int vi1 = scene.getVobIndex(c1);
	int vi2 = scene.getVobIndex(c2);
	// p("Connect: indices: "+vi1+" "+vi2);
	if(vi1 < 0 || vi2 < 0) return;
	int cs1 = scene.getCoordSys(vi1);
	int cs2 = scene.getCoordSys(vi2);

	Integer i = new Integer(100 * dx + dy);

	Object key = new GLCellConnector.TripleKey(c1, i, c2);

	GLVob v;
	// Assume horiz or vert (!!!) XXX
	int sideoffsx = (13*w+50)/100;
	int sideoffsy = (13*h+50)/100;
	int binsidex = (13*w+50)/100;
	int binsidey = (13*h+50)/100;
	float texcoord = .31f;
	if(dx > 0) {
	    v = new GLCellConnector(key, 
		conn[imgtype], texcoord, conn[imgtype], texcoord,
		w, sideoffsy, w, h - sideoffsy,
		w-binsidex, sideoffsy, w-binsidex, h-sideoffsy,
		0, h-sideoffsy, 0, sideoffsy,
		binsidex, h-sideoffsy, binsidex, sideoffsy
		);
	} else  {
	    v = new GLCellConnector(key, 
		conn[imgtype], texcoord, conn[imgtype], texcoord,
		w-sideoffsx, h, sideoffsx, h,
		w-sideoffsx, h-binsidey, sideoffsx, h-binsidey,
		sideoffsx, 0, w-sideoffsx, 0,
		sideoffsx, binsidey, w-sideoffsx, binsidey
		);
	}
	scene.put(v, cs1, cs2);

    }
}
