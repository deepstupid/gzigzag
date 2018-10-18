/*   
ZZViewComponent.java
 *    
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
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
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/** A generic view as an AWT component.
 */
abstract public  class ZZViewComponent extends Panel implements ZZView, 
    MouseListener, MouseMotionListener {
public static final String rcsid = "$Id: ZZViewComponent.java,v 1.49 2001/04/17 16:40:59 ajk Exp $";
    static public boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

    ZZCell viewCell;public ZZCell getViewcell() { return viewCell; }
    ZZKeyBindings kb;
    ZZView ctrlView, dataView;

    boolean needreraster = false;
    boolean validraster = false;

    ZZViewComponent parentView;

    Component pushedViewPanel;
    ZZViewComponent pushedView;

    /** ZZViewComponent per viewCell.
     */
    static Hashtable cpts = new Hashtable();

    static void redoCtrls(ZZCell vc) {
	if(vc == null) throw new ZZError("vc == null");
		
	if(vc.getRankLength("d.ctrlview") > 2)
	    throw new ZZError("d.ctrlview rank with more than two cells!");
	
	ZZCell cc, dc;
	if((cc = vc.s("d.ctrlview", -1)) != null)
	    dc = vc;
	else if((dc = vc.s("d.ctrlview", 1)) != null)
	    cc = vc;
	else
	    cc = (dc = vc);
	
	ZZViewComponent cv = (ZZViewComponent) cpts.get(cc);
	ZZViewComponent dv = (ZZViewComponent) cpts.get(dc);
	
	if(cv == null || dv == null)
	    // other ZZViewComponent doesn't exist yet
	    cv = (dv = (ZZViewComponent) cpts.get(vc));
	
	cv.setViewPair(dv, true);
	dv.setViewPair(cv, false);
	
/* // does somebody want it like this? -->
	ZZCell h = vc.h("d.ctrlview", -1);
	ZZViewComponent c = (ZZViewComponent)cpts.get(h);
	if(c != null) {
	    while((h=h.s("d.ctrlview", 1)) != null) {
		ZZViewComponent c1 = (ZZViewComponent)cpts.get(h);
		if(c1 != null)
		    c1.setCtrl(c);
	    }
	}
*/
    }

    public void setViewcell(ZZCell vc0) {
	viewCell = vc0;
	kb = new ZZKeyBindings1();
	cpts.put(vc0, this);
	redoCtrls(viewCell);
    }
    public void setViewPair(ZZView other, boolean ctrl) {
	if(ctrl) {
	    ctrlView = this;
	    dataView = other;
	} else {
	    ctrlView = other;
	    dataView = this;
	}
    }

    ZZViewComponent(ZZCell viewCell0) {
	this();
	setViewcell(viewCell0);
    }

    ZZViewComponent() {
	addFocusListener(new FocusListener() {
	public void focusGained(FocusEvent e) {
		p(this + " focusgained");
		repaint();
	 }
        public void focusLost(FocusEvent e) {
		p(this + " focuslost");
		repaint();
	 }
	});

	addMouseListener(this);
	addMouseMotionListener(this);

	addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			re_raster_soon();
		}
	});

	enableEvents(AWTEvent.KEY_EVENT_MASK);
    }

    public void addNotify() {
	ZZUpdateManager.addView(this);
        super.addNotify();
    }

    public void removeNotify() {
        ZZUpdateManager.rmView(this);
        super.removeNotify();
    } 

    protected void processEvent(AWTEvent e) {
	p("ProcessEvent: "+e);
	super.processEvent(e);
    }

    protected void processKeyEvent(KeyEvent e) {
	p("ProcessKeyEvent: "+e);
	
	// Apply any necessary key event hacks to this event.
	e = ZZKeyHacks.keyEventHack(e);
	p("After KeyEventHack: "+e);

        int id = e.getID();

	if(id == e.KEY_PRESSED) {
	    int kc = e.getKeyCode();
	    // These sometimes caused unshifted characters to
	    // appear. Very odd.
	    if(kc == e.VK_SHIFT || kc == e.VK_CONTROL || kc == e.VK_ALT 
		|| kc == e.VK_META) {
		super.processKeyEvent(e);
		return;
	    }
	    ZZUpdateManager.freeze();
	    try {
		e.consume();
		if(kbd != null)
		    kb.perform(e, kbd.dataView, kbd.ctrlView, null);
		else
		    kb.perform(e, dataView, ctrlView, null);
		ZZObsTrigger.runObsQueue();
	    } finally { ZZUpdateManager.thaw(); }
	}
	// We don't let it go upstairs...
	// super.processKeyEvent(e);
    }


    public void mouseEntered(MouseEvent e)  {
	// XXX ??? 
	requestFocus();
    }
    public void mouseExited(MouseEvent e)  {
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseClicked(MouseEvent e)  {
	// System.out.println("MOUENT");
	// System.out.println(e);
	// kb.perform(k, ZZView.this);
	requestFocus();
	// System.out.println("MOURET");
	e.consume();
    }

    public void mouseDragged(MouseEvent e)  {
    }
    public void mouseMoved(MouseEvent e)  {
	// XXX ?
	requestFocus();
    }

    public void destroy() {
	    ZZUpdateManager.rmView(this);
    }

    ZZViewComponent kbd;
    public void setKbdStandin(ZZViewComponent v) {
    	kbd = v;
    }
    public void re_raster_soon() {
	    // p(this + " RERASTERSOON");
	    needreraster = true;
	    repaint(50);
    }

    boolean wasInPaint;
    float fract;

    boolean dblbuf = true;
    private Image cache;
    private Dimension cacheSize;

    /** Paint this component into the given graphics object,
     * without consideration for double buffering.
     */
    public abstract void paintInto(Graphics gr);

    // Paint into the buffer and then into gr.
    public void paint(Graphics gr) {
	if(dblbuf) {
	    // XXX Really necessary every time?
	    Dimension d = getSize();
	    if(cache == null ||
		cacheSize == null ||
	       !cacheSize.equals(d)) {
		cacheSize = d;
		p("Creating cache: "+d.width+" "+d.height);
		cache=null;
		if(d.width>0&&d.height>0)
		    cache = createImage(d.width, d.height);
	    }
	    if(cache == null) {
		paintInto(gr);
	    } else {
		try {
		    Graphics g = cache.getGraphics();
		    paintInto(g);
		    // g.dispose() ???
		    gr.drawImage(cache, 0, 0, null);
		    // Dispose this as well?
		} catch(Throwable t) {
		    // FIXME ARGH!
		    cache = null;
		    paintInto(gr);
		}
	    }
	} else {
	    // Single-buffer. Flashes.
	    paintInto(gr);
	}
    }
    public void update(Graphics gr) { paint(gr); }

    public void paintNow(float fract) {
	wasInPaint = false;
	this.fract = fract;
	// p("Paintnow "+this+" "+parentView+" "+reraster+" "+anim);

        Graphics gr = getGraphics();
        if (gr == null) {
            repaint(30);
            return;
        }
	paint(gr);
    }

}



