/*   
AwtView1.zob
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
 * Written by Kimmo Wideroos
 */

/** View for a(ssociative) writing tool
*/

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;
//import java.lang.reflect.*;

public class AwtView1 extends Awtool {

    static AwtNileView awtNile = new AwtNileView();

    class AwtBgFlob extends CellBgFlob {

        public AwtBgFlob(int x, int y, int d, int w, int h, ZZCell c) {
            super(x, y, d, w, h, c);
        }

        public void render(Graphics g, int mx, int my, int md, int mw, int mh) {

            Color[] solids = getSolidColors();
            int nsolids = 0;

            if(solids!=null) nsolids = solids.length;

            Color oldfg = g.getColor();
            Shape oldclip = g.getClip();
            
            g.clipRect(mx, my, mw, mh);
            
            if(solids != null) {
                int iq=0, id=0;
                for(int i=0; i<nsolids-1; i++) {
                    g.setColor(solids[i]);
                    if(mw-iq-5>0 && mh-iq-5>0) {
                        g.drawRect(mx+id+1, my+id+1, mw-iq-3, mh-iq-3);
                        g.drawRect(mx+id+2, my+id+2, mw-iq-5, mh-iq-5);
                        id += 2;
                        iq += 4;
                    }
                g.setColor(bg);
                g.fillRect(mx+id+1, my+id+1, mw-iq-3, mh-iq-3);
                //g.fillRect(mx+(mw*i)/nsolids, my, mw/nsolids+1, mh);
                }
            } else {
                g.setColor(bg);
                g.fillRect(mx, my, mw, mh);
            }
            
            g.setColor(oldfg);
            
            renderContent(g, mx, my, mw, mh);
            
            // XXX Span indication???
            
            g.drawRect(mx, my, mw-1, mh-1);
        
            g.setClip(oldclip);
        }
    }

    class AwtLinkFlob extends CellBgFlob {

        Point from, to;

        AwtLinkFlob(Point from, Point to, int d, ZZCell c) {
            super((from.x+to.x)>>1, (from.y+to.y)>>1, d, Math.abs(from.x-to.x),
                  Math.abs(from.y-to.y), c);
            this.from = from;
            this.to = to;
            //super((from.x+to.x)>>1, (from.y+to.y)>>1, w, h, 2, c);
        }

	public void render(Graphics g, int mx, int my, int md,
			   int mw, int mh) {
    
            Color[] solids = getSolidColors();
            int nsolids = 0;

            if(solids!=null) nsolids = solids.length;

            Color oldfg = g.getColor();
            //Shape oldclip = g.getClip();
            
            // XXX save font?
            
            //g.clipRect(mx, my, mw, mh);
        
            if(solids != null) {
                int lx, ly, dw, dh;
                lx = from.x;
                ly = from.y;
                dw = lx - to.x;
                dh = ly - to.y;
                for(int i=0; i<nsolids; i++) {
                    g.setColor(solids[i]);
                    g.drawLine(lx, ly, lx+dw, ly+dh);
                    lx +=dw;
                    ly +=dh;
                }
            } else {
                //g.setColor(bg);
                g.setColor(Color.black);
                int px = from.x-to.x, py = from.y-to.y;
                g.drawLine(from.x, from.y, to.x, to.y);
            }
            
            g.setColor(oldfg);
                        
            //g.setClip(oldclip);
        }
        
        public Object hit(int x, int y) {
            double k, d;
            k = (double)(w*(y-from.y) - h*(x-from.x));
            d = Math.sqrt(k*k / (w*w+h*h));
            if(d<20.0) {
                // near enough the line
                return new ZZCursorVirtual(super.c);
            }
            return null;
        }
    }

    public void makeAwtNote(FlobSet into, ZZCell view, ZZCell noteCell,
                             Rectangle rect, boolean NileOn, int depth ) {
        Flob bgfl;

        // noteCell kind of emulates viewcell... ugly or elegant, don't know
        if(noteCell.s("d.cursor-cargo", -1)!=null && Awtool.dragNote==null) {
            ZZCell naccursed = ZZCursorReal.get(noteCell);
            awtNile.awtNileRaster(into, noteCell, naccursed, rect, depth);
        }

        bgfl = new AwtBgFlob(rect.x-2, rect.y-2, depth+1, rect.width+4,
                             rect.height+4, noteCell);

        ((CellBgFlob)bgfl).setBg(Color.lightGray);
        CellFlobFactory2.addSolidColors(into, (CellBgFlob)bgfl);
        into.add(bgfl);
    }

