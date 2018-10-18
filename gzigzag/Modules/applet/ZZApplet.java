/*   
ZZApplet.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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
 * Written by Benjamin Fallenstein
 */

package org.gzigzag;

import java.awt.*;
import java.applet.*;
import java.io.*;
import java.util.*;

public class ZZApplet extends Applet {
public static final String rcsid = "$Id: ZZApplet.java,v 1.12 2001/04/04 18:09:20 tjl Exp $";
    
    public void init() {
	// XXX Should really store the data somewhere. Currently ^S fails.
	ZZSpace space = new ZZCacheDimSpace(new DummyStreamSet());
	ZZDefaultSpace.create(space.getHomeCell());
	setLayout(new BorderLayout());
	ZZWindows.init(space,this);
	ZZObsTrigger.runObsQueue();
    }
}
