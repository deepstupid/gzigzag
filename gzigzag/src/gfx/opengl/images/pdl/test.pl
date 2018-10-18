use PDL;
use blib;
use PDL::Perlin;
use PDL::Graphics::TriD;

$m = float zeroes 1024, 1024;

$x = $m->xlinvals(0, 20);
$y = $m->ylinvals(0, 20);
$z = 5 * turbulence_1_3($x/3, $y/3, 0, 16);
# $z = 0.5;
$x = $x + 0.5 * turbulence_1_3($x, $y, 0.5, 16);

print "Into\n";
$spots = spots_1_3($x, $y, $z, 0.2, 0.5);
imagrgb [$spots];

# $p++; $p *= 0.5;

# imagrgb $p1 / 5;
# imagrgb $p2 / 5;
# imagrgb [$d1, $d2, $d1];


