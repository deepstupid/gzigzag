/*   
ZZWindows.java
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
 * Written by Tuomas Lukka, killing by Antti-Juhani Kaijanaho,
 * triggering and rootwin support by Rauli Ruohonen
 */

package org.gzigzag;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ZZWindows {
public static final String rcsid = "$Id: ZZWindows.java,v 1.23 2001/01/03 16:47:50 raulir Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

    /** Iff null, creates Frames at top level. */
    private static Container rootwin;
    private static ZZSpace rspace;
    private static Hashtable views, frames, ex;
    private static ZZObs obs = new ZZObs() { public void chg() { update(); }};

    /** Must be called before anything else. */
    public static void init(ZZSpace s,Container rootWin) {
	rootwin=rootWin;
	rspace=s;
	if(views!=null) updateEnd();
	views=new Hashtable();
	frames=new Hashtable();
	ex=new Hashtable();
	ZZObsTrigger.chg(obs);
    }
    public static synchronized void update() {
	p("update() start");
	ZZCell rcell=startCell();
	if(rcell==null) throw new ZZError("No 'Windows' cell found!");
	ZZCell s=rcell.getOrNewCell("d.1",1,obs);
	if(rootwin!=null) updateRoot(s);
	else updateFrames(s);
	updateEnd();
	p("update() end");
    }

    // XXX Should create a ZZWindow class for operating on the structure?
    public static Rectangle getBounds(ZZCell c, ZZObs obs) {
	try {
	    int[] a=ZZUtil.getInts(c.s("d.1",-1),"d.bounds",false,4,4,obs);
	    return new Rectangle(a[0], a[1], a[2], a[3]);
	} catch(ZZError e) {
	    return null;
	}
    }
    public static Rectangle getBounds(ZZCell c) { return getBounds(c,null); }
    public static void setBounds(ZZCell c, Rectangle r) {
	Rectangle b=getBounds(c);
	if(b!=null&&b.equals(r)) return;
	ZZUtil.putInts(c.s("d.1",-1), "d.bounds", false, 
		       new int[] { r.x, r.y, r.width, r.height });
    }
    public static ZZCell startCell() {
	return ZZDefaultSpace.findOnClientlist(rspace, "Windows", false);
    }
    public static synchronized ZZViewComponent getContent(ZZCell c) {
	String txt = c.getText(obs);
	if(txt.equals("Canvas")) {
	    ZZCanvasView v;
	    if(views.get(c) instanceof ZZCanvasView)
		v = (ZZCanvasView) views.get(c);
	    else 
		views.put(c, v = new ZZCanvasView());
	    ZZCell vcell = c.s("d.1", 1,obs);
	    if(vcell == null) return null;
	    v.setViewcell(vcell);
	    return v;
	}
	return null;
    }

    private static void updateRoot(ZZCell s) {
	ScalableFont.fmComp=rootwin;
	updateWindowAttributes(s,rootwin);
	updatePanels(s.s("d.2",1,obs),rootwin);
	rootwin.validate();
    }
    private static void updateFrames(ZZCell s) {
	s=s.s("d.2",1,obs);
	while(s!=null) {
	    boolean created=false;
	    ZZCell c=s.s("d.1",1,obs);
	    if(c!=null) {
		Frame f=(Frame)frames.get(c);
		if(f==null) {
		    created=true;
		    frames.put(c,f=new Frame());
		    initWindow(c,f);
		    final ZZCell cell=c;
		    f.addWindowListener(new WindowAdapter() {
			    public void windowClosing(WindowEvent e) {
				p("windowClosing()");
				ZZUpdateManager.freeze();
				try {
				    cell.s("d.1",-1).excise("d.2");
				    ZZObsTrigger.runObsQueue();
				} finally { ZZUpdateManager.thaw(); }
			    }
			});
		}
		ScalableFont.fmComp=f;
		ex.put(c,c);
		Rectangle r=getBounds(c,obs);
		if(r!=null&&!f.getBounds().equals(r)) f.setBounds(r);
		f.setTitle(s.getText(obs));
		p("Frame name: "+f.getTitle());
		Container cont=getContent(c);
		if(cont!=null) {
		    updateWindowAttributes(s,cont);
		    cont.removeAll(); // FIXME
		    updatePanels(c.s("d.2",1,obs),cont);
		    if(f.getComponentCount()!=1||f.getComponent(0)!=cont) {
			p("Changing frame component and validating.");
			f.removeAll();
			f.add(cont);
			f.validate();
		    }
		}
		if(created) f.setVisible(true);
	    }
	    s=s.s("d.2",1,obs);
	}
    }
    private static void updatePanels(ZZCell s,Container parent) {
	if(s==null) return;
	// FIXME This kludge is here so that applets still work. The real
	// functionality should be here Real Soon Now(TM). (Yeah, *right*)
	parent.removeAll();
	Container cont=getContent(s.s("d.1",1,obs));
	updateWindowAttributes(s,cont);
	parent.add(cont);
    }
    private static void initWindow(final ZZCell c,final Container w) {
	w.addComponentListener(new ComponentAdapter() {
		public void componentMoved(ComponentEvent e) {
		    ZZUpdateManager.freeze();
		    try {
			ZZObsTrigger.setEnabled(obs,false);
			setBounds(c, w.getBounds());
			ZZObsTrigger.setEnabled(obs,true);
			ZZObsTrigger.runObsQueue();
		    } finally { ZZUpdateManager.thaw(); }
		}
		public void componentResized(ComponentEvent e) {
		    ZZUpdateManager.freeze();
		    try {
			ZZObsTrigger.setEnabled(obs,false);
			setBounds(c, w.getBounds());
			ZZObsTrigger.setEnabled(obs,true);
			ZZObsTrigger.runObsQueue();
		    } finally { ZZUpdateManager.thaw(); }
		}
	    });
    }
    private static void updateWindowAttributes(ZZCell c,Container w) {
	ZZCell col=c.s("d.color",1,obs);
	if(col!=null) {
	    String colText = col.getText(obs);
	    
	    // See if it looks like an URL or a number.
	    if(Character.isDigit(colText.charAt(0)) ||
	       colText.charAt(0) == '-') 
		w.setBackground( Color.decode(colText) );
	    else {
		ZZLogger.log("Not digit first! '"+colText+"'");
		// XXX Find image, store it in the right place (should be 
		// in content...
		/*
		  URL url = null;
		  try {
		  url = new URL(cursor.h("d.photo", 1).getText());
		  } catch(MalformedURLException e) {
		  String s= e.toString();
		  g.drawString(s, 0, 0);
		  return;
		  }
		  if(!url.equals(cur)) {
		  cur = url;
		  img = getToolkit().getImage(url);
		  }
		  if(!g.drawImage(img, 0, 0, null)) {
		  repaint(1000);
		  }
		*/
	    }
	    
	    if((col=col.s("d.color", 1,obs)) != null) 
		w.setForeground( Color.decode(col.getText(obs)) );
	    }
    }
    private static void updateEnd() {
	for (Enumeration e = views.keys(); e.hasMoreElements();) {
	    ZZCell cell = (ZZCell) e.nextElement();
	    if(!ex.containsKey(cell)) views.remove(cell);
	}
	for (Enumeration e = frames.keys(); e.hasMoreElements();) {
	    ZZCell cell = (ZZCell) e.nextElement();
	    if(ex.containsKey(cell)) continue;
	    Frame f = (Frame)frames.get(cell);
	    f.dispose();
	    frames.remove(cell);
	}
	ex=new Hashtable();
    }
}
