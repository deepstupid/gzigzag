/*
Raytracer.java
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

/**
 *	This is the "main" class of the raytracer system.
 */

public class Raytracer
{
	public static final String rcsid = "$Id: Raytracer.java,v 1.3 2001/06/06 10:16:45 deetsay Exp $";

	private static final int SUPERSAMPLESIZE = 2;
	private static final int SUPERSAMPLESIZE2 = SUPERSAMPLESIZE*SUPERSAMPLESIZE;

	private int pixels[];

	private double deltax, deltay;
	private Ray rayx, rayy;
	private int width;

	private Model model = new Model();
	private Vector primitives;
	private Vector lights;

	public Raytracer()
	{
		primitives = model.getPrimitives();
		lights = model.getLights();
	}

	public void prepare(int wid, int height, ZZCell modelCell)
	{
		model.restart();

		/*
		 *	This call should build the primitives and lights -vectors according to the
		 *	model data in the structure.
		 */
		model.readModel(modelCell);

		width = wid;
		pixels = new int[width];

		deltax = -2.0/(width*SUPERSAMPLESIZE);
		deltay = -2.0/(height*SUPERSAMPLESIZE);

		rayx = new Ray();
		rayy = new Ray(0, 0, 0, 1, 1, 1.5);
	}

	public int[] nextLine()
	{
		rayx.set(rayy);
		double ddata[] = rayx.getd().getdata();
		int supr, supg, supb, supc;
		for (int x=0; x<width; x++)
		{
			supr = 0; supg = 0; supb = 0;
			for (int supy=0; supy<SUPERSAMPLESIZE; supy++)
			{
				for (int supx=0; supx<SUPERSAMPLESIZE; supx++)
				{
					supc = trace(rayx, 3);
					supr += supc & 0xff0000;
					supg += supc & 0xff00;
					supb += supc & 0xff;
					ddata[0] += deltax;
				}
				ddata[0] -= deltax*SUPERSAMPLESIZE;
				ddata[1] += deltay;
			}
			ddata[0] += deltax*SUPERSAMPLESIZE;
			ddata[1] -= deltay*SUPERSAMPLESIZE;
			pixels[x] = 0xff000000
				| ((supr / SUPERSAMPLESIZE2) & 0xff0000)
				| ((supg / SUPERSAMPLESIZE2) & 0xff00)
				| ((supb / SUPERSAMPLESIZE2) & 0xff);
		}
		rayy.getd().getdata()[1] += deltay*SUPERSAMPLESIZE;
		return pixels;
	}

	private int mulcolor(int color1, int color2)
	{
		int r1 = (color1 >> 16) & 0xff,
			r2 = (color2 >> 16) & 0xff,
			g1 = (color1 >> 8) & 0xff,
			g2 = (color2 >> 8) & 0xff,
			b1 = color1 & 0xff,
			b2 = color2 & 0xff;
		r1 = (r1 * r2) >> 8;
		g1 = (g1 * g2) >> 8;
		b1 = (b1 * b2) >> 8;
		return (r1 << 16) | (g1 << 8) | b1;
	}

