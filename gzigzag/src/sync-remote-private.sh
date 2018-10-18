#!/bin/sh -v

set -e

TMP=privatecopy

cleanup() {
	ssh gzzpriv@himalia.it.jyu.fi "sh -vc 'chmod 700 .lock ; rm -f .lock'"
}

if [ "$1" = "cleanup" ]; then cleanup; exit; fi

MS="../../Z"
MSREMOTE="gzz-base"
if [ "$1" != "" ]; then MS="$1"; fi
if [ "$2" != "" ]; then MSREMOTE="$2"; fi

mkdir -p $TMP/$MSREMOTE

echo -e "Trying to lock... "
ssh gzzpriv@himalia.it.jyu.fi lockfile -r2 .lock || (rmdir $TMP/$MSREMOTE; exit 1)
trap cleanup EXIT
echo "Success."
rsync --rsh=ssh -rtvz gzzpriv@himalia.it.jyu.fi:$MSREMOTE/ $TMP/$MSREMOTE/
${JAVA:-java} org.gzigzag.impl.Synch -dir $MS -dir $TMP/$MSREMOTE
rsync --rsh=ssh -rtvz $TMP/$MSREMOTE/ gzzpriv@himalia.it.jyu.fi:$MSREMOTE/
