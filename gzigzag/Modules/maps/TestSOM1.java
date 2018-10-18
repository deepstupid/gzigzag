/*   
TestSOM1.java
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
package org.gzigzag.test;
import org.gzigzag.map.*;
import java.util.*;
import junit.framework.*;

/** Test SOM1.
  * Warning: this test contains some random elements, and may 
  * occasionally fail.
 */

public class TestSOM1 extends TestCase {
    public TestSOM1(String s) { super(s); }

    public void dump(float[][] f) {
	String s = "";
	for(int i=0; i<f.length; i++) {
	    s += "[";
	    for(int j=0; j<f[i].length; j++) {
		s += f[i][j];
		s += "\t";
	    }
	    s += "]\n";
	}
	System.out.println(s);
    }

    public void test1() {
	SOM1.SquareLattice l = new SOM1.SquareLattice(3,3);

	float[][] test = new float[l.getSize()][2];
	l.teach(new SOM1.LinearTeacher(), 3,
		    test, 1, 0, new int[] {1,0});
	dump(test);

	SOM1 s = new SOM1(5, l);
	int[][] input = new int[][] {
	    new int[] {1,0,0,0,0},
	    new int[] {0,0,1,0,0},
	    new int[] {0,0,0,1,0},
	    new int[] {0,0,0,1,1}
	};
	int[] res0 = s.findBest(input);
	ArrayList set0 = new ArrayList();
	for(int i=0; i<res0.length; i++) set0.add(new Integer(res0[i]));

	dump(s.getVec());

	for(int i=0; i<10000; i++)
	    s.teachBatch(input, null);

	dump(s.getVec());

	int[] res = s.findBest(input);
	ArrayList set = new ArrayList();
	for(int i=0; i<res.length; i++) set.add(new Integer(res[i]));

	System.out.println(" "+set0+" ||| "+set);

	// Assert that all vectors are in corners.
	for(int x=0; x<2; x++) {
	for(int y=0; y<2; y++) {
	    assertTrue("xy: "+x+" "+y+" ("+set0+")  ("+set+")",	
			set.contains(new Integer(l.getN(x*2, y*2))));
	}
	}

    }
}

