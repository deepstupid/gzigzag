#!/usr/bin/perl -w
#############################################################################
#
#     
# old2xml.pl
# ==========
#
#     Copyright (c) 1999-2000, Ted Nelson, Tuomas Lukka and Vesa Parkkinen
#
#     You may use and distribute under the terms of either the GNU Lesser
#     General Public License, either version 2 of the license or,
#     at your choice, any later version. Alternatively, you may use and
#     distribute under the terms of the XPL.
#
#     See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
#     the licenses.
#
#     This software is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
#     file for more details.
#
#
#
#
# DON'T EXPECT THIS TO WORK !!
#
# However, if it doesn't work I'd like to know why, so please send me
# a patch to fix it or some kind of bug report.
#
# Author:
# =======
#
#   Vesa Parkkinen <veparkki@st.jyu.fi>
#
# Description:
# ===========
# 
#   This is a simple script to port ZigZag (Andrew Pam's perl version) spaces 
#   to xml. 
#
# Usage:
# =====
#
# First convert zigzag file to ASCII using zzdump from .
# Then convert that file to XML using this script.
#
#  zzdump oldfile > somefile
#  old2xml.pl < somefile > newfile.xml 
#
# 
#
# WARNING:
# =======
#
# This script leaves out all connections and cells that 
# have anything to do with d.cursor !!! 
# It also adds 1000 to cell ids, just to avoid collision with GZZ cell ids.
# ( This should be changed to something more elegant )
#
# 
#############################################################################


$_ = <>;

#%names;
print qq(
<?xml version="1.0" encoding="UTF-8"?>
<ZZ home="1">
);
my $buffer;
while (<>) {
  #print "line: $_\n"; 
  s/ {2,}//;

  if (/'(\d+)'.*/ or /^(\d+).*/) {
    
    $cellno = $1;
    
    /.*=>(.*)/;
    
    $val = $1;
    unless ( $val =~ /,/ ) {
      $val .= "\n" . <>; 
    }
    $val =~ s/'//g;
    # '
    $val =~ s/,//g;
    $val =~ s/^ //g;
    $val =~ s/ $//g;
    
    #print "$val\n";
    $names{$cellno} = $val;
  } else {
    $buffer .= $_;
  }
  
} 

#while ( ($key, $value) = each %names) {
#  print "KEY: $key = VALUE:$value\n";
#}

delete $names{0};

@lin = split /\n/,$buffer;

print qq(<Cell old="1" dim="d.3" dir="1" new="1000">home</Cell>\n);

#@lines = sort @lin; 
@lines = @lin;
#print "lines = @lines";

foreach (@lines) {
  
  if (/d\.cursor/) {
    goto NEW;
  }
  if (/'n' => 100/) {
    goto NEW;
  }
  /'(\d+)(.)(.+)'\s+=>\s+(\d+)/;
  
  $cell = $1;
  if ($2 eq "-"){ $dir = -1; }
  else {
    $dir = 1;
  }
  
  $dim   = $3;
  $other = $4;
  
  $cc  = $names{$other};
  $cc2 = $names{$cell};
  
  $nother = $other + 1000;
  $ncell  = $cell  + 1000;
  #print "cc = $cc, cc2 = $cc2\n";  
  if ($cc && ! $cc2) {
    
    print qq(<Cell old="$ncell" dim="$dim" dir="$dir" new="$nother">$cc</Cell>\n);
    delete $names{$other};
    
  } elsif ( $cc2 ) {
    
    $new_buf .= $_ . "\n";
    
  }
  else {
    $conn_buf .= $_ . "\n";
  }
 NEW:
}

while ($new_buf) {

  
  @new_lines = split /\n/,$new_buf;
  $new_buf = "";
  @new_lines = sort @new_lines;
  #print "NEW_LINES @new_lines /NEWLINES\n";
  
  foreach (@new_lines) {
    
    #print "LINE:$_\n";
    /'(\d+)(.)(.+)'\s+=>\s+(\d+)/;
    #print "S1 = $1  S2 = $2  S3 = $3  S4 = $4\n";
    $cell = $1;
    if ($2 eq "-"){ $dir = -1; }
    else {
      $dir = 1;
    }

    $dim  = $3;
    $other = $4;
    #print "S1 = $cell  S2 = $dir  S3 = $dim S4 = $other\n";  
    $cc = $names{$other};
    $cc2 = $names{$cell};
    #print "CELLS:cc = $cc, cc2 = $cc2\n";      
    $nother = $other + 1000;
    $ncell  = $cell  + 1000;
    
#    if (! $cc  || ! $cc2 ) {
#      print "CELLS2:cc = $cc, cc2 = $cc2\n";        
#      $new_buf .= $_ . "\n";
#      goto START;
#    }
    if ($cc && ! $cc2) {
    
      print qq(<Cell old="$ncell" dim="$dim" dir="$dir" new="$nother">$cc</Cell>\n);
      delete $names{$other};
      
    } elsif ( $cc2 ) {
      
      $new_buf .= $_ . "\n";
      
    }
    else {
      $conn_buf .= $_ . "\n";
    }
    
#    
#    print qq(<Cell old="$ncell" dim="$dim" dir="$dir" new="$nother">$cc</Cell>\n);
#    delete $names{$other};
  START:  
  }
}

if (! $conn_buf) {
  goto END;
}

@conn_lines = split /\n/,$conn_buf;

foreach (@conn_lines) {
  #print "$_\n";
  /'(\d+)(.)(.+)'\s+=>\s+(\d+)/;
  #print "S1 = $1  S2 = $2  S3 = $3  S4 = $4\n";
  $cell = $1;
  
  if ($2 eq "-"){ $dir = -1; }
  
  else {
    $dir = 1;
  }
  
  $dim  = $3;
  $other = $4;


  #print "S1 = $cell  S2 = $dir  S3 = $dim S4 = $other\n";  
  $cc = $names{$other};
  $other = $other + 1000;
  $cell  = $cell  + 1000;
  
  print qq(<Conn c1="$cell" dim="$dim" dir="$dir" c2="$other"/>\n)
}

END:
print "</ZZ>\n";
