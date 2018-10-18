/*
MediaFlobView.java
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
 * Written by Tero Mäyränen
 */

package org.gzigzag.module.multimedia;
import org.gzigzag.module.*;
import org.gzigzag.*;
import java.awt.*;

public class MediaFlobView extends Default implements FlobView
{
	public static final String rcsid = "$Id: MediaFlobView.java,v 1.1 2001/03/05 08:44:14 deetsay Exp $";

	private MediaFlobFactory mff = new MediaFlobFactory();

	public void raster(FlobSet into, FlobFactory fact, ZZCell view, java.lang.String[] dims,
		ZZCell accursed)
	{
		mff.placeFlob(into, accursed, accursed, 1, 0, 0, 1, 0, 0);
	}
}
