/*   
ReliableUDPMessageFactory.java
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
/* Written by Antti-Juhani Kaijanaho */

package org.gzigzag.net;

/** A factory creating ReliableUDPMessage objects for the use of a
 * ReliableUDPSocket.  
 * Typically each application of the reliable UDP
 * scheme implements this interface along with subclassing
 * ReliableUDPMessage.  
 */
public interface ReliableUDPMessageFactory {

    /** Create a ReliableUDPMessage object from the bytes given.  Note
     * that the seq, ack and optionally timestamp fields in the
     * ReliableUDPMessage need to be set from the message too.  */
    ReliableUDPMessage makeMessage(byte[] message);

    /** Create a fresh ReliableUDPMessage that is a no-op to a
     * receiver.  The return value is used to a acknowledge packets
     * when no payload is available for the ack message.  */
    ReliableUDPMessage makeNoOp();

}
