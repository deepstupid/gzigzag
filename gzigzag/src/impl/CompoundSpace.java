/*   
CompoundSpace.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
 *    Copyright (c) 2001, Rauli Ruohonen
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
 * Written by Benja Fallenstein, based on code by Rauli Ruohonen
 */

package org.gzigzag.impl;
import java.util.*;
import org.gzigzag.*;
import org.gzigzag.vob.CharRangeIter;

/** A space capable of containing other spaces.
 *  This space does in itself not have functionality for loading and saving.
 * <p>
 * XXX A large amount of complexity is caused by using the Cell's 
 * inclusionObject field as the cell of the space below, since
 * for a while, spaces have been saved so that the creation of a new
 * inclusion and creating connections into the inclusion happen
 * at the same time.
 */

public class CompoundSpace extends SimpleSpanSpace {
public static final String rcsid = "$Id: CompoundSpace.java,v 1.18 2002/03/27 07:04:55 bfallenstein Exp $";

    public static boolean dbg = false;
    private static void pa(String s) { System.out.println(s); }
    private static void p(String s) { if(dbg) pa(s); }

    // Space parts, by inclusion ID
    // Must be SliceSpacepart's, currently
    final private Map spaceparts = new HashMap();

    // The other way 'round: inclusion IDs by spacepart
    final private Map partids = new HashMap();

    // Space parts, by included Space
    final private Map partBySpace = new HashMap();

    public GIDSpace getSpaceByInclusionId(String id) {
	return (GIDSpace)((SliceSpacepart)spaceparts.get(id)).subspace;
    }
    public String getInclusionIdBySpace(Space s) {
	return ((Spacepart)partBySpace.get(s)).getBase().id;
    }
    public Collection getSpaces() {
	Set result = new HashSet();
	for(Iterator i=spaceparts.values().iterator(); i.hasNext();) {
	    SliceSpacepart part = (SliceSpacepart)i.next();
	    result.add(part.subspace);
	}
	return result;
    }

    /** The UndoList in this space. To be used for saving... */
    // XXX Stamps!
    UndoList undo;

    protected void init() {
	super.init();
        undo = new UndoList(2935);
    }

    public boolean isEditable(Space sp) {
	Spacepart part = (Spacepart)partBySpace.get(sp);
	Spacepart.InclusionType inclusionType = part.getInclusionType();
	
	if(inclusionType == part.EDITABLE)
	    return true;

	else if(inclusionType == part.NON_EDITABLE)
	    return false;

	else
	    throw new ZZError("Unhandled inclusion type: "+inclusionType);
    }

    public Set getEditableSpaces() {
	Set s = new HashSet();

	for(Iterator i = spaceparts.values().iterator(); i.hasNext();) {
	    SliceSpacepart part = (SliceSpacepart)i.next();
	    if(part.getInclusionType() == part.EDITABLE)
		s.add(part.subspace);
	}

	return s;
    }

    static final String separator = ":";
    protected Cell msSpace = new Cell(this, "home-id:0000000008000000E7BD6F28880004C62132A8CCF343828039ECC856521E3F49A054FF08883711-2");

    private Map dimCache = new HashMap();

    /** Whether we silently accept cells from spaces which aren't loaded.
     *  Currently to be used while reading stuff in.
     * When false, using DUMMYSUBCELL is an error.
     * This should be set to false after canonicalizeCells.
     */
    protected boolean ghostcells = false;

    /** An object used to represent that a base cell is not
     * available.
     */
    public final static Object DUMMYSUBCELL = new Object();

    // Overrides method in SimpleTransientSpace
    public Cell getMSBlockCell(String msid, Cell cell) {
	Space s = getSpace(cell);
	if(s == null) {
	    return super.getMSBlockCell(msid, cell);
	} else {
	    return translate(s.getMSBlockCell(msid, detranslate(cell)));
	}
    }


    Throwable thr = new Throwable();
    protected final void dum(Cell c) {
	if(!ghostcells) {
	    System.err.println("Dummy cell when not expected: "+c.id);
	    thr.fillInStackTrace();
	    thr.printStackTrace();
	    // throw new Error("Dummy cell! "+c.id);
	    Cell c2 = getCell(c.id);
	    if(c2.inclusionObject == DUMMYSUBCELL)
		throw new Error("And the canonicalization didn't work!");
	}
    }



    protected CompoundSpace() {
        includeRaw(homeblock, ((WrapperSpace)Id.blocks).base, false);
	includeRaw(homeid, ((WrapperSpace)Id.space).base, false);
	vstreamdim = new VStreamDimProxy(this, vstreamdim);
	dims.put(Id.stripHome(Dims.d_vstream_id.id), vstreamdim);
    }


    // XXX To be removed when we do triggering.
    protected void includeRaw(Cell c, Space sp, boolean editable) {
	Spacepart.InclusionType inclusionType;
	if(editable)
	    inclusionType = Spacepart.EDITABLE;
	else
	    inclusionType = Spacepart.NON_EDITABLE;

	SliceSpacepart part = new SliceSpacepart(this, c, separator,
						 inclusionType, sp);

	includeRaw(part);
    }

