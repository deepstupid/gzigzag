/*   
BenchFS.java
 *    
 *    Copyright (c) 2002, Benja Fallenstein
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

package org.gzigzag.impl;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.util.TestingUtil;
import org.gzigzag.benchmark.Bench;
import java.io.*;

/** Benchmark the file system.
 *  To see whether it's the fs that's making gzz slow on Windows and MacOS.
 */


public class BenchFS {
public static final String rcsid = "$Id: BenchFS.java,v 1.3 2002/02/19 16:02:52 bfallenstein Exp $";
    static private void p(String s) { System.out.println(s); }

    static final String key =
"798432175980427398457239857290185729384569823675923875926598327459826577532";
    static final int nfiles = 10000;

    final byte[] array = new byte[1024];

    public void runBench(Bench b) throws IOException {
	File dir = TestingUtil.tmpFile(new File("."));
	dir.mkdir();
	final Storer s = new DirStorer(dir);
	write(s.store(key));
	
	b.run(new Object[] {
	    "READ5KB", "Read a file containing five kilobytes of zeros",
	    new Runnable() { public void run() {
		try {
		    read(s.retrieve(key));
		} catch(IOException e) {
		    e.printStackTrace();
		    System.exit(1);
		}
	    }},
	});

        System.out.println();
        System.out.print("Creating "+nfiles+" files now... ");

	long millis = System.currentTimeMillis();
	for(int i=1; i<=nfiles; i++) {
	    File f = TestingUtil.tmpFile(dir);
	    write(new FileOutputStream(f));
	    if(i%500 == 0)
		System.out.print(i+" ");
	}
	millis = System.currentTimeMillis() - millis;

	System.out.println("... took me "+millis+" milliseconds.");
	System.out.println();

        b.run(new Object[] {
            "READBIGDIR", "Read a file in a dir containing "+nfiles+" files",
            new Runnable() { public void run() {
                try {
                    read(s.retrieve(key));
                } catch(IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }},
        });

	TestingUtil.deltree(dir);
    }

    void read(InputStream is) throws IOException {
	int read;
	do {
	    read = is.read(array);
	} while(read >= 0);
	is.close();
    }

    void write(OutputStream os) throws IOException {
	for(int i=0; i<5; i++) {
	    os.write(array);
	}
	os.close();
    }

    public static void main(String[] argv) throws IOException {
	org.gzigzag.benchmark.Bench b = new org.gzigzag.benchmark.Bench(argv);

        System.out.println("Benchmark file system");
        System.out.println("=====================");
	System.out.println("");
	new BenchFS().runBench(b);
	System.out.println("");
	System.out.println("Please press ENTER to quit.");
	System.in.read();
	System.exit(0);
    }
}
