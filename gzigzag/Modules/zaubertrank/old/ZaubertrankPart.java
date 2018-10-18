/*   
ZaubertrankPart.java
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
import java.util.*;

/** A spacepart for the zaubertrank.
 * Currently connects tokens in Reverse Polish Notation, and has cells whose
 * content is the computed result of a Zaubertrank function.
 */

public class ZaubertrankPart extends ZZROSpacePart {
public static final String rcsid = "$Id: ZaubertrankPart.java,v 1.1 2001/04/02 20:29:05 bfallenstein Exp $";
    public static boolean dbg = false;
    private static final void p(String s) { if(dbg) System.out.println(s); }
    private static final void pa(String s) { System.out.println(s); }

    public ZaubertrankPart(ZZSpace space, String id) {
	super(space, id);
    }

    public static class Result {
	String codeID;
	int nr;
	public Result(String c, int nr) { codeID = c; this.nr = nr; }
    }

    public Object parseIDPart(String idPart) {
	if(idPart.substring(0, 7).equals("result-")) {
	    String rest = idPart.substring(7);
	    int idx = rest.indexOf("-");
	    if(idx < 0 || idx+1 == rest.length()) return null;
	    String nr = rest.substring(0, idx);
	    String codeID = rest.substring(idx+1);
	    try {
		return new Result(codeID, Integer.parseInt(nr));
	    } catch(NumberFormatException e) {
		return null;
	    }
	} else
	    return null;
    }

    public String generateIDPart(Object parsed) {
	if(parsed instanceof Result) {
	    Result r = (Result)parsed;
	    return "result-" + r.nr + "-" + r.codeID;
	} else
	    throw new ZZError("Not a ZaubertrankPart parsed ID object!");
    }

    public String getText(ZZCellHandle c) {
	if(c.parsedID instanceof Result) {
	    Result r = (Result)c.parsedID;
	    ZZCell code = space.getCellByID(r.codeID);
	    pa("code "+code);
	    Object res;
	    try {
	        res = Evaluator.eval(code, new Data(c));
	    } catch(Throwable t) {
		// ZZLogger.exc(t);
		return t.getMessage();
	    }
	    return Data.s(res);
	} else
	    return "";
    }

    public ZZDimension getDim(String name) {
	if(name.equals("result")) return new ResultDimension();
	else if(name.equals("rpn")) return new ReversePolishNotation();
	return null;
    }

    public class ResultDimension extends ZZRODimension {
	public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
	    Result r = null;
	    if(c.parsedID instanceof Result) r = (Result)c.parsedID;
	    
	    if(r == null) {
		if(steps <= 0) return null;
		r = new Result(c.getID(), steps);
		return (ZZCellHandle)space.getCellByID(ZaubertrankPart.this, 
						       generateID(r), r);
	    } else {
		r = new Result(r.codeID, r.nr + steps);
		if(r.nr < 0) return null;
		else if(r.nr == 0)
		    return (ZZCellHandle)space.getCellByID(r.codeID);
		else 
		    return (ZZCellHandle)space.getCellByID(ZaubertrankPart.this,
					 generateID(r), r);
	    }
	}
    }

    public static ZZCell parent(ZZCell c, ZZObs o) throws SyntaxError {
	c = c.s(Parser.valuedim, -1, o);
	if(c == null) return null;
	c = c.h(Parser.paramdim, -1, true, o);
	if(c == null) throw new SyntaxError("Not connected to parent!");
	return c;
    }
    private static ZZCell sibling(ZZCell c, int dir, ZZObs o) 
							throws SyntaxError {
	c = c.s(Parser.valuedim, -1, o);
	if(c == null) return null; // XXX syntax error? has no siblings at all!
	c = c.s(Parser.paramdim, dir, o);
	if(dir < 0 && c == null) throw new SyntaxError("was parent already");
	else if(dir > 0 && c == null) return null;
		// i.e., this was the last sibling
	else if(dir < 0 && c.s(Parser.paramdim, -1, o) == null) return null;
		// i.e., this was the first sibling
	c = c.s(Parser.valuedim, 1, o);
	if(c == null) throw new MissingTermError("Term's missing");
	return c;
    }
    public static ZZCell prevSibling(ZZCell c, ZZObs o) throws SyntaxError {
	return sibling(c, -1, o);
    }
    public static ZZCell nextSibling(ZZCell c, ZZObs o) throws SyntaxError {
	return sibling(c, 1, o);
    }
    public static ZZCell firstChild(ZZCell c, ZZObs o) throws SyntaxError {
	c = c.s(Parser.paramdim, 1, o);
	if(c == null) return null;
	c = c.s(Parser.valuedim, 1, o);
	if(c == null) throw new MissingTermError("Term's missing");
	return c;
    }
    public static ZZCell lastChild(ZZCell c, ZZObs o) throws SyntaxError {
	c = c.h(Parser.paramdim, 1, true, o);
	if(c == null) return null;
	c = c.s(Parser.valuedim, 1, o);
	if(c == null) throw new MissingTermError("Term's missing");
	return c;
    }

    public static ZZCell firstDescendant(ZZCell c, ZZObs o) throws SyntaxError {
	LoopDetector l = new LoopDetector();
	ZZCell d = c;
	while(true) {
	    d = firstChild(c, o);
	    if(d == null) return c;
	    c = d;
	    l.detect(c);
	}
    }

    public static ZZCell rpnPrev(ZZCell c, ZZObs o) throws SyntaxError {
	ZZCell d = lastChild(c, o);
	if(d != null) return d;
	LoopDetector l = new LoopDetector();
	while(true) {
	    d = prevSibling(c, o);
	    if(d != null) return d;
	    l.detect(c);
	    c = parent(c, o);
	    if(c == null) return null;
	}
    }

    public static ZZCell rpnNext(ZZCell c, ZZObs o) throws SyntaxError {
	ZZCell d = nextSibling(c, o);
	if(d == null) return parent(c, o);
	else return firstDescendant(d, o);
    }

    public class ReversePolishNotation extends ZZRODimension {
	public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
	    // Sanity check: can this be a tree node?
	    if(c.s(Parser.valuedim, 1, o) != null) return null;
	    if(c.s(Parser.paramdim, -1, o) != null) return null;
	// Until we have syntax error cells, let the errors be throws.
	//    try {
		if(steps > 0)
		    for(int i=0; i<steps; i++)
			c = (ZZCellHandle)rpnNext(c, o);
		else
		    for(int i=0; i>steps; i--)
			c = (ZZCellHandle)rpnPrev(c, o);
		
		// Another sanity check
		if(c != null && (c.s(Parser.valuedim, 1, o) != null ||
		                 c.s(Parser.paramdim, -1, o) != null))
			throw new SyntaxError("Target not a tree node!");
		
		return c;
	//    } catch(SyntaxError e) {
	//	ZZLogger.exc(e);
	//	return null;
	//    }
	}
    }
}

