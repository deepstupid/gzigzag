/*   
Data.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
 * Written by Benjamin Fallenstein
 */
package org.gzigzag;

/** An immutable list of Clang data pieces.
 * Each piece can be a cell, a string, an int, or a bool. Pieces are
 * automatically converted if possible. That way, data can be stored
 * virtually someplace, or in cells; from a list of cells, a Data object
 * can be constructed which behaves just as a list of objects containing
 * the data directly. E.g.: <code>
 * 	Data d = new Data("hi", new Integer(17), new Boolean(false));
 * </code> can be stored as <code>
 * 	new Data(cell1, cell2, cell3);
 * </code> if these cells contain the strings "hi", "17", and "false". Saving
 * a Data object into a strip of cells is simple: just do something like this:
 * <code>
 * 	for(int i=0; i<data.len(); i++)
 * 		cell[i].setText(data.s(i));
 * </code>
 * (s(i) is the method to get the ith element of a Data strip as a String.)
 */

public final class Data {
String rcsid = "$Id: Data.java,v 1.3 2001/01/07 18:21:42 bfallenstein Exp $";

    private Object[] obs;

    public Data() { obs = new Object[0]; }
    public Data(Object o1) { obs = new Object[]{o1}; }
    public Data(Object o1, Object o2) {
	obs = new Object[]{o1,o2}; }
    public Data(Object o1, Object o2, Object o3) { 
	obs = new Object[]{o1,o2,o3}; }
    public Data(Object o1, Object o2, Object o3, Object o4) { 
	obs = new Object[]{o1,o2,o3,o4}; }
    public Data(Object o1, Object o2, Object o3, Object o4, Object o5) { 
	obs = new Object[]{o1,o2,o3,o4,o5}; }

    public Data(Object[] o) {
	obs = new Object[o.length];
	System.arraycopy(o, 0, obs, 0, o.length);
    }
    public Data(Data d, Object[] o) {
	obs = new Object[d.obs.length + o.length];
	System.arraycopy(d.obs, 0, obs, 0, d.obs.length);
	System.arraycopy(o, 0, obs, d.obs.length, o.length);
    }
    public Data(Data d1, Data d2) {
	obs = new Object[d1.obs.length + d2.obs.length];
	System.arraycopy(d1.obs, 0, obs, 0, d1.obs.length);
	System.arraycopy(d2.obs, 0, obs, d1.obs.length, d2.obs.length);
    }

    public int len() { return obs.length; }

    public Object o(int at) { return obs[at]; }
    public ZZCell c(int at) { return c(obs[at]); }
    public String s(int at) { return s(obs[at]); }
    public boolean b(int at) { return b(obs[at]); }
    public int i(int at) { return i(obs[at]); }
    
    public static Object o(Object o) { return o; }
    public static ZZCell c(Object o) {
	if(!(o instanceof ZZCell))
	    throw new ZZError("Wrong clang datum "+o+": not a cell");
	return (ZZCell)o;
    }
    public static String s(Object o) {
	if(o instanceof String)
	    return (String)o;
	else if(o instanceof Integer)
	    return ""+((Integer)o).intValue();
	else if(o instanceof ZZCell)
	    return ((ZZCell)o).getText();
	else if(o instanceof Boolean) {
	    if(((Boolean)o).booleanValue())
		return "true";
	    else
		return "false";
	} else
	    return o.toString();  // That's right, right?
	    // throw new ZZError("Wrong clang datum "+o+": not a str");
    }
    public static boolean b(Object o) {
	if(o instanceof Boolean)
	    return ((Boolean)o).booleanValue();
	else {
	    String str = s(o);
	    if(str.equals("true"))
		return true;
	    else if(str.equals("false"))
		return false;
	    else
	        throw new ZZError("Wrong clang datum " + o + ": " +
			          "not a boolean");
	}
    }
    public static int i(Object o) {
	if(o instanceof Integer)
	    return ((Integer)o).intValue();
	else {
	    try {
		return Integer.parseInt(s(o));
	    } catch(NumberFormatException e) {
		throw new ZZError("Wrong clang datum " + o + ": " +
				  "not an integer");
	    }
	}
    }
}
