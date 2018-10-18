#   
#    ZZPerlServ.pm
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




package ZZPerlServ;
use Event;
use IO::Socket;

sub oppdir {
	return "-" if $_[0] eq "+";
	return "+" if $_[0] eq "-";
	die("INVALID DIR!");
}

sub new {
	my($type, $id, $port) = @_;
	my $fh = IO::Socket::INET->new(
		Proto => 'tcp',
		LocalPort => $port,
		Listen => SOMAXCONN,
		Reuse => 1
	);
	my $this = bless {
		Id => $id,
		ConnId => 42,
		FH => $fh,
	}, $type;
	Event->io(
		fd => $fh,
		poll => 're',
		cb => sub {
			my $cfh = $fh->accept() 
				or die("Couldn't set up client");
			# XXX autoflush?
			$this->newconnection($cfh);
		}
	);
	Event->idle(
		max => 2,
		cb => sub {
			$this->flush_changes();
		}
	);
	return $this;
}

sub newconnection {
	my($this, $fh) = @_;
	my $c = bless {
		FH => $fh,
		S => $this,
		Id => $this->{ConnId}++,
	}, ZZPerlConn;
	$this->{Sess}{$c} = $c;
	# XXX Weaken!!!
	$c->start();
}

sub _c_new {
	my($this, $sess, $cfrom, $dim, $dir) = @_;
	my $cur = $this->{DB}{$cfrom.$dir.$dim};
	my $new = $this->_c__reallynew($sess);
	if(!$new) {
		return (0, "Couldn't create");
	}
	if($dir eq "-") {
		($cfrom, $cur) = ($cur, $cfrom);
	}
	# XXX Catch errors
	$this->_c_connect($sess, $cfrom, $dim, $new)
		if $cfrom;
	$this->_c_connect($sess, $new, $dim, $cur)
		if $cur;
	return ($new,"");
}

sub _c_insert {
	my($this, $sess, $cfrom, $dim, $dir, $cto) = @_;
	print "INSERT $cfrom $dim $dir $cto\n";
	my $cm = $this->_c_get($sess, $cto, $dim, "-");
	my $cp = $this->_c_get($sess, $cto, $dim, "+");
	if($cm && $cp) {
		$this->_c_connect($sess, $cm, $dim, $cto);
	} else {
		print "CM: $cm CP: $cp\n";
		$this->_c_disconnect($sess, $cto, $dim, "-") if $cm;
		$this->_c_disconnect($sess, $cto, $dim, "+") if $cp;
	}
	my $c = $this->_c_get($sess, $cfrom, $dim, $dir);
	print "C: $c\n";
	# if($c == $cto) {
	# 	return;
	# }
	if($dir eq "+") {
		$this->_c_connect($sess, $cfrom, $dim, $cto);
		$this->_c_connect($sess, $cto, $dim, $c) if $c;
	} else {
		$this->_c_connect($sess, $cto, $dim, $cfrom);
		$this->_c_connect($sess, $c, $dim, $cto) if $c;
	}
}

sub _c_ref {
	my($this, $sess, $cell, $ref) = @_;
	if(($this->{Fer}{$sess}{$cell} += $ref) <= 0) {
		delete $this->{Fer}{$sess}{$cell};
	}
	if(($this->{Ref}{$cell}{$sess} += $ref) <= 0) {
		delete $this->{Ref}{$cell}{$sess};
		if(!keys %{$this->{Ref}{$cell}}) {
			delete $this->{Ref}{$cell};
		}
	}
}

sub _chg {
	my $this = shift;
	if(ref $_[0]) {
		for(@{$_[0]}) {
			$this->{Chg}{$_} = 1;
		}
	} else {
		for(@_) {
			$this->{Chg}{$_} = 1;
		}
	}
}

sub _del {
	my($this, $cell) = @_;
	for my $sess (keys %{$this->{Ref}{$cell}}) {
		$sess->change("-1 deleted($cell)\n");
	}
	delete $this->{Ref}{$cell};
}

sub flush_changes {
	my($this) = @_;
	print "FLUSH!\n";
	for(keys %{$this->{Chg}}) {
		print "CELL: $_\n";
		for my $sess (keys %{$this->{Ref}{$_}}) {
			print "SESS: $sess CELL: $_\n";
			$this->{Sess}{$sess}->change("-1 changed($_)\n");
		}
	}
	%{$this->{Chg}} = ();
}

# XXX What happens when deleting a cell being waited for locking
sub _c_lock {
	my($this, $sess, $cell, $lock) = @_;
	my $l = {
			Sess => "$sess",
			N => $lock+0,
		};
	if($this->{Lock}{$cell}) {
		if($lock =~ /w/) {
			push @{$this->{Wait}{$cell}},  $l;
			# This will disable the filehandle in select.
			$this->{Waiting}{$sess} = 1;
		} elsif($lock =~ /a/) {
			push @{$this->{Wait}{$cell}},  $l;
		} else {
			#$sess->_send_wouldblock($cell);
		}
	} else {
		$this->{Lock}{$cell} = $l;
		if($lock =~ /a/) {
			$sess->_send_locked($cell);
		}
	}
}

sub del_sess {
	my($this, $sess) = @_;
	# XXX HANDLE LOCKS!
	for(keys %{ $this->{Fer}{$sess} }) {
		delete $this->{Ref}{$_}{$sess};
	}
	delete $this->{Fer}{$sess};
	delete $this->{Sess}{$sess};
}

sub _c_unlock {
	my($this, $sess, $cell, $lock) = @_;
}
sub _c_execute {
	my($this, $sess, $script, $obj, $ctrl, $view) = @_;
}


