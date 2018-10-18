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
package org.gzigzag.clang;
import org.gzigzag.*;

/** An Archimedes Procedural Layer callable (primitive or function definition).
 */

public abstract class Callable {
String rcsid = "$Id: Callable.java,v 1.3 2001/04/13 22:42:51 bfallenstein Exp $";

    public abstract ZZCell evaluate(Expression exp, Namespace context);

    public static final Callable get(ZZCell c) {
	ZZCell root = c.getRootclone();
	
	if(root.h("d.expression", true) != null)
	    return new Function(root);
	else
	    return AllPrimitives.get(root);
    }

}


