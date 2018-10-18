/*   
TransferEncodingHandler.java
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
 * Written by Antti-Juhani Kaijanaho
 */

package org.gzigzag.mediaserver.http;

/** An abstract HTTP transfer-encoding handler.  Implementations of
 * concrete transfer-encodings should be subclasses of this class.
 * This class also operates the global transfer-encoding register that
 * is used to find implementations of transfer encodings.  Note that
 * instantiating a subclass of this class will register that instance,
 * so all transfer-encoding classes are singletons.  The register will
 * always contain handlers for the mandatory transfer-encodings.
 */
public abstract class TransferEncodingHandler {
    
    /** Register that this transfer-encoding handler handles the given
     * encodings.
     * @param encs An array of encodings that this class implements
     */
    protected TransferEncodingHandler(String[] encs) {
        synchronized (handlers) {
            for (int i = 0; i < encs.length; i++) {
                String enc = encs[i];
                if (handlers.containsKey(enc))
                    throw new Error("duplicate transfer encoding handler");
                handlers.put(enc, this);
            }
        }
    }

    /** Register that this transfer-encoding handler handles the given
     * encoding.
     * @param enc An encoding that this class implements
     */
    protected TransferEncodingHandler(String enc) {
        this(new String[] { enc });
    }

    /** Return an OutputStream that encodes the data being output
     * using this transfer encoding and writes the result to os. 
     * @param os An output stream where encoded data should be
     * written
     * @return An output stream for data to be encoded
     * @throws java.io.IOException There was a problem constructing
     * the output stream.
    */
    public abstract java.io.OutputStream encode(java.io.OutputStream os)
        throws java.io.IOException;

    /** Return an InputStream that reads data from is and decodes it,
     * at most transfer_length characters. 
     * @param is An input stream of encoded data
     * @param transfer_length The maximum number of bytes to read from is
     * @return An input stream of decoded data
     * @throws java.io.IOException There was a problem constructing
     * the input stream.
     */
    public abstract java.io.InputStream  decode(java.io.InputStream  is,
                                                long transfer_length)
        throws java.io.IOException;


    /** Return an InputStream that reads data from is and decodes it.
     * @param is An input stream of encoded data
     * @return An input stream of decoded data
     * @throws java.io.IOException There was a problem constructing
     * the input stream.
     */
    public java.io.InputStream decode(java.io.InputStream is) 
               throws java.io.IOException {
        return decode(is, -1);
    }

    /** Get a handler for the given encoding.
     * @param enc The encoding that needs to be handled
     * @return A handler for the encoding given as argument
     * @throws java.util.NoSuchElementException There is no handler
     * for that encoding
     */
    public static TransferEncodingHandler get(String enc)
        throws java.util.NoSuchElementException {
        synchronized (handlers) {
            if (!handlers.containsKey(enc))
                throw new java.util.NoSuchElementException
                    ("unsupported transfer encoding: " + enc);
            return (TransferEncodingHandler)handlers.get(enc);
        }
    }

    private static java.util.HashMap handlers = new java.util.HashMap();

     static {
        MandatoryEncodings.initialize();
     }

}

