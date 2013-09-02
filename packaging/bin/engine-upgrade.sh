#!/bin/sh

cat >&2 <<__EOF__
$(basename "$0") is obsoleted by engine-setup.
please run engine-setup for upgrading product or update settings.
__EOF__

exit 1
