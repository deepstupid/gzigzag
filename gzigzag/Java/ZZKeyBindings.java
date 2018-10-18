/*   
ZZKeyBindings.java
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
import java.awt.event.*;
import java.awt.*;

/** A generic keybindings driver interface.
 * The point of this interface is to allow experimentation 
 * with different ways of specifying keybindings in the ZigZag structure.
 */

public interface ZZKeyBindings {
String rcsid = "$Id: ZZKeyBindings.java,v 1.13 2000/09/19 10:31:58 ajk Exp $";
	/** Perform a particular key or mouse
	 * event associated with a particular view.
	 */
	void perform(InputEvent k, ZZView v, ZZView ctrlv, ZZScene xi);
}

