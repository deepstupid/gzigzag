/*   
GZZ1Cache.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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
import org.gzigzag.mediaserver.Mediaserver;
import java.util.*;
import java.io.*;

/** A cache for GZZ1 information, able to read in more than one diff and
 *  flush them to another <code>GZZ1Handler</code>.
 *  This can be used to compile multiple diffs into a single version.
 */

public class GZZ1Cache implements GZZ1Handler.IDSettable {
String rcsid = "$Id: GZZ1Cache.java,v 1.9 2002/03/14 00:46:12 bfallenstein Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) System.out.println(s); }
    private static void pa(String s) { System.out.println(s); }

    /** The Id we're currently reading.
     *  Needed in order to interpret local-only cell IDs.
     */
    String currentBlock;

    /** The Mediaserver Id we're currently reading.
     *  Needed for the legacy content transclusions.
     */
    Mediaserver.Id currentId;

    CellIds cid = new DefaultCellIds();

    public void setId(Mediaserver.Id id) {
	this.currentBlock = id.getString();
	this.currentId = id;
	cid.setBlockId(id);
    }

    final CellIds.Id getGID(byte[] id) {
	return cid.get(new String(id));
    }

    /** The dimension maps storing the connections. */
    Map cdims = new HashMap();

    /** The dimension maps storing the disconnects not preceded by a connect.
     *  This is needed for <code>d.vstream</code> stuff.
     */
    Map ddims = new HashMap();

    List spanTransclusions = new ArrayList();

    List newCells = new ArrayList();

    List transcopies = new ArrayList();


    int connects, vstreamConnects, disconnects, vstreamDisconnects;



    Map getCDim(byte[] id) {
	return getCDim(getGID(id));
    }
    Map getDDim(byte[] id) {
	return getDDim(getGID(id));
    }
	
    Map getCDim(CellIds.Id gid) {
	Map m = (Map)cdims.get(gid);
	if(m == null) {
	    m = new HashMap();
	    cdims.put(gid, m);
	}
	return m;
    }
    Map getDDim(CellIds.Id gid) {
	Map m = (Map)ddims.get(gid);
	if(m == null) {
	    m = new HashMap();
	    ddims.put(gid, m);
	}
	return m;
    }

    Map cvstream = getCDim(cid.get(Id.stripHome(Dims.d_vstream_id.id)));
    Map dvstream = getDDim(cid.get(Id.stripHome(Dims.d_vstream_id.id)));

    public void flush(GZZ1Handler h) throws IOException {
	flush(h, true);
    }

