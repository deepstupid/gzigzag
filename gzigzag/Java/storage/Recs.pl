#
# $Id: Recs.pl,v 1.8 2001/03/06 13:44:39 ajk Exp $
#
# Generate Recs.java for record types for file formats.
#
# Java Serializable is CLOSE but not quite close enough to what we need
# (e.g. it would write out the "offset" field)
# AND we want a standard binary format.


$file = $ARGV[0];
$file = "Recs.pl" if ($file eq "");
open F, ">$file" or die "Can't open file $file";
$rcsid = '$Id: Recs.pl,v 1.8 2001/03/06 13:44:39 ajk Exp $';
print F <<END;
/*   
Recs.java
 *
 * GENERATED USING Recs.pl - DO NOT MODIFY THE JAVA CODE:
 * YOUR CHANGES WILL BE OVERWRITTEN. MODIFY THE GENERATING
 * PERL CODE IN Recs.pl instead.
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka
 */
package org.gzigzag;
import java.util.*;
import java.io.*;

/** A generated class that defines a number of binary record
 * types.
 */

public class Recs {
static final void p(String s) { System.out.println(s); }

    static short getMagic(long offset) {
        return (short)(23415 ^ (offset % 30411));
    }
    public static class InvalidMagic extends ZZError {
        public InvalidMagic(String s) { super(s); }
    }
    static Record readRecord(long offs, DataInputStream s) {
    try {
	short mag = s.readShort();
	if(mag != getMagic(offs))
	    throw new InvalidMagic("Invalid record magic!"+offs+" "+mag+" "+getMagic(offs)+" ");
	short typ = s.readShort();

	Record r = createRecord(typ);

	// p("Before rd:\\n "+r);
	r.read_data(offs, s);
	// p("After rd:\\n "+r);
	return r;

	// if(getTypeNo() != typ)
	  //   throw new ZZError("Invalid record type!");
    } catch(Exception e) {
	ZZLogger.exc(e);
	throw new ZZError("IO ERROR!");
    }
    }

    static ByteArrayOutputStream bos = new ByteArrayOutputStream();
    static DataOutputStream os = new DataOutputStream(bos);
    static void writeRecord(Record r, Writable w) {
	bos.reset();
	writeRecord(r, os);
	w.write(r.offset, bos.toByteArray());
    }
    static void writeRecord(Record r, DataOutputStream s) {
	r.write(s);
    }

    static public abstract class Record {
	long offset;
	public long getOffset() { return offset; }
	public int length() { return 4; } // magic + type
	public abstract short getTypeNo();
	public void write_data(DataOutputStream s) throws IOException {
	    // Unsymmetric: read back in readRecord
	    s.writeShort(getMagic(offset));
	    s.writeShort(getTypeNo());
	}
	public void read_data(long offs, DataInputStream s) 
	    throws IOException {
	    offset = offs;
	    // Skip 4 bytes, as those were read by readRecord already.
	}
	public String toString() {
	    return "    Offset: "+offset+"\\n";
	}
	public void write(DataOutputStream s) {
	    try {
		write_data(s);
	    } catch(Exception e) {
		ZZLogger.exc(e);
		throw new ZZError("IOERRROR");
	    }
	}
	public void read(long offs, DataInputStream s) {
	    try {
		read_data(offs, s);
	    } catch(Exception e) {
		ZZLogger.exc(e);
		throw new ZZError("IOERRROR");
	    }
	}
	
    }

END

my $rettype;

@recs = (
[    TreeRecord, Record, "int hash; int prevwhash", 			42],
[    ConnRecord, TreeRecord, "String cell; short othoffs",		43],
[    ContentRecord, TreeRecord, "String cell; boolean span; String content",
									44],
[    HdrRecord, Record, "short nbits; int mask; long lasthash; long end; 
		    long id",	45],
[    HashRecord, Record, "short nbits", 				46],
[    RootRecord, Record, "long nextCell", 				47],
);

for(@recs) {

    $rettype .= "if(typ == $_->[3]) return new $_->[0](); \n";

    my @f = map { [split ' '] } split ';', $_->[2];
    my $len = join '+', 
	" super.length() ", 
	map {
	       $_->[0] eq "long" and 8
	    or $_->[0] eq "int" and 4
	    or $_->[0] eq "short" and 2
	    or $_->[0] eq "boolean" and 1
	    or $_->[0] eq "String" and 
		" (2 + $_->[1].getBytes(\"UTF8\").length) "
	    or die "Invalid type $_->[0]"
	} @f;

    my $write = join '', 
	map {
	       $_->[0] eq "String" and 
	         "{
		   byte[] bt = $_->[1].getBytes(\"UTF8\");
		   s.writeShort(bt.length);
		   s.write(bt, 0, bt.length);
		  }
		 "
	    # or $_->[0] eq "bool" and "s.writeBoolean($_->[1]);"
	    or "s.write".(ucfirst $_->[0])."($_->[1]);";
	} @f;

    my $read = join '', 
	map {
	       $_->[0] eq "String" and 
	         "{
		   short l = s.readShort();
		   byte[] bt = new byte[l];
		   int n = s.read(bt);
		   if(n!=l) throw new ZZError(\"NOT ENOUGH DATA\");
		   $_->[1] = new String(bt, \"UTF8\");
		  }
		 "
	    # or $_->[0] eq "bool" and "$_->[1] = s.readBoolean();"
	    or "$_->[1] = s.read".(ucfirst $_->[0])."();";
	} @f;

    my $tostring = join '',
	map {
	    qq(    + "    $_->[1]: "+$_->[1]+"\\n" )
	} @f;


    print F <<END

    static public class $_->[0] extends $_->[1] {
	$_->[2];
	public int length() { 
	try {
	    return $len; 
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    throw new ZZError("ARGH");
	}
	}
	public short getTypeNo() { return $_->[3]; }
	public void write_data(DataOutputStream s) throws IOException {
	    super.write_data(s);
	    $write
	}
	public void read_data(long offs, DataInputStream s) 
	    throws IOException {
	    super.read_data(offs, s);
	    $read
	}
	public String toString() {
	    return "$_->[0]:\\n" 
		$tostring
		+ super.toString();
	}
    }
END
}

print F qq[
    static public Record createRecord(int typ) {
	$rettype
	throw new ZZError("Invalid record type "+typ);
    }
}
];
