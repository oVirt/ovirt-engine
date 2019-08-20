#!/bin/sh -x

SRCDIR="$(dirname "$0")/.."

cd "${SRCDIR}"

images="$(find frontend packaging -iname '*.jpg' ! -ipath 'frontend/brands/*/bundled/*')"

if [ -z "${images}" ]; then
	ret=0
else
	echo "ERROR: jpg images are not allowed" >&2
	echo "${images}"
	ret=1
fi

exit ${ret}
