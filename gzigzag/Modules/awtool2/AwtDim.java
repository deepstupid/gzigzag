/*   
AwtDim.java
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

/** Awt-dimensions
 */

public class AwtDim {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    // awt local (view wide) parameters
    public static String d_locals="d.awt_locals"; 

    // artefact link target&source dimensions 
    public static String link="d.awt_link";

    // dimensions for set-member -relationship
    public static String d_layerset="d.awt_layerset"; 
    public static String d_member="d.awt_member";

    // resulting artefact of logical operation applied 
    public static String d_result="d.awt_result";

    // all artefacts
    public static String d_artefact="d.awt_artefact";

    // general 
    public static String d_clone="d.clone";
    public static String d_1="d.1";
    public static String d_2="d.2";
    public static String d_nile="d.nile";
    public static String d_style = "d.awt_style";
}






