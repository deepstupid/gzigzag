/*   
AllPrimitives.java
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
package org.gzigzag.clang;
import org.gzigzag.*;

/** An class containing all Archimedes primitives written so far.
 */

public class AllPrimitives {
String rcsid = "$Id: AllPrimitives.java,v 1.9 2001/04/26 15:52:00 bfallenstein Exp $";

    static private final void pa(String s) { System.out.println(s); }

    static void setZaubertrankText(ZZCell main, Object[] parts) {
	if(main.s("d.zt-text") != null) main.disconnect("d.zt-text", 1);
	ZZCell c = main;
	for(int i=0; i<parts.length; i++) {
	    if(parts[i] instanceof ZZCell)
		c.connect("d.zt-text", 1, ((ZZCell)parts[i]).zzclone());
	    else if(parts[i] instanceof String)
		c.N("d.zt-text", 1).setText((String)parts[i]);
	    else
		throw new ZZError("Strange type at "+i+" in "+parts+": "+parts[i]);
	    c = c.s("d.zt-text", 1);
	}
    }

    /** Set the zaubertrank type of a parameter.
     *  Currently, creates a new type cell every time it is called.
     */
    static void setType(ZZCell param, String type) {
	param.N("d.zt-type", -1).setText("("+type+"?)");
    }

    public static abstract class Primitive extends Callable {
	/** Create the root information for this primitive.
	 *  Creates the parameter template and the text for the zaubertrank.
	 */
	public abstract void install(ZZCell main);
    }

    /** The list of primitives Clang.InstallPrimitives should install.
     */
    public static final String[] standardPrimitives = {
	"+", "*",
	"<", "<=", "==", "!=", ">=", ">",
	":=",
	"block", "while", "print",
	"STEP", "END",
	"terminate",
    };

    public static final Primitive get(ZZCell c) {
	return get(c.getText());
    }
    public static final Primitive get(String s) {
	if(s.equals("+")) return add;
	else if(s.equals("*")) return mul;
	
	else if(s.equals("<")) return lt;
	else if(s.equals("<=")) return lte;
	else if(s.equals("==")) return eq;
	else if(s.equals("!=")) return neq;
	else if(s.equals(">=")) return gte;
	else if(s.equals(">")) return gt;
	
	else if(s.equals(":=")) return assign;
	
	else if(s.equals("block")) return block;
	else if(s.equals("while")) return _while;
	else if(s.equals("print")) return print;

	else if(s.equals("STEP")) return step;
	// else if(s.equals("FIND")) return find;
	else if(s.equals("END")) return end;
	// else if(s.equals("SELECT")) return select; 
	
	else if(s.equals("terminate")) return terminate;
	
	// HACKY
	else if(s.equals("incparam")) return incparam;
	
	else throw new SyntaxError("Unknown Archimedes primitive: '"+s+"'");
    }

    /** Get the cell for a primitive.
     *  All primitives in a space are on a rank on d.primitive from the
     *  home cell. This finds the appropriate cell on d.primitive, or
     *  creates it.
     */
    static public ZZCell getCell(ZZSpace sp, String id) {
	// (Assures id is a real primitive.)
	Primitive prim = get(id);
	
	ZZCell c = sp.getHomeCell().findText("d.primitive", 1, id);
	if(c == null) {
	    c = sp.getHomeCell().N("d.primitive", 1);
	    c.setText(id);
	    prim.install(c);
	}
	return c;
    }

    static abstract class BinaryOp extends Primitive {
	protected String id, type;
	
	public void install(ZZCell main) {
	    ZZCell left = main.N("d.1", -1), right = main.N("d.1");
	    setType(left, type); setType(right, type);
	    left.setText("("+id+" left)"); right.setText("("+id+" right)");
	    setZaubertrankText(main, new Object[] { left, " "+id+" ", right });
	}
    }

    static abstract class ArithmeticOp extends BinaryOp {
	public abstract int eval(int left, int right);

