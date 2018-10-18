/*   
ZZDimSpace.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka (ID-stuff and d.cellcreation by Tuukka Hastrup)
 */
package org.gzigzag;
import java.util.*;

/** A dimension-centric implementation of a space.
 * The internals of this implementation are done by dimension objects
 * which know how cells are connected in their dimension.
 * @see ZZDimension
 */

public class ZZDimSpace extends ZZSpace {
public static final String rcsid = "$Id: ZZDimSpace.java,v 1.49 2001/03/07 12:51:24 ajk Exp $";

// XXX These should be made garbage collectable ASAP!

	/** ZZDimension given string.  */
	Hashtable dims = new Hashtable();

	/** DimCell given id.  */
	Hashtable cells = new Hashtable();

	/** Space parts by names. */
	Hashtable spaceParts = new Hashtable();

	/** Contents by id. */
        Hashtable contents = new Hashtable();

	/** Cell by span. 
	 * If this is null, overlapping spans cannot be searched.
	 */
	SpanSet spanset = null;
    
    public ZZDimSpace() { this(false); }
    public ZZDimSpace(boolean readonly) {
        super(readonly);
    }

	/** Obtain the space part corresponding to the given ID.
	 */
	public ZZSpacePart getSpacePartByID(String id) {
	    return (ZZSpacePart)spaceParts.get(id);
	}

	/** Obtain the ZZDimension corresponding to the given string.
	 */
	final public ZZDimension d(String s) { 
		ZZDimension r = (ZZDimension)dims.get(s); 
		if(r==null) {
		    r = createDimension(s);
		    if(r == null) 
			throw new ZZError("Illegal dimension: '"+s+"'");
		    dims.put(s,r);
		    if (!readonly) updateMasterDimList(s);
		}
		return r;
	}

	/** The cell given by the identifier, <b>if already created</b>.
	 * Note that this routine only returns existing cells,
	 * i.e. objects that have already been created.
	 * Dormant cells or virtual cells need to be obtained through
	 * getCellByID.
	 */
	DimCell c(String id) { return (DimCell)cells.get(id); }

	/** Obtain the cell given by the identifier.
	 */
	public ZZCell getCellByID(String s) {
            int at = s.indexOf('@');
            if (at != -1 && getIDOrNull() != null && s.substring(at+1).equals(getID())) {
                s = s.substring(0, at);
                at = -1;
            }
	    int ind = s.indexOf(':');
            if (at == -1 && ind == -1) {
                try {
                    if (nextID != null) {
                        long cid = Long.parseLong(s);
                        long nid = Long.parseLong(nextID);
                        if (cid >= nid) setNextID("" + (cid+1));
                    }
                } catch(NumberFormatException e) {
                    return null;
                }
            }
	    if(ind != -1) {
		ZZSpacePart part = getSpacePartByID(s.substring(0, ind));
		if(part == null) 
		    throw new ZZError("No such part for cell "+s);
		return getCellByID(part, s.substring(ind+1));
	    } 
	    return getCellByID(null, s);
	}
    
	/** Obtain a cell, possibly in a space part.
	 */
	public DimCell getCellByID(ZZSpacePart p, String s0) {
	    String s = (p != null ? p.id + ":" + s0 : s0);
            DimCell ret = null;
            if (p != null) ret = p.getCellByID(s0);
	    if (ret == null) ret = (DimCell) cells.get(s);
	    if(ret==null) {
		DimCell d = new DimCell(s, p, 
				    (p != null ? p.parseIDPart(s0) : null));
		cells.put(d.id, d);
		ret = d;
	    }
	    return ret;
	}

	public DimCell getCellByID(ZZSpacePart p, String s0, Object o) {
	    String s = p.id + ":" + s0;
            DimCell ret = null;
            if (p != null) ret = p.getCellByID(s0);
	    if (ret == null) ret =  (DimCell) cells.get(s);
	    if(ret==null) {
		DimCell d = new DimCell(s, p, o);
		cells.put(d.id, d);
		ret = d;
	    }
	    return ret;
	}
	
	public ZZCell[] overlaps(Span sp) {
	    if(spanset == null) return null;
	    Object[] o = spanset.overlaps(sp);
	    ZZCell[] res = new ZZCell[o.length];
	    for(int i=0; i<o.length; i++) res[i] = c((String)o[i]);
	    return res;
	}

