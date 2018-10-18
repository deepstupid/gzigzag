/*   
JEv.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
package org.gzigzag;
import java.awt.*;
import java.awt.event.*;

/** A program to simply print out events as they come. 
 * The point is to see what keyboard events actually contain on 
 * different platforms.
 */

public class JEv {
    public static void l(KeyEvent e) {
	System.out.println(e);
	System.out.println("Txt: \""+e.getKeyText(e.getKeyCode())+"\"\n");
    }
    public static void l(MouseEvent e) {
	System.out.println(e);
	System.out.println("Mouse: "+e.getModifiers());
    }
	
    public static void main(String[] argv) {
	Frame f = new Frame();
	f.setSize(300,300);
	f.show();
	f.addKeyListener(new KeyListener() {
	    public void keyTyped(KeyEvent e) { l(e); }
	    public void keyPressed(KeyEvent e) { l(e); }
	    public void keyReleased(KeyEvent e) { l(e); }
	});
	f.addMouseMotionListener(new MouseMotionListener() {
	    public void mouseDragged(MouseEvent e) { l(e); }
	    public void mouseMoved(MouseEvent e) { l(e); }
	});
	f.addMouseListener(new MouseListener() {
	    public void mouseClicked(MouseEvent e) { l(e); }
	    public void mousePressed(MouseEvent e) { l(e); }
	    public void mouseReleased(MouseEvent e) { l(e); }
	    public void mouseEntered(MouseEvent e) { l(e); }
	    public void mouseExited(MouseEvent e) { l(e); }
	});



	while(true) {
	    try { Thread.sleep(1000); }
	    catch(Exception e) {
		ZZLogger.exc(e);
	    }
	    System.out.println("------");
	}
    }
}
