/*   
WrapperSpace.java
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
 * Written by Rauli Ruohonen
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import java.util.*;

public class WrapperSpace extends AbstractSpace {
public static final String rcsid = "$Id: WrapperSpace.java,v 1.14 2001/08/18 15:03:29 bfallenstein Exp $";
    public final String prefix;
    public final Space base;
    protected static class WrapperDim extends AbstractDim {
	public final Dim base;
	public final WrapperSpace sp; // Casts are expensive in some JVM's
	public WrapperDim(WrapperSpace space, Dim base) {
	    super(space);
	    this.base = base;
	    this.sp = space;
	}
	public Cell s(Cell c, int steps, Obs o) {
	    return sp.translate(base.s(sp.detranslate(c), steps, o));
	}
	public void connect(Cell c, Cell d)
	    throws ZZAlreadyConnectedException {
	    base.connect(sp.detranslate(c), sp.detranslate(d));
	}
	public void disconnect(Cell c, int dir) {
	    base.disconnect(sp.detranslate(c), dir);
	}
	public boolean isCircularHead(Cell c, Obs o) {
	    return base.isCircularHead(sp.detranslate(c), o);
	}
        public void addRealNegSides(Set to) {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }
    public Cell translate(Cell c) {
	if (c == null) return null;
	if (c.id.startsWith("home-id:") && prefix.equals("home-id:"))
	    return new Cell(this, c.id);
	return new Cell(this, prefix + c.id);
    }
    public String detranslate(String id) {
	if (id.startsWith("home-id:") && !prefix.equals("home-id:"))
	    return id;
	if (!id.startsWith(prefix))
	    throw new ZZImpossibleCellException("Cell id "+id+
						" does not have the"+
						" correct prefix");
	return id.substring(prefix.length());
    }
    public Cell detranslate(Cell c) {
	if (c.space != this)
	    throw new IllegalArgumentException("Cell "+c+" is not from the"+
					       " correct space");
	return new Cell(base, detranslate(c.id));
    }
    public WrapperSpace(String prefix, Space base) {
	this.prefix = prefix + ":";
	this.base = base;
    }
    public Cell getHomeCell() { return translate(base.getHomeCell()); }
    public Cell getCell(String id) {
	return translate(base.getCell(detranslate(id)));
    }
    public Dim getDim(Cell name) {
	name = Id.get(name, base);
	return new WrapperDim(this, base.getDim(name));
    }
    public Dim getCloneDim() {
	return getDim(Dims.d_clone_id);
    }
    public Cell N(Cell c) { return translate(base.N(detranslate(c))); }
    public Cell N(Cell c, Dim dim, int dir, Obs o) {
	return translate(base.N(detranslate(c),
				((WrapperDim)dim).base, dir, o));
    }
    public void delete(Cell c) { base.delete(detranslate(c)); }
    public void setSpan(Cell c, Span s) { base.setSpan(detranslate(c), s); }
    public Span getSpan(Cell c, Obs o) {
	return base.getSpan(detranslate(c), o);
    }
    public void setText(Cell c, String s) { base.setText(detranslate(c), s); }
    public String getText(Cell c, Obs o) {
	return base.getText(detranslate(c), o);
    }
    public Object getJavaObject(Cell c, Obs o) {
	return base.getJavaObject(detranslate(c), o);
    }
}
