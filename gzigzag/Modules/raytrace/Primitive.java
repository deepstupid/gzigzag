/*
Primitive.java
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

public abstract class Primitive extends Traceable
{
	public static final String rcsid = "$Id: Primitive.java,v 1.3 2001/06/06 10:16:45 deetsay Exp $";

	protected double distance;
	public double getDistance() { return distance; }

	private int glow;
	public int getGlow() { return glow; }

	private int reflection;
	public int getReflection() { return reflection; }

	private int refraction;
	public int getRefraction() { return refraction; }

	public Primitive(Model m)
	{
		super(m);
		glow = m.getGlow();
		reflection = m.getReflection();
		refraction = m.getRefraction();
	}

	/**
	 *	return true if ray intersects with this primitive. else false.
	 *	<b>must set distance!</b>
	 *
	 *	@param ray is a ray, and we're interested to know if it intersects with (hits) this primitive.
	 */
	public abstract boolean intersects(Ray ray);

	public abstract void getNormalDirection(Ray ray);
}
