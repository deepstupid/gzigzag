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
 * Written by Rauli Ruohonen
 */

package org.gzigzag;
import java.awt.*;
import java.util.*;

/** A font object from which it is easy to obtain
 * instances and metrics scaled to requested multiplications.
 */

public class ScalableFont {
    public static final String rcsid = "$Id: ScalableFont.java,v 1.3 2001/01/01 01:20:22 raulir Exp $";
    public static boolean dbg=false;
    private static final void p(String s) { if(dbg) ZZLogger.log(s); }

    /** If set, font metrics will not be cached.
     */
    public static boolean dontCache=false;

    /** Used to get FontMetrics for a Font. 
     * Should be set in e.g. main(). 
     */
    public static Component fmComp;

    /** The font and font metrics caches.
     */
    private static Hashtable fCache=new Hashtable(),fmCache=new Hashtable();

    /** The font family.
     */
    private String fam;

    /** The font style.
     */
    private int st;
    
    /** The point size when scaling factor is 1.
     */
    private int defPt;

    /** Constructor similar to Java's Font(f,s,p).
     * @param pointsize Point size when scaling factor is 1.
     */
    public ScalableFont(String family,int style,int pointsize) {
	fam=family;
	st=style;
	defPt=pointsize;
    }

    /** Get a scaled instance of the font.
     * @param scale        Scaling factor is (scale/1000).
     * @return May not return null.
     */
    public Font getFont(int scale) {
	int pt=scale2pt(scale);
	return getFontPtId(pt,fam+"/"+st+"/"+pt); // XXX I dislike the ID -tjl
    }

    /** Get font metrics for a scaled instance of the font.
     * @param scale        Scaling factor is (scale/1000).
     * @return May not return null.
     */
    public FontMetrics getFontMetrics(int scale) {
	int pt=scale2pt(scale);
	String id=fam+"/"+st+"/"+pt;
	Font f=getFontPtId(pt,id);

	if(dontCache) return fmComp.getFontMetrics(f);

	FontMetrics fm=(FontMetrics)fmCache.get(id);

	if(fm==null) {
	    p("FontMetrics cache miss: "+id);
	    fm=fmComp.getFontMetrics(f);
	    fmCache.put(id,fm);
	}
	return fm;
    }



    private static Font lastFont;
    private static String lastId="";

    private synchronized Font getFontPtId(int pt,String id) {
	if(lastId.equals(id)) return lastFont;
	lastId=id;
	if(dontCache) lastFont=new Font(fam,st,pt);
	else {
	    lastFont=(Font)fCache.get(id);
	    if(lastFont==null) {
		p("Font cache miss: "+id);
		lastFont=new Font(fam,st,pt);
		fCache.put(id,lastFont);
	    }
	}
	return lastFont;
    }
    private int scale2pt(int scale) {
	return (defPt*scale+500)/1000;
    }

}
