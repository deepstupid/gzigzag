/*   
AwtCursorIdentifier.java
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
 * Written by Kimmo Wideroos
 */

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** 
 */

public class AwtCursorIdentifier {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    private Color color;
    private String colorStr;
    private String id;
    //private boolean shared;
    
    public AwtCursorIdentifier(Color color, String id) { //, boolean shared) {
	this.color = color;
        this.colorStr = color.toString();
	this.id = id;
	//this.shared = shared;
    }
    
    public Color color() { return color; }
    public String colorString() { return colorStr; }
    public String id() { return id; }
    //public boolean shared() { return shared; }    
}
