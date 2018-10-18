/*   
PlaneView.java
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
import org.gzigzag.impl.*;
import org.gzigzag.impl.Cursor;
import org.gzigzag.client.*;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import org.gzigzag.modules.pp.vob.*;
import java.util.*;
import java.util.List;
import java.awt.*;


/** A scrollable plane with notes and a background. Can be integrated into a 
 *  composite view to show associations.
 */

public class PlaneView implements View {
public static final String rcsid = "$Id: PlaneView.java,v 1.79 2002/03/08 09:59:43 vegai Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }

    static ScalableFont font = new ScalableFont("SansSerif", Font.PLAIN, 17);

    public static Cell 
	c_gridp = Id.space.getCell("home-id:"+"0000000008000000EB8ADB157800040F0160B464ED0BD2B1CA0F143E3FC592200A1E2F44A627BE-1");
    public static Cell 
	c_almanacp = Id.space.getCell("home-id:"+"0000000008000000EB8ADB157800040F0160B464ED0BD2B1CA0F143E3FC592200A1E2F44A627BE-2");

    public static Cell 
	c_familyp = Id.space.getCell("home-id:"+"0000000008000000EB8ADB157800040F0160B464ED0BD2B1CA0F143E3FC592200A1E2F44A627BE-3");

    protected static final Object TITLEVOBKEY = new Object();




    VobScene into;
    Cell[] dims;
    Cell planetype; 
    Cell pan;
    Cell contains;
    Cell components;
    Cell association;
    Cell window;

    /** Draws the whole view into a <code>VobScene</code>. 
     */
    public void render(VobScene into, Cell window) {
	Dimension d = into.getSize();
	render(into, window, Cursor.get(window), 
		0, 0, d.width, d.height, null, null);
    }

    /** Draws the view as a subview. This is called by other views that want to
     *  include this view in them.
     */
    public void render(VobScene into, Cell window, Cell cursor, 
	    int x, int y, int w, int h,
	    List leftBuoys, List rightBuoys)
    {
	dims = Params.getCells(Params.getParam(window, Client.c_dims),Client.d1,6,null);

	planetype = dims[1]; 
	pan = dims[2]; 
	contains = dims[3]; 
	components = dims[4]; 
	association = dims[5];

	p("parametri?"+window.s(pan,3));
	int state[] = Params.getInts(window.s(pan, 1), pan, 3, null);
	p("State: "+state[0]+" "+state[1]+" "+state[2]);
	final int panx = state[0], pany = state[1], zoom = state[2];

	render(into, window, cursor, x, y, w, h, panx, pany, zoom, 
		leftBuoys, rightBuoys);
	renderCycle(into, window, cursor, x, y, w, h);
    }

    protected void renderCycle(VobScene into, Cell window, Cell cursor,
			    int x, int y, int w, int h) {
	if(cursor==null)
	    return;
	Font f = font.getFont(1000);
	FontMetrics fm = font.getFontMetrics(1000);

	Cell plane = cursor.h(contains);

	int px = x + w/2;
	int py = y + h/2;
	int pad = 10; // NoteVob text padding

	int ly = y*3/4; // Vertical radius 
	int lx = w/2*4/3; // Horizontal radius

	// Place name on the current plane
	int dw = fm.stringWidth(plane.t())+pad;
	int dh = fm.getHeight()+pad;
	Vob title = new NoteVob(plane,f,fm,pad);
	title.key = TITLEVOBKEY;
	into.put(title,9,px-dw/2,y,dw,dh);

	// Find out list size
	Cell first = plane.h(Dims.d_user_1_id);
	if(first == null)
	    first = plane;
	Cell c = first;
	int num = 0;
	do {
	    num++;
	    c = c.s(Dims.d_user_1_id);
	} while(c!=null && !first.equals(c));

	if(num==1)
	    return; // Only current paper

	double angle, a;
	if(num==2) {
	    angle = 0; // Doesn't matter
	    a = (double)Math.PI/2; // One paper backgrounded
	} else {
	    angle = Math.PI / (num-2); // Upper demicircle divided
	    a = (double)0;
	}

	// Place the planes
	c = plane;
	while(true) {
	    c = c.s(Dims.d_user_1_id);
	    if(c==null)
		c = plane.h(Dims.d_user_1_id); // Continue from other end
	    if(c==null || c==plane)
		break;

	    dw = fm.stringWidth(c.t())+pad;
	    dh = fm.getHeight()+pad;

	    NoteVob v = new NoteVob(c,f,fm, pad);

	    into.put(v, 15, 
		     (int)(Math.cos(a)*lx+px-dw/2), 
		     (int)(-Math.sin(a)*ly+y-dh/2), 
		     dw, dh);

	    a += angle;
	}
    }

