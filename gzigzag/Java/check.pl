#
# $Id: check.pl,v 1.2 2000/03/13 00:19:29 tjl Exp $
#
# A small script to check some gzigzag coding rules.

# Ouch -- fool RCS
$idstr = 'Id:.*';

for(@ARGV) {
	/\.java/ or die "Sorry, only .java files";
	print "$_:\n";
	open F, "$_";
	my ($clsrcs, $ifrcs, $long, $length);
	while(<F>) {
		/^public static final String rcsid = "\$$idstr\$";/
			and $clsrcs = 1 and next;
		/^String rcsid = "\$$idstr\$";/
			and $ifrcs = 1 and next;
		s/\t/        /g;
		chomp;
		$length = length if length > $length;
	}
	print "\tNo RCS id\n" unless $clsrcs or $ifrcs;
	print "\tOverlong line (longest $length)\n" if $length > 80;
	    
}
