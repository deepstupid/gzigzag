/*   
Client.java
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
import org.gzigzag.impl.*;
import org.gzigzag.impl.Cursor;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import org.gzigzag.util.*;
import org.gzigzag.impl.clasm.*;
import org.gzigzag.impl.clasm.Function;
import org.gzigzag.vob.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/** The current hardcoded key bindings for the GZZ client.
 *  To be replaced by key bindings written in Clasm.
 */

public class HardcodedBindings {
public static final String rcsid = "$Id: HardcodedBindings.java,v 1.12 2002/03/18 08:34:06 tjl Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }
    private static void out(String s) { System.out.println(s); }

    /** A buffered reader reading from stdin.
     * Used for entering commands into the terminal window.
     */
    private static BufferedReader input =
	new BufferedReader(new InputStreamReader(System.in));


    static Cell d1 = Client.d1, d2 = Client.d2;


    public void moveCharCursor(Cell window, int steps) {
	Cell cur = Cursor.getVStreamCellBefore(window);
	if(cur == null) cur = window; // Otherwise, start at beginning.
	Cell c = cur.s(Dims.d_vstream_id, steps);
    }

    /** The currently marked cell. 
     * Unfortunately, not in the structure yet.
     */
    public static Cell mark = null;

    /** The current mark subposition (along vstreamdim)
     */
    public static Cell submark = null;


