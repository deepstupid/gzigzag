/*   
VersionPart.java
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
 * Written by Antti-Juhani Kaijanaho
 */
package org.gzigzag;
import java.util.*;

/** <b>EXPERIMENTAL:</b> A space part showing past versions of a ZZCacheDimSpace.
 */

public class VersionPart extends ZZROSpacePart {
public static final String rcsid = "$Id: VersionPart.java,v 1.16 2001/04/17 16:40:59 ajk Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }
    
    private static final Object nolla = new Object();

    protected static final ZZObsTrigger ot = new ZZObsTrigger();

    public static boolean regular_dimensions = false;

    protected Hashtable dimensions = new Hashtable();

    protected String listDimensionName = "list";
    protected String activeDimensionName = "active";
    public VersionPart(ZZCacheDimSpace cds, String s) {
        super(cds, s);
        if (cds == null) throw new ZZError("null cds");
        dimensions.put(listDimensionName, new ListDimension());
        dimensions.put("home", new HomeDimension());
        dimensions.put(activeDimensionName, new ActiveDimension());
        dimensions.put("version-active", new ActiveCellVersionsDimension());
    }

    // spaces and stamps form a combined data structure where spaces
    // provides fast lookup of SpaceElements based on space timestamps
    // and stamps contains the active timestamps in ascending order.
    private Hashtable spaces = new Hashtable();
    private Vector stamps = new Vector();

    /** Show what's in stamps.  This is a debugging method. */
    private void dump_stamps() {
        if (!dbg) return;

        for (int i = 0; i < stamps.size(); i++) {
            pa("stamps[" + i + "] == " + stamps.elementAt(i));
        }
    }

   /** A set of data about one space. */
    private class SpaceElement {
        /** The space timestamp. */
        Integer stampo;
        /** Index of this SpaceElement in stamps. */
        int inx;
        /** The space itself. */
        ZZCacheDimSpace space;
        /** The cell that denotes this space in the version:list
         * rank. */
        ZZDimSpace.DimCell cell;

        /** A cache of wrapper cells, keyed by unwrapped cell IDs, for
         * this space. */
        private Hashtable wrappercells = new Hashtable();
        
        /** A cache of wrapper dimensions, keyed by unwrapped
         * dimension IDs, for this space.  A key may map to "nolla",
         * which means that the key does not denote an existing
         * dimension.  */
        private Hashtable wrapperdims = new Hashtable();

        public void activate() {
            if (space == null) {
                space = new ZZCacheDimSpace((ZZCacheDimSpace)VersionPart.this.space, stampo.intValue());
                put(this);
            }
        }

        /** Get a wrapper cell for the cell "old".  */
        public VersionCell getWrapperCell(ZZCellHandle old) {
            return getWrapperCell(old, old.getID());
        }

        public VersionCell getWrapperCell(String oldid) {
            return getWrapperCell((ZZCellHandle)space.getCellByID(oldid), oldid);
        }

        public VersionCell getWrapperCell(ZZCellHandle old, String oldid) {
            if (wrappercells.containsKey(oldid)) {
                return (VersionCell)wrappercells.get(oldid);
            } else {
                VersionCell vc = new VersionCell(this, old);
                wrappercells.put(oldid, vc);
                return vc;
            }
        }

        /** Get a wrapper dimension for the dimension "dim". */
        public VersionDimension getWrapperDimension(String dim) {
            if (wrapperdims.containsKey(dim)) {
                Object o = wrapperdims.get(dim);
                if (o == nolla) return null;
                return (VersionDimension)o;
            } else {
                try {
                    wrapperdims.put(dim, new VersionDimension(this, dim));
                } catch (ZZError e) {
                    wrapperdims.put(dim, nolla);
                }
                return getWrapperDimension(dim);
            }
        }

    }

    /** Return a cell with this ID. */
    public ZZDimSpace.DimCell getCellByID(String s) {
        ID id = (ID)parseIDPart(s);
        SpaceElement se = getSpaceElement(id.stamp);
        if (id.id == null) {
            return se.cell;
        } else {
            se.activate();
            return se.getWrapperCell((ZZCellHandle)se.space.getCellByID(id.id));
        }
    }

