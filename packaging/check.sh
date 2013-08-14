#!/bin/sh

BASE="$(dirname "$0")"

FILES="$(
	find "${BASE}" -name '*.py' | while read f; do
		[ -e "${f}.in" ] || echo "${f}"
	done
)"

ret=0
pyflakes ${FILES} || ret=1
pep8 ${FILES} || ret=1

exit "${ret}"