    /** Flush the cached data to a <code>GZZ1Handler</code>.
     *  @param startstop Whether to call h.start() and h.close().
     */
    public void flush(GZZ1Handler h, boolean startstop) throws IOException {
	if(dbg) {
	    pa("Checking for consistency.");
	    HashSet cells = new HashSet();

	    //System.out.print("Building set of cells.");

	    cells.addAll(cdims.keySet()); cells.addAll(ddims.keySet());

	    for(Iterator i=cdims.values().iterator(); i.hasNext();) {
		//System.out.print(".");
		Map m = (Map)i.next();
		cells.addAll(m.keySet());
	    }
	    for(Iterator i=ddims.values().iterator(); i.hasNext();) {
		//System.out.print(".");
		Map m = (Map)i.next();
		cells.addAll(m.keySet());
	    }

	    pa("");
	    pa("Number of cells: "+cells.size());
	    System.out.print("Checking.");

	    Map ids = new HashMap();

	    for(Iterator i=cells.iterator(); i.hasNext();) {
		CellIds.Id c = (CellIds.Id)i.next();
		if(!c.equals(c))
		    pa("DOES NOT EQUAL ITSELF: "+c);

		String s = c.getString();
		CellIds.Id d = (CellIds.Id)ids.put(s, c);

		if(d != null) {
		    System.out.print("*");
		    if(!c.equals(d)) {
			pa("NOT EQUAL:");
			pa("    "+c);
			pa("    "+d);
		    }
		    if(!d.equals(c)) {
                        pa("NOT EQUAL:");
			pa("    "+c);
			pa("    "+d);
		    }
		    if(c.hashCode() != d.hashCode()) {
			pa("DIFFERENT HASH: ");
			pa("    "+c);
			pa("    "+d);
		    }
		}
	    }

	    pa("");
	    pa("Consistency checked.");

	}
	
	if(startstop)
	    h.start(null);
		
	GZZ1Handler.NewCells cs = h.newCellsSection();
	for(Iterator i=newCells.iterator(); i.hasNext(); ) {
	    String str = ((CellIds.Id)i.next()).getString();
	    cs.newCell(str.getBytes());
	}
	cs.close();

	for(Iterator i = transcopies.iterator(); i.hasNext();) {
	    TranscopyRecord tr = (TranscopyRecord)i.next();
	    GZZ1Handler.Transcopy sec = h.transcopySection(
		tr.tid.getBytes(), tr.spaceId);
	    sec.transcopy(tr.cid.getBytes());
	    sec.close();
	}
	
	GZZ1Handler.SpanTransclusion sts = h.spanTransclusionSection();
	for(Iterator i = spanTransclusions.iterator(); i.hasNext(); ) {
	    Transclusion tr = (Transclusion)i.next();
	    sts.transclude(tr.id.getString().getBytes(), 
			   tr.blockId, tr.first, tr.last);
	}
	sts.close();
	
	Set dimIds = new HashSet();
	dimIds.addAll(cdims.keySet());
	dimIds.addAll(ddims.keySet());
	
	for(Iterator i = dimIds.iterator(); i.hasNext(); ) {
	    CellIds.Id id = (CellIds.Id)i.next();
	    GZZ1Handler.SimpleDim ds = h.dimSection(id.getString().getBytes());

	    for(Iterator j = getDDim(id).entrySet().iterator(); j.hasNext();) {
		Map.Entry e = (Map.Entry)j.next();
		CellIds.Id 
		    c = (CellIds.Id)e.getKey(), 
		    d = (CellIds.Id)e.getValue();
		ds.disconnect(c.getString().getBytes(), 
			      d.getString().getBytes());
	    }

	    for(Iterator j = getCDim(id).entrySet().iterator(); j.hasNext();) {
		Map.Entry e = (Map.Entry)j.next();
		CellIds.Id 
		    c = (CellIds.Id)e.getKey(), 
		    d = (CellIds.Id)e.getValue();
		ds.connect(c.getString().getBytes(), 
			   d.getString().getBytes());
	    }
	
	    ds.close();
	}
	
	if(startstop)
	    h.close();
    }


    class SimpleDim implements GZZ1Handler.SimpleDim {
	Map cdim, ddim;
	
	SimpleDim(CellIds.Id id) { 
	    this.cdim = getCDim(id);
	    this.ddim = getDDim(id);
	}

	public void disconnect(byte[] id1, byte[] id2) {
	    Object old = cdim.remove(getGID(id1));
	    if(old == null) {
		ddim.put(getGID(id1), getGID(id2));
		disconnects++;
		if(ddim == dvstream)
		    vstreamDisconnects++;
	    } else if(!old.equals(getGID(id2)))
		throw new Error("Inconsistent GZZ1Cache data. Disconnect wrong cell-- "+getGID(id1)+" from "+getGID(id2)+" instead of "+old);
	}

	public void connect(byte[] id1, byte[] id2) {
	    Object a = getGID(id1), b = getGID(id2);

	    if(cdim.get(a) != null)
		throw new Error("Inconsistent GZZ1Cache data (connect)");

	    if(!b.equals(ddim.get(a))) {
		cdim.put(getGID(id1), getGID(id2));
		connects++;
		if(cdim == cvstream)
		    vstreamConnects++;
	    } else {
		ddim.remove(a);
	    }
	}

