/*   
LazyBlock.java
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

package org.gzigzag.mediaserver;
import org.gzigzag.mediaserver.ids.*;
import java.io.*;

class LazyBlock implements Mediaserver.Block {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }

    public LazyBlock(IDSpace isp, Mediaserver.Id id, InputStream is, String source) {
        this.is = is;
        this.id = id;
        this.isp = isp;
        this.f = null;
        this.source = source;
    } 

    public LazyBlock(IDSpace isp, Mediaserver.Id id, File f, String source) {
        this.is = null;
        this.id = id;
        this.isp = isp;
        this.f = f;
        this.source = source;
    }

    /** Get the ID of this block.
     */
    public Mediaserver.Id getId() { return id; }
    /** Get the MIME Content-Type.
     */
    public String getContentType() throws IOException { 
        getCT();
        close();
        return contentType;
    }
    /** Get the decoded data.
     */
    public byte[] getBytes() throws IOException {
        slurpRaw();
        close();
        return bytes;
    }
    /** Get the raw data.
     */
    public byte[] getRaw() throws IOException { 
        slurpRaw();
        close();
        return raw;
    }
    /** Get a checked input stream.
     */
    public InputStream getInputStream() throws IOException {
	if(f == null)
	    return new ByteArrayInputStream(getBytes()); // XXX!
	
	// XXX get rid of double BufInpStream -> how is the performance best?
	InputStream s = new BufferedInputStream(new FileInputStream(f), 8192);
	InputStream res = new BufferedInputStream(isp.checkInputStream(id, s), 8192);

	slurpHeader();
	close();
	for(int i=0; i<hdr_end; i++)
	    res.read();

	return res;
    }
    /** Get the raw header, as a string.
     */
    public String getHeader() throws IOException { 
        slurpHeader();
        close();
        return header;
    }
    
    public String readNthLine(int n) throws IOException {
        slurpHeader();
	if(hdr_end < raw_n - 1) readRawChunk();
	int i = hdr_end;
	int start = -1, end = hdr_end;
	for(int l=n; l >= 0; l--) {
	    while(raw[i] != 0x0A) {
		//System.out.print(raw[i]+" ");
		if(raw[i] == 0x0D) pa("0x0D found");
		i++;
		if(i >= raw_n) readRawChunk();
	    }
	    //if(start >= 0)
		//pa("found newline. the line was: '"+new String(raw, start+1, end-start-1)+"' and the 5th char was: "+raw[start + 4]);
	    //else
		//pa("first line");
	    start = end; end = i;
	    i++;
	    if(i >= raw_n) readRawChunk();
	}
	close();
	if(start < 0) throw new IOException("line not found");
	//pa("About to return: '"+new String(raw, start+1, end-start-1)+"'");
	return new String(raw, start+1, end-start-1);
    }

    public String getSource() {
        return source;
    }

    private final Mediaserver.Id id;
    private final IDSpace isp;

    private byte[] bytes = null;
    private byte[] raw = new byte[16];
    private String header = null;
    private String contentType = null;
    private final String source;


    private int raw_n = 0; // this much of raw is valid
    private int hdr_end = -1;
    private boolean raw_done = false;

    private InputStream is;
    private final File f;
 
    private void close() throws IOException {
        if (is != null && f != null) {
            is.close();
            is = null;
        }
    }
   
    private void slurpHeader() throws IOException {
        if (header != null) return;
        
        StringBuffer sb = new StringBuffer();

        int n = 0;
        boolean done = false;
        while (true) {
            int i;
            for (i = n; i + 3 < raw_n; i++) {
                if (raw[i + 0] == 13 && raw[i + 1] == 10
                    && raw[i + 2] == 13 && raw[i + 3] == 10) {
                    done = true;
                    i += 4;
                    break;
                }
                if (raw[i + 0] == 13 && raw[i + 1] == 10) continue;
                sb.append((char)raw[i]);
            }
            n = i;
            if (done) break;
            boolean more = readRawChunk();
            if (!more) throw new IOException("Premature end of block " + id);
        }
        header = new String(sb);
        hdr_end = n;
        trimRaw();
    }

    private void getCT() throws IOException {
        slurpHeader();
        String hdr = "\n" + header;
        final String ct = "\ncontent-type: ";

	// Must recognize spellings like "content-TyPe" etc.
	// Note: Lowercase is only used to find the index, nothing else!
        int inx = hdr.toLowerCase().indexOf(ct);
        if (inx == -1) throw new IOException("No Content-Type in " + id.getString());
	
        // XXX assumes here that CT is not folded
        int inx2 = hdr.indexOf("\n", inx + ct.length());
        if (inx2 == -1) inx2 = hdr.length();
        p("EEE: " + (inx + ct.length()) + " " + inx2);
        contentType = hdr.substring(inx + ct.length(), inx2);
    }

    private boolean readRawChunk() throws IOException {
        return readRawChunk(128);
    }

    private boolean readRawChunk(int chunk) throws IOException {
        if (raw_done) return false;

        if (is == null) {
            is = new BufferedInputStream(new FileInputStream(f), 8192);
            is.skip(raw_n);
        }

        if (raw_n + chunk >= raw.length) {
            int new_n = raw.length;
            while (raw_n + chunk >= new_n) new_n *= 2;
            byte[] new_raw = new byte[new_n];
            for (int i = 0; i < raw_n; i++) new_raw[i] = raw[i];
            raw = new_raw;
        }

        if (chunk == 0) chunk = raw.length - raw_n;

        int read = is.read(raw, raw_n, chunk);
        if (read == -1) {
            trimRaw();
            if(!isp.checkData(id, raw))
                throw new Mediaserver.InvalidID("Invalid raw data for block " + id.getString());
            bytes = new byte[raw_n - hdr_end];
            System.arraycopy(raw, hdr_end, bytes, 0, bytes.length);
            is.close();
            is = null;
            raw_done = true;
            return false;
        }
        raw_n += read;
        return true;
    }

    private void slurpRaw() throws IOException {
        slurpHeader();
        while (readRawChunk(0));
    }

    private void trimRaw() throws IOException {
        byte[] newraw = new byte[raw_n];
        System.arraycopy(raw, 0, newraw, 0, raw_n);
        raw = newraw;
    }
}
