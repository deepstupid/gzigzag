/*   
GaborPatterns.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
package org.gzigzag.util;
import org.gzigzag.*;
import java.awt.*;
import java.awt.image.*;

/** Routines to deal with Gabor patterns (see e.g.Ware, Information 
 * Visualization, Chapter 5).
 * Gabor patterns are a simple way to generate many different-looking (feeling?)
 * textures from few numeric parameters.
 */

public class GaborPatterns {
public static final String rcsid = "$Id: GaborPatterns.java,v 1.2 2001/10/10 11:47:01 tjl Exp $";

    /** Create an image of a random Gabor-spattered tileable pattern.
     * @param orientation The angle, in radians, of the pattern. Reasonable
     * 			values: 0..2*PI
     * @param frequency The frequency of the vibrational component of the pattern
     * 			in radians per pixel. Reasonable values: fairly small,
     * 			maybe 0.1 to 2.
     * @param size	The size of the patch, as sigma of the gaussian.
     * @param aspect	The aspect ration of the gaussian, 1 = circular.
     * @param phase	The phase shift, in radians
     * 			
     */
    static public Image createPattern(
	    double orientation,
	    double frequency,
	    double size,
	    double aspect,
	    double phase,
	    int color0,
	    int color1,
	    int width, 
	    int height
    ) {
	double[] pixels = new double[width * height];

	int patternsize = (int)(size * 15); // XXX?

	double[] pattern = new double[patternsize * patternsize];
	for(int x=0; x<patternsize; x++)
	    for(int y=0; y<patternsize; y++)
		pattern[x + y * patternsize] = gaborRot(
			x - patternsize / 2, y - patternsize / 2,
			orientation, frequency, size, aspect, phase);
	
	
	// Spatter.
	int nspatter = 1000;
	for(int i=0; i<nspatter; i++) {
	    int ctrx = (int) (width * Math.random());
	    int ctry = (int) (height * Math.random());
	    addTo(pixels, width, height, ctrx, ctry, 
			    pattern, patternsize, patternsize);
	}

	int[] pix = new int[width * height];
	for(int i=0; i<pix.length; i++) {
	    double mult = pixels[i];
	    mult = (mult + 1) / 2;
	    if(mult > 1) mult = 1;
	    if(mult < 0) mult = 0;
	    int red = interp(mult, (color0 >> 16) & 255, (color1 >> 16) & 255);
	    int green = interp(mult, (color0 >> 8) & 255, (color1 >> 8) & 255);
	    int blue = interp(mult, (color0 >> 0) & 255, (color1 >> 0) & 255);
	    pix[i] = (255 << 24) |
		     (red << 16) |
		     (green << 8) |
		     (blue << 0);
	}

	return Toolkit.getDefaultToolkit().createImage(
		    new MemoryImageSource(width, height, pix, 0, width));
    }

    static public void addTo(double[] to, int w, int h, int dx, int dy, double[] pattern,
		    int pw, int ph) {
	for(int y = 0; y < ph; y ++) {
	    int ty = (y + dy) % h;
	    for(int x = 0; x < pw; x ++) {
		int tx = (x + dx) % w;
		to[tx + w*ty] += pattern[x + pw * y];
	    }
	}
    }

    static public int interp(double mult, int i0, int i1) {
	return (int)(mult * i0 + (1-mult) * i1);
    }
    


    static double gaborRot(double x, double y, double orientation,
		    double frequency, double size, double aspect,
		    double phase)  {
	double x1 = x * Math.cos(orientation) + y * Math.sin(orientation);
	double y1 = -x * Math.sin(orientation) + y * Math.cos(orientation);
	return gabor(x1, y1, frequency, size, aspect, phase);
    }

    static double gabor(double x1, double y1, double frequency, 
		    double size, double aspect,
		    double phase) 
    {
	return Math.exp(-(x1*x1 + aspect * y1 * y1) / (2 * size * size)) *
	    Math.cos(frequency * x1 + phase);
    }

}
