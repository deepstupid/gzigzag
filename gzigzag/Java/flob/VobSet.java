/*   
VobSet.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
import java.util.*;
import java.awt.*;

/** A draft for a new interface to FlobSet.
 * The point is that this interface can be implemented by several
 * things and we can make dfferent views interact in interesting 
 * ways.
 * <p>
 * A VobSet can be thought of as a window in some ways...
 */

abstract public class VobSet implements ZZScene {
public static final String rcsid = "$Id: VobSet.java,v 1.1 2000/12/11 02:54:10 tjl Exp $";

    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    abstract public Rectangle getBounds();
    abstract public int getDepth();

    // XXX Make it use ZZDrawing2D to get the bg paint.
    abstract public Color getBackground();

    abstract public void add(Renderable r);
    abstract public void add(Flob f);

    // ??? protected void depthColor(Graphics g, int depth) {

    abstract public void dump();

    /** A callback interface for iterating over the Flobs
     * in a FlobSet in depth order.
     */
    public interface DepthIter {
	void act(Flob[] flobs, int start, int n);
    }

    /** Iterate a routine over a blocks of same-depth flobs.
     */
    abstract public void iterDepth(DepthIter di, boolean frontFirst);


	
    // XXX Spanset?
    // abstract public SpanFlob[] findFlobs(Span s);

    // abstract public 

}
