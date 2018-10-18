package org.gzigzag.mediaserver.storage;
import java.io.*;
import java.net.*;
import java.util.*;
import HTTPClient.*;

public class URLStorer implements Storer {

    HTTPConnection conn;
    String basepath;

    public URLStorer(String baseURL) throws IOException {
        conn = new HTTPConnection(new URL(baseURL));
	basepath = new URL(baseURL).getFile() + "/";
        def_prop_impl = new Storer.DefaultPropertiesImpl(this);
    }

    public OutputStream store(String key) throws IOException {
	try {
	    final HTTPResponse resp[] = new HTTPResponse[1];
	    HttpOutputStream os = new HttpOutputStream() {
		    public void close() throws IOException {
			super.close();
			HTTPResponse rsp = resp[0];
			try {
			    if (rsp.getStatusCode() >= 300)
				throw new IOException("PUT not successful.\nReceived Error: "+rsp.getReasonLine()+"\n"+rsp.getText());
			} catch (ModuleException e) {
			    throw new IOException("Error handling request: "+e.getMessage());
			} catch (ParseException e) {
			    throw new IOException("Error handling request: "+e.getMessage());
			}
		    }
		};
	    resp[0] = conn.Put(basepath + key, os);
	    return os;
	} catch (ModuleException me) {
	    throw new IOException("Error handling request: " + me.getMessage());
	}
    }

    public InputStream retrieve(String key) throws IOException {
	try {
	    HTTPResponse rsp = conn.Get(basepath + key);
	    if (rsp.getStatusCode() >= 300)
		throw new IOException("Received Error: "+rsp.getReasonLine()+"\n"+rsp.getText());
	    else
		return rsp.getInputStream();
        } catch (ModuleException e) {
            throw new IOException("Error handling request: "+e.getMessage());
	} catch (ParseException e) {
	    throw new IOException("Error handling request: "+e.getMessage());
	}
    }

    public Set getKeys() throws IOException {
	BufferedReader r = new BufferedReader(new InputStreamReader(retrieve("dirlist.cgi")));

	String s;
	Set res = new HashSet();
	while((s = r.readLine()) != null) {
	    res.add(s);
	}
	return res;
    }

    public File getFile(final String key) { return null; }

    public Storer.DefaultPropertiesImpl def_prop_impl;
    public String getProperty(String s) {
        return def_prop_impl.getProperty(s);
    }
        
    public void setProperty(String name, String data) throws IOException {
        def_prop_impl.setProperty(name,data);
    }
    

}
