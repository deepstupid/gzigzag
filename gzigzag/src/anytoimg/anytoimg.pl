#!/usr/bin/perl
$copyright = q{
/*   
anytoimg
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
};

# jpeg
# gif
# png

# ps
# pdf

# If generates a temporary file possibly useful in later conversions
# (e.g. pdf to ps) prints out its name.
# XXX Figure out good limitations for the semantics. How much control
# should Perl have, how much info transferred to Java.

# Parameters: 
#  1. a comma-separated list of accepted image types.
#  2. the name of the file to convert.
#  3. first page
#  4. number of pages
#  5..n. target files 
#
# XXX Resolution, location on page, antialias!

use Getopt::Long;
use Pod::Usage;

$dpi = 75;

GetOptions(
    'into=s' => \$into,
    'source=s' => \$source,
    'pages=s' => \$pages,
    'dpi=s' => \$dpi,
    'antialias' => \$antialias,
    'help|?' => \$help,
    'man' => \$man,
);
pod2usage(1) if $help;
pod2usage(-verbose => 2) if $man;

@accept = split ',', $into;

if($source !~ /^[a-zA-Z0-9_\.-]+$/) {
    die("Invalid characters in file name $source");
}

# Check the file type of the source.
open IN, "<$source"; # XXX taint? metachar trouble?
if((read IN, $chr, 4) != 4) {
    die("Invalid read");
}
# print STDERR $chr;
if($chr eq "\%PDF") { $type = "PDF" }
elsif($chr =~ /^\%PS/) { $type = "PS" }
else { die "Unknown file type, started with '$chr'" }

# Convert PDF to PS first.
if($type eq "PDF") {
    $tmp = "tmp-1.ps";
    system("pdftops $source $tmp");
    print "$tmp\n";
    $source = $tmp;
    $type = "PS";
}

$curpage = $firstpage;
for my $target (@ARGV) {
    #
}

__END__

=head1 NAME

anytoimg - try to convert anything to an acceptable image

=head1 SYNOPSIS

anytoimg [options] -source=... -into=...

 Options:
    -into=format1,format2,...	
		a comma-separated list of accepted output formats
    -source=file	
		The input file
    -pages=page1,page2,... 
		a comma-separated list of pages to output
    -dpi=n	Output resolution, in dots per inch
    -antialias / -noantialias
		Whether to attempt to antialias the output. 
    -help	brief help message
    -man	full documentation


=head1 DESCRIPTION

This is a utility program for automated conversion of images 
in different types to an accepted image type. Currently,
it supports converting PS and PDF to jpeg, png or gif.

The current options are geared towards scalable vector graphics.

=head1 AUTHOR

Tuomas J. Lukka 2001

=cut
