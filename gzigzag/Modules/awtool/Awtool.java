/* DO NOT EDIT THIS FILE. THIS FILE WAS GENERATED FROM ../Modules/awtool/Awtool.zob,
 * EDIT THAT FILE INSTEAD!
 * All changes to this file will be lost.
 */
/*   
Awtool.zob
 *    
 *    Copyright (c) 2000-2001 Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2000-2001 Kimmo Wideroos
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
 * Written by Kimmo Wideroos (inspired by Benjamin Fallenstein's notemap module)
 */

/** Awt = A(ssociative) writing tool -demo
*/

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

public abstract class Awtool implements FlobView, FlobSet.DragCursor, ZOb {
public static final String rcsid = "";
    public static boolean dbg = true;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    
    
	/** View dimension (not in use now...) 
 * <p>Default value: <PRE> 600;</PRE>. 
 
 * @structparam 1 
 */ 

 public 
	int viewWidth
	    = 600;
        int viewHeight
            = 600;
        int viewX
            = 100;
        int viewY
            = 100;
    

    /* AUTOGENERATED! */
    static final private int fullmask = 1;

    /* AUTOGENERATED! */
    public String readParams(ZZCell start) {
	int m = 0;
	try {
	    if(start != null)
		m = readParams(start, 0);
	} catch(Throwable t) {
	    ZZLogger.exc(t);
	} finally {
	    
	}
	if((m & fullmask) != fullmask) {
	    // not all parameters present - no problem right now.
	}
	return "";
    }

    /* AUTOGENERATED! */
    private int readParams(ZZCell start, int mask) {
	ZZCell n = start;
	while(n != null) {
	    String s = n.getText();
	    // Tests autogenerated from members.
	    
	    if(s.equals("viewWidth")) {
		mask |= 1;
		try {
		    ZZCell c = n.s("d.1"); s = c.getText(); viewWidth = Integer.parseInt(s);  
		} catch(Exception e) {
		    ZZLogger.exc(e);
		}
	    } else


	    { } // grab that last "else"
	    ZZCell h = n.h("d.3");
	    if(h != null && h != n) {
		// recurse
		mask |= readParams(h, mask);
	    }
	    n = n.s("d.2");
	}
	return mask;
    }





    static boolean NileOn = false;
    static AwtNote dragNote;
    static Point dragOffs = new Point(0, 0);
    static Point dragCoord = new Point(0, 0);

    static boolean resizeWidth = false, resizeHeight = false;
 
    static boolean creatingNote = false;

    static long focusChangedLastTime = -1; // millisecs

    static AwtCursor c_cursor1 = new AwtCursor(Color.red, true);
    static AwtCursor c_cursor2 = new AwtCursor(Color.gray, true);
    static AwtCursor c_activeCategory = new AwtCursor(Color.orange, false);
    static AwtCursor c_metrics = new AwtCursor(Color.white, false);

    public boolean accept(Object o) {
	if(!(o instanceof ZZCell)) return false;
	return true;
    }

    static public Hashtable categ;
    static public Hashtable notes;
    static public Hashtable validNotes;

    static class NotemapBM extends BooleanMesh {
        public NotemapBM(ZZCell bStructure) { super(bStructure); }
        public boolean testBelonging(ZZCell nhc, ZZCell category) {
            for(ZZCell l=category.h(AwtUtil.d_clone).s(AwtUtil.d_to); l != null; l=l.s(AwtUtil.d_to) )
                if(l.h(AwtUtil.d_from,1).h(AwtUtil.d_clone).equals(nhc)) return true;
            return false;
        }
    }



    /* categories are a kind of layers containing notes (i.e. (text) elements).
     * this class provides with logical operations for categories (AND, OR). 
     * the resulting categories should be able to store permanently in zzspace. 
     * heavily under construction ...
     */
    public class Categories extends ZObCellRepr {
        private Hashtable AllNoteHeadCells = null;
        private Hashtable AllNotes = null; 
        private Hashtable AllValidNotes = null;
        private Hashtable ValidNotes = null;
	private Hashtable K = new Hashtable();
        private NotemapBM bm; 
        private String categoryName = "SCRATCH";
        
	public boolean isCategory(ZZCell c) {
	    return (c.h(AwtUtil.d_clone).getRankLength(AwtUtil.d_categories)>1);
	}

