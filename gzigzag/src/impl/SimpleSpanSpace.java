/*
SimpleSpanSpace.java
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
import java.util.*;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;

/** A space supporting the new span concept.
 * To be merged with SimpleTransientSpace.
 */

public class SimpleSpanSpace extends SimpleTransientSpace {
public static final String rcsid = "$Id: SimpleSpanSpace.java,v 1.54 2002/03/17 12:51:27 bfallenstein Exp $";
    private static void pa(String s) { System.out.println(s); }

    TransientTextScroll scroll = new TransientTextScroll();

    /** A mapping from ScrollBlock IDs to ScrollBlocks. 
     *  This must contain all ScrollBlocks this space depends on. IDs can be
     *  temporary tor permanent.
     */
    protected Map scrolls = new HashMap();
    {
	scrolls.put(scroll.getID(), scroll);
    }

    /** Cache text? */
    public boolean cacheText = true;

    protected SpanSpacepart spanSpacepart = new SpanSpacepart(this);
    public SpanSpacepart getSpanSpacepart() { return spanSpacepart; }

    protected VStreamDim vstreamdim =
	//new BlockedVStreamDim(this, new DummyObsTrigger());
	new PlainVStreamDim(this);
    public VStreamDim getVStreamDim() { return vstreamdim; }
    {
	dims.put(Id.stripHome(Dims.d_vstream_id.id), getVStreamDim());
    }

    /** The mediaserver we're using.
     *  Needed to load scrollblocks. XXX refactor
     */
    protected Mediaserver mediaserver;

    public Mediaserver getMediaserver() { return mediaserver; }

    protected Dim getSpecialDim(Cell id) {
	if(id.id.equals(Dims.d_vstream_id.id))
	    return getVStreamDim();
	return super.getSpecialDim(id);
    }

    public boolean exists(String id) {
	if(id.lastIndexOf("$") < 0)
	    return super.exists(id);
	else
	    return spanSpacepart.exists(id);
    }

    public Cell getCell(String id) {
	if(id == null)
	    return null;
	else if(id.lastIndexOf("$") < 0)
	    return super.getCell(id);
	else
	    return spanSpacepart.getCell(id);
    }

    private Cell lastTid;
    private Span1D lastTranscludedSpan = null;

    /** Make a rank representing an existing text span.
     *  @param slice A cell in whose slice to create this.
     */
    public Cell makeSpanRank(VStreamDim vs, TextSpan s, Cell slice) {
	String block = s.getScrollBlock().getID();
	if(scrolls.get(block) == null)
	    scrolls.put(block, s.getScrollBlock());

	boolean isUpdate = (lastTranscludedSpan != null &&
			    lastTranscludedSpan.append(s) != null);

	Span appended = null;
	if(lastTranscludedSpan != null)
	    appended = lastTranscludedSpan.append(s);

	Cell tid;
	if(appended != null)
	    tid = lastTid;
	else
	    tid = N();
	lastTranscludedSpan = s;
	lastTid = tid;

	//String prefix = tid.id + ";" + s.getScrollBlock().getID() + "$";
        //int offs = s.offset();
	//Cell c[] = new Cell[(int)s.length()];
	//for(int i=0; i<c.length; i++) {
	//    c[i] = getCell(prefix + (offs + i));
	//    if(i > 0) vs.connect(c[i-1], c[i]);
	//}
	//return c[0];

	SpanSpacepart.Ref r = spanSpacepart.getRef(tid.id + ";" + s.getScrollBlock().getID() + "$");
	Cell c = spanSpacepart.getCell(r, s.offset());

	vs.notifyTransclusion(c, s.length());

	return spanSpacepart.getCell(r, s.offset());
    }

    public Cell makeSpanRank(TextSpan s, Cell slice) {
	return makeSpanRank(getVStreamDim(), s, slice);
    }

    public Cell makeSpanRank(TextSpan sp) {
	return makeSpanRank(sp, getHomeCell());
    }

