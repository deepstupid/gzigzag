/*   
CreateIdentities.java
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
package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import org.gzigzag.util.*;
import java.util.*;
import java.io.*;

public class CreateIdentities {
public static final String rcsid = "$Id: CreateIdentities.java,v 1.7 2002/03/10 01:16:23 bfallenstein Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }
    protected static void out(String s) { System.out.println(s); }

    public static void main(String argv[])  {
	try {
	Storer stor;
	if (argv.length < 1) {
	    pa("No filename given: aborting");
	    return;
	} else {
	    if(!new File(argv[0]).exists()) {
		pa("Mediaserver dir does not exist: aborting");
		return;
	    }
	    stor = new DirStorer(new File(argv[0]));
	}
	Mediaserver ms = new SimpleMediaserver(stor,new IDSpace(), 0);
	BufferedReader in = 
	    new BufferedReader(new InputStreamReader(System.in));
	List cells = new LinkedList();
	PermanentSpace space;
	if(argv.length < 2)
	    space = new PermanentSpace(ms);
	else
	    space = new PermanentSpace(ms, new Mediaserver.Id(argv[1]));
	
	out("CreateConstants. Copyleft (GNU) 2001 by Benja Fallenstein.");
	out("");
	out("Welcome to the CreateConstants tool. This tool will create a ");
	out("mediaserver block containing a GZZ1 space for you. You will ");
	out("enter a number of names for cells you would like to have; the ");
	out("tool will create these cells in the space and make them ");
	out("contain the names you specified. (Note: you cannot currently ");
	out("influence their IDs, due to the way the current Space impl ");
	out("works.) You will be asked for the names one after another; ");
	out("when you're finished, simply press return without entering a ");
	out("name (however, you have to enter at least one name). Then, the ");
	out("tool will save the space to the mediaserver directory you ");
	out("specified on the command line, and give you the IDs of the ");
	out("space and the identities of the cells.");
	out("");

	boolean first;
	String name;
	do {
	    first = cells.size() == 0;
	    System.out.print("Please enter the name of a cell to be created");
	    if(!first)
		System.out.print("\n  (just hit return when you're finished)");
	    System.out.print(": ");
	    name = in.readLine();
	    if(!name.equals("")) {
		Cell c = space.N();
		c.setText(name);
		cells.add(c);
	    }
	} while(first || !name.equals(""));

	String id = space.save(ms).getString();
	
	out("The space was created successfully. Its mediaserver ID is: ");
	out(id);
	out("The local IDs of the cells (inside that block) are:");

	for(Iterator i = cells.iterator(); i.hasNext(); ) {
	    Cell c = (Cell)i.next();
	    out("    "+c.t()+": "+c.id);
	}

	out("You can view the MS block now by reading it from the ");
	out("mediaserver directory you specified, "+argv[0]+".");

	}catch(IOException e) {
	    e.printStackTrace();
	}
    }
}
