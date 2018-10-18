/*
MediaMath.java
 *
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
 *	Written by Tero Mäyränen
 */

package org.gzigzag.module.multimedia;
import org.gzigzag.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 *	MediaMath is a collection of MediaZObs that do mathematical functions.
 *	If at all possible, in the future this should be done with some actual
 *	mathematical language designed for GZigZag...
 */

public class MediaMath
{
	public static final String rcsid = "$Id: MediaMath.java,v 1.1 2001/03/05 08:44:14 deetsay Exp $";

	public abstract class MathZOb extends MediaZOb
	{
		protected Pixels pix = null;

		protected Vector oldframes = new Vector();

		/**
		 *	The inputs for these are just lists of MediaZObs connected on d.2,
		 *	unlike the normal way, which is a list of parameter names, which
		 *	in turn are connected to ZObs on d.1........
		 */
		protected Vector readInputPixels()
		{
			Vector v = new Vector();
			Pixels inpix;
			ZZCell d = cell;
			do
			{
				inpix = MediaZOb.newMediaZOb(d).getPixels();
				v.addElement(inpix);
				d = d.s("d.2");
			}
			while ((d != null) && (!d.equals(cell)));
			return v;
		}

		protected Vector readInputZObs()
		{
			Vector v = new Vector();
			MediaZOb mz;
			ZZCell d = cell;
			do
			{
				mz = MediaZOb.newMediaZOb(d);
				v.addElement(mz);
				d = d.s("d.2");
			}
			while ((d != null) && (!d.equals(cell)));
			return v;
		}

		/**
		 *	Allocate enough pixels to fit all inputs in their correct coordinate positions.
		 */
		protected void createPix(Vector inputs)
		{
			if (inputs.size() < 1)
			{
				pix = new Pixels(1, 1);
				pix.getPixels()[0] = 0xff000000;
				return;
			}

			Pixels inpix = (Pixels)inputs.elementAt(0);
			int w = inpix.getWidth(), h = inpix.getHeight();
			int x = inpix.getX(), y = inpix.getY();

			for (int i=1; i<inputs.size(); i++)
			{
				inpix = (Pixels)inputs.elementAt(i);
				if (inpix.getX() < x)
				{
					w += x-inpix.getX();
					x = inpix.getX();
				}
				if (inpix.getY() < y)
				{
					h += y-inpix.getY();
					y = inpix.getY();
				}
				if ((inpix.getWidth() + inpix.getX()) > (w + x))
				{
					w = inpix.getWidth() + inpix.getX() - x;
				}
				if ((inpix.getHeight() + inpix.getY()) > (h + y))
				{
					h = inpix.getHeight() + inpix.getY() - y;
				}
			}
			if (pix == null)
			{
				pix = new Pixels(w, h);
			}
			else
			{
				pix.setSize(w, h);
			}
			pix.setX(x);
			pix.setY(y);
		}
	}

	public class add extends MathZOb
	{
		public int getInt(int def)
		{
			Vector inputs = readInputZObs();
			if (inputs.size() < 1) return def;
			int S = 0;
			for (int i=0; i<inputs.size(); i++) S += ((MediaZOb)inputs.elementAt(i)).getInt(0);
			return S;
		}

		public Pixels getPixels()
		{
			Vector inputs = readInputPixels();

			Pixels inpix;
/*			if (pix != null && inputs.size() == oldframes.size())
			{
				boolean dirty = false;
				for (int i=0; (i<inputs.size()) && (dirty==false); i++)
				{
					inpix = (Pixels)inputs.elementAt(i);
					if (inpix.getFrame() != ((Integer)oldframes.elementAt(i)).intValue())
						dirty = true;
				}
				if (!dirty) return pix;
			}
			oldframes.removeAllElements();
			for (int i=0; i<inputs.size(); i++)
			{
				oldframes.addElement(new Integer(((Pixels)inputs.elementAt(i)).getFrame()));
			}
*/
			createPix(inputs);

			int p[] = pix.getPixels(), ip[];

			for (int i=0; i<(pix.getWidth()*pix.getHeight()); i++) p[i] = 0xff000000;

			int pi, ipi;
			int pp, ipp;
			int r, g, b;
			for (int i=0; i<inputs.size(); i++)
			{
				inpix = (Pixels)inputs.elementAt(i);
				ip = inpix.getPixels();
				ipi = 0;
				pi = ((inpix.getY() - pix.getY()) * pix.getWidth()) + inpix.getX() - pix.getX();
				for (int y=0; y<inpix.getHeight(); y++)
				{
					for (int x=0; x<inpix.getWidth(); x++)
					{
						ipp = ip[ipi++];
						pp = p[pi];
						r = (pp & 0xff0000) + (ipp & 0xff0000);
						if (r > 0xff0000) r = 0xff0000;
						g = (pp & 0xff00) + (ipp & 0xff00);
						if (g > 0xff00) g = 0xff00;
						b = (pp & 0xff) + (ipp & 0xff);
						if (b > 0xff) b = 0xff;
						p[pi++] = 0xff000000 | r | g | b;
					}
					pi += pix.getWidth() - inpix.getWidth();
				}
			}
			pix.setFrame(pix.getFrame()+1);
			return pix;
		}
	}