    /** Put se in spaces and update stamps. */
    private void put(SpaceElement se) {
        synchronized (spaces) {
            int i;
            for (i = 0; i < stamps.size(); i++) {
                if (((Integer)stamps.elementAt(i)).intValue() >= se.stampo.intValue()) break;
            }
            stamps.insertElementAt(se.stampo, i);
            se.inx = i;
            for (int j = i + 1; j < stamps.size(); j++) {
                Integer stampo = (Integer)stamps.elementAt(j);
                SpaceElement sep = (SpaceElement)spaces.get(stampo);
                ++sep.inx;
            }
            spaces.put(se.stampo, se);
        }
        ot.chg(spaces);
    }

    /** Get a SpaceElement corresponding to stamp. */
    protected SpaceElement getActiveSpaceElement(int stamp) {
        return getActiveSpaceElement(new Integer(stamp));
    }
    /** Get a SpaceElement corresponding to stampo and activate it. */
    protected SpaceElement getActiveSpaceElement(Integer stampo) {
        synchronized (spaces) {
            SpaceElement se = getSpaceElement(stampo);
            se.activate();
            return se;
        }
    }

    /** Get a SpaceElement corresponding to stamp. */
    protected SpaceElement getSpaceElement(int stamp) {
        return getSpaceElement(new Integer(stamp));
    }

    /** Get a SpaceElement corresponding to stampo. */
    protected SpaceElement getSpaceElement(Integer stampo) {
        synchronized (spaces) {
            if (spaces.containsKey(stampo)) {
                return ((SpaceElement)spaces.get(stampo));
            } else {
                SpaceElement se = new SpaceElement();
                se.inx = Integer.MAX_VALUE;
                se.stampo = stampo;
                se.space = null;
                se.cell = new ListCell(se);
                return se;
            }
        }
    }


    /** Return the space corresponding to stamp. */
    protected ZZCacheDimSpace getSpace(int stamp) {
        return getActiveSpaceElement(stamp).space;
    }

    /** Activate the space corresponding to stamp. */
    public void loadVersion(int stamp) {
        if (stamp > ((ZZCacheDimSpace)space).timestamp)
            stamp = ((ZZCacheDimSpace)space).timestamp;
        getActiveSpaceElement(stamp);
    }

    /** A parsed representation of a cell's or dimension's ID. */
    protected static class ID {
        /** The wrapped dimension or cell's ID or null, if this is a list cell. */
        public final String id;
        /** The space timestamp related to this cell or dimension. */
        public final int stamp;

        public ID(String id, int stamp) {
            this.id = id;
            this.stamp = stamp;
        }

        public String toString() {
            if (id == null) return "" + stamp;
            return "" + stamp + "." + id;
        }
    }

    public ZZDimension getDim(String name) {
        pa("VersionPart.getDim(" + name + ")");
        if (dimensions.containsKey(name)) return (ZZDimension)dimensions.get(name);

        if (name.substring(0, 2).equals("d."))
            return new AllVersionDimension(name);

        ID did = (ID)parseIDPart(name);
        SpaceElement cds = getActiveSpaceElement(did.stamp);

        return cds.getWrapperDimension(did.id);
    }

    /** A wrapper cell for cells in old space versions. */
    protected class VersionCell extends ZZDimSpace.DimCell {
        final ZZCellHandle old;

        public VersionCell(SpaceElement se, ZZCellHandle old) {
            VersionPart.this.space.super(VersionPart.this,
                                         new ID(old.getID(), se.stampo.intValue()));
            this.old = old;
        }

        private boolean isIgnoredDim(String dim) {
            return dim.equals("d.cursor")
                || dim.equals("d.cursor-list")
                || dim.equals("d.cursor-cargo")
                || dim.indexOf(':') != -1;
        }

        public  ZZCell s(String dim, int dir, ZZObs o) {
            ZZCell rv = s(dim, dir, o, new Object());
            p("(" + this + ").s(" + dim + ", " + dir + ", " + o + ") ==> " + rv);
            return rv;
        }

