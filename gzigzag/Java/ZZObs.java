/*   
ZZObs.java
 *    
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
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
/** A simple class that observes something.
 * This class cannot distinguish what caused the event - either
 * the event has to be filtered prior to this or the whole thing reread
 * in any case.
 */

public interface ZZObs {
String rcsid = "$Id: ZZObs.java,v 1.11 2000/09/19 10:31:58 ajk Exp $";
    /** Called when something is changed.
     */
    void chg();
}
