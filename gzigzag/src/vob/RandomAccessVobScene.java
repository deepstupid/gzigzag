/*   
VobScene.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
package org.gzigzag.vob;
import java.awt.*;

/** A vob scene without constraints on placing vobs into subscenes.
 *  VobScene does not allow vobs to be placed inside a scene between creating
 *  a subscene with createSubScene; a RandomAccessVobScene is defined to
 *  allow this.
 */

public interface RandomAccessVobScene extends VobScene {
}