        private ZZCell s(String dim, int dir, ZZObs o, Object foo) {
            if (!regular_dimensions || isIgnoredDim(dim))
                return super.s(dim, dir, o);

            // Connected in current version?  If yes, use the connection.
            ZZCell c = super.s(dim, dir, o);
            if (c != null) return c;

            // No?  Is there a connection in past active versions.
            // If yes, use the newest.
            synchronized (spaces) {
                int n = stamps.size();
                for (int i = n - 1; i >= 0; --i) {
                    Integer stampo = (Integer)stamps.elementAt(i);
                    p("trying with " + stampo);
                    SpaceElement se = getActiveSpaceElement(stampo);
                    VersionDimension vd = se.getWrapperDimension(dim);
                    c = vd.s(this, dir, o);
                    if (c != null) return c;
                }
            }

            // No?  Then there is no connection.
            return null;
        }

        public void disconnect(String dim, int dir) {
            if (regular_dimensions && !isIgnoredDim(dim)) {
                if (s(dim, dir, null) != null) {
                    throw new ZZError("immutable connection");
                }
            }

            super.disconnect(dim, dir);
        }
        

        public void setText(String text) {
            throw new ZZError("readonly cell");
        }
        public void setSpan(Span text) {
            throw new ZZError("readonly cell");
        }
        public String getText(ZZObs o) {
            return old.getText(o);
        }
        public Span getSpan(ZZObs o) {
            return old.getSpan(o);
        }
        
        public void delete() {
            throw new ZZError("immutable cell");
        }
    }
    
    /** A cell in the active spaces list. */
    protected class ListCell extends ZZDimSpace.DimCell {
        SpaceElement se;

        public ListCell(SpaceElement se) {
            VersionPart.this.space.super(VersionPart.this,
                                         new ID(null, se.stampo.intValue()));
            p("new ListCell, ID = " + getID());
            this.se = se;
        }

        /** Create a new cell.  If creating along version:list, we
         * activate the next or previous space version.  Otherwise we
         * redelegate to the superclass. */
        public ZZCell N(String dim, int dir, ZZObs o, long flags) {
            if (!dim.equals(VersionPart.this.id + ":" + activeDimensionName))
                return super.N(dim, dir, o, flags);
            loadVersion(se.stampo.intValue() + dir);
            return VersionPart.this.space.d(VersionPart.this.id + ":" + activeDimensionName).s(this, dir, o);
        }

        public void setText(String text) {
            throw new ZZError("readonly cell");
        }
        public void setSpan(Span text) {
            throw new ZZError("readonly cell");
        }

        public void delete() {
            VersionPart.this.space.deleteCell(this);
            se.space = null;
        }

    }

    /** Dimension that lists all active versions.  This dimension has
     * funky semantics.  First of all, there is always at most one
     * nontrivial rank on this dimension.  That rank's headcell is
     * always the space homecell.  The other cells are ListCells (ID
     * of type "version:NUMBER") and they are listed on that rank in
     * ascending numeric order on their content.  The content always
     * equals the NUMBER in the cell ID.  If you connect some cell
     * containing a number to this rank, the number is interpreted as
     * a version and that version is activated.  This means that
     * instead of the cell appearing on that rank, a new cell appears
     * in the right place and time.  Disconnecting from this rank
     * deactivates that version.  One cannot connect to homecell's
     * negative side or to other ranks on this dimension.  */
    protected class ActiveDimension extends ZZDimension {
        public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs obs) {
            ZZCellHandle rv;
            // inx will first denote the index of c in stamps; steps
            // will then be added to it to make it point to the index
            // in staps of the return value (or off-bounds if we want
            // to return null)
            int inx = Integer.MIN_VALUE;
            try {
                synchronized (spaces) {
                    if (c.equals(VersionPart.this.space.getHomeCell())) {
                        // home cell's posward neighbour is stamps[0]
                        inx = -1;
                    } else {
                        ID id = (ID)parseID(c.getID());
                        if (id.id != null) {
                            // not a home cell or a ListCell, so we
                            // want to end up out of bounds
                            inx = -2-steps;
                        } else {
                            // this is a ListCell, look up the index in stamps
                            SpaceElement se = ((ListCell)c).se;
                            inx = se.inx;
                        }
                    }
                    // now inx will index the return value
                    inx += steps;
                    if (inx == -1) {
                        // stamps[0]'s negward neighbour is the home cell
                        rv = (ZZCellHandle)VersionPart.this.space.getHomeCell();
                    } else if (inx == stamps.size()) {
                        // We're technically out of bounds, but this
                        // is sooo useful!  We'll have the last
                        // committed version always as the tailcell.
                        // Its presence as the tailcell does not
                        // necessarily mean it's activated, however;
                        // we want it to remain unactivated after a
                        // new commit if it has not been examined, but
                        // also we want it to magically be active
                        // after a commit otherwise.
                        Integer stampo = new Integer(((ZZCacheDimSpace)VersionPart.this.space).timestamp);
                        // Careful here!  If the tailcell version is
                        // active, it is stamps[stamps.size()-1] and
                        // thus we must not give it a posward
                        // neighbour that it is itself (that would
                        // break rank invariants)
                        if (spaces.containsKey(stampo)) 
                            rv = null;
                        else {
                            // Okay, it's unactivated.
                            rv = getSpaceElement(stampo).cell;
                        }                           
                    } else if (inx < -1 || inx > stamps.size()) {
                        // off-bounds means no cigar
                        rv = null;
                    } else {
                        // within bounds, so this is straightforward
                        Integer stampo = (Integer)stamps.elementAt(inx);
                        rv = getSpaceElement(stampo).cell;
                    }
                }
            } catch (SyntaxError e) {
                rv = null;
            }
            p("ActiveDimension.s(" + c + ", " + steps + ") (inx == " + inx + ", stamps.size() == " + stamps.size() + ") ==> " + rv);
            dump_stamps();

            if (obs != null) { ot.addObs(spaces, obs); }

            return rv;
        }