    protected void includeRaw(Spacepart part) {
	Cell c = part.getBase();

        if(spaceparts.get(c.id) != null || partids.get(part) != null)
            throw new ZZError("Can't include: something is there already");

	if(part instanceof SliceSpacepart) {
	    Space sp = ((SliceSpacepart)part).subspace;

	    if(partBySpace.get(sp) != null)
		throw new ZZError("Space is already a slice: "+sp);

	    partBySpace.put(sp, part);
	}

	spaceparts.put(c.id, part);
	partids.put(part, c.id);
    }


    public void include(Cell c, Space sp, boolean editable, Cell pointer) {
	includeRaw(c, sp, editable);
	Cell x = getHomeCell().s(Dims.d_spaces_id), xp = getHomeCell();
	while (x != null) {
	    if (x.equals(c)) return;
	    xp = x;
	    x = x.s(Dims.d_spaces_id);
	}
	try {
	    xp.insert(Dims.d_spaces_id, 1, c);
	} catch(ZZAlreadyConnectedException e) {
	    throw new ZZError("Weird. c was already connected on d.spaces, "+
			      "but includeRaw did not throw an error. "+e);
	}
	if (sp instanceof GIDSpace) {
	    Cell block = Id.getBlock(((GIDSpace)sp).getLastId().getString(),
				     this);
	    try {
		c.connect(Dims.d_spacespec_id, 1, msSpace.zzclone());
		Cell curs = 
		    c.s(Dims.d_spacespec_id).N(Dims.d_spacespec_id, 1);
		Cursor.set(curs, block);
		Cell par = curs.N(Dims.d_spacespec_id);
		par.setText("" + editable);
		if(pointer!=null) 
		    par.connect(Dims.d_spacespec_id, pointer.zzclone());
		
	    } catch(ZZAlreadyConnectedException e) {
		throw new ZZError("Weird: "+e);
	    }
	}
    }

    public void include(Cell c, Space sp, boolean editable) {
	include(c, sp, editable, null);
    }

    public void include(Cell c, Space sp) {
	include(c, sp, false);
    }

    public Map getRealDims() {
	Map res = new HashMap();
        for(Iterator e = dims.keySet().iterator(); e.hasNext();) {
            String s = (String)e.next();
            res.put(s, ((CompoundSpaceDim)dims.get(s)).getBase());
        }
	return res;
    }

    public final Space byPrefix(String prefix) {
        SliceSpacepart part = (SliceSpacepart)spaceparts.get(prefix);
	if(part == null)
	    return null;
        return part.subspace;
    }

    // Get the space that contains the given cell.

    public Space getSpaceNF(String id) {
	Space s = getSpace(id);
	if(s != null) return s;
	else throw new ZZError("No such space: "+id);
    }

    public Space getSpace(String id) {
	int i = id.indexOf(separator);
	if(i < 0) return null;
	String sid = id.substring(0, i);
	return byPrefix(sid);
    }

    public Space getSpace(Cell c) { 
	if(c.space != this)
	    throw new ZZError("Cell "+c+" is not from this space");
	if(c.spacepart != null) {
	    if(c.spacepart instanceof SliceSpacepart)
		return ((SliceSpacepart)c.spacepart).subspace;
	    else
		return null;
	}
	else if(c.inclusionObject == null) return null;
	else if(c.inclusionObject == DUMMYSUBCELL) {
	    dum(c);
	    return getSpace(c.id);
	}
	else return ((Cell)c.inclusionObject).space;
    }

    public Space getSpaceNF(Cell c) { 
        Space s = getSpace(c);
        if(s != null) return s;
        else throw new ZZError("No such space: "+c.id+" "+c.spacepart+" "+
	    c.inclusionObject);
    }

    public Cell getIncludedCell(Cell prefix, Cell id) {
	Space s = byPrefix(prefix.id);
	if (s == null) throw new ZZError("No such space: "+prefix);
	if (!id.id.startsWith("home-id:"))
	    throw new IllegalArgumentException("Not an id cell: "+id);
	return translate(s.getCell(Id.stripHome(id.id)));
    }

    public Dim getDim(Cell dimCell) {
	Dim d = (Dim)dimCache.get(dimCell);
	if(d == null) {
	    dimCell = dimCell.getRootclone();
	    Cell identity = Id.get(dimCell, this);
	    d = getSpecialDim(identity);
	    if(d == null) {
		d = (Dim)dims.get(Id.stripHome(identity.id));
		if(d == null) {
		    d = new CompoundDim(this, identity, getObsTrigger());
		    dims.put(Id.stripHome(identity.id), d);
		}
	    }
	    dimCache.put(dimCell, d);
	}
	return d;
    }

