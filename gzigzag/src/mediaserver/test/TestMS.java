/*   
TestMS.java
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

package org.gzigzag.mediaserver;
import java.io.*;
import org.gzigzag.util.*;
import junit.framework.*;

/** Perform all currently relevant tests for org.gzigzag.mediaserver.
 */

public class TestMS {
public static final String rcsid = "$Id: TestMS.java,v 1.19 2002/03/11 12:48:13 bfallenstein Exp $";

    public static Test suite() {
	TestSuite suite = new TestSuite("Mediaserver tests");
	suite.addTestSuite(org.gzigzag.mediaserver.ids.TestIds.class);
	suite.addTestSuite(org.gzigzag.mediaserver.http.server.TestServerHTTPResponse.class);
	suite.addTestSuite(org.gzigzag.mediaserver.http.server.TestServerHTTPRequest.class);
	suite.addTestSuite(org.gzigzag.mediaserver.http.TestHTTPUtil.class);
	suite.addTestSuite(TestTransientStorer.class);
	suite.addTestSuite(TestSimpleMediaserver.class);
	suite.addTestSuite(TestMultiplexingMediaserver.class);
	suite.addTestSuite(TestDirStorer.class);
	suite.addTestSuite(TestDirStorerMaxlen.class);
	suite.addTestSuite(org.gzigzag.mediaserver.storage.TestZipStorer.class);
	return suite;
    }

    static public class TestTransientStorer 
      extends org.gzigzag.mediaserver.storage.TestStorer {
        public TestTransientStorer(String s) { super(s); }
	public void setUp() {
	    storer = new org.gzigzag.mediaserver.storage.TransientStorer();
	}
    }

    static public class TestSimpleMediaserver extends TestMediaserver {
	public TestSimpleMediaserver(String s) { super(s); }
	public void setUp() {
	    ms = new SimpleMediaserver(
		        new org.gzigzag.mediaserver.storage.TransientStorer(),
			new org.gzigzag.mediaserver.ids.IDSpace(),
			0);
	}
    }

    static public class TestMultiplexingMediaserver 
	extends TestMediaserver {
	public TestMultiplexingMediaserver(String s) { super(s); }
	public void setUp() {
	    ms = new MultiplexingMediaserver(new SimpleMediaserver(
		        new org.gzigzag.mediaserver.storage.TransientStorer(),
			new org.gzigzag.mediaserver.ids.IDSpace(), 0),
						 new SimpleMediaserver(
		        new org.gzigzag.mediaserver.storage.TransientStorer(),
			new org.gzigzag.mediaserver.ids.IDSpace(), 0)
						 );
	}
    }

    static public class TestDirStorer
		extends org.gzigzag.mediaserver.storage.TestStorer {

	File dir;

        public TestDirStorer(String s) { super(s); }
	public org.gzigzag.mediaserver.storage.Storer getStorer() throws IOException {
	    return new org.gzigzag.mediaserver.storage.DirStorer(dir);
	}
	public void setUp() throws IOException {
	    dir = TestingUtil.tmpFile(new File("."));
	    dir.mkdir();
	    storer = getStorer();
	}
	public void tearDown() {
	    TestingUtil.deltree(dir);
	}
	public void testCVSDirectory() throws IOException {
	    new File(dir, "CVS").mkdir();
	    assertTrue(!storer.getKeys().contains("CVS"));
	}
    }

    static public class TestDirStorerMaxlen extends TestDirStorer {
        public TestDirStorerMaxlen(String s) { super(s); }
        public org.gzigzag.mediaserver.storage.Storer getStorer() throws IOException {
            return new org.gzigzag.mediaserver.storage.DirStorer(dir, 3);
        }
    }

}
