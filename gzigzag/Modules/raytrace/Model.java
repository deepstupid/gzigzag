/*
Model.java
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
import java.awt.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import org.gzigzag.*;
import org.gzigzag.module.homer.*;

public class Model extends Euboia
{
	public static final String rcsid = "$Id: Model.java,v 1.2 2001/06/06 10:16:45 deetsay Exp $";

	private Object parameters[] = new Object[] { null, this };

	private Vector lights = new Vector();
	public void addLight(Light l) { lights.addElement(l); }
	public Vector getLights() { return lights; }

	private Vector primitives = new Vector();
	public void addPrimitive(Primitive p) { primitives.addElement(p); }
	public Vector getPrimitives() { return primitives; }

	private Matrix objectToWorld;
	public Matrix getObjectToWorld() { return objectToWorld; }
	private Matrix normalToWorld;
	public Matrix getNormalToWorld() { return normalToWorld; }
	private Matrix worldToObject;
	public Matrix getWorldToObject() { return worldToObject; }

	private int glow;
	public void setGlow(int c) { glow = c; }
	public int getGlow() { return glow; }

	private int reflection;
	public void setReflection(int c) { reflection = c; }
	public int getReflection() { return reflection; }

	private int refraction;
	public void setRefraction(int c) { refraction = c; }
	public int getRefraction() { return refraction; }

	public Model()
	{
		try
		{
			Class params[] = new Class[] { Class.forName("org.gzigzag.ZZCell"), Class.forName("org.gzigzag.module.raytrace.Model") };
			put("sphere", Class.forName("org.gzigzag.module.raytrace.Sphere").getMethod("readModel", params));
			put("polygon", Class.forName("org.gzigzag.module.raytrace.Polygon").getMethod("readModel", params));

			put("move", Class.forName("org.gzigzag.module.raytrace.Transform").getMethod("translate", params));
			put("scale", Class.forName("org.gzigzag.module.raytrace.Transform").getMethod("scale", params));
			put("rotate", Class.forName("org.gzigzag.module.raytrace.Transform").getMethod("rotate", params));

			put("group", Class.forName("org.gzigzag.module.raytrace.Group").getMethod("readModel", params));

			put("glow", Class.forName("org.gzigzag.module.raytrace.ColorTransform").getMethod("setGlow", params));
			put("reflection", Class.forName("org.gzigzag.module.raytrace.ColorTransform").getMethod("setReflection", params));
			put("refraction", Class.forName("org.gzigzag.module.raytrace.ColorTransform").getMethod("setRefraction", params));

			put("light", Class.forName("org.gzigzag.module.raytrace.Light").getMethod("readModel", params));
		}
		catch (Exception e) { ZZLogger.exc(e); }
	}

	/**
	 *	Prepares the Model for reading a model from
	 *	the Zigzag-structure (with the readModel() calls).
	 */
	public void restart()
	{
		glow = 0;
		reflection = 0;
		refraction = 0;
		objectToWorld = new Matrix(4, 4);
		normalToWorld = new Matrix(4, 4);
		worldToObject = new Matrix(4, 4);
		lights.removeAllElements();
		primitives.removeAllElements();
	}

	public void readModel(ZZCell c)
	{
		invoke(c, parameters);
	}
}
