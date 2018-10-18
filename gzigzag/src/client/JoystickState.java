/*   
JoystickState.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
import java.util.*;
import java.io.*;

/** The current state of a joystick.
 */

public class JoystickState implements JoystickListener {

    public int timestamp = 0;
    public long time;

    /** The current axis values, normalized to -1..1.
     */
    public float axes[] = new float[6];
    public boolean buttons[] = new boolean[20];

    public String toString() {
	return ""+
		    axes[0]+"\t"+axes[1]+"\t"+axes[2]+"\t"+
		    axes[3]+"\t"+axes[4]+"\t"+axes[5]+"\t"+
		    buttons[0]+"\t"+buttons[1]+"\t"+buttons[2]+"\t"+
		    buttons[3]+"\t"+buttons[4]+"\t"+buttons[5];
    }

    public void axis(long ms, int axis, float value) {
	axes[axis] = value;
	time = ms;
	timestamp ++;
    }

    public void button(long ms, int button, boolean value) {
	buttons[button] = value;
	time = ms;
	timestamp ++;
    }
}
