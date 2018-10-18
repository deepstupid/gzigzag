/*   
GZZ1Reader.java
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
import java.io.*;
import org.gzigzag.util.*;
import org.gzigzag.mediaserver.Mediaserver;

/** A class for reading GZZ1 data.
 *  Final so that smart compilers can inline routines in tight loops.
 */

public final class GZZ1Reader {
String rcsid = "$Id: GZZ1Reader.java,v 1.43 2002/03/13 22:18:22 bfallenstein Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }

    /** The size of the buffer blocks we use.
     */
    final int BLOCK = 4096;

    /** The reader we read from.
     *  <code>null</code> if we are given a char array to parse.
     */
    Reader reader;

    /** The buffer into which we slurp parts of the file.
     *  All the elements of the buffer are valid; when we have less than
     *  <code>BLOCK</code> bytes left, we create a smaller array. This is
     *  necessary because we want to use Java's subscript checking instead
     *  of adding our own.
     *  <p>
     *  Before, we slurped the whole file into this, but in the face of 2MB
     *  blocks, this was far too slow.
     *  <p>
     *  Once we deprecate the old format, all will be
     *  8-bit long and we can just use the byte array,
     *  but for now we must convert it to chars.
     */
    char[] content;

    /** The current position inside the buffer block we currently read.
     *  (The counter is between the previous and the next char.)
     *  <p>
     *  <code>bufferPosition + curPosition</code> is the current offset
     *  in the whole file.
     */
    int curPosition = 0;

    /** The position of the block we currently read, in the whole file.
     *  (The counter is the index of the first char of the block.)
     */
    int blockPosition = 0;

    int sread() {
	try {
	    return content[curPosition++];
	} catch(ArrayIndexOutOfBoundsException e) {
	    nextBlock();
	    return content[curPosition++];
	}
    }

    int getPosition() { return blockPosition + curPosition; }

    StringBuffer s = new StringBuffer(200);

    String until(int c) {
	int start = curPosition;
	try {
	    while(content[curPosition] != c) {
		curPosition ++;
	    }
	} catch(ArrayIndexOutOfBoundsException e) {
	    return interBlockUntil(start, c);
	}
	int end = curPosition;
	curPosition ++; // one past end
	return new String(content, start, end - start);
    }
    
    /** The complex case of <code>until</code>: if the string to be read
     *  transcends the boundary of a buffer block. (Benja:) I think this 
     *  doesn't need to be as efficient, so I've coded it to be readable.
     *  <code>;o)</code>
     */
    String interBlockUntil(int start, int c) {
	s.setLength(0); // clean contents of string buffer
	s.append(content, start, content.length - start);
	
	int i;
	while((i=sread()) != c) {
	    s.append((char)i); // does this append a character-- or a number?
	}
	
	return s.toString();
    }

    /** Read the next buffer block in.
     */
    void nextBlock() {
	if(reader == null)
	    throw new Error("unexpected EOF (no reader set)");
	
	blockPosition += Math.min(curPosition, content.length);
	int n;
	
	// XXX should *throw* IOException...

	try {
	    n = reader.read(content);
	} catch(IOException e) {
	    throw new Error("IOException: "+e+" "+e.getMessage());
	}
	
	if(n < 0)
	    throw new Error("unexpected EOF");
	else if(n < BLOCK) {
	    char[] ncontent = new char[n];
	    System.arraycopy(content, 0, ncontent, 0, n);
	    content = ncontent;
	}

	curPosition = 0;
    }

    static String idChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ:;,+-_$";
    static boolean[] isIdChar = new boolean[128];
    static {
	for(int i=0; i<idChars.length(); i++)
	    isIdChar[idChars.charAt(i)] = true;
    }

    byte[] cellId() {
	int in = sread();
	int startPos = curPosition;
	try {
	    if(in == 'C') {
		// Read until a ' ', checking the chars.
		int i = 0;
		for(; content[curPosition+i] != ' '; i++) {
		    int c = content[curPosition+i];
		    if(c > 127 || isIdChar[c] == false)
			throw new Error("Invalid char in Id: "+c);
		}
		byte[] ret = new byte[i];
		for(int j = 0; j<i; j++) {
		    ret[j] = (byte)content[curPosition + j];
		}
		curPosition += i + 1; // one past;
		return ret;
	    }
	} catch(ArrayIndexOutOfBoundsException _) {
	    curPosition = startPos;
	}
	String s = until(' ');
	switch(in) {
	case 'C': 
	    byte[] arr = new byte[s.length()];
	    for(int i=0; i<s.length(); i++) {
		int c = s.charAt(i);
		if(c > 127 || isIdChar[c] == false)
		    throw new Error("Invalid char in Id: "+c);
		arr[i] = (byte)c;
	    }
	    return arr;
	case 'c':
	    return HexUtil.hexToByteArr(s);
	default:
	    throw new Error("Unexpected char "+in);
	}
    }

    void handleSimpleDim(GZZ1Handler.SimpleDim h) {
	boolean hadPlus = false;
	byte[] id1, id2;
	int in;
	while(true) {
	    switch(in = sread()) {
	    case '-':
		if(hadPlus) throw new Error("Can't mingle - and +");
		id1 = cellId();
		id2 = cellId();
		if(sread() != 0x0a) throw new Error("Expected newline");
		if(h != null) h.disconnect(id1, id2);
		break;
	    case '+':
		hadPlus = true;
		id1 = cellId();
		id2 = cellId();
		if(sread() != 0x0a) throw new Error("Expected newline");
		if(h != null) h.connect(id1, id2);
		break;
	    case '0':
		if(sread() != 0x0a) throw new Error("Expected newline");
		if(h != null) h.close();
		return;
	    default:
		throw new Error("Unexpected char "+in);
	    }
	}
    }

