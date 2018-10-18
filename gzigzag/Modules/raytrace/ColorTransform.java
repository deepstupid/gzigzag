/*
ColorTransform.java
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

public abstract class ColorTransform
{
	public static final String rcsid = "$Id: ColorTransform.java,v 1.2 2001/05/29 13:55:03 deetsay Exp $";

	public static void setGlow(ZZCell c, Model m)
	{
		if (c == null) return;

		ZZCell d = c.s("d.2");
		if (d != null) try { m.setGlow(Integer.parseInt(d.getText(), 16)); } catch (Exception e) {}

		ZZCell zzc = c.s("d.1");
		if (zzc != null) m.readModel(zzc);
	}

	public static void setReflection(ZZCell c, Model m)
	{
		if (c == null) return;

		ZZCell d = c.s("d.2");
		if (d != null) try { m.setReflection(Integer.parseInt(d.getText(), 16)); } catch (Exception e) {}

		ZZCell zzc = c.s("d.1");
		if (zzc != null) m.readModel(zzc);
	}

	public static void setRefraction(ZZCell c, Model m)
	{
		if (c == null) return;

		ZZCell d = c.s("d.2");
		if (d != null) try { m.setRefraction(Integer.parseInt(d.getText(), 16)); } catch (Exception e) {}

		ZZCell zzc = c.s("d.1");
		if (zzc != null) m.readModel(zzc);
	}
}
