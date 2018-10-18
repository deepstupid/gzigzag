/*   
Screen.java
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
 * Written by Rauli Ruohonen, Antti-Juhani Kaijanaho and Tuomas Lukka
 */
package org.gzigzag.client;
import org.gzigzag.*;
import java.awt.*;
import java.awt.event.*;

import org.gzigzag.impl.View;
import org.gzigzag.impl.UpdateManager;
import org.gzigzag.util.InputEventUtil;
import org.gzigzag.vob.VobScene;
import org.gzigzag.vob.SimpleVobScene;
import org.gzigzag.vob.ScalableFont;
import org.gzigzag.impl.clasm.Callable;
import org.gzigzag.impl.clasm.ClasmException;
import org.python.core.*;
import org.python.util.PythonInterpreter;

/** A single output window.
 */
public class Screen extends Canvas
    implements MouseListener, MouseMotionListener, Obs,
	       UpdateManager.Window,
	       UpdateManager.EventProcessor {
    public static final String rcsid = "$Id: Screen.java,v 1.7 2002/03/18 08:34:07 tjl Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }
    private static void out(String s) { System.out.println(s); }

    public final Cell screenCell;
    public View view;
    public Cell wc;
    public Color fg = Color.black, bg = new Color(0xe4f6ff);
    public int margin = 5;

    protected SimpleVobScene prev, next;
    protected boolean animusef = true;

    // XXX Should use VolatileImage in new JDKs?
    Image cache;

    Client client;

    float latestFract = 0;
    float latestLod = 0;

    /** Sets window size and location.  
     *  @param x x-coordinate for window upper left-hand corner
     *  @param y y-coordinate for window upper left-hand corner
     *  @param w width of window in pixels
     *  @param h height of window in pixels
     */
    protected void setLocation(int x, int y, int w, int h) {
	setSize(w, h);
    }

    public void die() {
    }    

    public boolean isFocusTraversable() { return true; }
    
    public Screen(Client client, Cell screen) {
	this.client = client;
	this.screenCell = screen;
	prev = new SimpleVobScene();
	prev.setSize(new Dimension(0,0));
	
	addMouseListener(this);
	addMouseMotionListener(this);
	enableEvents(AWTEvent.KEY_EVENT_MASK);
	setVisible(true);
    }
    
    public boolean animUseful() { return animusef; }
    public boolean generateEndState(int millis, float lod) {
	if (view == null) {
	    next = null;
	    return true;
	}
	p("GenEndState: 1");
	long time = System.currentTimeMillis();
	Dimension d = getSize();
	p("GenEndState: 2");
	next = new SimpleVobScene();
	// VobScene size is window size minus margins
	next.setSize(scale(d));

	p("GenEndState: 3");
	view.render(next, wc);
	p("GenEndState: 4");
	animusef = false;
	if(!Client.doNotAnimate) {
	    animusef = prev.animUseful(next);
	    Client.doNotAnimate = false; // XXX Mmm?
	}
	
	// Make sure we do as much of the work to get the first 
	// scene out here as possible.
	prev.prepareRender(next);
	p("GenEndState: 5");
	
	// prev.dump();
	
	// However, this spends too much time here, we don't want
	// to take the hit for this.
	// next.prepareRender(prev);
	
	time = System.currentTimeMillis()-time;
	p("GenEndState: 6");
	// Printing out anything at this point causes a context
	// switch and we may get a much greater lag/jump than intended
	// pa("Time: "+time+" ms");
	
	return true;
    }
    
    public Dimension scale(Dimension d) {
	return new Dimension(d.width-2*margin,d.height-2*margin);
    }
    
    /** Renders and display a frame of the view animation on screen
     */
    public void renderAnim(float fract, float lod) {
	latestFract = fract;
	latestLod = lod;
	renderAnimImage(latestFract, latestLod);
	Graphics gr = getGraphics();
	if (gr == null) return;
	paint(gr);
	gr.dispose();
    }

    /** Renders and display current view state on screen
     */
    public void renderStill(float lod) {
	renderAnim(0, lod);
    }
    
    /** Renders a frame of the view animation onto Graphics buffer
     */
    void renderAnimImage(float fract, float lod) {
	latestFract = fract;
	latestLod = lod;
	Dimension d = getSize();
	if(Client.useGlobalCache) {
	    if(cache != Client.globalCache) cache = Client.globalCache;
	    if (cache == null || cache.getWidth(null) < d.width ||
		cache.getHeight(null) < d.height) {
		int width = (cache == null ? 0 : cache.getWidth(null));
		if(d.width > width) width = d.width;
		int height = (cache == null ? 0 : cache.getHeight(null));
		if(d.height > height) height = d.height;
		Client.globalCache = createImage(d.width, d.height);
		cache = Client.globalCache;
	    }
	} else {
	    if (cache == null || cache.getWidth(null) != d.width ||
		cache.getHeight(null) != d.height)
		cache = createImage(d.width, d.height);
	}
	if(cache == null) return;

	Graphics gr = cache.getGraphics();
	gr.setColor(bg);
	gr.fillRect(0, 0, d.width, d.height);
	gr.setColor(Color.black);

	if (next == null && fract != 0) {
	    // No VobScene to draw
	    gr.setColor(Color.white);
	    gr.fillRect(0, 0, d.width, d.height);
	    gr.setColor(Color.red);

	    ScalableFont f = new ScalableFont("SansSerif", Font.PLAIN, 18);
    	    FontMetrics fm = f.getFontMetrics(1000);

	    String errstr;
	    if(wc == null) 
		errstr = "No window defined for this screen";
	    else if (view == null)
		errstr = "No view defined for this window";
	    else
		errstr = "No vobscene to interpolate to";
	    int sw = fm.stringWidth(errstr);

    	    gr.setFont(f.getFont(1000));
	    gr.drawString(errstr,
			  (d.width-sw-10)/2,
			  (d.height-fm.getHeight())/2);
	} else {
	    gr.clipRect(margin, margin,
			d.width-2*margin, d.height-2*margin);
	    gr.translate(margin, margin);
	    if (fract < UpdateManager.jumpFract)
		prev.render(gr, fg, bg, next, fract);
	    else next.render(gr, fg, bg, prev, 1-fract);
	}
	gr.dispose();
    }
    
    public boolean hasEndState() { return next != null; }
    
    public void changeStartState(float fract) {
	next.setInterpCoords(prev, 1-fract);
	prev = next;
	next = null;
    }
    
    public void endAnimation() {
	if (next != null) {
	    prev = next; next = null;
	}
    }
    
    public void chg() {
	Cell b = Params.getParam(screenCell, Client.c_bounds, this);
	if (b != null) {
	    int[] bounds = Params.getInts(b, Client.d1, 4, this);
	    int len = bounds.length,
		x = len>=1 ? bounds[0] : 20, 
		y = len>=2 ? bounds[1] : 20, 
		w = len>=3 ? bounds[2] : 300, 
		h = len>=4 ? bounds[3] : 300;

	    p("Before check: "+x+" "+y+" "+w+" "+h);

	    x = x<0 ? 0 : x;
	    y = y<0 ? 0 : y;
	    w = w<200 ? 200 : w;
	    h = h<200 ? 200 : h;

	    p("After check: "+x+" "+y+" "+w+" "+h);
		
	    setLocation(x, y, w, h);
	}

	Cell old_wc = wc;
	wc = Params.getParam(screenCell, Client.c_window, this);
	View v2 = null;
	if (wc != null) {
	    // XXX Use a ZZWindow
	    Cell vc = Params.getParam(wc, Client.c_view, this);
	    if (vc != null) {
		v2 = (View)vc.getJavaObject(this);
		if (v2 == null) {
		    v2 = client.hardcodedView(vc.getRootclone(this));
		}
	    }
	}
	if (view != v2 || (old_wc == null) != (wc == null)) {
	    view = v2;
	    UpdateManager.setSlow(this);
	    UpdateManager.chg();
	}
    }

    /** Trigger a key binding for this screen.
     *  Calls Jython code.
     *  @param name the key event, e.g. "Alt-X" or "MouseClicked1."
     */
    public void triggerKeyBinding(String name) {
	Cell bindcell = Params.getParam(Client.clientCell, 
					Client.c_keybindings);
	if(bindcell == null)
	    throw new ZZError("No key binding for client "+
			      Client.clientCell);

	if(!bindcell.t().startsWith("#"))
	    throw new ZZError("Bindcell does not start with #");

	PythonInterpreter jython = new PythonInterpreter();
	// jython.exec("from org.gzigzag.client.gzz import *");
	jython.exec("from org.gzigzag import *");
	jython.exec("from org.gzigzag.impl import *");
	jython.exec("from org.gzigzag.client import *");

	Dim[] dims = VobVanishingClient.readDims(wc);
	
	jython.set("code", bindcell);
	jython.set("screen", Screen.this);
	jython.set("screencell", Screen.this.screenCell);
	jython.set("event", name);
	jython.set("window", wc);
	jython.set("cell", org.gzigzag.impl.Cursor.get(wc));
	jython.set("space", wc.space);
	jython.set("dims", dims);

	jython.exec(bindcell.t());

	/** Use Jython, no Clasm for now
	try {
	    Callable binding = Callable.getCallable(bindcell);
	    if(binding == null)
		throw new ZZError("Key binding is no callable: "+binding);
	    binding.call(name, Screen.this.screenCell);
	} catch(ClasmException e) {
	    System.err.println("Exception occurred in Clasm callback "+
			       bindcell+" for key binding "+name+":");
	    e.printStackTrace();
	}
	*/
    }

    /** Draws cached image of the view onto screen */
    public void paint(Graphics gr) {
	Dimension d = getSize();
	if (cache == null) {
	    gr.setColor(Color.white);
	    gr.fillRect(0, 0, d.width, d.height);
	    gr.setColor(Color.red);
	    String errstr = "No image for this screen";
	    ScalableFont f = new ScalableFont("SansSerif", Font.PLAIN, 18);
	    gr.setFont(f.getFont(1000));
	    FontMetrics fm = f.getFontMetrics(1000);
	    int sw = fm.stringWidth(errstr);
	    gr.drawString(errstr,
			  (d.width-sw-10)/2,
			  (d.height-fm.getHeight())/2);
	    return;
	}
	if(Client.useGlobalCache) 
	    gr.drawImage(cache, 0, 0, d.width-1, d.height-1, 
			 0, 0, d.width-1, d.height-1, null);
	else
	    gr.drawImage(cache, 0, 0, null);
    }
    public void update(Graphics gr) { 
	// Default behaviour overridden because we clear the canvas ourself
	paint(gr); 
    }

    // ALL THESE MUST DO synchronized(UpdateManager.getSynchronizer())
    // OR PROBLEMS WILL RESULT!


    public void zzProcessEvent(AWTEvent e) {
	if(dbg) p("ZZProcessEvent "+e);
	if(e instanceof KeyEvent) {
	    KeyEvent ke = (KeyEvent)e;
	    String name = InputEventUtil.getKeyEventName(ke);
	    if(name != null && !name.equals("")) {
		boolean gotHardcoded = false;
		if(System.getProperty("gzigzag.keybindings", "hardcoded")
		   .equals("hardcoded"))
		    gotHardcoded = client.hardcodedBinding(name, this);

		if(!gotHardcoded)
		    triggerKeyBinding(name);

		UpdateManager.setSlow(this);
		UpdateManager.chg();
	    }
	} else if(e instanceof MouseEvent) {
	    MouseEvent me = (MouseEvent) e;

	    VobScene sc = next;
	    if(sc == null) sc = prev;
		
	    client.hardcodedMouse(me, sc, wc);
	    UpdateManager.setSlow(this);
	    UpdateManager.chg();    
	}
    }

    // EventQueue systemEventQueue = getToolkit().getSystemEventQueue();
    public void processKeyEvent(KeyEvent e) {
	UpdateManager.addEvent(this, e);
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
	UpdateManager.addEvent(this, e);	
    }
    public void mouseClicked(MouseEvent e)  {
	UpdateManager.addEvent(this, e);
    }
    public void mouseMoved(MouseEvent e)  {
    }
    public void mouseDragged(MouseEvent e)  {
	UpdateManager.addEvent(this, e);
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
}


