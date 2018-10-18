/*   
ZZSoundView.java
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

/** A view showing a sound vstream on a sound scroll.
 * Assumes that all cells in the d.2 rank come from the same scroll.
 */

public class ZZSoundView extends ZZViewComponent {
public static final String rcsid = "$Id: ZZSoundView.java,v 1.8 2000/11/01 08:12:49 tjl Exp $";
	public static boolean dbg = true;
	void p(String s) { if(dbg) System.out.println(s); }
	void pa(String s) { System.out.println(s); }

	public ZZSoundView(ZZCell viewCell0) {
		super(viewCell0);
		setBackground(new Color(150,150,255));
	}

	ZZCell[] rank;
	int[] t0; int[] t1; // in pixels

	int top = 10; int bottom = 10;
	int right = 10;

	public boolean reraster() { return false; }

	public void paintInto(Graphics gr) {
	  synchronized(viewCell.getSpace()) {
	  	Dimension size = getSize();

		Insets ins = getInsets();
		gr.setColor(getBackground());
                gr.fillRect(ins.left, ins.top, 
		    size.width-ins.left-ins.right, size.height-ins.top-ins.bottom);
		gr.setColor(Color.black);

		// assume vertical display - that way putting in transcription text is easy

		ZZCell cursor = viewCell.h("d.cursor-cargo",-1).
				    h("d.cursor", -1);
		Span s = cursor.getSpan();
		if(s==null) {
			gr.drawString("Not a span", 0, size.height/2);
			return;
		}
		Address start = s.getStart();
		Address end = s.getEnd();

		Scroll scr0 = start.getScroll(viewCell.getSpace());
		if(!(scr0 instanceof SoundScroll)) {
			gr.drawString("Wrong type:"+scr0, 0, size.height/2);
			return;
		}
		SoundScroll scr = (SoundScroll)scr0;

		long len = scr.getDurationNanoseconds();

		ZZCell h = cursor.h("d.2", -1);
		rank = h.readRank("d.2", 1, true, null);

		t0 = new int[rank.length];
		t1 = new int[rank.length];

		int l = size.height - top - bottom;

		gr.drawLine(right, top, right, size.height-bottom);

		for(int i=0; i<rank.length; i++) {
			Span sp = rank[i].getSpan();
			if(sp==null) return;
			Address curstart = sp.getStart();
			Address curend = sp.getEnd();
			t0[i] = (int)((l*curstart.getOffs())/len);
			t1[i] = (int)((l*curend.getOffs())/len);

			
			if(rank[i] == cursor)
				gr.setColor(Color.white);

			gr.drawLine(right, t0[i], right+5, t0[i]);
			gr.drawLine(right, t1[i], right+5, t1[i]);
			gr.drawLine(right+1, t0[i], right+1, t1[i]);
			gr.drawLine(right+2, t0[i], right+2, t1[i]);

			if(rank[i] == cursor)
				gr.setColor(Color.black);
		}


	  }
	}
	
}
