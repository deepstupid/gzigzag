/*   
FMM.java
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

/* an implementation of fast multipole method
 *
 * references:
 *
 * 1.)
 * "A fast algorithm for particle simulations," 
 * by L. Greengard and V. Rokhlin, 
 * Journal of Computational Physics 72 (1987), 325-348
 *
 * 2.)
 * "A Parallel Hashed Oct-Tree N-Body Algorithm,"
 * by Michael S. Warren and John K. Salmon
 * Supercomputing '93, pages 12 - 21. 
 * IEEE Computer Society, Los Alamitos, 1993
 */

package org.gzigzag.map;
import java.util.*;
import org.gzigzag.test.TestFMM;

public class FMM {

    // n! = fact[n]
    static public float fact[] = new float[100]; 
    static {
	fact[0] = 1;
	for(int i=1; i<100; i++) fact[i] = fact[i-1] * i;
    }

    // binomial coefficient
    public static float bincof(int l, int k) {
	return fact[l] / (fact[k] * fact[l-k]);
    }
    
    public interface FMMStructure {
	FMMCell[] get(int level);
	FMMCell[] getInteractionList(FMMCell fc);
	FMMCell[] getNeigbourhood(FMMCell fc); 
	FMMCell[] getChildren(FMMCell fc);
	FMMParticle[] getAllParticles();
	FMMParticle[] getParticles(FMMCell fc);
	FMMParticle getParticle(float x, float y);
	int nLevel();
	int nTerms();
	void empty();
	FMMParticle makeParticle(float x, float y, int[] q);
	void add(FMMParticle fp);
	void add(float x, float y, int[] q);
	void add(float x, float y, int q);
    }

    private static float EPSILON = (float)0.000000001;
    private static Complex ZERO = new Complex((float)0.0);
    FMMStructure fmms;
    private int p;
    private int n;
    private int qind = 0;
    private float weight = (float)1.0;
    private boolean augm = false;

    public FMM(FMMStructure fmms) {
	this.fmms = fmms;
	this.p = fmms.nTerms();
	this.n = fmms.nLevel();
    }

    // power serie of 'z' (^0...^n-1)
    public static Complex[] zpow(Complex z, int n) {
	// z0[k] = pow(child_center, k)
	Complex[] z0 = new Complex[n];
 	z0[0] = new Complex((float)1.0);
	z0[1] = new Complex(z);
	for(int i=2; i<n; i++) {
	    z0[i] = new Complex(z0[i-1]);
	    z0[i].mul(z);
	}
	return z0;
    }

    public static Complex[] zpow(float r, float i, int n) {
	return zpow(new Complex(r, i), n);
    }

    public void proceed(int qind) {
	proceed(qind, (float)1.0, false);
    }

    public void proceed(int qind, float weight) {
	proceed(qind, weight, false);
    }

    /* compute forces due to variables of index 'qind'.
     * if 'augm' is true, add fields to fields computed previously.
     * 'weight' implicates how much this dimension ('qind') is
     * having weight. 
     */
    public void proceed(int qind, float weight, boolean augm) {
	this.qind = qind;
	this.weight = weight;
	this.augm = augm;

	FMMCell fc;
	FMMCell[] fcs = fmms.get(n);

	int ibox;
     
	// step 1
	for(ibox=0; ibox<fcs.length; ibox++)
	    multipole_expansion(fcs[ibox]);

	// step2
	for(int l=n-1; l>=0; l--) {
	    fcs = fmms.get(l);
	    for(ibox=0; ibox<fcs.length; ibox++)
		upward_pass(fcs[ibox]);
	}

	// step3
	for(int l=1; l<=n-1; l++) {
	    fcs = fmms.get(l);
	    for(ibox=0; ibox<fcs.length; ibox++) {
		fc = fcs[ibox];
		local_expansion(fc);
		translate_expansion(fc);
	    }
	}

	// step 4
	fcs = fmms.get(n);
	for(ibox=0; ibox<fcs.length; ibox++)
	    local_expansion(fcs[ibox]);

        /*
	// step 6
	for(ibox=0; ibox<fcs.length; ibox++) {
	    fc = fcs[ibox];
	    FMMParticle[] fps = fmms.getParticles(fc);
	    for(int i=0; i<fps.length; i++)
		particle_interactions(fps[i], fc);
	}
	*/

	// step 6 & 7
	for(ibox=0; ibox<fcs.length; ibox++) {
	    fc = fcs[ibox];
	    if(fc.a[0].near(ZERO, EPSILON)) continue;;
	    FMMParticle[] fps = fmms.getParticles(fc);
	    for(int i=0; i<fps.length; i++) {
		if(fps[i].q(qind) == 0) continue;
		compute_fields(fps[i], fc);
		particle_interactions(fps[i], fc);
	    }
	}
    }


