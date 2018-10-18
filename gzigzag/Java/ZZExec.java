/*   
ZZExec.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag;
import java.awt.*;

/** A generic script engine interface.
 * ZZExec specifies a way of interfacing various script engines to the ZZ space.
 * At the moment, only UI callback functions are allowed - later, it will be
 * expanded to a more complete class.
 */

public interface ZZExec {
String rcsid = "$Id: ZZExec.java,v 1.12 2000/09/19 10:31:58 ajk Exp $";
    /** Execute some code as a callback from the user interface.
     * This is a special execution context which is triggered through user
     * action on an user interface. Note that some of the params
     * are redundant and only provided for ease. All the other parameters
     * can be deduced from code, view, ctrlview and clicked.
     * @param code		Cell to start execution from
     * @param view		View where command executed
     * @param cview		Control view of view where command executed
     * @param key	The key the user pressed, as string, if any
     * @param pt 	The point the user clicked on, if any
     * @param xi    The extra object - to get what was clicked, if any
     */
    void execCallback(ZZCell code, 
		    ZZCell target,
		    ZZView view, 
		    ZZView cview,
		    String key,
		    Point pt, 
		    ZZScene xi
		     );
    // /** Execute some code with parameters.
    //  * XXX Needs rethinking of the arguments.
    //  */
    // public void exec(ZZCell code, ZZCell[] params);
}
