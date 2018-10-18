package org.gzigzag.mediaserver.storage;
import java.io.*;
import java.util.*;

public interface Storer {

    Set getKeys() throws IOException;

    OutputStream store(String key) throws IOException;
    InputStream retrieve(String key) throws IOException;

    /** Get a file that contains this key.
        @return A file that contains the content of this file, or null
        if such a file is not available (for example, the datum is
        transferred from the network and is not available on a file
        system. */
    File getFile(String key) throws IOException;

    String getProperty(String p);
    void setProperty(String name, String data) throws IOException;

    class DefaultPropertiesImpl {
        private Properties properties;
        private Storer store;
        
        public DefaultPropertiesImpl(Storer storer) throws IOException {
            this.store = storer;
            properties = new Properties(System.getProperties());
	    try {
		InputStream is = store.retrieve("properties");
		if(is!=null) properties.load(is);
	    } catch(Throwable t) {
		System.err.println("Warning: couldn't get properties in storer");
	    }
        }
        
        public String getProperty(String s) {
            return properties.getProperty(s);
        }
        
        public void setProperty(String name, String data) throws IOException {
            properties.setProperty(name, data);
            OutputStream os = store.store("properties");
            if (os == null) throw new IOException();
            properties.store(os, null);
        }
    }

}
