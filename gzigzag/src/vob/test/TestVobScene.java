/*   
TestVobScene.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag.vob;
import junit.framework.*;

/** A unit test for VobScenes.
 */

abstract public class TestVobScene extends TestCase {
String rcsid = "$Id: TestVobScene.java,v 1.11 2001/06/09 15:31:36 tjl Exp $";
    public TestVobScene(String s) { super(s); }

    int NVOBS;
    TestVob1[] vobs;
    TestVob2[] vobs2;
    Object[] keys;

    int inds[];
    int inds2[];
    int cur = 1;

    VobScene vobset;
    VobScene vobset2;
    VobScene vobset3;

    int[] order;

    void insert1(VobScene sc, int i) {
	sc.put(vobs[i], i, i*100, i*101,
		    i*131, i*526);
    }

    public void setUp() {
	    NVOBS = 20;
	    vobs = new TestVob1[NVOBS];
	    vobs2 = new TestVob2[NVOBS];
	    keys = new Object[NVOBS];
	    for(int i=0; i<NVOBS; i++)  {
		    if(i < NVOBS - 2)
			keys[i] = new Object();
		    vobs[i] = new TestVob1(keys[i], i); 
		    vobs2[i] = new TestVob2(keys[i], i); 
	    }
	
	    inds = new int[NVOBS];
	    inds2 = new int[NVOBS];
	    cur = 1;
	
	    vobset = getVobScene();
	    vobset2 = getVobScene();
	    vobset3 = getVobScene();

	    order = new int[] {1,2,0,3,9,4,6,5,8,7,11,19,18,13};
	    for(int i=0; i<order.length; i++) {
		    insert1(vobset, order[i]);
		    vobset2.put(vobs2[order[i]], order[i], order[i]*2, order[i]*4,
				order[i]*6, order[i]*8);
	    }
	
	    testVobCoords = false;
    }

    public abstract VobScene getVobScene();

    Vob.Coords coords = new Vob.Coords();
    boolean testVobCoords;

    /** A basic class that numbers them in the order they're
     * drawn.
     */
    private class TestVob1 extends Vob {
	TestVob1(Object key, int i) { super(key); this.ind = i; }
	int ind;
	public void render(java.awt.Graphics g, 
				int x, int y, int w, int h,
				boolean boxDrawn,
				RenderInfo info
				) {
	    if(!testVobCoords) {
		assertEquals("x @"+ind+" ", x, ind*100);
		assertEquals("y @"+ind+" ", y, ind*101);
		assertEquals("w @"+ind+" ", w, ind*131);
		assertEquals("h @"+ind+" ", h, ind*526);
	    }

	    if(testVobCoords) {
	        assertTrue(info.getInterpCoords(this, coords));
		assertEquals("ip "+ind, x, coords.x);
		assertEquals("ip "+ind, y, coords.y);
		assertEquals("ip "+ind, w, coords.width);
		assertEquals("ip "+ind, h, coords.height);
	    }
	    inds[ind] = cur++;
	}

	public String toString() {
	    return "[TV1: "+ind +"]";
	}
    }

    /** A basic class that numbers them in the order they're
     * drawn and checks the interpolated coordinates.
     */
    private class TestVob2 extends Vob {
	TestVob2(Object key, int i) { super(key); this.ind = i; }
	int ind;
	public void render(java.awt.Graphics g, 
				int x, int y, int w, int h,
				boolean boxDrawn,
				RenderInfo info
				) {
	    assertEquals("x @"+ind+" ", x,
			 (int)(0.5 + 0.25 * ind*100 + 0.75 * ind * 2));
	    assertEquals("y @"+ind+" ", y, 
			 (int)(0.5 + 0.25 * ind*101 + 0.75 * ind * 4));
	    assertEquals("w @"+ind+" ", w, 
			 (int)(0.5 + 0.25 * ind*131 + 0.75 * ind * 6));
	    assertEquals("h @"+ind+" ", h, 
			 (int)(0.5 + 0.25 * ind*526 + 0.75 * ind * 8));

	    if(testVobCoords) {
	        assertTrue(info.getInterpCoords(this, coords));
		assertEquals("ip "+ind, x, coords.x);
		assertEquals("ip "+ind, y, coords.y);
		assertEquals("ip "+ind, w, coords.width);
		assertEquals("ip "+ind, h, coords.height);
	    }
	    inds2[ind] = cur++;
	}
	public String toString() {
	    return "[TV2: "+ind +"]";
	}
    }

