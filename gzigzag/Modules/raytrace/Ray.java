/*
Ray.java
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

public class Ray
{
	public static final String rcsid = "$Id: Ray.java,v 1.2 2001/05/29 13:55:03 deetsay Exp $";

	private Matrix P = new Matrix(1, 4, new double[] { 0.0, 0.0, 0.0, 1.0 });

	private Matrix d = new Matrix(1, 4, new double[] { 0.0, 0.0, 0.0, 0.0 });

	public Matrix getP() { return P; }

	public Matrix getd() { return d; }

	public Ray() {}

	public Ray(Matrix PP) throws IndexOutOfBoundsException
	{
		P.set(PP);
	}

	public Ray(Matrix PP, Matrix dd)
	{
		P.set(PP); d.set(dd);
	}

	public Ray(double x, double y, double z)
	{
		double Pdata[] = P.getdata();
		Pdata[0] = x; Pdata[1] = y; Pdata[2] = z;
	}

	public Ray(double Px, double Py, double Pz, double dx, double dy, double dz)
	{
		double data[] = P.getdata();
		data[0] = Px; data[1] = Py; data[2] = Pz;
		data = d.getdata();
		data[0] = dx; data[1] = dy; data[2] = dz;
	}

	public Ray(Ray r) { set(r); }

	public void set(Ray ray) { P.set(ray.getP()); d.set(ray.getd()); }

	public double length()
	{
		double ddata[] = d.getdata();
		double x = ddata[0], y = ddata[1], z = ddata[2];
		return Math.sqrt((x*x) + (y*y) + (z*z));
	}

	public void normalize()
	{
		double ddata[] = d.getdata();
		double x = ddata[0], y = ddata[1], z = ddata[2];
		double len = Math.sqrt((x*x) + (y*y) + (z*z));
		if (len == 0) return;
		ddata[0] = x / len; ddata[1] = y / len; ddata[2] = z / len;
	}
}
