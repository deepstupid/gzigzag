/*   
ZZCanvasView.java
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

/** A view which creates 2-D canvases to show.
 */

public  class ZZCanvasView extends ZZViewComponent {
public static final String rcsid = "$Id: ZZCanvasView.java,v 1.37 2001/06/09 09:41:10 wikikr Exp $";
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

    public ZZCanvasView() {
	super();
    }

    public void setViewcell(ZZCell v0) {
	super.setViewcell(v0);
    }

    final void upd(InputEvent ev) {
	p("Upd "+this);
        ZZUpdateManager.setFast(this);
	ZZUpdateManager.freeze();
	try {
	    kb.perform(ev, this, ctrlView, curcanv);
	    ev.consume();
	    ZZObsTrigger.runObsQueue();
	} finally {
	    ZZUpdateManager.thaw();
	}
	p("Upd finished "+this);
    }

    public void mouseClicked(MouseEvent e) {
	p("Mouse clicked "+ e);
	upd(e);
	// super.mouseClicked(e);
    }
    public void mousePressed(MouseEvent e) {
	p("MousePressed " + e);
	// clicked = curcanv.getclicked(e.getX(), e.getY());
	upd(e);
    }
    public void mouseDragged(MouseEvent e)  {
	p("MouseDragged " + e);
	upd(e);
    }
    public void mouseReleased(MouseEvent e) {
	p("MouseReleased " + e);
	upd(e);
    }
    boolean xored = false;
    int prevx; int prevy; Object prevob;
    public void mouseMoved(MouseEvent e)  {
	if ( curcanv == null ) return;
 	Graphics g = getGraphics();
	Object ob = curcanv.getObjectAt(e.getX(), e.getY());
	// If there was a previous XOR cursor, wipe it out.
	if(xored) {
	    if(ob != null && ob.equals(prevob)) {
		prevx = e.getX();
		prevy = e.getY();
		return;
	    }
	    curcanv.renderXOR(g, prevx, prevy);
	}
	if((ob != null && !ob.equals(prevob)) || 
	   (prevob != null && !prevob.equals(ob))) {
	    prevx = e.getX();
	    prevy = e.getY();
	    prevob = ob;
	    upd(e);
	}   
	prevx = e.getX();
	prevy = e.getY();
	prevob = ob;
	// This simply allows the canvas to show which element would get
	// activated (such as where the insertion cursor would be placed)
	// by a mouse click.
	
	curcanv.renderXOR(g, prevx, prevy);
	xored = true;
	super.mouseMoved(e);
    }

    ZZScene oldcanv;
    long oldtime;
    ZZScene curcanv;
    ZZCell cursor;

    String[]dims;

    public boolean reraster() {
	p("reraster " + this);
	try {
	    dims = ZZDefaultSpace.getDimList(viewCell);
	    cursor = ZZCursorReal.get(viewCell);
	    if(cursor == null) {
		    validraster = false;
		    p("No cursor");
		    return false;
	    }

	    Dimension d = getSize();

	    // XXX ???
	    /* if(oldcanv == null) */ 
	    oldcanv = curcanv;

	    curcanv = ZZDefaultSpace.getScene(viewCell, this);
	    validraster = true;

	    p("Reraster returning");
	    return (oldcanv != null && oldcanv.isInterpUseful(curcanv));
	} catch(Exception e) {
	    ZZLogger.exc(e, "Exception while rastering");
	    validraster = false;
	    oldcanv = curcanv = null;
	    return false;
	} catch(ZZError e) {
	    ZZLogger.exc(e, "ZZError while rastering");
	    validraster = false;
	    oldcanv = curcanv = null;
	    return false;
	}
    }

    public void paintInto(Graphics gr) {
      // p("DOPAINT " + this + " " + parentView + " " + 
      //   needreraster + " " + validraster +" " + animate );
      synchronized(viewCell.getSpace()) {
	    if(fract >= 1.0) oldcanv = null;
	    if(!validraster) {
		reraster(); 
		if(!validraster) {
		    gr.setColor(Color.red);
		    gr.setFont(new Font("SansSerif", Font.BOLD, 30));
		    gr.drawString("RASTER ERROR", 0, getSize().height/2);
		    System.out.println("Not valid raster");
		    repaint(1000);
		}
		return;
	    }
	/*
	    if(needreraster) {
		    needreraster = false;
		    re_raster();
	    }
	*/
	    // p("DOPAINT: AFTER RERASTER: "+parentView+" "+validraster);
	    /*
	    if(!validraster) {
		    needreraster = true;
		    repaint(1000);
		    return;
	    }
	    */
	    // if(parentView!=null) {
	    // 	curcanv.dbgPrint();
	    // }
	    // super.paint(gr);
	    // paintBorder(gr);
	    Dimension thisd = getSize();
	    Color bg = getBackground();
	    ZZCell modec = ZZKeyBindings1.getMode(viewCell).s("d.color");
	    if(modec != null) {
		if(modec.getText().equals("CURSOR"))
		    bg = ZZCursorReal.getColorOrWhite(viewCell);
		else
		    bg = new java.awt.Color(Integer.parseInt(modec.getText()));
	    }
	    gr.setColor(bg);
	    Insets ins = getInsets();
	    gr.fillRect(ins.left, ins.top, 
		thisd.width-ins.left-ins.right, 
		thisd.height-ins.top-ins.bottom);
	    gr.setColor(Color.black);

	    // System.out.println(t+" "+oldtime);
	    if(fract < 1 && oldcanv != null) {
		    if(fract > 0.3) {
			curcanv.renderInterp(gr, oldcanv, 1-fract);
		    } else {
			oldcanv.renderInterp(gr, curcanv, fract);
		    }
			    
		    getToolkit().sync();
		    // repaint();
	    } else {
		    curcanv.render(gr);

		    // XXX THIS CODE SHOULDN'T BE HERE!
		    // IT SHOULD BE SPECIFIED IN THE STRUCTURE
		    // FOR EACH VIEW SEPARATELY IF DESIRED
		    String s = ZZDefaultSpace.getInbuf(viewCell.getSpace(), 
			    false);
		    if(s != null) {
			if(s.equals(""))
			    s = ZZCursorReal.get(viewCell).getID();
			Dimension d = getSize();
			gr.drawString(s, 10, d.height-20);
		    }

		    getToolkit().sync();
	    }
	    if(xored) {
		curcanv.renderXOR(gr, prevx, prevy);
		// XXX get this right during interpolation
	    }
      }
     // p("DOPAINTRET " + this + " " + needreraster + " " + animate );
     wasInPaint = true;
    }

}
