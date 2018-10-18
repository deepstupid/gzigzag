/*   
AwtUtil.java
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
 * Written by Kimmo Wideroos
 */

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** Some general stuff
 */

public class AwtUtil {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    public static String d_to="d.map-1"; 
    public static String d_from="d.map-2";
    public static String d_categories="d.categories";
    public static String d_notes="d.notes";
    public static String d_awtool="d.awtool";

    public static String d_clone="d.clone";

    public static ZZCell addDims(ZZCell defaultDimsBegin) {
        Hashtable awtDims = new Hashtable();

        awtDims.put(d_to, d_to);
        awtDims.put(d_from, d_from);
        awtDims.put(d_categories, d_categories);
        awtDims.put(d_notes, d_notes);
        awtDims.put(d_awtool, d_awtool);

        String dim;

        for(ZZCell c = defaultDimsBegin; c != null; c = c.s("d.2") ) {
            dim = c.getText();
            if(awtDims.containsKey(dim)) 
                awtDims.remove(dim);    
            if(c.equals(defaultDimsBegin.s("d.2", -1))) break;
        }

        for(Enumeration e = awtDims.keys(); e.hasMoreElements(); ) {
            dim = (String)e.nextElement();
            defaultDimsBegin.N("d.2", -1).setText(dim);            
        }
        return defaultDimsBegin;
    }

    /** return awt system list roocell
     */
    public static ZZCell getAwtSysList(ZZCell vc) {
        ZZCell awtc = ZZDefaultSpace.findInheritableParam(vc, "AwtSystemList");
        if(awtc == null) return null;
        return ZZCursorReal.get(awtc);
    }

    /** return active metrics 
    */
    public static AwtMetrics getAwtMetrics(ZZCell awtsyslist) {
        ZZCell metr = getCursoredCell(Awtool.c_metrics, awtsyslist);
        if(metr==null) {
            metr = awtsyslist.findText(d_awtool, 1, "Metrics");
            if(metr!=null) metr = metr.s("d.1");
        }
        if(metr == null) throw new ZZError("Awtool: Couldn't initialize metrics!!");
        AwtMetrics M = (AwtMetrics)readZObClone(metr);
        M.setCell(metr);
        return M;
    }

    public static ZZCell getCursoredCell(AwtCursor acur, ZZCell viewcellOrAwtsyslist) {
        String cursorName = acur.color.toString();
        ZZCell awtsyslist, cur=null, cursors;
        ZZSpace sp;

        // scrutinize 'viewcellOrAwtsyslist'
        if(viewcellOrAwtsyslist.getRankLength(d_awtool)>1) {
            if(acur.shared) throw new ZZError("AwtUtil: cursor can't be shared!!");
            awtsyslist = viewcellOrAwtsyslist;
            sp = null;
        } else {
            sp = viewcellOrAwtsyslist.getSpace();
            awtsyslist = getAwtSysList(viewcellOrAwtsyslist);
        }

        // shared cursors belongs to all awtool views
        if(!acur.shared) {
            cursors = awtsyslist.findText(d_awtool, 1, "Cursors").s("d.1");
            cur = cursors.findText("d.2", 1, cursorName);
        } else { 
            cursors = ZZDefaultSpace.findOnSystemlist(sp, "AwtSharedCursors", true).getOrNewCell("d.1");
            cur = cursors.findText("d.2", 1, cursorName);
        }
        if(cur==null) return null;
        return ZZCursorReal.get(cur);
    }

    public static void setCursor(AwtCursor acur, ZZCell c, ZZCell viewcellOrAwtsyslist) {
        String cursorName = acur.color.toString();
        ZZCell awtsyslist, cur=null, cursors=null;
        ZZSpace sp;

        // scrutinize 'viewcellOrAwtsyslist'
        if(viewcellOrAwtsyslist.getRankLength(d_awtool)>1) {
            if(acur.shared) throw new ZZError("AwtUtil: cursor can't be shared!!");
            awtsyslist = viewcellOrAwtsyslist;
            sp = null;
        } else {
            sp = viewcellOrAwtsyslist.getSpace();
            awtsyslist = getAwtSysList(viewcellOrAwtsyslist);
        }

        // shared cursors belongs to all awtool views
        if(!acur.shared) {
            cursors = awtsyslist.findText(d_awtool, 1, "Cursors").s("d.1");
            cur = cursors.findText("d.2", 1, cursorName);
        } else { 
            cursors = ZZDefaultSpace.findOnSystemlist(sp, "AwtSharedCursors", true).getOrNewCell("d.1");
            cur = cursors.findText("d.2", 1, cursorName);
            /*
            ZZCell win, vcShared;
            ZZCell[] wins;
            ZZSpace sp = vc.getSpace();
            win = ZZDefaultSpace.findOnSystemlist(sp, "Windows", false);
	    win = win.s("d.1");
	    wins = AwtUtil.findAll(win, "d.2", 1, "awtool");
            if(wins==null) throw new ZZError("Awtool: No awtool window found!!");
            for(int i=0; i<wins.length; i++) {
                vcShared = wins[i].s("d.1", 2);
                cursors = vcShared.findText("d.2", 1, "SharedCursors").s("d.1");
                cur = cursors.findText("d.2", 1, cursorName);
                if(cur!=null) break;
            }
            */
        }
        if(cur == null) {
            cur = cursors.N("d.2"); 
            cur.setText(cursorName);
            ZZCursorReal.set(cur, c);
            ZZCursorReal.setColor(cur, acur.color);
            return;
        }
        ZZCursorReal.set(cur, c);
    }

