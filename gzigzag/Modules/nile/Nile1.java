/*   
Nile1.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;
import java.io.*;

/** A stream with parallel markup.
 * <p>
 * Character-level formatting is currently done using B and I (as in HTML,
 * for bold and italics). These are placed in cells whose headcell
 * on <code>d.nile-fmt-start</code> is the cell in the stream that the 
 * markup starts from. Likewise, their headcell on <code>d.nile-fmt-end</code>
 * is the last cell that does have this markup.
 * This is a bit fragile with reordering but most effects can be limited by
 * knowing that all formats are allowed to touch only things inside a paragraph
 * and we can run a fixer script every once in a while.
 * <p>
 * Paragraph-, section- and chapter -level markup is done by cells
 * that say the level: P, H1..H6 on the dimension
 * <code>d.nile-struct</code>. These are interpreted by the space part 
 * so that the HOP operation can be used to move things around easily.
 * For higher-than-paragraph level sectioning, the part starting at
 * the section, up to the next paragraph or section is considered the
 * heading.
 * So, in <pre>H1 foo H2 bar P ab H2 zzz P cd</pre>, 
 * the headings would be foo, bar and zzz.
 */

public class Nile1 {
public static final String rcsid = "$Id: Nile1.java,v 1.10 2001/01/26 13:53:27 ajk Exp $";
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    static public ZZModule module = new ZZModule() {
	public void action(String id,
		ZZCell code, 
		ZZCell target,
		ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
	    ZZUpdateManager.setFast(view);
	
	    ZZCell viewCell = view.getViewcell();
	    ZZCell otherCell = viewCell.s("d.nile-wins");
	
	    boolean split = false;
	    ZZCell viewAcc = ZZCursorReal.get(viewCell);
	    ZZCell leftCell = viewAcc.s("d.nile-pair", -1);
	    ZZCell rightCell = viewAcc.s("d.nile-pair");
	    if(leftCell != null || rightCell != null) {
		split = true;
		
		if(leftCell != null) {
		    rightCell = viewAcc;
		    viewCell = rightCell;
		    otherCell = leftCell;
		} else {
		    leftCell = viewAcc;
		    viewCell = leftCell;
		    otherCell = rightCell;
		}
	    }

	    p("Nile action: id: '"+id+"' key: '"+key+"' code: "+code);

	    StringTokenizer st = new StringTokenizer(id);
	    String[] toks = new String[st.countTokens()];
	    for(int i=0; i<toks.length; i++)
		toks[i] = st.nextToken();

	    int dir = 0;
	    Nile2Unit unit = null;
	    if(toks.length > 1) {
		if(toks[1].equals("+")) dir = 1;
		else if(toks[1].equals("-")) dir = -1;
		
		if(toks.length > 2) {
		    if(toks[2].equals("CHAR"))
			unit = new Nile2Unit.Char();
		    else if(toks[2].equals("WORD"))
			unit = new Nile2Unit.Word();
		    else if(toks[2].equals("SENT"))
			unit = new Nile2Unit.Sentence();
		    else if(toks[2].equals("PARA"))
			unit = new Nile2Unit.Paragraph();
		}
	    }

	    id = toks[0];

	    if(id.equals("Cursor")) {
		p("Cursor: "+dir);
		Nile2Ops.moveCursor(unit, viewCell, dir);
		ZZUpdateManager.setFast(null);
	    }
	    else if(id.equals("AdjLeft")) {
		Nile2Ops.adjust(unit, viewCell, -1, dir);
		ZZUpdateManager.setFast(null);
	    }
	    else if(id.equals("AdjRight")) {
		Nile2Ops.adjust(unit, viewCell, 1, dir);
		ZZUpdateManager.setFast(null);
	    }
	    else if(id.equals("Hop")) {
		Nile2Ops.hop(unit, viewCell, dir);
	    }
	    else if(id.equals("Mode")) {
		Nile2Ops.setCursor(unit, viewCell, null);
		if(split)
		    Nile2Ops.setCursor(unit, otherCell, null);
	    } 
	    else if(id.equals("Insert")) {
		Nile2Ops.insert(viewCell, key);
		
		// Set the selection of other half to char mode, too
		if(split)
		    Nile2Ops.setCursor(new Nile2Unit.Char(), otherCell, null);
	    } 
	    else if(id.equals("BackSpace")) {
		Nile2Ops.backspace(unit, viewCell);
	    }
	    else if(id.equals("Delete")) {
		Nile2Ops.del(unit, viewCell);
	    }
	    else if(id.equals("BreakParagraph")) {
		Nile2Ops.breakParagraph(unit, viewCell);
	    }
	    else if(id.equals("Tunnel")) {
		if(otherCell != null) {
		    Nile2Ops.tunnel(unit, otherCell, viewCell, dir, false);
		}
	    }
	    else if(id.equals("Transcopy")) {
		if(otherCell != null) {
		    Nile2Ops.tunnel(unit, otherCell, viewCell, dir, true);
		}
	    }
	    else if(id.equals("PUICopy")) {
		ZZCell c = ZZCursorReal.get(viewCell);
		String s = Nile2Ops.stringify(c);
		ZZUtil.puiCopy(s);
	    }
/*
	    else if(id.equals("TraverseTranscopies")) {
		Nile2Ops.traverseTranscopies(viewCell, dir);
	    }
*/

	    Object ob = null;
	    if(pt != null)
		ob = xi.getObjectAt(pt.x, pt.y);

	    if(id.equals("MouseSetCursor")) {
		p("Setting cursor from mouse! Unit: "+unit);
		if(split) {
		    // Brute force: was the click in left window half?
		    if(pt.x < ((FlobSet)xi).getSize().width/2) {
			viewCell = leftCell; otherCell = rightCell;
		    } else {
			viewCell = rightCell; otherCell = leftCell;
		    }
		    ZZCursorReal.set(view.getViewcell(), viewCell);
		}
		Nile2Ops.setCursor(unit, viewCell, (ZZCursor)ob);
	    }
	}

	public ZOb newZOb(String id) {
	    if(id.equals("Normal")) {
		return new Nile1View();
	    } else if(id.equals("Parallel")) {
		return new SplitNileView();
	    }
	    return null;
	}
    };


