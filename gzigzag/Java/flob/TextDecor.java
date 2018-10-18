/*   
TextDecor.java
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
 * Written by Tuukka Hastrup
 */
 
package org.gzigzag;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class TextDecor extends Renderable {
public static final String rcsid = "$Id: TextDecor.java,v 1.4 2000/09/19 10:32:00 ajk Exp $";
    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    int[] x;
    int[] y;
    String[] txt;
    Color color;
    Font font;

    public TextDecor(int x, int y, String txt, Color color, Font font) {
	this(new int[] {x}, new int[] {y}, new String[] {txt}, color, font);
    }

    /** Usual constructor.
     */
    public TextDecor(int[] x, int[] y, String[] txt, Color color, Font font) {
	this.x = x;
	this.y = y;
	this.txt = txt;
	this.color = color;
	this.font = font;
    }

    public void render(Graphics g) {
	Color oldc = null;
	if(color != null) {
	    oldc = g.getColor();
	    g.setColor(color);
	}
	Font oldf = null;
	if(font != null) {
            oldf = g.getFont();
            g.setFont(font);
        }

	for(int i=0;i<txt.length;i++)
		g.drawString(txt[i], x[i], y[i]);

	if(oldc != null)
	    g.setColor(oldc);
	if(oldf != null)
	    g.setFont(oldf);
    }
}