	{ type = "int"; }
	public ZZCell evaluate(Expression exp, Namespace context) {
	    int left = Data.i(Archimedes.getValue(exp.getParam("d.1", -1), context));
	    int right = Data.i(Archimedes.getValue(exp.getParam("d.1"), context));
	    int result = eval(left, right);
	    ZZCell c = exp.main.N();
	    c.setText(""+result);
	    return c;
	}
    }

    public static ArithmeticOp add = new ArithmeticOp() {
	{ id = "+"; }
	public int eval(int a, int b) { return a+b; }
    };

    public static ArithmeticOp mul = new ArithmeticOp() {
	{ id = "*"; }
	public int eval(int a, int b) { return a*b; }
    };

    static abstract class CompareOp extends BinaryOp {
	public abstract boolean cmp(int left, int right);

	{ type = "int"; }
	public ZZCell evaluate(Expression exp, Namespace context) {
	    int left = Data.i(Archimedes.getValue(exp.getParam("d.1", -1), context));
	    int right = Data.i(Archimedes.getValue(exp.getParam("d.1"), context));
	    boolean result = cmp(left, right);
	    ZZCell c = exp.main.N();
	    if(result) c.setText("true");
	    else c.setText("false");
	    return c;
	}
    }

    public static CompareOp lt = new CompareOp() {
	{ id = "<"; }
	public boolean cmp(int a, int b) { return a < b; }
    };

    public static CompareOp lte = new CompareOp() {
	{ id = "<="; }
	public boolean cmp(int a, int b) { return a <= b; }
    };

    public static CompareOp eq = new CompareOp() {
	{ id = "=="; }
	public boolean cmp(int a, int b) { return a == b; }
    };

    public static CompareOp neq = new CompareOp() {
	{ id = "!="; }
	public boolean cmp(int a, int b) { return a != b; }
    };

    public static CompareOp gte = new CompareOp() {
	{ id = ">="; }
	public boolean cmp(int a, int b) { return a >= b; }
    };

    public static CompareOp gt = new CompareOp() {
	{ id = ">"; }
	public boolean cmp(int a, int b) { return a > b; }
    };

    public static BinaryOp assign = new BinaryOp() {
	{ id = ":="; type = "what"; }
	public ZZCell evaluate(Expression exp, Namespace context) {
	    ZZCell value = Archimedes.getValue(exp.getParam("d.1", 1), context);
	    ZZCell var = exp.getParam("d.1", -1);
	    context.putvalue(var, value);
	    return null;
	}
    };

    public static Primitive block = new Primitive() {
	public void install(ZZCell main) {
	    setZaubertrankText(main, new Object[] { "<BLOCK>" });
	}
	
	public ZZCell evaluate(Expression exp, Namespace context) {
	    ZZCell c = exp.getParam("d.2", 1);
	    ZZCell result = null;
	    while(c != null) {
		if(c.equals(exp.main))
		    throw new SyntaxError("Looping Archimedes block!");
		result = Archimedes.execute(c, context);
		c = c.s("d.2");
	    }
	    return result;
	}
    };

    public static Primitive _while = new Primitive() {
	public void install(ZZCell main) {
	    ZZCell cond = main.N("d.1", -1), act = main.N("d.1");
	    setType(cond, "boolean"); setType(act, "what");
	    cond.setText("(while condition)"); act.setText("(while action)");
	    setZaubertrankText(main, new Object[] { "while(", cond,
						    ") do ", act });
	}
		
	public ZZCell evaluate(Expression exp, Namespace context) {
	    ZZCell cond = exp.getParam("d.1", -1);
	    ZZCell act = exp.getParam("d.1");
	    ZZCell result = null;
	    while(Data.b(Archimedes.execute(cond, context)))
		result = Archimedes.execute(act, context);
	    return result;
	}
    };

    public static Primitive print = new Primitive() {
	public void install(ZZCell main) {
	    ZZCell par = main.N("d.1"); par.setText("(print what)");
	    setType(par, "what");
	    setZaubertrankText(main, new Object[] { "print ", par });
	}
	
	public ZZCell evaluate(Expression exp, Namespace context) {
	    ZZCell val = Archimedes.getValue(exp.getParam("d.1"), context);
	    ZZCell acc = ZZCursorReal.get(val);
	    if(acc != null)
		pa(""+acc);
	    else
		pa(val.getText());
	    return null;
	}
    };

