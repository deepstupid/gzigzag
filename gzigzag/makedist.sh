#!/bin/sh
# makedist.sh
# *    
# *    You may use and distribute under the terms of either the GNU Lesser
# *    General Public License, either version 2 of the license or,
# *    at your choice, any later version. Alternatively, you may use and
# *    distribute under the terms of the XPL.
# *
# *    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
# *    the licenses.
# *
# *    This software is distributed in the hope that it will be useful,
# *    but WITHOUT ANY WARRANTY; without even the implied warranty of
# *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
# *    file for more details.
# *
# *    Written by Antti-Juhani Kaijanaho

set -e

sign=no

cont=true
while $cont
do
    case "$1" in
       -sign) sign=yes
              shift ;;
           *) cont=false ;;
    esac
done

srcdir=${2:-$(pwd)}
distdir=${1:-$(pwd)/gzigzag-dist}
rm -rf $distdir

. makedist.param

if [ "$cvstree" = "yes" ]
then
    make Manifest
else
    if [ ! -f Manifest ]
    then
        echo "Manifest is missing"
        exit 1
    fi
fi

for d in $((for f in $(cat Manifest) ; do dirname $f ; done) | sort | uniq)
do
    echo -n "Making directory $distdir/$d ..."
    mkdir -p $distdir/$d
    echo "done."
done

for f in $(cat Manifest)
do
    echo -n "Copying $f ..."
    ln $f $distdir/$f
    echo "done."
done

cat Manifest | xargs md5sum > $distdir/md5sum

if [ "$sign" = "yes" ]
then
    gpg --clearsign $distdir/md5sum
    mv $distdir/md5sum.asc $distdir/md5sum
fi



