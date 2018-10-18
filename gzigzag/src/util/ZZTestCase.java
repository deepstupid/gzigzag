/*   
ZZTestCase.java
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
import java.util.*;
import junit.framework.*;

/** An extended version of junit.framework.TestCase implementing
 * some assertions about e.g.arrays.
 * Some of the code resembles the corresponding code in junit a lot...
 */

public class ZZTestCase extends TestCase {
public static final String rcsid = "$Id: ZZTestCase.java,v 1.3 2001/10/15 17:24:03 tjl Exp $";

    public ZZTestCase(String s) { super(s); }

    public void assertEquals(String message, byte[] expected, byte[] actual) {
	if (expected == null && actual == null)
		return;
	if (expected == null || actual == null)
		return;
	if (expected.length != actual.length) 
	    failNotEquals(message, HexUtil.toString(expected,0), 
				   HexUtil.toString(actual,0));
	for(int i=0; i<expected.length; i++) 
	    if(expected[i] != actual[i])
		failNotEquals(message, HexUtil.toString(expected,i), 
					HexUtil.toString(actual,i));
    }

    public void assertEquals(byte[] expected, byte[] actual) {
	assertEquals(null, expected, actual);
    }

    void failNotEquals(String message, String expected, 
				String actual) {
	String formatted= "";
	if (message != null)
		formatted= message+" ";
	fail(formatted+"expected:<"+expected+"> but was:<"+actual+">");
    }


    /** Assert that the given collection contains exactly the Integer object
     * given in the array, no others and no nulls.
     */
    public void assume(Collection coll, int[] objs) {
	if(coll == null) {
	    if(objs != null && objs.length != 0)
		fail("Null collection, did have objects");
	    return;
	}
	for(Iterator i = coll.iterator(); i.hasNext(); ) {
	    Integer ret = (Integer)i.next();
	    if(ret == null)
		fail("Null in return");
	    int num = ret.intValue();
	    if(num < 0) fail("Neg?!?!");
	    for(int j=0; j<objs.length; j++)
		if(objs[j] == num) {
		    num = -1;
		    objs[j] = -1;
		}
	    if(num >= 0)
		fail("RET NOT EXPECTED! "+num);
	}
	for(int j=0; j<objs.length; j++)
	    if(objs[j] >= 0)
		fail("DIDN'T RETURN "+objs[j]);
    }

}
