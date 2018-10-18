use PDL;
use PDL::Math;
use PDL::Graphics::TriD;
use PDL::IO::FastRaw;

$turb = readfraw("turb.32t");
$spots = readfraw("spots.3t");
$col = readfraw("col.33t");
$cell = readfraw("cell.2t");


sub makeslice {
    my ($size, $x0, $y0, $z0, $xu, $yu, $zu, $xv, $yv, $zv) = @_;

    my $m = 1-1/$size;

    my ($x, $y, $z);
    $x = $x0 + zeroes($size, 1)->xlinvals(0, $xu*$m) 
	+ zeroes(1, $size)->ylinvals(0, $xv*$m);

    $y = $y0 + zeroes($size, 1)->xlinvals(0, $yu*$m) 
	+ zeroes(1, $size)->ylinvals(0, $yv*$m);

    $z = $z0 + zeroes($size, 1)->xlinvals(0, $zu*$m) 
	+ zeroes(1, $size)->ylinvals(0, $zv*$m);
     
    return ($x, $y, $z);
}

sub makecoords2d {
    my ($size) = @_;
    my $m = 1-1/$size;

    return ( zeroes($size, $size)->xlinvals(0, $m),
	     zeroes($size, $size)->ylinvals(0, $m) );
}


sub wrap {
    my @arr = @_;
    for $x (@arr) { $x -= floor($x); }
    return @arr;
}

sub clamp {
    my $size = shift;
    my @arr = @_;
    for $x (@arr) { clip(inplace($x), 0, 1-1/$size); }
    return @arr;
   
}

sub index2 {
    my ($pdl, $x, $y) = @_;

    my @d = dims $pdl;
    my $split = 0;

    #print "dims: @d\n";
    
    if (@d > 3 || @d < 2) { die "foo" };
    if (@d == 3) {
	$c = shift @d;
	$split = 1;
    } else {
	$c = 1;
    }

    #print "x: $x";
    #print "y: $y";

    $ind = ((($y * $d[1])->long() + $x) * $d[0])->long();

    #print "The indices are: ", $ind, "\n";
    
    if ($split) {
	my $cmp = sequence($c, 1, 1);
	return flat($pdl)->index(($ind * $c)->dummy(0) + $cmp);
    } else {
	return flat($pdl)->index($ind);
    }
}

sub index3 {
    my ($pdl, $x, $y, $z) = @_;

    my @d = dims $pdl;
    my $split = 0;
    
    if (@d > 4 || @d < 3) { die "foo" };
    if (@d == 4) {
	$c = shift @d;
	$split = 1;
    } else {
	$c = 1;
    }

    #print "x: $x";
    #print "y: $y";
    #print "z: $z";


    $ind = ((((($z * $d[2])->long() + $y) * $d[1])->long() + $x) * $d[0])->long() ;

    #print "The indices are: ", $ind, "\n";

    if ($split) {
	my $cmp = sequence($c, 1, 1);
	return flat($pdl)->index(($ind * $c)->dummy(0) + $cmp);
    } else {
	return flat($pdl)->index($ind);
    }
}    

sub writeslice {
    my $file = shift;
    my $pdl = shift;
    writefraw(index3($pdl, wrap(makeslice(@_)))->byte(), $file);
}


my $size = 400;

if(0){

writeslice("turbslice", $turb, $size,  0, 0, 0,  0, 1, 0,  1/sqrt(2), 0, 1/sqrt(2));

writeslice("turbside0", $turb, $size,  0, 1, 0,  1, 0, 0,  0, -1, 0);
writeslice("turbside1", $turb, $size,  0, 1, 1,  1, 0, 0,  0, 0, -1);
writeslice("turbside2", $turb, $size,  0, 0, 1,  0, 1, 0,  0, 0, -1);

writeslice("spotsslice", $spots, $size,  0, 0, 0,  0, 1, 0,  1/sqrt(2), 0, 1/sqrt(2));

writeslice("spotsside0", $spots, $size,  0, 1, 0,  1, 0, 0,  0, -1, 0);
writeslice("spotsside1", $spots, $size,  0, 1, 1,  1, 0, 0,  0, 0, -1);
writeslice("spotsside2", $spots, $size,  0, 0, 1,  0, 1, 0,  0, 0, -1);
}

# Get 2d turb slice 
$ts = index3($turb, wrap(makeslice($size,  0, 0, 0,  0, 1, 0,  1/sqrt(2), 0, 1/sqrt(2))));

# Offset 2d coords by it
my ($x, $y) = makecoords2d($size);

#print "_x: $x\n";
#print "_y: $y\n";

sub makesigned {
    my $x = shift;
    return $x - ($x >= 128) * 256;
}

$offx = makesigned($ts->index(0)) * .003;
$offy = makesigned($ts->index(1)) * .003;

#print "offx: $offx\n";
#print "offy: $offy\n";

$x += $offx;
$y += $offy;

#print "offset x: $x\n";
#print "offset y: $y\n";
#print "clamp x,y: ", clamp($x,$y);


# Index the cell image with the offsetted coords

my $image =  index2($cell, clamp($size, $x, $y))->byte();

#print "The final cell image: $image\n";

writefraw($image, "turbcell");
