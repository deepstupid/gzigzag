/*   
TransientTextScroll.java
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
 * Written by Tuomas Lukka
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;

/** A text scroll block.
 */

public class TransientTextScroll extends TextScrollBlock {
String rcsid = "$Id: TransientTextScroll.java,v 1.8 2002/02/07 01:55:03 tuukkah Exp $";

    boolean finalized = false;

    public TransientTextScroll() {
	super( ScrollBlockManager.getTmpID() );
    }

    final StringBuffer current = new StringBuffer();
    char[] curchars;

    class SimpleTextSpan extends ScrollBlockManager.Span1DBase 
				    implements TextSpan{
	SimpleTextSpan(int offs0, int offs1) { 
	    super(TransientTextScroll.this, offs0, offs1);
	}

	protected ScrollBlockManager.Span1DBase
		createNew(int offs0, int offs1) {
	    return new SimpleTextSpan(offs0, offs1);
	}

	public String getText() {
	    char[] chars = new char[offs1-offs0];
	    current.getChars(offs0, offs1, chars, 0);
	    return new String(chars);
	}

    }

    public TextSpan append(char ch) throws ImmutableException {
	if(finalized)
	    throw new ImmutableException("Already saved; can't append");
	current.append(ch);
	return new SimpleTextSpan(current.length()-1, current.length());
    }

    public Span getCurrent() {
	return new SimpleTextSpan(0, current.length());
    }

    public Span getSpan(int offs, int len) {
	return new SimpleTextSpan(offs, offs+len);
    }

    public boolean isFinalized() {
	return finalized;
    }

    public Mediaserver.Id save(Mediaserver ms, Mediaserver.Id assocId) 
	throws java.io.IOException {

	if(current.length() == 0)
	    return null; // don't save empty block

	byte[] bytes = current.toString().getBytes("UTF8");
	String content_type = "text/plain; charset=UTF-8";
	finalized = true;
	return ms.addDatum(bytes, content_type, assocId);
    }

    public char[] getCharArray() {
	if(curchars == null || curchars.length != current.length()) 
	    curchars = current.toString().toCharArray();
	return curchars;
    }

}