# Subclasses must implement 
# c_get, c_new, c_delete,...

package ZZPerlConn;

%clcom = (
get => 5,
gethead => 5,
sethead => 3,
new => 5,
delete => 1,
connect => 3,
insert => 4,
disconnect => 3,
gettext => 1,
settext => 2,
ref => 2,
lock => 2,
unlock => 1,
execute => 4,
);
%bincom = map {($_=>1)} qw/settext/;

%excom = (
get => rl,
gethead => rl,
new => rl,
);

%respcom = (
get => cellno,
gethead => cellno,
sethead => ok,
new => cellno,
delete => ok,
connect => ok,
insert => ok,
disconnect => ok,
gettext => text,
settext => ok,
ref => 
);

my $ip = qr/\((.*?)\)/;
my @nip = (
	"",
	qr/$ip/,
	qr/$ip$ip/,
	qr/$ip$ip$ip/,
	qr/$ip$ip$ip$ip/,
	qr/$ip$ip$ip$ip$ip/,
);


use Fcntl;

sub start {
	my($this) = @_;
	$this->{FH}->print("ZZ(0.02)($this->{S}{Id})($this->{Id})\n");
	fcntl($this->{FH}, &O_NONBLOCK, $b);
	$this->{FH}->autoflush(1);
	$this->{State} = "init";
	my $buf = "";
	$this->{Watcher} = Event->io(
		fd => $this->{FH},
		poll => 're',
		cb => sub {
			my $b;
			print "GOTIN\n";
			my $l = $this->{FH}->sysread($b,1024);
			if(!$l) {
				$this->throwout(); return;
			}
			$buf .= $b;
	 	    TRYMORE:
			my $got = 0;
			print "BUF: '$buf'\n";
			if(exists $this->{Binary}) {
				if(length($buf)>= $this->{Binary}) {
					$got=1;
					my $s = substr($buf, 0, $this->{Binary},"");
					print "$this->{Binary} str: '$s'\n";
					$this->binary_input($s);
					delete $this->{Binary};
				}
			} else {
				if($buf =~ /^(.*?)(?:\x0d\x0a?|\x0a\x0d?)/) {
					$got=1;
					my $line = $1;
					chomp $line;
					chomp $line;
					$buf =~ s/^(.*?(?:\x0d\x0a?|\x0a\x0d?))//;
					$this->input($line);
				}
			}
			goto TRYMORE if $got;
		}
	);
}

sub throwout {
	my($this, $msg) = @_;
	print "THROWING OUT $this: $msg\n";
	return unless defined $this->{Watcher};
	$this->{Watcher}->cancel();
	$this->{S}->del_sess($this);
}

sub input {
	my($this, $line) = @_;
	print "IN: '$line'\n";
	for($this->{State}) {
		/normal/ and do {
			# print join ',', map {ord} split '', $line;
			# print "\n";
			$line =~ /^(\d+)\s+(\w+)/ or
 			  die("Illegal line '$line'");
			my $comid = $1;
			my $c = $2;
			if(!$clcom{$c}) {
				$this->throwout("Unknown command '$c'");
				return;
			}
			unless($line =~ /^\d+\s+\w+$nip[$clcom{$c}]$/) {
				$this->throwout("Need $clcom{$c} params for $c");
				return;
			}
			my @pars = map {defined $_ ? $_ : ()} 
				($1, $2, $3, $4, $5);
			if($bincom{$c}) {
				$this->{BC} = $c;
				$this->{BCID} = $comid;
				$this->{BP} = \@pars;
				print "BPARS: @pars\n";
				$this->{Binary} = pop @pars;
				print "Bin: $this->{Binary}\n";
			} else {
				my $m = "_c_$c";
				my @l = $this->{S}->$m($this,@pars);
				my $rc = $respcom{$c};
				if($rc eq "cellno") {
					print "CELL: $l[0]\n";
					if(!$l[0]) {
						# $this->{FH}->print("$comid error(FROM CELLNO $l[1])\n");
						# NO SUCH CELL
						$this->{FH}->print("$comid cellno(0)\n");

						last;
					}
					# assuming ref-lock.
					my $ref = $pars[-2];
					my $lock = $pars[-1];
					print "REF: $ref\n";
					$this->{S}->_c_ref($this,$l[0], $ref)
						if $ref;
					$this->{S}->_c_lock($this,$l[0], $ref)
						if $lock;
					$this->{FH}->print("$comid cellno($l[0])\n");
				} elsif($rc eq "ok") {
					$this->{FH}->print("$comid ok\n");
				} elsif($rc eq "text") {
					if(!defined $l[0]) {
						# $this->{FH}->print("$comid error(no such cell)\n");
						# last;
						$l[0] = "";
					}
					my $len = length $l[0];
					$this->{FH}->print("$comid text($pars[0])($len)\n$l[0]");
				}
				print "REPLIED!\n";
			}
			last;
		};
		/init/ and do {
			unless($line =~ /^ZZ$ip$ip$ip$/) {
				$this->throwout("Invalid startline");
				return;
			}
			$this->{CVer} = $1;
			$this->{CId} = $2;
			$this->{CSess} = $3;
			$this->{State} = normal;
			last;
		};
	}
}

sub change {
	my($this, $msg) = @_;
	$this->{FH}->print($msg);
}

sub binary_input {
	my($this, $data) = @_;
	my $m = "_c_$this->{BC}";
	$this->{S}->$m($this,@{$this->{BP}}, $data);
	print "REPLIEDBIN!\n";
	$this->{FH}->print("$this->{BCID} ok\n");
}


