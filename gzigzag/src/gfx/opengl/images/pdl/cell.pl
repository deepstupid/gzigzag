use PDL;

require 'lib.pl';

my $size = float zeroes(400, 250);

for my $cellno (0..15) {

    print "$cellno\n";

    my $x = $size->xlinvals(-100, 100);
    my $y = $size->ylinvals( -75, 75);

    my ($h) = 
	cellcoords($x, $y, 150, 100, 35, 15, 
		    "", # "up left down right",
	    # Distort the coordinates
	    sub {
		my $h = shift;
		my($dx, $dy) = blendf(
		    sub{
			my($x, $y) = @_;
			return (0, 0);
			# return ($x, $y);
			# my $mult = 
			 #    spots_1_3($x / 100, $y / 100, 0.5, 0.2, 0.4);
			# return (map { $mult * $_} 
			 #    noise_2_2($x / 200, $y / 200)) ; 
		    }, $h);
		for(qw/X NX BNX/) { $h->{$_} += 10 * $dx }
		for(qw/Y NY BNY/) { $h->{$_} += 10 * $dy }
	    });
    $::h = $h;

    my $cdmult = blendf(
	    sub {
		my($x, $y) = @_;
		my $t = 30 * fBm_1_3($x / 80, $y / 80, 0.5, 
			1.8, # + noise_1_3($x / 150, $y / 150, 0.5),
			((0.5 + noise_1_3($x / 500, $y / 500, 0.5)) / 1.4) ,
			    8);
		# $t *= spots_1_3($x / 100, $y / 100, 0.5, 0.2, 0.4) ;
		return $t;
	    }, $h);
    $h->{CellDist} += $cdmult;

    sub flat {
	my($x) = @_;
	my $max = abs(max($x));
	my $min = abs(min($x));
	my $div = 2 * ($max > $min ? $max : $min);
	return 0.5 + $x / $div;
    }


    my $outmask = ($h->{CellDist} > 0);
    my $edgemask = $outmask + ($h->{CellDist} < -5);

    # Then, the inside of the cell.
    my($r, $g, $b);

    my $pieces = blendf(sub {
	my($x, $y) = @_;
	my $div1 = noise_1_2($x / 1000, $y / 1000);
	my $div2 = noise_1_2($x / 1000, $y / 1000 + 12420);
	$x = $x / (100 + 10 * $div1);
	$y = $y / (100 + 10 * $div2);
	spots_1_3($x, $y, 0.5, 0.0, 0.5);
    }, $h);

    my $offs = 100;
    ($r, $g, $b) = map {blendf(
	sub {
	    my($x, $y) = @_;
	    $x = $x + $offs;
	    $offs += 10052.3;
	    1 - 0.5 * (noise_1_2($x / 10000, $y / 10000) + 0.5) *
			(noise_1_2($x / 1000, $y / 1000) + 0.5)
	      - 2 * $pieces * noise_1_2($x / 1700, $y / 1700);
	}, $h)} (0, 1, 2);

    ($r, $g, $b) = map { $_->clip($outmask, $edgemask) } ($r, $g, $b);

    wpic((cat $r, $g, $b)->mv(2,0), "cell$cellno.pnm");
    system("qiv cell$cellno.pnm&"); # if $cellno == 0;
}

for my $x (0..3) {
    my $files = join ' ', map { "cell".(4*$x + $_).".pnm" } 0..3;
    system("pnmcat -leftright $files >cells$x.pnm");
}
my $files = join ' ', map { "cells$_.pnm" } 0..3;
system("pnmcat -topbottom $files >allcells.pnm");



