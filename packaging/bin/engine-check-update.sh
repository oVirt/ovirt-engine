#!/bin/sh

cat >&2 <<__EOF__
$(basename "$0") is obsoleted by engine-upgrade-check.
please run engine-upgrade-check for upgrading product or update settings.
__EOF__

exit 1
