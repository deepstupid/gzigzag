/*   
JVobContainer.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.vob;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class JVobContainer extends JPanel {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println("JVobContainer: "+s); }
    static final void pa(String s) { System.out.println("JVobContainer: "+s); }

    public JVobContainer() {
	super(true);
	
	// XXX really do these here?
	setLayout(new BorderLayout());
	setBackground(Color.white);
    }

    VobScene scene, interpFrom;

    /** When the last rebuild was; -1 if we aren't interpolating */
    long lastRebuild = -1;

    /** The time one interpolation takes. */
    static final long interpTime = 750;
	
    boolean showAnim = true;
    protected boolean needsRebuild;

    public void paintVobScene(Graphics g) {
	if(needsRebuild == true) rebuild();
	Color oldcol = g.getColor();
	g.setColor(getBackground());
	g.fillRect(0, 0, getSize().width, getSize().height);
	g.setColor(oldcol);
	if(interpFrom != null) {
	    long sinceRebuild = System.currentTimeMillis() - lastRebuild;
	    if(sinceRebuild >= interpTime) {
		lastRebuild = -1; interpFrom = null;
		scene.render(g, getForeground(), getBackground(), null, 0);
	    } else {
		float fract = ((float)sinceRebuild) / (interpTime);
		if(fract < 0.3f)
		    interpFrom.render(g, getForeground(), getBackground(),
					 scene, fract);
		else
		    scene.render(g, getForeground(), getBackground(),
				  interpFrom, 1-fract);
		repaint();
	    }
	} else
	    scene.render(g, getForeground(), getBackground(), null, 0);
    }

    public void rebuild() {
	if(showAnim) interpFrom = scene;
	scene = new LinkedListVobScene(getSize());
	
	ComponentVob.placeChildren(this, scene);

	lastRebuild = System.currentTimeMillis();
	p("Rebuild at: "+lastRebuild);
	needsRebuild = false;
	repaint();
    }




    // stuff that overrides superclasses...
    
    public void doLayout() {
	super.doLayout();
	needsRebuild = true;
    }

    public Graphics getGraphics() {
	Graphics res = super.getGraphics();

	if(!repaintPending) {
	    repaint();
	    res.clipRect(0, 0, 0, 0); // don't allow others to draw on us
	}
	
	return res;
    }
	
    /** Whether repaint() has been called && paint(g) hasn't happened yet.
     *  This way, when repaint() is called during the getGraphics for paint(),
     *  no new repaint() will be issued.
     */
    boolean repaintPending = false;

    public void paint(Graphics g) {
	repaintPending = false;
	// super.paint(g);
	paintVobScene(g);
    }
    public void update(Graphics g) {
	repaintPending = false;
	// super.update(g);
	paintVobScene(g);
    }

    public void repaint() {
	if(!repaintPending) {
	    super.repaint();
	    repaintPending = true;
	}
    }
}


