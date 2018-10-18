#!/usr/bin/perl -w


# 
# Read in the filter list.
# Print out lines starting with + directly.
# Save the lines starting with - in the array @del for deleting
# the files from input later
#
open FILTER, "<manifest-filter.lst";
while (<FILTER>) {
    /^\+ *(.*)$/ and print "$1\n";
    /^- *(.*)$/ and push @del, $1; 
}

# Loop through the standard input, deleting lines that match regexes
# in @del.
LINE: while (<>) {
    for $f (@del) {
        /$f/ and next LINE;
    }
    print;
}

