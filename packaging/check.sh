#!/bin/sh

BASE="$(dirname "$0")"

for d in setup services; do
	find "${BASE}/${d}" -name '*.py' | xargs pyflakes
	find "${BASE}/${d}" -name '*.py' | xargs pep8
done
