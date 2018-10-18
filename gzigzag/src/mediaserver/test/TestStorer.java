/*   
TestStorer.java
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

package org.gzigzag.mediaserver.storage;
import junit.framework.*;
import java.io.*;
import java.util.*;

/** Abstract test for Storer implementations.
 */

public abstract class TestStorer extends TestCase {
public static final String rcsid = "$Id: TestStorer.java,v 1.5 2002/03/13 13:35:34 bfallenstein Exp $";

    public TestStorer(String name) { super(name); }

    public Storer storer;

    public void testStoreRetrieve() throws IOException {
	OutputStream os = storer.store("key0");
	os.write(0x01); os.write(0x05);
	os.close();

	InputStream is = storer.retrieve("key0");
	assertEquals(0x01, is.read());
	assertEquals(0x05, is.read());
	assertEquals(-1, is.read());
    }

    public void testKeys() throws IOException {
	assertEquals(0, storer.getKeys().size());

	OutputStream os = storer.store("key0");
	os.write(0x01);
	os.close();

	os = storer.store("key1");
	os.write(0x02);
	os.close();

	Set dir = storer.getKeys();
	assertEquals(2, dir.size());

	assertTrue(dir.contains("key0"));
	assertTrue(dir.contains("key1"));
	assertTrue(!dir.contains("key2"));
    }

    /** Test that retrieve() returns null if a key isn't there.
     */
    public void testRetrieveNull() throws IOException {
        assertEquals(null, storer.retrieve("DO_NOT_CREATE_THIS_FILE"));
    }

    public void testProperties() throws IOException {
	storer.setProperty("foo", "bar");
        assertEquals("bar", storer.getProperty("foo"));
    }
}



