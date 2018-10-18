/*
crop.java
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

/**
 *	unfinished
 */

public class crop extends MediaZOb
{
	public static final String rcsid = "$Id: crop.java,v 1.2 2001/03/05 08:44:14 deetsay Exp $";

	private Pixels pix = null;

	private int oldframe=0, oldpw=0, oldph=0, oldpx=0, oldpy=0;

	public Pixels getPixels()
	{
		Pixels inpix = readParam("input").getPixels();

		int iw = inpix.getWidth();
		int ih = inpix.getHeight();
		int ix = inpix.getX();
		int iy = inpix.getY();

		int pw = readParam("width").getInt(iw);
		int ph = readParam("height").getInt(ih);
		int px = readParam("x").getInt(ix);
		int py = readParam("y").getInt(iy);

		if (pix != null && inpix.getFrame() == oldframe
			&& pw == oldpw && ph == oldph && px == oldpx && py == oldpy)
		{
			return pix;
		}
		oldpw = pw; oldph = ph; oldpx = px; oldpy = py; oldframe = inpix.getFrame();

		if (pix == null)
		{
			pix = new Pixels(pw, ph);
		}
		else
		{
			pix.setSize(pw, ph);
		}
		pix.setX(px);
		pix.setY(py);

		int p[] = pix.getPixels(), ip[] = inpix.getPixels();
		int i = 0;
		for (int y=py; y<py+ph; y++)
		{
			if ((y < iy) || (y >= iy + ih))
			{
				for (int x=0; x<pw; x++) p[i++] = 0xff000000;
			}
			else
			{
				for (int x=px; x<px+pw; x++)
				{
					if ((x < ix) || (x >= ix + iw)) p[i++] = 0xff000000;
					else p[i++] = ip[ ((y-iy)*iw)+(x-ix) ];
				}
			}
		}
		pix.setFrame(pix.getFrame()+1);
		return pix;
	}
}