	public void close() {
	}
    }
	
    /** Cells for which we have set a legacy content vstream.
     *  One problem with the legacy content conversion is that we are creating
     *  connections to vstreams where the old handler just changed the
     *  content-- so if the user also created a connection on the vstream
     *  dimension from that cell, we get <code>ZZAlreadyConnected</code>.
     *  <p>
     *  To fix this, when we connect on d.vstream, and we have previously
     *  made a connection to a legacy content vstream there, we have to
     *  disconnect it; but if there is a connection which we didn't make,
     *  we do not disconnect.
     *  <p>
     *  Thus we need to store to which cells we have made vstream connections,
     *  which is exactly what this set does. (Note: we are storing
     *  <code>String</code> objects.)
     */
    Set cellsWithLegacyContent = new HashSet();

    class LegacyContent implements GZZ1Handler.LegacyContent {
	public void transcludeLegacyContent(byte[] id, int first, int last)
					                   throws IOException {
	    CellIds.Id sid = getGID(id);
            int length = last - first + 1;
	    if(length != 0) {
		spanTransclusions.add(new Transclusion(sid, currentId,
						       first, last));

		String vstream =
		    (sid.getString() + ";" + currentBlock + "$" + first);
		cvstream.put(sid, cid.get(vstream));
	    }
	}

	public void close() {
	}
    }

    public void start(Mediaserver.Id previous) {
    }

    public GZZ1Handler.SimpleDim dimSection(byte[] cellId) {
	return new SimpleDim(getGID(cellId));
    }

    LegacyContent sc = new LegacyContent();

    public GZZ1Handler.LegacyContent legacyContentSection() {
	return sc;
    }

    public NewCells newCellsSection() {
	return new NewCells() {
		public void newCell(byte[] cellId) {
		    newCells.add(getGID(cellId));
		}
		public void close() {}
	    };
    }

    class TranscopyRecord {
	TranscopyRecord(CellIds.Id tid, CellIds.Id cid, Mediaserver.Id spaceId) {
	    this.tid = tid.getString(); 
	    this.cid = cid.getString(); 
	    this.spaceId = spaceId;
	}
	String tid, cid;
	Mediaserver.Id spaceId;
    }

    public Transcopy transcopySection(byte[] transcopyId,
				      final Mediaserver.Id spaceId) {
	p("Transcopy section");
	final CellIds.Id tid = getGID(transcopyId);
        return new Transcopy() {
                public void transcopy(byte[] cellId) {
		    CellIds.Id cid = getGID(cellId);
		    transcopies.add(new TranscopyRecord(tid, cid, spaceId));
                }
                public void close() {}
            };
    }

    class Transclusion {
	Transclusion(CellIds.Id id, Mediaserver.Id blockId, 
		     int first, int last) {
	    this.id = id; this.blockId = blockId; 
	    this.first = first; this.last = last;
	}
	CellIds.Id id;
	Mediaserver.Id blockId;
	int first, last;
    }

    public SpanTransclusion spanTransclusionSection() {
	return new SpanTransclusion() {
		public void transclude(byte[] transclusionId,
				       Mediaserver.Id blockId,
				       int first, int last) throws IOException{
		    spanTransclusions.add(new Transclusion(
			getGID(transclusionId), blockId, first, last));
		}
		public void close() {}
	    };
    }

    public void close() {
	p("Some statistics:");
	p(" # of new cells: "+newCells.size());
	p(" # of span transclusions: "+spanTransclusions.size());
	p(" # of dims: "+cdims.size());
	p(" # of transcopies: "+transcopies.size());
	p(" # of connects/vstream connects: "+
	  connects + ", " + vstreamConnects);
	p(" # of disconnects/vstream disconnects: "+
	  disconnects + ", " + vstreamDisconnects);

	/**
	p("The dims are:");
	for(Iterator i=cdims.keySet().iterator(); i.hasNext();) {
	    p(" - " + i.next());
	}
	**/
    }

}