    public void testDepthSort() {
	vobset.render(null, null, null, null, 0);
	doInds(inds, order.length);
    }

    /** Check that the non-zero entries in inds are in descending order,
     * and that there are norder of them.
     * Used to check that the vobs have been rendered in the correct
     * order, least-depth ones last.
     */
    void doInds(int[] inds, int norder) {
	int cur = 1000;
	int n = 0;
	for(int i=0; i<inds.length; i++) {
	    if(inds[i] > 0) {
		if(inds[i] > cur) {
		    String s = " ";
		    for(int k = 0; k < inds.length; k++) 
			s += inds[k] + " ";
		    fail("Not sorted right (should be descending)! "
			+inds[i]+" "+cur+" "+
			    i + " ("+s+")");
		}
		cur = inds[i];
		n++;
	    }
	}
	if(n != norder)
	    fail("Wrong number of renders "+n+" "+norder);
    }

    public void testGet() {
	for(int i=0; i<order.length; i++) {
	    Vob g = vobset.get(keys[order[i]]);
	    if(g == null) {
		if(vobs[order[i]].key != null)
		    fail("Key not null but null vob");
	    } else {
		assertEquals("vob get", vobs[order[i]], g);
	    }
	}
	if(vobset.get(new Object()) != null)
	    fail("Get for no object");
    }

    public void testInterpolate() {
	vobset2.render(null, null, null, vobset, 0.25f);
	doInds(inds2, order.length - 2);
    }

    public void testInterpolateToSelf() {
	vobset.render(null, null, null, vobset, 0.1f);
	vobset.render(null, null, null, vobset, 0.25f);
	vobset.render(null, null, null, vobset, 0.7f);
    }

/*
    public void testInterpolateVobCoords() {
	testVobCoords = true;
	vobset2.render(null, null, null, vobset, 0.25f);
	doInds(inds2, order.length - 2);
    }

    public void testInterpolateToSelfVobCoords() {
	testVobCoords = true;
	vobset.render(null, null, null, vobset, 0.1f);
	vobset.render(null, null, null, vobset, 0.25f);
	vobset.render(null, null, null, vobset, 0.7f);
    }
*/

    Object strangeHash = new Object() {
	public int hashCode() { return -42; }
    };

    // Some hash code had trouble with negative hashCodes.
    // Test.
    Vob strangeHashVob = new TestVob2(strangeHash, 53);
    public void testAddNegHashcode() {
	vobset.put(strangeHashVob, 10, 20, 30, 40, 50);
    }

    public void testSubScene1() {
	assertTrue(vobset.createSubScene(new Object(), null, 100, 100) != null);
    }

/*
    public void testSubScene2() {
	int[] order0 = new int[] { 12,0,1,14,3, 2,11,15,4,16 };
	// Depth 5 : subscene
	int[] order1 = new int[] { 6,9,8,7,10 };

	int nthins = 5;

	for(int i=0; i<nthins; i++) {
	    insert1(vobset3, order0[i]);
	}

	VobBox sub = vobset3.createSubScene(new Object(), null, 100, 100);
	for(int i=0; i<order1.length; i++) {
	    insert1(sub, order1[i]);
	}
	vobset3.put(sub, 5, 20, 20, 200, 200);

	for(int i=nthins; i<order0.length; i++) {
	    insert1(vobset3, order0[i]);
	}

	vobset3.render(null, null, null, null, 0);
	doInds(inds, order0.length + order1.length);
    }
*/

}



