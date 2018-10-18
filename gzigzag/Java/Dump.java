/*   
Dump.java
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
import java.util.*;
import java.io.*;

/** A class to dump part of a zzspace in a cvsable way.
 * XXX sort for id when dumping! (elsewise, cvs won't be usable)
 * XXX make spans work correctly
 * XXX make work for dumping
 */

public class Dump {
public static final String rcsid = "$Id: Dump.java,v 1.3 2000/12/24 13:54:20 bfallenstein Exp $";
    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    static final int fmtversion = 2; // Version of Dump format

    /** Retrieve and check id for cell.
     * IDs must either be numbers (given as Strings XXX should use Integer?)
     * or correct Java identifiers.
     */
    private static String id(ZZCell c, Hashtable ids) {
	String s = (String)ids.get(c);
	if(s.length() == 0) throw new ZZError("Empty Dump cell id");
	// Check if it's a number; if not, the exception is thrown
	try {
	    Integer.parseInt(s);
	} catch(NumberFormatException e) {
	    // Not a number. Now, test if it's a valid Java identifier.
	    if(!Character.isJavaIdentifierStart(s.charAt(0)))
		throw new ZZError("Bad Dump cell id: '"+s+"'");
	    for(int i=1; i<s.length(); i++)
		if(!Character.isJavaIdentifierPart(s.charAt(i)))
		    throw new ZZError("Bad Dump cell id: '"+s+"'");
	}
	return s;
    }

    /** Write the dump of an array of cells to a Writer.
     * Dumps the cells' contents and all connections <em>between them</em> on
     * the dimensions given, exclusively.
     * @param ids A hashtable mapping cells to ids. This must contain an entry
     *		  for every cell in cells[], and there may not be any entry
     *		  for a cell not in cells[].
     */
    public static void writeDump(ZZCell[] cells, String[] dims,
				 Hashtable ids, int nextid, String title,
				 Writer writer) {
	PrintWriter w = new PrintWriter(writer);
	w.println("HEADER"); w.println();
	w.println("Format: Dump "+fmtversion);
	w.println("Title: "+title);
	w.println("rcsid: $Id: Dump.java,v 1.3 2000/12/24 13:54:20 bfallenstein Exp $");
	if(nextid >= 0) w.println("Next free ID: "+nextid);
	
	w.println(); w.println();
	w.println("CONTENTS"); w.println();
	for(int i=0; i<cells.length; i++) {
	    ZZCell c = cells[i];
	    w.println(id(c, ids) + " " + c.getText());
	    if(c.getSpan() != null)
		ZZLogger.log("ATTENTION! Span converted to plain text in Dump "
			     + "at cell "+c);
	}
	
	w.println(); w.println();
	w.println("CONNECTIONS");
	for(int i=0; i<dims.length; i++) {
	    w.println();
	    String d = dims[i];
	    w.println(d);
	    for(int j=0; j<cells.length; j++) {
		ZZCell neg = cells[j];
		ZZCell pos = neg.s(d);
		if(pos != null && ids.get(pos) != null)
		    w.println(id(neg, ids) + " " + id(pos, ids));
	    }
	}
	
	w.println(); w.println();
	w.println("END OF FILE");
	w.flush();
    }

    /** Find all cells connected to start on dims, directly or indirectly.
     */
    public static ZZCell[] select(ZZCell start, String[] dims) {
	Vector cells = new Vector(); cells.addElement(start);
	Vector todo = new Vector(); todo.addElement(start);
	Hashtable found = new Hashtable(); found.put(start, start);
	
	while(todo.size() > 0) {
	    ZZCell c1 = (ZZCell)todo.elementAt(0);
	    todo.removeElementAt(0);
	
	    for(int i=0; i<dims.length; i++) {
		String dim = dims[i];
		for(int dir=-1; dir<2; dir+=2) {
		    ZZCell c2 = c1.s(dim, dir);
		    if(c2 != null && found.get(c2) == null) {
			todo.addElement(c2);
			cells.addElement(c2);
			found.put(c2, c2);
		    }
		}
	    }
	}
	
	ZZCell[] arr = new ZZCell[cells.size()];
	for(int i=0; i<arr.length; i++)
	    arr[i] = (ZZCell)cells.elementAt(i);
	return arr;
    }

