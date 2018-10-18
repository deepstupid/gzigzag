/*   
Parser.java
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

/** A parser for zaubertrank programs.
 *  Currently micro-level: the biggest structure recognized is one statement.
 */

public class Parser {
public static final String rcsid = "$Id: Parser.java,v 1.1 2001/04/02 20:29:05 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    static Object NULL = new Object();

    static public final String paramdim = "d.2"; // parameter list dim
    static public final String valuedim = "d.1";
    static public final String recorddim = "d.1";
    static public final String sequencedim = "d.zaubertrank-list";
    static public final String textdim = "d.zaubertrank-txt-alt";
    static public final String textrankdim = "d.2";
    static public final String textgrammardim = "d.1";
    static public final String grammardim = "d.zaubertrank-grammar-list";
    static public final String decdim = "d.zaubertrank-declarations";

    /** A reference to an ExpNode param in a certain grammatical form. */
    static public final class DecRef {
	int i;
	GrammarNode g;

	public DecRef(int i, GrammarNode g) { this.i = i; this.g = g; }
    }

    /** A reference to a certain ExpNode in a certain grammatical form.
     *  The difference to DecRef is that DecRef contains only a param number
     *  and a GrammarNode; additionally, an ExpNode is needed to read the
     *  actual text.
     */
    static public final class ExpRef {
        ExpNode e;
        GrammarNode g;
	
        public ExpRef(ExpNode e, GrammarNode g) { this.e = e; this.g = g; }
        public Object[] getText() { return e.getText(g); }
    }

    static public final class TypeRef {
        TypeNode t;
        GrammarNode g;
	
        public TypeRef(TypeNode t, GrammarNode g) { this.t = t; this.g = g; }
        public ZZCell[] getText() { return t.getText(g); }
    }

    static public class Node {
	public ZZCell c;
	public boolean equals(Node other) { return c.equals(other.c); }
    }

    /** A node declaring a grammatical way to utter a type.
     *  Example: English noun ("the person" // "which person?")
     *  Example: English statement ("the ball is red" // "what is true?")
     *  Example: English genitive ("the person's" // "whose?")
     *  etc.
     */
    static public class GrammarNode extends Node {
	public GrammarNode(ZZCell c) {
	    this.c = c.getRootclone();
	    if(!this.c.h(grammardim).equals(this.c.getSpace().getHomeCell()))
		throw new ZZError("Attempt to create GrammarNode from " +
				  "non-grammar cell "+this.c);
	}
	public GrammarNode(ZZSpace sp) { 
	    this.c = sp.getHomeCell().N(grammardim, 1);
	}
    }

    static ZZCell[] readText(ZZCell c, GrammarNode g) {
	ZZCell root = c.intersect(textdim, 1, g.c, "d.clone", 1);
	p("Parse intersect "+c+" and "+g.c+" gives "+root);
	if(root == null) return null;
	return root.readRank(textrankdim, 1, false);
    }
    static void writeText(ZZCell c, GrammarNode g, ZZCell[] tc) {
	ZZCell here = g.c.zzclone();
	c.insert(textdim, 1, here);
	for(int i=0; i<tc.length; i++) {
	    here.insert(textrankdim, 1, tc[i]);
	    here = tc[i];
	}
    }

    /** A node declaring a data type. */
    static public class TypeNode extends Node {
	public TypeNode(ZZCell c) { this.c = c.getRootclone(); }
	public TypeNode(ZZSpace sp) { this.c = sp.getHomeCell().N(); }
	public ZZCell[] getText(GrammarNode g) {
	    return readText(c, g);
	}
	public void setText(GrammarNode g, ZZCell[] tc) {
	    writeText(c, g, tc);
	}
    }

    /** A node declaring a variable or a function of a data type. */
    static public class DecNode extends Node {
	TypeNode type;
	TypeNode[] params;
	
	/** Create from structure */
	public DecNode(ZZCell c) {
	    this.c = c;
	    type = new TypeNode(c.h(decdim));
	    ZZCell[] pcells = c.readRank(paramdim, 1, false);
	    params = new TypeNode[pcells.length];
	    for(int i=0; i<pcells.length; i++)
		params[i] = new TypeNode(pcells[i]);
	}
	
	/** Create anew, to put into the structure */
	public DecNode(TypeNode type, String title, TypeNode params[]) {
	    this.type = type;
	    c = type.c.N(decdim);
	    if(title != null) c.setText(title);
	    ZZCell here = c;
	    for(int i=0; i<params.length; i++) {
		ZZCell there = params[i].c.zzclone();
		here.insert(paramdim, 1, there);
		here = there;
	    }
	}

