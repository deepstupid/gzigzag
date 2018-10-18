/*   
GZZ1Writer.java
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
import java.io.*;
import org.gzigzag.util.*;

/** A class for writing GZZ1 data.
 */

public class GZZ1Writer implements GZZ1Handler {
String rcsid = "$Id: GZZ1Writer.java,v 1.21 2001/08/09 23:46:04 bfallenstein Exp $";
    Writer w;

    void write(int c) {
	try {
	    w.write(c);
	} catch(IOException e) {
	    throw new Error("IOError");
	}
    }

    void write(String c) {
	try {
	    w.write(c);
	} catch(IOException e) {
	    throw new Error("IOError");
	}
    }

    void writeId(byte[] b) {
	for(int i=0; i<b.length; i++) {
	    if(GZZ1Reader.idChars.indexOf(b[i]) < 0) {
		// Write as hex string
		write("c");
		write(HexUtil.byteArrToHex(b));
		write(" ");
		return;
	    }
	}
	write("C");
	for(int i=0; i<b.length; i++)
	    write(b[i]);
	write(" ");
	
    }

    public GZZ1Writer(Writer w) {
	this.w = w;
    }

    // 
    // Implementation of GZZ1Handler
    //

    public void start(org.gzigzag.mediaserver.Mediaserver.Id previous) {
	String prev;
	if(previous == null) prev = "";
	else prev = previous.getString();
	write("GZZ1");
	write(0x0a);
	write("1");
	write(0x0a);
	write(prev);
	write(0x0a);
    }

    public SimpleDim dimSection(byte[] cellId) {
	write("D");
	writeId(cellId);
	write(0x0a);
	return new SimpleDim() {
	    public void disconnect(byte[] id1, byte[] id2) {
		write('-');
		writeId(id1);
		writeId(id2);
		write(0x0a);
	    }
	    public void connect(byte[] id1, byte[] id2) {
		write('+');
		writeId(id1);
		writeId(id2);
		write(0x0a);
	    }
	    public void close() {
		write("0"); write(0x0a);
	    }
	};
    }

    public LegacyContent legacyContentSection() {
	throw new Error("Cannot write legacy content. It's deprecated-- " +
			"that's why it's called *legacy* content.");
    }

    public NewCells newCellsSection() {
        write("N");
        write(0x0a);
        return new NewCells() {
            public void newCell(byte[] id) {
		write("n");
                writeId(id);
		write(0x0a);
            }
            public void close() {
                write("0"); write(0x0a);
            }
        };
    }

    public Transcopy transcopySection(byte[] transclusionId,
			    org.gzigzag.mediaserver.Mediaserver.Id spaceId) {
        write("T");
	writeId(transclusionId);
	write(spaceId.getString());
        write(0x0a);
        return new Transcopy() {
		public void transcopy(byte[] id) {
		    write("t");
		    writeId(id);
		    write(0x0a);
		}
		public void close() {
		    write("0"); write(0x0a);
		}
	    };
    }

    public SpanTransclusion spanTransclusionSection() {
        write("S");
        write(0x0a);
        return new SpanTransclusion() {
                public void transclude(byte[] transclusionId,
         org.gzigzag.mediaserver.Mediaserver.Id blockId, int first, int last) {
                    write("s");
		    writeId(transclusionId);
		    write(blockId.getString());
		    write(' ');
		    write(""+first);
		    write(' ');
		    write(""+last);
                    write(0x0a);
                }
                public void close() {
                    write("0"); write(0x0a);
                }
            };
    }


    public void close() {
	write("00");
	write(0x0a);
    }
}
