/*   
Renderable.java
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


public abstract class Renderable {
public static final String rcsid = "$Id: Renderable.java,v 1.10 2001/03/17 20:13:08 bfallenstein Exp $";

    public int d;

    /** Render this thing.
     */
    abstract public void render(Graphics g);

    /** Render this thing.
     *  Simply calls render(g).
     *  @param orig The top-level graphics object without translations,
     *  	    clippings, rotations, etc.
     */
    public void render(Graphics g, Graphics orig) { render(g); }

    /** Translate the coordinates of this Renderable by (x, y)
     * I.e., add x from the x-coordinate and y from the y-coordinate.
     * This is used to translate between coordinate systems.
     * @returns False if translating is not supported.
     */
    public boolean translate(int x, int y) {
	return false;
    }


    /** Do a stable depth-sort of renderables.
     * Of two renderables on the same depth, the relative order remains.
     * Uses radix sort for extreme speed.
     */
    static public void depthSort(Renderable[] it, int nit) {
	Renderable[][] ftmp = new Renderable[][] { it, new Renderable[nit] };
	Renderable[][] ftmp2 = new Renderable[2][nit];
	Renderable[][] fs;
	int[] n = new int[] { nit, 0 };
	int[] newn = new int[2];
	int[] nex = null;
	int mx = 0;
	for(int i=0; i<nit; i++) {
	    if(it[i].d > mx) mx = it[i].d;
	}
	for(int bit = 1; mx >= bit && bit!=0; bit <<= 1) {
	    for(int bin=0; bin<2; bin++) {
		for(int i=0; i<n[bin]; i++) {
		    if(ftmp[bin][i] == null) continue;
		    int ind = (((ftmp[bin][i].d & bit) == 0) ? 0 : 1);
		    ftmp2[ind][newn[ind]++] = ftmp[bin][i];
		}
	    }
	    fs = ftmp2; ftmp2 = ftmp; ftmp = fs;
	    nex = n; n = newn; newn = nex;
	    newn[0] = newn[1] = 0;
	}
	for(int i=0; i<nit-n[0]; i++) 
	    ftmp[0][n[0]+i] = ftmp[1][i];

	if(it != ftmp[0]) {
	    System.arraycopy(ftmp[0], 0, it, 0, nit);
	}
    }
}
