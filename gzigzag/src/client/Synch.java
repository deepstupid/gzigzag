/*   
Synch.java
 *
 *    Copyleft (GNU) 2001 by Benja Fallenstein   
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

/** Synch two Mediserver repositories up completely.
 *  If there are conflicting pointers, break into merge.
 */

public class Synch {
public static final String rcsid = "$Id: Synch.java,v 1.5 2002/03/13 22:18:22 bfallenstein Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }
    protected static void out(String s) { System.out.println(s); }

    /** A buffered reader reading from stdin.
     * Used for asking the user for merge options.
     */
    private static BufferedReader input =
        new BufferedReader(new InputStreamReader(System.in));

    public static void sync(Mediaserver ms1, Mediaserver ms2, boolean readonly)
	    throws IOException {

        if (!ms1.getPoolName().equals(ms2.getPoolName()))
            throw new IOException("These mediaservers are in different pools: "
	     + "pool of first mediaserver is '"+ms1.getPoolName()+"', "
	     + "pool of second mediaserver is '"+ms2.getPoolName());

	// First, copy all blocks from both sides to both sides.

	Set id1 = ms1.getIDs(), id2 = ms2.getIDs();
	Set ids;

	ids = new HashSet(id2);
	ids.removeAll(id1);
	out("Copy blocks from second to first mediaserver:"); out("");
	copy(ms2, ids, ms1);
	out(""); out("");

	if(!readonly) {
	    ids = new HashSet(id1);
	    ids.removeAll(id2);
	    out("Copy blocks from first to second mediaserver:"); out("");
	    copy(ms1, ids, ms2);
	    out(""); out("");
	}

	out("Looking for conflicting pointers...");
	Map sets = PointerSet.getPointerSets(ms1);
	for(Iterator i = sets.values().iterator(); i.hasNext(); ) {
	    PointerSet ps = (PointerSet)i.next();
	    Map activeMap = ps.getActiveMap();
	    Set activeRecs = activeMap.keySet();
	    int n = activeRecs.size();
	    if(n <= 1)
		continue;
	    else if(n > 2) {
		out("OOOPS, we gonna have a problem here. :( Something went");
		out("wrong, and now there are *more than two* current");
		out("versions of the pointer '"+ps.id+"'. Here are the ");
		out("targets they point to:");
		for(Iterator j=activeRecs.iterator(); j.hasNext();) {
		    out("    "+activeMap.get(j.next()));
		}
		out("I have to give up on this pointer, because I can only");
		out("merge up to two targets. Please contact");
		out("zzdev@xanadu.net so that we can find a solution.");
		out("");
	    } else {
		out("OK, your and the repository version of the pointer");
		out(ps.id+" are in conflict; I will merge them. However,");
		out("first you have to tell me which of the two you want");
		out("to be merged into the other one. I will give you ");
		out("the headers of each block, and then you can tell me");
		out("which one you want the other one to be merged into.");
		
		Mediaserver.Id[] activeRecIds =
		    (Mediaserver.Id[])activeRecs.toArray(new Mediaserver.Id[2]);
		Mediaserver.Id[] activeIds =
		    new Mediaserver.Id[] { 
			(Mediaserver.Id)activeMap.get(activeRecIds[0]),
                        (Mediaserver.Id)activeMap.get(activeRecIds[1]) };

		out("Headers of [1]:");
		out(ms1.getDatum(activeIds[0]).getHeader());
		out("ID of space: "+activeIds[0]);
		out("ID of pointer: "+activeRecIds[0]);
		out("");
		out("Headers of [2]:");
		out(ms1.getDatum(activeIds[1]).getHeader());
                out("ID of space: "+activeIds[1]);
		out("ID of pointer: "+activeRecIds[1]);
		out("");

		String line;
		do {
		    out("So which one should be the \"official\" version merged into--");
		    out("number one or number two? Please enter '1' or '2',");
		    out("and press Enter.");
		    out("If you don't want to merge now and want to leave");
		    out("the conflicting versions in the pool, enter '0'.");
		    line = input.readLine();
		} while(!line.equals("1") && !line.equals("2") &&
			    !line.equals("0"));
		
		/** mergeWhat is merged into mergeInto. */
		Mediaserver.Id mergeWhat, mergeInto;

		if(line.equals("0")) {
		    continue;
		} else if(line.equals("1")) {
		    mergeWhat = activeIds[1];
		    mergeInto = activeIds[0];
		} else {
		    mergeWhat = activeIds[0];
		    mergeInto = activeIds[1];
		}

		Mediaserver.Id ancestor = 
		    findCommonAncestor(ms1, mergeWhat, mergeInto);

		Merge1.Result res = Merge1.merge(ms1, mergeInto, mergeWhat, ancestor);
                Set conflicts = res.conflicts;
                PermanentSpace merged = res.newSpace;
                Mediaserver.Id mergedId = merged.save(ms1);

                out("");
                out("Setting the pointer");
		Set obsoleted = activeMap.keySet();
		PointerRecord rec = new PointerRecord(ps.id, mergedId,
						      obsoleted, null);
		Mediaserver.Id pointerId = rec.save(ms1);

                if(!readonly) {
		    out("Copying the blocks");
		    Set toCopy = new HashSet();
		    toCopy.add(mergedId);
		    toCopy.add(pointerId);
		    copy(ms1, toCopy, ms2);
		} else {
		    out("Remember that the merged blocks will not be");
		    out("copied to the repository, as you are accessing");
		    out("the repository in read-only mode.");
		}

                out("Merge returned the following conflicts.");
                out("You really should do something about them.");
                out(Merge1.chgString(conflicts, merged));
	    }
	}

	out("Finished.");
    }

