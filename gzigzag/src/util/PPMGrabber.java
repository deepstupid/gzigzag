/*   
PPMGrabber.java
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
import java.awt.*;
import java.awt.image.*;
import java.io.*;

/** A simple class to write a given image into a PPM file.
 */

public class PPMGrabber {
public static final String rcsid = "$Id: PPMGrabber.java,v 1.2 2001/04/22 13:33:26 tjl Exp $";

    public static void writePPM(Image image, int x, int y, int w, int h,
		String file) {
	try {
	    writePPM(image, x, y, w, h, new FileOutputStream(file));
	} catch(IOException e) {
	    throw new Error("Ioexception");
	}
    }

    public static void writePPM(Image image, String file) {
	try {
	    writePPM(image, new FileOutputStream(file));
	} catch(IOException e) {
	    throw new Error("Ioexception");
	}
    }

    public static void writePPM(Image image, FileOutputStream file) {
	int w = image.getWidth(null);
	int h = image.getHeight(null);
	writePPM(image, 0, 0, w, h, file);
    }

    public static void writePPM(Image image, int x, int y, int w, int h,
		    FileOutputStream file) {
	
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(image, x, y, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            throw new Error("interrupted waiting for pixels!");
        }
        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            throw new Error("image fetch aborted or errored");
        }

	try {
	    DataOutputStream dos = new DataOutputStream(file);
	    dos.writeBytes("P6\n"+w+" "+h+"\n255\n");
	    for(int i=0; i<pixels.length; i++) {
		int pixel = pixels[i];
	        int red   = (pixel >> 16) & 0xff;
		int green = (pixel >>  8) & 0xff;
		int blue  = (pixel      ) & 0xff;
		dos.writeByte(red);
		dos.writeByte(green);
		dos.writeByte(blue);
	    }
 
	} catch(IOException e) {
	    throw new Error("Ioexception");
	}
    }
}
