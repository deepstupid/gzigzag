/*   
SimpleContentFile.java
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
import java.util.*;
import java.io.*;

/** A simple content file format.
 * The format consists of records: s(cell)(string), S(cell)(span),
 * t(time4). Currently spans are stored in their stringized format 
 * but this may be changed later.
 * <p>
 * The record types are defined by the ASCII values of the above characters.
 * <p>
 * Note the similarity with SimpleDimFile.
 */

public class SimpleContentFile implements ContentStorer, GZZ0.Runner {
public static final String rcsid = "$Id: SimpleContentFile.java,v 1.6 2000/09/19 10:32:00 ajk Exp $";

    final byte rString = 115;
    final byte rSpan = 83;

    ContentStorer sto;
    public void startRead(ContentStorer sto) {
	this.sto = sto;
    }
    public void endRead() {
	sto = null;
    }

    public int getContentType() {
	return 43;
    }
    public int getContentVersion() {
	return 0;
    }


    public void readRun(InputStream is) {
	DataInputStream dis = new DataInputStream(is);
	boolean eofOk = true;
	try {
	    while(true) {
		eofOk = true;
		byte rectype = dis.readByte();
		eofOk = false;
		switch(rectype) {
		case rString: {
		    String id = dis.readUTF();
		    String ct = dis.readUTF();
		    sto.putContent(id, ct); }
		    break;
		case rSpan: {
		    String id = dis.readUTF();
		    String cts = dis.readUTF();
		    Span sp = Span.parse(cts);
		    sto.putContent(id, sp); }
		    break;
		default:
		    throw new ZZFatalError("Invalid record");
		}
	    }
	} catch(EOFException e) {
	    if(!eofOk)
		throw new ZZError("Unexpected eof in simpledimfile");
	} catch(IOException e) {
	    ZZLogger.exc(e);
	    throw new ZZFatalError("Reading file");
	}
    }

    DataOutputStream dos;

    public void putContent(String id, Object ct) {
	if(ct == null)
	    throw new ZZFatalError("Tried to store null content!");
	try {
	    if(ct instanceof String) {
		dos.writeByte(rString);
		dos.writeUTF(id);
		dos.writeUTF((String)ct);
	    } else if(ct instanceof Span) {
		dos.writeByte(rSpan);
		dos.writeUTF(id);
		dos.writeUTF(((Span)ct).toString());
	    } else {
		throw new ZZFatalError("Can't put odd object: "+ct);
	    }
	} catch(IOException e) {
	    ZZLogger.exc(e);
	    throw new ZZFatalError("Reading file");
	}
    }

    public void startWrite(OutputStream dos) {
	this.dos = new DataOutputStream(dos);
    }

    public void endWrite() {
	try {
	    dos.flush();
	    dos = null;
	} catch(IOException e) {
	    ZZLogger.exc(e);
	    throw new ZZFatalError("Reading file");
	}
    }
    
}
