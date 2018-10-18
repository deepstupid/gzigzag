/*   
Expression.java
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

/** A clasm expression.
 *  The important thing about an expression is that it is a callable which
 *  does not accept parameters.
 */

public abstract class Expression extends Callable {
String rcsid = "$Id: Expression.java,v 1.1 2001/07/02 10:47:43 bfallenstein Exp $";

    /** Evaluate this Expression.
     *  Like Callable.call, except that it doesn't accept parameters.
     *  @see Callable.call
     */
    public abstract Object eval() throws ClasmException;

    public Object call(Object[] params) throws ClasmException {
	if(params.length > 0)
	    throw new ClasmException("Expression ("+this+") called with "+
				     "parameters. Expressions do not accept "+
				     "parameters.");
	return eval();
    }
}



