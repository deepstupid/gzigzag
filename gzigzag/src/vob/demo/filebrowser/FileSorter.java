/*   
FileSorter.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.vob.demo;

import java.io.*;
import java.util.*;

/** A class sorting files according to some custom criterium.
 *  This is to decouple the order of files from the view. Note that file
 *  sorters can be hierarchical: if you pass another FileSorter in the
 *  constructor, it is automatically applied before this one, so you can
 *  sort according to different criteria (if two files are equal according to
 *  <em>this</em> file sorter, the order of the file sorter you constructed
 *  this sorter with will be used.
 *  <p>
 *  Default: don't change the sorting (usually leaves it alphabetically).
 */

public class FileSorter {
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) System.out.println(s); }
    protected static void pa(String s) { System.out.println(s); }
		
    FileSorter applyFirst = null;
    public FileSorter() {}
    public FileSorter(FileSorter applyFirst) { this.applyFirst = applyFirst; }
		
    /** Compare two files.
     *  Of course, and invariant is that if a<b and b<c, then a<c; this must
     *  always be preserved. Same goes for a<=b && b<=c, a==b && b==c etc.
     *  <p>
     *  Default: return that the files are equal, thus always preserving the
     *  original order.
     *  @returns -1 if f1 should appear before f2; 0 if they are equal, i.e.
     *           the original ordering shall be preserved; +1 if f1 should
     *		 appear after f2.
     */
    public int cmp(FileInfo f1, FileInfo f2) {
	return f1.f.getAbsolutePath().compareTo(f2.f.getAbsolutePath());
    }

    /** Sort the elements in the <code>files</code> array.
     *  Default: sort according to the <code>cmp()</code> criterium.
     */
    public void sort(File[] files) {
	if(applyFirst != null) applyFirst.sort(files);
	
	long millis = System.currentTimeMillis();
	
	FileInfo[] infos = new FileInfo[files.length];
	for(int i=0; i<files.length; i++)
	    infos[i] = FileInfo.get(files[i]);
	
	long millis2 = System.currentTimeMillis();
	
	// bubble sort
	for(int i=1; i<infos.length; i++) {
	    for(int j=i; j>0; j--) {
		if(cmp(infos[j], infos[j-1]) < 0) {
		    FileInfo swap = infos[j];
		    infos[j] = infos[j-1];
		    infos[j-1] = swap;
		} else
		    break;
	    }
	}
	
	for(int i=0; i<files.length; i++)
	    files[i] = infos[i].f;
	
	long millisAfter = System.currentTimeMillis();
	
	p("Sorted. It took me: "+(millisAfter-millis)+"; "+
	  "not counting getFileInfo, it took me "+(millisAfter-millis2)+".");
    }

    public static class SizeSorter extends FileSorter {
        public int cmp(FileInfo f1, FileInfo f2) {
	    if(f1.isDir) {
	        if(f2.isDir)
		    return 0;
	        else
		    return -1;
	    } else if(f2.isDir)
	        return +1;
		
	    long s1 = f1.length, s2 = f2.length;
	    if(s1 < s2) return -1;
	    else if(s1 == s2) return 0;
	    else return +1;
        }
    }

    public static class DateSorter extends FileSorter {
        public int cmp(FileInfo f1, FileInfo f2) {
	    long s1 = f1.lastModified, s2 = f2.lastModified;
	    if(s1 < s2) return -1;
	    else if(s1 == s2) return 0;
	    else return +1;
        }
    }

}

