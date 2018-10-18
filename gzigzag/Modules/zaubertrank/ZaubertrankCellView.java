/*   
ZaubertrankCellView.zob
 *    
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

package org.gzigzag;
import org.gzigzag.clang.*;
import java.util.*;
import java.awt.*;

/** A cell view for showing one zaubertrank statement/entity/sentence etc.
 *  This is assembled from a number of tokens.
 */
 
public final class ZaubertrankCellView extends FTextCellView {
public static final String rcsid = "$Id: ZaubertrankCellView.java,v 1.7 2001/04/23 21:22:54 bfallenstein Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) ZZLogger.log(s); }
    protected static void pa(String s) { ZZLogger.log(s); }

    public static String zttext = "d.zt-text";
    public static String ztparams = "d.1";
    public static String zttype = "d.zt-type";



    ZZCursorVirtual[] append(ZZCursorVirtual[] path, ZZCell c) {
	ZZCursorVirtual[] nu = new ZZCursorVirtual[path.length+1];
	System.arraycopy(path, 0, nu, 0, path.length);
	nu[nu.length-1] = new ZZCursorVirtual(c);
	return nu;
    }



    /** Add the FText parts for an expression to the vector. 
     *  @param bg The background color for this expression.
     *  @param subBg The background color for parameters of this expression.
     */
    void addExpression(Vector v, Expression e, ZZCursorVirtual[] path,
			     Color bg, Color subBg, int scale) {
	p("add expression "+e.main);
		
	ZZCell token = e.main.getRootclone();
	
	// Add the token's text.
	for(ZZCell c = token.s(zttext); c != null; c = c.s(zttext)) {
	    if(c.equals(token)) throw new ZZInfiniteLoop("looping zttext");
	
	    ZZCell root = c.getRootclone();
	    if(c.equals(root))
		addCellPart(v, c, path, bg, scale);
	    else {
		ZZCell param = ParamTemplate.rootToExpression(root, e);
		addParameter(v, param, root, path, subBg, scale);
	    }
	}
    }



    void addParameter(Vector v, ZZCell param, ZZCell variable,
			    ZZCursorVirtual[] path, Color bg, int scale) {
	p("add parameter "+param);
	
	path = append(path, param);
	ZZCell exp = param.h("d.expression", -1, true);
	
	// The background color for sub-expressions of this expression
	Color subBg = bg;

	// See whether there's a cursor accursing the token, and take its color.
	Color col = ZZCursorReal.getAccursedColor(param);
	if(col != null) {
	    bg = col;
	    subBg = ZZUtil.mix(bg, Color.white, 64);
	}

	if(exp != null)
	    addExpression(v, new Expression(exp), path, bg, subBg, scale);
	else {
	    // QUICK fix to get literals shown.
	    
	    if(param.t().equals(""))
		addTypeQuestion(v, param.getSpace(), param, variable, path, 
				bg, scale);
	    else {
		addCellPart(v, param, path, bg, scale);
	    }
	}
    }



    /** Get the roottype of this space, the type of which all things are.
     *  Note that it's unclear whether there will be something like a "root
     *  type" when types actually mean something. Currently they are just
     *  tags for the questions asked if a parameter is missing (i.e., we look
     *  "of which type is this parameter", then ask the question associated
     *  with that type). If we cannot find a type (i.e., there's no type
     *  connected with this param), we use the root type, which just asks
     *  "what?".
     */
    ZZCell getRootType(ZZSpace sp) {
	ZZCell home = sp.getHomeCell(), res;
	if((res = home.s("d.zaubertrank-roottype")) != null)
	    return res;
	res = home.N("d.zaubertrank-roottype");
	res.setText("(what?)");
	return res;
    }



    void addTypeQuestion(Vector v, ZZSpace sp, ZZCell param, ZZCell variable,
			       ZZCursorVirtual[] path, Color bg, int scale) {
	p("add type question for param "+param+
	  ", path length "+path.length);

	if(param != null) {
	    Color col = ZZCursorReal.getAccursedColor(param);
	    if(col != null) bg = col;
	}
	
	ZZCell type = null;
	if(variable != null)
	    type = variable.h(zttype, true);
	if(type == null)
	    type = getRootType(sp);
	addCellPart(v, type, path, bg, scale);
    }



    /** Get the FText for this cell at the given scale.
     *  scale == (int)(fract * 1000).
     */
    protected FText getFText(ZZCell c, int scale) {
	Vector v = new Vector();
	ZZCursorVirtual[] path = new ZZCursorVirtual[] {
	    new ZZCursorVirtual(c)
	};
	
	ZZCell exp = c.s("d.expression", -1);
	if(exp != null)
	    addExpression(v, new Expression(exp), path, null, null, scale);
	else
	    addTypeQuestion(v, c.getSpace(), null, null, path, null, scale);
	return new FText(v);
    }



    /** Add a cell part to the vector of FText parts. */
    void addCellPart(Vector v, ZZCell c, Object hdl, Color bg, int scale) {
	p("add cell part for text cell "+c+", handle "+hdl);
	v.addElement(new FText.CellPart(c, hdl, 0, -1, f(scale), fm(scale),
					bg, null));
    }
}
