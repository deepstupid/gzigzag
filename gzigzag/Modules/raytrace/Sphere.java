/*
Sphere.java
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

public class Sphere extends Primitive
{
	public static final String rcsid = "$Id: Sphere.java,v 1.3 2001/06/06 10:16:45 deetsay Exp $";

	public static final double minDistance = 0.001;

	private double Px, Py, Pz, dx, dy, dz;

	public Sphere(Model m) { super(m); }

	private Ray osRay = new Ray();

	public boolean intersects(Ray ray)
	{
		Matrix.multiply(ray.getP(), worldToObject, osRay.getP());
		Matrix.multiply(ray.getd(), worldToObject, osRay.getd());
		double P[] = osRay.getP().getdata();
		double d[] = osRay.getd().getdata();

		Px = P[0]; Py = P[1]; Pz = P[2];
		dx = d[0]; dy = d[1]; dz = d[2];

		double A = (dx*dx + dy*dy + dz*dz);
		double B = 2 * ((Px*dx) + (Py*dy) + (Pz*dz));
		double D = (B * B) - (4 * A * (Px*Px + Py*Py + Pz*Pz - 1));

		if (D <= 0) return false;

		double sq = Math.sqrt(D);
		double root1 = (-B + sq) / (2 * A);
		double root2 = (-B - sq) / (2 * A);

		if (root1 < root2)
			if (root1 >= minDistance)
				distance = root1;
			else
				if (root2 >= minDistance)
					distance = root2;
				else
					return false;
		else
			if (root2 >= minDistance)
				distance = root2;
			else
				if (root1 >= minDistance)
					distance = root1;
				else
					return false;

		return true;
	}

	private Matrix osNormal = new Matrix(1, 4, new double[] { 0.0, 0.0, 0.0, 0.0 });

	public void getNormalDirection(Ray ray)
	{
		double d[] = osNormal.getdata();
		d[0] = Px + (distance * dx);
		d[1] = Py + (distance * dy);
		d[2] = Pz + (distance * dz);
		Matrix.multiply(osNormal, normalToWorld, ray.getd());
//		return new Ray(hx, hy, hz, hx, hy, hz);
	}

	public static void readModel(ZZCell c, Model m)
	{
		m.addPrimitive(new Sphere(m));
	}
}
