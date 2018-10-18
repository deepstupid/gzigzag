/*   
TestObsTrigger.java
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

package org.gzigzag;
import junit.framework.*;

/** Test ObsTrigger basic API.
 * Like the other core test classes, this is an abstract
 * test class - testing implementations of ObsTrigger is achieved
 * by subclassing and giving an instance of ObsTrigger to o.
 */

public abstract class TestObsTrigger extends TestCase {
public static final String rcsid = "$Id: TestObsTrigger.java,v 1.2 2001/06/09 15:31:36 tjl Exp $";

    public TestObsTrigger(String name) { super(name); }

    public ObsTrigger o;
    
    Object obj1 = new Object();
    Object obj2 = new Object();

    String id1 = "1";
    String id2 = "2";
    String id3 = "3";

    final int NOBS = 10;
    int[] obsResp = new int[NOBS];
    Obs[] obs = new Obs[NOBS];
    {
	for(int i=0; i<NOBS; i++) obs[i] = new O(i);
    }
    
    class O implements Obs {
	O(int i) { this.i = i; }
	int i;
	public void chg() {
	    synchronized(TestObsTrigger.this) {
		obsResp[i]++;
	    }
	}
    }


    public void assertResp(int[] ass) {
	for(int i=0; i<ass.length; i++) 
	    assertTrue(obsResp[i] == ass[i]);
    }

    public void testSimple() {
	o.addObs(obs[0], obj1, id1);
	assertResp(new int[] {0,0,0,0,0});
	o.chg(obj1, id1);
	assertResp(new int[] {0,0,0,0,0});
	o.callQueued();
	assertResp(new int[] {1,0,0,0,0});
    }

    public void testRemove() {
	o.addObs(obs[0], obj1, id1);
	o.chg(obj1, id1);
	o.callQueued();
	assertResp(new int[] {1,0,0,0,0});
	o.callQueued();
	assertResp(new int[] {1,0,0,0,0});
	o.chg(obj1, id1);
	o.callQueued();
	assertResp(new int[] {1,0,0,0,0});
	o.addObs(obs[0], obj1, id1);
	o.chg(obj1, id1);
	o.callQueued();
	assertResp(new int[] {2,0,0,0,0});
	o.callQueued();
    }

    public void testTwo() {
	o.addObs(obs[0], obj1, id1);
	o.addObs(obs[1], obj1, id1);
	o.chg(obj1, id1);
	o.callQueued();
	assertResp(new int[] {1,1,0,0,0});
	o.callQueued();
	assertResp(new int[] {1,1,0,0,0});
	o.chg(obj1, id1);
	o.callQueued();
	assertResp(new int[] {1,1,0,0,0});
	o.addObs(obs[1], obj1, id1);
	o.chg(obj1, id1);
	o.callQueued();
	assertResp(new int[] {1,2,0,0,0});
	o.callQueued();
    }

    public void testRemoveOne() {
	o.addObs(obs[0], obj1, id1);
	o.addObs(obs[1], obj1, id1);
	o.addObs(obs[2], obj1, id1);

	o.addObs(obs[1], obj1, id2);

	o.chg(obj1, id2);
	o.callQueued();
	assertResp(new int[] {0,1,0,0,0});

	o.chg(obj1, id1);
	o.callQueued();
	assertResp(new int[] {1,1,1,0,0});

	o.chg(obj1, id1);
	o.chg(obj1, id2);
	o.callQueued();
	assertResp(new int[] {1,1,1,0,0});
	
    }

}