    /*
    public void step1() {
	FMMCell[] fcs = fmms.get(n);
	int ibox;
     
	// step 1
	for(ibox=0; ibox<fcs.length; ibox++)
	    multipole_expansion(fcs[ibox]);
    }
	
    public void step2() {
	FMMCell[] fcs;
	int ibox;
     
	// step2
	for(int l=n-1; l>=0; l--) {
	    fcs = fmms.get(l);
	    for(ibox=0; ibox<fcs.length; ibox++)
		upward_pass(fcs[ibox]);
	}
    }

    public void step3() {
	FMMCell[] fcs;
	int ibox;
     
	// step3
	//fcs = fmms.get(0);
	//for(ibox=0; ibox<fcs.length; ibox++)
	for(int l=2; l<=n-1; l++) {
	    fcs = fmms.get(l);
	    for(ibox=0; ibox<fcs.length; ibox++) {
		local_expansion(fcs[ibox]);
		translate_expansion(fcs[ibox]);
	    }
	}
    }

    public void step4() {
	FMMCell[] fcs;
	int ibox;

	// step 4
	fcs = fmms.get(n);
	for(ibox=0; ibox<fcs.length; ibox++)
	    local_expansion(fcs[ibox]);
    }
    
    public void step5() {
	FMMCell[] fcs = fmms.get(n);
	int ibox;

	// step 5
	for(ibox=0; ibox<fcs.length; ibox++) {
	    FMMParticle[] fps = fmms.getParticles(fcs[ibox]);
	    for(int i=0; i<fps.length; i++)
		particle_local_expansion(fps[i], fcs[ibox]);
	}
    }
    
    public void step6() {
	FMMCell[] fcs = fmms.get(n);
	int ibox;

	// step 6
	for(ibox=0; ibox<fcs.length; ibox++) {
	    FMMParticle[] fps = fmms.getParticles(fcs[ibox]);
	    for(int i=0; i<fps.length; i++)
		particle_interactions(fps[i], fcs[ibox]);
	}
    }

    public void step7() {
	FMMCell[] fcs = fmms.get(n);
	int ibox;
     
	// step 7
	for(ibox=0; ibox<fcs.length; ibox++) {
	    FMMParticle[] fps = fmms.getParticles(fcs[ibox]);
	    for(int i=0; i<fps.length; i++)
		compute_fields(fps[i], fcs[ibox]);
	}
    }
    */
	 
    /* See Theorem 2.1 (2)
     */
    public void multipole_expansion(FMMCell fc) {
	FMMParticle[] fps = fmms.getParticles(fc);
	int m = fps.length;
	Complex t = new Complex();
	Complex[] zk = new Complex[m];
	// a(k) = Sigma{i=1...m}(q(i))
	fc.a[0].set((float)0.0);
	for(int i=0; i<m; i++) {
	    fc.a[0].add(fps[i].q[qind]);
	    zk[i] = new Complex(fps[i].z(fc));
	}
	// no use to continue stis stage, if no particles around...
	if(fc.a[0].near(ZERO, EPSILON)) return;
	for(int k=1; k<=p; k++) {
	    fc.a[k].set((float)0.0);
	    for(int i=0; i<m; i++) {
		t.set(-fps[i].q[qind]);
		t.mul(zk[i]);
		fc.a[k].add(t);
		zk[i].mul(fps[i].z(fc));
	    }
	    fc.a[k].div((float)k);
	}
    }

    /* See Lemma 2.3 (2)
     */
    public void upward_pass(FMMCell fc) {
	FMMCell[] fccs = fmms.getChildren(fc);
	FMMCell fcc;
	Complex tmp = new Complex();
	Complex ak = new Complex();
	Complex[] z0 = new Complex[p+1];

	for(int i=0; i<p+1; i++) fc.a[i].set((float)0.0);

	for(int i=0; i<fccs.length; i++) {
	    fcc = fccs[i];
	    if(fcc.a[0].near(ZERO, EPSILON)) continue;
	    z0 = zpow(fcc.xcenter-fc.xcenter, fcc.ycenter-fc.ycenter, p+1);
	    fc.a[0].add(fcc.a[0]);
	    for(int l=1; l<=p; l++) {
		tmp.set(fcc.a[0]);
		tmp.mul(z0[l]);
		tmp.div((float)l);
		fc.a[l].sub(tmp);
		for(int k=1; k<=l; k++) {
		    ak.set(fcc.a[k]);
		    ak.mul(z0[l-k]);
		    ak.mul((float)bincof(l-1, k-1));
		    fc.a[l].add(ak);
		}
	    } 
	}
    }
    
