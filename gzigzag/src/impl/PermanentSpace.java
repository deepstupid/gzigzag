/*
PermanentSpace.java
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
 * Written by Benja Fallenstein
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;
import java.util.*;
import java.io.*;

/** A space able to write itself to the disk.
 */

public class PermanentSpace extends CompoundSpace implements GZZ1Space {
public static final String rcsid = "$Id: PermanentSpace.java,v 1.12 2002/03/27 08:32:55 bfallenstein Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) System.out.println(s); }
    private static void pa(String s) { System.out.println(s); }

    /** The Id of the last saved version of this space.
     *  Needed when saving it again.
     *  XXX public b/c Slurp2 needs it
     */
    public Mediaserver.Id prevId;

    /** Original to transcopied cell ids. */
    protected HashMap origToTranscopied = new HashMap();

    /** Get a cell corresponding to the given cell in another space.
     * Used by Merge when creating new cells.
     */
    public String getCorrespondingCell(Space sfrom, String id) {
	if(exists(id)) return id;
	
	String oid; // original id
	int x = id.lastIndexOf(";");
	int y = id.lastIndexOf("$");
	if(y < 0)
	    oid = id;
	else
	    oid = id.substring(0, x);

	String nid = (String)origToTranscopied.get(oid); // new id
	if(nid == null) {
	    nid = transcopy(sfrom.getCell(oid)).id;
	    origToTranscopied.put(oid, nid);
	}

	if(!exists(nid))
	    throw new Error("BUG: getCorrespondingCell() does not exist");

	if(x < 0)
	    return nid;
	else {
	    return nid + id.substring(x);
	}
    }

    /** The transcopies we've done and need to save.
     *  Maps cells to mediaserver IDs of originating spaces.
     */
    protected Map transcopiedCells = new HashMap();

    public Cell transcopy(Cell c) {
	Cell t = super.transcopy(c);
	for(Cell u = t; u != null; u = u.s((Dim)getVStreamDim())) {
	    if(u.id.lastIndexOf("$") < 0)
		transcopiedCells.put(u, ((GIDSpace)c.space).getLastId());
	    else if(!u.id.startsWith(c.id))
		transcopy(c.space.getCell(u.id.substring(0, u.id.lastIndexOf(";"))));
	}
	return t;
    }

    /** The newly transcluded spans.
     *  Maps transclusion ids (as String objects!)
     *  to Span1D object representing transcluded spans.
     */
    public Map transcludedSpans = new HashMap();

    /** VStream cells we have to disconnect poswards very first thing
     *  when we build the GZZ1Cache for saving. This is because the
     *  connection is made when we transclude the span, but we don't
     *  want it.
     */
    protected Map vStreamDisconnects = new HashMap();

    /** All spans we've ever transcluded.
     *  Maps transclusion ids (as String objects!)
     *  to Span1D object representing transcluded spans.
     *  Maps to a List if there is more than one such span.
     */
    public Map allTranscludedSpans = new HashMap();
    
    public void registerTransclusion(String tid, Span span) {
	Object o = allTranscludedSpans.get(tid);
	if(o == null) {
	    allTranscludedSpans.put(tid, span);
	} else if(o instanceof List) {
	    ((List)o).add(span);
	} else {
	    List l = new ArrayList(2);
	    l.add(o);
	    l.add(span);
	    allTranscludedSpans.put(tid, l);
	}
    }

    /**
    // 2 params: tid, span
    protected UndoList.Op transclude = new UndoList.Op() {
	    public void undo(Object[] list, int nth) {throw new Error("XXX");}
	    public void redo(Object[] list, int nth) {throw new Error("XXX");}
	    public void commit(Object[] list, int nth, UndoList.GIDMaker gid, GZZ1Handler hdl) throws IOException {
		String tid = (String)list[nth+1];
		TextSpan sp = (TextSpan)list[nth+2];
		
		pa("BLOCK: "+sp.getScrollBlock().getID()+", "+new String(gid.make(sp.getScrollBlock().getID())));

		GZZ1Handler.SpanTransclusion h = hdl.spanTransclusionSection();
		h.transclude(gid.make(tid), new Mediaserver.Id(new String(gid.make(sp.getScrollBlock().getID()))),
			     sp.offset(), sp.offset() + sp.length());
		h.close();
	    }
	};
    **/

