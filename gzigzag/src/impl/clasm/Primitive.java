/*   
Primitive.java
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
import java.lang.reflect.*;

/** A clasm primitive.
 *  In subclasses, create a method called <code>exec</code>. It is
 *  an error to have more than one method called exec. Primitve.call will
 *  invoke this method by reflection. It may only have parameters of type
 *  Cell, String, int, boolean, and Expression (Callables are wrapped up
 *  inside of Expressions, as always). It may return everything; if it's not
 *  a Cell, a Callable, or null, it's converted to a String (e.g., booleans and
 *  ints are).
 *  <p>
 *  Additionally, create a method called <code>isFunctional</code>, which
 *  takes the same parameters as <code>exec</code>, and may return true only if
 *  the primitive has side effects called with these parameters. Alternatively,
 *  you can subclass <code>ImperativePrimitive</code> or 
 *  <code>FunctionalPrimitive</code>, which hard-wire the hasNoSideEffects
 *  method to return either false (ImperativePrimitive) or true 
 *  (FunctionalPrimitive).
 *  @see Callable.call, ImperativePrimitive, FunctionalPrimitive
 */

public abstract class Primitive extends Callable {
String rcsid = "$Id: Primitive.java,v 1.4 2001/07/08 17:23:15 bfallenstein Exp $";

    public Method getMethod(String name) throws ClasmException {
        Method exec = null;
        Method[] methods = this.getClass().getMethods();

        for(int i=0; i<methods.length; i++) {
	    if(methods[i].getName().equals(name)) {
		if(exec == null)
		    exec = methods[i];
		else
		    throw new ClasmException("More than one " + name + " " +
					     "method was found in primitve " +
					     this.getClass() + ".");
	    }
	}

	if(exec != null)
	    return exec;
	else
	    throw new ClasmException("No " + name + " method was found in " +
				     "primitive " + this.getClass() + ".");
    }



    public Object callByReflection(String method, Object[] params) 
      throws ClasmException {
	Method exec = getMethod(method);
	Class[] partypes = exec.getParameterTypes();
	if(partypes.length != params.length)
	    throw new ClasmException("Wrong number of parameters passed " +
				     "to primitive " + this.getClass() + ": " +
				     "expected " + partypes.length + ", but " +
				     "got " + params.length);

	Object[] args = new Object[partypes.length];
	for(int i=0; i<args.length; i++) {
	    Class type = partypes[i];
	    if(type.equals(Cell.class))
		args[i] = asCell(params[i]);
	    else if(type.equals(String.class))
		args[i] = asString(params[i]);
	    else if(type.equals(Integer.TYPE))
		args[i] = new Integer(asInt(params[i]));
	    else if(type.equals(Boolean.TYPE))
		args[i] = new Boolean(isTrue(params[i]));
	    else if(type.equals(Expression.class))
		args[i] = getExpression(params[i]);
	    else
		throw new ClasmException("Bad type of parameter ("+type+") " +
					 "at index " + i + " in the " +
					 method + " method of primitive " +
					 this.getClass() + ".");
	}

	try {
	    return exec.invoke(this, args);
	} catch(Throwable e) {
	    if(e instanceof InvocationTargetException)
		e = ((InvocationTargetException)e).getTargetException();
	    java.io.StringWriter msg = new java.io.StringWriter();
	    e.printStackTrace(new java.io.PrintWriter(msg));
	    throw new ClasmException("Exception occured in method " +
				     method + " in primitive " +
				     this.getClass() + ": " + msg);
	}
    }

    public Object call(Object params[]) throws ClasmException {
	Object res = callByReflection("exec", params);

	if(res == null || res instanceof Cell || res instanceof Callable)
	    return res;
	else
	    return res.toString();
    }

    public boolean hasNoSideEffects(Object[] params) throws ClasmException {
	Boolean res = (Boolean)callByReflection("isFunctional", params);
	return res.booleanValue();
    }
}



