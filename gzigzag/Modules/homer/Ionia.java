/*
Ionia.java
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

/*
 *	Homer wrote The Iliad around 800 B.C.
 *	The text-only version is around 800 Kb.
 *	It was originally released as a scrolltext and performed as a play.
 */

public abstract class Ionia implements ZOb
{
	protected ZZCell macroParameters = null;

	public static Class getClass(ZZCell cell)
	{
		if (cell == null) return null;

		ZZCell name = cell.h("d.clone");

		ZZCell type = name.s("d.1");
		if (type == null) return null;

		return getType(type.getText());
	}

	public static Ionia getObject(ZZCell cell) throws InstantiationException, IllegalAccessException
	{
		Class typeClass = getClass(cell);
		if (typeClass == null) return null;
		Ionia ion = (Ionia)typeClass.newInstance();

		ZZCell name = cell.h("d.clone");

		ZZCell type = name.s("d.1");
		if (type == null) return null;

		ZZCell master = type.h("d.clone");

		if (master.equals(type))
			ion.readParams(master.s("d.1"));

		else
			ion.readParams(master.s("d.1"), type.s("d.1"));

		return ion;
	}

	public static Class getType(String type) { return null; }

	public String readParams(ZZCell cell, ZZCell param)
	{
		macroParameters = param;
		return readParams(cell);
	}

	public String readParams(ZZCell cell) { return ""; }
}
