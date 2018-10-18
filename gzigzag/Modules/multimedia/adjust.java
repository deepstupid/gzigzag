/*
adjust.java
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

public class adjust extends MediaZOb
{
	public static final String rcsid = "$Id: adjust.java,v 1.1 2001/03/05 08:44:14 deetsay Exp $";

	private Pixels pix = null;

	private int oldframe=0, oldradj=0, oldgadj=0, oldbadj=0, oldhadj=0, oldsadj=0, oldbradj=0;

	public Pixels getPixels()
	{
		Pixels inpix = readParam("input").getPixels();

		int radj = readParam("red").getInt(100);
		int gadj = readParam("green").getInt(100);
		int badj = readParam("blue").getInt(100);

		int hadj = readParam("hue").getInt(100);
		int sadj = readParam("saturation").getInt(100);
		int bradj = readParam("brightness").getInt(100);

		if (pix != null && inpix.getFrame() == oldframe
			&& radj == oldradj && gadj == oldgadj && badj == oldbadj
			&& hadj == oldhadj && sadj == oldsadj && bradj == oldbradj)
		{
			return pix;
		}
		oldradj = radj; oldgadj = gadj; oldbadj = badj;
		oldhadj = hadj; oldsadj = sadj; oldbradj = bradj;
		oldframe = inpix.getFrame();

		int w = inpix.getWidth();
		int h = inpix.getHeight();
		int px = inpix.getX();
		int py = inpix.getY();

		if (pix == null)
		{
			pix = new Pixels(w, h);
		}
		else
		{
			pix.setSize(w, h);
		}
		pix.setX(px);
		pix.setY(py);

		float hsbvals[] = new float[3];
		int ip[] = inpix.getPixels();
		int pred, pgreen, pblue, prgb;
		int op[] = pix.getPixels();
		for (int i=0; i<(w*h); i++)
		{
			prgb = ip[i];
			pred = ((prgb >> 16) & 0xff) * radj / 100;
			pgreen = ((prgb >> 8) & 0xff) * gadj / 100;
			pblue = (prgb & 0xff) * badj / 100;

			Color.RGBtoHSB(
				(pred > 255 ? 255 : pred),
				(pgreen > 255 ? 255 : pgreen),
				(pblue > 255 ? 255 : pblue),
				hsbvals);

			hsbvals[0] = hsbvals[0] * hadj / 100;
			hsbvals[1] = hsbvals[1] * sadj / 100;
			hsbvals[2] = hsbvals[2] * bradj / 100;

			op[i] = Color.HSBtoRGB(
				(hsbvals[0] > 1 ? 1 : hsbvals[0]),
				(hsbvals[1] > 1 ? 1 : hsbvals[1]),
				(hsbvals[2] > 1 ? 1 : hsbvals[2]));
		}

		return pix;
	}
}
