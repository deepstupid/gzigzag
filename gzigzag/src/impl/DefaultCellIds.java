/*   
CellIds.java
 *
 *    Copyright (c) 2001 by Benja Fallenstein
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
package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.Mediaserver;
import java.util.*;

/** Default implementation of <code>CellIds</code>.
 *  XXX There are several structures possible to represent a single String id;
 *  however, we are doing structural comparison, and thus different Id objects
 *  representing the same String may test as not being equal. This is ok, but
 *  MUST NOT HAPPEN for any two Id objects returned by the
 *  <code>CellIds.get()</code> methods. Therefore, we need to take
 *  precautions that the same string is *always* parsed into the same
 *  structure.
 *  <p>
 *  This is one area where mathematical proofs would be very helpful.
 */

public class DefaultCellIds implements CellIds {
     
     class StrId implements Id {
	String str;
	StrId(String str) { this.str = str.intern(); }
		
	public boolean equals(Object other) {
	    if(!(other instanceof StrId)) return false;
	    return str == ((StrId)other).str;
	}
	
	public int hashCode() {
	    return str.hashCode();
	}
		
	public String getString() {
	    return str;
	}
	
	public String toString() {
	    return "StrId(\"" + str + "\")";
	}
     }

     class NumId implements Id {
	int i;
	NumId(int i) { this.i = i; }
	
	public boolean equals(Object other) {
	    if(!(other instanceof NumId)) return false;
	    return i == ((NumId)other).i;
	}
	
	public int hashCode() {
	    return i; // XXX
	}
	
	public String getString() {
	    return ""+i;
	}
	
	public String toString() {
	    return "NumId("+i+")";
	}
     }

     /** An id of the form "abc"+separator+"def". */
     class TwoPartId implements Id {
	Id firstPart, secondPart;
	char separator;
	
	TwoPartId(Id firstPart, char separator, Id secondPart) {
	    this.firstPart = firstPart;
	    this.separator = separator;
	    this.secondPart = secondPart;
	}
		
	public boolean equals(Object other) {
	    if(!(other instanceof TwoPartId)) return false;
	    TwoPartId s = (TwoPartId)other;
	    if(separator != s.separator) return false;
	    return firstPart.equals(s.firstPart) && 
		   secondPart.equals(s.secondPart);
	}
	
	public int hashCode() {
	    return firstPart.hashCode() ^ separator ^ secondPart.hashCode();
	}
		
	public String getString() {
	    // XXX is the character added as a character?
	    return firstPart.getString() + separator + secondPart.getString();
	}
	
	public String toString() {
	    return "TwoPartId(" + firstPart + ", '" + separator + "', "
		   + secondPart + ")";
	}
     }

     /** An id of the form "tmp(xyz)". */
     class TmpId implements Id {
	Id inner;
	TmpId(Id inner) { this.inner = inner; }
		
	public boolean equals(Object other) {
	    if(!(other instanceof TmpId)) return false;
	    TmpId s = (TmpId)other;
	    return inner.equals(s.inner);
	}
	
	public int hashCode() {
	    return inner.hashCode() ^ 100000;
	}
		
	public String getString() {
	    return "tmp(" + inner.getString() + ")";
	}
	
	public String toString() {
	    return "TmpId(" + inner + ")";
	}
     }






     static final char[] separators = { ':', ';', '-', '$' };

     String blockId;

     public Id get(String repr) {
	if(blockId != null && repr.startsWith("-"))
	    return get(blockId + repr);
		
	for(int i=0; i<separators.length; i++) {
	    char separator = separators[i];
	    int n = repr.indexOf(separator);
	    if(n >= 0)
		return new TwoPartId(get(repr.substring(0, n)),
				     separator,
				     get(repr.substring(n+1)));
	}
	
	try {
	    int val = Integer.parseInt(repr);
	    return new NumId(val);
	} catch(NumberFormatException _) {}
	
	if(repr.startsWith("tmp(") && repr.endsWith(")")) {
	    return new TmpId(get(repr.substring(4, repr.length()-1)));
	}
	
	return new StrId(repr);
     }

     public Id get(byte[] repr, int offs, int len) {
	if(blockId != null && repr[0] == '-')
	    return get(blockId + new String(repr, offs, len));
		
	for(int i=0; i<separators.length; i++) {
	    char separator = separators[i];
	    for(int j=0; j<len; j++) {
		if(repr[offs+j] == separator)
		    return new TwoPartId(get(repr, offs, j),
					 separator,
					 get(repr, j+1, len-j-1));
	    }
	}
		
	return get(new String(repr, offs, len));
     }

     public void setBlockId(Mediaserver.Id msid) {
	this.blockId = msid.getString();
     }
}
