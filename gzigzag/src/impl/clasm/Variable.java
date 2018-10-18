/*   
Variable.java
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

/** A clasm variable.
 *  In clasm, a variable is a callable which, when called without parameters,
 *  returns the value of that variable, and when called with a single
 *  parameter, evaluates the parameter, sets the variable to that value,
 *  and returns the value. When called with a parameter, it thus works much
 *  like Java's assignment operator (=), which also returns the value of its
 *  right-hand argument.
 *  <p>
 *  Calling a Variable without parameters is without side effects. Calling
 *  a Variable with a parameter always evaluates its parameter.
 */

public class Variable extends Callable {
String rcsid = "$Id: Variable.java,v 1.2 2001/07/08 17:23:15 bfallenstein Exp $";

    private Object value;

    /** Create a Variable with an initial value.
     */
    public Variable(Object value) {
	this.value = value;
    }

    /** Create a Variable with initial value <code>null</code>.
     */
    public Variable() {
	this.value = null;
    }

    public Object call(Object[] params) throws ClasmException {
	if(params.length == 0)
	    return value;
	else if(params.length == 1) {
	    value = Callable.value(params[0]);
	    return value;
	} else
	    throw new ClasmException("Wrong number of parameters for "+
				     "Variable. Variables only accept zero "+
				     "or one parameters; this was given "+
				     params.length+" ("+params+").");
    }

    public boolean hasNoSideEffects(Object[] params) throws ClasmException {
	if(params.length == 0)
	    return true;
	else if(params.length == 1)
	    return false;
	else
	    throw new ClasmException("Wrong number of parameters for "+
				     "Variable. Variables only accept zero "+
				     "or one parameters; this was given "+
				     params.length+" ("+params+").");
    }
}



