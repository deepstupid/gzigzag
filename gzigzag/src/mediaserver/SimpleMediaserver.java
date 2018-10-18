/*   
SimpleMediaserver.java
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

import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import org.gzigzag.util.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.net.InetAddress;

/** A mediaserver capable of retrieving and adding data.
 *  This is a combination of a Storer and an IDSpace. Unlike a Storer, a
 *  Mediaserver does not allow storing data under arbitrary IDs; rather,
 *  when adding a new datum, a new ID is assigned to it.
 */
public class SimpleMediaserver implements Mediaserver {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.out.println(s); }

    private Storer store;
    private IDSpace idspace;
    private Properties properties;

    private static DateFormat dateFormat =
        new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private Map cache = new HashMap();

    public SimpleMediaserver(Storer store, IDSpace idspace, long flags) {
	this.store = store;
	this.idspace = idspace;

    }
    
    public Set getIDs() throws IOException {
	Set keys = store.getKeys();
	Set ids = new HashSet();
	for(Iterator i = keys.iterator(); i.hasNext(); ) {
	    String s = (String)i.next();
	    if(!s.substring(0, 2).equals("b_")) continue;
	    ids.add(new Id(HexUtil.hexToByteArr(s.substring(2))));
	}
	return ids;
    }

    public Block getDatum(Id id) throws IOException {
	Block b = (Block)cache.get(id);
	if(b != null) return b;
        final String key = "b_" + id.getString();

        File f = store.getFile(key);
        if (f == null) {
            InputStream is = store.retrieve(key);
            if(is == null)
                throw new IOException("Datum not found: "+id);
            b = new LazyBlock(idspace, id, is, this.getPoolName());
        } else {
            b = new LazyBlock(idspace, id, f, this.getPoolName());
        }
        return b;

        /*
	// for now, do it the inefficient way: read all the input and then 
	// parse it from the byte array in the end.


	ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        p("getDatum: start copying");
        while(true) {
            p("read ");
            int r = is.read(buf);
            p("check("+r+") ");
            if(r == -1) break;
            p("write ");
            os.write(buf, 0, r);
        }
        p("... all read.");
        is.close();
	
	b = makeBlock(id, os.toByteArray());
	return b;
        */
    }

    private Block makeBlock(Id id, byte[] raw) throws IOException {
	byte[] b = raw;
	byte[] header = null;
	for(int i=0; i<b.length-3; i++) {
	    if(b[i] == 0xd && b[i+2] == 0xd) {
		header = new byte[i+2];
		System.arraycopy(b, 0, header, 0, i+2);
		byte[] nb = new byte[b.length - (i+4)];
		p("copy "+i+" "+b.length+" "+nb.length);
		System.arraycopy(b, i+4, nb, 0, nb.length);
		b = nb;
		break;
	    }
	}
	if(header == null)
	    throw new IOException("No header found in "+HexUtil.toString(b,0));

        String hdr = "\n";
        for (int i = 0; i < header.length; i++) {
            if (header[i] == 0x0d && i + 1 < header.length && header[i+1] == 0x0a) i++;
            hdr += (char)header[i];
        }
        p("[YYY\n" + hdr + "\nYYY]");

        final String ct = "\nContent-Type: ";
        int inx = hdr.indexOf(ct);
        if (inx == -1) throw new IOException("No Content-Type");
        // XXX assumes here that CT is not folded
        int inx2 = hdr.indexOf("\n", inx + ct.length());
        if (inx2 == -1) inx2 = hdr.length();
        p("EEE: " + (inx + ct.length()) + " " + inx2);
        String contentType = hdr.substring(inx + ct.length(), inx2);

	// Check the checksum
	if(!idspace.checkData(id, raw))
	    throw new InvalidID("Invalid raw data for block!");
        
	return new Block.Trivial(id, contentType, b, raw, hdr, this.getPoolName());
    }

    void writeHeader(OutputStream os, String contentType) throws IOException {
	String CRLF=""+((char)0xd)+((char)0xa);
	String s = "";
	String name = System.getProperty("user.name");
	// Also get the local host name...
	try {
	    InetAddress addr = InetAddress.getLocalHost();
	    name = name + "@" + addr.getHostName();
	} catch(Exception e) {
	}
	

	// XXX Potential problem: mangled user.name with e.g. newlines
	if(name != null)
	    s = s + "X-Injected-By: "+name+CRLF;

        s = s + "Date: " + dateFormat.format(new Date()) + " +0000" + CRLF;

	s = s + "Content-Type: "+contentType+CRLF+
		"Content-Transfer-Encoding: binary"+CRLF+CRLF;
	os.write(s.getBytes("ISO8859_1"));
    }

    public Id addDatum(byte[] data, String contentType, Id assocId) 
	throws IOException {
	// Don't care about associated id as there's only one pool
	return addDatum(data, contentType);
    }

    public Id addDatum(byte[] data, String contentType) throws IOException {
	Id id;
	// First, include header into block!
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	writeHeader(os, contentType);
	os.write(data);
	os.close();
	byte[] ndata = os.toByteArray();
	id = idspace.createID(ndata);
	OutputStream sos = store.store("b_" + id.getString());
	sos.write(ndata);
	sos.close();
	blockAdded(id);
	return id;
    }

    public Id addDatum(byte[] data, String[] headers, Id assocId) throws IOException {
	String CRLF=""+((char)0xd)+((char)0xa);
	Id id;
	// First, include header into block!
	String s = "";
	for(int i=0; i < headers.length; i++)
	    s += headers[i] + CRLF;
	s += CRLF;
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	os.write(s.getBytes("ISO8859_1"));
	os.write(data);
	os.close();
	byte[] ndata = os.toByteArray();
	id = idspace.createID(ndata);
	OutputStream sos = store.store("b_" + id.getString());
	sos.write(ndata);
	sos.close();
	blockAdded(id);
	return id;
    }

    public void storeDatum(Id id, byte[] data) throws IOException {
	Block block = makeBlock(id, data);
	OutputStream os = store.store("b_" + id.getString());
	os.write(data);
	os.close();
	blockAdded(id);
    }

    /** Get the pointer set for a specific pointer.
     *  XXX performance
     *  @param s The ID of the pointer whose pointer set we want to retrieve.
     */
    public PointerSet getPointerSet(String s) throws IOException {
	InputStream is = store.retrieve("ps_" + s);
	if(is != null)
	    return PointerSet.read(s, is);
	
	Map sets = PointerSet.getPointerSets(this);
	PointerSet set = (PointerSet)sets.get(s);
	if(set == null) set = new PointerSet(s);
	OutputStream os = store.store("ps_" + s);
	set.write(os);
	return set;
    }

    public void cache(Block b) {
	cache.put(b.getId(), b);
    }

    public void uncache(Block b) {
	cache.remove(b);
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
	if(dbg) {
	    pa("Setting pointer "+s+" to "+id);
            Set obs = getObsoletes(s);
	    for(Iterator i=obs.iterator(); i.hasNext(); ) {
		pa("Currently active record: "+i.next());
	    }
	    for(Iterator i=obsoletes.iterator(); i.hasNext(); ) {
                Object o = i.next();
		pa("Obsoleting pointer record: "+o);
                if (o == null) Thread.currentThread().dumpStack();
	    }
	}

	PointerRecord rec = new PointerRecord(s, id, obsoletes, null);
	rec.save(this);
    }

    public Id getPointer(String s) throws IOException {
	PointerSet pset = getPointerSet(s);
	if(pset != null) return pset.getSingleActive();
	else return null;
    }

    public Set getPointers() throws IOException {
	Map map = PointerSet.getPointerSets(this);
	return map.keySet();
    }

    /** Called when a new block is added; checks if it's a pointer
     *  changing a pointer set.
     *  XXX performance
     */
    protected void blockAdded(Mediaserver.Id id) throws IOException {
	Block b = getDatum(id);
	if(!b.getContentType().equals("application/x-gzigzag-ptr")) 
	    return;

	PointerRecord pr = PointerRecord.read(id,
	    new ByteArrayInputStream(b.getBytes()));
	PointerSet set = getPointerSet(pr.id);
	set.add(pr);
	set.write(store.store("ps_" + pr.id));
    }


    public String getPoolName() throws IOException {
        String rv = store.getProperty("simplemediaserver.poolname");
        if (rv == null) return "";
        return rv;
    }

    public void setPoolName(String name) throws IOException {
        store.setProperty("simplemediaserver.poolname", name);
    }
}