        public Categories(ZZCell bStructure) {
            setCell(bStructure); 
            _findCategories(bStructure); 
            bm = new NotemapBM(bStructure);

            AllNotes = new Hashtable();
            AllNoteHeadCells = new Hashtable();
            AllValidNotes = new Hashtable();
            ValidNotes = new Hashtable();

            ZZCell note, chc, nhc;
	    for(Enumeration e = K.keys(); e.hasMoreElements(); ) {
                // chc = category headcell
		chc = ((ZZCell)e.nextElement());
		for(ZZCell linkc = chc.s(AwtUtil.d_to); linkc != null; 
		    linkc = linkc.s(AwtUtil.d_to) ) 
		    {
			note = linkc.h(AwtUtil.d_from, 1);
                        nhc = note.h(AwtUtil.d_clone);
                        AllNotes.put(note, nhc);
			if(!AllNoteHeadCells.containsKey(nhc))
                            // AllNoteHeadCells must keep track of all note headcells
                            AllNoteHeadCells.put(nhc, note);
                        if(AllValidNotes.containsKey(nhc))
                            // AllValidNotes store valid headcells AND clones 
                            // all together
                            AllValidNotes.put(note, nhc);
                        else if(bm.evaluate(nhc)) AllValidNotes.put(note, nhc);
                        if(AllValidNotes.containsKey(note)) {
                            // If there exist 2 or more valid note clones, ValidNotes 
                            // store note headcell (otherwise note clone).
                            if(ValidNotes.containsKey(nhc)) ValidNotes.put(nhc, nhc);
                            else ValidNotes.put(nhc, note);
                        }
		    }
	    }
	}

	private void _findCategories(ZZCell bStructure) {
	    if(isCategory(bStructure)) {
		K.put(bStructure.h(AwtUtil.d_clone), bStructure);
		return;
	    } else 
		{
		    _findCategories(bStructure.s("d.1"));
		    _findCategories(bStructure.s("d.2"));
		}
	}

        public ZZCell[] getNoteCells() {
            ZZCell[] n = new ZZCell[AllNotes.size()];
            Enumeration e = AllNotes.elements();
            int i = 0;
            while (e.hasMoreElements()) { 
                n[i] = (ZZCell)e.nextElement(); i++;
            }
            return n;
        }
        public AwtNote[] getNotes() {
            AwtNote[] n = new AwtNote[AllNotes.size()];
            Enumeration e = AllNotes.elements();
            ZZCell ncell;
            int i = 0;
            while (e.hasMoreElements()) {
                ncell = (ZZCell)e.nextElement();
                n[i] = (AwtNote)AwtUtil.readZObClone(ncell);
                n[i].setCell(ncell);
                i++;
            }
            return n;
        }

        public ZZCell[] getAllValidNoteCells() {
            ZZCell[] ln = new ZZCell[AllValidNotes.size()];
            int i = 0;
            for(Enumeration e=AllValidNotes.keys(); e.hasMoreElements();) {
                ln[i] = (ZZCell)e.nextElement(); 
                i++; 
            }
            return ln;
        }

        public AwtNote[] getAllValidNotes() {
            AwtNote[] ln = new AwtNote[AllValidNotes.size()];
            ZZCell ncell;
            int i = 0;
            for(Enumeration e=AllValidNotes.keys(); e.hasMoreElements();) {
                ncell = (ZZCell)e.nextElement();
                ln[i] = (AwtNote)AwtUtil.readZObClone(ncell);
                ln[i].setCell(ncell);
                i++; 
            }
            return ln;
        }

        public ZZCell[] getValidNoteCells() {
            ZZCell[] ln = new ZZCell[ValidNotes.size()];
            int i = 0;
            for(Enumeration e=ValidNotes.elements(); e.hasMoreElements(); ) {
                ln[i] = (ZZCell)e.nextElement(); 
                i++;
            }
            return ln;
        }

        public AwtNote[] getValidNotes() {
            AwtNote[] ln = new AwtNote[ValidNotes.size()];
            ZZCell ncell;
            int i = 0;
            for(Enumeration e=ValidNotes.elements(); e.hasMoreElements(); ) {
                ncell = (ZZCell)e.nextElement();
                ln[i] = (AwtNote)AwtUtil.readZObClone(ncell);
                ln[i].setCell(ncell);
                i++;
            }
            return ln;
        }

        public ZZCell[] getK() {
            ZZCell[] cat = new ZZCell[K.size()];
            Enumeration e = K.elements();
            int i = 0;
            while (e.hasMoreElements()) 
                cat[i] = (ZZCell)e.nextElement(); i++;
            return cat;
        }

        public ZZCell[] getAllValidLinkCells(boolean internal, boolean external) { 
            return _getvalidlinks(internal, external, AllValidNotes.keys());
        }

        public AwtLink[] getAllValidLinks(boolean internal, boolean external)
        { 
            ZZCell[] linkcells = _getvalidlinks(internal, external, AllValidNotes.keys());
            AwtLink[] links = new AwtLink[linkcells.length];
            for(int i = 0; i<linkcells.length; i++ ) {
                links[i] = (AwtLink)AwtUtil.readZObClone(linkcells[i]);
                links[i].setCell(linkcells[i]);
            }
            return links;
        }

        public ZZCell[] getValidLinkCells(boolean internal, boolean external) { 
            return _getvalidlinks(internal, external, ValidNotes.elements());
        }

