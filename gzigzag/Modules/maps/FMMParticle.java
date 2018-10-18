/*   
FMMParticle.java
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

// fmm particle (single particle, FMMCell contains these)

package org.gzigzag.map;
import org.gzigzag.*;
import java.util.*;

public class FMMParticle implements ZZMap.Particle {
    public float x, y;
    public int[] q;
    public Complex near, far, F;
    public Complex A, V;
    float priority = 0;
    private int id;
    private ZZCell cell;

    //public float nearpot, farpot, potential;

    public FMMParticle(float x, float y, int[] q, int p) {
	this.id = -1;
	this.cell = null;
	this.q = new int[q.length];
	System.arraycopy(q, 0, this.q, 0, q.length);
	this.x = x;
	this.y = y;
	near = new Complex();
	far = new Complex();
	F = new Complex();
	A = new Complex();
	V = new Complex();
	//nearpot = farpot = potential = 0.0;
    }

    public FMMParticle(int id, ZZCell c, int[] q, int p) {
	this.id = id;
	this.cell = c;
	this.q = new int[q.length];
	System.arraycopy(q, 0, this.q, 0, q.length);
	Random r = new Random(System.currentTimeMillis());
	this.x = r.nextFloat();
	this.y = r.nextFloat();
	near = new Complex();
	far = new Complex();
	F = new Complex();
	A = new Complex();
	V = new Complex();
	//nearpot = farpot = potential = 0.0;
    }

    public FMMParticle(int id, ZZCell c, float x, float y, int[] q, int p) {
	this.id = id;
	this.cell = c;
	this.q = new int[q.length];
	System.arraycopy(q, 0, this.q, 0, q.length);
	this.x = x;
	this.y = y;
	near = new Complex();
	far = new Complex();
	F = new Complex();
	A = new Complex();
	V = new Complex();
	//nearpot = farpot = potential = 0.0;
    }

    // return Complex 'z' according to 'xc' & 'yc'
    public Complex z(FMMCell fc) {
	return new Complex(x-fc.xcenter, y-fc.ycenter);
    }
    public int id() { return id; }
    public ZZCell cell() { return this.cell; }
    public int q(int ind) { return q[ind]; }
    public int[] q() { return q; }
    public void setq(int ind, int qval) { q[ind] = qval; }
    public float x() { return x; }
    public float y() { return y; }
    public void x(float new_x) { x = new_x; }
    public void y(float new_y) { y = new_y; }
    public float[] coord() { return new float[] {x, y}; }
    public void coord(float[] new_c) { x = new_c[0]; y = new_c[1]; }
    public float priority() { return priority; }
    public void setPriority(float new_p) { priority = new_p; }
    public int size() { return q.length; }

}



