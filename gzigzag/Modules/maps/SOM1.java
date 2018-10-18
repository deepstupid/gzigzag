/*   
SOM1.java
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

package org.gzigzag.map;
import java.util.*;

/** A self-organizing map stored outside the structure.
 */

public class SOM1 {

    //
    // First, a framework for pluggable algorithms
    //

    /** An interface which calculates how far the given input
      * is from the given vector.
      */
    public interface Dist {
	float dist(int[] q, float[] vec);
    }

    /** An interface to move vectors closer to inputs
     */
    public interface Teacher {
	void teach(float weight, int nth, float[][] vec, int[] q);
    }

    /** An interface that chooses which vectors to move
      * by how much.
      */
    public interface Geom {
	int getSize();
	void teach(Teacher t, int nth, float[][] vec, float weight, 
		int qid, int[] q);
    }

    /** Teach the given input vector to the given basis.
      */
    public interface Rule {
	void teach( float[][] vecFrom,
		   float[][] vecTo,
		   Dist d,
		   Geom g, 
		   Teacher teach,
		   float weight,
		   int qid,
		   int[] q);
    }


    //
    // And now, some simple implementations
    //

    /** Euclidean distance squared.
      */
    static public class EuclideanDist implements Dist {
	public float dist(int[] q, float[] vec) {
	    float dist = 0;
	    for(int i=0; i<vec.length; i++) {
		float v = vec[i];
		int qi = q[i];
		float f = vec[i] - q[i];
		dist += f*f;
	    }
	    return dist;
	}
    }

    /** Euclidean, weighted distance.
     */

    /** Move linearly towards input.
      */
    static public class LinearTeacher implements Teacher {
	float rate = 0.05f;
	public void teach(float weight, int nth, float[][] vec, int[] q) {
	    float m1 = weight * rate;
	    float m0 = 1-m1;
	    float[] v = vec[nth];
	    for(int i=0; i<v.length; i++)
		v[i] = m1 * q[i] + m0 * v[i];
	}
    }

    /** Batch: sum up weights and vectors.
      */
    static public class BatchTeacher implements Teacher {
	BatchTeacher(int n) { weights = new float[n]; }
	float[] weights;
	public void start(float[][] vec) {
	    for(int i=0; i<weights.length; i++) weights[i]=0;
	    for(int i=0; i<weights.length; i++) 
		for(int j=0; j<vec[0].length; j++)
		    vec[i][j] = 0;
	}
	public void teach(float weight, int nth, float[][] vec, int[] q) {
	    float[] v = vec[nth];
	    weights[nth] += weight;
	    for(int i=0; i<v.length; i++)
		v[i] += weight * q[i];
	}
	public void finish(float[][] vec) {
	    for(int i=0; i<weights.length; i++) {
		float t = weights[i];
		if(t > 0.001)
		    for(int j=0; j<vec[0].length; j++)
			vec[i][j] /= t;
		else
		    for(int j=0; j<vec[0].length; j++)
			vec[i][j] = 0.5f;
		    
	    }
	}
    }

    /** Square lattice, half and quarter diamonds.
      */
    static public class SquareLattice implements Geom {
	int w,h;
	float onepersig2 = 0.6f;
	int[] weightToCenter;
	float ctrWeight = 0.6f;
	public SquareLattice(int w, int h) { this.w = w; this.h = h; }
	public int getSize() { return w*h; }
	public void teach(Teacher t, int nth, float[][] vec, float weight, 
			    int qid, int[] q) {
	    int x = nth % w, y = nth / w;
	    if(weightToCenter == null ||
	       weightToCenter[qid] == 0) {
		for(int dx=-4; dx<=4; dx++) {
		    if(x + dx < 0 || x + dx >= w) continue;
		    for(int dy=-4; dy<=4; dy++) {
			if(y + dy < 0 || y + dy >= h) continue;
			int dist = dx*dx + dy*dy;
			if(onepersig2 * dist > 4) continue;
			double w = Math.exp(-onepersig2*dist);
			t.teach((float)w * weight, getN(x+dx, y+dy), vec, q);
		    }
		}
	    } else {
		float cx = (x - (w-1)/2.0f) * ctrWeight + (w-1)/2.0f;
		float cy = (y - (h-1)/2.0f) * ctrWeight + (h-1)/2.0f;
		// System.out.println("Centering: "+x+" "+y+" "+cx+" "+cy);
		x = (int)cx;
		y = (int)cy;
		for(int dx=-4; dx<=4; dx++) {
		    if(x + dx < 0 || x + dx >= w) continue;
		    for(int dy=-4; dy<=4; dy++) {
			if(y + dy < 0 || y + dy >= h) continue;
			float fx = (x+dx - cx);
			float fy = (y+dy - cy);
			float dist = fx*fx + fy*fy;
			if(onepersig2 * dist > 4) continue;
			double w = Math.exp(-onepersig2*dist);
			t.teach((float)w * weight, getN(x+dx, y+dy), vec, q);
		    }
		}

	    }
	}