    public static boolean hardcodedBinding(String key, Screen screen) {
	Cell window = screen.wc;
        Cell c = Cursor.get(window);
	VStreamDim vs = Client.space.getVStreamDim();


	Cell viewtype = 
	    Params.getParam(window, Client.c_view).getRootclone();

	p("Executing hard-coded binding "+key+" for window "+window
	  + ", accursed " + c);

	if(c == null) {
	    pa("Nothing accursed in window");
	    Cursor.set(window, window.getHomeCell());
	}

	if(Id.equals(viewtype, Client.c_vstreamv)) {
	    // VStream specific bindings
	    if(key.equals("Ctrl-A")) {
		Cursor.setVStreamCellBefore(window, 
					    Cursor.get(window));
		return true;
	    } else if (key.equals("Ctrl-E")) {
		Cursor.setVStreamCellBefore(window, 
			    Cursor.get(window).h(Dims.d_vstream_id, 1));
		return true;
	    }
	}
        
        if(key.length()==1 || key.equals("Backspace") 
	   || key.equals("Delete") || key.equals("Ctrl-V")) { 
	    Client.doNotAnimate = true;
	    char ch = key.charAt(0);
	    if(ch < 32 || ch > 254) {
		pa("Ignoring key "+(int)ch+" '"+ch+"'");
		return true;
	    }
	    // Text edit
	    // allow only on root clone
	    if(!c.equals(c.getRootclone())) {
		pa("Text edit disabled on clones."
		   +"Please edit rootclone instead.");
		return true;
	    }
	    GZZ1Space sp = (PermanentSpace)Client.space.getSpace(c);
	    if(sp == null) sp = Client.space;
	    Cell pos = Cursor.getVStreamCellBefore(window);
	    if(pos == null) { // if the cursor wasn't on text
		Cursor.setVStreamCellBefore(window, c); // put it to beginning
		pos = Cursor.getVStreamCellBefore(window);
	    }

	    if(key.length() == 1) { // Insert one character
		vs.insertAfterCell(pos, 
		    Client.space.translate(sp.makeSpanRank(key)));
		// This shouldn't be necessary any more if cursors placed
		// correctly
		// c = CursorKludge.submove(window, +1);
	    } else if(!pos.equals(c) && key.equals("Backspace")) {
		// purpose of the condition above: backspacing at beginning
		// of stream shall do nothing
			// same as above: this not needed
			// c = CursorKludge.submove(window, -1);
		vs.removeRange(pos, pos);
	    } else if(key.equals("Delete")) {
		pos = pos.s(vs);
		if(pos!=null) {
		    Cursor.moveOnVStream(window, Dims.d_user_2_id, 1);
		    vs.removeRange(pos, pos);
		}
	    } else if(key.equals("Ctrl-V")) {
		// PUI paste from clipboard
		String text = PUIClipboard.getText();
		if(text != null && !text.equals("")) {
		    // Insert String exactly like one character (above)
		    vs.insertAfterCell(pos, 
			Client.space.translate(sp.makeSpanRank(text)));
	        }
	    }
            return true;
	} else if(key.equals("Ctrl-F")) {
	    Client.client.updateScreens();
	    return true;
	} else if(key.equals("Ctrl-R")) {
	    if(mark==null)
		pa("No mark set!");
	    paramTreeCopy(mark, c);
	    return true;
	} else if(key.equals("Ctrl-U")) {
	    // Update an included space
	    PermanentSpace sp = (PermanentSpace)c.space;
	    GIDSpace curr = sp.getSpaceByInclusionId(c.id);
	    if(curr==null) {
		System.out.println("The accursed cell doesn't include a space");
		return true;
	    }
	    if(sp.isEditable(curr)) {
		System.out.println("Won't update editable slices.");
		return true;
	    }
	    try {
		pa("spec 3: "+c.s(Dims.d_spacespec_id,3)+"; 4 "+
		   c.s(Dims.d_spacespec_id,4));
		Cell pointer = c.s(Dims.d_spacespec_id,4);
		String blockid=null;
		if(pointer!=null) {
		    pa(Id.stripHome(Id.get(pointer).id));
		    Mediaserver.Id block = Client.ms.getPointer(
				Id.stripHome(Id.get(pointer).id));
		    if (block!=null) 
			blockid = block.getString();
		    pa("block "+block+" blockid "+blockid);
		}
		if(blockid==null) {
		    System.out.println("Please enter ID of the updated version "+
				   "of the included space:"
				   +"(currently it must introduce no conflicts!)");
		    blockid = input.readLine();
		    if(blockid.equals("")) {
			System.out.println("Nothing to be done then.");
			return true;
		    }
		}
		Space upd = Loader.load(Client.ms, blockid);
		// Check for conflicts
		if(Cells.conflicts(sp, curr, upd)) {
		    System.out.println("Please resolve the conflicts before "+
				       "update.");
		    return true;
		} else {
		    Cell block = Id.getBlock(blockid, sp);

		    Cell n = c.s(Dims.d_spacespec_id, 2);
		    Cursor.set(n, block);
		    Client.quit();
		}
	    } catch(IOException e) {
		e.printStackTrace();
	    }
        } else if(key.equals("Ctrl-E")) {
	    try {
		ExternalEditor.edit(c);
	    } catch(Throwable t) {
		t.printStackTrace();
	    }
	    return true;
        } else if(key.equals("Ctrl-G")) {
	    try {
		System.out.print("Please enter cell ID to go to: ");
		String id = input.readLine();
		if(id.equals("")) 
		    c = mark;
		else
		    c = window.space.getCell(id);
		Cursor.set(window, c);
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	    return true;
	} else if(key.equals("Ctrl-M")) {
	    mark = c;
	    submark = Cursor.getVStreamCellBefore(window);
	    pa("Marked cell: "+mark.id+" Current position: "+submark);
	    return true;
        } else if(key.equals("Ctrl-Q")) {
	    Client.quit();
	} else if(key.equals("Ctrl-D")) {
	    // Rotate between dimension settings.
	    Cell dims = 
		Params.getParam(window, Client.c_dims).s(d1, -1);
	    p("Dim cell is: "+dims);
	    Cell next = dims.s(d1).s(d2);
	    if(next == null)
		throw new ZZError("This is the last dimension setting " +
				  "(there is no d.systemlist loop)!");
	    if(next.s(d1, -1) != null)
		throw new ZZError("Next dimension setting is already " +
				  "connected negwards on " +
				  "d.systemlist-params");
	    dims.disconnect(d1, 1);
	    try {
		dims.connect(d1, 1, next);
	    } catch(ZZAlreadyConnectedException e) {
		throw new ZZError("Strange-- this MUST NOT happen!");
	    }
	    return true;

	/*** No Ctrl-X or Ctrl-Y at the moment: use Ctrl-D
	} else if(key.equals("Ctrl-X") || key.equals("Ctrl-Y")) {
            if(mark == null) {
		pa("No mark set");
		return true;
	    }

	    int idim = key.equals("Ctrl-X") ? 1 : 2;
	    Cell before = Params.getParam(window, c_dims).s(d1, idim-1);
	    Cell after = before.s(d1, 2);
	    before.disconnect(d1, 1);
	    Cell clone = mark.zzclone();
	    before.insert(d1, 1, clone);
	    if(after != null) {
		after.disconnect(d1, -1);
		try {
		    after.connect(d1, -1, clone);
		} catch(ZZAlreadyConnectedException e) {
		    e.printStackTrace();
		    throw new ZZError("WEIRD.");
		}
	    }
	    return true;
	***/

	} else if(key.equals("Ctrl-P")) {
	    pa("Current cell is: "+c);
	    try {
		PermanentSpace sp = (PermanentSpace)c.space;
		String id = (sp.getSpaceByInclusionId(c.id)).getLastId().getString();
		pa("Current cell includes the space with mediaserver id: "+id);
	    } catch (NullPointerException e) {}
	    return true;
	} else if(key.equals("Enter")) {
	    try {
		if(c.s(ClasmDims.d_def) != null) {
		    Function func = new Function(c);
		    Object res = func.call(Callable.noparam);
		    if(res == null) res = "void";
		    pa("Function callback returned: " +
		       Callable.asString(res));
		} else {
		    Expression exp = new CallExpression(c, new HashMap());
		    Object res = exp.eval();
                    if(res == null) res = "void";
		    pa("Expression callback returned: " +
		       Callable.asString(res));
		}
	    } catch(ClasmException e) {
		pa("Exception in clasm callback:");
		e.printStackTrace();
	    }
	    return true;
	} else if(key.equals("NumPad /")) {
	    Cursor.moveOnVStream(window, Dims.d_user_2_id, -1);
	    return true;
	} else if(key.equals("NumPad *")) {
	     Cursor.moveOnVStream(window, Dims.d_user_2_id, 1);
	    return true;
	} else if(key.equals("Ctrl-L")) {
	    // Connect marked position to current cursor position as comment
	    if(mark == null) {
		out("No mark set");
		return true;
	    }
	    Cell end1, end2;
	    Cell curpos = Cursor.getVStreamCellBefore(window);
	    if(curpos == null) {
		out("Must be on a character to link comment.");
		return true;
	    }
	    end1 = curpos.N(vs);
	    end2 = submark.N(vs);
	    end1.connect(Dims.d_link_id, end2);
	    // end2.connect(Dims.d_link_id, end1);
	    return true;
	} else if(key.equals("Ctrl-N")) {
	    // Create new comment
	    Cell curr, comm, commhead, home;
	    curr = Cursor.getVStreamCellBefore(window).N(vs);
	    comm = curr.N(Dims.d_link_id);
	    // comm.connect(Dims.d_link_id, curr);
	    commhead = comm.N(vs, -1); /* Create headcell for the 
						     vstream */
	    home = Client.space.getHomeCell(commhead);
	    home.getOrNew(Dims.d_user_3_id).insert(Dims.d_user_2_id, 1, commhead);

	    Cursor.set(window, commhead);
	    Cursor.setVStreamCellBefore(window, Cursor.get(window));
	    return true;
	} else if(key.equals("Ctrl-S")) {
	    // Split stream into two on cursor position
	    Cell head, start;
	    head = c;
	    start = Cursor.getVStreamCellBefore(window).s(vs);
	    head = head.N(Dims.d_user_2_id); // Make new headcell

	    if(start!=null) {
		start.disconnect(vs, -1);
		head.connect(vs, start);
	    }
	    return true;
	} else if(key.equals("Ctrl-J")) {
	    // Join consequent streams
	    Cell head, streamend;
	    streamend = c.h(vs, 1);
	    head = c.s(Dims.d_user_2_id);
	    if(head != null) {
		head.excise(Dims.d_user_2_id);
		streamend.connect(vs, head);
		head.excise(vs);
	    }
	    return true;
	} else if(key.equals("Ctrl-Alt-P")) {
	    pa("Printing screen");
	    FrameScreen fscreen = (FrameScreen)screen;
	    PrintJob pjob = screen.getToolkit().getPrintJob(
						fscreen.getFrame(), 
			   			"Printing Test", 
			   			null);
	    //	    pjob.printDialog();
	    if (pjob != null) {
                Graphics pg = pjob.getGraphics();

                if (pg != null) {
                    screen.printAll(pg);
                    pg.dispose(); // flush page
                } else {
		    pa("Failed to get abstract surface for printing!");
		}
                pjob.end();
		pa("Printed. If you don't see any paper, look for a ps file in"
		   +"your current directory or home directory.");
            } else {
		pa("Failed to start printing!");
	    }
	    return true;
	} else if(key.equals("Ctrl-Z")) {
	    VobVanishingClient.toggleZoom();
	    pa("Zoom "+VobVanishingClient.zoom);
	    return true;
	} else if(key.equals("Ctrl-,")) {
	    if(VobVanishingClient.page>0)
		VobVanishingClient.page--;
	    pa("Page "+VobVanishingClient.page);
	    return true;
	} else if(key.equals("Ctrl-.")) {
	    VobVanishingClient.page++;
	    pa("Page "+VobVanishingClient.page);
	    return true;
	} else if(key.equals("Ctrl-Alt-R")) {
	    SpanView.uncacheSpanSet();
	    // XXX Size wrong?!
	    try {
		System.out.println("Enter mediaserver id of imageblock to make span to");
		String smsid = input.readLine();
		Mediaserver.Id msid = new Mediaserver.Id(smsid);
		ScrollBlock sb = ScrollBlockManager.getScrollBlock(
						   Client.ms, msid);
		pa("Scrollblock: "+sb);
		Span sp = sb.getCurrent();
		pa("Span found: "+sp);
		if(sp instanceof ImageSpan || 
		   sp instanceof PageSpan) {
		    pa("setting cell span : ");
		    c.setSpan(sp);
		}
	    } catch(Throwable t) {
		t.printStackTrace();
	    }
	} else if(key.equals("Ctrl-Alt-T")) {
	    SpanView.uncacheSpanSet();
	    if(mark == null) {
		pa("no mark");
	    } else {
		Span s = mark.getSpan();
		if(s != null) {
		    pa("Setting span to "+s);
		    c.setSpan(s);
		} else {
		    pa("Marked cell doesn't have span.");
		}
	    }
	}
	    

        
	boolean ctrl = false, alt = false, shift = false;
	String orig = key;
        if(key.startsWith("Ctrl-")) {
            ctrl = true; key = key.substring(5);
	}
	if(key.startsWith("Alt-")) {
	    alt = true; key = key.substring(4);
	}
	if(key.startsWith("Shift-")) {
	    shift = true; key = key.substring(6);
	}

	// idim: 1 == X axis, 2 == Y axis
	int idim, dir;
	if(key.equals("Left")) {
	    idim = 1; dir = -1;
	} else if(key.equals("Right")) {
	    idim = 1; dir = 1;
	} else if(key.equals("Up")) {
	    idim = 2; dir = -1;
	} else if(key.equals("Down")) {
	    idim = 2; dir = 1;
	} else if(key.equals("PgUp")) {
	    idim = 3; dir = -1;
	} else if(key.equals("PgDown")) {
	    idim = 3; dir = 1;
	} else if(key.equals("Home")) {
	    idim = 4; dir = -1;
	} else if(key.equals("End")) {
	    idim = 4; dir = 1;
	} else {
	    pa("Key without binding: "+key);
	    return false;
	}

	if(Id.equals(viewtype, Client.c_vstreamv)) {
	    // VStream specific movement
	    if(idim == 1) {
		Cursor.moveOnVStream(window, Dims.d_user_2_id, dir);
		return true;
	    } else if(idim == 2) {
		Cell to = c.s(Dims.d_user_2_id, dir);
		if(to != null)
		    Cursor.set(window, to);
		return true;
	    } else if(idim == 3) {
		Cell to = Cursor.getVStreamCellBefore(window).s(vs);
		if(to == null)
		    return true;
		to = to.s(Dims.d_link_id, dir);
		if(to != null) {
		    pa("following link on d.link");
		    Cursor.set(window, to, -1);
		}
		return true;
	    } else {
		out("No such axis for this view");
		return false;
	    }
	} else {
	    Cell dim = Params.getParam(window, Client.c_dims)
						      .s(d1, idim);
	    if(!ctrl && !alt && !shift) {
		Cell to = c.s(dim, dir);
		if(to != null)
		    Cursor.set(window, to);
		return true;
	    } else if((!ctrl && alt && !shift) || (ctrl && !alt && shift)) {
		Cursor.set(window, c.N(dim, dir));
		return true;
	    } else if(ctrl && !alt && !shift) {
		if(c.s(dim, dir) == null) {
		    if(mark == null) { pa("No cell marked"); return true; }
		    c.connect(dim, dir, mark);
		    Cursor.set(window, mark);
		} else {
		    c.disconnect(dim, dir);
		}
		return true;
	    } else if(!ctrl && !alt && shift) {
		c.hop(dim, dir);
		return true;
	    } else if(ctrl && alt && !shift) {
		if(mark == null) { pa("No cell marked"); return true; }
		Cell clone = mark.zzclone();
		try {
		    c.insert(dim, dir, clone);
		} catch(ZZAlreadyConnectedException e) {
		    throw new ZZError("Weird. A new clone was already connected:"
				      + e);
		}
		Cursor.set(window, clone);
		return true;
	    } else if(ctrl && alt && shift) {
		// Include a new slice into the space
		PermanentSpace sp = (PermanentSpace)c.space;
		try {
		    if(c.s(dim, dir) != null) {
			System.out.print("Cannot connect space there: there is ");
			System.out.println("something there already.");
			return true;
		    }
		    
		    String answer, blockid=null;
		    boolean markpointer, editable;
		    do {
			pa("Would you like to use the marked cell as the "+
			   "pointer to this included space (y/n)?");
			answer = input.readLine();
		    } while(!answer.equals("y") && !answer.equals("n"));
		    markpointer = answer.equals("y") ? true : false;
		    
		    if(markpointer && mark==null) {
			pa("No mark set.");
			return true;
		    }
		    if(markpointer) {
			Mediaserver.Id 
			    block = Client.ms.getPointer(
				    Id.stripHome(Id.get(mark).id));
			if (block!=null) 
			    blockid = block.getString();
			if(blockid == null)
			    pa("The pointer doesn't refer to anything yet, "+
			       "so I'll also ask for an initial mediaserver ID.");
		    }
		    if(!markpointer || blockid == null) {
			System.out.print("Please enter ID of space to include");
			System.out.print("(just hit Enter to create new space): ");
			blockid = input.readLine();
			
		    }
		    
		    do {
			System.out.print("Should the included space ");
			System.out.print("be editable (y/n)? ");
			answer = input.readLine();
		    } while(!answer.equals("y") && !answer.equals("n"));
		    editable = answer.equals("y") ? true : false;
		    
		    Space incl;
		    if(!blockid.equals(""))
			incl = Loader.load(Client.ms, blockid);
		    else
			incl = new PermanentSpace(Client.ms);
		    
		    if(incl.getHomeCell().s(dim, -dir) != null) {
			System.out.print("Cannot connect this space in the ");
			System.out.println("given direction: its homecell ");
			System.out.print("already has a connection in ");
			System.out.println("that direction.");
			return true;
		    }
		    
		    sp.include(c, incl, editable, markpointer ? mark : null);
		    c.connect(dim, dir, sp.translate(incl.getHomeCell()));
		    Cursor.set(window, sp.translate(incl.getHomeCell()));
		    
		} catch(java.io.IOException e) {
		    e.printStackTrace();
		}
		return true;
	    }
	}
	pa("Key without binding: "+orig);
	return false;
    }

    public static void hardcodedMouse(MouseEvent me, VobScene sc, 
				      Cell wc) {

	if(me.getID()!=MouseEvent.MOUSE_CLICKED) {
	    return;
	}
    	Vob v = sc.getVobAt(me.getX(), me.getY());
		if(v == null) {
		    pa("Empty click");
		    return;
		}
		if(v.key == null || !(v.key instanceof Cell)) {
		    pa("click on non-cell: "+v.key);
		    return;
		}
		Cursor.set(wc, (Cell)v.key);
    }


    /** Copy a parameter tree into a different place so that clones stay
     *  clones but otherwise make new cells.
     *  @param from Root cell of the tree
     *  @param to   The cell that should become root of the new tree
     */
    public static void paramTreeCopy(Cell from, Cell to) {
	Cell next, step, tmp, head, first;

	next = from.s(d1);
	head = next;
	step = to;
	first = null;

	while(next!=null) {
	    if(!next.getRootclone().equals(next))
		tmp = next.zzclone();
	    else {
		tmp = to.N();
		//		tmp.setText(next.t());
	    }
	    if(step == to) { // First step
		step.connect(d1, tmp);
		first = tmp;
	    } else
		step.connect(d2, tmp);

	    paramTreeCopy(next, tmp);

	    next = next.s(d2);
	    if (next==head) { // Loop
		tmp.connect(d2, first);
		break;
	    }
	    step = tmp;
	}
    }
}