        public AwtLink[] getValidLinks(boolean internal, boolean external) { 
            ZZCell[] linkcells = _getvalidlinks(internal, external, AllValidNotes.keys());
            AwtLink[] links = new AwtLink[linkcells.length];
            for(int i = 0; i<linkcells.length; i++ ) {
                links[i] = (AwtLink)AwtUtil.readZObClone(linkcells[i]);
                links[i].setCell(linkcells[i]);
            }
            return links;
        }
       
        // Get the links in this category
        private ZZCell[] _getvalidlinks(boolean internal, boolean
        external, Enumeration noteEnum) {
            Hashtable links = new Hashtable();
            ZZCell nhc, note, trgnote;
            for(Enumeration e=noteEnum; e.hasMoreElements();) {
                note = (ZZCell)e.nextElement();
                nhc = note.h(AwtUtil.d_clone);
                if(external)
                    for(ZZCell el=nhc.s(AwtUtil.d_to); el != null; el=el.s(AwtUtil.d_to)) {
                        // note has global relations
                        trgnote = el.h(AwtUtil.d_from, 1);
                        if(AllNoteHeadCells.containsKey(trgnote)) links.put(el, el);
                    }
                if(internal && !(external && note.equals(nhc)))
                    for(ZZCell il=note.s(AwtUtil.d_to); il != null; il=il.s(AwtUtil.d_to)) {
                        // note has categorywide relations
                        trgnote = il.h(AwtUtil.d_from, 1);
                        if(AllValidNotes.containsKey(trgnote)) links.put(il, il);
                    }
            }
            int s = links.size(), i=0;
            ZZCell[] res = new ZZCell[s];
            for(Enumeration e=links.elements(); e.hasMoreElements(); ) {
                res[i] = (ZZCell)e.nextElement();
                i++;
            }
            return res;
        }

        public String getZObName() { 
            return "Catecory/Layer";
        }

        /* store category back in cellstructure */
        public String cellRepr(ZZCell start, boolean clone) {
            ZZCell c, retCell, catCell = null;
            AwtLink catlink = new AwtLink();

            catCell.setText(getZObName());
            catCell = catCell.getOrNewCell(AwtUtil.d_to);
            AwtNote note;
            AwtNote[] validNotes = getValidNotes();
            for(int i=validNotes.length; i>=0; i--) {
                note = validNotes[i];
                catlink.setX(note.getX()); catlink.setY(note.getY()); 
                catlink.cellRepr(catCell, false);
                // note's centralcell is a clone of its (ZOb's) original
                // central cell; that's why '...im2), true);' 
                note.cellRepr(catCell.getOrNewCell(AwtUtil.d_from), true);
                catCell = catCell.getOrNewCell(AwtUtil.d_to);
            }
            return "";
            }
        }

    /** return note cell, if possible 
     */
    static ZZCell getNoteCellOrCursor(Object ob) {
        ZZCell c = null;
        if(ob instanceof ZZCursor) c = ((ZZCursor)ob).get(); 
        else if(ob instanceof ZZCell) c = (ZZCell)ob;
        else return null;
        ZZCell link = c.s(AwtUtil.d_from, -1);
        if(link == null) return null;
        if(link.s(AwtUtil.d_to, -1) == null) return null;
        return c;
    }