	private int trace(Ray ray, int iteration)
	{
		if (iteration == 0) return 0;

		Primitive p;
		Primitive near = null;
		for (int i=0; i<primitives.size(); i++)
		{
			p = (Primitive)primitives.elementAt(i);
			if (p.intersects(ray) && ((near == null) || (p.getDistance() < near.getDistance())))
			{
				near = p;
			}
		}
		if (near == null) return 0;

		// Hit-point and normal in world-space
		double P[] = ray.getP().getdata();
		double d[] = ray.getd().getdata();
		double dx = d[0];
		double dy = d[1];
		double dz = d[2];
		double distance = near.getDistance();
		double hx = P[0] + distance * dx;
		double hy = P[1] + distance * dy;
		double hz = P[2] + distance * dz;
		Ray normal = new Ray(hx, hy, hz);
		near.getNormalDirection(normal);

		/*
		 *	Direction of view-ray (for reflection-calculation) should be
		 *	opposite to the actual view-ray (= pointing towards the eye)...
		 */
		Ray view = new Ray(hx, hy, hz, -dx, -dy, -dz);

		//	Normalize them both: Neither of them are definitely not already normalized :-)
		view.normalize();
		normal.normalize();

		double vd[] = view.getd().getdata();
		double nd[] = normal.getd().getdata();

		/*
		 *	Calculate reflection-ray:
		 *	Project view-ray on normal-ray, double that, subtract view-ray, normalize!
		 */
		double vdx = vd[0], vdy = vd[1], vdz = vd[2], ndx = nd[0], ndy = nd[1], ndz = nd[2],
			dp = 2 * ((vdx * ndx) + (vdy * ndy) + (vdz * ndz));
		Ray reflection = new Ray(hx, hy, hz, (2*dp*ndx)-vdx, (2*dp*ndy)-vdy, (2*dp*ndz)-vdz);
		reflection.normalize();

		//	Calculate reflection (stuff reflected from other objects).
		int refl = near.getReflection();
		if (refl != 0) refl = mulcolor(refl, trace(reflection, iteration - 1));

		//	Calculate "phong" illumination from lightsources.
		int shadow = 0;
		boolean shadowed;
		double l[];
		Ray lightRay = new Ray(hx, hy, hz);
		double ld[] = lightRay.getd().getdata();
		for (int i=0; i<lights.size(); i++)
		{
			//	Get ray from hitpoint to lightsource
			l = ((Light)lights.elementAt(i)).getdata();
			ld[0] = l[0] - hx;
			ld[1] = l[1] - hy;
			ld[2] = l[2] - hz;

			shadowed = false;
			//	Check if any primitives are on the way
			for (int j=0; (j<primitives.size()) && (!shadowed); j++)
			{
				p = (Primitive)primitives.elementAt(j);
				shadowed |= (p.intersects(lightRay) && (p.getDistance() < 1.0));
			}
			//	In case there were no primitives blocking the lightsource, add some light
			if (!shadowed)
			{
				lightRay.normalize();
				shadow += lightAttenuation(reflection, lightRay);
			}
		}

		int refr = near.getRefraction();
		int refR = (refr >> 16) & 0xff;
		int refG = (refr >> 8) & 0xff;
		int refB = refr & 0xff;

		//	Make up some kind of rays for the refraction :-)
		nd = reflection.getd().getdata();
		ndx = nd[0]; ndy = nd[1]; ndz = nd[2];
		nd[0] = dx; nd[1] = dy; nd[2] = dz;
		reflection.normalize();
		vdx = nd[0]; vdy = nd[1]; vdz = nd[2];
		if (refR != 0)
		{
			nd[0] = vdx + 0.4 * ndx;
			nd[1] = vdy + 0.4 * ndy;
			nd[2] = vdz + 0.4 * ndz;
			refr = ((refR * (trace(reflection, iteration - 1) & 0xff0000)) >> 8) & 0xff0000;
		}
		if (refG != 0)
		{
			nd[0] = vdx + 0.35 * ndx;
			nd[1] = vdy + 0.35 * ndy;
			nd[2] = vdz + 0.35 * ndz;
			refr |= ((refG * (trace(reflection, iteration - 1) & 0xff00)) >> 8) & 0xff00;
		}
		if (refB != 0)
		{
			nd[0] = vdx + 0.3 * ndx;
			nd[1] = vdy + 0.3 * ndy;
			nd[2] = vdz + 0.3 * ndz;
			refr |= ((refR * (trace(reflection, iteration - 1) & 0xff)) >> 8) & 0xff;
		}

		int glow = near.getGlow();
		int r = (glow & 0xff0000) + (refl & 0xff0000) + (refr & 0xff0000) + (shadow << 16);
		if (r > 0xff0000) r = 0xff0000;
		int g = (glow & 0xff00) + (refl & 0xff00) + (refr & 0xff00) + (shadow << 8);
		if (g > 0xff00) g = 0xff00;
		int b = (glow & 0xff) + (refl & 0xff) + (refr & 0xff) + shadow;
		if (b > 0xff) b = 0xff;

		return r | g | b;
	}

	private Ray pong = new Ray(0, 0, 0);

	private int lightAttenuation(Ray view, Ray light)
	{
		double ld[] = light.getd().getdata();
		double vd[] = view.getd().getdata();
		double pd[] = pong.getd().getdata();
		pd[0] = ld[0] - vd[0]; pd[1] = ld[1] - vd[1]; pd[2] = ld[2] - vd[2];
		return (int)(255.0 / (1 + (pong.length()*4))) & 0xff;
	}
}
