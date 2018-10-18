/*   
FMMHashTree.java
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

// an implementation of FMM.FMMStructure

package org.gzigzag.map;
import java.util.*;

public class FMMHashTree implements FMM.FMMStructure {

    // number of terms in truncated series
    private int p; 
    // number of levels in tree
    private int n; 
    public FMMCell[] fmmcells;
    public Hashtable fmmparticles;
    private float xmin, xmax, ymin, ymax;
    
    public FMMHashTree(float xmin, float xmax, float ymin, float ymax, 
		       int N, float err) {
	fmmparticles = new Hashtable();
	// adjust p
	p = 1; 
	float tmp_err = (float)1.0;
	while(err<tmp_err) { p++; tmp_err /= 2.0; }
	this.xmin = xmin;
	this.xmax = xmax;
	this.ymin = ymin;
	this.ymax = ymax;
	n = 0;
	int N_max = 1; 
	while(N>N_max) { N_max <<= 2; n++; }
	init();
    }

    /* store all cells in tree structure
     */
    private void init() {
	int b = 1<<((n<<1)+1);
	fmmcells = new FMMCell[b];
	float[] c = new float[2];
	for(int l=0; l<=n; l++) {
	    b = 1<<(l<<1);
	    for(int k=b-1; k>=0; k--) {
		int key = k | b;
		breakKey(key, l, c);
		fmmcells[key] = new FMMCell(key, c[0], c[1], l, p);
	    }
	}
    }

    public int nLevel() { return n; }    
    public int nTerms() { return p; }

    public float xmin() { return xmin; }
    public float xmax() { return xmax; }
    public float ymin() { return ymin; }
    public float ymax() { return ymax; }

    public FMMCell[] get(int level) {
	int i, b = (1 << (level<<1));
	FMMCell[] fcs = new FMMCell[b];
	for(i=0; i<b; i++) fcs[i] = fmmcells[i | b];
	return fcs;
    }

    /*  children of 'fc'
     */
    public FMMCell[] getChildren(FMMCell fc) {
	int klevel=fc.keylevel;
	if(klevel>=n) return new FMMCell[] {};
	int[] ks = breakKey(fc.key, klevel);
	int i, nkey, ni, nj;
	float cx, cy;
	ni = ks[1]<<1;
	nj = ks[0]<<1;
	FMMCell[] fccs = new FMMCell[4];
	for(i=0; i<4; i++) {
	    nkey = buildKey(nj+(i & 1), ni+((i & 2)>>1), klevel+1);
	    fccs[i] = fmmcells[nkey];
	}
	return fccs;
    }

    public FMMCell getParent(FMMCell fc) {
	int klevel = fc.keylevel;
	if(klevel==0) return null;
	return fmmcells[transformKey(fc.key, klevel, klevel-1)];	
    }

    private final int[][] a = new int[][] {
	{6, 12, 18, 36},
	{12, 18, 24, 36},
	{7, 13, 19, 36},
	{13, 19, 25, 36}
    };
    
    /* 
     * return list of cells which 'x' should interact with.
     * (see the fig. below: 'i's)
     *
     *    ........
     *  ...iiiiii..
     *   ..iiiiii..
     *   ..i...ii...
     *  ...i.x.ii..
     *  ...i...ii..
     *  ...iiiiii..
     *    ........
     */
    public FMMCell[] getInteractionList(FMMCell fc) {
	int x, y, key, keylevel;
	key = fc.key;
	int[] keystuff = breakKey(key);
	x = keystuff[0];
	y = keystuff[1];
	keylevel = keystuff[2];
	Vector iv = new Vector();
	// q is index in 'a' table. 'a' in turn 
	// tells positions where to skip 3 steps
	// (in order to dismiss closest neighbourhood) 
	int q = ((x & 1) << 1) + (y & 1);
	int j0 = x - (x&1) - 2, j1 = j0+6;
	int i, j, m, n, k, cutp;
	m = n = k = 0; 
	i = y - (y&1) - 2;
	j = j0;
	cutp = a[q][k];
	while(m<36) {
	    key = buildKey(j, i, keylevel);
	    if(key>0) iv.add(fmmcells[key]);
	    if(m==cutp) {
		// dismiss nearest neighbours
		m += 3;
		j += 3;
		k++;
		cutp = a[q][k];
	    }
	    j++;
	    m++;
	    if(j==j1) { j=j0; i++; } 
	}
	FMMCell[] ilist = new FMMCell[iv.size()];
	for(i=0; i<ilist.length; i++)
	    ilist[i] = (FMMCell)iv.elementAt(i);
	return ilist;
    }
 
    /*  closest neighbours of 'fc' and fc itself
     */
    public FMMCell[] getNeigbourhood(FMMCell fc) {
	int x, y, key, keylevel;
	key = fc.key;
	int[] keystuff = breakKey(key);
	x = keystuff[0];
	y = keystuff[1];
	keylevel = keystuff[2];
	Vector nv = new Vector();
	int i, j, j0, j1;
	i = y-1;
	j = j0 = x-1; j1 = x+2;
	for(int k=0; k<9; k ++) {
	    key = buildKey(j, i, keylevel);
            if(key>0) nv.add(fmmcells[key]);
	    j++;
	    if(j==j1) { j=j0; i++; } 
	}
	FMMCell[] ilist = new FMMCell[nv.size()];
	for(i=0; i<ilist.length; i++) {
	    ilist[i] = (FMMCell)nv.elementAt(i);
	}
	return ilist;
    }
    
    
    /* d is between r[0] ... r[1]
     * return value is keyvalue for interval (level n) 
     * containing d. r[0] refers to the min value of 
     * that interval, r[1] max.
     * Example. d = 1.5, r[0] = 0.0, r[1] = 4.0, n = 2
     * yields to r[0] = 1.0, r[1] = 2.0, keyvalue = 01
     */
    public int makeKeyPart(float d, float[] r) {
	float mid;
	int k = 0;
	mid = (float)((r[0]+r[1])/2.0);
	if(d>mid) { k = 1; r[0] = mid; }
	else { r[1] = mid; }
	for(int i=1; i<n; i++) {
	    k <<= 1;
	    mid = (float)((r[0]+r[1])/2.0);
	    if(d>mid) { k |= 1; r[0] = mid; }
	    else { r[1] = mid; }
	}
	return k;
    }
        
    public int makeKey(float x, float y, float[] center) {
	float[] xrange = new float[] {xmin, xmax}; 
	float[] yrange = new float[] {ymin, ymax}; 
	int k = ((makeKeyPart(y, yrange) | (1<<n))<<n) | 
	          makeKeyPart(x, xrange);
	center[0] = (float)((xrange[0]+xrange[1]) / 2.0);
	center[1] = (float)((yrange[0]+yrange[1]) / 2.0);
	return k;
    }

    // keyvalue '0' is invalid
    public int buildKey(int xkey, int ykey, int level) {
	int b = (1<<level);
	if(xkey < 0 || ykey < 0 || xkey >= b || ykey >= b) return 0;
	return ((ykey | b) << level) | xkey;	
    }

    public int transformKey(int key, int level, int new_level) {
	int xkey, ykey, shift;
	int[] ks = breakKey(key, level);
	shift = new_level-level;
	if(shift > 0) {
	    xkey = ks[0] << shift;
	    ykey = ks[1] << shift;
	} else {
	    shift = -shift;
	    xkey = ks[0] >> shift;
	    ykey = ks[1] >> shift;
	}
	return buildKey(xkey, ykey, new_level);
    }
    
    public int[] breakKey(int key) {
	int btmp = 1 << n, level = n;
	// check level;
	if(key>btmp) {
	    btmp <<= 1;
	    while(key>=btmp) { level ++; btmp <<= 1; }
	} else {
	    btmp >>= 1;
	    while(btmp>=key) { level --; btmp >>= 1; }
	}
	return breakKey(key, level>>1);
    }

    public int[] breakKey(int key, int level) {
	int mask = (1<<(level))-1;
	int[] r = new int[3];
	r[0] = mask & key;
	r[1] = mask & (key>>level);
	r[2] = level;
	return r;
    }

    public int[] breakKey(int key, float[] center) {
	int[] r = breakKey(key);
	center[0] = xcenter(r[0], r[2]);
	center[1] = ycenter(r[1], r[2]);
	return r;
    }

    public int[] breakKey(int key, int level, float[] center) {
	int[] r = breakKey(key, level);
	center[0] = xcenter(r[0], level);
	center[1] = ycenter(r[1], level);
	return r;
    }

    public float xcenter(int partial_key, int level) {
	return (float)(xmin + (xmax-xmin) 
		       * (2.0*partial_key+1.0)/(1<<(level+1)));
    }

    public float ycenter(int partial_key, int level) {
	return (float)(ymin + (ymax-ymin) 
		       * (2.0*partial_key+1.0)/(1<<(level+1)));
    }

    public void add(FMMParticle fp) {
	int key;
	int[] keystuff;
	float xc, yc;
	float[] c = new float[] {(float)0.0, (float)0.0};
	key = makeKey(fp.x, fp.y, c);
	addParticle( fmmcells[key], fp );
    }


    public FMMParticle makeParticle(float x, float y, int q) {
	return new FMMParticle(x, y, new int[] {q}, p);
    }

    public FMMParticle makeParticle(float x, float y, int[] q) {
	return new FMMParticle(x, y, q, p);
    }

    public void add(float x, float y, int q) {
	add(makeParticle(x, y, q));
    }

    public void add(float x, float y, int[] q) {
	add(makeParticle(x, y, q));
    }

    /* Add particle 'fp' into cell 'fc'. Tree leaves can contain
     * several particles (not recommended...)
     */
    private void addParticle(FMMCell fc, FMMParticle fp) {
	if(!fmmparticles.containsKey(fc)) 
	    fmmparticles.put(fc, new FMMParticle[] {fp});
	else {
	    FMMParticle[] curpl = (FMMParticle[])fmmparticles.get(fc);
	    FMMParticle[] newpl = new FMMParticle[curpl.length+1];
	    for(int i=0; i<curpl.length; i++) newpl[i] = curpl[i];
	    newpl[curpl.length] = fp;
	    fmmparticles.put(fc, newpl);
	}
    }
    
    public void empty() {
	fmmparticles.clear();
    }

    public FMMParticle getParticle(float x, float y) {
	float[] c = new float[2];
	int key = makeKey(x, y, c);

	FMMCell fc = fmmcells[key];
	if(!fmmparticles.containsKey(fc)) return null;
	FMMParticle[] fps = (FMMParticle[])fmmparticles.get(fc);

	int nearest = -1; 
	float d, dist =  (float)100000000.0;
	for(int i=0; i<fps.length; i++) {
	    FMMParticle fp2 = fps[i];
	    d = (fp2.x-x) > 0 ? fp2.x-x : x-fp2.x;
	    d += (fp2.y-y) > 0 ? fp2.y-y : y-fp2.y;
	    if(d<dist) { nearest = i; dist = d; }
	}
	if(nearest == -1) return null;
	return fps[nearest];
    }

    public FMMParticle[] getParticles(FMMCell fc) {
	if(!fmmparticles.containsKey(fc)) {
	    return new FMMParticle[] {};
	}
	return (FMMParticle[])fmmparticles.get(fc);
    }

   public FMMParticle[] getAllParticles() {
       Vector fpv = new Vector();
       FMMParticle[] fps, fps0;
       int i;
       for(Enumeration e=fmmparticles.elements(); e.hasMoreElements(); ) {
	   fps0 = (FMMParticle[])e.nextElement();
	   for(i=0; i<fps0.length; i++) {
	       fpv.add(fps0[i]);
	   }
       }
       fps = new FMMParticle[fpv.size()];
       for(i=fps.length-1; i>=0; i--) {
	   fps[i] = (FMMParticle)fpv.elementAt(i);
       }
       return fps;
   }
    

}


