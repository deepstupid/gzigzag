/*   
LinuxJoystick.java
 *    
 *    Copyright (c) 2000-2001, Tuomas Lukka
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
 * Written by Tuomas Lukka and Rauli Ruohonen
 */
package org.gzigzag.impl;
import java.util.*;
import java.io.*;

/** A simple class to use the Linux joystick API.
 * A separate thread is used to read in the blocking mode from the
 * joystick and the appropriate callbacks are made.
 */

public class LinuxJoystick implements Runnable {
public static final String rcsid = "$Id: LinuxJoystick.java,v 1.1 2001/10/21 12:58:12 tjl Exp $";
    public static boolean dbg = true;
    private static final void p(String s) { if(dbg) pa(s); }
    private static final void pa(String s) { System.err.println(s); }
    FileInputStream in;
    Thread t = new Thread(this);

    final JoystickState state = new JoystickState();
    public JoystickState getState() { return state; }

    JoystickListener listener;
    public void setListener(JoystickListener listener) {
	this.listener = listener;
    }

    /** Whether timeOffs has been initialized.
     */
    boolean haveTimeOffs = false;

    /** The difference between the joystick ms timestamps and
     * System.currentTimeMillis().
     */
    long timeOffs; 
    
    Calibrator calibrator;

    float adjust(int axis, int val) {
	return calibrator.axes[axis].adjust(val);
    }

    public LinuxJoystick(File f) {
	try {
	    calibrator = new Calibrator(new File("/home/tjl/.joystick"), 
			    f.toString());
	    p("Starting read");
	    in = new FileInputStream(f);
	    p("Opened");
	    t.start();
	    p("Started");
	} catch(Exception e) {
	    e.printStackTrace();
	    throw new Error("Can't start"+e);
	}
    }

    float DO_NOT_REPORT = 124.53f;
    
    public void run() {
	try {
	    p("Run thread");
	    byte[] bpacket = new byte[8];
	    int[] packet = new int[8];
	    while(true) {
		//		p("Start treading");
		int n = in.read(bpacket);
		//		p("Read "+n);
		for(int i=0; i<8; i++) {
		    packet[i] = bpacket[i];
		    if(packet[i] < 0) packet[i] += 256;
		}
		/*
		p("R: "+
		  packet[0]+" "+
		  packet[1]+" "+
		  packet[2]+" "+
		  packet[3]+" "+
		  packet[4]+" "+
		  packet[5]+" "+
		  packet[6]+" "+
		  packet[7]);
		*/
		    
		long ms = packet[0] + (packet[1]<<8) + (packet[2]<<16)
		    + (packet[3]<<24);
		if(!haveTimeOffs) {
		    timeOffs = System.currentTimeMillis() - ms;
		}

		ms += timeOffs;

		int value = packet[4]+((packet[5]&0x7F)<<8);
		if((packet[5]&0x80)!=0)
		    value -= 32768;
		int type = packet[6];
		int num = packet[7];

		// p("EV: "+ms+" "+value+" "+type+" "+num);
		// p("EV: "+value+" "+type+" "+num);

		// type = 1 -> button, type = 2 -> axis
		type &= ~0x80; // Don't differentiate between initialization
		if(type == 1) {
		    state.button(ms, num, value != 0);
		    if(listener != null)
			listener.button(ms, num, value != 0);
		} else if(type == 2) {
		    float val = adjust(num, value);
		    state.axis(ms, num, val);
		    if(val != DO_NOT_REPORT && listener != null)
			listener.axis(ms, num, val);
		} else {
		    pa("Strange type: "+type);
		}
	    }
	} catch(Exception e) {
	    p("Exception: " +e);
	    throw new Error("Couldn't run joystick");
	}
    }

    public void printLoop() throws Exception {
	while(true) {
	    Thread.sleep(100);
	    p("Cur: \t"+state);
	}
    }

    /** For testing. */
    public static void main(String argv[]) throws Exception {
	p("LinuxJoystick");
	LinuxJoystick j = new LinuxJoystick(new File(argv[0]));
	j.printLoop();
    }

    /** A class for reading and using jscalibrator files.
     */
    static public class Calibrator {
	/** The corrections for a single axis.
	 */
	class Axis {
	    int min;
	    int ctr;
	    int max;
	    int nullZone;
	    int tolerance;
	    boolean isHat;

	    /** Return an adjusted value for the given integer input.
	     */
	    public float adjust(int input) {
		if(isHat) {
		    return 0.0f; // XXX !!!
		}
		input -= ctr;
		if(input < 0) {
		    if(input > -nullZone) return 0;
		    input += nullZone;
		    return ((float)input) / (ctr - min - nullZone);
		} else {
		    if(input < nullZone) return 0;
		    input -= nullZone;
		    return ((float)input) / (max - ctr - nullZone);
		}
		
	    }

	    /** Parse a single Axis field from the jscalibrator file.
	     */
	    public void parse() throws IOException {
		while(true) {
		    Line l = Calibrator.this.parse();
		    if(l.key.equals("EndAxis")) return;
		    if(l.key.equals("Minimum"))
			min = l.i();
		    else if(l.key.equals("Center")) 
			ctr = l.i();
		    else if(l.key.equals("Maximum"))
			max = l.i();
		    else if(l.key.equals("NullZone"))
			nullZone = l.i();
		    else if(l.key.equals("Tolorance"))
			tolerance = l.i();
		    else if(l.key.equals("IsHat"))
			isHat = true;
		    else 
			throw new Error("Strange line");
		}
	    }
	}

	Axis[] axes = new Axis[10]; // XXX
	{
	    for(int i=0; i<axes.length; i++) axes[i] = new Axis();
	}

	/** A line of the file. Used during parsing.
	 */
	class Line {
	    String key;
	    String value;
	    /** Return the value as an integer.
	     */
	    int i() { return Integer.parseInt(value); }
	}

	BufferedReader reader;
	boolean eof = false;

	/** Parse a single line from the reader.
	 */
	private Line parse() throws IOException {
	    String s;
	    while(true) {
		s = reader.readLine();
		if(s == null) { eof = true; return null; }
		s = s.trim();
		if(s.startsWith("#")) continue;
		break;
	    }

	    Line l = new Line();
	    int i0 = s.indexOf(' ');
	    if(i0 < 0) { l.key = s; l.value = null; return l;}
	    l.key = s.substring(0, i0);
	    if(s.charAt(i0+1) != '=') throw new Error("Invalid line");
	    if(s.charAt(i0+2) != ' ') throw new Error("Invalid line");
	    l.value = s.substring(i0+3);
	    return l;
	}

	/** Parse the whole file and grab the given device.
	 */
	private void parseDevice(String correct) throws IOException {
	    while(true) {
		Line l = parse();
		if(l == null) return;
		if(!l.key.equals("BeginJoystick")) throw new Error("No beginjoystick found");
		if(l.value.equals(correct)) {
		    while(true) {
			l = parse();
			if(l.key.equals("EndJoystick")) break;
			if(!l.key.equals("BeginAxis")) 
			    throw new Error("No beginaxis");
			int axis = l.i();
			axes[axis].parse();
		    }
		} else {
		    // Skip this one
		    while(true) {
			l = parse();
			if(l.key.equals("EndJoystick")) break;
		    }
		}
	    }
	}

	public Calibrator(File f, String device) {
	    try {
		reader = new BufferedReader(new FileReader(f));

		parseDevice(device);
	    } catch(IOException e) {
		e.printStackTrace();
		throw new Error(" "+e);
	    }
	}
    }

}
