/*   
TextSpanVob.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
 * Written by Benja Fallenstein and Tuomas Lukka
 */
package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.vob.*;

/** A vob which shows a single span of text.
 * XXX Prev/Next
 */

public class TextSpanVob extends TextVob implements SpanVob {
String rcsid = "$Id: TextSpanVob.java,v 1.1 2001/10/21 12:58:12 tjl Exp $";

    public final TextSpan span;
    public TextSpanVob prev, next;

    public TextSpanVob(Object key, TextSpan span, TextStyle style) {
	super(key, style, span.getText());
	this.span = span;
    }
    public Span getSpan() { return span; }

    public SpanVob getPart(Span subspan) {
	return new TextSpanVob(null, (TextSpan)subspan, style);
    }

    public void getPartCoords(int x, int y, int w, int h, Span subspan,
			      java.awt.Rectangle write) {
	write.y = y; write.height = h;
	int start = span.getRelativeStart((TextSpan)subspan),
	    end = span.getRelativeEnd((TextSpan)subspan);
	int x1 = style.getX(text, scale, start), 
	    x2 = style.getX(text, scale, end);
	write.x = x + x1; write.width = x2-x1;
    }

    public void setPrev(HBox h) {
	if(!(h instanceof TextSpanVob)) return;
	TextSpanVob other = (TextSpanVob) h;
	other.next = this; prev = other;
    }

    static public void addToChain(LinebreakableChain ch,
			    Object key,
			    TextSpan span,
			    TextStyle style,
			    LinebreakableChain.GlueStyle gs) {
	String s = span.getText();
	int ind = -1;
	int cur = 0;
	while((ind = s.indexOf(' ',ind+1)) >= 0) {
	    gs.addSpace(ch);
	    if(ind > cur) {
		// Make a new fragment
		ch.addBox(new TextSpanVob(
			key,
			(TextSpan)span.subSpan(cur, ind),
			style));
	    }
	    cur = ind + 1;
	}
	// And the rest.
	if(cur < span.length())
	    ch.addBox(new TextSpanVob(
		    key,
		    (TextSpan)span.subSpan(cur),
		    style));
    }

}
