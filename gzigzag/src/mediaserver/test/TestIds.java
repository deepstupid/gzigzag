/*   
TestIds.java
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

package org.gzigzag.mediaserver.ids;
import org.gzigzag.mediaserver.*;
import org.gzigzag.util.*;
import junit.framework.*;

public class TestIds extends ZZTestCase {
public static final String rcsid = "$Id: TestIds.java,v 1.8 2002/03/28 20:45:40 bfallenstein Exp $";

    public TestIds(String name) { super(name); }

    IDSpace ids42;

    public void setUp() {
	ids42 = new IDSpace();
    }

    public void testArrs() {
	// This works because of ZZTestCase
	assertEquals("Eq", new byte[] { 3, 2, 1 }, new byte[] { 3, 2, 1 });
    }

    static byte b(int i) { return (byte)i; }

    public void testCreateID() {
	byte[] data = new byte[] {10, 11, 12};
	
	Mediaserver.Id id = ids42.createID(data);
	Mediaserver.Id expectedId = new Mediaserver.Id("01BE99D8769B726224B8042344B400AE2F5DF5680E");

	assertEquals("Id42", expectedId.getBytes(), id.getBytes());

	data = new byte[] {11, 11, 12}; // change one bit
	id = ids42.createID(data);
	expectedId = new Mediaserver.Id("014E7539E91D067A6C483CB4C5A610C55695E40C0B");

	assertEquals("Id42", expectedId.getBytes(), id.getBytes());
    }

    public void testCheck() {
	byte[] data = new byte[] {10, 11, 12};
	byte[] spoofdata = new byte[] {11, 11, 12};
	byte[] id = ids42.createID(data).getBytes();

	Mediaserver.Id id0 = new Mediaserver.Id(id);
	assertTrue(ids42.checkData(id0, data));
	assertTrue(!ids42.checkData(id0, spoofdata));

	id[id.length-2] ++;
	id0 = new Mediaserver.Id(id);
	assertTrue(!ids42.checkData(id0, data));
    }

}
