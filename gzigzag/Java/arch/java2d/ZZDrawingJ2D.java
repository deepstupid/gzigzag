/*   
ZZDrawingJ2D.java
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
import java.awt.geom.*;

/** An implementation of ZZDrawing for the Java2D API.
 * This allows us to use Java2D's features if present, but
 * not to depend on them.
 */

public class ZZDrawingJ2D extends ZZDrawing {
public static final String rcsid = "$Id: ZZDrawingJ2D.java,v 1.22 2001/04/30 09:31:33 tjl Exp $";

    public String getType() {
	return "Java2D: antialias, alpha(if used), rotation";
    }

    public void setAlpha(Graphics g0, float alpha) {
	Graphics2D g = (Graphics2D)g0;
	g.setComposite(AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, alpha));
    }

    public RenderingHints qualityHints = new RenderingHints(null);
    {
	qualityHints.put(
	    RenderingHints.KEY_TEXT_ANTIALIASING,
	    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	qualityHints.put(
	    RenderingHints.KEY_ANTIALIASING,
	    RenderingHints.VALUE_ANTIALIAS_ON);

	qualityHints.put(RenderingHints.KEY_RENDERING,
			 RenderingHints.VALUE_RENDER_QUALITY);
	qualityHints.put(RenderingHints.KEY_STROKE_CONTROL,
			 RenderingHints.VALUE_STROKE_PURE);

	    //	qualityHints.put(RenderingHints.KEY_FRACTIONALMETRICS,
	    //	    RenderingHints.VALUE_FRACTIONALMETRICS_ON);

    }
    public RenderingHints speedHints = new RenderingHints(null);
    {
	speedHints.put(RenderingHints.KEY_RENDERING,
		       RenderingHints.VALUE_RENDER_SPEED);
    }

    public void setFIXME(boolean t) {
	if (t) {
	    qualityHints.put(RenderingHints.KEY_ANTIALIASING,
			     RenderingHints.VALUE_ANTIALIAS_ON);
	    qualityHints.put(RenderingHints.KEY_RENDERING,
			     RenderingHints.VALUE_RENDER_QUALITY);
	    qualityHints.put(RenderingHints.KEY_STROKE_CONTROL,
			     RenderingHints.VALUE_STROKE_PURE);
	}
	else {
	    qualityHints.put(RenderingHints.KEY_ANTIALIASING,
			     RenderingHints.VALUE_ANTIALIAS_OFF);
	    qualityHints.put(RenderingHints.KEY_RENDERING,
			     RenderingHints.VALUE_RENDER_SPEED);
	    qualityHints.put(RenderingHints.KEY_STROKE_CONTROL,
			     RenderingHints.VALUE_STROKE_DEFAULT);
	}
    }

    public boolean enableQuality(boolean t) {
	super.enableQuality(t);
	return true;
    }

    public void setQuality(Graphics g0) {
	Graphics2D g = (Graphics2D)g0;
	if (qualityEnabled()) g.setRenderingHints(qualityHints);
	else g.setRenderingHints(speedHints);
    }
    
    public void setDefaults(Graphics g0) {
	//Graphics2D g = (Graphics2D)g0;
	//g.rotate(0.1);
    }

    /** A flob which shows the given ZZScene rotated by given angle.
     */
    public class RotatedSceneFlob extends SceneFlob {

	/** SceneFLob is a boxedflob, but here boxes screw up things badly.
	 */
	public boolean needsBox() { return false; }

	double rot;

	public RotatedSceneFlob(int x, int y, int d, int w, int h, ZZCell c, FlobSet sc,
		    double rot) {
	    super(x,y,d,w,h,c, sc);
	    this.rot = rot;
	}


	public void transform(double[] srcPtr, double[]dstPts, boolean toExt) {
	    AffineTransform tr =
		AffineTransform.getRotateInstance(rot, x+w/2, y+h/2);
	    try {
	    if(toExt)
		tr.transform(srcPtr, 0, dstPts, 0, srcPtr.length);
	    else
		tr.inverseTransform(srcPtr, 0, dstPts, 0, srcPtr.length);
	    } catch(Exception e) {
		ZZLogger.exc(e);
	    }
	}

	public Point transform(Point src, Point dst, boolean toExt) {
	    AffineTransform tr =
		AffineTransform.getRotateInstance(rot, x+w/2, y+h/2);
	    try {
	    if(toExt)
		tr.transform(src, dst);
	    else
		tr.inverseTransform(src, dst);
	    } catch(Exception e) {
		ZZLogger.exc(e);
	    }
	    return dst;
	}

	void prepareGraphics(Graphics g0, float fract) {
	    Graphics2D g = (Graphics2D)g0;
	    if(fract == 0) {
		if(rot != 0)
		    g.rotate(rot, x+w/2, y+h/2); 
		return;
	    }
	    if(!(interpTo instanceof RotatedSceneFlob)) return; // XXX?
	    double nrot = rot + fract*(((RotatedSceneFlob)interpTo).rot - rot); 
	    if(nrot == 0) return;
	    g.rotate(nrot, 
		(int)((x+w/2)*(1-fract) + 
			fract * (interpTo.x + interpTo.w/2)),
		(int)((y+h/2)*(1-fract) +
			fract * (interpTo.y + interpTo.h/2))
		);
	}

	
    }
}