    /*  Lemma 2.4 (2)
     */
    public void local_expansion(FMMCell fc) {
	FMMCell[] interacts = fmms.getInteractionList(fc);
	Complex tmp = new Complex();
	Complex bl = new Complex();
	Complex[] b = fc.b;
	
	for(int i=0; i<p+1; i++) fc.b[i].set((float)0.0);

	for(int i=0; i<interacts.length; i++) {
	    FMMCell fci = interacts[i];
	    Complex[] a = fci.a;
	    if(a[0].near(ZERO, EPSILON)) continue;
	    Complex[] z0 = zpow(fci.xcenter-fc.xcenter, 
	    		fci.ycenter-fc.ycenter, p+1);
	    tmp.set(z0[1]); tmp.neg(); tmp.log();
	    bl.set(a[0]);
	    bl.mul(tmp);
	    for(int k=1; k<=p; k++) {
		tmp.set(a[k]);
		tmp.div(z0[k]);
		// tmp *= (-1)^k
		if((k & 1) == 1) tmp.neg();
		bl.add(tmp);
	    }
	    b[0].add(bl);
	    for(int l=1; l<p+1; l++) {
		bl.set((float)0.0);
		for(int k=1; k<p+1; k++) {
		    tmp.set((float)bincof(l+k-1, k-1));
		    tmp.mul(a[k]);
		    tmp.div(z0[k]);
		    // tmp * (-1)^k
		    if((k & 1) == 1) tmp.neg();
		    bl.add(tmp);
		}
		bl.div(z0[l]);
		tmp.set(a[0]);
		tmp.div((float)l);
		tmp.div(z0[l]);
		bl.sub(tmp);
		b[l].add(bl);
	    }
	}
    }

    /* Lemma 2.5 (2)
     */
    public void translate_expansion(FMMCell fc) {
	FMMCell[] fccs = fmms.getChildren(fc);
	Complex tmp = new Complex();
	for(int i=0; i<fccs.length; i++) {
	    FMMCell fcc = fccs[i];
	    Complex[] z0 = zpow(fc.xcenter-fcc.xcenter, 
	    		fc.ycenter-fcc.ycenter, p+1);
	    for(int l=0; l<p+1; l++) {
		for(int k=l; k<p+1; k++) {
		    tmp.set(fc.b[k]);
		    tmp.mul((float)bincof(k, l));
		    tmp.mul(z0[k-l]);
		    if(((k-l) & 1) == 1) tmp.neg();		    
		    fcc.b[l].add(tmp);
		}
	    }
	}
    }

    public void particle_interactions(FMMParticle fp, FMMCell fc) {
	FMMCell[] fcs = fmms.getNeigbourhood(fc);
	Complex delta_field = new Complex();
	Complex dist = new Complex();
	Complex dforce = new Complex();
	Complex z = new Complex();
	Complex zk = new Complex();
	float dabs; //, dpot;
	fp.near.set((float)0.0);
	//fp.nearpot = (float)0.0; 
	for(int ibox=0; ibox<fcs.length; ibox++) {
	    FMMCell nbc = fcs[ibox];
	    FMMParticle[] nbparticles = fmms.getParticles(nbc);
	    for(int i=0; i<nbparticles.length; i++) {
		FMMParticle nbp = nbparticles[i];
		if(nbp.equals(fp)) continue;
		dist.set(nbp.x, nbp.y);
		dist.sub(new Complex(fp.x, fp.y));
		dabs = dist.abs();
		//dpot = (float)(-Math.log(dabs)*nbp.q[qind]);
		//fp.nearpot += dpot;
		dforce.set(nbp.q[qind]);
		dforce.div(dist);
		dforce.conj();
		fp.near.add(dforce);
	    }
	}
    }

    public void compute_fields(FMMParticle fp, FMMCell fc) {

	if(!augm) { 
	    fp.F.set((float)0.0);
	    //fp.potential = 0.0;
	}

	Complex zj = new Complex((float)1.0, (float)0.0);
	Complex z = new Complex(fp.z(fc));
	Complex f = new Complex();

	fp.far.set((float)0.0);
	for(int j=1; j<p+1; j++) {
	    f.set(fc.b[j]);
	    f.mul((float)j);
	    f.mul(zj);
	    fp.far.add(f);
	    zj.mul(z);
	}    

	fp.far.conj(); fp.far.neg();
	fp.far.mul(weight);
	fp.near.mul(weight);
	fp.F.add(fp.far);
	fp.F.add(fp.near);

	/*
	zj.set(1.0, 0.0);
	Complex farpot = new Complex(0.0);
	for(int j=0; j<p+1; j++) {
	    f.set(fc.b[j]);
	    f.mul(zj);
	    farpot.add(f);
	    zj.mul(z);
	}

	fp.farpot = farpot.r();

	fp.farpot *= weight;
	fp.nearpot *= weight;
	fp.potential -= fp.farpot;
	fp.potential += fp.nearpot;
	*/
    }
}










