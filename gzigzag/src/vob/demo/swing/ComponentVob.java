/*   
ComponentVob.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
import javax.swing.*;

/** A vob containing a <strong>lightweight</strong> AWT Component.
 *  <strong>This class is not thread-safe.</strong> You need to acquire a
 *  lock on the component rendered by this class if you want to render the
 *  same component in different threads.
 */

public class ComponentVob extends Vob {
String rcsid = "$Id: ComponentVob.java,v 1.2 2001/05/13 02:30:21 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println("ComponentVob: "+s); }
    static final void pa(String s) { System.out.println("ComponentVob: "+s); }

    public Component comp;

    /** <code>comp</code> if that's a Container; <code>null</code> otherwise. */
    public Container cont;

    boolean[] visible;

    /** Create a ComponentVob whose key is its component.
     *  Whenever a component doesn't represent some object from an underlying
     *  model directly, but only plays a role in the UI structure, this
     *  constructor is be used.
     */
    public ComponentVob(Component comp) {
	this(comp, comp);
    }

    /** Create a ComponentVob with a key different than the vob's component.
     *  This constructor is used when a component is known to represent an
     *  object from an underlying structure. This way, e.g. a toolbar button
     *  and a menu item representing the same Swing action can be connected /
     *  interpolated to each other.
     */
    public ComponentVob(Object key, Component comp) {
	super(key);
	this.comp = comp;
	if(comp instanceof Container)
	    cont = (Container)comp;
	else
	    cont = null;
    }


    public void render(java.awt.Graphics g, 
		       int x, int y, int w, int h,
		       boolean boxDrawn,
	               RenderInfo info) {
	// Rectangle bounds = comp.getBounds();
	
	if(cont != null) {
	    int count = cont.getComponentCount();
	    if(visible == null || visible.length<count)
		visible = new boolean[count];
	    for(int i=0; i<count; i++) {
		Component child = cont.getComponent(i);
		visible[i] = child.isVisible();
		child.setVisible(false);
	    }
	}
		
	g.translate(x, y);
	
	Shape clip = g.getClip();
	g.clipRect(0, 0, w, h);
	
	/** Do not change the component: too slow */
	// comp.setBounds(0, 0, w, h);
	
	comp.paint(g);
	
	// comp.setBounds(bounds);
	
	g.setClip(clip);
	g.translate(-x, -y);

	if(cont != null) {
	    int count = cont.getComponentCount();
	    for(int i=0; i<count; i++)
		cont.getComponent(i).setVisible(visible[i]);
	}
    }

    /** The vob key used for ComponentVobs with children...
     *  XXX explain
     */
    static final Object vobKey = new Object();

    public static void placeChildren(Container cont, VobScene scene) {
	p("start placeChildren");
	int count = cont.getComponentCount();
	for(int i=0; i<count; i++) {
	    Component comp = cont.getComponent(i);
	    Rectangle bounds = comp.getBounds();
	    p("comp #"+i+" is "+comp+" bounds "+bounds);
	    if(comp instanceof Container) {
		VobBox sub = scene.createSubScene(comp, null, bounds.width,
							      bounds.height);
		if(sub == null) {
		    // work around TrivialVobScene bug
		    scene.put(new ComponentVob(comp), 1, bounds.x, bounds.y,
			      bounds.width, bounds.height);
		    p("put common instead of sub due to TrivialVobScene bug");
		    continue;
		}
		sub.put(new ComponentVob(vobKey, comp), 2, 0, 0,
					 bounds.width, bounds.height);
	        placeChildren((Container)comp, sub);
		scene.put(sub, 1, bounds.x, bounds.y, bounds.width, bounds.height);
		p("put sub");
	    } else {
		scene.put(new ComponentVob(comp), 1, bounds.x, bounds.y,
			  bounds.width, bounds.height);
		p("put common");
	    }
	}
	p("end placeChildren");
    }

    public static void main(String[] argv) {
	JPanel p = new JPanel();
	JButton bla = new JButton("Hello world");
	p.add(bla);
	
	for(int i=0; i<40; i++)
	    p.add(new JButton("Button Number "+i));
	
	// final Vob v = new ComponentVob(p);
	// s.put(v, 1, 20, 20, 150, 150);
/****
	JPanel f = new JPanel() {
	    VobScene s;
	    public void paint(Graphics g) {
		s.render(g, null, null, null, 0);
	    }
	    public void paintAll(Graphics g) {
		paint(g);
	    }
	    public void doLayout() {
		super.doLayout();
		s = new TrivialVobScene(getSize());
		placeChildren(this, s);
	    }
	};
****/
	JVobContainer f = new JVobContainer();
	f.add(p);
	Frame fr = new Frame();
	fr.add(f);
	fr.setVisible(true);
    }
}