    public Cell makeSpanRank(TextSpan sp, Cell slice) {
	if(getSpace(slice) != null)
	    return translate(((SimpleSpanSpace)getSpace(slice)).makeSpanRank(sp, detranslate(slice)));

	VStreamDimProxy proxy = (VStreamDimProxy)getVStreamDim();
        UndoableVStreamDim uvs = (UndoableVStreamDim)proxy.base;
	VStreamDim vs = uvs.getBase();
	Cell res = super.makeSpanRank(vs, sp, slice);
	
	String tid = res.id.substring(0, res.id.lastIndexOf(";"));
	
	//undo.add(transclude, tid, sp);
	
	TextSpan old = (TextSpan)transcludedSpans.get(tid);
	if(old == null) {
	    transcludedSpans.put(tid, sp);
	    registerTransclusion(tid, sp);
	} else {
	    Span1D appended = old.append(sp);
	    if(appended == null)
		throw new Error("ARGH! Noncontinuous span produced by SimpleSpanSpace.makeSpanRank!");
	    transcludedSpans.put(tid, appended);
	    registerTransclusion(tid, appended);

	    int i = sp.offset();
	    String middle = ";" + sp.getScrollBlock().getID() + "$";

            Cell a = getCell(tid + middle + (i-1));
	    Cell b = getCell(tid + middle + i);
	    vStreamDisconnects.put(a, b);
	}

	return res;
    }

    public Set getTranscludedSpanCells() {
	Set set = new HashSet();
	for(Iterator i=transcludedSpans.keySet().iterator(); i.hasNext(); ) {
	    String tid = (String)i.next();
	    Span1D span = (Span1D)transcludedSpans.get(tid);
	    String ref = tid + ";" + span.getScrollBlock().getID() + "$";
	    int offs = span.offset(), len = span.length();
	    int first = offs, afterLast = offs + len;
	    for(int j=first; j<afterLast; j++)
		set.add(ref + j);
	}
	return set;
    }

    public List getTranscludedSpans(String tid) {
	Object o = allTranscludedSpans.get(tid);
	if(o == null) return null;
	else if(o instanceof List) return (List)o;
	else return Collections.nCopies(1, o);
   }

    public void gzz1_transcludeSpan(Cell tid, Mediaserver.Id blockId,
				    int first, int last) 
	        throws ScrollBlockManager.CannotLoadScrollBlockException {
	if(first > last)
	    throw new Error("Bad inclusion in file: first > last");
	String block = blockId.getString();
	TextScrollBlock scroll = (TextScrollBlock)scrolls.get(block);
	if(scroll == null) {
	    scroll = ScrollBlockManager.getTextScrollBlock(mediaserver, blockId);
	    scrolls.put(block, scroll);
	}
	registerTransclusion(tid.id, scroll.getSpan(first, last-first+1));

	Cell c = spanSpacepart.getCell(tid.id+";"+block+"$"+first);
	int len = last - first + 1;
	getVStreamDim().notifyTransclusion(c, len);
    }

    /** After loading/creating, store the current status for use when saving.
     *  @param id The prevId to set.
     */
    protected void check(Mediaserver.Id id) {
	prevId = id;
    }

    public Mediaserver.Id getLastId() {
	return prevId;
    }

