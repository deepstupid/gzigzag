/*   
JoystickListener.java
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

/** An interface for receiving joystick events.
 */

public interface JoystickListener {
    /** An axis was moved.
     * @param ms The timestamp of the event, in milliseconds
     * 		compatible with System.currentTimeMillis().
     */
    void axis(long ms, int axis, float value);
    void button(long ms, int button, boolean value);
}
