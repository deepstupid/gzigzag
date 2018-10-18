/*   
ZZMbox.java
 *    
 *    Copyright (c) 2000, Tuomas Lukka
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

/** A simple class to use the Linux joystick API.
 * A separate thread is used to read in the blocking mode from the
 * joystick and the appropriate callbacks are made.
 */

public class LinuxJoystick implements Runnable {
public static final String rcsid = "$Id: LinuxJoystick.java,v 1.4 2001/06/13 21:42:31 tjl Exp $";
	public static final boolean dbg = true;
	static final void p(String s) { if(dbg) System.out.println(s); }
	static final void pa(String s) { System.out.println(s); }

	FileInputStream in;
	Thread t = new Thread(this);

	public LinuxJoystick(File f) {
	try {
		in = new FileInputStream(f);
		t.start();
	} catch(Exception e) {
		throw new Error("Couldn't open joystick");
	}
	}

	public synchronized void run() {
	try {
		byte[] bpacket = new byte[8];
		int[] packet = new int[8];
		while(true) {
			int n = in.read(bpacket);
			p("Read "+n);
			for(int i=0; i<8; i++) {
				packet[i] = bpacket[i];
				if(packet[i] < 0) packet[i] += 256;
			}
			p("R: "+
				packet[0]+" "+
				packet[1]+" "+
				packet[2]+" "+
				packet[3]+" "+
				packet[4]+" "+
				packet[5]+" "+
				packet[6]+" "+
				packet[7]);

			long ms = packet[0] + (packet[1]<<8) + (packet[2]<<16)
				+ (packet[3]<<24);
			int value = packet[4]+((packet[5]&0x7F)<<8);
			if((packet[5]&0x80)!=0)
				value -= 32768;
			int type = packet[6];
			int num = packet[7];

			p("EV: "+ms+" "+value+" "+type+" "+num);
		}
	} catch(Exception e) {
		throw new Error("Couldn't open joystick");
	}
	}

	/** For testing. */
	static public void main(String argv[]) {
		LinuxJoystick j = new LinuxJoystick(new File(argv[0]));
	}

}