    public void gzz1_NewCell(String id) {
	if(cells.get(id) == null)
	    cells.put(id, new Cell(this, id));
    }
    public Cell gzz1_transcopy(Cell tid, Cell c) {
        if(c.id.startsWith("-"))
            throw new ZZError("cannot gzz1_transclude cell without gid");
	Cell res = new Cell(this, tid.id + ";" + c.id);
	cells.put(res.id, res);

	Set transcopied = new HashSet(); transcopied.add(res.id);

        Cell vs = Dims.d_vstream_id;
        for(Cell d = c.s(vs); d != null; d = d.s(vs)) {
            String id = tid.id + ";" + d.id;
            if(!transcopied.contains(id)) {
                String block = id.substring(id.lastIndexOf(";") + 1,
                                            id.lastIndexOf("$"));
                if(scrolls.get(block) == null) try {
                    Mediaserver.Id blockId = new Mediaserver.Id(block);
                    scrolls.put(block, ScrollBlockManager.getTextScrollBlock(mediaserver, blockId));
                } catch(ScrollBlockManager.CannotLoadScrollBlockException e) {
                    throw new Error("problem loading scroll block: "+e.getMessage());
                }
                cells.put(id, new Cell(this, id)); // XXX timebomb?
                transcopied.add(id);
            }
        }
	
        return res;
    }

    protected void readInclusions(Mediaserver ms) throws IOException {
	p("Getting x");
	Cell x = getHomeCell().s(Dims.d_spaces_id);
	p("Start iteration");
	while (x != null) {
	    p("Space inclusion iteration");
	    Cell c = x.s(Dims.d_spacespec_id);
	    if (c != null && Id.equals(c, msSpace)) {
		c = c.s(Dims.d_spacespec_id);
		Cell block = Cursor.get(c);
		if(block == null)
		    // Read in deprecated format for space inclusions
		    block = c.getRootclone();
		if (!block.id.startsWith("home-block:"))
		    throw new ZZError("Invalid block in d.spaces list!"+c);
		String id = Id.stripBlock(block.id);
		c = c.s(Dims.d_spacespec_id);
		boolean editable;
		if(c == null)
		    editable = false;
		else
		    editable = Boolean.valueOf(c.t()).booleanValue();
		// XXX Obviously can't handle recursion.
		//  <- Benja: ???
		p("Calling Loader.load");
		includeRaw(x, Loader.load(ms, new Mediaserver.Id(id)), editable);
	    }
	    x = x.s(Dims.d_spaces_id);
	}
	p("End iteration");
    }

    public PermanentSpace(Mediaserver ms) throws IOException {
	this(ms, new Mediaserver.Id("0000000008000000E7C28830B50004CCCB7107A8BBE16B5ECE3D6819D93AE20C77AC95B149B165"));
    }

    /** Load a space from a mediaserver. */
    public PermanentSpace(Mediaserver ms, Mediaserver.Id id) throws IOException {
	mediaserver = ms;
	ghostcells = true;
	undo.setActive(false);

	p("Calling GZZ1SpaceHandler");
	GZZ1SpaceHandler.read(ms, id, this);
	p("Calling check()");
	check(id);
	//for(Iterator i = cells.keySet().iterator(); i.hasNext(); ) {
	//  System.out.println(""+i.next());
	//}
	msSpace = (Cell)cells.get("0000000008000000E7C28830B50004CCCB7107A8BBE16B5ECE3D6819D93AE20C77AC95B149B165-1;0000000008000000E7BD6F28880004C62132A8CCF343828039ECC856521E3F49A054FF08883711-2");
	p("Reading inclusions");
	readInclusions(ms);
	// The following seems to cause errors:
        //msSpace = new Cell(this, "0000000008000000E7C28830B50004CCCB7107A8BBE16B5ECE3D6819D93AE20C77AC95B149B165-2:0000000008000000E7BD6F28880004C62132A8CCF343828039ECC856521E3F49A054FF08883711-2");
	ghostcells = false;
	canonicalizeCells();

	undo.setActive(true);

	p("Returning");
    }



