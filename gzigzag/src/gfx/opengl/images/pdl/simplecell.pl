use PDL;
use PDL::IO::FastRaw;
require 'lib.pl';


sub docell {
    my($x, $y, $sides, $conrad, $conthick, $lineover, $cutrad, $file) = @_;
    my $h = cellcoords($x, $y, 200, 200, $conrad, $conthick, $sides, undef);

    my $origcd = $h->{CellDist};

    if($sides && $lineover) {
	my $h2 = cellcoords($x, $y, 200, 200, $conrad, $conthick, "", undef);
	$h->{CellDist} = cond($h2->{CellDist} <= 0 , $h2->{CellDist}, $h->{CellDist});
    }


    my $i = $h->{CellDist} < -10;
    my($r, $g, $b) = ($i, $i, $i);

    my $alpha = ($origcd <= $cutrad);
    $alpha->slice("(0),(0)") .= 0.5; # Make at least one semi-transparent point..

    if($cutrad) {
	# To avoid artifacts, color all the rest the same bg color
	my $mask = ($origcd > 0) ;
	$r = $r + $mask * .14;#0.22;
	$g = $g + $mask * .23;#0.29;
	$b = $b + $mask * .45;#0.54;
    }


    wpic((cat $r, $g, $b)->mv(2, 0),$file.".pnm");
    wpic($alpha,$file.".alpha.pgm");



    system("pnmtopng -alpha $file.alpha.pgm $file.pnm >$file");


}

for my $type (
    { CutRad => 15, ConRad => 50, ConThick => 50, Type => 0 },
    { CutRad => 15, ConRad => 0, ConThick => 50, Type => 1 },
    { CutRad => 15, ConRad => 50, ConThick => 50, Type => 2, LineOver => 1 },
    { CutRad => 15, ConRad => 0, ConThick => 50, Type => 3, LineOver => 1},
    { CutRad => 15, ConRad => 0, ConThick => 20, Type => 4 },
    { CutRad => 0, ConRad => 50, ConThick => 50, Type => 5 },
    { CutRad => 0, ConRad => 0, ConThick => 50, Type => 6 },
    { CutRad => 0, ConRad => 50, ConThick => 50, Type => 7, LineOver => 1 },
    )
{

my $size = float zeroes(201, 201);

# my $x = $size->xlinvals(-100, 100);
# my $y = $size->ylinvals(-100, 100);
my $x = $size->xlinvals(-120, 120);
my $y = $size->ylinvals(-120, 120);

docell($x, $y, "", $type->{ConRad}, $type->{ConThick}, $type->{LineOver}, $type->{CutRad},
			"tcell$type->{Type}.png");

my $size = float zeroes(151, 76);

my $x = $size->xlinvals(-75, 75);
my $y = $size->ylinvals(-150, -75);

docell($x, $y, "up down", $type->{ConRad}, $type->{ConThick}, $type->{LineOver}, $type->{CutRad},
			"tconn$type->{Type}.png");




}

my $size = float zeroes(151, 76);
my $x = $size->xlinvals(-75, 75);
my $y = $size->ylinvals(-195, -120);
docell($x, $y, "up down", 0, 20, 0, 0, "tconn8.png");

