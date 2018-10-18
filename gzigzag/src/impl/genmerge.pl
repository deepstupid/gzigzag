#!/usr/bin/perl

#
# The syntactic sugar
# 	iterate Type c through set.iterator() {
#		...
#		remove c; # // calls Iterator.remove();
#	}
#
#	iteratemap (Type key, Type2 value) through map {
#		...
#		remove key;
#	}
#
#
# These are translated, respectively, to
#	for(Iterator tmp = set.iterator(); tmp.hasNext();) {
#		Type c = (Type)tmp.next();
#		...
#		tmp.remove();
#	}
#
#	for(Iterator tmp = map.entrySet().iterator(); tmp.hasNext();) {
#		Map.Entry tmp2 = (Map.Entry) tmp.next();
#		Type key = tmp2.getKey();
#		Type2 value = tmp2.getValue();
#		...
#		tmp.remove();
#	}
#

for(@ARGV) {
    /\.java/ and die "SHouldn't have .java in input file names";
    open F, $_;
    $f = join '', <F>;
    close F;


    open OUT, ">$_.java";
    print OUT process($f);
    close OUT;

}

my $tmp = 42;

sub process {
    my($text) = @_;

    my $id = '[a-zA-Z]\w*';
    my $type = $id.'[\[\]]*';

    $text =~ s/\b(?:iterate|for)\s+
			((?:\($id\))?)\s*\b
			($type)\s+
			($id)\s+
			through\b
			([^{]+){
	    /$name = $1;
	     $type = $2;
	     $id = $3;
	     $expr = $4;
	     $name =~ s|[\(\)]||g;
	     if($name eq "") {$name = "gzz_i".($tmp++);}
	     "for(Iterator $name = $expr.iterator(); $name.hasNext();) { $type $id = ($type) $name.next(); ";
	    /sxeg;

    # Make it look like computer-generated to avoid accidental mods
    #
    $text =~ s/\n[ \t]+/\n/g;
    # Delete first line break for the notice 
    # in order not to change line numbers.
    $text =~ s/\n//;

    $text = <<END . $text;
/* THIS IS GENERATED CODE! DO NOT MODIFY! Modify the original, $_ instead. Any changes will be lost when regenerated.  */
END
    return $text;

}
