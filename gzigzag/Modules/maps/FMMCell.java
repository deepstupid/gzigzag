/*   
FMMCell.java
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
 * Written by Kimmo Wideroos
 */

// fmm cell

package org.gzigzag.map;
import java.util.*;

public class FMMCell {

    public int key;
    public int keylevel;
    public float xcenter, ycenter;
    private int trunc_p;
    public Complex[] a;
    public Complex[] b;
    
    public FMMCell(int key, float xc, float yc, int klevel, int p) {
	this.key = key;
	xcenter = xc;
	ycenter = yc;
	trunc_p = p;
	//n = 0;
	keylevel = klevel;
	a = new Complex[p+1];
	b = new Complex[p+1];
	for(int i=0; i<p+1; i++) {
	    a[i] = new Complex();
	    b[i] = new Complex();
	}
    }
}
