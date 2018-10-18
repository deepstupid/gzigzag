/*   
UniqueID.java
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

package org.gzigzag.p2p;

/** A unique bit-block identifier. 
 * These identifiers are supposed to be globally unique and
 * identify a single, continuous block of bits.
 * @see Phys
 */

public interface UniqueID {
String rcsid = "$Id: UniqueID.java,v 1.4 2001/04/15 11:00:43 tjl Exp $";

   /** A simple representation of the whole ID as a string.
    * Phys.str2id(s) is the inverse.
    */
   String toString();

   /** A writable version.
    */
   interface Writable extends UniqueID {
       /** Get a stream for writing into the object
	* represented by this unique id.
	* Naturally, this is only usable once; calling
	* it more than once results in an exception.
	* Closing the stream finalizes the object - 
	* it's not visible anywhere prior to that.
  	*/
       java.io.OutputStream getOutputStream();
   }
}


