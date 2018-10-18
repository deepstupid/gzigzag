/*   
Actions.java
 *    
 *    Copyright (c) 2000, Benjamin Fallenstein
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
 * Written by Benjamin Fallenstein
 */
package org.zaubertrank;
import org.gzigzag.*;

/** Primitive actions for the Zaubertrank applitude.
 */

public class Actions {
public static final String rcsid = "$Id: Actions.java,v 1.1 2001/04/02 20:29:05 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    static ZZCell getDim(ZZCell any, String id) { 
	return getDim(any.getSpace(), id); 
    }
    static ZZCell getDim(ZZSpace sp, String id) {
	ZZCell c = ZZDefaultSpace.findOnSystemlist(sp, "DimLists", false);
	c = c.s("d.1");
	if(c.t().equals(id)) return c;
	ZZCell res = c.findText("d.2", 1, id);
	if(res == null) { res = c.N("d.2", -1); res.setText(id); }
	return res;
    }
    static void setDims(ZZCell win, String[] id) {
	ZZSpace sp = win.getSpace();
	ZZCell[] cells = new ZZCell[id.length];
	for(int i=0; i<id.length; i++)
	    cells[i] = getDim(sp, id[i]);
	ZZDefaultSpace.setDims(win, cells);
    }

    static ZZCell grammarCurs(ZZSpace sp) {
	ZZCell c = ZZDefaultSpace.findOnSystemlist(sp, "Zaubertrank", true);
	c = c.getOrNewCell("d.1");
	return c;
    }
    static void setGrammarNode(ZZCell win) {
	ZZCell curs = grammarCurs(win.getSpace());
	ZZCursorReal.set(curs, ZZCursorReal.get(win));
    }
    static void gotoGrammarNode(ZZCell win) {
	ZZCell c = grammarCurs(win.getSpace());
	c = ZZCursorReal.get(c);
	if(c == null) {
	    pa("org.zaubertrank.Actions.gotoGrammarNode failed: ");
	    pa("--- no grammar node set");
	} else {
	    ZZCursorReal.set(win, c);
	}
    }

    static ZZCell typeCurs(ZZSpace sp) {
	ZZCell c = ZZDefaultSpace.findOnSystemlist(sp, "Zaubertrank", true);
	c = c.getOrNewCell("d.1");
	c = c.getOrNewCell("d.2");
	return c;
    }
    static void setTypeNode(ZZCell win) {
	ZZCell curs = typeCurs(win.getSpace());
	ZZCursorReal.set(curs, ZZCursorReal.get(win));
    }
    static void gotoTypeNode(ZZCell win) {
	ZZCell c = typeCurs(win.getSpace());
	c = ZZCursorReal.get(c);
	if(c == null) {
	    pa("org.zaubertrank.Actions.gotoTypeNode failed: ");
	    pa("--- no type node set");
	} else {
	    ZZCursorReal.set(win, c);
	}
    }

    static public void viewGrammarNodes(ZZCell win) {
	gotoGrammarNode(win);
	setDims(win, new String[] {
	    Parser.grammardim, 
	    Parser.textgrammardim, 
	    Parser.textrankdim
	});
    }
    static public void viewTexts(ZZCell win) {
	setDims(win, new String[] {
	    Parser.textdim, 
	    Parser.textrankdim, 
	    Parser.textgrammardim
	});
    }
    static public void viewTypeNode(ZZCell win) {
	setDims(win, new String[] {
	    Parser.textdim, 
	    Parser.decdim, 
	    Parser.paramdim
	});
    }
    static public void viewDecNode(ZZCell win) {
	setDims(win, new String[] {
	    Parser.valuedim, 
	    Parser.paramdim, 
	    Parser.decdim
	});
    }
    static public void viewExpNode(ZZCell win) {
	setDims(win, new String[] {
	    Parser.valuedim, 
	    Parser.paramdim, 
	    Parser.sequencedim
	});
    }
    static public void viewSequence(ZZCell win) {
	setDims(win, new String[] {
	    Parser.paramdim, 
	    Parser.sequencedim, 
	    Parser.valuedim
	});
    }
    static public void stdView(ZZCell win) {
        ZZCell home = win.getSpace().getHomeCell();
        ZZCell defDims = ZZDefaultSpace.findDefaultDimlist(home);
       ZZDefaultSpace.setDimsFromList(win, defDims, true);
    }

    static public void mkGrammarNode(ZZCell win) {
	Parser.GrammarNode gn = new Parser.GrammarNode(win.getSpace());
	ZZCursorReal.set(grammarCurs(win.getSpace()), gn.c);
	viewGrammarNodes(win);
    }
    static public void mkText(ZZCell win) {
	ZZCell c = ZZCursorReal.get(win);
	ZZCell res = ZZCursorReal.get(grammarCurs(win.getSpace())).zzclone();
	ZZCursorReal.get(win).insert(Parser.textdim, 1, res);
	ZZCursorReal.set(win, res);
    }
    static public void mkTypeNode(ZZCell win) {
	ZZCell c = ZZCursorReal.get(win).N("d.2");
	ZZCursorReal.set(win, c);
	ZZCursorReal.set(typeCurs(win.getSpace()), c);
    }
    static public void mkDecNode(ZZCell win) {
	ZZCell c = ZZCursorReal.get(typeCurs(win.getSpace()));
	Parser.TypeNode tn = new Parser.TypeNode(c);
	Parser.DecNode dn = new Parser.DecNode(tn, "", new Parser.TypeNode[0]);
	ZZCursorReal.get(win).insert("d.1", 1, dn.c);
	ZZCursorReal.set(win, dn.c);
    }
    static public void mkExpNode(ZZCell win) {
	Parser.DecNode dn = new Parser.DecNode(ZZCursorReal.get(win));
	Parser.ExpNode en = new Parser.ExpNode(dn, new Parser.ExpNode[0]);
	ZZCursorReal.set(win, en.c);
    }
    static public void mkDecRef(ZZCell win) {
	ZZCell c = ZZCursorReal.get(win);
	c = c.N(Parser.textrankdim);
	c.setText("0");
	c.connect(Parser.textgrammardim, 1,
		  ZZCursorReal.get(grammarCurs(win.getSpace())).zzclone());
	ZZCursorReal.set(win, c);
    }

    static public void twoWinExp(ZZCell win) {
	ZZCell other = win.s("d.ctrlview");
	if(other == null) other = win.s("d.ctrlview", -1);
	Parser.DecNode dn = new Parser.DecNode(ZZCursorReal.get(other));
	Parser.ExpNode en = new Parser.ExpNode(dn, new Parser.ExpNode[0]);
	ZZCursorReal.get(win).insert(Parser.valuedim, 1, en.c);
    }
    static public void remove(ZZCell win) {
	ZZCell c = ZZCursorReal.get(win);
	c = c.h(Parser.valuedim);
	ZZCursorReal.set(win, c);
	if(c.s(Parser.valuedim) != null) c.disconnect(Parser.valuedim, 1);
    }

    static public void move(ZZCell win, int dir) {
	ZZCell c = ZZCursorReal.get(win);
	if(c.s(Parser.paramdim, -1) == null) return;
	c = c.s(Parser.paramdim, dir);
	if(c.s(Parser.paramdim, -1) != null) ZZCursorReal.set(win, c);
    }
    static public void movein(ZZCell win) {
	ZZCell c = ZZCursorReal.get(win);
	c = c.h(Parser.valuedim, 1);
	c = c.s(Parser.paramdim, 1);
	if(c != null) ZZCursorReal.set(win, c);
    }
    static public void moveout(ZZCell win) {
	ZZCell c = ZZCursorReal.get(win);
	c = c.h(Parser.valuedim);
	c = c.h(Parser.paramdim);
	c = c.h(Parser.valuedim);
	ZZCursorReal.set(win, c);
    }

    static public void click(ZZCell win, Flob f) {
    	if( !(f instanceof TokenFlob)) { p("No TkFlob"); return; }
    	TokenFlob tk = (TokenFlob)f;
    	if(tk.tkHandle == null) { p("No handle in TkFlob"); return; }
    	ZZCursorReal.set(win, tk.tkHandle);
    }
	

    static public void action(ZZCell win, String id, Flob f) {
	if(id.equals("SETGRAMMAR")) setGrammarNode(win);
	else if(id.equals("GOTOGRAMMAR")) gotoGrammarNode(win);
	else if(id.equals("SETTYPE")) setTypeNode(win);
	else if(id.equals("GOTOTYPE")) gotoTypeNode(win);

	else if(id.equals("VIEWGRAMMAR")) viewGrammarNodes(win);
	else if(id.equals("VIEWTEXTS")) viewTexts(win);
	else if(id.equals("VIEWTYPE")) viewTypeNode(win);
	else if(id.equals("VIEWDEC")) viewDecNode(win);
	else if(id.equals("VIEWEXP")) viewExpNode(win);
	else if(id.equals("VIEWSEQ")) viewSequence(win);
	else if(id.equals("STDVIEW")) stdView(win);

	else if(id.equals("MKGRAMMAR")) mkGrammarNode(win);
	else if(id.equals("MKTEXT")) mkText(win);
	else if(id.equals("MKTYPE")) mkTypeNode(win);
	else if(id.equals("MKDEC")) mkDecNode(win);
	else if(id.equals("MKDECREF")) mkDecRef(win);
	else if(id.equals("MKEXP")) mkExpNode(win);
	
	else if(id.equals("TWOWINEXP")) twoWinExp(win);
	else if(id.equals("REMOVE")) remove(win);
	
	else if(id.equals("MOVELEFT")) move(win, -1);
	else if(id.equals("MOVERIGHT")) move(win, 1);
	else if(id.equals("MOVEIN")) movein(win);
	else if(id.equals("MOVEOUT")) moveout(win);
	
	else if(id.equals("CLICK")) click(win, f);
	
	else throw new ZZError("Unknown Zaubertrank action "+id);
    }
}
