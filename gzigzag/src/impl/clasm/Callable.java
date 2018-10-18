/*   
Callable.java
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

/** A callable Clasm entity.
 *  @see call
 */

public abstract class Callable {
String rcsid = "$Id: Callable.java,v 1.13 2001/08/10 04:04:24 bfallenstein Exp $";

    // The interface

    /** Call this callable.
     *  <code>params</code> is the list of parameters for this call. Each
     *  entry must be one of the following:
     *  <ul>
     *  <li><code>null</code>.
     *  <li>A Cell.
     *  <li>A String.
     *  <li>An Expression which, when evaluated, returns one of the above,
     *      or a Callable.
     *  </ul>
     *  You can get the values as Cell, String, int, boolean, and Callable
     *  by using asCell, asString, asInt, isTrue, and asCallable, all of
     *  which are public static convenience functions in Callable. (isTrue is
     *  not called asBoolean because the former name sounds better in the
     *  usual context of an if() statement.) Additionally, you can request
     *  the Expression evaluating this parameter by calling getExpression
     *  (if this is passed as a value, i.e. String or Cell, a
     *  ConstantExpression is created containing that value).
     *  <p>
     *  <b>CAUTION.</b> Although a callable may <i>return</i> a Callable,
     *  Callables may not directly be <i>passed</i> to Callable.call(). This
     *  is because Expressions which evaluate to the values of some
     *  parameters may be passed, and there would be confusion as to whether
     *  an Expression <i>is</i> the value of a parameter, or <i>returns</i>
     *  that value. Thus, Callables need to be wrapped up in 
     *  ConstantExpressions returning that callable. If you pass the result
     *  of calling a Callable to another Callable, you need to check whether
     *  that result was a Callable and if so, wrap it up in a
     *  ConstantExpression! (If that's too hairy for you, you can also always
     *  wrap the result in a ConstantExpression, without checking what it is.)
     *  <p>
     *  Note that although each entry in the list may be <code>null</code>,
     *  the parameter array itself may not. To call a <code>Callable</code>
     *  without giving parameters, pass Callable.noparam, which is an empty
     *  array of Objects.
     *  @returns A String, Cell, or Callable.
     */
    public abstract Object call(Object[] params) throws ClasmException;


    /** Given a list of parameters, whether this Callable is non-destructive.
     *  Given a list of parameters, this function returns true iff the
     *  callable has no side-effects when Callable.call is called with this
     *  list of parameters, provided that the parameters themselves do not
     *  have side-effects. This method itself must be without side-effects,
     *  again provided that all parameters passed to it are without
     *  side-effects, too.
     *  <p>
     *  Side-effects are currently defined a bit non-obviously:
     *  <ul>
     *  <li>Basically, if a Callable changes the structure, some internal
     *      state, or some state outside GZZ, or if it might return different
     *      values when called multiple times with the same parameters
     *      (and without Callables with side-effects called in between),
     *      it has side-effects.
     *  <li>However, changing the ZZ structure is not a side-effect if only
     *      new cells are created and operated upon which have no connection
     *      to what was there before the Callable was called.
     *  <li>Actually, it may even have connections, but these may only be
     *      either one of the new cells being a clone of one of the existing
     *      cells, or one of the new cells accursing one of the existing cells.
     *  </ul>
     *  This definition is currently necessary so that side-effect-less
     *  functions can create data structures and aren't limited to primitive
     *  data types and references to existing cells.
     *  <p>
     *  Note: this method is not required to throw an exception if the
     *  parameters passed to it are wrong for this Callable (although it may):
     *  if it knows without looking too closely at the parameters that
     *  the Callable will be with/without side effects, it does not need to
     *  look at them more closely, i.e. the later Callable.call() call should
     *  raise the exception.
     *  <p>
     *  This method is <em>not</em> efficient. But when we can compile clasm,
     *  we should be able to do it much more efficiently (hopefully).
     *  <p>
     *  Default: return false. (Note: if you don't know for sure a Callable
     *  will be without side-effects with a set of params, return false.)
     */
    public boolean hasNoSideEffects(Object[] params) throws ClasmException {
	return false;
    }



