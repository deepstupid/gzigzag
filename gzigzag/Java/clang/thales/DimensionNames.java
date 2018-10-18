/*   
DimensionNames.java
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
 * Written by Antti-Juhani Kaijanaho.
 */

package org.gzigzag.clang.thales;

import org.gzigzag.*;

/** Parametrized names of Thales Clang special dimensions.  This class
    allows easy renaming of these dimensions should one become
    necessary. */
class DimensionNames {
    // Note: the members should not be final!  (Allows substituting
    // this class alone to a precompiled GZigZag.)

    // syntax
    public static String sequence = "d.thales-sequence";
    public static String literal = "d.thales-literal";
    public static String argument = "d.thales-argument";
    public static String procedure = "d.thales-procedure";
    public static String primitives = "d.thales-internal-primitives";
    public static String primbinding = "d.thales-internal-binding";
    public static String syntaxform = "d.thales-internal-syntactic-form";
    public static String syntaxformlist = "d.thales-internal-syntactic-form-list";

    // runtime
    public static String tasks = "d.thales-rt-active-tasks";
    public static String stack = "d.thales-rt-stack";
    public static String stackptr = "d.thales-rt-stackptr";
    public static String activations = "d.thales-rt-denoter-activations";
    public static String environment = "d.thales-rt-environment";
    public static String applicable = "d.thales-rt-applicable";

}

