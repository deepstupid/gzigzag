/*
Light.java
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

public class Light
{
	public static final String rcsid = "$Id: Light.java,v 1.3 2001/06/06 10:16:45 deetsay Exp $";

	protected Matrix wsLight = new Matrix(1, 4);

	public Light(Model m)
	{
		Matrix.multiply(new Matrix(1, 4, new double[] { 0.0, 0.0, 0.0, 1.0 }), m.getObjectToWorld(), wsLight);
	}

	public double[] getdata() { return wsLight.getdata(); }

	public static void readModel(ZZCell c, Model m)
	{
		m.addLight(new Light(m));
	}
}