    // This is really hacky part...
    static ZZCell curAct;
    static void a(String key, String act) { a(key, act, null); }
    static void a(String key, String act, ZZCell next) {
	curAct = curAct.N("d.2", 1);
	curAct.setText(key);
	ZZCell n = curAct.N("d.1", 1);
	n.setText("Nile1." + act);
	if(next != null)
	    next.insert("d.3", 1, n);
    }

    static void actions(ZZCell start) {
	start.setText("Nile char");
	ZZCell nchar = start;
	ZZCell nword = nchar.N("d.1", 1); nword.setText("Nile word");
	ZZCell nsent = nword.N("d.1", 1); nsent.setText("Nile sent");
	ZZCell npara = nsent.N("d.1", 1); npara.setText("Nile para");

	ZZCell ncommon = npara.N("d.1", 1); ncommon.setText("Nile common");

	curAct = nword;
	// Annoying - these should be structurally 
	// describable!!!
	a("MouseClicked1", "MouseSetCursor 0 WORD");
	a("Backspace", "BackSpace 0 WORD");
	a("Delete", "Delete 0 WORD");
	a("Alt-Backspace", "Delete 0 WORD");
	a("Ctrl-P", "Tunnel - WORD"); // P for pull
	a("Ctrl-T", "Transcopy - WORD");
	a("Left", "Cursor - WORD");
	a("Right", "Cursor + WORD");
	a("Shift-Left", "AdjLeft - WORD");
	a("Shift-Right", "AdjRight + WORD");
	a("Ctrl-Left", "AdjRight - WORD");
	a("Ctrl-Right", "AdjLeft + WORD");
	a("Alt-Left", "Hop - WORD");
	a("Alt-Right", "Hop + WORD");
	a("Enter", "BreakParagraph 0 WORD");

	a("Up", "Cursor - SENT");
	a("Down", "Cursor + SENT");
	a("Alt-Up", "Hop - SENT");
	a("Alt-Down", "Hop + SENT");

	a("INSERT", "Insert", nchar);
	a("Tab", "Mode 0 SENT", nsent);

	curAct = nsent;
	a("MouseClicked1", "MouseSetCursor 0 SENT");
	a("Backspace", "BackSpace 0 SENT");
	a("Delete", "Delete 0 SENT");
	a("Alt-Backspace", "Delete 0 SENT");
	a("Ctrl-P", "Tunnel - SENT"); // P for push
	a("Ctrl-T", "Transcopy - SENT");
	a("Left", "Cursor - SENT");
	a("Right", "Cursor + SENT");
	a("Shift-Left", "AdjLeft - SENT");
	a("Shift-Right", "AdjRight + SENT");
	a("Ctrl-Left", "AdjRight - SENT");
	a("Ctrl-Right", "AdjLeft + SENT");
	a("Alt-Left", "Hop - SENT");
	a("Alt-Right", "Hop + SENT");
	a("Enter", "BreakParagraph 0 SENT");

	a("Up", "Cursor - PARA");
	a("Down", "Cursor + PARA");
	a("Alt-Up", "Hop - PARA");
	a("Alt-Down", "Hop + PARA");

	a("INSERT", "Insert", nchar);
	a("Tab", "Mode 0 CHAR", nchar);

/*
	curAct = npara;
	a("MouseClicked1", "MouseSetCursor 0 PARA");
	a("Backspace", "BackSpace 0 PARA");
	a("Delete", "Delete 0 PARA");
	a("Alt-Backspace", "Delete 0 PARA");
	a("Left", "Cursor - PARA");
	a("Right", "Cursor + PARA");
	a("Shift-Left", "AdjLeft - PARA");
	a("Shift-Right", "AdjRight + PARA");
	a("Ctrl-Left", "AdjRight - PARA");
	a("Ctrl-Right", "AdjLeft + PARA");
	a("Alt-Left", "Hop - PARA");
	a("Alt-Right", "Hop + PARA");

	a("INSERT", "Insert");
	a("Tab", "Mode 0 CHAR", nchar);
 */

	curAct = nchar;
	a("MouseClicked1", "MouseSetCursor 0 CHAR");
	a("Left", "Cursor - CHAR");
	a("Right", "Cursor + CHAR");
	a("Backspace", "BackSpace 0 CHAR");
	a("Delete", "Delete 0 CHAR");
	a("Alt-Backspace", "Delete 0 CHAR");
	a("INSERT", "Insert");
	a("Tab", "Mode 0 WORD", nword);
	a("Enter", "BreakParagraph 0 CHAR");
	
	a("Ctrl-C", "PUICopy 0 CHAR");
    
	a("Up", "Cursor - WORD");
	a("Down", "Cursor + WORD");

    }

    static String[][] actions = {
	{ "MouseClicked1", "Nile1.MouseSetCursor" },
	{ "Left", "Nile1.Cursor -" },
	{ "Right", "Nile1.Cursor +" },
	{ "Shift-Left", "Nile1.AdjLeft -" },
	{ "Shift-Right", "Nile1.AdjRight +" },
	{ "Ctrl-Left", "Nile1.AdjRight -" },
	{ "Ctrl-Right", "Nile1.AdjLeft +" },
	{ "Alt-Left", "Nile1.Hop -" },
	{ "Alt-Right", "Nile1.Hop +" },

	{ "INSERT",	   "Nile1.Insert" }
    };

}


// vim: set syntax=java :
