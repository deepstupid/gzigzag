#
# $Id: ZZClangOps.pl,v 1.5 2000/11/03 08:25:16 ajk Exp $
#
# Generate ZZClangOps.java for clang operations

$file = $ARGV[0];
$file = "ZZClangOps.java" if ($file eq "");
open F, ">$file" or die "Can't open file $file";
$rcsid = '$Id: ZZClangOps.pl,v 1.5 2000/11/03 08:25:16 ajk Exp $';
print F <<END;
/*   
ZZClangOps.java
 *
 * GENERATED USING ZZClangObs.pl - DO NOT MODIFY THE JAVA CODE:
 * YOUR CHANGES WILL BE OVERWRITTEN. MODIFY THE GENERATING
 * PERL CODE IN ZZClangObs.pl instead.
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
 *
 *    You may use and distribute under the terms of either the GNU Lesser
 *    General Public License, either version 2 of the license or,
 *    at your choice, any later version. Alternatively, you may use and
 *    distribute under the terms of the XPL.
 *
 *    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
 *    the licenses.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
 *    file for more details.
 *
 */
/*
 * Written by Tuomas Lukka
 */
package org.gzigzag.clang;
import org.gzigzag.*;
import java.util.*;

/** Some primitive operations for Clang.
 */

public class ZZClangOps {
public static final String rcsid = "$rcsid";
    public static final boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }

    /** Convert a cell to a dimension. */
    static final String d(ZZCell c) { return c.getText(); }

END
		    
$exec = " public void exec(ZZCell c, ZZCursor crs, ZZClangContext ctxt) ";

for(
# 
# First, operators without side effects, operating just on the given
# cursor
#
[	Step,
    2, 
    q{dim n},
    q%
	int n = Integer.parseInt($P(n).getText());
	String dim = d($P(dim));
	ZZCell it = $CURS.get();
	it = it.s(dim, n);
	$CURS.set(it);
    %,
    q{steps the cursor n steps (positive or negative) along a dim. },
], 
[	Set,
    2,
    q{cell},
    q%
	$CURS.set($P(cell));
    %, 
    q{set the cursor cursor to point to cell cell. },
],
# [	Deref,
#     0,
#     q{},
#     q%
# 	ZZCursor curs = ctxt.getCursor();
# 	curs.set(ZZDefaultSpace.getCursor(curs.get()));
#     %,
#     q{dereference a cursor. },
# ],
[	Connect,
    3,
    q{cell0 dim cell1},
    q%
	$P(cell0).connect(d($P(dim)), $P(cell1));
    %,
    q{connect two cells along dim. },
],
[	Connect2,
    3,
    q{dim cell1},
    q%
	$CURS.get().connect(d($P(dim)), $P(cell1));
    %,
    q{connect the cursor to cells along dim. },
],
) {
    $lcname = lc $_->[0];
    @pars = split ' ',$_->[2];
    $npars = @pars;
    my $ind = 1;
    my %pars = map {($_ => $ind++)} @pars;

    $_->[3] =~ s/\$P\((.*?)\)/(ctxt.paramAsCell(c.s("d.1", $pars{$1}, null)))/g;
    $_->[3] =~ s/\$CURS/ crs /g;

    $classes .= "
	/** $_->[2] */
	static class $_->[0] implements ZZClangOp {
	    public String name() { return \"$lcname\"; }
	    $exec 
	    {
		/* if(params.length != $npars)
		    throw new ZZError(\"Wrong number of params for $lcname, got \"+
			params.length+\", was expecting $npars\");
		*/
		$_->[3]
	    }
	}
    ";
    push @arrays, " new $_->[0]() ";
}

$ja = join ",\n",@arrays;

print F <<END;
    $classes
    
    public static ZZClangOp[] getOps1() {
	return new ZZClangOp[] {
	    $ja
	};
    }
}
END
