/*   
TestVobs.java
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
import junit.framework.*;

/** Perform all currently relevant tests for org.gzigzag.vob.
 */

public class TestVobs {
public static final String rcsid = "$Id: TestVobs.java,v 1.20 2002/03/08 21:14:39 bfallenstein Exp $";

    public static junit.framework.Test suiteStable() {
	TestSuite suite = new TestSuite("Stable Vob tests");
	suite.addTestSuite(TestSimpleVobScene.class);
        suite.addTestSuite(TestTrivialVobScene.class);
	suite.addTestSuite(TestLBChain.class);
        suite.addTestSuite(TestWordBreaker.class);
	suite.addTestSuite(TestCharArrayVobFactory.class);
	suite.addTestSuite(TestBuoy2.class);
	return suite;
    }

    public static junit.framework.Test suiteUnstable() {
	TestSuite suite = new TestSuite("Unstable Vob tests");
	suite.addTestSuite(TestLinkedListVobScene.class);
	suite.addTestSuite(TestTextStyle.class);
	return suite;
    }



    //
    // Classes that implement generic tests
    //

    static public class TestSimpleVobScene extends TestVobScene {
	public TestSimpleVobScene(String s) { super(s); }
	public VobScene getVobScene() {
	    return new SimpleVobScene();
	}
    }

    static public class TestTrivialVobScene extends TestVobScene {
        public TestTrivialVobScene(String s) { super(s); }
        public VobScene getVobScene() {
            return new TrivialVobScene(new java.awt.Dimension(400, 400));
        }
    }

    static public class TestLinkedListVobScene extends TestVobScene {
	public TestLinkedListVobScene(String s) { super(s); }
	public VobScene getVobScene() {
	    return new LinkedListVobScene(new java.awt.Dimension(400, 400));
	}
    }
}



