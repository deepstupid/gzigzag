package org.gzigzag.mediaserver.storage;
import java.io.*;
import java.util.*;

/** A storer storing everything in memory, not on the disk.
 */
public class TransientStorer implements Storer {

    private Map data = new HashMap();
    private Properties properties = new Properties();

    public Set getKeys() throws IOException {
	return Collections. unmodifiableSet(data.keySet());
    }

    public InputStream retrieve(String key) throws IOException {
	byte[] b = (byte[])data.get(key);
	if(b == null) return null;
	else return new ByteArrayInputStream(b);
    }

    public OutputStream store(final String key) throws IOException {
        return new ByteArrayOutputStream() {
	    public void close() throws IOException {
		super.close();
		data.put(key, toByteArray());
	    }
	};
    }

    public File getFile(final String key) { return null; }

    public String getProperty(String p) {
        return properties.getProperty(p);
    }
    public void setProperty(String name, String data) throws IOException {
        properties.setProperty(name, data);
    }


}
