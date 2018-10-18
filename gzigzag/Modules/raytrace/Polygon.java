/*
Polygon.java
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

public class Polygon extends Primitive
{
	public static final String rcsid = "$Id: Polygon.java,v 1.3 2001/06/06 10:16:45 deetsay Exp $";

	public static final double minDistance = 0.001;

	public Polygon(Model m) { super(m); }

	private double Px, Py, Pz, dx, dy, dz;

	private Ray osRay = new Ray();

	public boolean intersects(Ray ray)
	{
		Matrix.multiply(ray.getP(), worldToObject, osRay.getP());
		Matrix.multiply(ray.getd(), worldToObject, osRay.getd());
		double P[] = osRay.getP().getdata();
		double d[] = osRay.getd().getdata();

		Px = P[0]; Py = P[1]; Pz = P[2];
		dx = d[0]; dy = d[1]; dz = d[2];

		if (dz == 0) return false; // polygon is parallel to eye

		distance = -Pz / dz;
		double hx = Px + (distance * dx);
		double hy = Py + (distance * dy);

		if (hx < 0 || hy < 0 || hx + hy > 1 || distance < minDistance) return false;

		return true;
	}

	private Matrix osNormal = new Matrix(1, 4, new double[] { 0.0, 0.0, 0.0, 0.0 });

	public void getNormalDirection(Ray ray)
	{
		osNormal.getdata()[2] = -dz;
		Matrix.multiply(osNormal, normalToWorld, ray.getd());
	}

	public static void readModel(ZZCell c, Model m)
	{
		m.addPrimitive(new Polygon(m));
	}
}
