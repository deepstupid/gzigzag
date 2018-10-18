/*   
JoyImg.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag.ideas;
import java.util.*;
import java.io.*;
import java.awt.*;
import org.gzigzag.impl.*;

/** A prototype UI for browsing multi-page documents with joystick
 * zoom.
 * Assumes a joystick similar to the Logitech Wingman 3D I have in that
 * the joystick has axes 0, 1 as x, y, axes 3, 4 as twist and throttle
 * and 5 and 6 as the hat.
 */

public class JoyImg implements Runnable {
public static final String rcsid = "$Id: JoyImg.java,v 1.3 2001/10/14 17:15:44 tjl Exp $";
    public static boolean dbg = true;
    private static final void p(String s) { if(dbg) pa(s); }
    private static final void pa(String s) { System.err.println(s); }

    LinuxJoystick joy = new LinuxJoystick(new File("/dev/js0"));

    JoystickState state = joy.getState();
    Image[] images;
    Toolkit toolkit = Toolkit.getDefaultToolkit();

    int curImage = 0;
    double curZoom = 1.0;
    double curCenterX = 0.5, curCenterY = 0.5;
    long prevTime = System.currentTimeMillis();

    int imWidth = 40, imHeight = 40;

    Frame frame;
    Panel panel;

    public void setPlaces() {
	p("State: "+state.axes[3]);
	curZoom = (state.axes[3] + 1) / 2;

	Dimension dim = panel.getSize();
	int iw = images[curImage].getWidth(null);
	int ih = images[curImage].getHeight(null);
	if(iw == -1 || ih == -1) return ;

	// Find the smaller ratio.
	double r1 = dim.width / (double)iw;
	double r2 = dim.height / (double)ih;
	double r = (r1 > r2 ? r2 : r1);
	if(r >= 1) {
	    imWidth = iw; imHeight = ih;
	} else {
	    double m = r + curZoom * (1.0 - r);
	    imWidth = (int) (iw * m);
	    imHeight = (int) (ih * m);
	}
    }

    public void run() {
	pa("Start update loop!");
	try {
	    while(true) {
		setPlaces();
		/** 
		Graphics2D g = (Graphics2D)panel.getGraphics();
		RenderingHints rh = g.getRenderingHints();
		rh.put(rh.KEY_RENDERING, rh.VALUE_RENDER_SPEED);
		g.setRenderingHints(rh);
		*/
		Graphics g = panel.getGraphics();
		// p("StartDraw "+rh);
		g.drawImage(images[curImage], 0, 0, imWidth, imHeight, null);
		// p("EndDraw");
	    }
	} catch(Exception e) {
	    e.printStackTrace();
	    pa("Exc!" +e);
	    System.exit(0);
	}
    }


    JoyImg(String[] argv) {
	images = new Image[argv.length];
	for(int i=0; i<argv.length; i++) 
	    images[i] = toolkit.getImage(argv[i]);
	frame = new Frame();
	frame.setBounds(10, 10, 600, 600);
	panel = new Panel();
	frame.add(panel);
	frame.show();
    }

    
    public static void main(String[] argv) {
	(new Thread(new JoyImg(argv))).start();
    }

}

