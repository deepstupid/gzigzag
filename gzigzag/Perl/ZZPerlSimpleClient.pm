#   
#    ZZPerlSimpleClient.pm
#    
#    Copyright (c) 1999, Tuomas Lukka
#
#    You may use and distribute under the terms of either the GNU Lesser
#    General Public License, either version 2 of the license or,
#    at your choice, any later version. Alternatively, you may use and
#    distribute under the terms of the XPL.
#
#    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
#    the licenses.
#
#    This software is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
#    file for more details.


# Provides the same routines as are inside the C++ scriptAPI for
# Perl, but using the remote interface.

package ZZPerlSimpleClient;
use IO::Socket;
use base Exporter;
@EXPORT = qw/zzconnect get_cell new_cell delete_cell
	connect_cells insert_cells insert_cell disconnect get_text
	set_text/;

my $socket;
my $reqno = 1;

sub zzconnect {
	my($hostport, $id) = @_;
	$socket = IO::Socket::INET->new(
		PeerAddr => $hostport, # host:port
		Proto => tcp,
	) or die("No socket: $!");
	$socket->autoflush(1);
	$init = <$socket>;
	print "INI: $init\n";
	$socket->print("ZZ(0.03)($id)(1)\n");
}
sub request {
	my($req, $resp) = @_;
	$reqno++;
	$socket->print("$reqno $req");
	while(<$socket>) {
		print "REP: $_\n";
		next unless s/^$reqno\s+//;
		unless(/^$resp\b/) {
			die("INVALID RESPONSE $_\n");
		}
		if($resp eq "ok") { return }
		if($resp eq "cellno") {
			/^cellno\((.*?)\)/ or die("INV $_");
			return $1;
		}
		if($resp eq "text") {
			/^text\((.*?)\)\((.*?)\)/ or die("INVT $_");
			my $nb = $2;
			my $res;
			my $b;
			my $n;
			my $cur;
			while($nb > $n) {
				$cur = read $socket, $b, $nb - $n;
				if($cur == 0) {
					die("NOR BYT");
				}
				$n += $cur;
			}
			return $b;
		}
	}
}

sub get_cell {
	my($from, $dim, $dir) = @_;
	return request("get($from)($dim)($dir)(0)(0)\n", "cellno");
}
sub new_cell {
	my($from, $dim, $dir) = @_;
	return request("new($from)($dim)($dir)(0)(0)\n", "cellno");
}
sub delete_cell {
	my($cell) = @_;
	request("delete($cell)", "ok");
}
sub connect_cells {
	my($from, $dim, $other) = @_;
	return request("connect($from)($dim)($other)\n", "ok");
}
sub insert_cell {
	my($from, $dim, $dir, $other) = @_;
	return request("connect($from)($dim)($dir)($other)\n", "ok");
}
sub disconnect {
	my($from, $dim, $dir) = @_;
	return request("new($from)($dim)($dir)\n", "ok");
}
sub get_text {
	my($cell) = @_;
	return request("gettext($cell)\n", "text");
}
sub set_text {
	my($cell, $t) = @_;
	my $l = length $t;
	return request("settext($cell)($l)\n$t", "ok");
}

