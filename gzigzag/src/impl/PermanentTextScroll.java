/*   
PermanentTextScroll.java
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
import java.io.*;

/** A text scroll block loaded from somewhere.
 */

public class PermanentTextScroll extends TextScrollBlock {
String rcsid = "$Id: PermanentTextScroll.java,v 1.5 2002/02/23 11:04:20 bfallenstein Exp $";

    char[] chars;

    public PermanentTextScroll(String id, String string) {
	super( id );
	this.chars = string.toCharArray();
    }

    Mediaserver ms;
    Mediaserver.Id msid;
    boolean loadingFailed;

    public PermanentTextScroll(Mediaserver ms, Mediaserver.Id msid) {
	super(msid.getString());
	this.ms = ms;
	this.msid = msid;
    }

    protected final void load() {
	if(chars != null || loadingFailed) return;

        Mediaserver.Block block ;
        String ct;
        try {
            block = ms.getDatum(msid);
            ct = block.getContentType();
        } catch(IOException e) {
	    loadingFailed = true;
	    e.printStackTrace();
            throw new Error("Couldn't load block: "+e);
        }

        // Note: for the legacy string content to work, we need to be able
        // to load GZZ1 diffs as text blocks (see GZZ1Handler.LegacyContent
        // javadoc for more info).
	if(!ct.equals("text/plain; charset=UTF-8") &&
	   !ct.equals("application/x-gzigzag-GZZ1")) {
	    loadingFailed = true;
	    throw new Error("Unknown text block '"+ct+"'");
	}

	String string;
	try {
	    string = new String(block.getBytes(), "UTF8");
	} catch(Exception e) {
	    loadingFailed = true;
	    e.printStackTrace();
	    throw new Error("Exception while reading: "+e);
	}

        this.chars = string.toCharArray();
    }

    class SimpleTextSpan extends ScrollBlockManager.Span1DBase 
			    implements TextSpan{
	SimpleTextSpan(int offs0, int offs1) { 
	    super(PermanentTextScroll.this, offs0, offs1);
	}

	protected ScrollBlockManager.Span1DBase
		createNew(int offs0, int offs1) {
	    return new SimpleTextSpan(offs0, offs1);
	}

	public String getText() {
	    load();
	    return new String(chars, offs0, offs1-offs0);
	}

    }

    public TextSpan append(char ch) throws ImmutableException {
	throw new ImmutableException("Can't append to permanent scroll block");
    }

    public Span getCurrent() {
	load();
	return new SimpleTextSpan(0, chars.length);
    }

    public Span getSpan(int offs, int len) {
	return new SimpleTextSpan(offs, offs+len);
    }

    public boolean isFinalized() {
	return true;
    }

    public char[] getCharArray() {
	load();
	return chars;
    }

}


