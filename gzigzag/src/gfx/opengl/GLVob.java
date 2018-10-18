/*   
GLVob.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
package org.gzigzag.gfx;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

public abstract class GLVob {
public static final String rcsid = "$Id: GLVob.java,v 1.3 2002/03/10 15:52:18 tjl Exp $";

    /** The key associated with this vob.
     * This may be set to null for things that are - well - just
     * things rendered. 
     * Keys are compared with the Object.equals method.
     */
    public Object key; // Not final: CharArrayVob is its own key...
    
    /** Creates a vob which will represent some object in a visualization. 
     *  Two
     *  vobs representing the same object can be interpolated in-between, and
     *  the represented object can be used when directing user interface 
     *  events
     *  back to the underlying model.
     *  @param key the object this vob represents
     */
    public GLVob(Object key) {
	this.key = key;
    }

    /** Add the current vob to the given display list, using
     * the given coordinate systems.
     * If only one coordinate system is needed, coordsys1 is used.
     */
    abstract public int addToList(int[] list, int curs, int coordsys1,
		int coordsys2);
}