	ZZObsTrigger textTrig = new ZZObsTrigger();

	public void invalidateText(String id) {
            DimCell dc = c(id);
            if (dc == null) {
                ZZLogger.log("FIXME: invalidateText called with a nonexistent ID!");
                return;
            }
	    // Do nothing now. Maybe?
	}

	protected Span getSpan(String id) {
	    return (Span) contents.get(id);
	}
	protected String getText(String id) {
	    String s = (String) contents.get(id);
	    return s;
	}
        protected void setText(String id, Object ct) {
	    contents.put(id,ct);
	    if(spanset != null && ct instanceof Span)
		spanset.addSpan((Span)ct, id);
	    // XXX what if a span content is replaced by a non-span content?!?
	    // -- for now we can assume span contents don't change (I know
	    // no span-using code that does) but later, we'll need that?
	}

	/** A ZZCell in the dimspace representation.
	 * Interestingly, in this representation the cell is simply
	 * its identifier and the implicit reference to the surrounding
	 * ZZDimSpace object.
	 * <p>
	 * The reason for this is that it needs to be allowable for
	 * two cell objects with the same ID to be created without
	 * too much confusion.
	 * <p>
	 * This is because we're aiming for Java 1.1 which does not 
	 * have weak references.
	 */
	public class DimCell extends ZZCellHandle {

		protected DimCell(String id) {
		    super(id, null, null);
		}
            protected DimCell(ZZSpacePart part, Object parsedID) {
                this(part.generateID(parsedID), part, parsedID);
            }

		protected DimCell(String id, ZZSpacePart p, Object o) {
		    super(id, p, o);
		}

		public String[] getRankNames() { 
			throw new ZZError("getRankNames Not implemented");
		}

		public final ZZSpace getSpace() { return ZZDimSpace.this; }

		public void connect(String dim, ZZCell to) {
                    this.disconnect(dim, 1);
                    to.disconnect(dim, -1);
			d(dim).connect(this, ((DimCell)to));
		}
		public void disconnect(String dim, int dir) {
			d(dim).disconnect(this, dir);
		}
		public void insert(String dim, int dir, ZZCell to) {
			d(dim).insert(this, dir, ((DimCell)to));
		}
		public void hop(String dim, int steps) {
		    d(dim).hop(this, steps);
		}

		public ZZCell s(String dim, int dir, ZZObs o) {
			return d(dim).s(this, dir, o);
		}

		public ZZCell N(String dim, int dir, ZZObs o, long flags)
		{
		synchronized(ZZDimSpace.this) {
			DimCell n = getNewCell(id);
			insert(dim, dir, n);
			if(o != null)
			    d(dim).s(this, dir, o);
			return n;
		}
		}

		public ZZCell N() {
		synchronized(ZZDimSpace.this) {
		    return getNewCell(id);
		}
		}

		public void setText(String text) {
		synchronized(ZZDimSpace.this) {
			ZZCell rootClone = getRootclone();
			if(rootClone != this) {
			    rootClone.setText(text);
			    return;
			}
			if(part != null) {
			    part.setContent(this, text);
			} else {
			    p("TEXT");
			    ZZDimSpace.this.setText(id, text);
			    textTrig.chg(id);
			}
		}
		}
		public void setSpan(Span text) {
		synchronized(ZZDimSpace.this) {
			ZZCell rootClone = getRootclone();
			if(rootClone != this) {
			    rootClone.setSpan(text);
			    return;
			}
			if(part != null) {
			    part.setContent(this, text);
			} else {
			    ZZDimSpace.this.setText(id, text);
			    textTrig.chg(id);
			}
		}
		}

		// XXX part.getText should NOT BE HERE!!!!
		public String getText(ZZObs o) {
		synchronized(ZZDimSpace.this) {
		    String s;
		    // XXX Interaction with slices??
		    // XXX Interaction with ZZObs
			ZZCell rootClone = getRootclone();
			if(rootClone != this)
			    return rootClone.getText(o);
			if(o != null) textTrig.addObs(id, o);
			if(part != null)
			    s = part.getText(this);
			else
			    s = ZZDimSpace.this.getText(id);
			if( s == null )
			    return "";
			return s;
		}
		}
		public Span getSpan(ZZObs o) {
		synchronized(ZZDimSpace.this) {
		    ZZCell rootClone = getRootclone();
		// XXX As in getText!!!
		    if(rootClone != this)
			return rootClone.getSpan(o);
		    if(o != null) textTrig.addObs(id, o);
		    if(part != null)
			return part.getSpan(this);
		    return ZZDimSpace.this.getSpan(id);
		}
		}

