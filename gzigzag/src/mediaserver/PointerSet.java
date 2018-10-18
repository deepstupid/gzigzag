/*   
PointerSet.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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

package org.gzigzag.mediaserver;
import java.util.*;
import java.io.*;

/** A set of pointer changes with a particular id.
 *  @see PointerRecord
 */

public class PointerSet {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.out.println(s); }

    public String id;

    /** <code>Mediaserver.Id</code>s of obsoletes.
     */
    Set obsolete = new HashSet();

    /** <code>Mediaserver.Id</code> of the pointer record to 
     *  <code>Mediaserver.Id</code> of the target that record sets this
     *  pointer to.
     */
    Map all = new HashMap();

    PointerSet(String id) {
	this.id = id;
    }

    void add(PointerRecord r) {
	if(!r.id.equals(id)) throw new Error("Wrong pointer to add");
	if(r.getMSId() == null) 
	    throw new Error("Can only make a set of non-null msid ptrs");
	all.put(r.getMSId(), r.to);
	obsolete.addAll(r.obsoletes);
    }

    // XXX is this correct?
    public void union(PointerSet other) {
        if (!id.equals(other.id)) throw new Error("bad union");
	/* At least this was not right
        // Obsolete only those that are obsoleted in both of them
	for (Iterator it = obsolete.iterator(); it.hasNext();) {
	Object o = it.next();
	if (!other.obsolete.contains(o)) it.remove();
        }*/
	obsolete.addAll(other.obsolete);

        all.putAll(other.all);
    }

    public Map getActiveMap() {
        Map active = new HashMap(all);
        for(Iterator i=obsolete.iterator(); i.hasNext();) {
            Mediaserver.Id obs = (Mediaserver.Id)i.next();
            active.remove(obs);
        }
	return active;
    }

    /** Get the active <code>Mediaserver.Id</code>s of the targets this
     *  pointer points to.
     *  <p>
     *  These are the targets of this pointer that have not been obsoleted;
     *  in other words, "where this pointer <em>currently</em> points to."
     *  <p>
     *  Currently, we expect there to be only one of these except during
     *  repository synchronization; this single one can be get through
     *  <code>getSingleActive()</code>, which throws an exception if there
     *  is more than one active target.
     */
    public Set getActive() {
        return new HashSet(getActiveMap().values());
    }

    public Mediaserver.Id getSingleActive() {
	Set active = getActive();
	if(active.size() > 1) {
	    String targets = "";
	    for(Iterator i = active.iterator(); i.hasNext(); )
		targets += "    * " + i.next() + "\n";
	    throw new Error("There was more than one active target for "+
			    "pointer "+id+". The targets were:\n"+targets);
	} else if(active.size() == 0)
	    //throw new Error("No destination for pointer: "+id);
	    return null;
	Iterator i = active.iterator();
	return (Mediaserver.Id)i.next();
    }

    /** Get a map from String to PointerSet.
     * XXX Currently, this LOADS every block.
     */
    static public Map getPointerSets(Mediaserver ms) throws IOException {
	Set ids = ms.getIDs();
	Map res = new HashMap();
	for(Iterator i = ids.iterator(); i.hasNext();) {
	    Mediaserver.Id id = (Mediaserver.Id)i.next();
	    Mediaserver.Block b = ms.getDatum(id);
	    if(!b.getContentType().equals("application/x-gzigzag-ptr"))
		continue;
	    PointerRecord pr = PointerRecord.read(id, 
		new ByteArrayInputStream(b.getBytes()));
	    PointerSet ps = (PointerSet)res.get(pr.id);
	    if(ps == null)
		res.put(pr.id, ps = new PointerSet(pr.id));
	    ps.add(pr);
	}
	return res;
    }


    /** Write this PointerSet to an OutputStream.
     *  Useful for caching pointer destinations on the disk.
     */
    public void write(OutputStream o) throws IOException {
        StringBuffer b = new StringBuffer();
        b.append("GZZ-pointerset-cache\012");
	b.append("\012");

	for(Iterator i=getActiveMap().entrySet().iterator();
	    i.hasNext(); ) {
	    Map.Entry entry = (Map.Entry)i.next();
	    Mediaserver.Id 
		ptr = (Mediaserver.Id)entry.getKey(),
		block = (Mediaserver.Id)entry.getValue();
	    b.append(ptr.getString() + " " + block.getString() + "\012");
	}

	b.append("\012");
        
        for(Iterator i=obsolete.iterator(); i.hasNext();) {
            Mediaserver.Id 
		ptr = (Mediaserver.Id)i.next(),
		block = (Mediaserver.Id)all.get(ptr);
	    if(block == null) {
		p("Warning: obsoleted block not in PointerSet (this is normal during sync, but may be a problem otherwise)");
		b.append(ptr.getString() + "\012");
	    } else {
		b.append(ptr.getString() + " " + block.getString() + "\012");
	    }
	}

        o.write(b.toString().getBytes("ISO8859_1"));
        o.flush();
        o.close();	
    }

    static public PointerSet read(String id, 
                           InputStream i) throws IOException {
        BufferedReader br = new BufferedReader(
	    new InputStreamReader(i, "ISO8859_1"));
        String s = br.readLine();
	if(s == null)
	    throw new IOException("Empty file");
        else if(!s.equals("GZZ-pointerset-cache"))
            throw new IOException("Invalid format");
	s = br.readLine();
	if(!s.equals(""))
            throw new IOException("No blank line after PointerSet format string");

	PointerSet set = new PointerSet(id);

	boolean obs = false;
	while((s = br.readLine()) != null) {
	    if(s.equals("")) {
		if(obs)
		    throw new Error("More than one blank line in PointerSet");
		obs = true;
		continue;
	    }
	    
	    int sp = s.indexOf(" ");

	    if(sp+1 >= s.length()) {
                throw new Error("Bad line in PointerSet (space at end): '"+s+"'");
	    }

	    Mediaserver.Id ptr, block;
	    try {
		if(sp >= 0) {
		    ptr = new Mediaserver.Id(s.substring(0, sp));
		    block = new Mediaserver.Id(s.substring(sp+1));
		} else {
		    ptr = new Mediaserver.Id(s);
		    block = null;
		}
	    } catch(RuntimeException e) {
		pa("Error in line: '"+s+"'");
		pa("Length of line: "+s.length());
		throw e;
	    }
	    
	    if(block != null)
		set.all.put(ptr, block);
	    else
		p("Info: PointerSet being read in contains obsoleted pointer whose target isn't known.");
	    if(obs) set.obsolete.add(ptr);
	}

	i.close();
        return set;
    }
}

