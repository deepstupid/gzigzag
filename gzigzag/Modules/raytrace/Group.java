/*
Group.java
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

package org.gzigzag.module.raytrace;
import org.gzigzag.*;
import java.awt.*;
import java.util.*;

public class Group
{
	public static final String rcsid = "$Id: Group.java,v 1.3 2001/06/06 10:16:45 deetsay Exp $";

	public static void readModel(ZZCell c, Model m)
	{
		if (c == null) return;

		ZZCell d = c.s("d.1");
		if (d == null) return;

		Matrix objectToWorld = new Matrix(m.getObjectToWorld());
		Matrix normalToWorld = new Matrix(m.getNormalToWorld());
		Matrix worldToObject = new Matrix(m.getWorldToObject());
		int glow = m.getGlow();
		int reflection = m.getReflection();
		int refraction = m.getRefraction();
		ZZCell e = d;
		do
		{
			m.readModel(d);

			m.getObjectToWorld().set(objectToWorld);
			m.getNormalToWorld().set(normalToWorld);
			m.getWorldToObject().set(worldToObject);
			m.setGlow(glow);
			m.setReflection(reflection);
			m.setRefraction(refraction);

			d = d.s("d.2");
		}
		while (d != null && !d.equals(e));
	}
}
