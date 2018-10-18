/*   
TestZipStorer.java
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
 * Written by Anne Siepelmeyer and Benja Fallenstein
 */

package org.gzigzag.mediaserver.storage;
import org.gzigzag.util.TestingUtil;
import junit.framework.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/** Abstract test for Storer implementations.
 */

public class TestZipStorer extends TestCase {
    public static final String rcsid = "$Id: TestZipStorer.java,v 1.3 2002/03/13 13:35:34 bfallenstein Exp $";

    public TestZipStorer(String name) { super(name); }

    public ZipStorer storer;
    File tmp;

    public void setUp() throws IOException {
        tmp = TestingUtil.tmpFile(new File("."));

        ZipOutputStream os = new ZipOutputStream(new FileOutputStream(tmp));

        os.putNextEntry(new ZipEntry("key0"));
        os.write(0x01); os.write(0x05);
        os.closeEntry();

        os.putNextEntry(new ZipEntry("key1"));
        os.write(0x02);
        os.closeEntry();

	os.putNextEntry(new ZipEntry("properties"));
	os.write("foo=bar\n".getBytes());
	os.closeEntry();

        os.close();

        storer = new ZipStorer(new ZipFile(tmp));
    }

    public void tearDown() throws IOException {
        tmp.delete();
    }

    public void testStoreRetrieve() throws IOException {
        InputStream is = storer.retrieve("key0");
        assertEquals(0x01, is.read());
        assertEquals(0x05, is.read());
        assertEquals(-1, is.read());
    }

    public void testKeys() throws IOException {
        Set dir = storer.getKeys();
        assertEquals(3, dir.size());

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
	assertEquals("bar", storer.getProperty("foo"));
    }
}
