/*
ZipStorer.java
*
* Copyright (c) 2002, OS programming group
*
* You may use and distribut under the term of either the GNU Lesser
* General Public License, either version 2.0 of the license or,
* at your choice, any later version. Auternativly, you may use and
* distribute under the terms of the XPL.
*
* See the LICENSE.lgpl and LICENSE.xpl files forthe specific term of
* the license.
*
* This software is distributed in hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MECHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the REDME
* file for more details.
*/
/*
 * Written by os group
 */

package org.gzigzag.mediaserver.storage;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/* (doc tbd)
 */

public class ZipStorer implements Storer {

    public ZipFile zipfile;
    Storer.DefaultPropertiesImpl def_prop_impl;
    
    public ZipStorer(ZipFile zipfile) throws IOException {
        this.zipfile = zipfile;
	def_prop_impl = new Storer.DefaultPropertiesImpl(this);
    }

    public OutputStream store(String key) throws IOException {
        throw new IOException();
    }
 
    public void setProperty(String name, String data) throws IOException {
        throw new IOException();
    }

    public File getFile(String key) throws IOException{
        return null;
    }

    public InputStream retrieve(String id) throws IOException {
	ZipEntry e = zipfile.getEntry(id);
	if(e != null)
	    return zipfile.getInputStream(e);
	else
	    return null;
    }

    public String getProperty(String s) {
        return def_prop_impl.getProperty(s);
    }

    public Set getKeys() throws IOException {
        Enumeration entries = zipfile.entries();
        Set result = new HashSet();

        for(; entries.hasMoreElements();) {
                ZipEntry e = (ZipEntry)entries.nextElement();
                String name = e.getName();
                result.add(name);
        }

        return result;
    }    
}
