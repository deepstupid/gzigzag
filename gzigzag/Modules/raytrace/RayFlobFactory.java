/*
RayFlobFactory.java
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
import java.awt.image.*;
import java.util.*;

public class RayFlobFactory implements ZOb, FlobFactory
{
	public static final String rcsid = "$Id: RayFlobFactory.java,v 1.3 2001/05/29 13:55:03 deetsay Exp $";

	protected int width = 256;
	protected int height = 256;

	protected static Hashtable flobCache = new Hashtable();

	public RayFlobFactory() {}

	public RayFlobFactory(int w, int h) { width = w; height = h; }

	public String readParams(ZZCell c)
	{
		ZZCell d = c;
		do
		{
			String t = d.getText();
			if (t.equals("width"))
			{
				c = d.s("d.1");
				try
				{
					width = Integer.parseInt(c.getText());
				}
				catch (Exception e) {}
			}
			if (t.equals("height"))
			{
				c = d.s("d.1");
				try
				{
					height = Integer.parseInt(c.getText());
				}
				catch (Exception e) {}
			}
			d = d.s("d.2");
		}
		while ((d != null) && (!d.equals(c)));
		return "";
	}

	public Dimension getSize(ZZCell c, float fract)
	{
		return new Dimension(width, height);
	}

	public Flob makeFlob(FlobSet into, final ZZCell zzc, final ZZCell handleCell,
		final float fract, int x, int y, int d, int w, int h)
	{
		Flob cf = (Flob)flobCache.get(zzc);
		if (cf == null)
		{
			cf = new Flob(x, y, d, w, h, zzc)
			{
				private Image img = null;
				protected Raytracer raytracer = new Raytracer();

				public class RayObserver implements ImageObserver
				{
					public boolean imageUpdate(Image img, int infoflags, int nx, int ny, int w, int h)
					{
						ZZUpdateManager.chg();
						return true;
					}
				}
				public class RaySource implements ImageProducer
				{
					protected Vector imageConsumers = new Vector();
					private Thread lanka = null;

					private void killThread()
					{
						if (lanka != null)
						{
							lanka.stop();
							ImageConsumer cons;
							for (int i=0; i<imageConsumers.size(); i++)
							{
								cons = (ImageConsumer)imageConsumers.elementAt(i);
								cons.imageComplete(cons.IMAGEABORTED);
							}
							lanka = null;
						}
					}

					public class prodThread extends Thread
					{
						public void run()
						{
							ImageConsumer cons;
							raytracer.prepare(width, height, c);
							synchronized(lanka)
							{
								for (int i=0; i<imageConsumers.size(); i++)
								{
									cons = (ImageConsumer)imageConsumers.elementAt(i);
									cons.setDimensions(width, height);
									cons.setHints(cons.TOPDOWNLEFTRIGHT | cons.SINGLEFRAME | cons.COMPLETESCANLINES);
								}
							}
							int pix[];
							for (int y=0; y<height; y++)
							{
								pix = raytracer.nextLine();
								synchronized(lanka)
								{
									for (int i=0; i<imageConsumers.size(); i++)
									{
										cons = (ImageConsumer)imageConsumers.elementAt(i);
										cons.setPixels(0, y, width, 1, ColorModel.getRGBdefault(), pix, 0, width);
									}
								}
							}
							synchronized(lanka)
							{
								for (int i=0; i<imageConsumers.size(); i++)
								{
									cons = (ImageConsumer)imageConsumers.elementAt(i);
									cons.imageComplete(cons.STATICIMAGEDONE);
								}
							}
						}
					}
					public void addConsumer(ImageConsumer ic)
					{
						synchronized(lanka)
						{
							if ((ic != null) && (!isConsumer(ic)))
							{
								if (lanka != null)
									startProduction(ic);
								else
									imageConsumers.addElement(ic);
							}
						}
					}
					public boolean isConsumer(ImageConsumer ic)
					{
						return imageConsumers.contains(ic);
					}
					public void removeConsumer(ImageConsumer ic)
					{
						synchronized(lanka)
						{
							imageConsumers.removeElement(ic);
						}
					}
					public void requestTopDownLeftRightResend(ImageConsumer ic)
					{
						startProduction(ic);
					}
					public void startProduction(ImageConsumer ic)
					{
						if (lanka != null) synchronized(lanka)
						{
							killThread();
						}
						if ((ic != null) && (!isConsumer(ic))) imageConsumers.addElement(ic);
						lanka = new prodThread();
						lanka.start();
					}
				}

				public void renderInterp(Graphics g, float frac)
				{
					Flob r = interpTo;
					if (r==null) return;
					int mx=(int)(this.x+frac*(r.x-this.x)),
						my=(int)(this.y+frac*(r.y-this.y)),
						mw=(int)(this.w+frac*(r.w-this.w)),
						mh=(int)(this.h+frac*(r.h-this.h));
					g.drawRect(mx, my, mw-1, mh-1);
				}

				public void render(Graphics g, int mx, int my, int md, int mw, int mh)
				{
					if (img == null)
					{
						img = ScalableFont.fmComp.createImage(new RaySource());
					}
					g.drawImage(img, mx, my, mw, mh, new RayObserver());
				}

				public Object hit(int hx, int hy)
				{
					if (hx >= this.x && hy >= this.y && hx < this.x+this.w
						&& hy < this.y+this.h) return handleCell;
					return null;
				}
			};
			flobCache.clear();
			flobCache.put(zzc, cf);
		}
		into.add(cf);
		return cf;
	}

	public Flob placeFlob(FlobSet into, ZZCell c, ZZCell handleCell,
		float fract, int x, int y, int depth, float xfract, float yfract)
	{
		Dimension d = getSize(c, fract);
		return makeFlob(into, c, handleCell, fract,
			(int)(x-xfract*d.width), (int)(y-yfract*d.height), depth, d.width, d.height);
	}

	public Flob centerFlob(FlobSet into, ZZCell c, ZZCell handleCell,
		float fract, Point p, int xalign, int yalign, int depth, Dimension d)
	{
		if (d==null) d = getSize(c, fract);
		int x, y;
		x = p.x - d.height/2; y = p.y - d.height/2;
		if (xalign<0) x = p.x;
		if (xalign>0) x = p.x - d.width;
		if (yalign<0) y = p.y;
		if (yalign>0) y = p.y - d.height;
		return makeFlob(into, c, handleCell, fract, x, y, depth, d.width, d.height);
	}

	public Flob centerFlob(FlobSet into, ZZCell c, ZZCell handleCell,
		float fract, Point p, int xalign, int yalign, int depth)
	{
		return centerFlob(into, c, handleCell, fract, p, xalign, yalign, depth, null);
	}
}
