/*   
SoundObs.java
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
import java.util.*;
import java.io.*;

/** Called by SoundScroll when things happen.
 */
public interface SoundObs {
String rcsid = "$Id: SoundObs.java,v 1.3 2000/09/19 10:31:58 ajk Exp $";

	int STARTED = 42;
	int PLAYING = 43;
	int STOPPED = 44;
	void playingAt(int event, long l);
}