    public static ZZCell[] findAll(ZZCell start, String dim, int dir, String id) {
        ZZCell c;
	Vector found = new Vector();
        if(start.getText().equals(id)) found.addElement(start); 
	for(c = start.s(dim, dir); (c!=null) && (c!=start); c = c.s(dim, dir) ) {
            if(c.getText().equals(id)) found.addElement(c);
	}
        int i = found.size();
        ZZCell[] fc = new ZZCell[i];
        for(i-=1; i>=0; i--) {
            fc[i] = (ZZCell)found.elementAt(i);
        }
        return fc;
    }

    public interface ObjectComparator {
        int compare(Object o1, Object o2);
    }

    public static void quickSort(Vector a, int lo0, int hi0, ObjectComparator c) {
        int lo = lo0;
        int hi = hi0;

        if (lo >= hi)
            return;

        Object mid = a.elementAt((lo + hi) / 2);
        while (lo < hi) {
            while (lo<hi && c.compare(a.elementAt(lo), mid) < 0)
                lo++;
            while (lo<hi && c.compare(a.elementAt(hi), mid) > 0)
                hi--;
            if (lo < hi) {
                Object T = a.elementAt(lo);
                a.setElementAt(a.elementAt(hi), lo);
                a.setElementAt(T, hi);
                lo++;
                hi--;
            }
        }
        if (hi < lo) {
            int T = hi;
            hi = lo;
            lo = T;
        }
        quickSort(a, lo0, lo, c);
        quickSort(a, lo == lo0 ? lo+1 : lo, hi0, c);
    }

    /** Construct and return a ZOb of the given type.
     */
    public static ZOb newModuleZOb(String id) {
    // XXX Figure out if we can cache something.
    // XXX Should ZOb be able to reset? Easier or less easy than
    // recreate? At least ZObs should cache...
        try {
            Class z = Class.forName("org.gzigzag.module."+id);
            return (ZOb)Class.forName("org.gzigzag.module."+id).newInstance();
        } catch(Exception e) {
            ZZLogger.exc(e);
            return null;
        }
    }
    
    /** Construct a ZOb corresponding to a cell and read the params.
     * DOES NOT Go to the rootclone, takes the next cell on d.1 as the ZOb's name,
     * and takes the NEXT cell on d.1 as the head of the list from which to
     * read the structparams.
     */
    /*  Differs from readZOb by considering clones themselves as zobs
     */
    static public ZOb readZObClone(ZZCell start) {
	//ZZCell c = start.h("d.clone", -1).s("d.1", 1);
        
        if(start.s("d.1") == null) start = start.h("d.clone", -1);
	ZZCell c = start.s("d.1", 1);
	if(c == null)
	    throw new ZZError("Error reading ZOb from cell "+start.getText());
	ZOb z = newModuleZOb(c.getText());
	z.readParams(c.s("d.1", 1));
	return z;
    }

    /** return category cell of the note c 
     */
    static public ZZCell getNoteCellCategory(ZZCell c) {
        ZZCell qwerty = c.s(d_from, -1);
        if(qwerty == null) return null;
        if(qwerty.equals(qwerty.h(d_to, -1))) return null;
        qwerty = qwerty.h(d_to, -1);
        if(qwerty.getRankLength(d_categories) == 1) return null;
        return qwerty;
    }

    static public AwtNote createNewNote(ZZCell c, double[] ncenter, 
                                        double[] ndim) { 
        AwtNote newNote = new AwtNote();
        newNote.setCell(c);
        newNote.setCoord(ncenter[0], ncenter[1]);
        newNote.setDimension(ndim[0], ndim[1]);
        newNote.cellRepr();
        return newNote;
    }

    public void deleteNote(ZZCell nc) {
        // should delete ZOb structures ... in future
        // ZObCellRepr will have 'deleteZOb' method. 
        ZZCell hc = nc.h(d_clone);
        nc.delete();
        if(hc.getRankLength(d_clone)==1) {
            hc.delete();
        }
    }

    static public void createNewLink(ZZCell from, ZZCell to) {
        ZZCell newLinkCell = from.h(AwtUtil.d_to, 1).N(AwtUtil.d_to, 1);
        to.h(AwtUtil.d_from, -1).connect(AwtUtil.d_from, -1, newLinkCell);
        AwtLink al = new AwtLink();
        al.setCell(newLinkCell);
        al.cellRepr();
    }

    static public void renderGrid(FlobSet into, AwtMetrics M, double x1, double
                             y1, double x2, double y2, Color color, int density) 
        {
        double[] coord = new double[2], cstart = new double[2], cinc = new double[2],
                 d = new double[2], dh = new double[2];
        Point p1, p2;
        int i, j, a, b;
        cstart[0] = (x1 < x2) ? x1 : x2;
        cstart[1] = (y1 < y2) ? y1 : x2;
        cinc[0] = Math.abs((x2-x1) / ((double)density + 1.0));
        cinc[1] = Math.abs((y2-y1) / ((double)density + 1.0));
        d[0] = cinc[0] / 5.0;
        d[1] = cinc[1] / 5.0;
        dh[0] = d[0]/2.0;
        dh[1] = d[1]/2.0;

        LineDecor.Builder ldb = new LineDecor.Builder(into, color);

        for(a=0; a<2; a++) {
            b = (a==0) ? 1 : 0;
            coord[a] = cstart[a];
            for(i=0; i<=density+1; i++) {
                coord[b] = cstart[b]+dh[b]/2.0;
                ldb.startl((density+1)*20, 2000);
                for(j=1; j<=(density+1)*5; j++) {
                    p1 = M.mapToRealView(coord);
                    coord[b] += dh[b];
                    p2 = M.mapToRealView(coord);
                    coord[b] += dh[b];
                    ldb.l(p1.x, p1.y, p2.x, p2.y);
                }
                ldb.endl();
                coord[a] += cinc[a];
            }
        }
    }
}






