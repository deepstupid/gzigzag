#!/usr/bin/perl

@not = qw/
0000000008000000E951C69F2500047B509B19C36307791FF5813EE8F1C5DF461505ED93327A9F
0000000008000000E951C8305800044FD3E704860499CD06AC4FEF6B8B0232FEAC9548908CD189
/;

$not = join '|', @not;

for $file (<../../Z/*>,<../../Z-private/*>) {
    $head = `head -10 $file `;
    push @pdf, $file if $head =~ "Content-Type: application/pdf";
    push @ps, $file if $head =~ "Content-Type: application/postscript";
}

print "PDF: ", join ' ',@pdf, "\n";

$mstmpimg = "mstmpimg/";

# system "rm -rf $mstmpimg";
system "mkdir $mstmpimg";
system "mkdir $mstmpimg/tmp";

sub psystem($) {
    print "RUNNING: $_[0]\n";
    system($_[0]);
}

for $resolution (144, 72) {

    for $file (grep {!/$not/} @pdf) {
	$file =~ /b_(\w+)$/ or die "Invalid block name $file";
	$block = $1;
	# First, do into triple resolution
	$tres = $resolution * 4;

	if(!$do_all) {
	    if(-e "$mstmpimg/$block-$resolution-1") {
		print "Ignoring existing: $block\n";
		next;
	    }
	}

	$tmpfile = "$mstmpimg/tmp/$block-tmp";
	system "perl stripblock.pl $file >$tmpfile";
	psystem "(cd mstmpimg/tmp; gs -dBATCH -dNOPAUSE -sDEVICE=ppmraw -r$tres -sOutputFile=$block-tmp-$resolution-%d.ppm $block-tmp) ";
	for $image (<$mstmpimg/tmp/$block-tmp-$resolution-*.ppm>) {
	    $to = $image;
	    $to =~ s/-tmp-/-/;
	    $to =~ s/\.ppm//;
	    system "pnmscale -reduce 4 $image | pnmtopng >$to";
	    unlink $image;
	}
	system "mv $mstmpimg/tmp/$block* $mstmpimg/";
	unlink $tmpfile;
    }
}


