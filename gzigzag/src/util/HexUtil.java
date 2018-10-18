/*   
HexUtil.java
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
package org.gzigzag.util;

/** Convert between byte arrays and strings of Hex digits
 */

public final class HexUtil {
public static final String rcsid = "$Id: HexUtil.java,v 1.6 2001/09/16 20:39:33 tjl Exp $";

    /** Generate a readable hex string representation of the given byte array.
     * If the array is long, show only the beginning, the part around 
     * the index ind and the end (not yet implemented).
     */
    static public String toString(byte[] b, int index) {
	if(b == null) return "null";
	StringBuffer buf = new StringBuffer("{");
	for(int i=0; i<b.length; i++) {
	    if(i != 0) buf.append(',');
	    int c = b[i];
	    if(c < 0) c += 2*128;
	    String h = Integer.toHexString(c);
	    if(h.length() == 1) h = "0"+h;
	    buf.append(h);
	}
	buf.append("}");
	return buf.toString();
    }

    static int digit(int ch) {
	if(ch >= '0' && ch <= '9')
	    return ch - '0';
	if(ch >= 'A' && ch <= 'F')
	    return ch - 'A' + 10;
	if(ch >= 'a' && ch <= 'f')
	    return ch - 'a' + 10;
	throw new NumberFormatException("Digit!");
    }

    static char character(int digit) {
	if(digit >= 0 && digit <= 9)
	    return (char)(digit + '0');
	else if(digit >= 10 && digit <= 15)
	    return (char)(digit + 'A' - 10);
	throw new Error("DigitToChar!");
    }

    static public byte[] hexToByteArr(String hex) throws NumberFormatException {
	if(hex.length() % 2 > 0)
	    throw new NumberFormatException("Odd number of digits: "+hex);

	byte[] b = new byte[hex.length() / 2];
	for(int i=0; i<b.length; i++) {
	    int i1 = digit(hex.charAt(i*2)); 
	    int i2 = digit(hex.charAt(i*2+1));
	    if(i1 < 0 || i2 < 0) 
		throw new NumberFormatException("Invalid digit");
	    b[i] = (byte)(i1 * 16 + i2);
	}
	//    b[i] = (byte)Integer.parseInt(hex.substring(i*2, i*2 + 2), 16);
		// Have to use parseInt: parseByte doesn't like
		// too large values, e.g. 0x81 since that is supposed
		// to be negative.

	return b;
    }

    /** Convert the given byte array to a hex string.
     * The converted string consists of the characters 0-9, a-f.
     */
    static public String byteArrToHex(byte[] b) {
	char[] hex = new char[b.length*2];
	int n = 0;
	for(int i=0; i<b.length; i++) {
	    int val = b[i];
	    if(val < 0) val += 2 * 128;

	    hex[n++] = character(val / 16);
	    hex[n++] = character(val % 16);
	}
	return new String(hex);
    }
}