    static public void nile_action(String id, ZZCell vc1, ZZCell vc2, String key,
                                   ZZCursor cur) {

        boolean split = false;

        ZZCell viewCell = vc1;
        ZZCell otherCell = vc2;
	
        ZZCell transFrom = otherCell, transTo = viewCell;
        ZZCursor tfcur = Nile2Ops.curs(transFrom);
        ZZCursor tfendcur = Nile2Ops.endcurs(transFrom);
        
        if(tfendcur.get() != null) {
            // if othercell does not have content, use viewcell as source 
            // when tunneling and transcluding
            Nile2Iter tfiter = new Nile2Iter(tfcur);
            Nile2Iter tfenditer = new Nile2Iter(tfendcur);
            if(tfenditer.isOrdered(tfiter)) {
                transFrom = viewCell;
                transTo = otherCell;
            }
        }

        p("Nile action: id: '"+id+"' key: '"+key);
        
        StringTokenizer st = new StringTokenizer(id);
        String[] toks = new String[st.countTokens()];
        for(int i=0; i<toks.length; i++)
            toks[i] = st.nextToken();
        
        int dir = 0;
        Nile2Unit unit = null;
        if(toks.length > 1) {
            if(toks[1].equals("+")) dir = 1;
            else if(toks[1].equals("-")) dir = -1;
            
            if(toks.length > 2) {
                if(toks[2].equals("CHAR"))
                    unit = new Nile2Unit.Char();
                else if(toks[2].equals("WORD"))
                    unit = new Nile2Unit.Word();
                else if(toks[2].equals("SENT"))
                    unit = new Nile2Unit.Sentence();
                else if(toks[2].equals("PARA"))
                    unit = new Nile2Unit.Paragraph();
            }
        }
        
        id = toks[0];
        
        if(id.equals("Cursor")) {
            p("Cursor: "+dir);
            Nile2Ops.moveCursor(unit, viewCell, dir);
            ZZUpdateManager.setFast(null);
        }
        else if(id.equals("AdjLeft")) {
            Nile2Ops.adjust(unit, viewCell, -1, dir);
            ZZUpdateManager.setFast(null);
        }
        else if(id.equals("AdjRight")) {
            Nile2Ops.adjust(unit, viewCell, 1, dir);
            ZZUpdateManager.setFast(null);
        }
        else if(id.equals("Hop")) {
            Nile2Ops.hop(unit, viewCell, dir);
        }
	else if(id.equals("Mode")) {
            Nile2Ops.setCursor(unit, viewCell, null);
            if(split)
                Nile2Ops.setCursor(unit, otherCell, null);
        } 
	else if(id.equals("Insert")) {
            Nile2Ops.insert(viewCell, key);
            
            // Set the selection of other half to char mode, too
            if(split)
                Nile2Ops.setCursor(new Nile2Unit.Char(), otherCell, null);
        } 
	else if(id.equals("BackSpace")) {
            Nile2Ops.backspace(unit, viewCell);
        }
        else if(id.equals("Delete")) {
            Nile2Ops.del(unit, viewCell);
        }
        else if(id.equals("BreakParagraph")) {
            Nile2Ops.breakParagraph(unit, viewCell);
        }
        else if(id.equals("Tunnel")) {
            if(otherCell != null) {
                //Nile2Ops.tunnel(unit, otherCell, viewCell, dir, false);
                Nile2Ops.tunnel(unit, transFrom, transTo, dir, false);
            }
        }
        else if(id.equals("Transcopy")) {
            if(otherCell != null) {
                //Nile2Ops.tunnel(unit, otherCell, viewCell, dir, true);
                Nile2Ops.tunnel(unit, transFrom, transTo, dir, true);
            }
        }
        else if(id.equals("PUICopy")) {
            ZZCell c = ZZCursorReal.get(viewCell);
            String s = Nile2Ops.stringify(c);
            ZZUtil.puiCopy(s);
        }
        /*
          else if(id.equals("TraverseTranscopies")) {
          Nile2Ops.traverseTranscopies(viewCell, dir);
          }
        */
        
        //Object ob = null;
        //if(pt != null)
        //    ob = xi.getObjectAt(pt.x, pt.y);
        
        if(id.equals("MouseSetCursor") && cur!=null) {
            p("Setting cursor from mouse! Unit: "+unit);
            if(cur.get().h("d.nile").equals(viewCell)) {
                Nile2Ops.setCursor(unit, viewCell, cur);
            } else
            if(cur.get().h("d.nile").equals(otherCell)) {
                Nile2Ops.setCursor(unit, otherCell, cur);
            }
        }
    }
    

    abstract public void raster(FlobSet into, FlobFactory fact,
	    ZZCell view, String[] dims, ZZCell accursed);

