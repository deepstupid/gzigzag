/*   
AddPointer.java
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
 * Written by Benja Fallenstein and Antti-Juhani Kaijanaho
 */
package org.gzigzag.mediaserver;
import org.gzigzag.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import org.gzigzag.util.*;
import java.util.*;
import java.io.*;

/** A stand-alone application to add a pointer to the mediaserver.
 */

public class AddPointer {
public static final String rcsid = "$Id: AddPointer.java,v 1.1 2001/08/05 18:13:07 tjl Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }
    protected static void out(String s) { System.out.println(s); }

    public static void main(String argv[])  {
	try {
	Storer stor;
	if (argv.length < 3) {
	    pa("AddToMediaserver takes at least three arguments:");
	    pa("The mediaserver directory, the pointer id ");
	    pa("and the block to set the pointer to.");
	    pa("Additionally, obsoleted pointer blocks can be given.");
	    return;
	}
	stor = new DirStorer(new File(argv[0]));
	Mediaserver ms = new SimpleMediaserver(stor,new IDSpace(), 0);

	String id = argv[1];
	Mediaserver.Id to = new Mediaserver.Id(argv[2]);
	
	HashSet obsoletes = new HashSet();
	for(int i=3; i<argv.length; i++) 
	    obsoletes.add(new Mediaserver.Id(argv[i]));
		
	PointerRecord rec = new PointerRecord(id, to, obsoletes, null);
	String msid = rec.save(ms).getString();
	
	out("The mediaserver block for the pointer was created successfully.  "+
	    "Its mediaserver ID is: ");
	out(msid);

	}catch(IOException e) {
	    e.printStackTrace();
	}
    }
}
