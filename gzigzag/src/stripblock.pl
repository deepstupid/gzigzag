#!/usr/bin/perl
#
# Strip the mediaserver header off a block and print the result in standard output.
# One parameter only, for now.

# XXX inefficient
$s = join '',<>;

$s =~ s/^.*?\x0d\x0a\x0d\x0a//s;

print $s;