    static public ZZModule module = new ZZModule() {
	public void action(String id,
		ZZCell code, 
		ZZCell target,
		ZZView view, ZZView cview, String key, Point pt, ZZScene xi) {

            ZZUpdateManager.setFast(view);
	    Object ob = null;
            AwtNote note = null;
            AwtMetrics M;
            ZZCell nc;
            Point noteRVCoord = null;
            double[] noteVVCoord = null;
            double deltaX, deltaY;
            
	    if(pt != null && xi != null) ob = xi.getObjectAt(pt.x, pt.y);

            nc = getNoteCellOrCursor(ob);

            if(nc != null) { 
                note = (AwtNote)AwtUtil.readZObClone(nc);
                note.setCell(nc);
            }

            boolean forceOrigoChange = false;
            ZZCell AwtSystemList;
            ZZCell vc = view.getViewcell();
            
            pa("NOTECELL: "+nc);

	    pa("Note action! '"+id+"' key: "+key);

            // is nile mode on?
            boolean nileOn = false;
            ZZCell curb = vc.h("d.bind", 1, true);
            if(curb!=null) {
                String mode = ZZCursorReal.get(curb).getText();
                if(mode.indexOf("Nile") >= 0)
                    nileOn = true; 
            }
            pa("OBJECT: "+ob);
            boolean isC = false;
            if(ob instanceof ZZCursor) isC = true;
            pa("IS CURSOR?: "+isC);
            // if we have object which is not cursor, then nile action is impossible  
            if(nileOn && !(!(ob instanceof ZZCursor) && (ob != null)) ) {
                ZZCell nvc1 = AwtUtil.getCursoredCell(c_cursor1, vc);
                ZZCell nvc2 = AwtUtil.getCursoredCell(c_cursor2, vc);
                if(nvc1==null) return;
                
                if(nvc1.s("d.cursor-cargo", -1)==null) {
                    ZZCursorReal.set(nvc1, nvc1);
                    ZZCursorReal.setOffs(nvc1, 0);
                }
                if(nvc2!=null)
                    if(nvc2.s("d.cursor-cargo", -1)==null) {
                        ZZCursorReal.set(nvc2, nvc2);
                        ZZCursorReal.setOffs(nvc2, 0);
                    }


                p("NVC1: "+nvc1);
                p("NVC2: "+nvc2);

                nile_action(id, nvc1, nvc2, key, (ZZCursor)ob);
                //Nile1.module.action(id,code,target,nview,cview,key,pt,xi);
                return;
            }

	    if(id.equals("TESTWIN")) {
                AwtSystemList = target;

                // set awt dims
                AwtUtil.addDims(ZZDefaultSpace.findDefaultDimlist(code.getHomeCell()));

		// Cursors
                ZZCell curs = AwtSystemList.findText(AwtUtil.d_awtool, 1, "Cursors");
                if(curs == null) { 
                    ZZCell nCurs1, nCurs2, cat1;
                    ZZCell cursors = AwtSystemList.N(AwtUtil.d_awtool);
                    cursors.setText("Cursors");
                    cursors.N("d.1");
                }
                
                // View
                ZZCell rastname, rl, rc = AwtSystemList.findText(AwtUtil.d_awtool, 1, "View");
                if(rc == null) {
                    rc = AwtSystemList.N(AwtUtil.d_awtool);
                    rc.setText("View");
                    rl = rc.N("d.1");
                    rl.connect("d.2", 1, rl);
                    rl.setText("Awtool"); 
                    rastname = rl.N("d.1");
                    rastname.setText("Awtool.View1");
                } else {
                    rl = rc.s("d.1");
                    rastname = rl.s("d.1");
                }
                    
                
                // Bindings                
                ZZCell bindhome = AwtSystemList.findText(AwtUtil.d_awtool, 1, "Bind");
                if(bindhome == null) {
                    bindhome = AwtSystemList.N(AwtUtil.d_awtool);
                    bindhome.setText("Bind");
                    bindhome = bindhome.N("d.1", 1);

                    //ZZDefaultSpace.mkCorner(bindhome, actions);
                    actions(bindhome.h("d.2", 1));
                } else {
                    bindhome = bindhome.s("d.1");
                }
		// View metrics
                ZZCell metr = AwtSystemList.findText(AwtUtil.d_awtool, 1, "Metrics");
                if(metr == null) {
                    Dimension RV = new Dimension(400, 400);
                    Dimension VV = new Dimension(1000, 1000);
                    // create 'metrics collection'
                    metr = AwtSystemList.N(AwtUtil.d_awtool);
                    metr.setText("Metrics");
                    ZZCell mnew, metrList;

                    mnew = metrList = metr.N("d.1");
                    
                    M = new AwtMetricsNormal(mnew, RV, VV, 0.0,0.0);
                    M.cellRepr();

                    mnew = mnew.N("d.2");
                    M = new AwtMetricsFC(mnew, RV, VV, 0.0, 0.0);
                    M.cellRepr();
                     
                    mnew = mnew.N("d.2");
                    M = new AwtMetricsFC2(mnew, RV, VV, 0.0, 0.0);
                    M.cellRepr();
                    
                    // make circle
                    mnew.connect("d.2", 1, metrList);
                    AwtUtil.setCursor(c_metrics, metrList, AwtSystemList);                    
                    

                } else {
                    ZZCell currentMetrics =
                        AwtUtil.getCursoredCell(c_metrics, AwtSystemList);
                    if(currentMetrics == null) {
                        currentMetrics = metr.s("d.1");
                        AwtUtil.setCursor(c_metrics, currentMetrics, AwtSystemList);
                    }
                    M = (AwtMetrics)AwtUtil.readZObClone(currentMetrics);
                    M.setCell(currentMetrics);
                }

                // searching for active category ...
                ZZCell ac = AwtUtil.getCursoredCell(c_activeCategory, AwtSystemList);
                if(ac == null) {
                    if(AwtSystemList.getRankLength(AwtUtil.d_categories)>1) {
                        ac = AwtSystemList.s(AwtUtil.d_categories);
                    } else {
                        ac = AwtSystemList.N(AwtUtil.d_categories);
                        ac.setText("* SCRATCH *");
                    }
                    AwtUtil.setCursor(c_activeCategory, ac, AwtSystemList);
                }
                
                ZZCell v = ZZDefaultSpace.newToplevelView(rc.getSpace(), 
			"awtool", 0, 0, M.RealView[0], M.RealView[1], 
			rl, null, 
			null, null,
			null,
			new Color(0xFFFFB1));

                // Then, set bindings
                ZZCell bcurs = v.getOrNewCell("d.bind", 1);
                ZZCursorReal.set(bcurs, bindhome);
                ZZCursorReal.setColor(bcurs, Color.black);

                
                ZZCell awtc = v.h("d.2", 1).N("d.2");
                awtc.setText("AwtSystemList");
                ZZCursorReal.set(awtc, AwtSystemList);
                // init shared cursors
                ZZCell scursors = v.h("d.2",1).N("d.2");
                scursors.setText("SharedCursors");
                scursors.N("d.1");

                return;
	    }

            AwtSystemList = AwtUtil.getAwtSysList(view.getViewcell());
            if(AwtSystemList == null && !id.equals("TESTWIN")) {
                throw new ZZError("Awtool: AwtSystemList missing!!");
            }

            M = AwtUtil.getAwtMetrics(AwtSystemList);

            if(id.equals("StartDrag")) {
                if(note != null) {
                    dragNote = note;
                    noteRVCoord = M.mapToRealView(note.getX(), note.getY());
                    dragOffs.x = noteRVCoord.x - pt.x; 
                    dragOffs.y = noteRVCoord.y - pt.y;
                }
            } else

            if(id.equals("StopDrag") && dragNote != null) {
                noteVVCoord = M.mapToVirtualView(pt.x + dragOffs.x, pt.y + dragOffs.y);
                dragNote.setCoord(noteVVCoord[0], noteVVCoord[1]);
                dragNote.cellRepr();
                dragOffs.x = 0; dragOffs.y = 0;
                dragNote = null;
	    } else
            
            if(id.equals("Dragged") && dragNote != null) {
                pa("dragged!");
                noteVVCoord = M.mapToVirtualView(pt.x + dragOffs.x, pt.y + dragOffs.y);
                dragNote.setCoord(noteVVCoord[0], noteVVCoord[1]);
                dragNote.cellRepr();
                ZZUpdateManager.chg();                
            } else


            if(id.equals("StartResize")) {
                if(note != null) {
                    int dpx, dpy;
                    Dimension dim;
                    dragNote = note;
                    resizeWidth = resizeHeight = false;
                    noteRVCoord = M.mapToRealView(note.getX(), note.getY());
                    dpx = noteRVCoord.x - pt.x; 
                    dpy = noteRVCoord.y - pt.y;
                    dim = M.getRealDimension(note.getX(),
                    note.getY(), note.getDimension());

                    if(Math.abs(dpx)>(dim.width>>1)-4)
                        resizeWidth = true;
                    if(Math.abs(dpy)>(dim.height>>1)-4)
                        resizeHeight = true;

                    if(!resizeWidth && !resizeHeight) {
                        dragNote = null;
                    }
                }
            } else

            if(id.equals("StopResize") && dragNote != null) {
                dragNote = null;
                resizeWidth = resizeHeight = false;
                ZZUpdateManager.chg();                
	    } else
            
            if(id.equals("Resizing") && dragNote != null) {
                int dpx, dpy;
                Dimension dim;
                double[] vdim;
                noteRVCoord = M.mapToRealView(dragNote.getX(), dragNote.getY());

                dpx = noteRVCoord.x - pt.x; 
                dpy = noteRVCoord.y - pt.y;
                dim = M.getRealDimension(dragNote.getX(), dragNote.getY(),
                                         dragNote.getDimension());
                if(Math.abs(dpx)>dim.width>>1) resizeWidth = true;
                if(Math.abs(dpy)>dim.height>>1) resizeHeight = true;

                if(resizeWidth) dim.width = (dpx>=0) ? dpx<<1 : (-dpx)<<1;
                if(resizeHeight) dim.height = (dpy>=0) ? dpy<<1 : (-dpy)<<1;
                
                vdim = M.getVirtualDimension(noteRVCoord.x, noteRVCoord.y, dim);    

                dragNote.setDimension(vdim[0], vdim[1]);
                dragNote.cellRepr();
            } else

            if(id.equals("StartCreatingNote")) {
                dragCoord = pt;
                // this is chanced to 'true' when dragging occurs 
                creatingNote = false;                
            } else
 
            if(id.equals("CreatingNote")) {
                creatingNote = true;
            } else

            if(id.equals("StopCreatingNote") && creatingNote) {
                ZZCell newNoteCell;
                AwtNote newNote;
                int cx = (dragCoord.x+pt.x)>>1;
                int cy = (dragCoord.y+pt.y)>>1;
                double[] ndim, nvcenter = M.mapToVirtualView(cx, cy); 
                Dimension nd = new Dimension(Math.abs(dragCoord.x-pt.x), Math.abs(dragCoord.y-pt.y));
                ndim = M.getVirtualDimension(cx, cy, nd);

                ZZCell ac = AwtUtil.getCursoredCell(c_activeCategory, vc);

                ZZCell noteCell =
                       ac.h(AwtUtil.d_to).N(AwtUtil.d_to).N(AwtUtil.d_from);

                AwtUtil.createNewNote(noteCell, nvcenter, ndim);
                // attach new note also to 'notes' dimension along
                // active category cell.
                ac.h(AwtUtil.d_notes, 1).connect(AwtUtil.d_notes, 1, noteCell);
                dragCoord.x = 0;
                dragCoord.y = 0;
                creatingNote = false;

	    } else
            
            if(id.equals("SetCursor1") && nc != null) {
                ZZCell red = AwtUtil.getCursoredCell(c_cursor1, vc);
                if(red!=null && red.equals(nc)) return;
                AwtUtil.setCursor(c_cursor2, red, vc);
                AwtUtil.setCursor(c_cursor1, nc, vc);
                if(red!=null) {
                    red.excise("d.nile-wins");
                    nc.connect("d.nile-wins", 1, red);
                    nc.connect("d.nile-wins", -1, red);
                }
            } else

            if(id.equals("SetCursor2") && nc != null) {
                ZZCell gray = AwtUtil.getCursoredCell(c_cursor2, vc);
                if(gray!=null && gray.equals(nc)) return;
                AwtUtil.setCursor(c_cursor1, gray, vc);
                AwtUtil.setCursor(c_cursor2, nc, vc);
                if(gray!=null) {
                    gray.excise("d.nile-wins");
                    nc.connect("d.nile-wins", 1, gray);
                    nc.connect("d.nile-wins", -1, gray);
                }

            } else

            if(id.equals("NewLink")) {
                ZZCell c1, c2;
                c1 = AwtUtil.getCursoredCell(c_cursor1, vc);
                c2 = AwtUtil.getCursoredCell(c_cursor2, vc);
                if(c1 != null && c2 != null && c1 != c2) {
                    AwtUtil.createNewLink(c1, c2);
                }
            } else

            if(id.equals("ChangeMetrics")) {
                // save current metrics (focus, origo, etc.)
                M.cellRepr();
                ZZCell mc = AwtUtil.getCursoredCell(c_metrics, vc);
                mc = mc.s("d.2");
                AwtUtil.setCursor(c_metrics, mc, vc);
                M = (AwtMetrics)AwtUtil.readZObClone(mc);
                M.setCell(mc);
                ZZUpdateManager.chg();
            } else

            if(id.equals("ResetOrigo")) {
                //pa("ORIGO RESET!");
                M.setOrigo(0.0, 0.0);
                ZZUpdateManager.chg();
            } else

            if(id.equals("GridOnOff")) {
                //pa("GRID ON/OFF!");
	        boolean gon = M.GridOn;
                M.GridOn = gon ? false : true;
		if(M.GridOn != gon) {
		    M.cellRepr();
                }
                ZZUpdateManager.chg();
            } else 

            if(id.equals("FocusOrigoAndSelect")) {
                long elapsedTime = Long.MAX_VALUE;
                long cur = System.currentTimeMillis();
                double[] vf1, vf2;

                vf1 = M.mapToVirtualView(pt.x, pt.y);
                M.setFocusOrigo(pt.x, pt.y, nc);
                vf2 = M.mapToVirtualView(pt.x, pt.y);
                M.moveOrigo(vf1[0]-vf2[0], vf1[1]-vf2[1]);
                M.cellRepr();

                if(focusChangedLastTime > 0) {
                    elapsedTime = cur - focusChangedLastTime;
                    focusChangedLastTime = -1;
                } else {
                     focusChangedLastTime = cur;
                }

                pa("ELAPSED_TIME: "+elapsedTime);

                if(elapsedTime < 1500) {
                    // 'doubleclick'-focus should change also origo 
                    // this does not work reasonably 
                    // forceOrigoChange = true;
                    forceOrigoChange = false;
                } else { ZZUpdateManager.chg(); }
            }


            if(id.equals("ChangeOrigo") || forceOrigoChange) {
                double[] vvp = M.mapToVirtualView(pt.x, pt.y);
                // new origo is wanted to be in the center of realview
                double[] realOrigo = M.mapToVirtualView(M.RealView[0]>>1,
                M.RealView[1]>>1);
                M.moveOrigo(vvp[0]-realOrigo[0], vvp[1]-realOrigo[1]);
                M.cellRepr();
                ZZUpdateManager.chg();
            }
	}

	public ZOb newZOb(String id) {
	    if(id.equals("View1"))
		return new AwtView1();
	    else
		return null;
	}
    };

