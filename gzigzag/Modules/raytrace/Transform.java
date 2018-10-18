/*
Transform.java
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

public abstract class Transform
{
	public static final String rcsid = "$Id: Transform.java,v 1.3 2001/06/06 10:16:45 deetsay Exp $";

	private static double x, y, z;

	private static void readXyz(ZZCell c)
	{
		ZZCell d = c.s("d.2");
		if (d != null)
		{
			try { x = (new Double(d.getText())).doubleValue(); } catch (Exception e) {}

			d = d.s("d.2");
			if (d != null)
			{
				try { y = (new Double(d.getText())).doubleValue(); } catch (Exception e) {}

				d = d.s("d.2");
				if (d != null)
				{
					try { z = (new Double(d.getText())).doubleValue(); } catch (Exception e) {}
				}
			}
		}
	}
/*
	private static void printar(Matrix p)
	{
		String poo = "";
		for (int i=0; i<16; i++)
			poo += " " + p.getdata()[i];
		System.out.println(poo);
	}
*/
	private static Matrix temp = new Matrix(4, 4);

	private static Matrix translate_temp = new Matrix(4, 4);
	private static Matrix rotatex_temp = new Matrix(4, 4);
	private static Matrix rotatey_temp = new Matrix(4, 4);
	private static Matrix rotatez_temp = new Matrix(4, 4);
	private static Matrix scale_temp = new Matrix(4, 4);

	private static void translate_sub(Matrix m, boolean dir)
	{
		double d[] = translate_temp.getdata();
		d[3] = x; d[7] = y; d[11] = z;
		if (dir) Matrix.multiply(m, translate_temp, temp);
		else Matrix.multiply(translate_temp, m, temp);
		m.set(temp);
	}

	public static void translate(ZZCell c, Model m)
	{
		if (c == null) return;
		ZZCell zzc = c.s("d.1");
		if (zzc == null) return;

		x = 0.0; y = 0.0; z = 0.0;
		readXyz(c);

		translate_sub(m.getObjectToWorld(), false);
//		translate_sub(m.getNormalToWorld(), false);
		x = -x; y = -y; z = -z;
		translate_sub(m.getWorldToObject(), true);
/*
		System.out.println("translate:");
		Matrix.multiply(m.getObjectToWorld(), m.getWorldToObject(), temp);
		printar(temp);
*/
		m.readModel(zzc);
	}

	private static void rotate_sub(Matrix m, boolean dir)
	{
		double d[], cos, sin;
		if (dir)
		{
			d = rotatex_temp.getdata();
			cos = Math.cos(x); sin = Math.sin(x);
			d[5] = cos; d[6] = -sin; d[9] = sin; d[10] = cos;
			Matrix.multiply(m, rotatex_temp, temp);

			d = rotatey_temp.getdata();
			cos = Math.cos(y); sin = Math.sin(y);
			d[0] = cos; d[2] = sin; d[8] = -sin; d[10] = cos;
			Matrix.multiply(temp, rotatey_temp, m);

			d = rotatez_temp.getdata();
			cos = Math.cos(z); sin = Math.sin(z);
			d[0] = cos; d[1] = -sin; d[4] = sin; d[5] = cos;
			Matrix.multiply(m, rotatez_temp, temp);
		}
		else
		{
			d = rotatex_temp.getdata();
			cos = Math.cos(x); sin = Math.sin(x);
			d[5] = cos; d[6] = -sin; d[9] = sin; d[10] = cos;
			Matrix.multiply(rotatex_temp, m, temp);

			d = rotatey_temp.getdata();
			cos = Math.cos(y); sin = Math.sin(y);
			d[0] = cos; d[2] = sin; d[8] = -sin; d[10] = cos;
			Matrix.multiply(rotatey_temp, temp, m);

			d = rotatez_temp.getdata();
			cos = Math.cos(z); sin = Math.sin(z);
			d[0] = cos; d[1] = -sin; d[4] = sin; d[5] = cos;
			Matrix.multiply(rotatez_temp, m, temp);
		}
		m.set(temp);
	}

	public static void rotate(ZZCell c, Model m)
	{
		if (c == null) return;
		ZZCell zzc = c.s("d.1");
		if (zzc == null) return;

		x = 0.0; y = 0.0; z = 0.0;
		readXyz(c);
		x *= Math.PI / 180; y *= Math.PI / 180; z *= Math.PI / 180;

		rotate_sub(m.getObjectToWorld(), false);
		rotate_sub(m.getNormalToWorld(), false);
		x = -x; y = -y; z = -z;
		rotate_sub(m.getWorldToObject(), true);
/*
		System.out.println("rotate:");
		Matrix.multiply(m.getObjectToWorld(), m.getWorldToObject(), temp);
		printar(temp);
*/
		m.readModel(zzc);
	}

	private static void scale_sub(Matrix m, boolean dir)
	{
		double d[] = scale_temp.getdata();
		d[0] = x; d[5] = y; d[10] = z;
		if (dir) Matrix.multiply(m, scale_temp, temp);
		else Matrix.multiply(scale_temp, m, temp);
		m.set(temp);
	}

	public static void scale(ZZCell c, Model m)
	{
		if (c == null) return;
		ZZCell zzc = c.s("d.1");
		if (zzc == null) return;

		x = 1.0; y = 1.0; z = 1.0;
		readXyz(c);

		if (x == 0) x = 1.0;
		if (y == 0) y = 1.0;
		if (z == 0) z = 1.0;

		scale_sub(m.getObjectToWorld(), false);
		x = 1.0/x; y = 1.0/y; z = 1.0/z;
		scale_sub(m.getNormalToWorld(), false);
		scale_sub(m.getWorldToObject(), true);
/*
		System.out.println("scale:");
		Matrix.multiply(m.getObjectToWorld(), m.getWorldToObject(), temp);
		printar(temp);
*/
		m.readModel(zzc);
	}
}
