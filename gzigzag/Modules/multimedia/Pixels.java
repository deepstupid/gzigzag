/*
Pixels.java
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
import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 *	The purpose of this class is to be the type of return value
 *	for MediaZOb's getPixels() method.
 *
 *	@see MediaZOb
 */

public class Pixels
{
	public static final String rcsid = "$Id: Pixels.java,v 1.2 2001/03/05 08:44:14 deetsay Exp $";

	private int width, height, x, y;
	private int allocatedSize;
	private int pixels[];

	private int frame = 0;
	public int getFrame() { return frame; }
	public void setFrame(int f) { frame = f; }

	public int getX() { return x; }
	public int getY() { return y; }
	public int getCenterX() { return x + (width >> 1); }
	public int getCenterY() { return y + (height >> 1); }

	public void setX(int a) { x = a; }
	public void setY(int a) { y = a; }
	public void setCenterX(int a) { x = a - (width >> 1); }
	public void setCenterY(int a) { y = a - (height >> 1); }

	public int getWidth() { return width; }
	public int getHeight() { return height; }

	private void checkSize()
	{
		int newsize = width*height;
		if (newsize > allocatedSize)
		{
			pixels = null;
			allocatedSize = newsize;
			pixels = new int[allocatedSize];
		}
	}
	public void setSize(int a, int b)
	{
		width = a;
		height = b;
		checkSize();
	}
	public void setWidth(int a)
	{
		width = a;
		checkSize();
	}
	public void setHeight(int a)
	{
		height = a;
		checkSize();
	}
	public void setCenterWidth(int a)
	{
		x += ((width - a) >> 1);
		width = a;
		checkSize();
	}
	public void setCenterHeight(int a)
	{
		y += ((height - a) >> 1);
		height = a;
		checkSize();
	}

	public int[] getPixels() { return pixels; }

	/**
	 *	Thanks to this method there's no need to use MemoryImageSource
	 *	to get an Image from pixel data.
	 */
	public ImageProducer getSource()
	{
		return new ImageProducer()
		{
			private Vector imageConsumers = new Vector();

			public void addConsumer(ImageConsumer ic)
			{
				if ((ic != null) && (!isConsumer(ic)))
				{
					imageConsumers.addElement(ic);
				}
			}
			public boolean isConsumer(ImageConsumer ic)
			{
				return imageConsumers.contains(ic);
			}
			public void removeConsumer(ImageConsumer ic)
			{
				imageConsumers.removeElement(ic);
			}
			public void requestTopDownLeftRightResend(ImageConsumer ic)
			{
				startProduction(ic);
			}
			public void startProduction(ImageConsumer ic)
			{
				addConsumer(ic);

				int i = 0;
				ImageConsumer cons;
				try
				{
					while (true)
					{
						cons = (ImageConsumer)imageConsumers.elementAt(i++);
						cons.setDimensions(getWidth(), getHeight());
						cons.setHints(ImageConsumer.TOPDOWNLEFTRIGHT
							| ImageConsumer.SINGLEFRAME | ImageConsumer.SINGLEPASS);
						cons.setPixels(0, 0, getWidth(), getHeight(), ColorModel.getRGBdefault(),
							getPixels(), 0, getWidth());
						cons.imageComplete(ImageConsumer.STATICIMAGEDONE);
					}
				}
				catch (Exception e) {}
			}
		};
	}

	public Pixels(int w, int h)
	{
		x = 0;
		y = 0;
		width = w;
		height = h;
		pixels = new int[width*height];
	}
}
