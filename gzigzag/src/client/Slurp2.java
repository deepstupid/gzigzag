/*   
Slurp2.java
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
import org.gzigzag.mediaserver.*;
import org.gzigzag.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import org.gzigzag.util.*;
import java.util.*;
import java.io.*;

/** A stand-alone application to create an equivalence block for a space.
 *  Slurps in the old space and writes it back as a new MS block not dependent
 *  on the first one. This version uses <code>GZZ1Cache</code>.
 */

public class Slurp2 {
public static final String rcsid = "$Id: Slurp2.java,v 1.7 2002/03/27 07:04:55 bfallenstein Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }
    protected static void out(String s) { System.out.println(s); }

    public static void main(String argv[]) throws IOException {
	Storer stor;
	if (argv.length != 2) {
	    pa("Slurp2 takes two arguments:");
	    pa("The mediaserver directory and the pointer id.");
	    return;
	}

	out("Starting Slurp2");

	stor = new DirStorer(new File(argv[0]));
	Mediaserver ms = new SimpleMediaserver(stor, new IDSpace(), 0);

	String ptr = argv[1];

        Mediaserver.Id spaceId = ms.getPointer(ptr);
        if(spaceId == null)
            throw new Error("Didn't find pointer "+ptr);

	slurp(ms, spaceId, ptr, true);

	out("I'm done!");
    }

    public static Mediaserver.Id slurp(Mediaserver ms, Mediaserver.Id spaceId, String ptr, boolean slurpInclusions) throws IOException {
	out("Getting pointer");

	if(slurpInclusions) {
	    out("load space");

	    PermanentSpace sp = new PermanentSpace(ms, spaceId);

	    for(Iterator i=sp.getEditableSpaces().iterator(); i.hasNext(); ) {
		out("Processing inclusion...");

		PermanentSpace incl = (PermanentSpace)i.next();
                String incl_id = sp.getInclusionIdBySpace(incl);
		Cell c = sp.getCell(incl_id);
		Cell ptr_c = c.s(Dims.d_spacespec_id, 4);

		String incl_ptr = null;
		if(ptr_c != null)
		    incl_ptr = Id.stripHome(Id.get(c).id);
		    
		Mediaserver.Id set_to = slurp(ms, incl.prevId,
					      incl_ptr, true);

		Cell block = Id.getBlock(set_to.getString(), sp);
		c = c.s(Dims.d_spacespec_id, 2);
		Cursor.set(c, block);

		out("Inclusion processed.");
	    }

	    out("writing space to mediaserver");

	    Mediaserver.Id tmp = sp.save(ms);
	    if(tmp != null) spaceId = tmp;
	}

	out("reading in");

	GZZ1Cache cache = new GZZ1Cache();
	GZZ1SpaceHandler.dbg = true; // for getting progress messages. XXX
	GZZ1SpaceHandler.read(ms, spaceId, cache);

	out("flushing");

	StringWriter w = new StringWriter();
	cache.flush(new GZZ1Writer(w));

	out("writing to mediaserver");

        Mediaserver.Id to = ms.addDatum(w.getBuffer().toString().getBytes(), 
					"application/x-gzigzag-GZZ1");

	if(ptr != null) {
	    out("setting pointer");
	
	    ms.setPointer(ptr, to);
	}
	
	out("The equivalence block has been created. Its mediaserver ID is:");
	out(to.toString());

	return to;
    }
}