    // This is really hacky part...
    static ZZCell curAct;
    static void a(String key, String act) { a(key, act, null); }
    static void a(String key, String act, ZZCell next) {
	curAct = curAct.N("d.2", 1);
	curAct.setText(key);
	ZZCell n = curAct.N("d.1", 1);
	n.setText("Awtool." + act);
	if(next != null)
	    next.insert("d.3", 1, n);
    }

    static void actions(ZZCell start) {
        ZZCell awtact = start;           awtact.setText("Awtool mode");
	ZZCell nchar = awtact.N("d.1",1); nchar.setText("Nile char");
	ZZCell nword = nchar.N("d.1", 1); nword.setText("Nile word");
	ZZCell nsent = nword.N("d.1", 1); nsent.setText("Nile sent");
	ZZCell npara = nsent.N("d.1", 1); npara.setText("Nile para");

	ZZCell ncommon = npara.N("d.1", 1); ncommon.setText("Nile common");

	

	curAct = nword;
	// Annoying - these should be structurally 
	// describable!!!
	a("MouseClicked1", "MouseSetCursor 0 WORD");
	a("Backspace", "BackSpace 0 WORD");
	a("Delete", "Delete 0 WORD");
	a("Alt-Backspace", "Delete 0 WORD");
	a("Ctrl-P", "Tunnel - WORD"); // P for pull
	a("Ctrl-T", "Transcopy - WORD");
	a("Left", "Cursor - WORD");
	a("Right", "Cursor + WORD");
	a("Shift-Left", "AdjLeft - WORD");
	a("Shift-Right", "AdjRight + WORD");
	a("Ctrl-Left", "AdjRight - WORD");
	a("Ctrl-Right", "AdjLeft + WORD");
	a("Alt-Left", "Hop - WORD");
	a("Alt-Right", "Hop + WORD");
	a("Enter", "BreakParagraph 0 WORD");

	a("Up", "Cursor - SENT");
	a("Down", "Cursor + SENT");
	a("Alt-Up", "Hop - SENT");
	a("Alt-Down", "Hop + SENT");

	a("INSERT", "Insert", nchar);
	a("Tab", "Mode 0 SENT", nsent);
        // awtool specific
        a("Alt-N", "NileOnOff", awtact);

	curAct = nsent;
	a("MouseClicked1", "MouseSetCursor 0 SENT");
	a("Backspace", "BackSpace 0 SENT");
	a("Delete", "Delete 0 SENT");
	a("Alt-Backspace", "Delete 0 SENT");
	a("Ctrl-P", "Tunnel - SENT"); // P for push
	a("Ctrl-T", "Transcopy - SENT");
	a("Left", "Cursor - SENT");
	a("Right", "Cursor + SENT");
	a("Shift-Left", "AdjLeft - SENT");
	a("Shift-Right", "AdjRight + SENT");
	a("Ctrl-Left", "AdjRight - SENT");
	a("Ctrl-Right", "AdjLeft + SENT");
	a("Alt-Left", "Hop - SENT");
	a("Alt-Right", "Hop + SENT");
	a("Enter", "BreakParagraph 0 SENT");

	a("Up", "Cursor - PARA");
	a("Down", "Cursor + PARA");
	a("Alt-Up", "Hop - PARA");
	a("Alt-Down", "Hop + PARA");

	a("INSERT", "Insert", nchar);
	a("Tab", "Mode 0 CHAR", nchar);
        // awtool specific
        a("Alt-N", "NileOnOff", awtact);

/*
	curAct = npara;
	a("MouseClicked1", "MouseSetCursor 0 PARA");
	a("Backspace", "BackSpace 0 PARA");
	a("Delete", "Delete 0 PARA");
	a("Alt-Backspace", "Delete 0 PARA");
	a("Left", "Cursor - PARA");
	a("Right", "Cursor + PARA");
	a("Shift-Left", "AdjLeft - PARA");
	a("Shift-Right", "AdjRight + PARA");
	a("Ctrl-Left", "AdjRight - PARA");
	a("Ctrl-Right", "AdjLeft + PARA");
	a("Alt-Left", "Hop - PARA");
	a("Alt-Right", "Hop + PARA");

	a("INSERT", "Insert");
	a("Tab", "Mode 0 CHAR", nchar);
 */

	curAct = nchar;
	a("MouseClicked1", "MouseSetCursor 0 CHAR");
	a("Left", "Cursor - CHAR");
	a("Right", "Cursor + CHAR");
	a("Backspace", "BackSpace 0 CHAR");
	a("Delete", "Delete 0 CHAR");
	a("Alt-Backspace", "Delete 0 CHAR");
	a("INSERT", "Insert");
	a("Tab", "Mode 0 WORD", nword);
	a("Enter", "BreakParagraph 0 CHAR");
	
	a("Ctrl-C", "PUICopy 0 CHAR");
    
	a("Up", "Cursor - WORD");
	a("Down", "Cursor + WORD");
        // awtool specific
        a("Alt-N", "NileOnOff", awtact);

        // awtool acts
        curAct = awtact;
        a("MouseStartDrag", "StartDrag");
        a("MouseDragged1", "Dragged");
        a("MouseReleased1", "StopDrag");
        a("Ctrl-MouseStartDrag", "StartResize");
        a("Ctrl-MouseDragged1", "Resizing");
        a("Ctrl-MouseReleased1", "StopResize");
        a("Shift-MouseStartDrag", "StartCreatingNote");
        a("Shift-MouseDragged1", "CreatingNote");
        a("Shift-MouseReleased1", "StopCreatingNote");
        a("Shift-MouseClicked3", "ChangeOrigo");
        a("MouseClicked3", "FocusOrigoAndSelect");
        a("Shift-MouseClicked1", "SetCursor2");
        a("MouseClicked1", "SetCursor1");
        a("l", "NewLink");
        a("m", "ChangeMetrics");
        a("g", "GridOnOff");
        a("r", "ResetOrigo");
        a("Alt-N", "NileOnOff", nchar);

    }
}
