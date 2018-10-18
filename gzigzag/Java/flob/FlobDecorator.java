/*   
FlobDecorator.java
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
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** An interface for classes that decorate FlobSets by drawing connections
 * or so.
 */

public interface FlobDecorator {
String rcsid = "$Id: FlobDecorator.java,v 1.6 2000/10/26 18:09:29 tjl Exp $";
    /** Add Renderables or Flobs to the given flobset, 
     * based on the flobs already in there.
     */
    void decorate(FlobSet f, String path, ZZCell view);
}
