/*
Default.java
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
 *	Written by Tero Mäyränen
 */

package org.gzigzag.module.multimedia;
import org.gzigzag.*;
import java.awt.*;
import java.awt.image.*;

public class Default extends MediaZOb
{
	public static final String rcsid = "$Id: Default.java,v 1.3 2001/02/22 15:31:13 deetsay Exp $";

	private Pixels pix = new Pixels(1, 1);

	public Pixels getPixels()
	{
		pix.getPixels()[0] = 0xff000000;
		return pix;
	}
}