	public class and extends MathZOb
	{
		public int getInt(int def)
		{
			Vector inputs = readInputZObs();
			if (inputs.size() < 1) return def;
			int S = 0xffffffff;
			for (int i=0; i<inputs.size(); i++) S &= ((MediaZOb)inputs.elementAt(i)).getInt(0xffffffff);
			return S;
		}

		public Pixels getPixels()
		{
			Vector inputs = readInputPixels();

			Pixels inpix;
/*			if (pix != null && inputs.size() == oldframes.size())
			{
				boolean dirty = false;
				for (int i=0; (i<inputs.size()) && (dirty==false); i++)
				{
					inpix = (Pixels)inputs.elementAt(i);
					if (inpix.getFrame() != ((Integer)oldframes.elementAt(i)).intValue())
						dirty = true;
				}
				if (!dirty) return pix;
			}
			oldframes.removeAllElements();
			for (int i=0; i<inputs.size(); i++)
			{
				oldframes.addElement(new Integer(((Pixels)inputs.elementAt(i)).getFrame()));
			}
*/
			createPix(inputs);

			int p[] = pix.getPixels(), ip[];

			for (int i=0; i<(pix.getWidth()*pix.getHeight()); i++) p[i] = 0xffffffff;

			int pi, ipi;
			int pp, ipp;
			int r, g, b;
			for (int i=0; i<inputs.size(); i++)
			{
				inpix = (Pixels)inputs.elementAt(i);
				ip = inpix.getPixels();
				ipi = 0;
				pi = ((inpix.getY() - pix.getY()) * pix.getWidth()) + inpix.getX() - pix.getX();
				for (int y=0; y<inpix.getHeight(); y++)
				{
					for (int x=0; x<inpix.getWidth(); x++)
					{
						ipp = ip[ipi++];
						pp = p[pi];
						p[pi++] = 0xff000000 | (pp & ipp);
					}
					pi += pix.getWidth() - inpix.getWidth();
				}
			}
			pix.setFrame(pix.getFrame()+1);
			return pix;
		}
	}

	public class mul extends MathZOb
	{
		public int getInt(int def)
		{
			Vector inputs = readInputZObs();
			if (inputs.size() < 1) return def;
			int S = 1;
			for (int i=0; i<inputs.size(); i++) S *= ((MediaZOb)inputs.elementAt(i)).getInt(1);
			return S;
		}

		public Pixels getPixels()
		{
			Vector inputs = readInputPixels();

			Pixels inpix;
/*			if (pix != null && inputs.size() == oldframes.size())
			{
				boolean dirty = false;
				for (int i=0; (i<inputs.size()) && (dirty==false); i++)
				{
					inpix = (Pixels)inputs.elementAt(i);
					if (inpix.getFrame() != ((Integer)oldframes.elementAt(i)).intValue())
						dirty = true;
				}
				if (!dirty) return pix;
			}
			oldframes.removeAllElements();
			for (int i=0; i<inputs.size(); i++)
			{
				oldframes.addElement(new Integer(((Pixels)inputs.elementAt(i)).getFrame()));
			}
*/
			createPix(inputs);

			int p[] = pix.getPixels(), ip[];

			for (int i=0; i<(pix.getWidth()*pix.getHeight()); i++) p[i] = 0xffffffff;

			int pi, ipi;
			int pp, ipp;
			int r, g, b;
			for (int i=0; i<inputs.size(); i++)
			{
				inpix = (Pixels)inputs.elementAt(i);
				ip = inpix.getPixels();
				ipi = 0;
				pi = ((inpix.getY() - pix.getY()) * pix.getWidth()) + inpix.getX() - pix.getX();
				for (int y=0; y<inpix.getHeight(); y++)
				{
					for (int x=0; x<inpix.getWidth(); x++)
					{
						ipp = ip[ipi++];
						pp = p[pi];
						r = (((pp >> 12) & 0xff0) * ((ipp >> 12) & 0xff0)) & 0xff0000;
						g = (((pp >> 8) & 0xff) * ((ipp >> 8) & 0xff)) & 0xff00;
						b = ((pp & 0xff) * (ipp & 0xff)) >> 8;
						p[pi++] = 0xff000000 | r | g | b;
					}
					pi += pix.getWidth() - inpix.getWidth();
				}
			}
			pix.setFrame(pix.getFrame()+1);
			return pix;
		}
	}

