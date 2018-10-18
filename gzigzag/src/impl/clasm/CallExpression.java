/*   
CallExpression.java
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
import org.gzigzag.impl.Cursor;
import java.util.HashMap;

/** A clasm expression which calls some Callable.
 */

public class CallExpression extends Expression {
String rcsid = "$Id: CallExpression.java,v 1.16 2001/07/30 17:54:04 tjl Exp $";

    private Callable callable;
    private Object[] params;

    /** Create a CallExpression given its callable and parameters.
     */
    public CallExpression(Callable callable, Object[] params) {
	this.callable = callable;
	this.params = params;
	if(callable == null)
	    throw new IllegalArgumentException("Passed null as the callable "+
					       "to CallExpression.");
    }

    protected Object getSingleParam(Cell c, HashMap stackframe)
            throws ClasmException {
	Cell expr = c.s(c.space.getDim(ClasmDims.d_call));
	c = c.getRootclone();

	if(expr != null) return new CallExpression(expr, stackframe);
	else if(stackframe.get(c) != null)
	    // return a Variable instance; it's a callable and needs be wrapped
	    return new ConstantExpression(stackframe.get(c));
	else if(Cursor.get(c) != null)
	    return Cursor.get(c);
	else
	    return c;
    }

    /** Create a CallExpression from a call strip in a ZigZag space.
     *  @param stackframe A mapping of variable cells to Variable objects.
     */
    public CallExpression(Cell callstrip, HashMap stackframe) 
      throws ClasmException {
	Object callableObj = getSingleParam(callstrip, stackframe);
	if(callableObj instanceof Expression)
	    if(!((Expression)callableObj).hasNoSideEffects(noparam))
		throw new ClasmException("The callable in call expression "+
					 "at "+callstrip+", "+callableObj+", "+
					 "does not guarantee it has no "+
					 "side-effects. The evaluation of "+
					 "an expression's callable must be "+
					 "without side-effects.");
	callable = asCallable(callableObj);
	// System.out.println("callable: "+callable);

	final Dim dim = callstrip.space.getDim(ClasmDims.d_params);
	
	// count cells on rank
	int n = 0;
	for(Cell c = callstrip.s(dim); 
	    c != null && !c.equals(callstrip); 
	    c = c.s(dim))
	        n++;
	
	if(n == 0) {
	    // does not have params: use noparam
	    params = Callable.noparam;
	    return;
	}
	
	Cell c = callstrip;
       	params = new Object[n];

	for(int i=0; i<n; i++) {
            c = c.s(dim);
	    params[i] = getSingleParam(c, stackframe);
	}
    }

    /** Call the given callable with the given parameters.
     */
    public Object eval() throws ClasmException {
	// System.out.println("...use callable: "+callable);
	return callable.call(params);
    }

    /** Ask the given callable whether it has side-effects when called with
     *  the parameters in this call expression.
     *  <p>
     *  Note: by definition of Callable.hasNoSideEffects(), this must first
     *  ensure that none of the params has side-effects. It is vital to do
     *  this <em>first</em>, i.e. before the callable of this call is asked,
     *  because Callable.hasNoSideEffects() is itself defined to have no
     *  side effects <em>provided that</em> evaluating the parameters passed
     *  to it has no side effects in itself.
     *  <p>
     *  @param pars Supposed to be an empty array, as this is an expression.
     *              This is currently not checked for.
     */
    public boolean hasNoSideEffects(Object[] pars) throws ClasmException {
	for(int i=0; i<this.params.length; i++) {
	    Object o = this.params[i];
	    if(o instanceof Expression)
		if(!((Expression)o).hasNoSideEffects(Callable.noparam))
		    return false;
	}
	return callable.hasNoSideEffects(this.params);
    }

}



