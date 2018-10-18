/*   
TestImpl.java
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

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import java.io.*;
import junit.framework.*;
import java.awt.*;
import java.util.Set;

/** Perform all currently relevant tests for org.gzigzag.impl.
 */

public class TestImpl {
public static final String rcsid = "$Id: TestImpl.java,v 1.48 2002/03/26 16:18:40 deetsay Exp $";

    public static File zdir;
    public static Mediaserver zms;

    // To check integrity
    public static Mediaserver zms0;
    public static Set zms0ids;

    static {
	try {
	    zdir = new File(System.getProperty("zdir"));
	    Storer primstor = new DirStorer(zdir);
	    zms0 = new SimpleMediaserver(primstor, 
						     new IDSpace(), 0);
	    zms0ids = zms0.getIDs();
	    Mediaserver ms1 = new SimpleMediaserver(new TransientStorer(), 
						    new IDSpace(), 0);
	    zms = new MultiplexingMediaserver(ms1, zms0);
	} catch(Throwable t) {
	    System.err.println("Exception while loading test mediaserver");
	    t.printStackTrace();
	    throw new ZZError("Exception while loading test mediaserver-- printed stack trace");
	}
    }

    public static Test suite() {
	TestSuite suite = new TestSuite("org.gzigzag.impl tests");
	suite.addTest(TestDims.suite());
	suite.addTest(org.gzigzag.impl.clasm.TestClasm.suite());
	suite.addTestSuite(TestSimpleObsTrigger.class);
	suite.addTestSuite(TestTransientTextScroll.class);
	suite.addTestSuite(TestSimpleImageScroll.class);
	suite.addTestSuite(TestSimpleSpanSet.class);
	suite.addTestSuite(TestTextSpanVob.class);
	suite.addTestSuite(TestGZZ1.class);
	suite.addTestSuite(TestPermanentSpace.class);
	suite.addTestSuite(TestCloning.class);
	suite.addTestSuite(TestCursors.class);
	suite.addTestSuite(TestSimpleSpanSpace.TSimpleSpan.class);
	suite.addTestSuite(TestSimpleSpanSpace.TPermanentSpace.class);
	suite.addTestSuite(TestMSText.class);
	suite.addTestSuite(TestGidFunction.class);
	suite.addTestSuite(TestExtEdit.class);
	suite.addTestSuite(TestGZZ1Ugliness.class);
        suite.addTestSuite(TestSynch.class);
	suite.addTestSuite(TestCellVobFactory.class);
	suite.addTestSuite(TestMSLeak.class);
	suite.addTestSuite(TestFullSpacepart.class);
	suite.addTestSuite(TestPlainVStreamDim.class);
	return suite;
    }

    public static Test suiteSlow() {
        TestSuite suite = new TestSuite("org.gzigzag.impl tests");
	suite.addTestSuite(TestReal.class);
	suite.addTestSuite(TestSlices.class);
        suite.addTestSuite(TestMerge1.class);
        suite.addTestSuite(TestPartialOrder.class);
        suite.addTestSuite(TestMSImage.class);
	suite.addTestSuite(TestSpaceImageSpans.class);
	suite.addTestSuite(TestMSLeak.class);
        return suite;
    }

    static public class TestSimpleSpanSet 
			    extends TestSpanSet {
	public TestSimpleSpanSet(String name) { super(name);} 
	public SpanSet getSpanSet() {
	    return new SimpleSpanSet();
	}
    }

    static public class TestTransientTextScroll 
			    extends TestTextScrollBlock {
	public TestTransientTextScroll(String name) { super(name);} 
	public void setUp() throws Exception {
	    mutable = new TransientTextScroll();
	    mutable2 = new TransientTextScroll();
	    super.setUp();
	}
    }

    static public class TestSimpleImageScroll 
			    extends TestImageScrollBlock  {
	public TestSimpleImageScroll(String name) { super(name);} 
	public ScrollBlock getScrollBlock(Image img, int w, int h) {
	    return new SimpleImageScroll(img, w, h);
	}
    }

}
