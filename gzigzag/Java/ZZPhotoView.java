/*   
ZZPhotoView.java
 *    
 *    Copyright (c) 1999, Ted Nelson and Tuomas Lukka
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
 *  Patched to work with ZZViewComponend format by Jarkko Laine
 */

package org.gzigzag;
import java.awt.*;
import java.net.*;
import java.awt.event.*;

/** A simple view showing photos from the cursor.
 * This view shows the image at the URL on the end of d.photo from
 * the cell the cursor this view is associated with is on.
 */

public class ZZPhotoView extends ZZViewComponent {
public static final String rcsid = "$Id: ZZPhotoView.java,v 1.14 2000/11/01 08:12:48 tjl Exp $";
	ZZCell cursor;


        public ZZPhotoView(ZZCell viewCell0) {
                super(viewCell0);
	}

	URL cur;
	Image img;

	public boolean reraster() { return false; }

        public void paintInto(Graphics g) {


                
                // Clear the area
		Dimension thisd = getSize();
		Color bg = getBackground();
                g.setColor(bg);
		Insets ins = getInsets();
                g.fillRect(ins.left, ins.top, thisd.width-ins.left-ins.right, thisd.height-ins.top-ins.bottom);
                g.setColor(Color.black);



                // Let's try it like this.. Is this a bad way of doing this?
                cursor = ZZCursorReal.get(viewCell);


                // If the current cell has no neighbours in the
                // photo dimention..
		if(cursor == null ||
                   cursor.s("d.photo", 1) == null)
                {
                        Color c = Color.black;
                        g.setColor(c);

// For some weird reason, I can't get the drawStrings to work.... Jarkko

                        g.drawString("No photo for this cell", 0, 0);
		   	return;
		}
 

                

		URL url = null;
		try {
		url = new URL(cursor.h("d.photo", 1).getText());
		} catch(MalformedURLException e) {
			String s= e.toString();
                        g.drawString(s, 0, 0);
			return;
		}
		if(!url.equals(cur)) {
			cur = url;
			img = getToolkit().getImage(url);
		}
		if(!g.drawImage(img, 0, 0, null)) {
			repaint(1000);
		}
  
	}
}

