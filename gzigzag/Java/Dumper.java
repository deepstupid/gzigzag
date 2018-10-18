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

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/** A module interface to Dump.
 * While Dump provides methods to dump any subspace, this module has a special
 * zz infrastructure to save information about subspaces, as follows:
 * <ul>
 * <li> A subspace (SP) has a handle cell (HC).
 * <li> The HC is connected to a cell of the subspace on +d.dump-conn
 * <li> The HC contains the SP file name.
 * <li> Connected to the HC on +d.2 is a rank with dim names; only these dims
 *      are saved to the SP file.
 * <li> After loading, every cell in the SP has a connection on +d.dump-id. The
 *      id found in that cell is a Java identifier for the cell.
 * <li> Connected to the HC on +d.dump-nextid is the next free numeric id for
 *      SP cells (if not present, assumed to be 1). When dumping, this is used
 *      to create ids for cells without +d.dump-id connections; the number is
 *      prepended with a 'c' in that case to make it a valid Java identifier.
 * </ul>
 * Dump IDs may be changed to something more meaningful. When a java class
 * is created from the dump by dump2java.pl, they are instance variables.
 */

public class Dumper {
public static final String rcsid = "$Id: Dumper.java,v 1.2 2000/12/22 22:44:34 bfallenstein Exp $";
    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    static public void create(ZZCell from) {
	ZZCell hdl = from.N("d.1");
	hdl.setText("MyDump");
	hdl.N("d.dump-nextid").setText("2");
	hdl.N("d.2").setText("d.3");
	hdl.N("d.2").setText("d.2");
	hdl.N("d.2").setText("d.1");
	ZZCell c1 = hdl.N("d.dump-conn");
	c1.N("d.dump-id").setText("1");
    }

    static public void write(ZZCell hdl, Writer writer) {
	String title = hdl.t();
	ZZCell[] dcs = hdl.readRank("d.2", 1, false);
	String[] dims = new String[dcs.length];
	for(int i=0; i<dims.length; i++) dims[i] = dcs[i].t();
	
	ZZCell start = hdl.s("d.dump-conn");
	if(start == null) throw new ZZError("Not a Dumper handle: "+hdl);
	ZZCell[] cells = Dump.select(start, dims);
	
	ZZCell nextidcell = hdl.s("d.dump-nextid");
	int nextid;
	try { 
	    nextid = Integer.parseInt(nextidcell.t());
	} catch(NumberFormatException e) {
	    throw new ZZError("Bad next Dumper cell id: "+nextidcell);
	} catch(NullPointerException e) {
	    throw new ZZError("Not a Dumper handle: "+hdl);
	}
	
	Hashtable ids = new Hashtable();
	for(int i=0; i<cells.length; i++) {
	    ZZCell c = cells[i], idc = c.s("d.dump-id");
	    if(idc != null) ids.put(c, idc.t());
	    else {
		String id = String.valueOf(nextid);
		nextid++;
		c.N("d.dump-id").setText(id);
		ids.put(c, id);
	    }
	}
	
	Dump.writeDump(cells, dims, ids, nextid, title, writer);
	
	nextidcell.setText(String.valueOf(nextid));
    }

    static public void read(ZZCell hdl, Reader reader) {
	if(hdl.s("d.2") != null) hdl.disconnect("d.2", 1);
	if(hdl.s("d.dump-conn") != null) hdl.disconnect("d.dump-conn", 1);
	if(hdl.s("d.dump-nextid") != null) hdl.disconnect("d.dump-nextid", 1);
	
	Dump.Read r = new Dump.Read(hdl.getSpace(), reader);
	
	hdl.setText(r.title);
	
	if(r.nextid > 0) hdl.N("d.dump-nextid").setText(""+r.nextid);
	else hdl.N("d.dump-nextid").setText("1");
	
	for(int i=0; i<r.cells.length; i++)
	    r.cells[i].N("d.dump-id").setText(r.ids[i]);
	
	for(int i=r.dims.length-1; i>=0; i--)
	    hdl.N("d.2").setText(r.dims[i]);
	
	if(r.cells.length > 0)
	    hdl.connect("d.dump-conn", 1, r.cells[0]); // save start cell!
	else
	    ZZLogger.log("Strange: Dump without cells...");
    }

    static public ZZModule module = new ZZModule() {
	public void action(String id,
		ZZCell code, 
		ZZCell target,
		ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {
	    if(id.equals("Dump")) 
		write(target, new OutputStreamWriter(System.out));
	    else if(id.equals("Write")) {
		try {
		    write(target, new FileWriter(target.t()+".zzd"));
		    ZZLogger.log("Successfully written dump: "+target.t());
		} catch(IOException e) { ZZLogger.exc(e); }
	    } else if(id.equals("Read")) {
		try {
		    read(target, new FileReader(target.t()+".zzd"));
		} catch(FileNotFoundException e) { ZZLogger.exc(e); }
	    } else if(id.equals("Create")) create(target);
	    else if(id.equals("Enter")) {
		ZZCell vc = view.getViewcell();
		ZZCell to = ZZCursorReal.get(vc).s("d.dump-conn");
		if(to != null) ZZCursorReal.set(vc, to);
	    } else if(id.equals("ChangeID")) {
		ZZCell vc = view.getViewcell();
		ZZCell acc = ZZCursorReal.get(vc);
		ZZCell to = acc.s("d.dump-id");
		if(to == null) to = acc.h("d.dump-id", true);
		if(to == null) to = acc.N("d.dump-id");
		ZZCursorReal.set(vc, to);
	    }
	}

	public ZOb newZOb(String id) {
	    return null;
	}
    };
}