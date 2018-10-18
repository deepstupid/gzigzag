/*   
MultiplexingMediaserver.java
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

package org.gzigzag.mediaserver;

import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import org.gzigzag.util.*;
import java.io.*;
import java.util.*;

/** A mediaserver channeling all requests through to other mediaservers.
 *  One of the servers is a fallback, others are either read-only or
 *  read-write. Fallback is used to write all data that isn't written 
 *  elsewhere, eg. new slices and pointers to nonexistent blocks. Otherwise
 *  data is written to all the servers that contain an associated block.
 */
public class MultiplexingMediaserver implements Mediaserver {

    Mediaserver fallback, readable[], writable[]; 

    String pool;

    /** Get the name of the pool this mediaserver stores. */
    public String getPoolName() throws IOException { return pool; }

    /** Set the name of the pool this mediaserver stores. */
    public void setPoolName(String s) throws IOException { 
        throw new IOException("not implemented"); 
    }

    /** @param fb fallback storage pool, read-write, fallback for writes
	@param ro read-only storage pools
	@param rw read-write storage pools
    */
    public MultiplexingMediaserver(Mediaserver fb, 
				   Mediaserver[] ro, 
				   Mediaserver[] rw) {
	if(ro == null)
	    ro = new Mediaserver[] {};
	if(rw == null)
	    rw = new Mediaserver[] {};

	fallback = fb;

	readable = new Mediaserver[1+rw.length+ro.length];
	writable = new Mediaserver[1+rw.length];
	readable[0] = writable[0] = fb;

	System.arraycopy(rw, 0, readable, 1, rw.length);
	System.arraycopy(rw, 0, writable, 1, rw.length);

	System.arraycopy(ro, 0, readable, 1+rw.length, ro.length);

        SortedSet set = new TreeSet();
        for (int i = 0; i < readable.length; i++) {
	    try {
            set.add(readable[i].getPoolName());
	    } catch(IOException _) {
		set.add("<no name>");
	    }
        }

        pool = "";
        for (Iterator it = set.iterator(); it.hasNext();) {
            pool += "; " + (String)it.next();
        }
    }

    public MultiplexingMediaserver(Mediaserver fb, Mediaserver ro) {
	this(fb, new Mediaserver[] { ro }, null);
    }

    public Set getIDs() throws IOException {
	Set result = new HashSet();
	for(int i=0; i<readable.length; i++) {
	    result.addAll(readable[i].getIDs());
	}
	return result;
    }

    public Block getDatum(Id id) throws IOException {
	for(int i=0; i<readable.length; i++) {
	    try {
		return readable[i].getDatum(id);
	    } catch(IOException e) {
	    }
	}
	throw new IOException("Datum not found or error while reading: "+id);
    }


    /** Creates a new block (to the fallback pool) */
    public Id addDatum(byte[] data, String contentType) throws IOException {
	return addDatum(data, contentType, null);
    }

    /** Creates a new block associated with some previous block. Association
     *  is used to determine which pools the block is saved to.
     */
    public Id addDatum(byte[] data, String contentType, Id assocId) 
	throws IOException {
	Id newId = null;

	if(assocId != null) {
	    for(int i=0; i<writable.length; i++) {
		try {
		    Block tester = writable[i].getDatum(assocId);
		    // No exception, so the associated block is there
		    newId = writable[i].addDatum(data, contentType);
		} catch (IOException _) {}
	    }
	}

	if(newId == null)
	    return fallback.addDatum(data, contentType);
	return newId;
    }

    public Id addDatum(byte[] data, String[] headers, Id assocId) 
        throws IOException {
        Id newId = null;

	if(assocId != null) {
            for(int i=0; i<writable.length; i++) {
		try {
                    Block tester = writable[i].getDatum(assocId);
                    // No exception, so the associated block is there
                    newId = writable[i].addDatum(data, headers, null);
                } catch (IOException _) {}
	    }
        }

        if(newId == null)
            return fallback.addDatum(data, headers, null);
	return newId;

    }

    public void storeDatum(Id id, byte[] data) throws IOException {
	fallback.storeDatum(id, data);
    }

    public void cache(Block b) {}
    public void uncache(Block b) {}

    public Id getPointer(String s) throws IOException {
	PointerSet pset = getPointerSet(s);
	if(pset != null) return pset.getSingleActive();
	else return null;
    }

    public Set getPointers() throws IOException {
	throw new UnsupportedOperationException("not implemented");
    }

    public PointerSet getPointerSet(String s) throws IOException {
        PointerSet rv = readable[0].getPointerSet(s);
        for (int i = 1; i < readable.length; i++) {
            rv.union(readable[i].getPointerSet(s));
        }
        return rv;
    }

    private Set getObsoletes(String s) throws IOException {
	PointerSet pset = getPointerSet(s);
	Set obsolete;
	if(pset != null)
	    obsolete = pset.getActiveMap().keySet();
	else
	    obsolete = Collections.EMPTY_SET;
        return obsolete;
    }

    public void setPointer(String s, Id id) throws IOException {
        setPointer(s, id, getObsoletes(s));
    }

    public void setPointer(String s, Id id, Id obsolete) throws IOException {
        Set o = new HashSet();
        o.add(obsolete);
        setPointer(s, id, o);
    }

    public void setPointer(String s, Id id, Set obsoletes) throws IOException {
	PointerRecord rec = new PointerRecord(s, id, obsoletes, null);
	rec.save(this);
    }
}
