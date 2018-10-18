#   
#    ZZPerlDBServ.pm
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


package ZZPHash;
require Tie::Hash;
@ISA='Tie::StdHash';
sub TIEHASH {
	bless $_[1], $_[0];
}
sub STORE {
	$_[0]->{$_[1]} = $_[2];
	print "STORED: '$_[1]' '$_[2]'\n";
}
sub DELETE {
	my $r0 = $_[0]->{$_[1]};
	my $r = delete $_[0]->{$_[1]};
	print "DELETED: '$_[1]' ( = '$r0' '$r')\n";
	return $r0;
}

sub sync { tied (%{$_[0]})->sync }

package ZZPerlDBServ;
use base ZZPerlServ;
use DB_File;

sub _init_zzspace {
	my($this) = @_;
	my $db = $this->{DB};
	$db->{1} = "Home";
	$db->{Newno} = 42;
	$db->{Inited} = 1;
}

sub _c__reallynew {
	my($this) = @_;
	my $n = $this->{DB}->{Newno}++;
	$db->{$n} = "";
	return $n;
}

sub new {
	my($type, $id, $port, $dbf) = @_;
	my $this = $type->SUPER::new($id,$port);
	my %h;
	tie %h, DB_File, $dbf, &O_RDWR|&O_CREAT, 0640, $DB_HASH;
	$this->{DB} = \%h;
	my %h2;
	tie %h2, ZZPHash, $this->{DB};
	$this->{DB} = \%h2;
	$this->_init_zzspace() unless $this->{DB}{Inited};

	Event->timer(
		interval => 2,
		cb => sub { $this->sync() }
	);
	return $this;
}

# gethead, sethead not implemented
#
sub _c_get {
	my($this, $sess, $cfrom, $dim, $dir) = @_;
	my $c = $this->{DB}{$cfrom.$dir.$dim};
	$c += 0; # Undef -> 0
	return $c;
}

sub _c_delete {
	my($this, $sess, $cell) = @_;
	my @chg;
	for(@{$this->{Dims}}) {
		my $c;
		push @chg, $c = delete $this->{DB}{$cell."+".$_};
		delete $this->{DB}{$c."-".$_} if $c;
		push @chg, $c = delete $this->{DB}{$cell."-".$_};
		delete $this->{DB}{$c."+".$_} if $c;
	}
	push @chg;
	$this->_chg(\@chg);
	$this->_del($cell);
}
sub _c_connect {
	my($this, $sess, $cfrom, $dim, $cto) = @_;
	my @chg;
	print "C_CONN $cfrom $dim $cto\n";
	my $c;
	push @chg, $c = delete $this->{DB}{$cfrom."+".$dim};
	delete $this->{DB}{$c."-".$dim} if $c;
	push @chg, $c = delete $this->{DB}{$cto."-".$dim};
	delete $this->{DB}{$c."+".$dim} if $c;
	$this->{DB}{$cfrom."+".$dim} = $cto;
	$this->{DB}{$cto."-".$dim} = $cfrom;
	$this->_chg(@chg, $cfrom, $cto);
}
sub _c_disconnect {
	my($this, $sess, $cfrom, $dim, $dir) = @_;
	print "C_DISCONN $cfrom $dim $dir\n";
	my $c = delete $this->{DB}{$cfrom.$dir.$dim};
	return unless $c;
	delete $this->{DB}{$c.(ZZPerlServ::oppdir($dir)).$dim};
	$this->_chg($cfrom, $c);
}
sub _c_gettext {
	my($this, $sess, $cell) = @_;
	my $v = $this->{DB}{$cell};
	if(!defined $v) {
		return "" if exists $this->{DB}{$cell};
		return undef;
	}
	return $v;
}
sub _c_settext {
	my($this, $sess, $cell, $text) = @_;
	$this->{DB}{$cell} = $text;
	$this->_chg($cell);
}

sub sync { my($this) = @_; (tied %{$this->{DB}})->sync }

1;
