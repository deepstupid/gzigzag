#!/usr/bin/perl -w
# Written by Antti-Juhani Kaijanaho for the GZigZag project

use strict 'vars';

sub main {
    my @dirs = ();
    my $dn = "";
    push @dirs, ".";
    DIRS : while ($#dirs >= 0) {
        $dn = pop @dirs;
	print STDERR "Finding files in `$dn'...";
        open(ENTRIES, "<$dn/CVS/Entries") || next DIRS;
        while (<ENTRIES>) {
            if (m|^(D?)/([^/]+)/-?([0-9]+(\.[0-9]+)*)?/(dummy timestamp)?|) {
                my $dir = ($1 eq "D");
                my $name = $2;
		if (defined $5) {
		    print STDERR "Ignoring $name\n";
		} elsif ($dir) {
                    push @dirs, "$dn/$name";
                } else {
                    print "$dn/$name\n";
                }
            } elsif (/^D$/) {
                # ignore
            } else {
                print STDERR "###$_###";
                die "syntax error"
            }
        }
        close(ENTRIES);
        print STDERR "done.\n";
    }
}
main();