        /** Is this cell a ListCell? */
        private boolean isOurs(ZZCellHandle c) {
            return c.equals(VersionPart.this.space.getHomeCell())
                || (c.part == VersionPart.this
                    && ((ID)c.parsedID).id == null);
        }

        /** "Connect" two cells on this dimension.  This is rather
         * magical, see the class documentation. */
        public void connect(ZZCellHandle c, ZZCellHandle d) {
            pa("c = " + c + ", d = " + d);
            if (isOurs(c) && isOurs(d)) return;  // already on the rank
            if (isOurs(c)) loadVersion(Integer.parseInt(d.getText()));
            else if (isOurs(d)) loadVersion(Integer.parseInt(c.getText()));
            else throw new ZZError("cannot connect those cells");
            dump_stamps();
        }

        /** "Disconnect" two cells on this dimension.  This is rather
         * magical, see the class documentation. */
        public void disconnect(ZZCellHandle c, int dir) {
            synchronized (spaces) {
                if (c.part != VersionPart.this || ((ID)c.parsedID).id != null) return;
                Integer stampo = new Integer(((ID)c.parsedID).stamp);
                if (!spaces.containsKey(stampo)) return;
                SpaceElement se = (SpaceElement)spaces.get(stampo);
                spaces.remove(stampo);
                int n = stamps.size();
                stamps.removeElementAt(se.inx);
                for (int i = se.inx; i < stamps.size(); i++) {
                    Integer stampone = (Integer)stamps.elementAt(i);
                    SpaceElement sese = getActiveSpaceElement(stampone);
                    sese.inx = i;
                }
                dump_stamps();
            }
        }

    }

    protected class ListDimension extends ZZRODimension {
        public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs obs) {
            ZZCellHandle home = (ZZCellHandle)VersionPart.this.space.getHomeCell();
            int i;
            if (c.equals(home)) {
                i = 0;
            } else {
                if (c.part != VersionPart.this) return null;
                ID id = (ID)c.parsedID;
                if (id.id != null) return null;
                i = id.stamp;
            }
            i += steps;
            if (!(0 <= i && i <= ((ZZCacheDimSpace)VersionPart.this.space).timestamp)) return null;
            if (obs != null) ot.addObs(dimensions, obs);
            if (i == 0) return home;
            return getSpaceElement(i).cell;
        }
    }

    /** This dimension connects ListCells to their spaces' homecells.*/
    protected class HomeDimension extends ZZRODimension {
        // note, this dimension never triggers anything as it is truly RO
        public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
            synchronized (spaces) {
                if (c.part != VersionPart.this) return null;
                ID id = (ID)c.parsedID;
                if (id.id != null) {
                    if (!getSpace(id.stamp).getHomeCell().getID().equals(id.id)) {
                        return null;
                    }
                    if (steps != -1) return null;
                    return getActiveSpaceElement(id.stamp).cell;
                }
                if (steps != 1) return null;
                return ((ListCell)c).
                    se.getWrapperCell((ZZCellHandle)getSpace(id.stamp).getHomeCell());
            }
        }
    }

    /** This dimension wraps around dimensions in old space versions. */
    protected class VersionDimension extends ZZRODimension {
        SpaceElement se;
        ZZDimension old;
        public VersionDimension(SpaceElement se, String dim) {
            this.se = se;
            Object o = se.space.d(dim);
            if (o == null) throw new ZZError("no such dimension");
            old = (ZZDimension)o;
        }
        public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
            p("VersionDimension(" + se.stampo + ").s, c = " + c + ", c.part = " + c.part + ", c.getClass() = " + c.getClass());
            if (c.part != VersionPart.this) return null;
            ID id = (ID)c.parsedID;
            if (id.id == null) return null;
            if (!(c instanceof VersionCell)) return null;
            ZZCellHandle oldc = ((VersionCell)c).old;
            if (id.stamp != se.stampo.intValue()) {
                se.activate();
                oldc = (ZZCellHandle)se.space.getCellByID(c.getID());
            }
            ZZCellHandle oldrc = old.s(oldc, steps, o);
            p("oldc = " + oldc + ", oldrc = " + oldrc);
            if (oldrc == null) return null;
            return se.getWrapperCell(oldrc);
        }
    }

    protected class AllVersionDimension extends ZZRODimension {
	String name;
	public AllVersionDimension(String name) {
	    this.name = name;
	}
	public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs o) {
	    if(c.part != VersionPart.this) 
		return (ZZCellHandle)c.s(name, steps, o);
	    ID id = (ID)c.parsedID;
	    if(id == null)
		return (ZZCellHandle)c.s(name, steps, o);
	    return getActiveSpaceElement(id.stamp)
	    		.getWrapperDimension(name)
			.s(c, steps, o);
	}
    }

    protected class ActiveCellVersionsDimension extends ZZRODimension {
        public ZZCellHandle s(ZZCellHandle c, int steps, ZZObs obs) {
            synchronized (spaces) {
                String cid;
                int inx;
                if (c.part == VersionPart.this) {
                    ID id = (ID)c.parsedID;
                    if (id.id == null) return null;
                    SpaceElement se_cur = getActiveSpaceElement(id.stamp);
                    inx = se_cur.inx;
                    cid = id.id;
                } else if (c.part == null) {
                    inx = stamps.size();
                    cid = c.getID();
                } else {
                    return null;
                }
                inx -= steps;
                if (inx == stamps.size())
                    return (ZZCellHandle)VersionPart.this.space.getCellByID(cid);
                if (inx < 0 || inx > stamps.size())
                    return null;
                Integer stampo = (Integer)stamps.elementAt(inx);
                SpaceElement se_new = getActiveSpaceElement(stampo);
                if (obs != null) ot.addObs(spaces, obs);
                return se_new.getWrapperCell(cid);
            }
        }
    }

    public Object parseIDPart(String s) {
        int idx = s.indexOf(".");
        if (idx == s.length() -1) {
            throw new SyntaxError("syntax error in cell/dimension ID: missing period");
        }
        String id;
        if (idx == -1) {
            id = null;
            idx = s.length();
        } else {
            id = s.substring(idx+1);
        }
        int stamp;
        try {
            stamp = Integer.parseInt(s.substring(0, idx));
        } catch (NumberFormatException e) {
            throw new SyntaxError("syntax error in cell/dimension ID: " + e.getMessage());
        }
        return new ID(id, stamp);
    }
    
    public String generateIDPart(Object parsed) {
        String rv = ((ID)parsed).toString();
        p("VersionPart.generateIDPart returns " + rv);
        return rv;
    }

    public String getText(ZZCellHandle c) {
        ID id = (ID)parseID(c.getID());
        if (id.id != null) {
            return getSpace(id.stamp).getText(id.id);
        }
        return ""+id.stamp;
    }

    public void postCommitHook() { ot.chg(dimensions); }

}

