/*
CellVob.java
 *
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
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
import org.gzigzag.vob.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** A factory for making basic cell vobs.
 * This is an enclosing class and a factory at the same time
 * in order to store Dim objects for fast access.
 * <p>
 * New cell vobs are created like
 * <pre>
 * 	CellVobFactory cvf;
 * 	Vob v = cvf.new CellVob(...);.
 * </pre>
 */

public class CellVobFactory {

    Dim d_clone;
    Dim d_cursor;
    Dim d_spaces;

    CompoundSpace space;

    /** @see #getSliceId.
     * To be compared using ==
     **/
    Object sliceId = null;

    public CellVobFactory(Space s) {
	Cell cl = Dims.d_clone(s);
	d_clone = s.getDim(cl);
	d_cursor = s.getDim(Dims.d_cursor_id);
	d_spaces = s.getDim(Dims.d_spaces_id);
	if(s instanceof CompoundSpace)
	    space = (CompoundSpace)s;
    }

    /** Create a new cellvobfactory whose "current slice" is
     * the slice of the given center cell.
     */
    public CellVobFactory(Cell center) {
	this(center.space);
	sliceId = getSliceId(center);
    }

    static public final Dimension getSize(FontMetrics fm, String str,
		int xoffs, int yoffs) {
	int w = fm.stringWidth(str) + xoffs * 2 + 2;
	int h = fm.getHeight() + 2*yoffs;
	if(w < h) w = h;
	return new Dimension( w, h );
    }

    /** Get an Id for the slice this Cell is in.
     *  Iff getSliceId(c).equals(getSliceId(d)), c and d are in the same slice.
     */
    public Object getSliceId(Cell c) {
	if(space == null) return null;
	return space.getSpace(c);
	/*
	String id = c.id;
	int x = id.lastIndexOf(":");
	if(x < 0)
	    return "";
	else
	    return id.substring(0, x);
	*/
    }

    public class CellVob extends CellBgVob {
    public static final String rcsid = "$Id: CellVobFactory.java,v 1.3 2002/03/10 01:16:23 bfallenstein Exp $";
	public static final boolean dbg = true;

	String s;
	Font f;
	// XXX Should this *really* need to be stored here?
	FontMetrics fm;

	/** Font height. */
	int fh;
	int fasc;
	/** String width. */
	int sw;

	Cell strCell;

	int xoffs = 0; // Previously, these margins where three, but at the moment
	int yoffs = 0; // margins are cared for by the BoxTypes



	public void renderContent(Graphics g,
		int mx, int my, int mw, int mh, Vob.RenderInfo info) {

	    Shape oldClip = g.getClip();
	    g.clipRect(mx, my, mw, mh);
	    int ty = my + (mh-fh)/2 + fasc;
	    g.setFont(f);
	    g.drawString(s, mx+xoffs, ty);
	    g.setClip(oldClip);
	}

	public CellVob(Cell c,
		Font f, FontMetrics fm, CellConnector connector) {
	    super(c, connector);

	    this.s = c.t();

	    if (c.s(d_clone, -1) != null) // if clone
		setBg(Color.yellow);
	    else if (c.s(d_clone, 1) != null) // if rootclone
		setBg(new Color(0xffff8c));

	    if (c.s(d_cursor) != null) // if accursed
		addColor(new Color(0xaa96ff));

	    Object sliceId2 = getSliceId(c);
	    if((sliceId==null && sliceId2!=null)
	       || (sliceId != null && !sliceId.equals(sliceId2))
	       ) { // if different slice
		setBg(bg.darker().darker());
	    }

	    if(c.s(d_spaces, -1) != null) // if includes a slice
		setBg(new Color(bg.getRGB() & (int)0xff00ffffL));

	    if (this.s == null) this.s = "";
	    this.f = f;
	    this.fm = fm;

	    this.fh = fm.getAscent()+fm.getDescent();
	    this.fasc = fm.getAscent()+fm.getLeading();
	    this.sw = fm.stringWidth(this.s);
	}
    }
}
