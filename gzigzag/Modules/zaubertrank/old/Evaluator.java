/*   
Evaluator.java
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

/** An evaluator for zaubertrank expressions.
 */

public class Evaluator {
public static final String rcsid = "$Id: Evaluator.java,v 1.1 2001/04/02 20:29:05 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    // XXX should be able to store the following in the structure for debugging
    public Stack data = new Stack();
    public Stack returns = new Stack();
    public Stack frames = new Stack();
    public Data frame;
    public ZZCell pos;
    public boolean failed = false;

    public Evaluator(ZZCell what, Data args) {
	pos = what.h("zaubertrank:rpn");
	frame = args;
    }

    public void step() {
	ZZCell root = pos.getRootclone();
	Primitive p = PrimitiveSet.findPrimitive(root);
	if(root == null) { // RETURN STEP -- XXX return cell in ztpart
	    if(returns.empty()) return;
	    pos = (ZZCell)returns.pop();
	    frame = (Data)frames.pop();
	} else if(p != null) {
	    Object[] args = new Object[p.args()];
	    for(int i=args.length-1; i>=0; i--) args[i] = data.pop();
	    data.push(p.eval(new Data(args)));
	} else if(root.s("d.zt-arg") != null) {
	    int nr = Data.i(root.s("d.zt-arg"));
	    data.push(frame.o(nr));
	} else {      // FUNCTION CALL! (XXX need argument reads)
	    throw new ZZError("Function calls aren't working yet!");
/*
	    Parser.DecNode dn = new Parser.DecNode(root);
	    Object[] args = new Object[dn.params.length];
	    for(int i=args.length-1; i>=0; i--) args[i] = data.pop();
	    returns.push(pos);
	    frames.push(frame);
	    frame = new Data(args);
	    pos = dn.c.h("zaubertrank:rpn"); // XXX fix
*/
	}
	pos = pos.s(Parser.sequencedim);
    }

    static public Object eval(ZZCell what, Data args) {
	Evaluator e = new Evaluator(what, args);
	while(e.running()) e.step();
	if(e.failed) return null;
	return e.data.pop();
    }

    public boolean running() {
	return (!failed && pos != null);
    }
    
    public boolean finished() {
	return (!failed && pos == null);
    }
}