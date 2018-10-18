/*   
AwtNile.java
 *    
 *    Copyright (c) 2000-2001 Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2000-2001 Kimmo Wideroos
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
 * Written by Kimmo Wideroos (inspired by Benjamin Fallenstein's notemap module)
 */

/** Nile related awt-stuff
*/

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

public class AwtNile {
public static final String rcsid = "";
    public static boolean dbg = true;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }


    public static class Tokenizer {

	int dir;
	Nile2Unit unit;
	String short_id;
	public String[] toks = new String[3];

	public Tokenizer() {}

	public Tokenizer(String id) {
	    set_id(id);
	}

        public void set_id(String id) {
	    StringTokenizer st = new StringTokenizer(id);
	    int toks_size = st.countTokens();
            for(int i=0; i<toks_size; i++)
            toks[i] = st.nextToken();
            
            dir = 0;
            unit = null;
            if(toks_size > 1) {
                if(toks[1].equals("+")) dir = 1;
                else if(toks[1].equals("-")) dir = -1;
                
                if(toks_size > 2) {
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
            
            short_id = toks[0];
	}
	public int dir() { return dir; }
	public Nile2Unit unit() { return unit; }
	public void set_unit(Nile2Unit u) {
	    if(toks[2] == null) return;
	    unit = u;
	    toks[2] = u instanceof Nile2Unit.Char ? "CHAR" :
		u instanceof Nile2Unit.Word ? "WORD" :
		u instanceof Nile2Unit.Sentence ? "SENT" :
		u instanceof Nile2Unit.Paragraph ? "PARA" : "";
	}
	public String short_id() { return short_id; }
	public String id() {
	    String id = new String();
	    for(int i=0; i<3; i++) {
		if(toks[i] == null) break; 
		id = id+toks[i]+" ";
	    }
	    return id;
	}

    }

    protected static Tokenizer tokenizer;


    // set cursor, swap if possible 
    public static void setInputCursors(int which, ZZCell target, 
                                       AwtCursors awtcursors) {
        AwtCursorIdentifier c1=null, c2=null;
        if(which==1) {
            c1 = AwtCursors.c_input1;
            c2 = AwtCursors.c_input2;
        }
        if(which==2) {
            c1 = AwtCursors.c_input2;
            c2 = AwtCursors.c_input1;
        }
        if(c1==null || c2==null) {
	    throw new ZZError("AwtNile: cursor null!!");
	}
        
        ZZCell i1 = awtcursors.get(c1);
        if(i1!=null && i1.equals(target)) return;
        if(i1!=null) awtcursors.set(c2, i1);
        awtcursors.set(c1, target);
        if(i1!=null) {
            i1.excise("d.nile-wins");
            target.connect("d.nile-wins", 1, i1);
            target.connect("d.nile-wins", -1, i1);
        }
    }

    public static ZZCell spancell(ZZCell artef) {
	return artef.h("d.clone").s("d.1");
    }

    public static void initSpanCursors(ZZCell artef) {
	ZZCell sp_cur = artef.h("d.clone");
	Nile2Iter iter = new Nile2Iter();
	if(_init_span_cursors(sp_cur)) {
	    Nile2Ops.breakParagraph(new Nile2Unit.Char(), sp_cur);
	    iter.set(Nile2Ops.curs(sp_cur));
	    iter.streamEnd(1);
	}
	/*
	if(_init_span_cursors(sp_cur)) {
	    Nile2Ops.breakParagraph(new Nile2Unit.Char(), sp_cur);
	   iter.set(Nile2Ops.curs(sp_cur));
	   iter.streamEnd(1);
       }
	*/
    }

    private static boolean _init_span_cursors(ZZCell sp_cur) {
	if(Nile2Ops.curs(sp_cur).get() != null &&
	   Nile2Ops.endcurs(sp_cur).get() != null) 
	    return false;
	ZZCell spans = sp_cur.getOrNewCell("d.1");
	Nile2Ops.curs(sp_cur).set(spans);
	Nile2Ops.curs(sp_cur).setOffs(0);
	Nile2Ops.endcurs(sp_cur).set(spans);
	Nile2Ops.endcurs(sp_cur).setOffs(0);
        return true;
    }

    static public ZZModule module = new ZZModule() {
        public void action(String id,
                           ZZCell code, 
                           ZZCell target,
                           ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
                               
          
            ZZCell accursed = ZZCursorReal.get(view.getViewcell());

            AwtCursors awtcursors = new AwtCursors(accursed);
        
            boolean split = false;

            ZZCell viewCell = awtcursors.get(AwtCursors.c_input1);
            ZZCell otherCell = awtcursors.get(AwtCursors.c_input2);

            if(viewCell==null) return;

	    viewCell = viewCell.h("d.clone");

	    if(otherCell!=null) 
		otherCell = otherCell.h("d.clone");

	    ZZCell vspans=null, ospans=null;

            if(ZZCursorReal.get(viewCell) == null)
		initSpanCursors(viewCell);

            if(otherCell!=null) 
                if(ZZCursorReal.get(otherCell) == null)
		    initSpanCursors(otherCell);

	    ZZCursor tfcur=null, tfendcur=null;
	    ZZCursor ttcur=null, ttendcur=null;
            //ZZCell transFrom = otherCell, transTo = viewCell;
            ZZCell transFrom, transTo;
	    transFrom = viewCell;
	    transTo = otherCell;
	    if(transTo!=null) { 
		tfendcur = Nile2Ops.endcurs(transFrom);
		tfcur = Nile2Ops.curs(transFrom);
		Nile2Iter tfiter = new Nile2Iter(tfcur);
		Nile2Iter tfenditer = new Nile2Iter(tfendcur);
		if(tfenditer.isOrdered(tfiter)) {
		    ZZCursor swap = tfcur;
		    tfcur = tfendcur;
		    tfendcur = swap;
		}
	    }
	    /*
		if(ttendcur.get() != null) {
		    //pa("ttendcur.get() != null");
		    tfcur = Nile2Ops.curs(transFrom);
		    tfendcur = Nile2Ops.endcurs(transFrom);
		    Nile2Iter tfiter = new Nile2Iter(tfcur);
		    Nile2Iter tfenditer = new Nile2Iter(tfendcur);
		    // XXX not finished!!!!
		    if(tfenditer.isOrdered(tfiter)) {
			//pa("tfenditer.isOrdered(tfiter)");
			// if viewcell does not have content, use othercell 
			// as source when tunneling and transcluding
			ttcur = Nile2Ops.curs(transTo);
			Nile2Iter ttiter = new Nile2Iter(ttcur);
			Nile2Iter ttenditer = new Nile2Iter(ttendcur);
			if(ttiter.isOrdered(ttenditer)) {
			    //pa("ttiter.isOrdered(ttenditer)");
			    ZZCell swap = transFrom;
			    transFrom = transTo;
			    transTo = swap;
			    tfendcur = ttendcur;
			}
		    }
		}
	    }
	    */
            //p("Nile action: id: '"+id+"' key: '"+key);
            //pa("vspans = "+vspans);
            //pa("ospans = "+ospans);
	    
	    
	    tokenizer = new Tokenizer(id);
	    int dir = tokenizer.dir();
	    Nile2Unit unit = tokenizer.unit();
	    id = tokenizer.short_id();
            
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
                if(otherCell != null && tfendcur.get() != null) {
                    //Nile2Ops.tunnel(unit, otherCell, viewCell, dir, false);
                    Nile2Ops.tunnel(unit, transFrom, transTo, dir, false);
                }
            }
            else if(id.equals("Transcopy")) {
                if(otherCell != null && tfendcur.get() != null) {
                    //Nile2Ops.tunnel(unit, otherCell, viewCell, dir, true);
		    /*int[] inds = AwtOps.subSpansLengths(transFrom);
		    pa("a1="+inds[0]);
		    pa("a2="+inds[1]);
		    pa("a3="+inds[2]);*/
                    Nile2Ops.tunnel(unit, transFrom, transTo, dir, true);
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
            
            ZZCursor cur = null;
	    Object awtobj = null;
            if(pt != null) {
                Object ob = xi.getObjectAt(pt.x, pt.y);
                if(ob instanceof ZZCursor) cur = (ZZCursor)ob;
		awtobj = AwtUtil.getAwtObject(view.getViewcell());
            }

            //pa("[AwtNile] cur: "+cur);
            //pa("[AwtNile] awtobj: "+awtobj);
            
            if(id.equals("MouseSetCursor")) {
                if(cur != null) {
                    p("Setting cursor from mouse! Unit: "+unit);
                    if(AwtUtil.isPartOfspan(cur.get(), viewCell))
                        Nile2Ops.setCursor(unit, viewCell, cur);
                    else if(AwtUtil.isPartOfspan(cur.get(), otherCell))
                        Nile2Ops.setCursor(unit, otherCell, cur);
                }
                if(awtobj != null && awtobj instanceof AwtAccursable) {
		    ZZCell ac = ((AwtAccursable)awtobj).getCell();
                    if(ac.equals(viewCell)) setInputCursors(1, viewCell,
                                                            awtcursors);
                    if(ac.equals(otherCell)) setInputCursors(1, otherCell,
                                                             awtcursors);
                }
                
            }
        }
    };   
     
    // check current action binding, whether Nile is on. 
    static public boolean nileStatus(ZZCell vc) {
        ZZCell curb = vc.h("d.bind", 1, true);
        if(curb!=null) {
            String mode = ZZCursorReal.get(curb).getText();
            if(mode.indexOf("Nile") >= 0)
                return true; 
        }
        return false;
    }

    // This is really hacky part...
    static ZZCell curAct;
    static void a(String key, String act) { a(key, act, null); }
    static void a(String key, String act, ZZCell next) {
	curAct = curAct.N("d.2", 1);
	curAct.setText(key);
	ZZCell n = curAct.N("d.1", 1);
	n.setText("AwtNile." + act);
	if(next != null)
	    next.insert("d.3", 1, n);
    }

    static void awt_nile_actions(ZZCell start) {
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
}
