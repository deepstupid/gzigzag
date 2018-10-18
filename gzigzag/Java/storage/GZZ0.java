/*   
GZZ0.java
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
import java.io.*;

/** An implementation of the GZZ0 file format.
 */

public class GZZ0 {
String rcsid = "$Id: GZZ0.java,v 1.7 2001/03/01 13:53:53 ajk Exp $";
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }
    final static void pa(String s) { System.out.println(s); }

    final int magic = 0x475a5a30; // "GZZ0"
    final int version = 0;
    final byte rTime = 116;

    public interface Runner {
	/** Returns the GZZ0 content type of this runner.
	 */
	int getContentType();
	/** Returns the GZZ0 content version of this runner.
	 */
	int getContentVersion();
	/** Read a run.
	 */
	void readRun(InputStream is);
	/** Start writing.
	 */
	void startWrite(OutputStream os);
	/** Finish writing.
	 */
	void endWrite();

    }

    StreamSet ss;
    String id;

    public GZZ0(StreamSet ss, String id) {
	this.ss = ss; this.id = id;
    }

    /** Read the latest dataset into the given runner.
     * @param r The runner into which to give the runs
     * @param maxtimestamp The maximum timestamp whose changes to include.
     * @return The latest timestamp read.
     */
    public int read(Runner r, int maxtimestamp) {
	InputStream is = ss.getInputStream(id);
	CountingStream counts = null;
	if(dbg) {
	    is = counts = new CountingStream(is);
	}
	DataInputStream dis = new DataInputStream(is);

	// Read header
	try {
	    int i = dis.readInt();
	    if(i != magic) 
		throw new ZZFatalError("Invalid file format! Doesn't begin with GZZ0 magic");
	    i = dis.readInt();
	    if(i != version)
		throw new ZZFatalError("Invalid file version! Not "+version);
	    i = dis.readInt();
	    if(i != r.getContentType())
		throw new ZZFatalError("Wrong content type!");
	    i = dis.readInt();
	    if(i != r.getContentVersion())
		throw new ZZFatalError("Wrong content version!");
	} catch(IOException e) {
	    ZZLogger.exc(e);
	    throw new ZZFatalError("Reading file");
	}

	// Read body.

	int lastTime = -1;
	boolean eofOk = true;
	byte[] byt = new byte[4096];
	try {
	    while(true) {
		eofOk = true;
		byte rectype = dis.readByte();
		eofOk = false;
		if(rectype != rTime)
		    throw new ZZFatalError("Invalid timestamp record! " + id + " " +
			(counts != null ? counts.getCount() : -1));
		lastTime = dis.readInt();
		int nbytes = dis.readInt();

		if(maxtimestamp > 0 && lastTime > maxtimestamp) {
		    dis.close();
		    return lastTime;
		}

		if(nbytes > byt.length)
			byt = new byte[nbytes];

		dis.readFully(byt, 0, nbytes);
		ByteArrayInputStream bis = 
			new ByteArrayInputStream(byt, 0, nbytes);
		r.readRun(bis);
	    }
	} catch(EOFException e) {
	    if(!eofOk)
		throw new ZZFatalError("Unexpected eof in simpledimfile");
	} catch(IOException e) {
	    ZZLogger.exc(e);
	    throw new ZZFatalError("Reading file");
	}
	return lastTime;
    }

    ByteArrayOutputStream outputStream;
    DataOutputStream dos;
    Runner runner;
    int timestamp;

    /** Write the changes back. 
     * XXX Should check runner's type...
     */
    public void startWrite(int timestamp, Runner r) {

	outputStream = new ByteArrayOutputStream();
	this.timestamp = timestamp;
	this.runner = r;

	runner.startWrite(new DataOutputStream(outputStream));
    }

    /** Finish writing. */
    public void endWrite(boolean forceStamp) {
	boolean ex = ss.exists(id);
	runner.endWrite();

	try {
	    OutputStream os = ss.getAppendStream(id);
	    dos = new DataOutputStream(os);
	    if(!ex) {
		// Write header.
		dos.writeInt(magic);
		dos.writeInt(version);
		dos.writeInt(runner.getContentType());
		dos.writeInt(runner.getContentVersion());
		dos.flush();
	    }
	    byte[] byt = outputStream.toByteArray();
	    if(byt.length == 0 && !forceStamp) {
		pa("Not writing "+id);
		return;
	    }
	    pa("Writing "+id+" "+byt.length);

	    dos.writeByte(rTime);
	    dos.writeInt(timestamp);
	    dos.writeInt(byt.length);
	    dos.write(byt);
	    dos.flush();
	    dos = null;
	    outputStream = null;
	    runner = null;
	} catch(IOException e) {
	    ZZLogger.exc(e);
	    throw new ZZFatalError("Writing file");
	}
    }

}