	public class or extends MathZOb
	{
		public int getInt(int def)
		{
			Vector inputs = readInputZObs();
			if (inputs.size() < 1) return def;
			int S = 0;
			for (int i=0; i<inputs.size(); i++) S |= ((MediaZOb)inputs.elementAt(i)).getInt(0);
			return S;
		}

		public Pixels getPixels()
		{
			Vector inputs = readInputPixels();

			Pixels inpix;
/*			if (pix != null && inputs.size() == oldframes.size())
			{
				boolean dirty = false;
				for (int i=0; (i<inputs.size()) && (dirty==false); i++)
				{
					inpix = (Pixels)inputs.elementAt(i);
					if (inpix.getFrame() != ((Integer)oldframes.elementAt(i)).intValue())
						dirty = true;
				}
				if (!dirty) return pix;
			}
			oldframes.removeAllElements();
			for (int i=0; i<inputs.size(); i++)
			{
				oldframes.addElement(new Integer(((Pixels)inputs.elementAt(i)).getFrame()));
			}
*/
			createPix(inputs);

			int p[] = pix.getPixels(), ip[];

			for (int i=0; i<(pix.getWidth()*pix.getHeight()); i++) p[i] = 0xff000000;

			int pi, ipi;
			int pp, ipp;
			int r, g, b;
			for (int i=0; i<inputs.size(); i++)
			{
				inpix = (Pixels)inputs.elementAt(i);
				ip = inpix.getPixels();
				ipi = 0;
				pi = ((inpix.getY() - pix.getY()) * pix.getWidth()) + inpix.getX() - pix.getX();
				for (int y=0; y<inpix.getHeight(); y++)
				{
					for (int x=0; x<inpix.getWidth(); x++)
					{
						ipp = ip[ipi++];
						pp = p[pi];
						p[pi++] = 0xff000000 | pp | ipp;
					}
					pi += pix.getWidth() - inpix.getWidth();
				}
			}
			pix.setFrame(pix.getFrame()+1);
			return pix;
		}
	}

	public class xor extends MathZOb
	{
		public int getInt(int def)
		{
			Vector inputs = readInputZObs();
			if (inputs.size() < 1) return def;
			int S = 0;
			for (int i=0; i<inputs.size(); i++) S ^= ((MediaZOb)inputs.elementAt(i)).getInt(0);
			return S;
		}

		public Pixels getPixels()
		{
			Vector inputs = readInputPixels();

			Pixels inpix;
/*			if (pix != null && inputs.size() == oldframes.size())
			{
				boolean dirty = false;
				for (int i=0; (i<inputs.size()) && (dirty==false); i++)
				{
					inpix = (Pixels)inputs.elementAt(i);
					if (inpix.getFrame() != ((Integer)oldframes.elementAt(i)).intValue())
						dirty = true;
				}
				if (!dirty) return pix;
			}
			oldframes.removeAllElements();
			for (int i=0; i<inputs.size(); i++)
			{
				oldframes.addElement(new Integer(((Pixels)inputs.elementAt(i)).getFrame()));
			}
*/
			createPix(inputs);

			int p[] = pix.getPixels(), ip[];

			for (int i=0; i<(pix.getWidth()*pix.getHeight()); i++) p[i] = 0xff000000;

			int pi, ipi;
			int pp, ipp;
			int r, g, b;
			for (int i=0; i<inputs.size(); i++)
			{
				inpix = (Pixels)inputs.elementAt(i);
				ip = inpix.getPixels();
				ipi = 0;
				pi = ((inpix.getY() - pix.getY()) * pix.getWidth()) + inpix.getX() - pix.getX();
				for (int y=0; y<inpix.getHeight(); y++)
				{
					for (int x=0; x<inpix.getWidth(); x++)
					{
						ipp = ip[ipi++];
						pp = p[pi];
						p[pi++] = 0xff000000 | pp ^ ipp;
					}
					pi += pix.getWidth() - inpix.getWidth();
				}
			}
			pix.setFrame(pix.getFrame()+1);
			return pix;
		}
	}
}