    int legacyContentSpec() {
	int i;
	switch(i=sread()) {
	case 't':
	    until(0x0a);
	    return getPosition() - 2;
	default:
	    throw new Error("Unexpected char "+i);
	}
    }

    void handleLegacyContent(GZZ1Handler.LegacyContent h) {
	int in;
	byte[] id;
	int first, last;
	while(true) {
	    switch(in = sread()) {
	    case 'k':
		id = cellId();
		legacyContentSpec();
		first = getPosition() + 1;
		last = legacyContentSpec();
		break;
	    case 'K':
		id = cellId();
		first = getPosition() + 1;
		last = legacyContentSpec();
		break;
	    case '0':
		if(sread() != 0x0a) throw new Error("Expected newline");
		if(h != null) h.close();
		return;
	    default:
		throw new Error("Unexpected char "+in);
	    }
	    try {
                if(h != null) h.transcludeLegacyContent(id, first, last);
	    } catch(IOException e) {
		e.printStackTrace();
		throw new Error("Problem with legacy content loading: "+e);
	    }
	}
    }

    void handleNewCells(GZZ1Handler.NewCells h) {
	int in;
        while(true) {
            switch(in = sread()) {
            case 'n':
		if(h != null) h.newCell(cellId());
		if(sread() != 0x0a) throw new Error("Expected newline");
                break;
            case '0':
                if(sread() != 0x0a) throw new Error("Expected newline");
                if(h != null) h.close();
                return;
            default:
                throw new Error("Unexpected char "+in);
	    }
	}
    }

    void handleTranscopy(GZZ1Handler.Transcopy h) {
        int in;
        while(true) {
            switch(in = sread()) {
            case 't':
                if(h != null) h.transcopy(cellId());
                if(sread() != 0x0a) throw new Error("Expected newline");
                break;
            case '0':
                if(sread() != 0x0a) throw new Error("Expected newline");
                if(h != null) h.close();
                return;
            default:
                throw new Error("Unexpected char "+in);
            }
        }
    }

    void handleSpanTransclusions(GZZ1Handler.SpanTransclusion h) {
	int in;
	while(true) {
	    switch(in = sread()) {
	    case 's':
		byte[] tid = cellId();
		String block = until(' ');
		String num1 = until(' ');
		String num2 = until('\n');
		int n1 = Integer.parseInt(num1);
		int n2 = Integer.parseInt(num2);
		try {
		    if(h != null) h.transclude(tid, new Mediaserver.Id(block), n1, n2);
		} catch(IOException e) {
		    e.printStackTrace();
		    throw new Error("Problem with transclusion loading: "+
				    block+" "+e);
		}
		break;
	    case '0':
                if(sread() != 0x0a) throw new Error("Expected newline");
                if(h != null) h.close();
                return;
            default:
                throw new Error("Unexpected char "+in);
	    }
	}
    }


    /** Read a GZZ1 file from the given Reader, passing events to the handler.
     */
    static public void read(Reader r0, GZZ1Handler h) throws IOException {
	new GZZ1Reader().readIn(r0, h);
    }
    static public void read(char[] content, GZZ1Handler h) {
	new GZZ1Reader().readIn(content, h);
    }

    void readIn(Reader r0, GZZ1Handler h) throws IOException {
	this.content = new char[BLOCK];
	this.reader = r0;
	nextBlock();

	readIn(h);

	r0.close();
    }

    void readIn(char[] content, GZZ1Handler h) {
	this.content = content;
	this.reader = null;
	
	readIn(h);
    }

    void readIn(GZZ1Handler h) {

	curPosition = 0;
	
	if(sread() != 'G' ||
	   sread() != 'Z' ||
	   sread() != 'Z' ||
	   sread() != '1' ||
	   sread() != 0x0a) throw new Error("Invalid header");
	int ver = Integer.parseInt(until(0x0a));
	if(ver != 1) throw new Error("Invalid format version");
	String prevId = until(0x0a);

	h.start(
	    prevId.length() == 0 ? null :
		    new Mediaserver.Id(prevId));
	while(true) {
	    int i;
	    switch(i=sread()) {
	    case 'N':
		if(sread() != 0x0a) throw new Error("Expected newline");
		handleNewCells(h.newCellsSection());
		break;
	    case 'T':
		byte[] tid = cellId();
		String msid = until(0x0a);
		handleTranscopy(h.transcopySection(tid, new Mediaserver.Id(msid)));
		break;
	    case 'D':
		byte[] id = cellId();
		if(sread() != 0x0a) throw new Error("Expected newline");
		handleSimpleDim(h.dimSection(id));
		break;
	    case 'E':
		if(sread() != 0x0a) throw new Error("Expected newline");
		handleLegacyContent(h.legacyContentSection());
		break;
	    case 'S':
		if(sread() != 0x0a) throw new Error("Expected newline");
		handleSpanTransclusions(h.spanTransclusionSection());
		break;
	    case '0':
		if(sread() != '0' ||
		    sread() != 0x0a) 
		    throw new Error("Expected zero and newline");
		h.close();
		p("End readIn");
		return;
	    default:
		pa("Unexpected char "+i+". Printing out context:");
		pa(new String(content));
		throw new Error("Unexpected char "+i+".");
	    }
	}
	
    }

}