	/** Read the text for a given GrammarNode.
	 *  Returns an Object[] array where the Objects are either ZZCells
	 *  (which contain a piece of the text) or DecRef instances containing
	 *  an int and a GrammarNode as reference to a param.
	 */
	public Object[] getText(GrammarNode g) {
	    ZZCell[] rank = readText(c, g);
	    if(rank == null) return null;
	    Object[] res = new Object[rank.length];
	    for(int i=0; i<rank.length; i++) {
		ZZCell gr = rank[i].s(textgrammardim, 1);
		if(gr == null) res[i] = rank[i];
		else res[i] = new DecRef(Integer.parseInt(rank[i].t()),
					 new GrammarNode(gr));
	    }
	    return res;
	}
	
	/** Write the text for a given GrammarNode.
	 *  Returns an Object[] array where the Objects are either ZZCells
	 *  (which contain a piece of the text) or DecRef instances containing
	 *  an int and a GrammarNode as reference to a param.
	 */
	public void setText(GrammarNode g, Object[] text) {
	    ZZCell[] rank = new ZZCell[text.length];
	    for(int i=0; i<text.length; i++) {
		if(text[i] instanceof ZZCell) rank[i] = (ZZCell)text[i];
		else if(text[i] instanceof DecRef) {
		    DecRef ref = (DecRef)text[i];
		    rank[i] = c.N();
		    rank[i].setText(""+ref.i);
		    rank[i].connect(textgrammardim, 1, ref.g.c.zzclone());
		} else
		    throw new ZZError("Strange object: "+text[i]);
	    }
	    writeText(c, g, rank);
	}
    }

    /** A node representing an expression.
     *  An expression consists of a reference to a DecNode (which references
     *  a data type), and references to ExpNodes as parameters.
     */
    static class ExpNode extends Node {
	public ExpNode[] params;
	public DecNode dec;

	/** Create from structure */
	public ExpNode(ZZCell c) {
	    this.c = c;
	    dec = new DecNode(c.getRootclone());
	    int i = 0;
	    for(ZZCell var=c.s(paramdim); var!=null; var=var.s(paramdim)) {
		i++;
	    }
	    params = new ExpNode[i];
	    i = 0;
	    for(ZZCell var=c.s(paramdim); var!=null; var=var.s(paramdim)) {
	        ZZCell val = var.s(valuedim);
		if(val != null) params[i] = new ExpNode(val);
		i++;
	    }
	}
	
	/** Create anew, to put into the structure */
	public ExpNode(DecNode dec, ExpNode[] params) {
	    this.dec = dec;
	    this.params = new ExpNode[dec.params.length];
	    System.arraycopy(params, 0, this.params, 0, params.length);
	    c = dec.c.zzclone();
	    ZZCell here = c;
	    for(int i=0; i<dec.params.length; i++) {
		here = here.N(paramdim);
		if(i >= params.length || params[i] == null) continue;
		if(params[i].c.s(valuedim, -1) != null)
		    params[i].c.disconnect(valuedim, -1);
		here.connect(valuedim, 1, params[i].c);
	    }
	}
	
	/** Change a param in the structure. */
	public void setParam(int nr, ExpNode val) {
	    ZZCell var = c.getOrNewCell(paramdim, nr+1);
	    if(var.s(valuedim) != null) var.disconnect(valuedim, 1);
	    if(val.c.s(valuedim, -1) != null) val.c.disconnect(valuedim, -1);
	    var.connect(valuedim, 1, val.c);
	}
	
	/** Read the text for a given GrammarNode.
	 *  Returns an Object[] array where the Objects are ZZCells
	 *  (which contain a piece of the text) ExpRef objects, or TypeRef
	 *  objects.
	 *  <br>
	 *  NOTE: there is no corresponding setText function because the
	 *  text is deducted from params[] and dec.getText().
	 */
	public Object[] getText(GrammarNode g) {
	    Object[] res = dec.getText(g);
	    if(res == null) return null;
	    for(int i=0; i<res.length; i++) {
	        if(res[i] instanceof DecRef) {
	            DecRef ref = (DecRef)res[i];
		    if(params[ref.i] != null)
	        	res[i] = new ExpRef(params[ref.i], ref.g);
		    else
			res[i] = new TypeRef(dec.params[ref.i], ref.g);
	        }
	    }
	    return res;
	}
	
	/** Step through a sequence (list) of expression nodes. */
	public ExpNode step(int steps) {
	    ZZCell c0 = c.s(sequencedim, steps);
	    if(c0 == null) return null;
	    return new ExpNode(c0);
	}
    }
}