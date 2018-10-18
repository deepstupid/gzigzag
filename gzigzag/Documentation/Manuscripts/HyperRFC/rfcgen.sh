#!/bin/sh
for i in *.css
do
    j=$(basename $i .css)
    sed "s/DEMOSHEET/$i/" < rfc1034.html > rfc1034-$j.html
done