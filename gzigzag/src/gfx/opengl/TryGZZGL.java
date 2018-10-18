/*   
TryGZZGL.java
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

/** Some trivial exercises for GZZGL.
 */
public class TryGZZGL {
    public static void p(String s) { System.out.println(s); }
    static GZZGL.Window window;
    static GZZGL.TexturedQuad quad;
    static GZZGL.HorizText txt;
    static GZZGL.SmoothConnector nur;
    static float cur = 0;
    static float curd = 0.01f;

    static void updtime() {
	cur += curd;
	if(cur > 1) { curd = -0.01f; }
	if(cur < 0) { curd = 0.01f; }
    }

    static class EH0 implements GZZGL.EventHandler {
	public void repaint() {
	    p("REPAINT!!!");

	    int[] codes = new int[200];
	    int curs = 0;
	    curs = quad.addToList(codes, curs, 0);
	    curs = txt.addToList(codes, curs, 0);
	    curs = nur.addToList(codes, curs, 0, 1);
	    codes[curs++] = 0;

	    GZZGL.render(window, 
		    codes,
		new float[] {
		    100, 100, // center
		    1, 0,
		    0, 1,
		    0,
		    500, 500,
		    1, 2, 
		    0, 1,
		    0
		},
		new float[] {
		    200, 300, // center
		    1, 1,
		    -0.5f, 1,
		    0,
		    500, 500,
		    1, 2, 
		    0, 1,
		    0
		}, 14, cur);
	    updtime();
	    p("Calling window.repaint()");
	    window.repaint();
	    p("EH.repaint returning");
	}
	public boolean keystroke(String s) {
	    p("GOT KEYSTROKE '"+s+"'");
	    return true;
	}
    }

    static GLVobScene vs1, vs2;

    static class EH1 implements GZZGL.EventHandler {
	public void repaint() {
	    // vs1.renderFull(window);
	    vs2.renderInterp(window, vs1, cur);
	    updtime();
	    window.repaint();
	}
	public boolean keystroke(String s) {
	    p("GOT KEYSTROKE '"+s+"'");
	    return true;
	}
    }

    public static void main(String[] argv) throws Exception {
	window = GZZGL.createWindow(10, 10, 400, 400, new EH1());

	Thread.sleep(1000);

	GZZGL.Image img = GZZGL.createImage("argh/ex3-cell.png");
	GZZGL.TexRect imgtile = GZZGL.createTexRect(img);
	quad = GZZGL.createTexturedQuad(imgtile, 0, 0, 200, 100, .01f);

	GZZGL.Font font = GZZGL.createFont("a010013l.pfb", 24);
	txt = GZZGL.createHorizText(font, "GZZ!!", 100, 150, 0);

	nur = GZZGL.createSmoothConnector(imgtile, .2f, imgtile, .2f,
					 0, 0, 100, 0, 
					 0, 13, 100, 13, 
					 0, 0, 100, 0, 
					 0, 13, 100, 13);

	GLCellVob cv1 = new GLCellVob("foo", "GZZ!!", imgtile, font, 100, 100);
	GLCellVob cv2 = new GLCellVob("bar", "GZZ2!!", imgtile, font, 100, 100);
	GLCellConnector con = new GLCellConnector("con", 
						  imgtile, .2f,
						  imgtile, .2f,
						  0, 0, 100, 0, 
						  0, 13, 100, 13,
						  0, 0, 100, 0, 
						  0, 13, 100, 13);
	
	vs1 = new GLVobScene();
	int cs1 = vs1.newAffineCoordSys(100, 100, 1, 0.5f, -0.5f, 1, 1);
	int cs2 = vs1.newAffineCoordSys(400, 100, 1, -0.5f, 0.5f, 0.8f, 1);

	vs1.put(cv1, cs1);
	vs1.put(cv2, cs2);
	vs1.put(con, cs1, cs2);
	vs1.makeFullList();

	vs2 = new GLVobScene();
	int cs2_1 = vs2.newAffineCoordSys(200, 200, 0.1f, 0.9f, -0.9f, 0.1f,1);
	int cs2_2 = vs2.newAffineCoordSys(600, 100, -0.1f, 0.9f, -0.9f, 0.1f,1);

	vs2.put(cv1, cs2_1);
	vs2.put(cv2, cs2_2);
	vs2.put(con, cs2_1, cs2_2);
	vs2.makeInterpList(vs1);


	p("Going to loop");
	GZZGL.eventLoop(GLUpdateManager.getTicker());
    }
}
