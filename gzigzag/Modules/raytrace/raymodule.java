/*
raymodule.java
 *
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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

package org.gzigzag.module;
import org.gzigzag.module.raytrace.*;
import org.gzigzag.*;
import java.awt.*;

/**
 *	Raytrace module
 */
public class raymodule
{
	static public final String rcsid = "$Id: raymodule.java,v 1.2 2001/05/29 13:55:03 deetsay Exp $";

	static public ZZModule module = new ZZModule()
	{
		public class RayFlobView implements FlobView, ZOb
		{
			public String readParams(ZZCell c) { return ""; }

			private org.gzigzag.module.raytrace.RayFlobFactory rff
				= new org.gzigzag.module.raytrace.RayFlobFactory(256, 256);

			public void raster(FlobSet into, FlobFactory fact, ZZCell view,
				java.lang.String[] dims, ZZCell accursed)
			{
				rff.placeFlob(into, accursed, accursed, 1, 0, 0, 1, 0, 0);
			}
		}

		public ZOb newZOb(String id)
		{
			if (id.equals("factory")) { return new org.gzigzag.module.raytrace.RayFlobFactory(); }
			if (id.equals("view")) { return new RayFlobView(); }
			return null;
		}
	};
}
