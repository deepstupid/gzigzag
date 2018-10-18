/*   
ImperativePrimitive.java
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

/** A primitive with side effects.
 *  In subclasses, only create an <code>exec</code> method, as described in
 *  <code>Primitive</code>.
 *  <p>
 *  Note: A control abstraction which calls its parameters multiple times,
 *  especially a loop, may want to subclass ImperativePrimitive instead of
 *  checking whether the parameters passed to it really have side-effects,
 *  since doing the same thing multiple times does not make sense if it does
 *  not have side-effects.
 *  @see Primitive, FunctionalPrimitive
 */

public abstract class ImperativePrimitive extends Primitive {
String rcsid = "$Id: ImperativePrimitive.java,v 1.1 2001/07/08 17:23:41 bfallenstein Exp $";

    public boolean hasNoSideEffects(Object[] params) throws ClasmException {
	return false;
    }
}