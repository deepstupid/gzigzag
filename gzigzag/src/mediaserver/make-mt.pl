#!/usr/bin/perl -w

use strict;


open MT, "<mime.types" || die "cannot open mime.types";
open TARGET, ">MediaTypes.java" || die "cannot open MediaTypes.java";

print TARGET <<'END';
// DO NOT EDIT THIS FILE; IT IS GENERATED BY make-mt.pl FROM mime.types
/*   
MediaTypes.java
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

class MediaTypes {
    private static java.util.HashMap types = new java.util.HashMap();

    public static String getType(String ext) {
        Object o = types.get(ext);
        if (o == null) return null;
        return (String)o;
    }

    static {
END
  my %seen;
  while (<MT>) {
    if (/ ^\#/) { next; }
    if (/^([^ \t\/]+\/[^ \t\/]+)[ \t]+(.*)$/) {
      my $type = $1;
      $_ = $2;
      foreach (split) {
        if (defined $seen{$_}) { die "Duplicate definition of the extension $_"; }
        $seen{$_} = 1;
        print TARGET "        types.put(\"$_\", \"$type\");\n";
      }
    }
  }

print TARGET "    }\n}\n";