    /** Internal helper function for save().
     *  Returns the global ID of this cell as a byte array, if possible.
     *  In the process, all temporary scroll identifiers are turned into
     *  permanent ones.
     */
    public static byte[] gid(Cell c, Map transientToPermanent) {
	return gid(c.id, transientToPermanent);
    }
    public static byte[] gid(String id, Map transientToPermanent) {
        p("id: " + id);
	int x;
	while((x = id.indexOf("tmp(")) >= 0) {
	    int y = id.indexOf(")", x) + 1;
	    String trans = id.substring(x, y);
	    String perm = (String)transientToPermanent.get(trans);
	    if(perm == null)
		throw new NullPointerException("no transientToPermanent " +
					       "mapping for "+trans);
	    id = id.substring(0, x) + perm + id.substring(y);
	}
	return id.getBytes();
    }

    /** Store this space into the given mediaserver.
     *  <code>ms</code> must contain the previous version of this space.
     *  @returns The ID of the saved space; null if there were no changes
     *           (and the space thusly wasn't saved).
     */
    public Mediaserver.Id save(Mediaserver ms) throws IOException {
	if(!mutable) {
	    throw new ZZError("Cannot save immutable space");
	}
	
	p("Start saving.");

	boolean changed = false;

        final Map transientToPermanent = new HashMap();
	
	// First, save the editable included spaces.
	// This changes the structure, so it would be wise to do it before
	// we save that ;o)
	
	/** Cell identity strings of pointers to Mediaserver.Ids. */
	Map pointersToSet = new HashMap();
	
	p("Saving editable spaces.");

	for(Iterator i=getEditableSpaces().iterator(); i.hasNext(); ) {
	    PermanentSpace incl = (PermanentSpace)i.next();
	    Mediaserver.Id incl_id = incl.save(ms);
	    if(incl_id == null) continue;
	    
	    changed = true;
	    transientToPermanent.put(incl.tmpId, incl_id.getString());
	    Cell block = Id.getBlock(incl_id.getString(), this);
	    Cell c = getCell(getInclusionIdBySpace(incl));
	    c = c.s(Dims.d_spacespec_id, 2);
	    Cursor.set(c, block);
	    c = c.s(Dims.d_spacespec_id, 2);
	    p("pointer for editable slice: "+c);
	    if(c != null) {
		String identity = Id.stripHome(Id.get(c).id);
		p("Add pointer to pointersToSet: "+identity);
		pointersToSet.put(identity, incl_id);
	    }
	}

	// Save the new scroll blocks.
	
	p("Saving new scroll blocks.");

	for(Iterator i = scrolls.keySet().iterator(); i.hasNext();) {
	    String id = (String)i.next();
	    Object scrOb = scrolls.get(id);
	    if(scrOb instanceof PermanentTextScroll) continue;
	    TransientTextScroll scr = (TransientTextScroll)scrOb;
	    Mediaserver.Id gid_ms = scr.save(ms, prevId);
	    if(gid_ms != null) {
		String gid = gid_ms.getString();
		transientToPermanent.put(id, gid);
	    }
	}

	// Cells created in this block ("tmp#nn-xxx") are saved as "-xxx"
	transientToPermanent.put(tmpId, "");
	
	p("Starting to prepare the diff block.");

	StringWriter sw = new StringWriter();
	GZZ1Writer w = new GZZ1Writer(sw);
	w.start(prevId);
	
	p("  ... diffing new cells");

	GZZ1Handler.NewCells cs = w.newCellsSection();
	for(Iterator i=newCells.iterator(); i.hasNext(); ) {
	    changed = true;
	    String str = (String)i.next();
	    cs.newCell(gid(str, transientToPermanent));
	}
	cs.close();
	
	p("  ... diffing transcopies");

	for(Iterator i=transcopiedCells.entrySet().iterator(); i.hasNext();) {
	    Map.Entry entry = (Map.Entry)i.next();
	    String t = ((Cell)entry.getKey()).id;
	    Mediaserver.Id msid = (Mediaserver.Id)entry.getValue();
	    int idx = t.indexOf(";");
	    String tid = t.substring(0, idx);
	    String cid = t.substring(idx+1);
	    
	    GZZ1Handler.Transcopy ts = w.transcopySection(gid(tid, transientToPermanent), msid);
	    ts.transcopy(gid(cid, transientToPermanent));
	    ts.close();
	    
	    changed = true;
	}
        
        p("  ... diffing span transclusions");

        GZZ1Handler.SpanTransclusion sts = w.spanTransclusionSection();
        for(Iterator i=transcludedSpans.keySet().iterator(); i.hasNext();) {
            String tid = (String)i.next();
            Span1D span = (Span1D)transcludedSpans.get(tid);
            ScrollBlock scroll = span.getScrollBlock();
            String block = scroll.getID();
            if(block.startsWith("tmp(")) {
                block = (String)transientToPermanent.get(block);
                if(block == null)
                    throw new NullPointerException("No transientToPermanent "+
                                                   "mapping for scroll block");
            }
            Mediaserver.Id blockId = new Mediaserver.Id(block);
            sts.transclude(gid(tid, transientToPermanent), blockId,
                           span.offset(), span.offset() + span.length() - 1);

            changed = true;
        }
        sts.close();
	
	p("  ... committing UndoList");

	/* commit UndoList */ {
	    GZZ1Cache cache = new GZZ1Cache();
	    int stamp0 = undo.stamp0;

	    /* process vStreamDisconnects */ {
		byte[] vs_id = Id.stripHome(Dims.d_vstream_id.id).getBytes();
		GZZ1Handler.SimpleDim sd = cache.dimSection(vs_id);
		for(Iterator i=vStreamDisconnects.entrySet().iterator(); i.hasNext();) {
		    Map.Entry e = (Map.Entry)i.next();
		    Cell c = (Cell)e.getKey(), d = (Cell)e.getValue();
		    sd.disconnect(gid(c, transientToPermanent),
				  gid(d, transientToPermanent));
		}
		sd.close();
	    }
	    
	    UndoList.GIDMaker gidmaker = new UndoList.GIDMaker() {
		    public byte[] make(String s) {
			return gid(s, transientToPermanent);
		    }
		};
	    
	    int stamp1 = undo.commit(gidmaker, cache);
	    
	    if(stamp0 != stamp1) {
		cache.flush(w, false);
		changed = true;
	    }
	}

	w.close();
	
	p("Diff prepared.");

	if(!changed) {
	    // Nothing changed: we do not save, and return null.
	    p("Nothing changed; return without saving.");
	    return null;
	}
	
	p("Saving diff...");

	Mediaserver.Id nid = ms.addDatum(sw.getBuffer().toString().getBytes(), 
					 "application/x-gzigzag-GZZ1", prevId);
	mutable = false;
	
	p("Diff saved.");

	// After having saved everything, we set the pointers of the
	// included spaces. This depends on the transientToPermanent
	// mapping to map from the tmp id of the current space to
	// its permanent id, instead of the empty string (which is why we
	// can only do updating the pointers here, after the space has
	// acquired its permanent id).

	transientToPermanent.put(tmpId, nid.getString());
	
	// Now, set the pointers.
	
	p("Setting pointers...");

	for(Iterator e = pointersToSet.keySet().iterator(); e.hasNext(); ) {
	    String pointer = (String)e.next();
	    Mediaserver.Id target = (Mediaserver.Id)pointersToSet.get(pointer);
            pointer = new String(gid(pointer, transientToPermanent));
	    pa("set pointer "+pointer+" -> "+target);
	    
	    ms.setPointer(pointer, target);
	}
	
	p("Finished.");

	return nid;
    }

}
