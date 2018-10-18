/*   
RawTextStyle.java
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.vob;
import java.awt.*;

/** A style drawing raw text.
 */

public class RawTextStyle extends TextStyle {
public static final String rcsid = "$Id: RawTextStyle.java,v 1.3 2001/08/04 09:12:57 bfallenstein Exp $";

    public RawTextStyle(ScalableFont font, Color col) { super(font, col); }

    public void render(java.awt.Graphics g,
                       char[] chars, int offs, int len,
		       int scale, int x, int y, int w, int h,
                       boolean boxDrawn,
                       Vob.RenderInfo info) {
        Font f = font.getFont(scale);
        FontMetrics fm = font.getFontMetrics(scale);

        int fh = fm.getHeight(), fasc = fm.getAscent();
        int ty = y + (h-fh)/2 + fasc;

        g.setFont(f);
        g.drawChars(chars, offs, len, x, ty);
    }
}


