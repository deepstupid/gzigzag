/*   
Dbg.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka
 */
package org.gzigzag.util;
import org.gzigzag.*;
import java.util.*;
import java.lang.reflect.*;

/** Turning on debugging according to gzz conventions.
 * The GZigZag convention for debuggable classes is that every class must have
 * a public static member dbg, e.g. as in
 * <pre>
 * class Foo {
 *	static public boolean dbg = false;
 *      ...
 *	private final void p(String s) { if(dbg) System.out.println(s); }
 *      ...
 *      ...
 *      void method() {
 *         p("Here");
 *         ...
 *         if(dbg)
 *             object-&lt;printDebugInfo();
 *         ...
 *      }
 * }
 * </pre>
 * This member can be turned on from outside.
 * <p>
 * <b>Caveats</b>
 * <dl>
 *  <dt> Efficiency
 *    	<dd> Some Java versions, such as Kaffe 1.0.6, are not smart enough
 *		to optimize code like
 *			<pre>
 * p("AGG "+object+" "+object2+" "+number);
 *			</pre>
 *		so that the string is not formed if dbg is false.
 *		This is very unfortunate.
 *		If code like above is inside tight loops, a profiler will
 *		show that your program is spending a good amount of its
 *		time in StringBuffer code, making strings that will never be
 *		used for anything.
 *		Therefore, the above statements, especially in tight loops,
 *		should be changed to
 *			<pre>
 * if(dbg) p("AGG "+object+" "+object2+" "+number);
 *			</pre>
 *  <dt> Class garbage collection
 *  	<dd> Some (most?) Java implementations garbage-collect classes.
 *		This means that setting the static dbg value may not
 *		last if the class is garbage-collected and then reloaded.
 *		Because of this, Dbg keeps inside it a list of the classes
 *		for which debugging has been turned on.
 *		Therefore, <b>you should keep the Dbg object somewhere where
 *		it will not be garbage collected</b>.
 *		
 * </dl>
 */

public final class Dbg {
public static final String rcsid = "$Id: Dbg.java,v 1.2 2001/07/16 18:05:34 bfallenstein Exp $";

    static boolean dbg = true;
    final void p(String s) { if(dbg) System.out.println(s); }

    List dbgClasses = new ArrayList();

    /** Turn on debugging for a class.
     * @param name The name of the class (without the org.gzigzag. prefix.)
     * @param dbg Whether to turn debugging on or off.
     */
    public void debugClass(String name, boolean dbg) {
	try {
	    p("Turning on debugging for class "+name);
	    Class clazz = Class.forName("org.gzigzag."+name);
	    Field f = clazz.getField("dbg");
	    f.setBoolean(null, dbg);
	    dbgClasses.add(clazz);
	    dbgClasses.add(f);
	} catch(Exception e) {
	    e.printStackTrace();
	    p(""+e);
	    throw new Error("Error while turning on debug info");
	}
    }
}
