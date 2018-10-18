/*
mm.java
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

package org.gzigzag.module;
import org.gzigzag.module.multimedia.*;
import org.gzigzag.*;
import java.awt.*;

/**
 *	Module for multimedia stuff.
 */
public class mm
{
	static public final String rcsid = "$Id: mm.java,v 1.3 2001/03/05 08:44:14 deetsay Exp $";

	static public ZZModule module = new ZZModule()
	{
		public ZOb newZOb(String id)
		{
			try
			{
				return (ZOb)Class.forName("org.gzigzag.module.multimedia." + id).newInstance();
			}
			catch (Exception e)
			{
				// ZZLogger.exc(e);
				return null;
			}
		}
	};
}
