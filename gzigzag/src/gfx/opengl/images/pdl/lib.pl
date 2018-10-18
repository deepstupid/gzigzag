use PDL;
use blib;
use PDL::Perlin;
# use PDL::Graphics::TriD;

use PDL::IO::Pic;

use strict;

our $doblend = 0;

sub cond {
    my($c, $x, $y) = @_;
    return $y + $c * ($x-$y);
}

sub blendf {
    my($func, $h) = @_;
    if(!$doblend) {
	return $func->($h->{NX}, $h->{NY});
    }
    my @a = $func->($h->{NX}, $h->{NY});
    my @b = $func->($h->{BNX}, $h->{BNY});
    my @ret;
    for my $index (0..$#a) {
	$ret[$index] = cond($h->{BlendFact}, $a[$index], $b[$index]);
    }
    if(!wantarray()) { return $ret[0]; }
    return @ret;
}

sub cellcoords {
    my($x, $y, $cw, $ch, $conrad, $conthick, $conns, $distort) = @_;
#    my $pixw = $cw + 2 * $conrad;
#    my $pixh = $ch + 2 * $conrad;
#    my $em = 1 + $extra;
#    my $x = $p->xlinvals(-$pixw/2 * $em,$pixw/2 * $em);
#    my $y = $p->ylinvals(-$pixh/2 * $em,$pixh/2 * $em);

    my $nx = $x + rand 1000000;
    my $ny = $y + rand 1000000;

    my $xa = abs($x) - $cw/2;
    my $ya = abs($y) - $ch/2;
    # my $mask = (($xa < $conrad) & ($ya < $conrad));
    my $mask = 1;

    my $ww = ($cw/2 + $conrad);
    my $wh = ($ch/2 + $conrad);

    my ($blendx, $blendy, $blend, $bx, $by);
    if($doblend) {
	$blendx = abs($x) - $ww;
	$blendy = abs($y) - $wh;
	$blend = cond($blendx > $blendy, $blendx, $blendy)
			->clip(-20, 0) / -20;
	$bx = cond($blendx > $blendy,
			cond($x > 0, $x - $ww, $x + $ww),
			$x);
	$by = cond($blendy > $blendx,
			cond($y > 0, $y - $wh, $y + $wh),
			$y);
    }

    $distort->({
	X => $x, 
	Y => $y,
	NX => $nx,
	NY => $ny,
	BlendFact => $blend,
	BNX => $bx,
	BNY => $by
    }) if $distort;

    $xa = abs($x) - $cw/2;
    $ya = abs($y) - $ch/2;

    my $sq = cond($xa > $ya , $xa , $ya);

    my($dx, $dy);

    if($conns =~ /up|down/) {
	my $r;
	if($conrad > 0) {
	    $dx = abs($x) - $conrad - $conthick/2;
	    $dy = abs($y) - $ch/2 - $conrad ;
	    $r = $conrad - sqrt ( $dx*$dx + $dy*$dy );
	} else {
	    $dx = abs($x) - $conthick / 2;
	    $dy = 0;
	    $r = $dx;
	}

	my $cond = 1;
	if($conns !~ /up/) { $cond *= ($y < 0) }
	if($conns !~ /down/) { $cond *= ($y > 0) }
	# $sq = cond( $cond * ($dx < 0) & ($r < $sq) , $r , $sq);
	$sq = cond( $cond * ($dx < $sq) * ($r < $sq) , $r , $sq);
    }

    if($conns =~ /left|right/) {
	my $r;
	if($conrad > 0) {
	    $dx = abs($x) - $cw/2 - $conrad ;
	    $dy = abs($y) - $conrad - $conthick/2;
	    $r = $conrad - sqrt ( $dx*$dx + $dy*$dy );
	} else {
	    $dx = 0;
	    $dy = abs($y) - $conthick / 2;
	    $r = $dy;
	}

	my $cond = 1;
	if($conns !~ /right/) { $cond *= ($x < 0) }
	if($conns !~ /left/) { $cond *= ($x > 0) }
	# $sq = cond( $cond * ($dy < 0) & ($r < $sq) , $r , $sq);
	$sq = cond( $cond * ($dy < $sq) * ($r < $sq) , $r , $sq);
    }

    return {
	X => $x, 
	Y => $y,
	NX => $nx,
	NY => $ny,
	CellDist => $sq, 
	Mask => $mask, 
	BlendFact => $blend, 
	BNX => $bx, 
	BNY => $by};

}

1;
