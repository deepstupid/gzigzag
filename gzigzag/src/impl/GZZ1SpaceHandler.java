/*   
GZZ1SpaceHandler.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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

/** (To be renamed) A handler reading GZZ1 into a space.
 *  An event handler for the GZZ1 format parser.
 * Use the read method.
 */

public class GZZ1SpaceHandler implements GZZ1Handler.IDSettable {
String rcsid = "$Id: GZZ1SpaceHandler.java,v 1.50 2002/03/16 18:35:18 bfallenstein Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) System.out.println(s); }
    private static void pa(String s) { System.out.println(s); }

    /** Read the space given by the mediaserver id into the given space.
     * @param ms An instance of mediaserver where to query for id.
     * @param id The mediaserver id of the space to be loaded.
     * @param sp The space to write the connections etc into.
     */
    static public void read(Mediaserver ms, Mediaserver.Id id, 
			    GZZ1Space sp) 
                       throws IOException {
	read(ms, id, new GZZ1SpaceHandler(sp, ms));
    }
	
    static public void read(Mediaserver ms, Mediaserver.Id id,
			    GZZ1Handler.IDSettable hdl) throws IOException {
	p("Start reading history");

	List history = new ArrayList();
	while(true) {
	    history.add(0, id);
	    Mediaserver.Block b = ms.getDatum(id);
	    String idstr = b.readNthLine(2);
	    if(idstr.equals("")) break;
	    id = new Mediaserver.Id(idstr);
	}

	if(dbg) {
	    pa("Here is a history:");
	    for(Iterator i=history.iterator(); i.hasNext(); )
		pa("   "+i.next());
	}

	for(Iterator i = history.iterator(); i.hasNext(); ) {
	    id = (Mediaserver.Id)i.next();

	    byte[] data = null;
	    try {
		//data = ms.getDatum(id).getBytes();

		/*
		InputStream is = new ByteArrayInputStream(data);
		Reader r = new InputStreamReader(is);
		*/
		
		hdl.setId(id);
		p("Calling reader. Id: "+id);

		//char[] cdata = new char[data.length];
		//for(int j=0; j<cdata.length; j++)
		//    cdata[j] = (char)data[j];
		Reader r = new InputStreamReader(ms.getDatum(id).getInputStream());
		
		GZZ1Reader.read(r, hdl);

		p("Reader finished."+id);
	    } catch(IOException e) {
		pa("EXCEPTION WHILE READING BLOCK:");
		pa(id.toString());
		pa(e.getMessage());
		if(data != null) pa(" len: "+data.length);
		throw e;
            } catch(Error e) {
                pa("ERROR WHILE READING BLOCK:");
		pa(id.toString());
                pa(e.getMessage());
		if(data != null) pa(" len: "+data.length);
                throw e;
            } catch(RuntimeException e) {
                pa("EXCEPTION WHILE READING BLOCK:");
		pa(id.toString());
                pa(e.getMessage());
		if(data != null) pa(" len: "+data.length);
                throw e;
	    }
	}
	p("Read that history.");
    }


    // Internals.

    /** The space into which we're reading.
     */
    GZZ1Space space;

    /** The Mediaserver to use.
     *  We need a Mediaserver to read the previous deltas from-- this is
     *  the one we use.
     */
    Mediaserver mediaserver;

    /** The Id we're currently reading.
     *  Needed in order to interpret local-only cell IDs.
     */
    String currentBlock;

    /** The Mediaserver Id we're currently reading.
     *  Needed for the legacy content transclusions.
     */
    Mediaserver.Id currentId;

    protected GZZ1SpaceHandler(GZZ1Space space, Mediaserver mediaserver) { 
	this.space = space; 
	this.mediaserver = mediaserver;
    }

    public void setId(Mediaserver.Id id) {
	this.currentBlock = id.getString();
	this.currentId = id;
	this.localMap = new HashMap();
    }

    final String getGID(byte[] id) {
	String str = new String(id);
	if(str.charAt(0) == '-')
	    return currentBlock + str;
	else
	    return str;
    }


    HashMap localMap;
    HashMap globalMap = new HashMap();
    final Cell getCell(byte[] id) {
	String str = new String(id);
	HashMap map;
	if(str.charAt(0) == '-') {
	    map = localMap;
	} else {
	    map = globalMap;
	}
	Cell c = (Cell) map.get(str); // XXX ???
	if(c == null)
	    map.put(str, c = space.getCell(getGID(id)));
	return c;
    }

