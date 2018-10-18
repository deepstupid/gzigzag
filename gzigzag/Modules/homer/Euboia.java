/*
Euboia.java
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
import java.awt.*;
import java.util.*;
import java.lang.reflect.*;
import org.gzigzag.*;

/**
 *	Euboia is one way to make programming languages with static calls.
 *	You can add and remove calls dynamically.
 *
 *	For a practical example, see Modules/raytrace/Model.java
 */

public class Euboia
{
	public static final String rcsid = "$Id: Euboia.java,v 1.1 2001/05/29 13:57:02 deetsay Exp $";

	protected Hashtable hashtable = new Hashtable();

	/**
	 *	Add a command to the language.
	 */
	public void put(String type, Method method) { hashtable.put(type, method); }

	/**
	 *	Remove a command from the language.
	 */
	public void remove(String type) { hashtable.remove(type); }

	/**
	 *	Invoke a command in the language.
	 */
	public void invoke(ZZCell name, Object params[])
	{
		if (name == null) return;
		ZZCell type = name.h("d.clone").s("d.1");
		if (type == null) return;
		Method method = (Method)hashtable.get(type.getText());
		if (method != null)
		{
			params[0] = type;
			try { method.invoke(null, params); } catch (Exception e)
			{
				ZZLogger.exc(e);
			}
		}
	}
}
