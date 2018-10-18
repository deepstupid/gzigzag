/*
VobVanishingClient.java
 *    
 *    Copyright (c) 2002, Tuomas Lukka
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
package org.gzigzag.client;
import org.gzigzag.impl.*;
import org.gzigzag.impl.Cursor;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.util.*;
import java.awt.*;

public class VobVanishingClient implements VanishingClient, View {
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }

    public PlainVanishing vanishing = new PlainVanishing();

    CellVobFactory cvf;
    static ScalableFont font = new ScalableFont("SansSerif", Font.PLAIN, 12);
    public CellConnector cellConnector = new CellConnector();
    VobScene into;

    /** The zoom level.
     * Currently, three levels: 0 = usual cell size, 1 = half-zoomed,
     * 2 = pixel-pixel.
     */
    static int zoom = 0;

    static void toggleZoom() {
        zoom++;
	zoom %= 3;
    }

    /** Whether to apply zoom to this view.
     */
    boolean usezoom = true;

    /** Which page to show on multipage spans
     * XXX We might want this to be reset when the span changes
     */
    static int page = 0;


    public void render(VobScene into, Cell window) {
	Dimension d = into.getSize();
	render(into, window, d.width / 2, d.height / 2);
    }

    public void render(VobScene into, Cell window, int x, int y) {
	render(into, window, Cursor.get(window), x, y);
    }

    Dim[] dims;

    public void render(VobScene into, Cell window, Cell cursor, int px, int py) {

	cvf = new CellVobFactory(cursor);
	this.into = into;
	dims = readDims(window);

	vanishing.render(this, cursor, px, py, dims);

	int w = 60; int h = 20;


//	XXXX SpanView (uses multiple PlainVanishings) draws extra lines
//	if decorate is called here... :-/
//	decorate(into);

	// Place the dimension cells
	// XXX This must be abstracted out to somewhere!
	Font f = font.getFont(1000);
	FontMetrics fm = font.getFontMetrics(1000);
	int dw = fm.stringWidth("XXXXXXX")+10;
	int dh = fm.getHeight()+10;
	Cell dimc = Params.getParam(window, Client.c_dims);
	into.put(cvf.new CellVob(dimc,f,fm,cellConnector), 1, 0, 0, dw, dh);
	dimc = dimc.s(Client.d1);
	int y = dh;
	while (dimc != null) {
	    int x = 0;
	    Cell c = dimc;
	    while (c != null) {
		into.put(cvf.new CellVob(c,f,fm,cellConnector), 1, x, y, dw, dh);
		x += dw;
		c = c.s(Client.d2);
	    }
	    y += dh;
	    dimc = dimc.s(Client.d1);
	}
    }

    /** Read the current set of dimensions of window.
     * XXX Should be abstracted out!
     */
    static public Dim[] readDims(Cell window) {
	Cell dimlist = Params.getParam(window, Client.c_dims).s(Client.d1);
	Cell[] cells = Params.getCells(dimlist, Client.d1, 200, null);
	int NDIR = 4;
	Dim[] dims = new Dim[cells.length > NDIR ? NDIR : cells.length];
	for(int i=0; i<dims.length; i++)
	    dims[i] = window.space.getDim(cells[i]);
	return dims;

    }
    public Object getVobSize(Cell c, float fract, int flags, Dimension outDim) {

	if((flags & VanishingClient.CENTER) != 0) {
	    pa("Center: "+c+" span: "+c.getSpan());
	}

	int iw = (int)(60 * fract);
	int ih = (int)(20 * fract);
	Span span = c.getSpan();
	Vob v;
	if (span != null && span instanceof ImageSpan) {
	    // Paint the span, if it's e.g. an imagespan.
	    Image img = null;
	    if (span instanceof PageSpan) {
		// Take 1st page if not under cursor, otherwise page'th page
		pa("Is pagespan");
		PageSpan pagespan = (PageSpan)span;
		if((flags & VanishingClient.CENTER) != 0) {
		    pa("Center: take page'th page");
		    // clip page index to span length
		    if(page >= pagespan.length()) {
			pa("Clipping page to "+pagespan);
			page = pagespan.length() - 1;
		    }
		    img = ((PageSpan)(pagespan.subSpan(page, page+1))).
				getImage();
		} else {
		    img = ((PageSpan)(pagespan.subSpan(0, 1))).getImage();
		}
	    }
	    else if (span instanceof ImageSpan) {
		pa("Is imagespan");
		img = ((ImageSpan)span).getImage();
	    }

	    pa("Image for cellimagevob: "+img);
	    
	    // If available, use the dimensions of the actual image
	    // returned. There are some bugs with assuming the letter paper size
	    // for now.
	    Dimension dii = ((ImageSpan)span).getSize();
	    v = new CellImageVob(c, img, cellConnector);

	    if (img != null) {
		iw = img.getWidth(null);
		ih = img.getHeight(null);
	    }
	    if(iw == -1) iw = (int)dii.width;
	    if(ih == -1) ih = (int)dii.height;
	    
	    
	    if (iw > 0 && ih > 0) {
		double img_ratio = ((double)iw) / ih;
		boolean zoomnow = usezoom && 
		    ((flags & VanishingClient.CENTER) != 0);
		if(zoomnow && zoom == 2) {
		    // no change: just use iw and ih -- pixel-to-pixel.
		} else {
		    if(!zoomnow || zoom == 0)
			iw = (int)(60 * fract);
		    else /* zoom == 1 */
			iw = (int)((iw + 60 * fract) / 2);
		    ih = (int)(iw / img_ratio);
		}
		/*
		  if (img_ratio < 1) {
		  ih = h;
		  iw = (int)(img_ratio * h);
		  }
		  else {
		*/
		// iw =  (int)(60 * fract); // XXX
		// ih = (int)(((double)iw) / img_ratio);
		/* } */
	    }
	    else {
		iw = (int)(60 * fract); 
		ih = (int)(20 * fract);
	    }

	    pa("Vobsize for image: "+iw+" "+ih+" "+v);
	}
	else {
	    // A normal CellVob
	    v = cvf.new CellVob(c, font.getFont((int)(1000*fract)),
				font.getFontMetrics((int)(1000*fract)), cellConnector);
	}
	outDim.width= iw; 
	outDim.height= ih; 
	return v;
    }

    public void place(Cell c, Object o, float fract, int x0, int y0, int x1, int y1,
		int depth, float rot) {
	Vob v = (Vob)o;
	into.put(v, depth, x0, y0, x1-x0, y1-y0);
    }

    public void connect(Cell cell1, Cell cell2, int dx, int dy) {
	if(cell2 == null) return;
	DecoratedVob vob1 = (DecoratedVob)into.get(cell1);
	DecoratedVob vob2 = (DecoratedVob)into.get(cell2);
	cellConnector.connect(into, vob1, vob2, 
		    (int)(Math.atan2(dx, -dy) * 360 / (2 * Math.PI)));
	cellConnector.connect(into, vob2, vob1, 
		    180 + (int)(Math.atan2(dx, -dy) * 360 / (2 * Math.PI)));
    }

}