    /** Make a rank representing a new text span. 
     *  @param slice A cell in whose slice to create this.
     */
    public Cell makeSpanRank(String s, Cell slice) {
	if(s.length() == 0) throw new IllegalArgumentException("empty string");
	try {
	    TextSpan sp = scroll.append(s.charAt(0));
	    for(int i=1; i<s.length(); i++)
		sp = (TextSpan)sp.append(scroll.append(s.charAt(i)));
	    return makeSpanRank(sp, slice);
	} catch(ImmutableException e) {
	    e.printStackTrace();
	    throw new ZZError("Weird, the scroll should be mutable: "+e.getMessage());
	}
    }

    public Cell makeSpanRank(String s) {
	return makeSpanRank(s, getHomeCell());
    }

    protected Map textcache = new HashMap();

    public void invalidateCache(String cell) {
	invalidateCache(getCell(cell));
    }

    public void invalidateCache(Cell cell) {
	if(!cacheText) return;
	textcache.remove(cell);
    }

    public String getText(Cell c, Obs o) {
	c = c.getRootclone(o);
	if(cacheText) {
	    String cached = (String)textcache.get(c);
	    if(cached != null) return cached;
	}

	VStreamDim dim = getVStreamDim();
	
	if(c.s(dim, -1) != null) {
	    // we're in a vstream
	    Span s = c.getSpan();
	    if(s instanceof TextSpan)
		return ((TextSpan)s).getText();
	    else
		return "";
	}

	if(c.s(dim) == null)
	    return "";

        final StringBuffer buf = new StringBuffer();
        dim.iterate(new org.gzigzag.vob.CharRangeIter() {
                public void range(Object tag, char[] chars, 
				  int first, int last) {
                    buf.append(chars, first, last-first+1);
                }
            }, c, null);

	String stream = buf.toString();

	if(cacheText)
	    textcache.put(c, stream);
	return stream;
    }

    public Span getSpan(Cell c, Obs o) {
	if(c.spacepart != spanSpacepart)
	    return super.getSpan(c, o);

	SpanSpacepart.Ref r = (SpanSpacepart.Ref)c.inclusionObject;

	return r.block.getSpan(c.inclusionIndex, 1);
    }

    public TextScrollBlock getTextScroll(String id) {
        Object res = scrolls.get(id);
	if(res == null) 
            throw new ZZError("invalid block id for getTextScroll(): "+id+
			"\nHave: "+scrolls);
	return (TextScrollBlock)res;
    }

    public void setText(Cell c, String s) {
	Dim vs = (Dim)getVStreamDim();
	c.disconnect(vs, 1);
	if(s != null)
	    c.connect(vs, makeSpanRank(s, c));
	textcache.remove(c);
    }

    public Cell transcopy(Cell c) {
        if(c.id.indexOf("tmp(") >= 0)
            throw new ZZError("cannot transclude cell without gid");
        Cell tid = N();
        Cell res = new Cell(this, tid.id + ";" + c.id);

        Set transcopied = new HashSet(); transcopied.add(res.id);

	Cell vs = Dims.d_vstream_id;
        for(Cell d = c.s(vs); d != null; d = d.s(vs)) {
            String id = tid.id + ";" + d.id;
            if(!transcopied.contains(id)) {
		String block = id.substring(id.lastIndexOf(";") + 1,
					    id.lastIndexOf("$"));
		if(scrolls.get(block) == null) try {
		    Mediaserver.Id blockId = new Mediaserver.Id(block);
		    scrolls.put(block, ScrollBlockManager.getScrollBlock(mediaserver, blockId));
		} catch(ScrollBlockManager.CannotLoadScrollBlockException e) {
		    throw new Error("problem loading scroll block: "+e.getMessage());
		}
                cells.put(id, new Cell(this, id));
		transcopied.add(id);
	    }
	}

        cells.put(res.id, res);
        return res;
    }
}
