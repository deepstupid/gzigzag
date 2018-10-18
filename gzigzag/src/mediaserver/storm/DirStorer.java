package org.gzigzag.mediaserver.storage;
import java.io.*;
import java.util.*;

/** A storer storing things in a directory.
 */
public class DirStorer implements Storer {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.out.println(s); }

    private File dir;

    /** The maximum length of a filename/dirname used. 
     *  Must be at least 2. Should be an odd number because one char is for
     *  internal use, which means that the remaining number of chars will
     *  be even; thus, the two hex digits representing one byte won't be
     *  scattered.
     */
    private int maxlen = 15;

    public Storer.DefaultPropertiesImpl def_prop_impl;
    public String getProperty(String s) {
        return def_prop_impl.getProperty(s);
    }
        
    public void setProperty(String name, String data) throws IOException {
        def_prop_impl.setProperty(name, data);
    }
    

    public DirStorer(File dir, int maxlen) throws IOException { 
	this.dir = dir; 
        def_prop_impl = new Storer.DefaultPropertiesImpl(this);
	if(maxlen < 0) {
	    String ml = getProperty("dirstorer.maxlen");
	    if(ml == null) maxlen = 31;
	    else maxlen = Integer.parseInt(ml);
	}
	this.maxlen = maxlen;
	if(maxlen < 2)
	    throw new IllegalArgumentException("maxlen must at least be 2");
    }
    public DirStorer(File dir) throws IOException { 
	this(dir, -1); 
    }

    public Set getKeys() throws IOException {
	Set s = new HashSet();
	add_keys("", dir, s);
	return s;
    }

    private void add_keys(String prefix, File dir, Set s) {
        String[] d = dir.list();
        for(int i=0; i<d.length; i++) {
	    if(d[i].equals("CVS"))
		continue;
	    else if(d[i].length() < maxlen)
		s.add(prefix + d[i]);
	    else if(d[i].substring(maxlen-1).equals("_"))
		add_keys(prefix + d[i].substring(0, maxlen-1), 
			 new File(dir, d[i]), s);
	    else
		System.out.println("Strange file "+d[i]+" in "+dir);
	}
    }

    // For speed debugging: count accesses
    private static int store, retrieve, getFile;
    private static long getFile_size;

    public InputStream retrieve(String key) throws IOException {
	if(dbg)
	    pa("DirStorer.retrieve called "+(++retrieve)+" times.");
	return get_is(dir, key);
    }

    public File getFile(String key) throws IOException {
	File f = get_file(dir, key);
        if(dbg) {
	    getFile++;
	    getFile_size += f.length();
            pa("DirStorer.getFile called "+(getFile)+" times. "+
	       "Average file size: "+(getFile_size / getFile));
	}
	return f;
    }

    private InputStream get_is(File dir, String key) throws IOException {
        File f = get_file(dir, key);
        if (f == null) return null;
        return new BufferedInputStream(new FileInputStream(f), 8192);
    }

    private File get_file(File dir, String key) throws IOException {
	if(key.length() < maxlen) {
	    File f = new File(dir, key);
	    if(!f.isFile()) return null;
            return f;
	} else {
	    File sub = new File(dir, key.substring(0, maxlen-1) + "_");
	    if(!sub.isDirectory()) return null;
	    return get_file(sub, key.substring(maxlen-1));
	}
    }

    public OutputStream store(final String key) throws IOException {
        if(dbg)
            pa("DirStorer.store called "+(++store)+" times.");
	return get_os(dir, key);
    }

    private OutputStream get_os(File dir, String key) throws IOException {
	// System.out.println("get_os "+dir+" "+key);
	if(key.length() < maxlen) {
	    return new FileOutputStream(new File(dir, key));
	} else {
	    File sub = new File(dir, key.substring(0, maxlen-1) + "_");
	    if(!sub.exists()) sub.mkdir();
	    return get_os(sub, key.substring(maxlen-1));
	}
    }

}





