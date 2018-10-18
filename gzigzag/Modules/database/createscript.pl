#!/usr/bin/perl
#
# Create a sample database.
# Use the linux kernel CREDITS file as input.


print <<'STOP';
DROP TABLE members;
DROP TABLE positions;
DROP TABLE sigs;

CREATE TABLE members (
        id      INT4,
        name    TEXT,
        address TEXT,
        phone   TEXT,
        email   TEXT,
        PRIMARY KEY(id)
);

CREATE TABLE positions (
        person  INT4,
        sig     INT4
/*      type    TEXT */
);

CREATE TABLE sigs (
        id      INT4,
        name    TEXT,
        PRIMARY KEY(id)
);
STOP

$/ = "\n\n";

$id = 0;


while(<>) {
	/^\s*N:/ or next;
	s/'//g;
	my %h;
	for(split "\n") {
		/^([A-Z]): (.*)$/ or next;
		push @{$h{$1}}, $2;
	}
	$phone = int(1000000 + rand 9000000);
	$phone =~ s/(...)(....)/\1-\2/g;
	print "INSERT INTO members VALUES ($id, '$h{N}[0]', '$h{S}[0]', '$phone', '$h{E}[0]');\n";
	$id++;
	last if $id > 100;
}

$sid = 0;
for(qw/filesys mm drivers boot console vfs/) {
	print "INSERT INTO sigs VALUES ($sid, '$_');\n";
	$sid++;
}

my %pos;

for(1..120) {
	$pers = int rand $id;
	$sig = int rand $sid;
	next if $pos{$pers}{$sig}++;
	print "INSERT INTO positions VALUES ($pers, $sig);\n";
}