    /** Draws the view as a subview. The caller gives view position (pan and 
     *  zoom) instead of it being read from the structure. This is used for
     *  smaller versions and to reference to some point in the view.
     */
    public void render(VobScene into, Cell window, Cell cursor, 
	    int x, int y, int w, int h,
	    int panx, int pany, int zoom,
	    List leftBuoys, List rightBuoys) {

	if(cursor==null) {
	    pa("No cursor for PlaneView");
	    return;
	}
	
	final int pad = 10;

	Font f = font.getFont(h*1000/600);
	FontMetrics fm = font.getFontMetrics(h*1000/600);

	Cell plane = cursor.h(contains);

	int px = x + w/2;
	int py = y + h/2;

	Rectangle clip = new Rectangle(x, y, w, h);
	Cell accursednote = cursor.h(Dims.d_vstream_view_id);
	Cell note = plane.s(contains);

	BgVob bg = null;

	this.into = into;
	this.window = window; // XXX



	Cell bgCell = plane.s(Dims.d_user_3_id, 2);
	if (bgCell != null) {
	    
	    if(Id.equals(bgCell, c_gridp)) 
		bg = new GridBgVob(plane, -w/2  + panx,
				   -h/2  + pany,
				   w/2  + panx,
				   h/2  + pany, panx, pany, zoom);
	    else if(Id.equals(bgCell, c_almanacp))
		bg = new BgVob(plane, -w/2 * zoom/1000 + panx,
				      -h/2 * zoom/1000 + pany,
				      w/2 * zoom/1000 + panx,
				      h/2 * zoom/1000 + pany, zoom);
	    else if(Id.equals(bgCell, c_familyp))
		bg = new FamilyBgVob(plane, -w/2 * zoom/1000 + panx,
				     -h/2 * zoom/1000 + pany,
				     w/2 * zoom/1000 + panx,
				     h/2 * zoom/1000 + pany, zoom);
	}
	else
	    bg = new BgVob(plane, -w/2 * zoom/1000 + panx,
			   -h/2 * zoom/1000 + pany,
			   w/2 * zoom/1000 + panx,
			   h/2 * zoom/1000 + pany, zoom);
	bg.clip = clip;
	into.put(bg, 10, x, y, w, h);

	while(note != null) {
	    int place[] = Params.getInts(note.s(pan, 1), pan, 2, null);

	    int notex = (place[0]-panx)*zoom/1000;
	    int notey = (place[1]-pany)*zoom/1000;

	    int dw = fm.stringWidth(note.t())+pad;
	    int dh = fm.getHeight()+pad;

	    NoteVob v = new NoteVob(note,f,fm, pad);
	    v.clip = clip;

	    p("note:"+note+"\npan:"+pan+" place: "+place[0]+" "+place[1]+
	      "  pans: "+panx+" "+pany+"  zoom: "+zoom);
	    p("Note("+notex+","+notey+"): "+note.t());


	    into.put(v, 1, notex+px-dw/2, notey+py-dh/2, dw, dh);
	    if(Client.mode == 1 && rightBuoys != null && 
	       note.equals(accursednote)
	       ) {
		// show association candidates for the cell currently in edit
		// search only planes on d.user-1, look for same prefix
		
		Cell start = plane.h(Dims.d_user_1_id); //.s(Dims.d_user_1_id);
		String cur = note.t();
		while(start != null) {
		    for(Cell otherNote = start.s(contains); otherNote != null;
			otherNote = otherNote.s(contains)) {
			if(otherNote.equals(note)) continue;
			if(otherNote.t().startsWith(cur)) {
			    rightBuoys.add(new PlaneBuoy(v, otherNote));
			}
		    }
		    start = start.s(Dims.d_user_1_id);
		}
	    } else if(Client.mode == 0 && (leftBuoys != null 
					   || rightBuoys != null)){
		// insert the already associated things in their
		// proper margins
		pa("Buoys");
		for(Cell assoc = note; assoc != null; 
		    assoc = assoc.s(Dims.d_clone_id)) {
		    Cell left = assoc.s(association, -1);
		    Cell right = assoc.s(association, 1);
		    pa("left: "+left+" right: "+right);
		    if(leftBuoys != null && left != null)
			leftBuoys.add(new PlaneBuoy(v, left));
		    if(rightBuoys != null && right != null)
			rightBuoys.add(new PlaneBuoy(v, right));
		}
	    }
	    note = note.s(contains);
	}

	// XXX Need to check here what we're dealing with.. don't want that 
	// ugly cross in the margin notes XXX
	// Why not? Maybe make it less ugly -Tuukka
	into.put(new CursorVob(plane.t()), 1, 
		 px, py, 5 * zoom/1000, 
		 5 * zoom/1000);
    }
    
    class PlaneBuoy extends AbstractBuoy {
	Cell center;
	public PlaneBuoy(Vob anchor, Cell center) {
	    super(anchor, new Dimension(100, 60));
	    this.center = center.getRootclone();
	}
	public void put(VobScene into, int depth,
			int x, int y, int w, int h) {
	    int place[] = Params.getInts(center.s(pan, 1), pan, 2, null);
	    render(into, window, center, x, y, w, h,
		    place[0], place[1], // coordinates of the note at center
		    500, // smaller
		    null, null);
	}
    }
}