    public static void writeDump(ZZCell start, String[] dims, Writer writer) {
	ZZCell[] cs = select(start, dims);
	Hashtable ids = new Hashtable();
	for(int i=0; i<cs.length; i++) ids.put(cs[i], cs[i].getID());
	writeDump(cs, dims, ids, -1, "Dumped", writer);
    }

    public static class Read {
	public ZZCell[] cells;
	public String[] ids;
	public String[] dims;
	public int nextid = -1;
	public String title;

	public Hashtable cellByID = new Hashtable();
	
	private final void fail() {
	    throw new ZZError("Bad Dump.Read input");
	}
	private final void test(String s1, String s2) {
	    if(!s1.equals(s2)) fail();
	}
	private final void test(BufferedReader r, String s) throws IOException {
	    if(!s.equals(r.readLine())) fail();
	}
	private final void skip(BufferedReader r) throws IOException {
	    if(!r.readLine().equals("")) fail();
	}
	private final String[] split(BufferedReader r, String sp) 
							throws IOException {
	    String s = r.readLine();
	    if(s.equals("")) return null;
	    int i = s.indexOf(sp);
	    return new String[] { s.substring(0, i), 
				  s.substring(i+sp.length()) };
	}
	
	public Read(ZZSpace sp, Reader reader) {
	  try {
			
	    ZZCell home = sp.getHomeCell();
	    BufferedReader r = new BufferedReader(reader);
	    String s;
	    String[] parts; String var, val;
	    p("Reading Dump...");
	    
	    test(r, "HEADER"); skip(r);
	    while((parts=split(r, ": ")) != null) {
		var = parts[0]; val = parts[1];
		if(var.equals("Format")) test(val, "Dump "+fmtversion);
		else if(var.equals("Next free ID"))
		    nextid = Integer.parseInt(val);
		else if(var.equals("Title"))
		    title = val;
		else if(!var.equals("rcsid"))
		    ZZLogger.log("Strange line in Dump header: "+var+": "+val);
	    }
	    p(" Header.");

	    skip(r);
	    test(r, "CONTENTS"); skip(r);
	    Vector v = new Vector(); // cell ids found
	    while((parts=split(r, " ")) != null) {
		var = parts[0]; val = parts[1]; p(" Ctnt: "+var+", "+val);
		v.addElement(var);
		ZZCell c = home.N();
		c.setText(val);
		cellByID.put(var, c);
	    }
	    
	    cells = new ZZCell[v.size()];
	    ids = new String[cells.length];
	    for(int i=0; i<cells.length; i++) {
		ids[i] = (String)v.elementAt(i);
		cells[i] = (ZZCell)cellByID.get(ids[i]);
	    }
	
	    skip(r);
	    test(r, "CONNECTIONS"); skip(r);
	    v = new Vector(); // dimensions found
	    while(!(s = r.readLine()).equals("")) {
		v.addElement(s);
		p(" Dim: "+s);
		while((parts=split(r, " ")) != null) {
		    var = parts[0]; val = parts[1]; p(" Conn: "+var+", "+val);
		    ZZCell c1 = (ZZCell)cellByID.get(var);
		    ZZCell c2 = (ZZCell)cellByID.get(val);
		    if(c1 == null || c2 == null)
			throw new ZZError("Dump input: attempt to connect a "
					+ "cell whose content isn't specified "
					+ "("+(c1==null ? var : val)+")");
		    c1.connect(s, 1, c2);
		}
	    }
	
	    dims = new String[v.size()];
	    for(int i=0; i<dims.length; i++)
		dims[i] = (String)v.elementAt(i);
	    
	    test(r, "END OF FILE");
	    p("...reading finished.");
	
	  } catch(IOException e) { ZZLogger.exc(e); }
	}
    }
}
