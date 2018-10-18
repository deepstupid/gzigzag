/*   
TestSimpleObsTrigger.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
import org.gzigzag.*;
import junit.framework.*;

public class TestSimpleObsTrigger extends TestObsTrigger {
public static final String rcsid = "$Id: TestSimpleObsTrigger.java,v 1.1 2001/04/16 12:34:40 tjl Exp $";

    public TestSimpleObsTrigger(String name) { super(name); }

    public void setUp() {
	o = new SimpleObsTrigger();
    }
}
