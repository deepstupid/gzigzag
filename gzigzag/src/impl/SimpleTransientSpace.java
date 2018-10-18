/*   
SimpleTransientSpace.java
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
 * Written by Tuomas Lukka and Tuukka Hastrup
 */

package org.gzigzag.impl;
import org.gzigzag.mediaserver.*;
import java.util.*;
import org.gzigzag.*;

/** A ZZ space - a set of cells and connections.
 * This class is transient - it has no functionality for saving
 * or loading but it is used by the current persistent space class
 * between saves and loads.
 */

public class SimpleTransientSpace extends AbstractSpace implements GIDSpace {
public static final String rcsid = "$Id: SimpleTransientSpace.java,v 1.78 2002/03/25 20:33:08 tjl Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }


    // Very first thing, call init().
    {
	init();
    }


    /** The cells keyed by id.
     */
    final Map cells = new HashMap();

    /** The home cell of the space.
     */
    final Cell home = new Cell(this, "home-cell");
    { cells.put(home.id, home); }
    
    /** The home-id.
     */
    final Cell homeid = new Cell(this, "home-id");
    { cells.put(homeid.id, homeid); }

    /** The home-block.
     */
    final Cell homeblock = new Cell(this, "home-block");
    { cells.put(homeblock.id, homeblock); }

    /** The serial number of the next cell to be created.
     *  For block id bid and serial number n, the cell id is "bid-n".
     */
    int nextCell = 1;

    /** Dims keyed by cell id (String).
     *  No spacepart dims are allowed here, for now!
     */
    final HashMap dims = new HashMap();

    /** Whether this space is mutable.
     */
    protected boolean mutable = true;

    /** The clone dimension.
     */
    final CloneDim cloneDim = createCloneDim();
    { dims.put(Id.stripHome(Dims.d_clone_id.id), cloneDim.base); }

    /** Cache: maps cells to their rootclones.
     *  NOTE: CloneDim removes cells from this cache when their
     *  connections change.
     */
    final Map rootcloneCache = new HashMap();

    /** The ids of the cells created in this space. */
    final Collection newCells = new ArrayList();

    /** The temporary Id of this space. */
    String tmpId = ScrollBlockManager.getTmpID();

    public org.gzigzag.mediaserver.Mediaserver.Id getLastId() {
	return null;
    }
    public Cell transcopy(Cell c) {
	if(c.id.indexOf("tmp(") >= 0)
	    throw new ZZError("cannot transclude cell without gid");
	Cell tid = N();
	Cell res = new Cell(this, tid.id + ";" + c.id);
	if(c.t() != null)
	    setText(res, c.t());
	cells.put(res.id, res);
	return res;
    }
    public Map getRealDims() {
	return dims;
    }

    /** Create the clone dimension for this space. */
    protected CloneDim createCloneDim() {
	return new CloneDim(new SimpleDim(this, getObsTrigger()));
    }

    /** Get the special dim for this ID, if any.
     *  If there is a special, non-standard Dim object associated with this
     *  ID, return it; otherwise, return <code>null</code>.
     *  <p>
     *  Subclasses that override this method should return 
     *  <code>super.getSpecialDim(id)</code>
     *  if they do not provide a special Dim object for this dimension ID
     *  themselves.
     *  @param id From the home-id spacepart in this space.
     */
    protected Dim getSpecialDim(Cell id) {
	if(id.id.equals(Dims.d_clone_id.id))
	    return getCloneDim();
	return null;
    }

    protected ObsTrigger trigger;
    public ObsTrigger getObsTrigger() {
	if(trigger == null) trigger = new SimpleObsTrigger();
	return trigger;
    }

    /** Do initialization that has to be done before anything else. 
     *  XXX kludge...
     */
    protected void init() {
    }

    //
    // --- From here on, just implementation of Space.

    public Object getJavaObject(Cell c, Obs o) {
	Cell root = getRootclone(c);
	if(c.equals(root))
	    return null;
	else
	    return root.getJavaObject(o);
    }

