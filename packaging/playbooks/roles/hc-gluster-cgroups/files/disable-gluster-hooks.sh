#!/bin/sh
# Renames the hook scripts. This is a GNU/Linux specific script, uses rename(1)
# to rename the files


HOOKSDIR="/var/lib/glusterd/hooks"

FILES="1/set/post/S30samba-set.sh 1/start/post/S30samba-start.sh \
       1/stop/pre/S30samba-stop.sh 1/reset/post/S31ganesha-reset.sh \
       1/start/post/S31ganesha-start.sh 1/start/post/S29CTDBsetup.sh \
       1/stop/pre/S29CTDB-teardown.sh"

for f in $FILES; do
    for g in $HOOKSDIR/$f;do
        test ! -x $g || rename -v S D $g
    done
done