    public static void copy(Mediaserver from, Set ids, Mediaserver to) 
            throws IOException {
	int n=0, count=ids.size();
	for(Iterator i = ids.iterator(); i.hasNext(); ) {
	    n++;
	    Mediaserver.Id id = (Mediaserver.Id)i.next();
	    out("Copy block: "+id+" ("+n+"/"+count+")");
	    to.storeDatum(id, from.getDatum(id).getRaw());
	}
    }

    public static void copyPointers(Mediaserver from, Set ps, Mediaserver to)
	    throws IOException {
	for(Iterator i = ps.iterator(); i.hasNext(); ) {
	    String p = (String)i.next();
	    out("Copy pointer: "+p);
	    to.setPointer(p, from.getPointer(p));
	}
    }

    /** Find out which one of two spaces is an earlier version of the other.
     *  Used in sync, to know how to set a pointer. If the spaces are on
     *  different branches, we need to merge.
     *  <p>
     *  XXX maybe move somewhere else?
     *  @returns -1 if the first space is newer; +1 if the second space is
     *           newer; and 0 if the two spaces are in different branches.
     *           Result is undefined if the two histories are equal (XXX).
     *  @param h1 The first space history, as given by readSpaceHistory
     */
    public static int whichSpaceIsNewer(Mediaserver.Id[] h1,
					Mediaserver.Id[] h2) {
	int l = h1.length < h2.length ? h1.length : h2.length;
	
	for(int i=0; i<l; i++)
	    if(!h1[i].equals(h2[i])) return 0;

	if(h1.length > h2.length) return -1;
	else return 1;
    }

    /** Find out the common ancestor of two spaces.
     */
    public static Mediaserver.Id findCommonAncestor(Mediaserver ms,
			    Mediaserver.Id s1, Mediaserver.Id s2) 
				    throws IOException{
	
	Mediaserver.Id[]
	    h1 = readSpaceHistory(ms, s1),
	    h2 = readSpaceHistory(ms, s2);

	int len = h1.length > h2.length ? h2.length : h1.length, 
	    k;
	for(k=0; k<len; k++)
	    if(!h1[k].equals(h2[k])) break;
	return h1[k-1];

    }
    
    /** Return a list of all mediaserver IDs a space has had since creation.
     *  The list starts with the first version after the null (home) version,
     *  and ends with the current id (the one passed to this function).
     *  <p>
     *  XXX move somewhere else-- this is of general value!
     */
    public static Mediaserver.Id[] readSpaceHistory(Mediaserver ms, 
				      Mediaserver.Id id) throws IOException {
	final List list = new LinkedList();
	final Mediaserver.Id[] cur = new Mediaserver.Id[] { id };

	p("initialize: "+id);

	GZZ1Handler hdl = new GZZ1NullHandler() {
		public void start(Mediaserver.Id previous) {
		    p("handler.start called: " + previous);
		    cur[0] = previous;
		}
	    };

	while(cur[0] != null) {
	    list.add(0, cur[0]);
	    Mediaserver.Id last = cur[0];

	    byte[] data = ms.getDatum(cur[0]).getBytes();
	    InputStream is = new ByteArrayInputStream(data);
	    Reader r = new InputStreamReader(is);
	    GZZ1Reader.read(r, hdl);	 

	    if(last.equals(cur[0])) throw new ZZError("ARGH!");
	}

	return (Mediaserver.Id[])list.toArray(cur);
    }

    protected static Mediaserver getms(String type, String arg) throws IOException {
	Storer stor;
	if(type.equals("-dir")) {
            if(!new File(arg).exists()) {
                throw new IOException("Mediaserver dir does not exist");
            }
            stor = new DirStorer(new File(arg));
	} else if(type.equals("-zip")) {
            if(!new File(arg).exists()) {
                throw new IOException("Mediaserver .zip does not exist");
            }
            stor = new ZipStorer(new java.util.zip.ZipFile(arg));
	} else if(type.equals("-url")) {
	    stor = new URLStorer(arg);
	} else {
	    throw new IOException("Unrecognized mediaserver type: " + type);
	}
	return new SimpleMediaserver(stor, new IDSpace(), 0);
    }

    public static Mediaserver[] getMediaservers(String argv[], int i) throws IOException {
	List l = new ArrayList();
	while(i<argv.length) {
	    String type, arg;

	    if(argv[i].startsWith("-")) {
		type = argv[i];
		arg = argv[i+1];
		i = i + 2;
	    } else {
		type = "-dir";
		arg = argv[i];
		i++;
	    }

	    l.add(getms(type, arg));
	}
	return (Mediaserver[])l.toArray(new Mediaserver[l.size()]);
    }
	

    public static void main(String argv[])  throws IOException {
	boolean readonly = false;
	int x = 0;

	if(argv[0].equals("-ro")) {
	    readonly = true;
	    x = 1;
	}
	
	Mediaserver[] ms = getMediaservers(argv, x);

	if(ms.length != 2) {
	    pa("Synch needs to be given exactly two mediaservers.");
	}

	sync(ms[0], ms[1], readonly);
    }
}
