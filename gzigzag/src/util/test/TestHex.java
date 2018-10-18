/*   
TestHex.java
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

package org.gzigzag.util;
import org.gzigzag.*;
import junit.framework.*;

public class TestHex extends ZZTestCase {
public static final String rcsid = "$Id: TestHex.java,v 1.2 2001/06/30 09:44:21 tjl Exp $";

    public TestHex(String name) { super(name); }

    byte[] bytes = new byte[] {0x21, (byte)0xa2, 0x35};
    String str = "21A235";

    public void testToHex() {
	String s = HexUtil.byteArrToHex(bytes);
	assertEquals(str, s);
    }

    public void testToByteArr() {
	byte[] b = HexUtil.hexToByteArr(str);
	assertEquals(bytes, b);
    }

    public void testInvalidOdd() {
	try {
	    byte[] b = HexUtil.hexToByteArr("21a2357");
	} catch(NumberFormatException e) {
	    return;
	}
	fail("Odd not detected");
    }

    public void testInvalidChar() {
	try {
	    byte[] b = HexUtil.hexToByteArr("21g235");
	} catch(NumberFormatException e) {
	    return;
	}
	fail("Invalid char not detected");
    }

}
