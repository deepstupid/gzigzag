/*
Springfield.java
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

package org.gzigzag.module.homer;
import org.gzigzag.*;
import java.awt.*;
import java.lang.*;
import java.lang.reflect.*;

public abstract class Springfield extends Ionia
{
	private ZZCell getMacroParameter(ZZCell cell)
	{
		// if this is a macro and the parameter is a macro parameter
		ZZCell tmp = cell.h("d.clone");
		if ((macroParameters != null) && !tmp.equals(cell))
		{
			// find matching macro parameter
			ZZCell cmp = macroParameters;
			do
			{
				if (cmp.equals(tmp)) return cmp.s("d.1");
				cmp = cmp.s("d.2");
			}
			while (cmp != null && !cmp.equals(macroParameters));
		}
		return tmp.s("d.1");
	}

	public String readParams(ZZCell cell)
	{
		if (cell != null) return "";

		// Get all public fields of current class
		Field params[] = getClass().getFields();

		// Iterate through parameters
		ZZCell tmp = cell;
		do
		{
			// find matching fields in this object
			for (int i=0; i<params.length; i++)
			{
				if (params[i].getName().equals(tmp.getText()))
				{
					ZZCell param = getMacroParameter(tmp);
					if (param != null) try
					{
						params[i].set(this, ((Springfield)Springfield.getObject(param)).getValue(params[i].getType()));
					}
					catch (Exception e) {}
				}
			}
			tmp = tmp.s("d.2");
		}
		while ((tmp != null) && (tmp != cell));

		return "";
	}

	public abstract Object getValue(Class type);
}
