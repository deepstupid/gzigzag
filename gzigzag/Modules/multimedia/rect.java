/*
rect.java
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
 * Written by Tero Mäyränen
 */

package org.gzigzag.module.multimedia;
import org.gzigzag.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class rect extends MediaZOb
{
	public static final String rcsid = "$Id: rect.java,v 1.4 2001/03/05 08:44:14 deetsay Exp $";

	private Pixels pix = null;

	private int fade(int x, int fg, int bg)
	{
		int y = 0xff-x;
		return 0xff000000
			| (((((fg & 0xff0000) * x) + ((bg & 0xff0000) * y)) >> 8) & 0xff0000)
			| (((((fg & 0xff00) * x) + ((bg & 0xff00) * y)) >> 8) & 0xff00)
			| (((((fg & 0xff) * x) + ((bg & 0xff) * y)) >> 8) & 0xff);
	}

	private int oldw=0, oldh=0, oldbg=0, oldfg=0, oldstarsize=0;
	private String oldmode="";

	public Pixels getPixels()
	{
		int w = readParam("width").getInt(64);
		int h = readParam("height").getInt(64);
		int bg = readParam("bg").getInt(0);
		int fg = readParam("fg").getInt(0xffffff);
		int starsize = readParam("size").getInt(16);
		if (starsize==0) starsize=1;
		String mode = readParam("fillmode").getText("solid");

		if (pix != null && oldw==w && oldh==h && oldbg==bg && oldfg==fg
			&& oldstarsize==starsize && mode.equals(oldmode))
		{
			return pix;
		}
		oldw=w; oldh=h; oldbg=bg; oldfg=fg; oldstarsize=starsize; oldmode=mode;

		if (pix == null)
		{
			pix = new Pixels(w, h);
		}
		else
		{
			pix.setSize(w, h);
		}

		int p[] = pix.getPixels();
		if (mode.equals("solid"))
		{
			for (int i=0; i<(w*h); i++)
			{
				p[i] = 0xff000000 | fg;
			}
		}
		if (mode.equals("starburst"))
		{
			// älä optiomoi, vaihda algoritmia
			int w2 = w >> 1;
			int h2 = h >> 1;
			int i = 0, j;
			for (int y=-h2; y<h-h2; y++)
			{
				for (int x=-w2; x<w-w2; x++)
				{
					j = ((int)Math.sqrt(x*x+y*y)*(256 / starsize));
					if (j > 0xff) j = 0xff;
					p[i++] = fade(j, bg, fg);
				}
			}
		}
		if (mode.equals("radial"))
		{
			int w2 = w >> 1;
			int h2 = h >> 1;
			int i = 0, j = w, i2 = w*h, j2 = (w*h)-w, k;
			for (int y=-h2; y<0; y++)
			{
				for (int x=-w2; x<0; x++)
				{
					k = ((int)(Math.atan(((double)x) / y) * 128 / Math.PI)) & 0x3f;
					p[i++] = fade(k, bg, fg);
					p[--j] = fade(k, fg, bg);
					p[--i2] = fade(k | 0x80, bg, fg);
					p[j2++] = fade(k | 0x80, fg, bg);
				}
				i += w2;
				j += w + w2;
				i2 -= w2;
				j2 -= w + w2;
			}
		}
		pix.setFrame(pix.getFrame()+1);
		return pix;
	}
}
