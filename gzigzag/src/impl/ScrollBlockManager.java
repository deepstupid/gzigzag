/*   
ScrollBlockManager.java
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
 * Written by Tuomas Lukka
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.util.*;
import org.gzigzag.mediaserver.*;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Image;
import java.util.*;
import java.io.*;

/** A central place which takes care of the ordering of scroll blocks.
 * At the moment, the ordering is transient.
 */

public class ScrollBlockManager {
String rcsid = "$Id: ScrollBlockManager.java,v 1.34 2002/03/24 19:50:54 tjl Exp $";
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }
    final static void pa(String s) { System.out.println(s); }

    /** A base class for physical spans that belong to one
     * particular scrollblock.
     */
    static abstract public class SpanBase implements Span {
	final ScrollBlock sb;
	SpanBase(ScrollBlock sb) { this.sb = sb; }
	public ScrollBlock getScrollBlock() { return sb; }
    }

    /** A base class for 1-D spans.
     * Implements everything except the actual getText().
     */
    static abstract public class Span1DBase extends SpanBase
					    implements Span1D {
	protected final int offs0, offs1;

	/** Create a new 1-D span, which starts at offs0 and
	 * ends just before offs1 - analogous to String.substring.
	 */
	protected Span1DBase(ScrollBlock sb, int offs0, int offs1) { 
	    super(sb);
	    this.offs0 = offs0;
	    this.offs1 = offs1;
	}

	public int offset() { return offs0; }

	public int length() { return offs1-offs0; }

	public Span1D subSpan(int o1, int o2) {
	    if(o1 < 0 ||
	       o2 > length() ||
	       o1 > o2)
		throw new IndexOutOfBoundsException();
	    return createNew(offs0+o1, offs0+o2);
	}

	protected abstract Span1DBase createNew(int offs0, int offs1);

	public Span1D subSpan(int o1) {
	    return subSpan(o1, length());
	}

	public int getRelativeStart(Span1D subspan) {
	    Span1DBase sp = (Span1DBase)subspan;
	    if(!sp.getScrollBlock().equals(this.getScrollBlock())
	       || sp.offs0 < offs0 || sp.offs1 > offs1)
		throw new Error("subspan "+subspan+" not wholly contained "+
				" in this span, "+this);
	    return sp.offs0 - offs0;
	}

	public int getRelativeEnd(Span1D subspan) {
            Span1DBase sp = (Span1DBase)subspan;
            if(!sp.getScrollBlock().equals(this.getScrollBlock())
               || sp.offs0 < offs0 || sp.offs1 > offs1)
                throw new Error("subspan "+subspan+" not wholly contained "+
                                " in this span, "+this);	
	    return sp.offs1 - offs0;
	}

	public Span1D append(Span s) {
	    if(!(s instanceof Span1DBase)) return null;
	    Span1DBase sts = (Span1DBase)s;
	    if(sts.getScrollBlock() != this.getScrollBlock()) return null;
	    if(offs1 == sts.offs0) 
		return createNew(offs0, sts.offs1);
	    return null;
	}

	private Span1DBase isSame(Span s) {
	    if(!(s instanceof Span1DBase)) return null;
	    Span1DBase t = (Span1DBase)s;
	    if(!this.getScrollBlock().equals(t.getScrollBlock())) return null;
	    return t;
	}

	public boolean intersects(Span s) {
	    Span1DBase t = isSame(s);
	    if(t == null) return false;
	    return (offs0 < t.offs1) && (offs1 > t.offs0);
	}

	public boolean intersectsAfter(Span s) {
	    Span1DBase t = isSame(s);
	    if(t == null) return false;
	    return (offs0 < t.offs1);
	}

	public boolean intersectsBefore(Span s) {
	    Span1DBase t = isSame(s);
	    if(t == null) return false;
	    return (offs1 > t.offs0);
	}

	public boolean equals(Object o) {
	    if(!(o instanceof Span1DBase)) return false;
	    Span1DBase s = (Span1DBase)o;
	    return (s.getScrollBlock() == this.getScrollBlock() 
			&& s.offs0 == offs0 && s.offs1 == offs1);
	}

	public String toString() {
	    return "SPAN1D("+sb+" "+offs0+" "+offs1+")";
	}

    }

    /** A base class for image spans.
     */
    static abstract public class ImageSpanBase extends SpanBase
	implements ImageSpan {
	protected final int x, y, w, h;
	ImageSpanBase(ScrollBlock sb, int x, int y, int w, int h) {
	    super(sb);
	    this.x = x;
	    this.y = y;
	    this.w = w;
	    this.h = h;
	}
	protected abstract ImageSpanBase 
	    createNew(int x, int y, int w, int h);

	public Dimension getSize() { return new Dimension(w, h); }
	public Point getLocation() { return new Point(x, y); }
	public ImageSpan subArea(int x0, int y0, int w0, int h0) {
	    if(x0 < 0 || y0 < 0)
		throw new ZZError("Negative coordinates");
	    if(x0 + w0 > w || y0 + h0 > h)
		throw new ZZError("Too large imagearea");
	    return createNew(x+x0, y+y0, w0, h0);
	}

	private ImageSpanBase isSame(Object s) {
	    if(!(s instanceof ImageSpanBase)) return null;
	    ImageSpanBase t = (ImageSpanBase)s;
	    if(!this.getScrollBlock().equals(t.getScrollBlock())) return null;
	    return t;
	}

	public boolean intersects(Span s) {
	    ImageSpanBase t = isSame(s);
	    if(t==null) return false;
	    return x < t.x + t.w && x + w > t.x &&
		   y < t.y + t.h && y + h > t.y;
	}

	public boolean equals(Object o) {
	    ImageSpanBase s = isSame(o);
	    if(s == null) return false;
	    return s.sb == sb &&
		   s.x == x &&
		   s.y == y &&
		   s.w == w &&
		   s.h == h;
	}
    }

    /** A base class for page spans. This duplicates ImageSpanBase
     * because Java doesn't allow multiple inheritance...
     */
    static abstract public class PageSpanBase extends Span1DBase
				implements PageSpan {
	protected final int x, y, w, h;
	PageSpanBase(ScrollBlock sb, int page0, int page1, 
		    int x, int y, int w, int h) {
	    super(sb, page0, page1);
	    this.x = x;
	    this.y = y;
	    this.w = w;
	    this.h = h;
	}
	protected abstract PageSpanBase 
	    createNew(int page0, int page1, int x, int y, int w, int h);

	protected Span1DBase createNew(int page0, int page1) {
	    return createNew(page0, page1, this.x, this.y, this.w, this.h);
	}
	protected PageSpanBase createNew(int x, int y, int w, int h) {
	    return createNew(this.offs0, this.offs1, x, y, w, h);
	}

	public Dimension getSize() { return new Dimension(w, h); }
	public Point getLocation() { return new Point(x, y); }

	public PageSpan subArea(int p0, int p1, int x0, int y0, int w0, int h0) {
	    if(p0 < 0 || p1 > length() || p0 > p1)
		throw new ZZError("Invalid pages");
	    if(x0 < 0 || y0 < 0)
		throw new ZZError("Negative coordinates");
	    if(x0 + w0 > w || y0 + h0 > h)
		throw new ZZError("Too large imagearea");
	    return createNew(offs0 + p0, offs0+p1, x+x0, y+y0, w0, h0);
	}
	public PageSpan subArea(int p0, int p1) {
	    return createNew(offs0 + p0, offs0+p1, x, y, w, h);
	}

	public ImageSpan subArea(int x0, int y0, int w0, int h0) {
	    if(x0 < 0 || y0 < 0)
		throw new ZZError("Negative coordinates");
	    if(x0 + w0 > w || y0 + h0 > h)
		throw new ZZError("Too large imagearea");
	    return createNew(x+x0, y+y0, w0, h0);
	}

	private PageSpanBase isSamePS(Object s) {
	    if(!(s instanceof PageSpanBase)) return null;
	    PageSpanBase t = (PageSpanBase)s;
	    if(!this.getScrollBlock().equals(t.getScrollBlock())) return null;
	    return t;
	}

	public boolean intersects(Span s) {
	    PageSpanBase t = isSamePS(s);
	    if(t==null) return false;
	    return super.intersects(s) &&
		   x < t.x + t.w && x + w > t.x &&
		   y < t.y + t.h && y + h > t.y;
	}

	public boolean equals(Object o) {
	    PageSpanBase s = isSamePS(o);
	    if(s == null) return false;
	    return super.equals(o) &&
		   s.sb == sb &&
		   s.x == x &&
		   s.y == y &&
		   s.w == w &&
		   s.h == h;
	}
    }



    /** Give an ordering of the given scrollblocks.
     * @return 0 if equal, -1 if s1 &lt; s2, 1 if s1 &gt; s2.
     */
    static public int compare(ScrollBlock s1, ScrollBlock s2) {
	return s1.getID().compareTo(s2.getID());
    }

    static int tmpid = 0;

    /** Create a new temporary ID for a scrollblock.
     */
    static public String getTmpID() {
	tmpid++;
	return "tmp(" + tmpid + ")";
    }

    static public class CannotLoadScrollBlockException extends IOException {
	CannotLoadScrollBlockException(String s) { super(s); }
	CannotLoadScrollBlockException() { super(); }
    }

    public static Span getSpan(Mediaserver ms, Mediaserver.Id id, 
			       int x, int y, int w, int h) {
	SimpleImageScroll block = null;
	try {
	    block = (SimpleImageScroll)getScrollBlock(ms, id);
	} catch (CannotLoadScrollBlockException _) {
	    throw new Error("Loading image failed: "+id);
	}
	if(block==null) {
	    block = new SimpleImageScroll(ms, id.getString());
	    msCache.put(id, block);
	}
	return block.getSpan(x, y, w, h);
    }

    public static Span getSpan(Mediaserver ms, Mediaserver.Id id, 
			       int p0, int p1, int x, int y, int w, int h) {

	PageImageScroll block = null;
	
	try {
	    block = (PageImageScroll)getScrollBlock(ms, id);
	} catch (CannotLoadScrollBlockException _) {
	    throw new Error("Loading pageimage failed: "+id);
	}
	if(block==null) {
	    block = new PageImageScroll(ms, id.getString());
	    msCache.put(id, block);
	}
	return block.getSpan(p0, p1, x, y, w, h);
    }

    /** We can use this if we know a specific scroll block is a text
     *  scroll block. (We do know because we handle text transclusions
     *  in a special way.)
     */
    static public TextScrollBlock getTextScrollBlock(Mediaserver ms,
						     Mediaserver.Id id)
	throws CannotLoadScrollBlockException {
	if(id == null)
            throw new NullPointerException("cannot get block with id null");
	TextScrollBlock b = (TextScrollBlock)msCache.get(id);
	if(b == null) {
	    b = new PermanentTextScroll(ms, id);
	    msCache.put(id, b);
	}
	return b;
    }
	

    private static HashMap msCache = new HashMap();
    /** Get a scrollblock from a mediaserver block.
     * The content-type of the block determines what to do.
     * XXX This is something of a problem: we need to be able
     * to have spans work even when 
     * their targets are not currently accessible.
     * The type should therefore be gotten from somewhere...
     * <p>
     * Maybe the space storage formats should be extended to include
     * enough information on the scrollblocks:
     * type (image/text/page/audio/video), size in standard coordinates.
     * <p>
     * OTOH, we may be able to do this on the mediaserver side, propagating
     * the headers of blocks...
     * @return The scrollblock.
     */
    static public ScrollBlock getScrollBlock(Mediaserver ms,
					     Mediaserver.Id id,
					     boolean lazy)
                        throws CannotLoadScrollBlockException {
	if(id == null)
	    throw new NullPointerException("cannot get block with id null");
	ScrollBlock b = (ScrollBlock)msCache.get(id);
	if(b == null) {
	    if(lazy)
		return null;
	    b = loadScrollBlock(ms, id);
	    
	    msCache.put(id, b);
	}
	return b;
    }

    static public ScrollBlock getScrollBlock(Mediaserver ms,
					     Mediaserver.Id id)
			throws CannotLoadScrollBlockException {     
	return getScrollBlock(ms, id, false);
    }

    static private  ScrollBlock loadScrollBlock(Mediaserver ms,
						Mediaserver.Id id) 
		throws CannotLoadScrollBlockException {
	p("Loading scroll block: "+id);
				
	Mediaserver.Block block ;
        String ct;
	try {
	    block = ms.getDatum(id);
            ct = block.getContentType();
	} catch(IOException e) {
	    throw new CannotLoadScrollBlockException("Couldn't load block");
	}
	int ind = ct.indexOf('/'); 
	if(ind < 0) 
	 throw new CannotLoadScrollBlockException("Can't parse mediatype "+ ct);
	String type = ct.substring(0,ind);

	// Note: for the legacy string content to work, we need to be able
	// to load GZZ1 diffs as text blocks (see GZZ1Handler.LegacyContent
	// javadoc for more info).
	if(type.equals("text") || ct.equals("application/x-gzigzag-GZZ1")) {
	    if(!ct.equals("text/plain; charset=UTF-8") &&
	       !ct.equals("application/x-gzigzag-GZZ1"))
	     throw new CannotLoadScrollBlockException("Unknown text block '"+ct
			    +"'");
	    String s;
	    try {
		s = new String(block.getBytes(), "UTF8");
	    } catch(Exception e) {
		throw new CannotLoadScrollBlockException("Encoding problem "+e);
	    }
	    p("Loaded text scroll block.");
	    return new PermanentTextScroll(id.getString(), s);

	} else if(type.equals("image")) {
            Image img = null;
            try {
                img = GlobalToolkit.toolkit.createImage(block.getBytes());
            } catch (IOException _) {}
	    if(img == null) 
	      throw new CannotLoadScrollBlockException("Unknown image type");
	    int count = 0;
	    while(img.getWidth(null) < 0 || img.getHeight(null) < 0) {
		try {
		    count++;
		    if(count > 50) 
		      throw new CannotLoadScrollBlockException("Timeout");
		    Thread.sleep(200);
		} catch(InterruptedException e) {
		    throw new Error("Interrupted");
		}
	    }
	    p("Loaded image scroll block.");
	    return new SimpleImageScroll(id.getString(), img, 
	                      img.getWidth(null), img.getHeight(null));
	} else if(ct.equals("application/postscript") || 
		  ct.equals("application/pdf")) {
	    p("Loaded page image scroll block.");
	    return new PageImageScroll(ms, id.getString());
	} else {

	    throw new CannotLoadScrollBlockException("Unknown mediatype "+ct);
	}

    }

}