    public Cell getCell(String id) {
        if(id == null) return null;
	Cell c = (Cell)cells.get(id);
	if(c != null) return c;
	
        int i = id.indexOf(separator);
        if(i < 0)
	    return super.getCell(id);
	else {
	    String sid = id.substring(0, i);
	    SliceSpacepart part = (SliceSpacepart)spaceparts.get(sid);
	    if(part == null) {
		if(ghostcells) {
		    return new Cell(this, id, null, DUMMYSUBCELL, -1); 
			// XXX ??? XXX ??? !!!
		}
		throw new IllegalArgumentException("No such space: "+sid);
	    }

	    return part.getCell(id);
	}
    }

    public boolean exists(String id) {
	if(id.indexOf(separator) < 0)
	    return super.exists(id);
	else {
	    if(ghostcells) return true;
	    Space s = getSpace(id);
	    return s != null && s.exists(detranslate(id));
	}
    }

    /** Get the homecell in the slice the cell is from
     */

    public Cell getHomeCell(Cell inSlice) {
	Space space = getSpace(inSlice);
	if(space!=null)
	    return translate(space.getHomeCell());
	return getHomeCell();
    }

    public Cell N(Cell c) {
	if(!mutable) throw new ZZError("not mutable");
	if(c == null) return super.N(null);
	Space sp = getSpace(c);
	if(sp == null || !isEditable(sp))
	    return super.N(c);
	else {
	    Cell result = translate(sp.N(detranslate(c)));
	    pa(""+result);
	    if(!getSpace(result).equals(sp))
		throw new Error("ARGH: N(Cell) did not yield correct space");
	    return result;
	}
    }

    public void delete(Cell c) {
	// XXX Nothing
    }

    public Span getSpan(Cell c, Obs o) {
	c = c.getRootclone();
	if(isIncluded(c))
	    return getSpaceNF(c).getSpan(detranslate(c), o);
	else
	    return super.getSpan(c, o);
    }

    public void setSpan(Cell c, Span span) {
	c = c.getRootclone();
	if(isIncluded(c))
	    getSpaceNF(c).setSpan(detranslate(c), span);
	else
	    super.setSpan(c, span);
    }

    public void setText(Cell c, String s) {
	c = c.getRootclone();
        Space sp = getSpace(c);
	if(sp == null)
	    super.setText(c, s);
	else if(!isEditable(sp))
	    throw new ZZError("included, non-editable space");
	else
	    sp.setText(detranslate(c), s);
    }
    public String getText(Cell c, Obs o) {
	c = c.getRootclone();
	if(isIncluded(c))
	    return getSpaceNF(c).getText(detranslate(c), o);
	else
	    return super.getText(c, o);
    }
    public Object getJavaObject(Cell c, Obs o) {
	c = c.getRootclone();
	if(isIncluded(c))
	    return getSpaceNF(c).getJavaObject(detranslate(c), o);
	else
	    return super.getJavaObject(c, o);
    }

    public boolean isIncluded(Cell c) {
	return c.id.indexOf(separator) > -1;
    }
    public void assertFromHere(Cell c) {
	if(isIncluded(c))
	    throw new ZZError("included space");
    }

    // XXX think about this
    Map translationCache = new HashMap();

    public Cell translate(Cell c) {
	if(c == null) return null;
	if(c.space == this) return c;

	Cell d = (Cell)translationCache.get(c);
	if(d == null) {
	    SliceSpacepart part = (SliceSpacepart)partBySpace.get(c.space);
	    d = part.fromSub(c);
	    translationCache.put(c, d);
	}
	// XXX too much time?
	if(d.inclusionObject == DUMMYSUBCELL) dum(d);
	return d;
    }

    public String translate(Space space, String c) {
	if(c == null) return null;
	if(space != this && space != null)
	    return getInclusionIdBySpace(space) + separator + c;
	else
	    return c;
    }

    public Cell detranslate(Cell c) {
        if (c.space != this)
            throw new IllegalArgumentException("Cell "+c+" is not from the"+
                                               " correct space");
	if(c.spacepart == null && c.inclusionObject == null)
	    return c;
	else if(c.inclusionObject == DUMMYSUBCELL) {
	    dum(c);
	    return getSpaceNF(c).getCell(detranslate(c.id));
	}
	else if(c.inclusionObject instanceof Cell)
	    return (Cell)c.inclusionObject;
	else
	    return c;
    }
    public String detranslate(String id) {
	int x = id.indexOf(separator);
	if(x >= 0)
	    return id.substring(x + separator.length());
	else
	    return id;
    }
    public CloneDim createCloneDim() {
	// XXX Should we register this somewhere?
        return new CloneDim(new CompoundDim(this, Dims.d_clone_id, getObsTrigger()));
    }

    /** Go through all dims and call canonicalizeCells on them.
     */
    protected void canonicalizeCells() {
	Collection s = dims.values();
	for(Iterator i = s.iterator(); i.hasNext(); ) {
	    ((Dim)i.next()).canonicalizeCells();
	}
	//  XXX SHould be SimpleTransientSpace's responsibility?
	cloneDim.canonicalizeCells();
	// Clear caches
	translationCache = new HashMap();
	Id.cleanCache(this);
    }
}
