/*   
DB0.java
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
 * Written by Tuomas Lukka
 */

package org.gzigzag.module;

import org.gzigzag.*;
import java.sql.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.awt.*;

public class DB0 {
public static final String rcsid = "$Id: DB0.java,v 1.9 2000/10/18 14:35:32 tjl Exp $";

    public static boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }
    

    static public class StrComp implements ZZUtil.Comparator {
	public int compare(ZZCell c1, ZZCell c2) {
	    return c1.getText().compareTo(c2.getText());
	}
    }

    static public class DHeadComp implements ZZUtil.Comparator {
	DHeadComp(String dim) { this.dim = dim; }
	String dim;
	public int compare(ZZCell c1, ZZCell c2) {
	    return c1.h(dim, -1).getText().compareTo(
			    c2.h(dim, -1).getText());
	}
    }

    static public void insertCells(ZZCell head, String x, String y, 
			    ResultSet rs,
			    String maincol,
			    Hashtable id2col
			    ) throws SQLException {
	ResultSetMetaData meta = rs.getMetaData();
	int n = meta.getColumnCount();
	ZZCell[] hdr = new ZZCell[n];
	ZZCell[] cur = new ZZCell[n];
	ZZCell nextnew = head;
	int mcid = -1;
	int idid = -1;
	for(int i=0; i<n; i++) {
	    nextnew = cur[i] = hdr[i] = nextnew.N(x, 1);
	    String name = meta.getColumnName(i+1);
	    hdr[i].setText(name);
	    if(maincol != null && name.equals(maincol))
		mcid = i;
	    if(name.equals("id"))
		idid = i;
	}
	if(maincol != null && mcid < 0) throw new Error("Invalid main col id");
	ZZCell curhead = head;
	while(rs.next()) {
	    String id = null;
	    ZZCell mcol = null;
	    ZZCell prev = null;
	    for(int i=0; i<n; i++) {
		String s = rs.getString(i+1);
		if(s==null) s="(null)";
		cur[i] = cur[i].N(y, 1);
		cur[i].setText(s);
		if(prev != null)
		    prev.connect(x, 1, cur[i]);
		prev = cur[i];
		if(i == mcid) {
		    mcol = cur[i];
		    /* curhead.connect("d.2", cur[i]);
		    curhead = cur[i]; */
		}
		if(i == idid)
		    id = s; 
	    }
	    if(id != null && mcol != null)
		id2col.put(id, mcol);
	}
	for(int i=0; i<n; i++) {
	    ZZUtil.sortRank(hdr[i], y, 1, new StrComp(), false);
	}
	// ZZUtil.sortRank(head,"d.2", 1, new StrComp(), false);
	rs.close();

    }

    static public ZZModule module = new ZZModule() {
	public void action(String id, ZZCell code, 
			   ZZCell target, 
			   ZZView v, ZZView cv,
			   String key, Point pt, 
			   ZZScene xi) {
	try {
	    if(id.equals("READMEMS")) {
	    synchronized(target.getSpace()) {

		ZZSpace zz = target.getSpace();
		
		String x = "d.1";
		String y = "d.2";

		Hashtable id2mem = new Hashtable();
		Hashtable id2sig = new Hashtable();


		String url = "jdbc:postgresql://himalia.mit.jyu.fi/zz";
		String usr = "tjl";
		String pwd = "";
		Class.forName("org.postgresql.Driver");

		Connection db = DriverManager.getConnection(url, usr, pwd);
		Statement st = db.createStatement();

		ZZCell n = target.N("d.3", 1);
		n.setText("members");
		
		ResultSet rs = st.executeQuery("select * from members");
		insertCells(n, x, y, rs, "name", id2mem);

		n = n.N("d.3", 1); n.setText("sigs");
		rs = st.executeQuery("select * from sigs");
		insertCells(n, x, y, rs, "name", id2sig);

		n = n.N("d.3", 1); n.setText("positions");
		rs = st.executeQuery("select * from positions");
		insertCells(n, x, y, rs, null, null);

		// ZZCell title = target.N();
		// title.setText("<Linux>");
		// Create people rank in alphabetical order
		rs = st.executeQuery("select * from members order by name");
		ZZCell prev = null;
		while(rs.next()) {
		    ZZCell curr = (ZZCell)id2mem.get(rs.getString(1));
		    
	    	    if(prev != null) prev.connect("d.people", 1, curr);
		    prev = curr;
		}
		
		// Create sig rank in alphabetical order
		rs = st.executeQuery("select * from sigs order by name");
		prev = null;
		while(rs.next()) {
		    ZZCell curr = (ZZCell)id2sig.get(rs.getString(1));
		    
		    if(prev != null) prev.connect("d.insigs", 1, curr);
		    prev = curr;
		}


		

		p("Rels: ");

		// Insert relcells after the positions table
		rs = st.executeQuery("select * from positions");
		while(rs.next()) {
		    String pers = rs.getString(1);
		    String sig = rs.getString(2);
//		    p("Rel: "+pers+" "+sig);
		    ZZCell pc = (ZZCell)id2mem.get(pers);
		    ZZCell sc = (ZZCell)id2sig.get(sig);
		    if(pc == null || sc == null)
			throw new Error("No such relation "+pers+" "+sig);
		    ZZCell relc = pc.N("d.insigs",1);
		    sc.insert("d.people", 1, relc);
		    relc.setText("+");
		}
		rs.close();

		// Sort the ranks of the in-which-sigs tables.

		for(Enumeration e = id2mem.elements();
		    e.hasMoreElements(); ) {
		    ZZCell c = (ZZCell)e.nextElement();
		    ZZUtil.sortRank(c, "d.insigs", 1, 
			new DHeadComp("d.people"), false);
		}

		for(Enumeration e = id2sig.elements();
		    e.hasMoreElements(); ) {
		    ZZCell c = (ZZCell)e.nextElement();
		    ZZUtil.sortRank(c, "d.people", 1, 
			new DHeadComp("d.insigs"), false);
		}

		st.close();
		db.close();
		
	    }
	    } /* else if(id.equals("IMPORT")) {
	    // XXX How to give 2 arguments: the file name and the home cell
		(new XML()).load(new File(target.getText()), 
	    }*/
	} catch(SQLException e) {
	    System.out.println(e);
	    e.printStackTrace();
	} catch(ClassNotFoundException e) {
	    System.out.println(e);
	    e.printStackTrace();
	}
	}
    };
}
