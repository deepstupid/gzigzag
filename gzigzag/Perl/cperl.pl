#   
#    cperl.pl
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



use lib ".";
use ZZPerlSimpleClient;
zzconnect('localhost:3546', foo);

print get_text(1),"\n";
$x = new_cell(1,"d.1", "+");
$y = get_cell($x,"d.1", "-");
$z = get_cell(1,"d.1", "+");
print "GOT: '$x' '$y' '$z'\n";
set_text($x,"FOOFOO");

print "DONE!\n";

