/*   
ZZDrawing.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag;
import java.awt.*;

/** An abstraction of improved drawing primitives.
 * We are aiming for Java 1.1, but for instance Java2D provides
 * many features we'd like to use, if present.
 * Normally, simply testing <pre>g instanceof Graphics2D</pre>
 * would be sufficient but we want to also be able to <em>compile</em>
 * with only the 1.1 libraries available, so we cannot do that.
 * Also, we want to be able to hook up to e.g. OpenGL without 
 * having to create a full Java2D implementation there.
 * <p>
 * This class is used to abstract out any graphics operations that
 * are not in java.awt.Graphics. The default of this class is that
 * each operation is a no-op (unless otherwise specified).
 * All code should function reasonably under such an assumption.
 * Subclasses of this object are allowed to implement operations
 * in any way they like.
 * <p>
 * TODO: Should make it possible to get drawing parameters
 * from either system
 * properties (possibly preferable) or the ZZ structure.
 */

public class ZZDrawing {
public static final String rcsid = "$Id: ZZDrawing.java,v 1.6 2001/03/26 12:41:00 tjl Exp $";

    /** Get a human-readable string identifying the ZZDrawing
     * instance and the settings.
     */
    public String getType() {
	return "Default 1.1 API -compliant version: no antialiasing or rotation";
    }

    /** The default instance of ZZDrawing for others to use.
     */
    static public final ZZDrawing instance = createInstance();
    private boolean qEnabled = true;

    /** Create an instance.
     */
    static public ZZDrawing createInstance() {
	ZZDrawing ret = null;
	Class g2d = null, zzdj2d = null;
	try {
	    g2d = Class.forName("java.awt.Graphics2D");
	    zzdj2d = Class.forName("org.gzigzag.ZZDrawingJ2D");
	    ret = (ZZDrawing)zzdj2d.newInstance();
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    if(g2d != null) {
		ZZLogger.log("Graphics2D found but support not compiled in.");
	    }
	}
	if(ret == null) ret = new ZZDrawing();
	return ret;
    }

    /** Set rendering so that the graphics context will draw
     * over translucently, with the given alpha.
     */
    public void setAlpha(Graphics g, float alpha) {
    }

    /** Set good rendering quality.
     * XXX Should take a parameter.
     */
    public void setQuality(Graphics g) {
    }

    /** Return true if it makes any difference. */
    public boolean enableQuality(boolean t) {
	qEnabled = t;
	return false;
    }
    public boolean qualityEnabled() { return qEnabled; }

    /** Set the defaults. To be called on all
     * Graphics objects used.
     */
    public void setDefaults(Graphics g0) {
    }
}