    public Object call() throws ClasmException {
	return call(noparam);
    }
    public Object call(Object o) throws ClasmException {
	return call(new Object[] { o });
    }
    public Object call(Object o1, Object o2) throws ClasmException {
        return call(new Object[] { o1, o2 });
    }
    public Object call(Object o1, Object o2, Object o3) throws ClasmException {
        return call(new Object[] { o1, o2, o3 });
    }
    public Object call(Object o1, Object o2,
		       Object o3, Object o4) throws ClasmException {
        return call(new Object[] { o1, o2, o3, o4 });
    }


    // The factory functionality

    /** Get the callable associated with cell <code>c</code>.
     *  There are two ways in which a callable can be associated with a cell;
     *  firstly, it can be a Primitive associated with the cell through
     *  <code>Cell.getJavaObject</code>; secondly, the cell's rootclone
     *  can be a function definition. The latter case is recogized by the
     *  rootclone having a connection poswards on the function
     *  definition dimension.
     *  @returns <code>null</code>, if there is no callable associated with
     *           <code>c</code>.
     *  @see Cell.getJavaObject, Function
     */
    public static Callable getCallable(Cell c) throws ClasmException {
	c = c.getRootclone();
	Cell prim = c.s(ClasmDims.d_prim);
	if(prim != null)
	    return (Primitive)prim.getJavaObject();
	else if(c.s(c.space.getDim(ClasmDims.d_def)) != null)
	    return new Function(c);
	else
	    return null;
    }
    


    // Some convenience functions

    /** An empty array of Objects.
     *  To be used when calling parameterless Callables, specifically
     *  Expressions. We do not want to create a new array there every time,
     *  especially as Expressions are called very frequently when executing
     *  Clasm code. On the other hand, neither do we want to test for
     *  <code>null</code> everywhere, nor have strange null pointer exceptions.
     *  Thus, here is a static final array which is to be passed when
     *  calling a parameterless callable.
     */
    public static final Object[] noparam = {};

    /** Get the value of <code>o</code>, evaluating it if it's an expression.
     */
    public static Object value(Object o) throws ClasmException {
	if(o instanceof Expression)
	    return ((Expression)o).eval();
	else
	    return o;
    }

    /** Get the expression which returns the value of this parameter. 
     *  If the parameter is an Expression, return it; if it is a value,
     *  construct a ConstantExpression returning that value.
     */
    public static Expression getExpression(Object o) throws ClasmException {
	if(o instanceof Expression)
	    return (Expression)o;
	else
	    return new ConstantExpression(o);
    }

    public static Cell asCell(Object o) throws ClasmException {
	o = value(o);
	if(o == null)
	    return null;
	else if(!(o instanceof Cell))
	    throw new ClasmException("Wrong clasm datum "+o+": not a cell");
	return (Cell)o;
    }
    public static String asString(Object o) throws ClasmException {
	o = value(o);
	if(o instanceof String)
	    return (String)o;
	else if(o instanceof Integer)
	    return ""+((Integer)o).intValue();
	else if(o instanceof Cell)
	    return ((Cell)o).t();
	else if(o instanceof Boolean) {
	    if(((Boolean)o).booleanValue())
		return "true";
	    else
		return "false";
	} else if(o == null)
	    return null;
	else
	    throw new ClasmException("Wrong clasm datum "+o+": not a str");
    }
    public static boolean isTrue(Object o) throws ClasmException {
	o = value(o);
	if(o instanceof Boolean)
	    return ((Boolean)o).booleanValue();
	else {
	    String str = asString(o).toLowerCase();
	    if(str.equals("true"))
		return true;
	    else if(str.equals("false"))
		return false;
	    else
	        throw new ClasmException("Wrong clasm datum " + o + ": " +
					 "not a boolean");
	}
    }
    public static int asInt(Object o) throws ClasmException {
	o = value(o);
	if(o instanceof Integer)
	    return ((Integer)o).intValue();
	else {
	    try {
		return Integer.parseInt(asString(o));
	    } catch(NumberFormatException e) {
		throw new ClasmException("Wrong clasm datum " + o + ": " +
					 "not an integer");
	    }
	}
    }
    public static Callable asCallable(Object o) throws ClasmException {
	if(o instanceof Expression) {
	    o = value(o);
	    if(o instanceof Callable)
		return (Callable)o;
	}
	Callable res = null;
	if(o instanceof Cell)
	    res = getCallable((Cell)o);
	if(res == null)
	    throw new ClasmException("Wrong clasm datum " + o + ": " +
				     "not a callable");
	return res;
    }
}



