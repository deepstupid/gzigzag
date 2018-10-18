/*   
Function.java
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
package org.gzigzag.impl.clasm;
import org.gzigzag.*;
import java.util.HashMap;

/** A clasm expression which calls some Callable.
 */

public class Function extends Callable {
String rcsid = "$Id: Function.java,v 1.14 2001/08/05 18:57:43 tjl Exp $";

    /** An array of the parameter cells associated with this function.
     *  This is the list of cells with which the parameters passed to this
     *  function will be assigned as variables.
     *  <p>
     *  The number of parameter cells passed to the function must equal the
     *  number of elements in paramcells; otherwise, an exception is raised.
     */
    private Cell[] paramcells;

    /** A list telling which of the params contain the expression rather than
     *  the value.
     *  <p>
     *  When expparams[i] is true, then params[i] won't be evaluated; instead,
     *  the parameter variable will contain an expression that, when called,
     *  evaluates the parameter. This allows for creating abstractions like
     *  conditions and loops easily.
     *  <p>
     *  <code>null</code> if false for all.
     */
    private boolean[] expparams;

    /** Whether the last parameter of this function is a "list parameter".
     *  If true, and the function has n parameter cells, it will take
     *  n-1 to any number of parameters, and make a list of all parameters
     *  starting from the nth, and it will assign that list to the last one
     *  of its parameter cells. In other words, a function of the form
     *  <pre>
     *      fun(a, b, c, d[])
     *  </pre>
     *  can be declared, which, when called as fun(1, 2, 3, 4, 5, 6) assigns
     *  the following values to its parameters: a = 1; b = 2; c = 3;
     *  d = { 4, 5, 6 }. The list is represented as a rank down on
     *  d.params (not implemented: currently we put <code>null</code> into
     *  the variable).
     */
    private boolean listparam;

    /** The cells declared to be variables of this function.
     */
    private Object[] varcells;

    private Cell body;

    /** For functions without parameters.
     */
    private static Cell[] nocells = {};

    /** Create a Function from a function definition in a ZZ space.
     */
    public Function(Cell def) throws ClasmException {
	Space space = def.space;
	body = def.s(space.getDim(ClasmDims.d_def));
	if(body == null)
	    throw new ClasmException("Function " + def + " has no body.");

	Dim d1 = space.getDim(ClasmDims.d_call),
	    d2 = space.getDim(ClasmDims.d_params);
	Cell varhead = def.s(d1);

	// --- read the parameters in ---

        // count cells on rank, determine whether we have expparams
        int n = 0;
	boolean haveExpParams = false;

        for(Cell c = def.s(d2); c != null && !c.equals(def); c = c.s(d2)) {
	    n++;
	    if(c.s(d1) != null)
		haveExpParams = true;
	}

        if(n == 0) {
            // does not have params: use nocells
            paramcells = nocells;
	    expparams = null;
        } else {
	    Cell c = def;
	    paramcells = new Cell[n];
	    if(haveExpParams)
		expparams = new boolean[n];
	    else
		expparams = null;
	    
	    for(int i=0; i<n; i++) {
		c = c.s(d2);
		if(c.s(d1) == null)
                    paramcells[i] = c;
		else if(c.s(d1).equals(c)) {
		    if(i < n-1)
			throw new ClasmException("Parse error: listparam declared not at the end of the parameter list");
		    paramcells[i] = c;
		    listparam = true;
		} else {
		    paramcells[i] = c.s(d1);
		    expparams[i] = true;
		    if(paramcells[i].equals(paramcells[i].s(d2))) {
			if(i < n-1)
			    throw new ClasmException("Parse error: listparam declared not at the end of the parameter list");
			listparam = true;
		    }
		}
	    }
	}


	// --- now, for the variables ---

	if(varhead == null) {
	    varcells = nocells;
	    return;
	}

        // count cells on rank
        n = 0;
        for(Cell c = varhead.s(d2);
            c != null && !c.equals(varhead);
            c = c.s(d2))
	    n++;

        if(n == 0) {
            // does not have params: use nocells
            varcells = nocells;
            return;
        }

        Cell c = varhead;
        varcells = new Cell[n];

        for(int i=0; i<n; i++) {
            c = c.s(d2);
            varcells[i] = c;
        }
	
    }

    public Expression getExpression(Object[] params) throws ClasmException {
	HashMap stackframe = new HashMap();

	int n = listparam ? paramcells.length-1 : paramcells.length;

	if(params.length < paramcells.length || 
	   (!listparam && params.length > paramcells.length))
	    throw new ClasmException("Wrong number of params in function "+
				     body+": expected "+n+
				     (listparam ? " or more" : "")+
				     ", "+"but got "+params.length+".");
	
	for(int i=0; i<n; i++) {
	    if(expparams != null && expparams[i])
		stackframe.put(paramcells[i], new Variable(params[i]));
	    else
		stackframe.put(paramcells[i], 
			       new Variable(Callable.value(params[i])));
	}

	if(listparam) {
	    // XXX Fixme: really read the parameters into a list
	    stackframe.put(paramcells[n], new Variable(null));
	    for(int i=n; i<params.length; i++)
		Callable.value(params[i]);
	}

	for(int i=0; i<varcells.length; i++) {
	    stackframe.put(varcells[i], new Variable());
	}
	
	return new CallExpression(body, stackframe);
    }

    public Object call(Object[] params) throws ClasmException {
	return getExpression(params).eval();
    }

    public boolean hasNoSideEffects(Object[] params) throws ClasmException {
	return getExpression(params).hasNoSideEffects(Callable.noparam);
    }

}



