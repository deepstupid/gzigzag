Title: Internals of Clasm primitive set 1
Rcs-Id: $Id: clasm-primitives-internals.ly,v 1.6 2002/03/02 20:06:37 bfallenstein Exp $
Ly-Version: 0.0.2

This is a placeholder for a literate program explaining the
internals of |ClasmPrimitiveSet1.java|. This file does not actually
contain any primitives: it contains the bureaucracy around the primitives.
So if you want to debug or learn about the existing primitives, or if
you want to add a new one, you should go to the file |clasm-primitives.ly|,
which is merged with this file during tangling.


-- file "ClasmPrimitiveSet1.java":
	/* THIS IS GENERATED CODE! DO NOT MODIFY! Modify the original, 
	   impl/clasm/clasm-primitives.ly and 
	   impl/clasm/clasm-primitives-internals.ly, instead. Any changes 
	   to this file will be lost when it is regenerated.  
	*/

	/*
	ClasmPrimitiveSet1.java
	 *    
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
	import org.gzigzag.impl.*;
	import org.gzigzag.client.*;
	import org.gzigzag.*;
	import java.util.HashMap;
	import java.lang.reflect.*;
	import java.io.*;

	/** A first set of clasm primitives.
	 *  The idea is to put everything here we need for the very first GZZ
	 *  client...
	 * <p>
	 * See the literate source code, <code>clasm-primitives.ly</code>
	 * and <code>clasm-primitives-internals.ly</code> (or better, the
	 * .html files produced from these).
	 * <p>
	 * Note that these are really primitives used by actual clasm functions;
	 * any changes to these should be done with UTMOST care, as it is
	 * as dangerous as changing a system library like libc.
	 */

	public class ClasmPrimitiveSet1 extends PrimitiveSpace {
	String rcsid = "$Id: clasm-primitives-internals.ly,v 1.6 2002/03/02 20:06:37 bfallenstein Exp $";

	    static HashMap prims = new HashMap();
	    public Object getPrimitive(String s) {
	        if(!s.substring(0, s.indexOf("-")).equals(getLastId().getString()))
	            return null;
	        s = s.substring(s.indexOf("-") + 1);
	        Primitive res = (Primitive)prims.get(s);
	        if(res != null) return res;

	        try {
	            return
	                ClasmPrimitiveSet1.class.getField(s).get(null);
	        } catch(Exception e) {
	            e.printStackTrace();
	            throw new Error("Primitive "+s+" could not be instantiated.");
	        }
	    }
	
	    -- Clasm primitives.
	
	    static public void main(String[] argv) {
	        System.out.println("My name is:");
	        System.out.println(new ClasmPrimitiveSet1().getLastId().getString());
	    }
	}
