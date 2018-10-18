/*   
ConstantExpression.java
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

/** A clasm expression simply returning the value passed to its constructor.
 *  Used when a Callable got a literal and requests to see it as an expression.
 */

public class ConstantExpression extends Expression {
String rcsid = "$Id: ConstantExpression.java,v 1.2 2001/07/08 17:23:15 bfallenstein Exp $";

    private Object value;

    /** Create a ConstantExpression which returns <code>value</code>.
     */
    public ConstantExpression(Object value) { this.value = value; }

    /** Simply return this ConstantExpression's value.
     */
    public Object eval() throws ClasmException {
	return value;
    }

    /** Constant expressions are always without side-effects. */
    public boolean hasNoSideEffects(Object[] params) throws ClasmException {
	return true;
    }

}



