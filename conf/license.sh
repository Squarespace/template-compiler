#!/bin/bash

CWD=$(cd `dirname $0`; pwd)
GIT=$CWD/..

for f in `find $GIT/src -name '*.java'` ; do
    if ! grep -q '* Licensed under the Apache License' $f ; then
        cat $CWD/apache-2.0-header.txt $f >$f.new && mv $f.new $f
    fi
done

