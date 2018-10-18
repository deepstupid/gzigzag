/*
PolygonRenderer.java
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
 *	unfinished..
 */

public class PolygonRenderer extends MediaZOb
{
	public static final String rcsid = "$Id: PolygonRenderer.java,v 1.1 2001/03/05 08:44:14 deetsay Exp $";

	private Pixels pix;

	public Pixels getPixels()
	{
		int w = readParam("width").getInt(256);
		int h = readParam("height").getInt(256);
		if (pix == null)
		{
			pix = new Pixels(w, h);
		}
		else
		{
			pix.setSize(w, h);
		}
		int p[] = pix.getPixels();
		for (int i=0; i<(w*h); i++) p[i] = 0xff000000;
	
		/* sort polygons */
		
		/* render */

		return pix;
	}
}
