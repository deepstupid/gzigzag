/*   
VobPanel.java
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
package org.gzigzag.vob.demo;
import org.gzigzag.vob.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/** Components allowing the use of Vobs and VobScenes inside Swing.
 *  Usage: create a VobPanel and put Areas somewhere inside it. ((tbd))
 *  <p>
 *  XXX The Swing dependancy is only in using JPanel for double buffering.
 *      Implement double buffering of our own and get rid of Swing here.
 *      Also, then need to find a new catchy name for VobPanel ;)
 */

public class VobPanel extends JPanel implements ComponentListener,
						MouseListener,
						MouseMotionListener {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println("VobPanel: "+s); }
    static final void pa(String s) { System.out.println("VobPanel: "+s); }

    public VobPanel() {
	super(true);
	
	// XXX really do these here?
	setLayout(new BorderLayout());
	setBackground(Color.white);
    }

    DecoratableVobScene scene, interpFrom;

    /** When the last rebuild was; -1 if we aren't interpolating */
    long lastRebuild = -1;

    /** The time one interpolation takes. */
    static final long interpTime = 750;
	
    /** The vob areas currently visible in this VobPanel. */
    ArrayList areas = new ArrayList();
    HashMap locationByArea = new HashMap();
    HashMap sizeByArea = new HashMap();

    BlendingConnector cnct;
    boolean focuslock;
    Color foclockcol = new Color(0xffff8c);

    boolean showConns = true, showAnim = true;

    public void paintVobScene(Graphics g) {
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


    /** Check whether the locations or sizes of the areas have changed.
     *  If so, re-build the view.
     */
    public void checkAreas() {
	p("Checking areas.");
	if(scene == null) { rebuild(); return; }
		
	boolean needsRebuild = false;
	for(Iterator iter = areas.iterator(); e.hasNext();) {
	    Area area = (Area)iter.next();
	    p("Checking area: "+area);
	    if(!area.getSize().equals(sizeByArea.get(area))) {
		p("The area's size has changed.");
		needsRebuild = true; break;
	    }
	    if(!area.getLocation().equals(locationByArea.get(area))) {
		p("The area's location has changed.");
		needsRebuild = true; break;
	    }
	}
	if(needsRebuild)
	    rebuild();
    }

    /** Rebuild the view.
     *  Also, re-reads the size and location of each area, so that we notice
     *  when the current view becomes invalid.
     *  <p>
     *  This is called by checkAreas(), if differences in the size or location
     *  of registered Areas are detected. But it can also be called by
     *  the controller if it's noticed that the model we're showing has
     *  changed somehow, and the view thus needs to be re-built.
     */
    public void rebuild() {
	if(showAnim) interpFrom = scene;
	scene = new TrivialVobScene(getSize());
	
	for(Iterator iter = areas.iterator(); iter.hasNext();) {
	    Area area = (Area)iter.next();
	
	    area.putIntoVobScene();
	
	    sizeByArea.put(area, area.getSize());
	    locationByArea.put(area, area.getLocation());
	}

	cnct = new BlendingConnector(10);
	SimpleConnector.decorate(scene, cnct);
	cnct.putInto(scene);
	
	lastRebuild = System.currentTimeMillis();
	p("Rebuild at: "+lastRebuild);
	repaint();
    }
    

    
    /** An area where vobs are placed into. 
     *  Needs to be able to rebuild its own part of the view.
     */
    public static abstract class Area extends Component {
	VobPanel enclosing;
	public Area(VobPanel enclosing) {
	    this.enclosing = enclosing;
	    addComponentListener(enclosing);
	    addMouseListener(enclosing);
	    addMouseMotionListener(enclosing);
	    if(isVisible())
		enclosing.areas.add(this);
	}
	public void setVisible(boolean visible) {
	    p("setVisible "+visible);
	    if(visible == isVisible())
		// no change
		return;
		
	    p("setVisible-- do change");	
	    if(visible)
		enclosing.areas.add(this);
	    else
		enclosing.areas.remove(this);
	    enclosing.rebuild();
	}
		
	/** Do everything necessary to put this in enclosing's vob scene.
	 *  This creates a subscene, calls buildView() and places the subscene
	 *  into the enclosing's scene.
	 */
	private void putIntoVobScene() {
	    Dimension size = getSize();
	    VobBox subscene = enclosing.scene.createSubScene(this, null, 
						size.width, size.height);
	    buildView(subscene);

	    Point p = inScene();
	    enclosing.scene.put(subscene, 0, p.x, p.y, size.width, size.height);
	}
	
	/** Return the upper left corner of this area
	 *  in the coordinate system of the enclosing VobPanel's vob scene.
	 */
	public Point inScene() {
	    Point p = enclosing.getLocationOnScreen(),
		  q = this.getLocationOnScreen();
	    Insets ins = enclosing.getInsets();
	    p.x += ins.left; p.y += ins.top;
	    q.x -= p.x; q.y -= p.y;
	    return q;
	}
	
	/** Build the view for this area.
	 *  @param placer A vob placer as returned by getVobPlacer().
	 */
	public abstract void buildView(VobPlacer placer);
    }


 
    class BlendingConnector extends SimpleConnector {
	/** The number of connections to show. */
	int how_many;
	
	/** The cached colors, by distance from the mouse cursor. */
	Color[] colors;
	
	public BlendingConnector(int how_many) {
	    super(null);
	    this.how_many = how_many;
	    colors = org.gzigzag.util.ColorUtil.fadingColors_line(how_many);
	}
	
	int x, y;

	/** Is the mouse inside the vob panel? */
	boolean inside = false;
	
	/** The arrays of vobs, as read from the vector. */
	Vob[][] vobs;
	
	/** The minimum distance to the mouse from a member of
	 * the vector.
	 */
	double[] dists;
	
	/** The indices of the connections to interpolate.
	 *  close_indices[0] is the closest set of vobs.
	 */
	int[] close_indices;
	
	/** Whether initialize() has been called. */
	boolean initialized = false;
	
	public void initialize() {
	    dists = new double[conns.size()];
	    close_indices = new int[how_many];
	    vobs = new Vob[conns.size()][];
	    for(int i=0; i<vobs.length; i++)
		vobs[i] = (Vob[])conns.get(i);
	    conns = null;
	    initialized = true;
	}
	
	public void moved(MouseEvent ev) {
	    p("Mouse event processed.");
	    Point p = ((Area)ev.getSource()).inScene();
	    if (!focuslock) {
		x = ev.getX()+p.x; y = ev.getY()+p.y; inside = true; repaint();
	    }
	    p("x: "+x+" y: "+y);
	}
	
	/** Fill the close_indices arrays.
	 *  Smaller distances are better, but remember that -1 means "do not
	 *  use." If the number of usable vobs is &lt; how_many, then fill the
	 *  rest of the array with -1's.
	 *  <p>
	 *  This is probably a pretty stupid implementation. Feel free to write
	 *  a better one if the demo's speed is too slow, but remember that
	 *  we only need to make this as fast as needed.
	 */
	public void sort() {
	    int ncur = how_many;
	    if(ncur > dists.length) ncur = dists.length;

	    for(int i=0; i<close_indices.length; i++)
		close_indices[i] = -1;

	    OUTER: for(int i=0; i<dists.length; i++) {
		double curdist = dists[i];
		int curindex = i;
		if(curdist < 0) continue;
		
		int j = 0;

/*** nonworking efficiency hack
		for(j=close_indices.length-1; j>=0; j--) {
		    if(close_indices[j] >= 0 &&
		       dists[close_indices[j]] > curdist)
			break;
		}
		
		if(j < 0) j = 0;
***/
		
		for(; j<close_indices.length; j++) {
		    int jindex = close_indices[j];
		    if(jindex < 0) {
			close_indices[j] = curindex;
			continue OUTER;
		    }
		    
		    double jdist = dists[jindex];
		    if(curdist < jdist) {
			close_indices[j] = curindex;
			curindex = jindex;
			curdist = jdist;
		    }
		}
	    } 
	}

        public void render(java.awt.Graphics g, 
			   int x, int y, int w, int h,
			   boolean boxDrawn, RenderInfo info) {
	    if(!showConns) return;
	    if(!initialized) initialize();
					
	    Vob.Coords icoords = new Vob.Coords(), jcoords = new Vob.Coords();
	    Point p = new Point();
	
	    for(int el=0; el<vobs.length; el++) {
		Vob[] arr = (Vob[])vobs[el];
		double curdist = -1;
		int count = 0;
		for(int i=0; i<arr.length; i++) {
		    if(!info.getInterpCoords(arr[i], icoords)) continue;
		    count++;
		    /*** don't use visual center for computing distance
		    if(arr[i] instanceof CenteredVob) {
			((CenteredVob)arr[i]).getCenter(icoords, p);
			icoords.x = p.x; icoords.y = p.y;
			icoords.width = 0; icoords.height = 0;
		    }
		    ***/
		    double
			xdist = icoords.x + icoords.width/2 - this.x,
			ydist = icoords.y + icoords.height/2 - this.y,
			dist = Math.sqrt(xdist*xdist + ydist*ydist);
		    if(curdist < 0 || curdist > dist) curdist = dist;
		}
		if (count < 2) curdist = -1;
		dists[el] = curdist;
		// p("el: "+el+" curdist: "+curdist);
	    }
	
	    sort();
	
	    Color oldfg = g.getColor();

	    // render backwards, so that brigher lines are under darker lines
	    for(int k=close_indices.length-1; k>=0; k--) {
		p("close index "+k+": "+close_indices[k]);
		if(close_indices[k] < 0) continue;
		
	        Vob[] arr = (Vob[])vobs[close_indices[k]];
		
		g.setColor(colors[k]);
	        for(int i=0; i<arr.length; i++) {
		    if(!info.getInterpCoords(arr[i], icoords)) continue;
		    if(arr[i] instanceof CenteredVob) {
		        ((CenteredVob)arr[i]).getCenter(icoords, p);
		        icoords.x = p.x; icoords.y = p.y;
		        icoords.width = 0; icoords.height = 0;
		    }
		    for(int j=i+1; j<arr.length; j++) {
		        if(!info.getInterpCoords(arr[j], jcoords)) continue;
		        if(arr[j] instanceof CenteredVob) {
			    ((CenteredVob)arr[j]).getCenter(jcoords, p);
			    jcoords.x = p.x; jcoords.y = p.y;
			    jcoords.width = 0; jcoords.height = 0;
		        }

		        g.drawLine(icoords.x + icoords.width/2,
				   icoords.y + icoords.height/2,
				   jcoords.x + jcoords.width/2,
				   jcoords.y + jcoords.height/2);
		    }
	        }
	    }
	    g.setColor(oldfg);
	}
    }



    // stuff that overrides superclasses...

    public Graphics getGraphics() {
	Graphics res = super.getGraphics();

	if(!repaintPending) {
	    repaint();
	    res.clipRect(0, 0, 0, 0); // don't allow others to draw on us
	}
	
	return res;
    }
	
    public void componentResized(ComponentEvent e) { checkAreas(); }
    public void componentMoved(ComponentEvent e) { checkAreas(); }
    public void componentShown(ComponentEvent e) { checkAreas(); }
    public void componentHidden(ComponentEvent e) { checkAreas(); }

    public void mousePressed(MouseEvent e) {
	if ((e.getModifiers() & e.BUTTON3_MASK) != 0) {
	    focuslock = true;
	    setBackground(foclockcol);
	    repaint();
	}
	if(cnct != null) cnct.moved(e);
    }
    public void mouseReleased(MouseEvent e) {
	if ((e.getModifiers() & e.BUTTON3_MASK) != 0) {
	    focuslock = false;
	    setBackground(Color.white);
	    repaint();
	}
	if(cnct != null) cnct.moved(e);
    }
    public void mouseClicked(MouseEvent e) { if(cnct != null) cnct.moved(e); }
    public void mouseEntered(MouseEvent e) { if(cnct != null) cnct.moved(e); }
    public void mouseDragged(MouseEvent e) { if(cnct != null) cnct.moved(e); }
    public void mouseMoved(MouseEvent e) { if(cnct != null) cnct.moved(e); }

    public void mouseExited(MouseEvent e) {
	if(cnct != null) cnct.inside = false;
    }


    /** Whether repaint() has been called && paint(g) hasn't happened yet.
     *  This way, when repaint() is called during the getGraphics for paint(),
     *  no new repaint() will be issued.
     */
    boolean repaintPending = false;

    public void paint(Graphics g) {
	repaintPending = false;
	super.paint(g);
	paintVobScene(g);
    }

    public void repaint() {
	if(!repaintPending) {
	    super.repaint();
	    repaintPending = true;
	}
    }
}


