/*   
PointerRecord.java
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

/** A record of a pointer change, as specified in the 
 *  <code>Documentation/Mediaserver</code> spec.
 */

public class PointerRecord {

    public PointerRecord(String id, Mediaserver.Id to, Set obsoletes,
			 Mediaserver.Id myId) {
	this.id = id;
        if (to == null) throw new NullPointerException();
	this.to = to;
	this.obsoletes = new HashSet(obsoletes);
	this.myId = myId;
    }

    /** The mediaserver id of the block containing this pointer record.
     *  May be <code>null</code> if this is a new, unsaved pointer.
     */
    final Mediaserver.Id myId;

    public Mediaserver.Id getMSId() { return myId; }

    /** The identifier of the pointer.
     */
    final String id;

    /** The mediaserver id this pointer record points to.
     */
    final Mediaserver.Id to;

    /** The set of the <code>Mediaserver.Id</code> objects of the pointer
     *  records obsoleted by this one. 
     */
    final Set obsoletes;

    public Mediaserver.Id save(Mediaserver ms) throws IOException {
	if(myId != null)
	    throw new Error("Tried to save pointer record that has a "+
			    "mediaserver id already (i.e., is already saved");
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	write(os);
	return ms.addDatum(os.toByteArray(),
			   "application/x-gzigzag-ptr",
			   to
		);
    }

    public void write(OutputStream o) throws IOException {
	// be a bit too trusting about the data :(
	StringBuffer b = new StringBuffer();
	b.append("GZZPTR0\012");
	b.append(id); b.append("\012");
	b.append(to.getString()); b.append("\012");
	for(Iterator i=obsoletes.iterator(); i.hasNext();) {
	    Mediaserver.Id oid = (Mediaserver.Id)i.next();
	    if(oid == null) {
		System.out.println("Null oid!");
		continue;
	    }
	    String s = oid.getString();
	    b.append(s); 
	    b.append("\012");
	}
	o.write(b.toString().getBytes("ISO8859_1"));
	o.flush();
	o.close();
    }

    static public PointerRecord read(
	    Mediaserver.Id myId, InputStream i) throws IOException {
	BufferedReader br = new BufferedReader(
	    new InputStreamReader(i, "ISO8859_1"));
	String s = br.readLine();
	if(!s.equals("GZZPTR0"))
	    throw new IOException("Invalid format");
	String id = br.readLine();
	Mediaserver.Id to = new Mediaserver.Id(br.readLine());
	String ob;
	Set obsoletes = new HashSet();
	while((ob = br.readLine()) != null)
	    obsoletes.add(new Mediaserver.Id(ob));
	return new PointerRecord(id, to, obsoletes, myId);
    }


}