    public Cell getRootclone(Cell c, Obs o) {
	Cell res = (Cell)rootcloneCache.get(c);
	if(res == null) {
	    res = super.getRootclone(c, o);
	    rootcloneCache.put(c, res);
	}
	return res;
    }
    
    public Cell getHomeCell() { 
	return home;
    }

    public boolean exists(String id) {
	if(cells.get(id) != null) return true;;
	// home-id: -cells must exist. Sadly, an interaction
	// between this and higher-level code.
	if(id.indexOf("home-id:") == 0)
	    return true;
	return false;
    }

    public Cell getCell(String id) {
	if(id == null) return null;
	if(!exists(id)) {
	    throw new ZZImpossibleCellException("Cell "+id+" does not exist.");
	}
        Cell c = (Cell)cells.get(id);
        if(c == null) {
            c = new Cell(this, id);
            cells.put(id, c);
        }
        return c;
    }

    public Dim getDim(Cell ref) {
	ref = Id.get(ref, this);
	Dim d = getSpecialDim(ref);
	if(d != null) return d;
	d = (Dim)dims.get(Id.stripHome(ref.id));
	if(d == null) {
	    d = new SimpleDim(this, getObsTrigger());
	    dims.put(Id.stripHome(ref.id), d);
	}
	return d;
    }

    public Cell N(Cell c) {
	if(!mutable) throw new ZZError("not mutable");
	String id = tmpId + "-" + nextCell;
	nextCell++;
	newCells.add(id);
	Cell nu = new Cell(this, id);
	cells.put(id, nu);
	return nu;
    }

    public Cell N(Cell c, Dim d, int dir, Obs o) {
	if(!mutable) throw new ZZError("not mutable");
	Cell n = N(c);
	try {
	    d.insert(c, dir, n); // XXX Obs!!!
	} catch(ZZAlreadyConnectedException e) {
	    throw new ZZError("Something really weird happened: "+e);
	}
	return n;
    }

    public void delete(Cell c) {
        if(!mutable) throw new ZZError("not mutable");
	// XXX Nothing
    }

    public Dim getCloneDim() {
	return cloneDim;
    }

    Dim imageRefDim;
    public Dim getImageRefDim() {
	if(imageRefDim == null) 
	    imageRefDim = getDim(Dims.d_image_ref_id);
	return imageRefDim;
    }


    public Cell getMSBlockCell(String msid, Cell cell) {
	return getCell("home-block:"+msid);
    }

    public void setSpan(Cell c, Span s) {

	c = c.getRootclone();
	if(!(s instanceof ImageSpan))
	    throw new UnsupportedOperationException(
			"no setSpan any more except for images");
	ImageSpan imgspan = (ImageSpan) s;

	java.awt.Dimension sd = imgspan.getSize();
	java.awt.Point sp = imgspan.getLocation();

	ScrollBlock sb = s.getScrollBlock();
	int x = sp.x,
	    y = sp.y,
	    w = sd.width,
	    h = sd.height;
	String block = sb.getID();

	Dim d = getImageRefDim();
	// c = c.getOrNew(d, 1);
	c.disconnect(d, 1); // XXX Save by checking more!

	Cell msblock = getMSBlockCell(sb.getID(), c);
	p("MSBLOCK CELL: "+msblock.id);
	// XXX Problems if included slice is not editable!?
	Cell bc = msblock.zzclone();
	c.connect(d, bc);
	c = bc;
    /* Don't allow this condition
	if(x == 0 && y == 0 && w == sb.width && h == sb.height) {
	    // Full image -- no need to put in coordinates
	    c.disconnect(d, 1);
	    return;
	} 
    */
	c = c.getOrNew(d, 1); c.setText(""+x);
	c = c.getOrNew(d, 1); c.setText(""+y);
	c = c.getOrNew(d, 1); c.setText(""+w);
	c = c.getOrNew(d, 1); c.setText(""+h);

	if(s instanceof PageSpan) {
	    PageSpan page = (PageSpan)s;
	    int offs = page.offset(),
		len = page.offset() + page.length();
	    c = c.getOrNew(d, 1); c.setText(""+offs);
	    c = c.getOrNew(d, 1); c.setText(""+len);
	} else {
	    c.disconnect(d, 1);
	}
    }

