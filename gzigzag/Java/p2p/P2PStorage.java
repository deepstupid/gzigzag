/*   
P2PStorage.java
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

/** The physical layers of the mediaserver p2p layer.
 * This is a mechanism layer; no policy is included here.
 * For example, uncache unconditionally deletes the given span
 * from the cache. Higher-up levels are expected to make e.g. reference
 * counting functionality available to yet higher levels.
 * <p>
 * This level simply associates unique ids to permanent, immutable
 * blocks of bits: the important invariant is that 
 * get(id, offs, len) always returns either the same bits or
 * an error if the bits are unobtainable at that time.
 * <p>
 * Later on, digital signature functionality will be provided 
 * to help this.
 */

public interface P2PStorage {
String rcsid = "$Id: P2PStorage.java,v 1.4 2001/04/15 11:00:43 tjl Exp $";

   /** Convert a string to a unique id object.
    */
   UniqueID str2id(String s);

   /** Get the length of the bits pointed to by the given id.
    */
   long length(UniqueID id);

   /** Get the bytes from the given id.
    */
   byte[] get(UniqueID id);

   /** Get an input stream from the given id.
    */
   java.io.InputStream getInputStream(UniqueID id);

   /** Store the given block in the cache.
    */
   void cache(UniqueID id);

   /** Remove the given block from the cache.
    */
   void uncache(UniqueID id);

   /** A structure showing a single cached entry.
    */
   class InCache {
      UniqueID id;
      /** For future extension.
       */
      long offs, len;
   }
   /** Get a listing of the local cache/storage.
    */
   InCache[] getDirectory();

   /** Create a new globally unique id that can then be written to.
    */
   UniqueID.Writable newUniqueID();
}

