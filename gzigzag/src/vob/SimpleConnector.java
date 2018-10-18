/*   
SimpleConnector.java
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.vob;
import java.awt.*;
import java.util.*;

/** A simple class connecting all vobs with the same key in a vobscene.
 *  To decorate a vobscene, call the static method <code>decorate</code>.
 */

public class SimpleConnector extends Vob {
String rcsid = "$Id: SimpleConnector.java,v 1.10 2001/07/30 18:20:24 tjl Exp $";

    // A vob which has a special center.
    public interface CenteredVob {
	void getCenter(Vob.Coords coords, Point writeInto);
    }

    private static Object KEY = new Object();

    /** Get an array of all vobs in the vobscene with this key. */
    static Vob[] getArray(VobScene set, Object key) {
	int n=0;
	for(Vob v=set.get(key); v!=null; v=set.getNext(key, v))
	    n++;

	Vob[] res = new Vob[n];	
	
	int i=0;
	for(Vob v=set.get(key); v!=null; v=set.getNext(key, v))
	    res[i++] = v;
	
	return res;
    }

    // XXX Why is this static --Tjl
    public static void decorate(VobScene set, SimpleConnector connector) {
	ArrayList conns = new ArrayList();
	
	for(Iterator i = set.keys(); i.hasNext();) {
	    Object key = i.next();
	    conns.add(getArray(set, key));
	}
	
	connector.conns = conns;
    }

    protected Color col;

    /** The connections to draw.
     *  A vector of arrays of vobs. In an array, draw connections from every
     *  element to every element.
     */
    protected ArrayList conns;

    public SimpleConnector(Color col) {
	super(KEY);
	this.col = col;
    }

    public void putInto(DecoratableVobScene scene) {
	scene.setDecor(this);
    }

    public Color getColor(Vob v1, Vob.Coords c1, Vob v2, Vob.Coords c2) {
	return col;
    }

    public void render(java.awt.Graphics g, 
		       int x, int y, int w, int h,
		       boolean boxDrawn,
	               RenderInfo info) {
	if(conns == null)
	    throw new Error("Attempt to render SimpleConnector whose " +
			    "connections haven't been filled in yet");
			
	Color oldfg = g.getColor();

	Vob.Coords icoords = new Vob.Coords(), jcoords = new Vob.Coords();
	Point p = new Point();

	for(Iterator iter = conns.iterator(); iter.hasNext();) {
	    Vob[] arr = (Vob[])iter.next();
		
	    for(int i=0; i<arr.length; i++) {
		if(!info.getInterpCoords(arr[i], icoords)) continue;
		if(arr[i] instanceof CenteredVob) {
		    ((CenteredVob)arr[i]).getCenter(icoords, p);
		    icoords.x = p.x; icoords.y = p.y;
		    icoords.width = 0; icoords.height = 0;
		}
		for(int j=i+1; j<arr.length; j++) {
		    if(!info.getInterpCoords(arr[j], jcoords)) continue;
		    
		    Color c = getColor(arr[i], icoords, arr[j], jcoords);
		    if(c == null) continue;
		    g.setColor(c);
		
		    if(arr[j] instanceof CenteredVob) {
			((CenteredVob)arr[j]).getCenter(jcoords, p);
			jcoords.x = p.x; jcoords.y = p.y;
			jcoords.width = 0; jcoords.height = 0;
		    }
		    g.drawLine( icoords.x + icoords.width/2,
				icoords.y + icoords.height/2,
				jcoords.x + jcoords.width/2,
				jcoords.y + jcoords.height/2);
		}
	    }
	}
	g.setColor(oldfg);
    }

}

