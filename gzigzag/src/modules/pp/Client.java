/*   
Client.java
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
 * Written by Tuukka Hastrup
 */
package org.gzigzag.modules.pp;
import org.gzigzag.*;
import org.gzigzag.client.*;
import org.gzigzag.impl.*;
import org.gzigzag.vob.*;
import org.gzigzag.modules.pp.vob.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;
import org.gzigzag.impl.Cursor;
import org.gzigzag.impl.clasm.*;

/** An extended client for use in PP project
 */

public class Client extends org.gzigzag.client.Client {
    public static final String rcsid = "$Id: Client.java,v 1.19 2002/03/10 01:16:23 bfallenstein Exp $";
    public static boolean dbg = true;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }
    
    /** Current UI mode in paper views */
    public static int mode = 0; 

    private static int drag_x0, drag_y0, drag_zoom0;
    
    public static Cell 
	c_planev = Id.space.getCell("home-id:" + "0000000008000000E99BD882A20004A0AD6630BFDA693D810DED76CAE65DA9B7E873053466C52E-2");

    static {
    	client = new Client();
    }
    
    public View hardcodedView(Cell id) {
	View v = null;
	// pa("id:"+id+"Id.get(id):"+Id.get(id)+"c_planev:"+c_planev+"Id.get(c_planev:"+Id.get(c_planev));
	if(Id.equals(id, c_planev)) 
	    v = new BuoyView();
       
	if(v != null)
	    return v;
	else
	    return super.hardcodedView(id);
    }
    
    
    public void hardcodedMouse(MouseEvent me, VobScene vs, Cell wc) {


	Cell vc = Params.getParam(wc, c_view, null);
	if(!Id.equals(vc, c_planev)) {
	    super.hardcodedMouse(me, vs, wc);
	    return;
	}

	p("Hardcoded mouse of ppclient: "+me);

        Vob v = vs.getVobAt(me.getX(), me.getY());
        Cell c = Cursor.get(wc);
	Cell target = null, dummy = null;
        Cell dims[] = Params.getCells(Params.getParam(wc, 
		        Client.c_dims),Client.d1,6,null);
	Cell planetype = dims[1], pan = dims[2], contains = dims[3], 
	    components = dims[4], association = dims[5];
	int state[] = Params.getInts(wc.s(pan), pan, 3, null);
	int x0 = state[0], y0 = state[1], zoom = state[2];

	int id = me.getID();

	if(id==MouseEvent.MOUSE_PRESSED) {
	    drag_x0 = me.getX();
	    drag_y0 = me.getY();
	    drag_zoom0 = zoom;
	}

	if(id==MouseEvent.MOUSE_DRAGGED) {
	    zoom = drag_zoom0 - (me.getY()-drag_y0);
	    wc.s(pan, 3).setText(""+zoom);
	}

	if(me.getID()!=MouseEvent.MOUSE_CLICKED) {
	    return;
	}

        if (v == null || !(v.key instanceof Cell)) {
	    /* We didn't have a BgVob
            target = c.h(contains).h(components); // Plane cell
	    x0 = x0 + (me.getX()-me.getComponent().getSize().width)*zoom/1000;
	    y0 = y0 + (me.getY()-me.getComponent().getSize().height)*zoom/1000;
	    */
	    p("Empty click!!!");
	    return;
	} else {
	    Cell clicked = (Cell) v.key;
	    // if(v instanceof PlaneView.NoteVob) { // Click on a note
	    if(clicked.s(contains,-1)!=null) { // Click on a note cell
		// XXX This means that clicks have to be exact
		if(mode==1) { // Association mode
		    if(c.equals(clicked)) {
			p("Won't link to itself, mode reset");
			mode = 0;
			return;
		    }
		    if(!clicked.t().startsWith(c.t())) {
			p("Won't link to a note without same start");
			return;
		    }
		    // Make association
		    c.zzclone().connect(association, 1, ((Cell)clicked).zzclone());
		    mode = 0;
		    return;
		} else { // Browse mode
		    target = clicked; // Note cell
		    state = Params.getInts(target.s(pan), pan, 2, null);
		    x0 = state[0]; y0 = state[1];
		}
	    // } else if(v instanceof PlaneView.BgVob) { // Click on background paper
	    } else {
		if(mode == 1) {
		    if(clicked.equals(c.h(contains))) {
			p("Mode reset");
			mode = 0;
		    } else { 
		       p("Won't link to whole paper");
		       return;
		    }
		}
		target = clicked;
		Vob.Coords coords = new Vob.Coords();
		vs.getCoords(v, coords);
		if(v instanceof BgVob) {
		    Point p = ((BgVob)v).getPan(coords, 
							  me.getX(),me.getY());
		    x0 = p.x; y0 = p.y;
		} else {
		    x0 = 0; y0 = 0;
		}
	    }
	    /* } else {
	       p("Cell Vob of unknown type clicked.");
	       return; 
	    } */
	}

	// Set new cursor position
	Cell arg = wc.getOrNew(pan);
	arg.setText("" + x0); // XXX
	
	arg = arg.getOrNew(pan);
	arg.setText("" + y0);

	Cursor.set(wc, target);
	mode = 0;
    }


    public boolean hardcodedBinding(String key, Screen screen) {
    	     	
	Cell window = screen.wc;
	Cell c = Cursor.get(window);
       	Cell viewtype = Params.getParam(window, c_view).getRootclone();

	if(!Id.equals(viewtype, c_planev))
	    return super.hardcodedBinding(key, screen);

	p("Executing hard-coded binding "+key+" for window "+window+ ", accursed " + c);

        Cell dims[] = Params.getCells(Params.getParam(window, 
            Client.c_dims),Client.d1,6,null);
	Cell planetype = dims[1], pan = dims[2], contains = dims[3], 
	    components = dims[4], association = dims[5];
	int state[] = Params.getInts(window.s(pan), pan, 3, null);
	int x0 = state[0], y0 = state[1], zoom = state[2];

	if(c == null) {
	    pa("Nothing accursed in PlaneView");
	    Cursor.set(window, window.getHomeCell());
	    return true;
	}

	if(key.length() == 1 || key.equals("Backspace") || key.equals("Delete")
	   || key.equals("Ctrl-V") || key.equals("Enter")) { 
	    mode = 1;
	    char ch = key.charAt(0);
	    if(ch < 32 || ch > 254) {
		pa("Ignoring key " + (int) ch + " '" + ch + "'");
		return true;
	    }
	    // Text edit
	    if(c.s(contains, -1) == null) { // Not in edit mode
		// Make new note (puts into edit mode)
		c = c.N(contains);
		Cell arg = c.N(pan);
		arg.setText("" + x0);
		arg = arg.N(pan);
		arg.setText("" + y0);
	    }

	    int place[] = Params.getInts(c.s(pan, 1), pan, 2, null);

	    if(key.equals("Enter")) {
		// Add a note under the current one
		c = c.N(contains);
		Cell arg = c.N(pan);
		arg.setText("" + place[0]);
		arg = arg.N(pan);
		arg.setText("" + (place[1] + 18));
		Cursor.setVStreamCellBefore(window, c);
		return true;
	    }

	    // allow only on root clone // XXX Should we really?
	    if(!c.equals(c.getRootclone())) 
		return true;
	  	  
	    VStreamDim vs = space.getVStreamDim();
	    GZZ1Space sp = (PermanentSpace)space.getSpace(c);
	    if(sp == null) sp = (PermanentSpace)space;
	    Cell pos = Cursor.getVStreamCellBefore(window);
	    if(pos == null) {
		Cursor.setVStreamCellBefore(window, c);
		pos = Cursor.getVStreamCellBefore(window);
	    }

	    if(key.length() == 1) { // Insert one character
		vs.insertAfterCell(pos, space.translate(sp.makeSpanRank(key)));
	    } else if(key.equals("Backspace")) {
		if(!pos.equals(c)) {
		    vs.removeRange(pos, pos);
		} else if (c.t().equals("")) {
		    mode = 0;
		    Cursor.set(window, c.h(contains));
		    Cell arg = window.getOrNew(pan);
		    arg.setText(""+place[0]);
		    arg = arg.getOrNew(pan);
		    arg.setText(""+place[1]);
		    // Remove all occurrences
		    c = c.h(Dims.d_clone_id);
		    while(c!=null) {
			c.excise(contains);
			c.excise(association);
			c = c.s(Dims.d_clone_id);
		    }
		}
	    } else if(key.equals("Delete")) {
		pos = pos.s(vs);
		if(pos != null) {
		    Cursor.moveOnVStream(window, Dims.d_user_2_id, 1);
		    vs.removeRange(pos, pos);
		}
	    }
            return true;

	} else if(key.equals("Ctrl-G")) {
	    mode = 0;
	    Cursor.set(window, HardcodedBindings.mark);
	    Cell arg = window.getOrNew(pan);
	    arg.setText("0");
	    arg = arg.getOrNew(pan);
	    arg.setText("0");
	    arg = arg.getOrNew(pan);
	    arg.setText("1000");
	    return true;
	}
	pa("Key without binding: "+key);
	return false;
    } 

    public void start() {
	try {
	    // Find PP window
	    Cell ppscreen = space.getHomeCell().s(d1).h(d2,1).s(d1);
	    Cell ppwindow = Params.getParam(ppscreen, c_window);
	    pa("PP window found");
	    Cell c = space.getHomeCell();
	    // Find example data
	    while(!c.t().equals("Slices"))
		c = c.s(Dims.d_user_2_id);
	    pa("Slices found");
	    while(!c.t().equals("PP project"))
		c = c.s(Dims.d_user_1_id);
	    pa("PP project found");
	    while(!c.t().equals("ruutupaperi"))
		c = c.s(Dims.d_user_2_id);
	    pa("\"ruutupaperi\" paper found");
	    // Set window cursor
	    Cursor.set(ppwindow, c);
	    // Set screen size
	    c = Params.getParam(ppscreen, c_bounds).s(d1, 2);
	    c.setText("800");
	    c = c.s(d1);
	    c.setText("800");
	} catch (Exception e) {e.printStackTrace();};
	super.start();
    }
}




