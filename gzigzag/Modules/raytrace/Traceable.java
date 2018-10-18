/*
Traceable.java
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

public abstract class Traceable
{
	public static final String rcsid = "$Id: Traceable.java,v 1.3 2001/06/06 10:18:15 deetsay Exp $";

	protected Matrix normalToWorld = new Matrix(4, 4);
	public Matrix getNormalToWorld() { return normalToWorld; }
	protected Matrix worldToObject = new Matrix(4, 4);
	public Matrix getWorldToObject() { return worldToObject; }
/*
	private void printar(Matrix p)
	{
		double d[] = p.getdata();
		String poo = "";
		for (int i=0; i<16; i++)
			poo += " " + d[i];
		System.out.println(poo);
	}
*/
	public Traceable(Model m)
	{
		normalToWorld.set(m.getNormalToWorld());
		worldToObject.set(m.getWorldToObject());
/*
		Matrix mx = new Matrix(4, 4);
		Matrix.multiply(m.getWorldToObject(), m.getObjectToWorld(), mx);
		printar(mx);
*/
	}
}
