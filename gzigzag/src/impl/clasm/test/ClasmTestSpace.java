/*   
ClasmTestSpace.java
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
import org.gzigzag.*;
import org.gzigzag.impl.*;
import java.util.*;
import java.io.*;

/** A space for testing Clasm.
 *  This implements getJavaObject() through a HashMap and a corresponding
 *  method, setJavaObject().
 */

public class ClasmTestSpace extends SimpleTransientSpace {
public static final String rcsid = "$Id: ClasmTestSpace.java,v 1.3 2001/07/30 17:54:04 tjl Exp $";

    HashMap objects = new HashMap();

    public void setJavaObject(Cell c, Object ob) {
	objects.put(c.getRootclone(), ob);
    }
    public Object getJavaObject(Cell c, Obs o) {
	return objects.get(c.getRootclone());
    }
}