    public static Primitive terminate = new Primitive() {
	public void install(ZZCell main) {
	    ZZCell par = main.N("d.1"); setType(par, "(which cell?)");
	    par.setText("(early termination cell)");
	    setZaubertrankText(main, new Object[] {
		"Terminate early, using ", par, " as the early termination cell."
	    });
	}
	
	public ZZCell evaluate(Expression exp, Namespace context) {
	    ZZCell c = Archimedes.getValue(exp.getParam("d.1"), context);
	    throw new EarlyTermination(ZZCursorReal.get(c));
	}
    };

    abstract static class PathOp extends Primitive {
	/** Perform this path operation.
	 *  @param c The position before the path operation.
	 *  @param dim The dimension to go along.
	 *  @param dir The direction to go.
	 *  @param par An additional parameter cell, if present.
	 *  @returns The position after the path operation.
	 */
	public abstract ZZCell step(ZZCell c, String dim, int dir, ZZCell par);

	public abstract void install(ZZCell main, ZZCell cpar, ZZCell dimpar,
				     ZZCell dirpar);

	public void install(ZZCell main) {
	    ZZCell cpar = main.getOrNewCell("d.1", -1); 
	    cpar.setText("(cell)");
	    setType(cpar, "which cell");

	    ZZCell dimpar = main.getOrNewCell("d.1", 1);
	    ZZCell dirpar = dimpar.getOrNewCell("d.1", 1);
	    dimpar.setText("(dim)");
	    dirpar.setText("(dir)");
	    setType(dimpar, "which dimension");
	    setType(dirpar, "in which direction");

	    install(main, cpar, dimpar, dirpar);
	}

	public ZZCell evaluate(Expression exp, Namespace context) {
	    ZZCell var = exp.main.s("d.1", -1);
	    if(var == null) {
		// XXX get from the namespace somehow
		throw new ZZError("not implemented");
	    }
	    ZZCell c = context.getcell(var);
	    String dim = Data.s(Archimedes.getValue(exp.getParam("d.1"), context));

	    int dir = 1;
	    ZZCell dircell = exp.main.s("d.1", 2);
	    if(dircell != null)
		dir = Data.i(Archimedes.getValue(dircell, context));

	    ZZCell par = null;
	    ZZCell parcell = exp.main.s("d.1", 3);
	    if(parcell != null)
		par = Archimedes.getValue(parcell, context);

	    context.putvalue(var, step(c, dim, dir, par));
	    return null;
	} 
    }

    public static PathOp step = new PathOp() {
	public void install(ZZCell main, ZZCell cpar, ZZCell dimpar, ZZCell dirpar) {    
	    setZaubertrankText(main, new Object[] {
		cpar, ": Step ", dirpar, " on ", dimpar, "."
	    });
        }

	public ZZCell step(ZZCell c, String dim, int dir, ZZCell par) {
	    return c.s(dim, dir);
	}
    };

    public static PathOp end = new PathOp() {
        public void install(ZZCell main, ZZCell cpar, ZZCell dimpar, ZZCell dirpar) {
	    setZaubertrankText(main, new Object[] {
		cpar, ": Go to the endcell ", dirpar, " on ", dimpar
	    });
	}
	    
	public ZZCell step(ZZCell c, String dim, int dir, ZZCell par) {
	    return c.h(dim, dir);
	}
    };    

    /** A hack that reads the value cell and writes IN THAT CELL.
     *  For testing purposes only.
     */
    public static Primitive incparam = new Primitive() {
	public void install(ZZCell main) {
	    main.N("d.1");
	    setZaubertrankText(main, new Object[] { "incparam (for testing "+
						    "purposes only!)" });
	}
	
	public ZZCell evaluate(Expression exp, Namespace context) {
	    ZZCell c = exp.getParam("d.1", 1);
	    int i = (Data.i(c) + 1);
	    c.setText(""+i);
	    return c;
	}
    };


}