		public boolean equals(ZZCell c) {
		    DimCell o = (DimCell)c;
		    return (this == o) ||
			(ZZDimSpace.this == o.getSpace() &&
			    id.equals(o.id));
		}

		public void delete() {
		synchronized(ZZDimSpace.this) {
			ZZDimSpace.this.deleteCell(this);
		}
		}
		
		public ZZCell h(String dim, int dir, 
			boolean ensuremove, ZZObs o) {
		    ZZCell res = d(dim).h(this, dir, o);
		    if(ensuremove && (res == this)) return null;
		    return res;
		}

	}

	void deleteCell(ZZCell c) { 
	    for(Enumeration e = dims.keys(); e.hasMoreElements(); ) {
		((ZZDimension)(dims.get(e.nextElement()))).excise((DimCell)c);
	    }
	}

        String nextID="2"; // first cell is home cell, ID 1
    protected void setNextID(String s) {
        nextID = s;
    }

	/** Get the cell ID relative to a given ID, in creation order.
	 *  @param id		Given ID
	 *  @param steps	How many steps to take (negative means earlier)
	 */
        public String getRelativeCellID(String id, int steps) {
                try {
                        long i = Long.parseLong(id);
                        if(i<0 || i+steps<1)
                                return null;
                        return ""+(i+steps);
                } catch(NumberFormatException e) {
                        return null;
                }
        }
        protected String getFreeCellID() {
                String curID = nextID;
                setNextID(getRelativeCellID(nextID, 1));
                return curID;
        }

        // XXX What's this? (compared to the next)
        protected DimCell getNewCell(String id) {
                return getNewCell();
        }
        protected DimCell getNewCell() {
                return (DimCell)getCellByID(nextID);
        }
	public ZZCell newCell() {
		return getNewCell();
	}

	/** A simple dimension listing the cells in creation order.
	 *  It uses getRelativeCellID() to find the cells, and will check
	 *  nextID to know whether the cell doesn't exist yet.
	 *  XXX The positive steps taken must be 1 or skipping nextID 
	 *  is possible!
         */
        class IDDimension extends ZZRODimension {
		/** get a cell ID along the dimension.
		 * XXX Obs doesn't work correctly.
                 */
                public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
                        String id = getRelativeCellID(c.id, steps);
                        if(id == null || id.equals(nextID))
                                return null;
                        return (DimCell)getCellByID(id);
                }
        }



	// XXX posConnections()?
	public ZZCell[] findLongRankHeads(String dim) { return null; }

	public void rmAllObs(ZZObs o) { }

	public String getHomeCellID() { 
	    return "1";
	}
	public ZZCell getHomeCell() { 
	    return getCellByID(getHomeCellID());
	}

	/** Create a new dimension with the name s.
	 * In order to make things fast, this is the method
	 * to override instead of the d() method (which is final
	 * exactly because of that).
	 * <p>
	 * The default version simply returns a new ZZLocalDimension,
	 * except for d.cellcreation
	 * <p>
	 * Note that this routine should NOT touch the dimension hash
	 * but simply return the new dimension.
	 */
	protected ZZDimension createDimension(String s) {
	    if(s.indexOf(':') != -1) return createPartDimension(s);
		if(!validDim(s)) return null;
		ZZDimension l;
		if(s.equals("d.cellcreation")) {
			l = new IDDimension();
		} else {
			l = new ZZLocalDimension();
		}
		l.setSpace(this);
		return l;

	}

	protected ZZDimension createPartDimension(String s) {
	    int ind = s.indexOf(':');
	    ZZSpacePart part = getSpacePartByID(s.substring(0, ind));
	    ZZDimension d = part.getDim(s.substring(ind+1));
	    d.setSpace(this);
	    return d;
	}


}

