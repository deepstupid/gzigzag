/*   
ArchimedesClang.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
package org.gzigzag.module;
import org.gzigzag.*;
import org.gzigzag.clang.*;
import java.awt.*;

/** A module interface to the Archimedes Procedural Layer executor/evaluator.
 */

public class ArchimedesClang {
String rcsid = "$Id: ArchimedesClang.java,v 1.1 2001/04/16 10:37:59 bfallenstein Exp $";
    static public ZZModule module = new ZZModule() {
	public void action(String id,
		ZZCell code, 
		ZZCell target,
		ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
	    if(target == null) target = ZZCursorReal.get(view.getViewcell());
				
	    if(id.equals("Run")) {
		// Evaluate the Archimedes expression connected negwards
		// on d.expression.
		ZZCell expCell = code.h("d.expression", -1);
		if(expCell == null)
		    throw new SyntaxError("No expression found for "+code);
		Expression e = new Expression(expCell);
		Namespace n = new Namespace(expCell.getSpace());
		Archimedes.evaluateExpression(e, n);
	    }	
	    
	    else if(id.equals("Install")) {
		installArchimedes(target.getSpace());
	    }
	
	    else if(id.equals("CreateExpression")) {
		ZZCell from = ZZCursorReal.get(cview.getViewcell());
		Expression e = new Expression(target.getSpace(), from);
		e.connect(target);
		
		target.excise("d.cellview");
		getZaubertrankCellView(target.getSpace())
			.insert("d.cellview", 1, target);
	    }
	
	    else if(id.equals("MakeAction")) {
		target.setText("ArchimedesClang.Run");
	    }
	
	    else
		throw new ZZError("ArchimedesClang action "+id+" not recognized.");
	}

	public ZOb newZOb(String id) { return null; }
    };

    public static void installArchimedes(ZZSpace sp) {
	ZZCell c = sp.getHomeCell().findText("d.2", 1, "Archimedes");
	if(c == null) {
	    c = sp.getHomeCell().h("d.2", 1).N("d.2");
	    c.setText("Archimedes");
	}
	c = c.getOrNewCell("d.1", 1);
	installArchimedesPrimitives(c);
	c = c.getOrNewCell("d.1", 1);
	installArchimedesBindings(c);
    }

    public static void installArchimedesPrimitives(ZZCell start) {
	ZZSpace sp = start.getSpace();
	start.setText("Primitives");
	start.excise("d.2");
	String[] prims = AllPrimitives.standardPrimitives;
	for(int i=0; i<prims.length; i++) {
	    ZZCell p = AllPrimitives.getCell(sp, prims[i]);
	    start.h("d.2", 1).connect("d.2", 1, p.zzclone());
	}
    }

    public static final ZZCell addAct(ZZCell start, String key, String act, 
				      ZZCell mode) {
	ZZCell c = start.N("d.2"); c.setText(key);
	ZZCell d = c.N("d.1"); d.setText(act);
	if(mode != null) mode.insert("d.3", 1, d);
	return c;
    }

    public static void installArchimedesBindings(ZZCell start) {
	ZZSpace sp = start.getSpace();

	start.setText("Bindings");
	start.excise("d.2");
	start.excise("d.3");

	ZZCell c = start.getOrNewCell("d.color", 1);
        c.setText(String.valueOf(new Color(180, 0, 255).getRGB()));

	ZZCell normal = ZZDefaultSpace.findOnSystemlist(start.getSpace(),
		"Bindings", true).getOrNewCell("d.1", 1);
	ZZCell ref = addAct(normal, "Ctrl-Z", "", start);
	
	// Inherit standard mode key bindings
	ref.insert("d.3", 1, start.N("d.2"));

	addAct(start, "Enter", "ArchimedesClang.CreateExpression", null);
	addAct(start, "MouseClicked3", "ArchimedesClang.CreateExpression", null);
	addAct(start, "Esc", "", normal);
	addAct(start, " ", "", normal);
	addAct(start, "Ctrl-Z", "", normal);
	addAct(start, "Ctrl-A", "ArchimedesClang.MakeAction", null);
    }

    public static ZZCell getZaubertrankCellView(ZZSpace sp) {
	ZZCell home = sp.getHomeCell();
	ZZCell c = home.s("d.zaubertrank-cellview");
	if(c != null) return c;
	
	c = home.N("d.zaubertrank-cellview");
	c.setText("The zaubertrank's cellview");
	
	c = c.N("d.1");
	c.setText("ZaubertrankCellView");
	
	c.N("d.1");
	
	return c.s("d.1", -1);
    }
}