	public void setWeightToCenter(int[] wtc) { weightToCenter = wtc; }

	public int getX(int n) { return n % w; }
	public int getY(int n) { return n / w; }
	public int getN(int x, int y) { return x+y*w; }
    }

    static int findMin(float[][] vec, Dist d, int[] q) {
	int imin = -1; 
	float min = 0;
	for(int i=0; i<vec.length; i++) {
	    float dist = d.dist(q, vec[i]);
	    if(imin == -1 || dist < min) {
		imin = i;
		min = dist;
	    }
	}
	return imin;
    }

    /** Winner-takes-all
      */
    static public class WTA implements Rule {
	public void teach(
		   float[][] vecFrom,
		   float[][] vecTo,
		   Dist d,
		   Geom g, 
		   Teacher teach,
		   float weight,
		   int qid, 
		   int[] q) {
	    g.teach(teach, findMin(vecFrom, d, q), vecTo, weight, qid, q);
	}
    }

    //
    // Finally, their user
    //

    /** The current vectors
     * The dimensions are [neuron][dim]
     */
    float vec[][];

    float vecnext[][];

    Dist dist = new EuclideanDist();
    BatchTeacher batchteacher;
    Teacher teacher = new LinearTeacher();
    Geom geom;
    Rule rule = new WTA();

    public Geom getGeom() { return geom; }


    public SOM1(int dim, Geom geom) {
	this.geom = geom;
	vec = new float[geom.getSize()][dim];
	vecnext = new float[geom.getSize()][dim];

	Random r = new Random();
	for(int i=0; i<vec.length; i++)
	    for(int j = 0; j<dim; j++)
		vec[i][j] = r.nextFloat();
    }


    public void teach(int[] input, int nth) {
	geom.teach(teacher, nth, vec, 1, 0, input);
    }

    public void teach(ZZMap.Particle input, int nth) {
	geom.teach(teacher, nth, vec, 1, 0, input.q());
    }

    public void teachOnline(int[][] input, float[] weights) {
	for(int i=0; i<weights.length; i++) {
	    rule.teach(vec, vec,
			dist, geom, teacher,
			weights[i], i, input[i]);
	}
    }

    public void teachOnline(ZZMap.Particle[] input, float[] weights) {
	for(int i=0; i<weights.length; i++) {
	    rule.teach(vec, vec,
			dist, geom, teacher,
			weights[i], i, input[i].q());
	}
    }

    /** Teach the map a set of input vectors as a batch
      * for one round.
      * The weights may be null.
      */
    public void teachBatch(int[][] input, float[] weights) {
	if(batchteacher == null)
	    batchteacher = new BatchTeacher(vec.length);
	batchteacher.start(vecnext);
	for(int i=0; i<input.length; i++) {
	    rule.teach(vec, vecnext,
			dist, geom, batchteacher,
			weights==null ? 1 : weights[i], 
			i, input[i]);
	}
	batchteacher.finish(vecnext);
	float[][] tmp = vec;
	vec = vecnext;
	vecnext = tmp;
    }

    public void teachBatch(ZZMap.Particle[] input, float[] weights) {
	if(batchteacher == null)
	    batchteacher = new BatchTeacher(vec.length);
	batchteacher.start(vecnext);
	for(int i=0; i<input.length; i++) {
	    rule.teach(vec, vecnext,
			dist, geom, batchteacher,
			weights==null ? 1 : weights[i], 
			i, input[i].q());
	}
	batchteacher.finish(vecnext);
	float[][] tmp = vec;
	vec = vecnext;
	vecnext = tmp;
    }

    public int[] findBest(int[][] input) {
	int[] ret = new int[input.length];
	for(int i=0; i<input.length; i++)
	    ret[i] = findMin(vec, dist, input[i]);
	return ret;
    }

    public int[] findBest(ZZMap.Particle[] input) {
	int[] ret = new int[input.length];
	for(int i=0; i<input.length; i++)
	    ret[i] = findMin(vec, dist, input[i].q());
	return ret;
    }

    public int findBest(int[] input) {
	return findMin(vec, dist, input);
    }

    public int findBest(ZZMap.Particle input) {
	return findMin(vec, dist, input.q());
    }

    public float[][] getVec() { return vec; }

}

