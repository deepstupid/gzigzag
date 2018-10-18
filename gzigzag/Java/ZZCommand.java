/*   
ZZCommand.java
 *    
 *    Copyright (c) 1999, Ted Nelson and Tuomas Lukka
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
 * Written by Benjamin Fallenstein
 */

package org.gzigzag;
import java.awt.*;

/** An abstract class representing a command stored in zz space.
 * This should be thought of as a possible successor for ZZExec. Other than
 * ZZExec, the object created is not a scripting engine, but rather a
 * command which can be executed with a scripting engine. Subclasses of
 * ZZCommand are expected to implement ZOb.
 * <p>
 * This class also provides the static method getCommand, which returns the
 * ZZCommand associated with a cell, if any.
 */

public abstract class ZZCommand {
    /** Execute some code as a callback from the user interface.
     * This is a special execution context which is triggered through user
     * action on a user interface. Note that some of the params
     * are redundant and only provided for ease. All the other parameters
     * can be deduced from view, ctrlview and clicked.
     * @param view		View where command executed
     * @param cview		Control view of view where command executed
     * @param key		The key the user pressed, as string, if any
     * @param pt 		The point the user clicked on, if any
     * @param xi	The extra object - to get what was clicked, if any
     */
    public void execCallback(
		    ZZCell target,
		    ZZView view, 
		    ZZView cview,
		    String key,
		    Point pt, 
		    ZZScene xi
		     ) {
	exec(target);
    }

    // /** Execute with parameters.
    //  * XXX Needs rethinking of the arguments.
    //  */
    // public abstract void exec(ZZCell[] params);

    /** Execute with a single parameter.
     * An intermediate for calls which don't come from the UI, but from
     * somewhere else (e.g., a trigger).
     * <p>
     * Note that UI callbacks are proxied to exec by standard (that is, if
     * execCallback isn't overridden.)
     */
    public abstract void exec(ZZCell param);

    public static ZZCommand getCommand(ZZCell code) {
	// Check if it's a ZOb first:
	if(code.s("d.clone", -1) == null &&
	   code.s("d.1", 1) == null)
	    return null;
	ZOb z = ZZDefaultSpace.readZOb(code);
	if(z != null && z instanceof ZZCommand)
	    return (ZZCommand) z;
	return null;
    }
}
