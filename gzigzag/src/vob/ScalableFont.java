/*
ScalableFont.java
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
 * Written by Rauli Ruohonen and Benja Fallenstein
 */

package org.gzigzag.vob;
import java.awt.*;
import java.util.*;

/** A font object from which it is easy to obtain
 * instances and metrics scaled to requested multiplications.
 * This exists because our target, the Java API 1.1.8 does not
 * contain scaling functionality.
 * <p>
 * Scale is used in units of one thousand, i.e. 1000 = "normal" size.
 */

public final class ScalableFont {
    public static final String rcsid = "$Id: ScalableFont.java,v 1.9 2001/09/27 14:53:43 tjl Exp $";
    public static boolean dbg=false;
    private static final void p(String s) { if(dbg) System.out.println(s); }

    /** If set, font metrics will not be cached.
     */
    public static boolean dontCache=false;

    /** Used to get FontMetrics for a Font. 
     * Should be set in e.g. main(). 
     */
    public static Component fmComp;
    {
	// just to get this initialized always... is it bad to do it like this?
	fmComp = new Panel();
    }

    /** A cache for Fonts and FontMetrics of same family and style.
     */
    static class Cache {
	String family;
	int style;
	Font[] fonts = new Font[20];
	FontMetrics[] metrics = dontCache ? new FontMetrics[0] 
	                                  : new FontMetrics[20];
	
	Cache(String family, int style) {
	    this.family = family;
	    this.style = style;
	}

	Font getFont(int pt) {
	    if(fonts.length <= pt) {
		Font[] nfonts = new Font[pt+1];
		System.arraycopy(fonts, 0, nfonts, 0, fonts.length);
		fonts = nfonts;
	    }
	    Font f = fonts[pt];
	    if(f == null) {
		f = new Font(family, style, pt);
		fonts[pt] = f;
	    }
	    return f;
	}

	FontMetrics getFontMetrics(int pt) {
	    if(dontCache) return fmComp.getFontMetrics(getFont(pt));

            if(metrics.length <= pt) {
                FontMetrics[] nmetrics = new FontMetrics[pt+1];
                System.arraycopy(metrics, 0, nmetrics, 0, metrics.length);
                metrics = nmetrics;
            }
            FontMetrics m = metrics[pt];
            if(m == null) {
                m = fmComp.getFontMetrics(getFont(pt));
                metrics[pt] = m;
            }
	    return m;
	}
    }

    /** The font and font metrics caches.
     */
    private static Map caches = new HashMap();

    /** The cache for this scalable font.
     */
    private Cache myCache;

    /** The point size when scaling factor is 1.
     */
    private int defPt;

    /** Constructor similar to Java's Font(f,s,p).
     * @param pointsize Point size when scaling factor is 1.
     */
    public ScalableFont(String family, int style, int pointsize) {
	defPt = pointsize;

	String id = family + "/" + style; // XXX I dislike the ID -tjl
	myCache = (Cache)caches.get(id);
	if(myCache == null) {
	    myCache = new Cache(family, style);
	    caches.put(id, myCache);
	}
    }

    /** Get a scaled instance of the font.
     * @param scale        Scaling factor is (scale/1000).
     * @return May not return null.
     */
    public Font getFont(int scale) {
	int pt=scale2pt(scale);
	return myCache.getFont(pt);
    }

    FontMetrics lastFM;
    int lastFMScale = -1;

    /** Get font metrics for a scaled instance of the font.
     * @param scale        Scaling factor is (scale/1000).
     * @return May not return null.
     */
    public FontMetrics getFontMetrics(int scale) {
	if(scale == lastFMScale) return lastFM;
	int pt=scale2pt(scale);
	FontMetrics fm = myCache.getFontMetrics(pt);
	lastFM = fm; lastFMScale = scale;
	return fm;
    }

    private int scale2pt(int scale) {
	return (defPt*scale+500)/1000;
    }

    /** Get the scale for the point size which is nearest the given height.
     *  XXX does not work
     */
    public int getScale(int height) {
	return 1000;
    }

}






















