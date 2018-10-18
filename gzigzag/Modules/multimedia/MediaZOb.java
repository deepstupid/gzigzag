/*
MediaZOb.java
 *
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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

package org.gzigzag.module.multimedia;
import org.gzigzag.module.*;
import org.gzigzag.*;
import java.awt.*;

/**
 */

public abstract class MediaZOb implements ZOb
{
	public static final String rcsid = "$Id: MediaZOb.java,v 1.7 2001/03/05 08:44:14 deetsay Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

	protected ZZCell cell = null;
	protected ZZCell callFrom = null;

	public void setMacro(ZZCell cf) { callFrom = cf; }

	static private MediaZOb nullZOb = new Default();
	static private MediaMath mediaMath = new MediaMath();

	/**
	 *	This is the readParams method, defined in the ZOb interface.
	 *	Unlike in most ZObs, this method doesn't read parameters from
	 *	the structure: it only sets the cell for reading the parameters later.
	 */
	public String readParams(ZZCell c) { cell = c; return ""; }

	static public MediaZOb newMediaZObNoDefault(ZZCell c)
	{
		if (c == null) return nullZOb;

		c = c.h("d.clone");

		ZZCell cc;
		MediaZOb mz;

		/*
		 *	psssssst!.... this is a secret feature:
		 *	you can show images from the old-fashioned
		 *	file:// or http:// protocols... :-)
		 */
		try
		{
			if (c.getText().indexOf("://") >= 0)
			{
				mz = new image();
				mz.readParams(c);
				return mz;
			}
		}
		catch (Exception e) {}

		cc = c.s("d.1");

		if (cc == null) return null;

		ZZCell macroCell = cc;
		cc = macroCell.h("d.clone");
		if (macroCell.equals(cc)) macroCell = null;

		String t = cc.getText();
		int x;

		/*
		 *	This is an unnecessary check just because
		 *	you get some errors if you try newZOb() on an URL :-)
		 */
		if (t.indexOf("://") >= 0) return null;

		x = t.indexOf('.');
		if (x > 1) try
		{
			mz = (MediaZOb)ZZModule.getModule(t.substring(0, x)).newZOb(t.substring(x + 1));
			mz.readParams(cc.s("d.1"));
			if (macroCell != null) mz.setMacro(macroCell);
			return mz;
		}
		catch (Exception e) {}

		/*
		 *	Mathematical functions are hidden inside MediaMath..
		 */
		if (t.equals("+")) { mz = mediaMath.new add(); mz.readParams(cc.s("d.1")); return mz; }
		if (t.equals("&")) { mz = mediaMath.new and(); mz.readParams(cc.s("d.1")); return mz; }
		if (t.equals("*")) { mz = mediaMath.new mul(); mz.readParams(cc.s("d.1")); return mz; }
		if (t.equals("|")) { mz = mediaMath.new or(); mz.readParams(cc.s("d.1")); return mz; }
		if (t.equals("%")) { mz = mediaMath.new xor(); mz.readParams(cc.s("d.1")); return mz; }

		return null;
	}

	static public MediaZOb newMediaZOb(ZZCell c)
	{
		MediaZOb mz = newMediaZObNoDefault(c);
		if (mz == null)
		{
			mz = new Default();
			mz.readParams(c);
		}
		return mz;
	}

	/**
	 *	Find a parameter of this cell from the ZZ-structure, and
	 *	return it as a MediaZOb.
	 *
	 *	@param name is the name of the parameter that you hope to find.
	 */
	protected MediaZOb readParam(String name)
	{
		MediaZOb mz = null;

		if (cell == null) return nullZOb;

		mz = readParam(name, cell, null);

		return (mz == null ? nullZOb : mz);
	}

	/**
	 *	Recursive part of the previous method
	 */
	private MediaZOb readParam(String name, ZZCell key, ZZCell first)
	{
		if ((key == null) || key.equals(first)) return null;

		if (first == null) first = key;

		if ((name != null) && (name.equals(key.getText())))
		{
			ZZCell value = key.s("d.1");

			//-------------------------------------------------
			// An acceptable way of passing macro parameters?

			if ((value != null) && (value.getText().equals("param")))
			{
				MediaZOb mz = null;
				ZZCell parname = value.s("d.1");
				if (parname != null)
				{
					if (callFrom != null)
						mz = readParam(parname.getText(), callFrom.s("d.1"), null);

					if (mz == null)
						mz = newMediaZOb(parname.s("d.1"));

					return mz;
				}
			}
			//-------------------------------------------------
			return newMediaZOb(value);
		}

		return readParam(name, key.s("d.2"), first);
	}

	/**
	 *	Contents of cell as text.
	 *
	 *	@param def	Default value.
	 */
	public String getText(String def)
	{
		return (cell == null ? def : cell.getText());
	}

	/**
	 *	Contents of cell as integer.
	 *
	 *	@param def	Default value.
	 */
	public int getInt(int def)
	{
		String t = getText("");
		// try to parse hex
		try { if (t.substring(0, 2).equals("0x")) return Integer.parseInt(t.substring(2), 16); }
		catch (Exception e) {}
		// try to parse decimal
		try { return Integer.parseInt(t); } catch (Exception e) {}

		return def;
	}

	public abstract Pixels getPixels();
}

// Local variables:
//   tab-width: 4
// End:
