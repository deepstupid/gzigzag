# A script to convert wordnet files to ZZ.
# Uses Lingua::Wordnet from CPAN.

$N = 10000;
$NLETT = 3;

use Lingua::Wordnet;

$wn = new Lingua::Wordnet;


for my $k (keys %{$wn->{indexhash}}) {
	print $k,"\n" if ++$n % 1000 == 0;
	last if $n > $N;
	local $_ = $k;
	s/\%.*//;
	# Only nouns.
	@synsets = $wn->lookup_synset($_, "n");
	next if !@synsets;
	push @words, $_;
}

@words = sort @words;

$id = 1;
$txt = "";
$conns = "";
$pref = "";

$hiercon = "";
@hier = ();

$conns .=qq{<DIMENSION name="d.n-words">};
# Cells for all words.
print "WORDS\n";
undef $prev;
for(@words) {
	my $prevhier;
	if($pref ne substr $_, 0, $NLETT) {
		for my $i (1..$NLETT) {
			my $sub = substr($_, 0, $i);
			if(substr($pref, 0, $i) ne $sub) {
				$sub = qxml($sub);
				$txt .= 
				  qq{<CELL id="$id" data="TEXT">$sub</CELL>\n};
				if(defined $hier[$i]) {
					$conns .= qq{
					 <CONN neg="$hier[$i]" pos="$id"/>};
				}
				$hier[$i] = $id;
				if(defined $prevhier) {
					$hiercon .= qq{
					 <CONN neg="$prevhier" pos="$id"/>};
				}
				$prevhier = $id;
				$id++;
			}
		}
		$pref = substr $_, 0, $NLETT;
	}
	my $t = qxml($_);
	$txt .= qq{<CELL id="$id" data="TEXT">$t</CELL>\n};
	$conns .= qq{<CONN neg="$prev" pos="$id"/>\n} if defined $prev;
	if(defined $prevhier) {
		$hiercon .= qq{
		 <CONN neg="$prevhier" pos="$id"/>\n};
	}
	$prev = $id;
	$words{$_} = $id++;
}
$conns .= qq{</DIMENSION>};

$conns .= qq{ <DIMENSION name="d.hier">$hiercon</DIMENSION> };

# Look for synsets.
print "SYNSETS\n";
$conns .=qq{<DIMENSION name="d.n-synsets">};
for my $w (@words) {
	@synsets = $wn->lookup_synset($w, "n");
	$prev = $words{$w};
	for my $synset (@synsets) {
		my $offs = $synset->offset();
		if(!exists $synsets{$offs}) {
			$gloss = qxml($synset->gloss());
			$txt .= qq{<CELL id="$id" data="TEXT">$gloss</CELL>\n};
			$synsets{$offs} = $id++;
		}
		$txt .= qq{<CELL  id="$id" data="TEXT"/>\n};
		$conns .= qq{<CONN neg="$prev" pos="$id"/>\n};
		$prev = $id;
		$ssw{$offs."%".$w} = $id++;
	}
}
$conns .= qq{</DIMENSION>};

# Connect words inside synset.
print "WORDS IN SYNSETS\n";
$conns .=qq{<DIMENSION name="d.synset">};
for my $k (keys %synsets) {
	$synset = $wn->lookup_synset_offset($k);
	my @w = $synset->words();
	$prev = $synsets{$k};
	for my $w (@w) {
		$w =~ s/\%.*//;
		next unless exists $words{$w};
		$c = $ssw{$k."%".$w};
		die "NO SSW '$k' '$w'"  unless defined $c;
		$conns .= qq{<CONN neg="$prev" pos="$c"/>\n};
		$prev = $c;
	}
}
$conns .= qq{</DIMENSION>};
undef %ssw;
undef @words;
undef %words;

# Hypo- and hypernyms.
print "HYPO\n";
$conns .=qq{<DIMENSION name="d.hypo">};
for my $k (keys %synsets) {
	@hypos = $wn->lookup_synset_offset($k)->hyponyms();
	$prev = $synsets{$k};
	for my $h (@hypos) {
		my $offs = $h->offset();
		next  unless exists $synsets{$offs};
		$txt .= qq{<CELL id="$id" data="TEXT"/>\n};
		$conns .= qq{<CONN neg="$prev" pos="$id"/>\n};
		$prev = $id;
		$hyp{$offs."%".$k} = $id++;
	}
}
$conns .= qq{</DIMENSION>};

print "HYPER\n";
$conns .=qq{<DIMENSION name="d.hyper">};
for my $k (keys %synsets) {
	@hyper = $wn->lookup_synset_offset($k)->hypernyms();
	$prev = $synsets{$k};
	for my $h (@hyper) {
		$lid = $hyp{$k."%".$h->offset()};
		next unless defined $lid;
		$conns .= qq{<CONN neg="$prev" pos="$lid"/>\n};
		$prev = $lid;
	}
}
$conns .= qq{</DIMENSION>};
undef %hyp;


open F, ">out.xml";
print F qq{<ZZSpace homeid="1000"> }, $txt, $conns, "</ZZSpace>";

sub qxml {
	local $_ = $_[0];
	s/&/&amp;/g;
	s/</&lt;/g;
	s/>/&gt;/g;
	s/"/&quot;/g;
	return $_;
}