    public void raster(FlobSet into, FlobFactory fact,
	    ZZCell view, String[] dims, ZZCell accursed) {

        ZZCell awtsyslist = AwtUtil.getAwtSysList(view);
       
        AwtMetrics M = AwtUtil.getAwtMetrics(awtsyslist);

        Dimension d = into.getSize();
        if(!M.RealViewEquals(d)) {
            M.setRealView(d, true);
            M.cellRepr();
        } 

        boolean nileOn = false;
        ZZCell curb = view.h("d.bind", 1, true);
        if(curb!=null) {
            String mode = ZZCursorReal.get(curb).getText();
            if(mode.indexOf("Nile") >= 0)
                nileOn = true; 
        }

        Dimension nDim;
        Point nCoord = new Point();
        Rectangle rect;

        ZZCell activeCategory = 
            AwtUtil.getCursoredCell(Awtool.c_activeCategory, awtsyslist);

        if(activeCategory==null) {
            activeCategory = 
                awtsyslist.h(AwtUtil.d_categories,1).
                           s(AwtUtil.d_categories, -1).
                           getOrNewCell(AwtUtil.d_categories);
            AwtUtil.setCursor(Awtool.c_activeCategory, activeCategory, awtsyslist);
        }
        Categories C = new Categories(activeCategory);

        ZZCell cursor1 = AwtUtil.getCursoredCell(Awtool.c_cursor1, view);
        ZZCell cursor2 = AwtUtil.getCursoredCell(Awtool.c_cursor2, view);

        AwtNote[] validNotes = C.getValidNotes();

        // 'renderedNotes': key = notecell, value = {int x, y, w, h}
        Hashtable renderedNotes = new Hashtable(); 

        Vector notFixedNotes = new Vector();
        ZZCell noteCell, noteParams, categoryLinkCell;
        AwtLink catlink;
        AwtNote note;
        int[] noteSize;
        int depth = 100;
        double y, x, yRowMax=0.0, w, h, gap=0.1;

        for(int i=0; i<validNotes.length; i++) {
            note = validNotes[i];
            noteCell = note.getCell();
            // only category notes have fixed position
            if(note.getCategoryLink() == null) {
                notFixedNotes.addElement(note);
                continue;
            }
            nCoord = M.mapToRealView(note.getX(), note.getY());
            nDim = M.getRealDimension(note.getX(),note.getY(),note.getDimension());

            // importtant: notes' headcells as keys 
            renderedNotes.put(noteCell.h(AwtUtil.d_clone), 
                              new int[] { nCoord.x, nCoord.y, nDim.width, nDim.height });

            rect = new Rectangle(nCoord.x-(nDim.width>>1), nCoord.y-(nDim.height>>1), 
                                 nDim.width, nDim.height);

            if(noteCell.equals(cursor1)) {
                depth = 1; 
            } else 
            if(noteCell.equals(cursor2)) { 
                depth = 10; 
            } else { depth = 100; }

            makeAwtNote(into, view, noteCell, rect, nileOn, depth);

        }

        AwtUtil.quickSort(notFixedNotes, 0, notFixedNotes.size()-1, 
                  new AwtUtil.ObjectComparator() {
            public int compare(Object o1, Object o2) {
                return (( ((AwtNote)o1).getHeight() <
                          ((AwtNote)o2).getHeight() ) ? 1 : 0 );
            }
        });

        y = 1.0-gap; x = gap;
        for(Enumeration e=notFixedNotes.elements(); e.hasMoreElements();){
            note = (AwtNote)e.nextElement();
            noteCell = note.getCell();
            w = note.getWidth();
            if(x+w>1.0-gap) {
                x = gap;
                y += yRowMax;
                yRowMax = 0;
            }
            h = note.getHeight();
            note.setCoord(x+(w/2.0), y+(h/2.0));
            x += (w+gap);
            if(h>yRowMax) yRowMax = h;


            nCoord = M.mapToRealView(note.getX(), note.getY());

            nDim = M.getRealDimension(note.getX(), note.getY(), note.getDimension());

            // importtant: notes' headcells as keys 
            renderedNotes.put(noteCell.h(AwtUtil.d_clone), 
                              new int[] { nCoord.x, nCoord.y, nDim.width, nDim.height });

            rect = new Rectangle(nCoord.x, nCoord.y, nDim.width,
                                 nDim.height); 

            if(noteCell.equals(cursor1) || noteCell.equals(cursor2)) { 
                depth = 1; 
            } else { depth = 100; }

            makeAwtNote(into, view, noteCell, rect, nileOn, depth);
        }

        // render links
 
        int[] toField, fromField;
        ZZCell toc, fromc;
        AwtLink al;
        AwtLink[] validLinks = C.getAllValidLinks(true, true);

        for(int i=0; i<validLinks.length; i++ ) {
            al = validLinks[i];
            toc = al.toHeadCell(); 
            fromc = al.fromHeadCell();

            if(toc == null || fromc == null) continue;
            if(!renderedNotes.containsKey(toc) || 
               !renderedNotes.containsKey(fromc)) continue;
            toField = (int[])renderedNotes.get(toc);
            fromField = (int[])renderedNotes.get(fromc);

            Flob alinkflob = new AwtLinkFlob(new Point(fromField[0], fromField[1]), 
                                     new Point(toField[0], toField[1]), 2000, 
                                     al.getCell());

            into.add(alinkflob);
        }

	SimpleBeamer2 sb = new SimpleBeamer2();
	sb.decorate(into, "", null);

        if(M.GridOn) {
            AwtUtil.renderGrid(into, M, -1.0, -1.0, 1.0, 1.0, Color.gray, 10);
        }

        return;
    }
}
