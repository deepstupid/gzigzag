use PDL;
use PDL::IO::Pic;

@images = map { rpic "$_.ppm" } 
    qw/ d1_125 d1_25 d1_375 d1_50 d1_625 d1_75 d1_875 d1end /;

$one = $images[0]->max;

$im = $images[0] * 0;

for(@images) {
    $im = lclip($im, $one-$_);
    $im *= 0.8;
}

$im = ($one-$im);

wpic $im, "composite.ppm";