    // This is not called as often as getCell and is allowed
    // to be slower.
    final Dim getDim(byte[] id) {
	return space.getDim(Id.space.getCell("home-id:"+getGID(id)));
    }

    /** Handles the changes to a simple dimension.
     */
    class SimpleDim implements GZZ1Handler.SimpleDim {
	Dim dim;
	boolean isVStreamDim;
	String ds; // string to use when debugging
	SimpleDim(Dim dim, String ds) { 
	    this.dim = dim; 
	    this.ds = ds; 
	    this.isVStreamDim = ds.equals(Id.stripHome(Dims.d_vstream_id.id));
	}

	public void disconnect(byte[] id1, byte[] id2) {
	    dim.disconnect(getCell(id1), +1);
	    // p("Disconnected: "+new String(id1)+"-"+ds+"-"+new String(id2));
	}

	public void connect(byte[] id1, byte[] id2) {
	    Cell c1 = getCell(id1), c2 = getCell(id2);
	    try {
		if(isVStreamDim && cellsWithLegacyContent.contains(c1))
		    dim.disconnect(c1, 1);
		dim.connect(c1, c2);
		// p("Connected: "+new String(id1)+"-"+ds+"-"+new String(id2));
	    } catch(ZZAlreadyConnectedException e) {
		// XXX throw exception:
		// this is something we want to recover from (error msg)
		pa("Attempted: "+new String(id1)+"-"+ds+"-"+new String(id2));

		if(c2.equals(dim.s(c1, 1))) {
		    pa("Warning: Ignoring error, correct cells were already connected: \n"+c1+"\n"+c2);
		    return;
		}
		throw new Error("Bad file: was already connected: "+dim+" \n"+
		    getCell(id1)+" (\n"+getCell(id1).s(dim)+" \n"+getCell(id2).s(dim, -1)+") \n"+getCell(id2));
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
     *  <code>Cell</code> objects.)
     */
    Set cellsWithLegacyContent = new HashSet();

    class LegacyContent implements GZZ1Handler.LegacyContent {
	public void transcludeLegacyContent(byte[] id, int first, int last)
					                   throws IOException {
	    Cell c = getCell(id);
            int length = last - first + 1;
	    if(length != 0) {
		space.gzz1_transcludeSpan(c, currentId, first, last);

		Cell vstream =
		    space.getCell(c.id + ";" + currentBlock + "$" + first);
		if(cellsWithLegacyContent.contains(c))
		    c.disconnect(Dims.d_vstream_id, 1);
		else
		    cellsWithLegacyContent.add(c);
		
		c.connect(Dims.d_vstream_id, vstream);

		/** Debug...
		try{
		    c.connect(Dims.d_vstream_id, vstream);
		}catch(ZZAlreadyConnectedException e){
		    throw new ZZError("was "+first+" "+last+" "+length+" "+
				      vstream+": "+e);
		}
		**/
	    }
	}

	public void close() {
	}
    }

    public void start(Mediaserver.Id previous) {
    }

    public GZZ1Handler.SimpleDim dimSection(byte[] cellId) {
	// XXX cache? (re-used between deltas)
	return new SimpleDim(getDim(cellId),
			     new String(cellId));
    }

    LegacyContent sc = new LegacyContent();

    public GZZ1Handler.LegacyContent legacyContentSection() {
	return sc;
    }

    public NewCells newCellsSection() {
	return new NewCells() {
		public void newCell(byte[] cellId) {
		    space.gzz1_NewCell(getGID(cellId));
		}
		public void close() {}
	    };
    }

    public Transcopy transcopySection(byte[] transcopyId,
                            org.gzigzag.mediaserver.Mediaserver.Id spaceId) {
	p("Transcopy section");
	final Cell tid = getCell(transcopyId);
	final Space from;
	try {
	    from = Loader.load(mediaserver, spaceId);
	} catch(IOException e) {
	    throw new Error("IOException while reading: "+e);
	}
        return new Transcopy() {
                public void transcopy(byte[] cellId) {
                    space.gzz1_transcopy(tid, from.getCell(getGID(cellId)));
                }
                public void close() {}
            };
    }

    public SpanTransclusion spanTransclusionSection() {
	return new SpanTransclusion() {
		public void transclude(byte[] transclusionId,
				       Mediaserver.Id blockId,
				       int first, int last) throws IOException{
		    Cell tid = getCell(transclusionId);
		    space.gzz1_transcludeSpan(tid, blockId, first, last);
		}
		public void close() {}
	    };
    }

    public void close() {}

}

