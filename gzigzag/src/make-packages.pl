#!/usr/bin/perl

# Separate the packages index file into single package.html files.

my $destdir = shift;

my $header; my $inheader = 1;
my $footer; my $infooter;

my $tofile;
my %files;

while(<>) {
    if(m|<h1>(.*)</h1>|) {
	$inheader = 0;
	/IGNORE/ and next; # Ignore the first section
	if($1 eq Overview) {
	    $tofile = "overview.html";
	} else {
	    my $f = $1;
	    chomp $f;
	    $f =~ s/<.*?>//g;
	    $f =~ tr|\.|\/|;
	    $tofile = $f."/package.html";
	    next; # Don't write the H1
	}
    }
    if($infooter || m|</body>|) {
	$footer .= $_;
	$infooter = 1;
	undef $tofile;
    } elsif($inheader) { $header .= $_; next; }
    elsif($tofile) {
	$files{$tofile} .= $_;
    } els
}

# use Data::Dumper;
# print Dumper(\%files);

for(keys %files) {
    my $f = $destdir."/".$_;
    open O, ">$f" or die "Can't write $f";
    print O $header;
    print O $files{$_};
    print O $footer;
    close O;
}



