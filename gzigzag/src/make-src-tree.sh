#!/bin/sh

if [ -z "$1" ]; then
        echo "Location for source tree needed!"
        exit 10
fi

rm -Rf $1

for f in `find . -name "*.java" `; do
        DIR=`perl -e 'while (<>) { if (/^\s*package\s+([a-zA-Z.]*)\s*;/) { print
 "$1\n"; } }' $f | tr '.' '/'`
        mkdir -p $1/$DIR
        cp $f $1/$DIR
done

perl make-packages.pl $1 packages.html
