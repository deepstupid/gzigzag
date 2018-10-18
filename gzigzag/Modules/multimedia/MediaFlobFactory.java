/*
MediaFlobFactory.java
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
import org.gzigzag.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class MediaFlobFactory extends Default implements FlobFactory
{
	public static final String rcsid = "$Id: MediaFlobFactory.java,v 1.14 2001/02/27 08:42:42 raulir Exp $";

	static private String flobText;
	static private float flobFract;
	static private Vector flobColors = new Vector();
	static public String getFlobText() { return flobText; }
	static public float getFlobFract() { return flobFract; }
	static public Vector getFlobColors() { return flobColors; }

    static private Hashtable mediaCache = new Hashtable();
    static private ImageObserver imgobs = new ImageObserver() {
			public boolean imageUpdate(Image i, int inf, int i2, 
									   int i3, int i4, int i5) {
			    if ((inf & ALLBITS) != 0) {
					ZZUpdateManager.chg();
					return false;
			    }
			    return true;
			}
		};

	/**
	 *	Get cached MediaZOb for ZZCell or make a new MediaZOb and
	 *	add it to the cache.
	 *
	 *	@param c	ZZCell whose corresponding MediaZOb we want
	 */
	private MediaZOb getFromCache(ZZCell c)
	{
		MediaZOb mz;
		ZZCell rc = c.h("d.clone");			// Cache rootclones only
		if (mediaCache.containsKey(rc))
		{
			mz = (MediaZOb)mediaCache.get(rc);
		}
		else
		{
			mz = MediaZOb.newMediaZOb(rc);
			mediaCache.put(rc, mz);
		}
		return mz;
	}

	public Dimension getSize(ZZCell c, float fract)
	{
		if (c == null) return new Dimension
		(
			readParam("width").getInt(80), readParam("height").getInt(15)
		);
		flobText = c.getText();

		MediaZOb mz = getFromCache(c);

//		Image img = ScalableFont.fmComp.createImage(mz);
//		return new Dimension(img.getWidth(null), img.getHeight(null));
		Pixels pix = mz.getPixels();
		if (pix.getWidth() < 0 || pix.getHeight() < 0) {
			return new Dimension(0, 0);
		}
		return new Dimension(pix.getWidth(), pix.getHeight());
	}

	public Flob makeFlob(FlobSet into, ZZCell zzc, final ZZCell handleCell,
		final float fract, int x, int y, int d, int w, int h)
	{
		Flob cf = new Flob(x, y, d, w, h, zzc)
		{
			public void renderInterp(Graphics g,float frac)
			{
				Flob r=interpTo;
				if (r==null) return;
				int mx=(int)(this.x+frac*(r.x-this.x)),
					my=(int)(this.y+frac*(r.y-this.y)),
					mw=(int)(this.w+frac*(r.w-this.w)),
					mh=(int)(this.h+frac*(r.h-this.h));
				g.drawRect(mx,my,mw-1,mh-1);
			}

			public void render(Graphics g, int mx, int my, int md, int mw, int mh)
			{
				if (!ZZDrawing.instance.qualityEnabled()) return;
				flobColors.removeAllElements();
				ZZCell orig = c.h("d.clone", -1);
				if (orig.h("d.cursor-list", -1).s("d.cursor", -1) != null)
				{
					flobColors.addElement(ZZCursorReal.getColorOrWhite(orig));
				}
				else
				{
					Enumeration e = ZZCursorReal.getPointers(c);
					while (e.hasMoreElements())
					{
						ZZCell cu = (ZZCell)e.nextElement();
						Color color = ZZCursorReal.getColor(cu);
						if (color != null) flobColors.addElement(color);
					}
				}
				flobText = c.getText();
				MediaZOb mz = getFromCache(c);

				Image img = ScalableFont.fmComp.createImage(mz.getPixels().getSource());
				ZZDrawingJ2D drw = null;
				if (ZZDrawing.instance instanceof ZZDrawingJ2D)
					drw = (ZZDrawingJ2D) ZZDrawing.instance;
				if (drw != null) {
					drw.setFIXME(false);
					drw.setQuality(g);
				}
				if (mw == 0 && mh == 0)
					g.drawImage(img, mx, my, mw, mh, imgobs);
				else
					g.drawImage(img, mx, my, mw, mh, null);
				if (drw != null) {
					drw.setFIXME(true);
					drw.setQuality(g);
				}
			}

		    public Object hit(int hx,int hy) {
				if (hx >= this.x && hy >= this.y && hx < this.x+this.w
					&& hy < this.y+this.h) return handleCell;
				return null;
		    }
		};
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
// Local variables:
//   tab-width: 4
// End:
