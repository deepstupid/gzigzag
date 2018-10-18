/*
move.java
 *
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
 *	Written by Tero Mäyränen
 */

package org.gzigzag.module.multimedia;
import org.gzigzag.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class move extends MediaZOb
{
	public static final String rcsid = "$Id: move.java,v 1.2 2001/03/05 08:44:14 deetsay Exp $";

	Pixels pix = null;

	private int oldframe=0, oldx=0, oldy=0;

	public Pixels getPixels()
	{
		Pixels inpix = readParam("input").getPixels();
		int x = readParam("x").getInt(0);
		int y = readParam("y").getInt(0);
		if (pix != null && inpix.getFrame() == oldframe && x == oldx && y == oldy)
		{
			return pix;
		}
		oldframe = inpix.getFrame(); oldx = x; oldy = y;

		int w = inpix.getWidth();
		int h = inpix.getHeight();

		if (pix == null)
		{
			pix = new Pixels(w, h);
		}
		else
		{
			pix.setSize(w, h);
		}

		pix.setX(inpix.getX() + x);
		pix.setY(inpix.getY() + y);

		int p[] = pix.getPixels(), ip[] = inpix.getPixels();
		for (int i=0; i<w*h; i++) p[i] = ip[i];

		pix.setFrame(pix.getFrame()+1);
		return pix;
	}
}
