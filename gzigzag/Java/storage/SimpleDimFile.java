/*   
SimpleDimFile.java
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

/** A simple dimension file format.
 * The format consists of records: c(cell)(cell), d+(cell), d-(cell),
 * Later to be extended with h(cell) which defines the
 * given cell to be the headcell of its rank.
 * <p>
 * The record types are defined by the ASCII values of the above characters.
 * <p>
 */

public class SimpleDimFile implements ZZDimStorer, GZZ0.Runner{
public static final String rcsid = "$Id: SimpleDimFile.java,v 1.6 2000/09/19 10:32:00 ajk Exp $";

    final byte rConn = 99;
    final byte rDisc = 100;
    final byte rPlus = 43;
    final byte rMinus = 45;

    

    ZZDimStorer sto;
    public void startRead(ZZDimStorer sto) {
	this.sto = sto;
    }
    public void endRead() {
	sto = null;
    }

    public int getContentType() {
	return 42;
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
		case rConn: 
		    String id1 = dis.readUTF();
		    String id2 = dis.readUTF();
		    sto.storeConnect(id1, id2);
		    break;
		case rDisc:
		    byte bdir = dis.readByte();
		    int dir;
		    if(bdir == rPlus)
			dir = 1;
		    else if(bdir == rMinus)
			dir = -1;
		    else
			throw new ZZFatalError("Invalid directon");
		    String id = dis.readUTF();
		    sto.storeDisconnect(id, dir);
		    break;
		default:
		    throw new ZZFatalError("Invalid record");
		}
	    }
	} catch(EOFException e) {
	    if(!eofOk)
		throw new ZZFatalError("Unexpected eof in simpledimfile");
	} catch(IOException e) {
	    ZZLogger.exc(e);
	    throw new ZZFatalError("Reading file");
	}
    }

    DataOutputStream dos;

    public void storeConnect(String a, String b) {
	try {
	    dos.writeByte(rConn);
	    dos.writeUTF(a);
	    dos.writeUTF(b);
	} catch(IOException e) {
	    ZZLogger.exc(e);
	    throw new ZZFatalError("Writing file");
	}
    }
    public void storeDisconnect(String a, int dir) {
	try {
	    dos.writeByte(rDisc);
	    dos.writeByte(dir > 0 ? rPlus : rMinus);
	    dos.writeUTF(a);
	} catch(IOException e) {
	    ZZLogger.exc(e);
	    throw new ZZFatalError("Writing file");
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
