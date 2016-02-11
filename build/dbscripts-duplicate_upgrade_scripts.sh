#!/bin/sh -x

OUT="$(find "$(dirname "$0")/../packaging/dbscripts" -exec basename {} \; | grep -P '^\d{2}_\d{2}_\d{2,8}' -o | sort | uniq -d)"

if [ -n "${OUT}" ]; then
	echo "Found duplicate upgrade scripts with version $(echo ${OUT}), please resolve and retry" >&2
	exit 1
fi

exit 0
