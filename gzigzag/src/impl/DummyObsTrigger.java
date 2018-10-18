/*   
DummyObsTrigger.java
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

package org.gzigzag.impl;
import org.gzigzag.*;

/** An dummy ObsTrigger.
 */

public class DummyObsTrigger implements ObsTrigger {
String rcsid = "$Id: DummyObsTrigger.java,v 1.5 2001/07/08 21:25:12 tjl Exp $";
    
    public void addObs(Obs o, Object obj, Object code) { }
    public void rmObs(Obs o) { }
    public void chg(Object obj, Object code) { }
    public void callQueued() { }
    
}

