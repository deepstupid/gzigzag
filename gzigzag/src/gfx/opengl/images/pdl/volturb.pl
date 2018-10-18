use PDL;
use blib;
use PDL::Perlin;
use PDL::Graphics::TriD;
use PDL::IO::FastRaw;

require 'lib.pl';

$size = 128;
$size2 = 512;
$tilew = 20;

{
    my $size = float zeroes($size2, $size2);
    my $x = $size->xlinvals(-120, 120);
    my $y = $size->ylinvals(-120, 120);
    my $h = cellcoords($x, $y, 200, 200, 0, 0, "", undef);
    my $i = $h->{CellDist} < -10;
    my $alpha = $h->{CellDist} <= 0;

    writefraw(((cat $i, $i, $i, $alpha)*255)->byte()->mv(2, 0), "cell.2t");
}

sub tile {
    my($x) = @_;
    my $tilewm1 = $tilew-1;
    $sl1 = $x->slice("0:${tilewm1}");
    $sl2 = $x->slice("-${tilew}:-1");
    $sl1c = $sl1->copy();
    $sl2c = $sl2->copy();

    my $wt = $sl1c->xlinvals(0, 1);

    $sl1 .= $sl1c * $wt + $sl2c * (1-$wt);
    $sl2 .= $sl1c * $wt + $sl2c * (1-$wt);

    return $x->slice("1:$size")->sever();
}


sub tileall {
    my($x) = @_;
    $x = tile($x);
    $x = tile($x->xchg(0,1))->xchg(0,1);
    $x = tile($x->xchg(0,2))->xchg(0,2);
	return $x;
}

$tsize = $size + $tilew;


$s = float zeroes($tsize, $tsize, $tsize);

$x = zeroes($tsize,1,1)->xlinvals(-1, 1);
$y = zeroes(1,$tsize,1)->ylinvals(-1, 1);
$z = zeroes(1,1,$tsize)->zlinvals(-1, 1);

{ 
    my $spots = spots_1_3(2.5*$x, 2.5*$y, 2.5*$z, 0.0, 0.5);
	    # + 3.0*noise_1_3(2*$x, 2*$y, 2*$z), 0.3, 0.4);
    $spots = tileall($spots);
    writefraw(($spots*255)->byte(), "spots.3t");
}
exit 0;

{
    my $turb = fBm_1_3(2*$x, 2*$y, 2*$z, 1.9, 0.3+noise_1_3(4*$x,4*$y,4*$z), 10) * (0.3 + noise_1_3(6*$x,6*$y,6*$z));
    
    $turb = tileall($turb);
#    imagrgb [ $turb->slice(":,:,(50)") ];
    writefraw(($turb*128)->byte(), "turb.3t");

    my $turb2 = fBm_1_3(2*$x, 2*$y, 2*$z+15.5, 1.9, 0.3 + noise_1_3(4*$x,4*$y,4*$z+31.0), 10) * (0.3 + noise_1_3(6*$x+23.3,6*$y,6*$z));
    $turb2 = tileall($turb2);
    writefraw((cat($turb, $turb2)*128)->byte()->mv(3, 0), "turb.32t");
}

{ 
    my $col = cat(noise_3_3(5*$x, 5*$y, 5*$z))*0.5+0.5;
    $col = tileall($col);
    $col = $col->mv(3, 0);
    writefraw(($col*255)->byte(), "col.33t");
}


# points3d [ map { $_ + 0.01 * $_->random() } where($x, $y, $z, turbulence_1_3($x, $y, $z, 10) < 0.1) ];