    public Span getSpan(Cell c, Obs o) {
    // XXX cache!
    // XXX Obs!!
    Span span  = null;
    try {

	pa("SimpleTransientSpace::getSpan");
	c = c.getRootclone();
	Dim d = getImageRefDim();
	c = c.s(d, 1);
	if(c == null) return null;
	pa("SimpleTransientSpace::has image ref");

	Cell block = c.getRootclone(); // XXX clones?
	if(!block.id.startsWith("home-block:")) return null;
	pa("SimpleTransientSpace::has home-block ref");

	Mediaserver ms = getMediaserver();
	Mediaserver.Id id = new Mediaserver.Id(Id.stripBlock(block.id));
    
	c = c.s(d, 1);
	if(c != null) {
	    int x = Integer.parseInt(c.t()); c = c.s(d, 1);
	    int y = Integer.parseInt(c.t()); c = c.s(d, 1);
	    int w = Integer.parseInt(c.t()); c = c.s(d, 1);
	    int h = Integer.parseInt(c.t()); 
	    // See if page numbers are present for PageSpans
	    c = c.s(d, 1);
	    if(c != null) {
		int page = Integer.parseInt(c.t()); c = c.s(d, 1);
		int len = Integer.parseInt(c.t());
		span = ScrollBlockManager.getSpan(ms, id, page, len, x, y, w, h);
		//		return ((PageSpan)span).subArea(page, len, x, y, w, h);
	    } else

		 span = ScrollBlockManager.getSpan(ms, id, x, y, w, h);
	    //return ((ImageSpan)span).subArea(x, y, w, h);
	} else {
	    // throw new Error("(Image)Span format error");
	    // pa("Span format error...");
	    pa("SimpleTransientSpace::Image format error ");
	    return null;
	}
    } catch(Throwable t) {
	pa("SimpleTransientSpace::Image error "+t);
	return null; // XXX
    }

	return span;
    }

    /** Get a SpanSet which contains all the (image!) spans
     * of this space. 
     * Text spans are not included. 
     * The Objects are the cells.
     */
    public SpanSet getSpanSet() {
	Set set = new HashSet();
	Dim d = getImageRefDim();
	d.addRealNegSides(set);
	Dim clo = getCloneDim();
	SpanSet res = new SimpleSpanSet(); // XXX
	for(Iterator i = set.iterator(); i.hasNext();) {
	    Cell c = getCell((String)i.next());
	    if(d.s(c, -1) != null) continue;
	    p("Negend: "+c);
	    Span s = getSpan(c, null) ;
	    p(" getspan: "+s);
	    if(s == null) continue;
	    while(c != null) {
		res.addSpan(s, c);
		c = clo.s(c);
	    }
	}
	p("GetSpanSet: "+res);
	return res;
    }

    /**
     *  <code>Space</code> has an implementation of this, but it does not
     *  work for <code>impl/</code> spaces because it requires the cell
     *  representing the dimension to be in this space.
     */
    public Dim getDim(String s) {
        return getDim(getCell("home-id:" + s));
    }

    // public CellTexter getCellTexter() { return celltexter; }

    /** The string contents.
     *  Only used in getText and setText. Subclasses use spans.
     *  <p>
     *  XXX merge SimpleSpanSpace into this, get rid of strs.
     */
    private HashMap strs = new HashMap();

    public void setText(Cell c, String s) {
        if(!mutable) throw new ZZError("not mutable");
	strs.put(c, s);
    }
    public String getText(Cell c, Obs o) {
	return (String)strs.get(c);
    }
}
