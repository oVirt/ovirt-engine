#!/bin/sh

cd "$(dirname "$0")/../../packaging/dbscripts"

OUT="$(ls upgrade | grep -P '\d{2}_\d{2}.\d{2,8}' -o | uniq -d)"

if [ -n "${OUT}" ]; then
	echo "Found duplicate upgrade scripts with version $(echo ${OUT}), please resolve and retry" >&2
	exit 1
fi

exit 0
