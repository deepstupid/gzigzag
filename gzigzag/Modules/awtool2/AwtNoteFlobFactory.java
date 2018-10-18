/*   
AwtNoteFlobFactory.java
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

/** An flob factory that shows awt notes.
 */
 
public class AwtNoteFlobFactory extends AwtArtefactFlobFactory {

    static AwtNileView awtnile = new AwtNileView();
    private AwtDraggable dragObj = null;

    public AwtNoteFlobFactory(AwtMetrics awtm) { super(awtm); }
    public AwtNoteFlobFactory() { super(); }

    public void setDragging(AwtDraggable dobj) {
        dragObj = dobj;
    }

    public Flob renderFlob(FlobSet into, ZZCell c, ZZCell handleCell,
                         float fract, int x, int y, int d, int w, int h) {
        // note cell kind of emulates viewcell... ugly or elegant, don't know
	ZZCell sp_cur = c.h("d.clone");
        if(sp_cur.s("d.cursor-cargo", -1)!=null && dragObj==null) {
            ZZCell naccursed = ZZCursorReal.get(sp_cur);
	    Rectangle rect = new Rectangle(x, y, w, h);
            awtnile.raster(into, sp_cur, naccursed, rect, d);
        }
        return null;
    }
}

