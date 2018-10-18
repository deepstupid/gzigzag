#!/bin/sh

PREFIX="temp"

rm -Rf $PREFIX

for f in `find . -name "*.java" `; do
        DIR=`perl -e 'while (<>) { if (/^\s*package\s+([a-zA-Z.]*)\s*;/) { print "$1\n"; } }' $f | tr '.' '/'`
        mkdir -p $PREFIX/$DIR
        ln  $f $PREFIX/$DIR
done


ln -s $PREFIX/org/ .

