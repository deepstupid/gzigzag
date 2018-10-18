/*
sin.java
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

/**
 */

public class sin extends Default
{
	public static final String rcsid = "$Id: sin.java,v 1.1 2001/03/05 08:44:14 deetsay Exp $";

	public int getInt(int def)
	{
		int a = ((int)System.currentTimeMillis()) >> 7;
		return ((int)(Math.sin(((double)a * readParam("speed").getInt(64)) / (Math.PI * 100))
			* readParam("amplitude").getInt(16))) + readParam("bias").getInt(64);
	}
}
