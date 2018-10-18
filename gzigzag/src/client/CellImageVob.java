/*
CellImageVob.java
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
 * Written by Tero Mäyränen
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class CellImageVob extends DecoratedVob {
public static final String rcsid = "$Id: CellImageVob.java,v 1.2 2002/03/24 19:50:54 tjl Exp $";
    public static final boolean dbg = true;

    private Image img = null;

    public void render(Graphics g, int x, int y, int w, int h,
	boolean boxDrawn, RenderInfo info) {
	pa("Render cellimagevob");
	renderDecorations(g, x, y, w, h, info);
	if(info.isFast()) {
	    pa("Render cellimagevob fast");
	    g.setColor(Color.white);
	    g.fillRect(x, y, w, h);
	    return;
	}
	if (img != null) {
	    pa("Render cellimagevob with image");
	    g.drawImage(img, x, y, w, h, UpdateManager.getImageObserver());
	}
    }

    public CellImageVob(Cell c, Image im, CellConnector connector) {
	super(c, connector);
	pa("Construct cellimagevob");
	img = im;
    }
}
