#   
#    PerlServImpl.pm
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


FOR A MODEL ONLY - COPY & IMPLEMENT

# ref & lock handled by the sessions...

# gethead, sethead not implemented
#
sub _c_get {
	my($this, $sess, $cfrom, $dim, $dir) = @_;
}
sub _c_new {
	my($this, $sess, $cfrom, $dim, $dir) = @_;
}
sub _c_delete {
	my($this, $sess, $cell) = @_;
}
sub _c_connect {
	my($this, $sess, $cfrom, $dim, $cto) = @_;
}
sub _c_insert {
	my($this, $sess, $cfrom, $dim, $dir, $cto) = @_;
}
sub _c_disconnect {
	my($this, $sess, $cfrom, $dim, $dir) = @_;
}
sub _c_gettext {
	my($this, $sess, $cell) = @_;
}
sub _c_settext {
	my($this, $sess, $cell) = @_;
}
sub _c_ref {
	my($this, $cell, $ref) = @_;
}
sub _c_lock {
	my($this, $cell, $lock) = @_;
}
sub _c_unlock {
	my($this, $cell, $lock) = @_;
}
sub _c_execute {
	my($this, $script, $obj, $ctrl, $view) = @_;
}


