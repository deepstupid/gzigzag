/*
image.java
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
import java.net.*;

/** Image mediazob.
 * Images can be read from a different place by setting the property images.from, 
 * like 
 * java -Dimages.from=img:///=file:///home/antkaij/cvs/gzigzag-demo/Java/demospace-lomat.2.zz/ org.gzigzag.Main  demospace-lomat.2.zz
 */

public class image extends MediaZOb
{
	public static final String rcsid = "$Id: image.java,v 1.7 2001/02/28 05:16:55 tjl Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

	private static Hashtable convert = null;
	private Pixels pix = null;

	private static String doConvert(String str) {
		if (convert == null) {
			convert = new Hashtable();
			String prop = null;
			try {
				prop = System.getProperty("images.from");
			} catch(Throwable t) {}
			if (prop != null) {
				StringTokenizer tok = new StringTokenizer(prop, ";=");
				while (tok.hasMoreTokens()) {
					String a = tok.nextToken();
					String b = tok.nextToken();
					p(""+a+" => "+b);
					convert.put(a, b);
				}
			}
		}
		for(Enumeration e = convert.keys(); e.hasMoreElements(); ) {
			String from = (String)e.nextElement();
			p(""+str+"; "+from);
			if (!str.startsWith(from)) continue;
			String to = (String)convert.get(from);
			p(""+str+" => "+to + str.substring(from.length()));
			str = to + str.substring(from.length());
			return str;
		}
		p("Not converted: "+str);
		return str;
	}
	public Pixels getPixels()
	{
		if (pix == null)
		{
			try
			{
				URL imgURL = new URL(doConvert(getText("")));
				p("URL: "+imgURL);
				Image img = Toolkit.getDefaultToolkit().getImage(imgURL);
				int w = img.getWidth(null);
				int count = 2000000;
				while ((w < 0) && (count-- > 0)) w = img.getWidth(null);
				int h = img.getHeight(null);
				
				pix = new Pixels(w, h);
				PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pix.getPixels(), 0, w);
				pg.grabPixels();
			}
			catch (Exception e) {}
		}
		return pix;
	}
}

// Local variables:
//   tab-width: 4
// End:
