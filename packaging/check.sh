#!/bin/sh

BASE="$(dirname "$0")"

for d in setup services pythonlib; do
	find "${BASE}/${d}" -name '*.py' -not -name config.py | xargs pyflakes
	find "${BASE}/${d}" -name '*.py' -not -name config.py | xargs pep8
done
